# Design

## Context

See `proposal.md` for motivation and scope. This design is required because optional AE2 integration crosses machine registration, recipe payment, asynchronous planning, and saved-run state.

Observed project state before implementation:

- `build.gradle:193` pins GTCEu 7.5.2; Minecraft is 1.20.1, Forge 47.4.10, Java 17, and LDLib 1.0.40.b. AE2 is not currently a declared addon dependency.
- `IncompleteWorldEaterTamer.java:94` defines one shared frame predicate accepting independently limited item/fluid I/O parts. Its lower-rear position also accepts an intention hatch. Tier derives from actual casings, not parts.
- `WorldEaterTamerRecipeLogic.java:216` creates a fresh recipe copy, pays the token, then selects the demand. `handleDemand`/`tryPayDemand` at lines 420-442 simulate the entire quantity across attached input handlers before consumption. `clearRun`, reset, and custom persistence already centralize demand lifetime.
- Existing `WorldEaterDemandTests`, `WorldEaterStructureTests`, `WorldEaterUnloadTests`, and client checks cover native startup, split payment, expiry, queued runs, reload, and presentation. They do not exercise AE2 networks.

Version-matched upstream source inspected:

- GTCEu commit `a2f67623feb91103b0491fa9881e8991af7ba1f2` (`v7.5.2-1.20.1`): `gradle/forge.versions.toml` pins AE2 15.0.18. `GTAEMachines.STOCKING_IMPORT_BUS_ME` registers `gtceu:me_stocking_input_bus`, named ME Stocking Input Bus. `MEStockingBusPartMachine` exposes virtual stock and extracts from ME during consumption, rather than retaining a real item buffer. `WorkableMultiblockMachine.onStructureFormed` collects attached parts' recipe handlers independently of their registered part ability.
- AE2 commit `d75ed36f21c8b51e58ddff5a22299e45c19976be` (`forge/v15.0.18`): `ICraftingService` provides asynchronous `beginCraftingCalculation`, `getCpus`, and `submitJob`. `ICraftingCPU.getJobStatus().crafting()` identifies a job's final output. `isRequesting` only covers production AE2 is currently awaiting and explicitly excludes final job output; it cannot be the sole duplicate test. `submitJob` accepts a null requesting machine, avoiding requester-owned links and direct delivery.
- Official dependency coordinate `appeng:appliedenergistics2-forge:15.0.18` is available from ModMaven; its POM was fetched successfully. This is source/dependency inspection, not compilation or gameplay evidence.

## Goals / Non-Goals

**Goals:**

- Keep the controller's paid demand as the only authoritative request, and reuse native input payment and ME security/network behavior.
- Make the single scheduling opportunity survive ordinary reload without creating an AE2 job-management subsystem.
- Keep manual operation and addon loading without AE2 working.

**Non-Goals:**

- Continuous stocking, repeated crafting attempts, job ownership/cancellation/status tracking, crafting CPUs or patterns supplied by the addon, custom network routing, fluids, or demand queues.
- Automating preparation tests, initial surveys, casing recipes, token supply, or reward export. Other existing buses can provide tokens and export rewards.
- Changing deadlines, consequences, reward quantity, casing progression, native AE2 recipe batch sizes, or other multiblocks' compatibility.
- Mixins, reflection into AE2 job internals, a generic scheduling framework, or unrelated refactors.

## Decisions

### 1. Reuse the stocking bus connection and virtual input path

Add a dedicated endgame part using the pinned `MEStockingBusPartMachine` as the starting reuse point, with one controller-managed request exposed in a read-only UI. Do not retain the parent's freely editable stock list, auto-pull mode, configuration copying, or circuit controls as alternate ways to change this request. A requested item shown in the hatch is a ghost configuration, not a fabricated physical item.

Refresh matching stock while a valid real demand is pending and online; feed it into the existing complete-demand simulation/payment path. Refresh before the payment check rather than relying on a stale native display cache at the deadline. Perform extraction only when the full demand can be paid, so interruption leaves unpaid items in their original inventories. Clear the virtual configuration immediately after payment, including while the earned reward is blocked.

For the one startup calculation, determine the unfulfilled quantity after counting matching attached supply and extractable ME stock once. A network-backed quantity exposed through another stocking bus is not a second copy of that inventory. Preserve native duplicate-stock protections and add an aliasing regression; do not introduce a second independent payment path or compensate for shortages by partially consuming inputs. If a small shared remainder calculation is needed, keep it local to demand payment rather than building a general inventory abstraction.

Alternative rejected: a physical ME input buffer would need return/drop handling on cancellation and removal and could strand or lose unpaid items. An entirely custom AE2 node would duplicate the existing connection lifecycle.

### 2. Give the hatch an independent structure allowance

Register a dedicated addon part ability, not an unrestricted ordinary import/export ability. Add it to the shared frame predicate with a global maximum of one. Its item recipe handler still participates in native input aggregation. This prevents the ordinary five-input branch from accidentally accepting a second demand hatch and avoids changing other machines' accepted parts.

Planning default: its allowance is separate from the existing five-per-I/O-category limits, like other specialized parts. The hatch can occupy any existing frame I/O location, including the rear-center slot if no intention hatch occupies it; doing so does not satisfy the real run's separate intention requirement. Interior/face-panel blocks do not attach. It contributes no casing tier and does not select intention.

Use a single controller attachment for this demand-controlled part: reject sharing the same hatch between two formed tamers rather than multiplexing requests or choosing an owner implicitly. This is a narrow ownership safeguard, not multi-network support.

### 3. The scheduling opportunity belongs to paid startup

After successful real-run token payment and demand selection, consume the run's scheduling opportunity before any external operation. Candidate checks, failed starts, preparation tests, and old loaded demands never initiate it.

```text
new paid real demand
    |
    +--> mark scheduling considered for this run
    +--> mirror demand to the optional attached hatch
    +--> sufficient available supply? ----------> monitor stock only
    +--> absent/offline/unusable hatch? --------> monitor stock only
    +--> matching scheduled production? --------> monitor stock only
    +--> otherwise start one missing-quantity calculation
              |
              +--> invalid/failed/stale ----------> monitor stock only
              +--> valid plan; guarded submission once
                         |
                         +--> success or failure -> monitor stock only
```

There is no retry timer or deferred first attempt after reconnection. Missing patterns/materials, busy or unsuitable CPUs, failed submission, another job's later cancellation, or stock disappearing after the startup decision do not reopen the opportunity. A genuinely new paid recipe gets its own opportunity, even when its randomly selected item is identical to the previous run's.

### 4. Use public job information and recheck before submission

On the connected grid, compare the exact demanded AE item key against active CPUs' final requested outputs, and also suppress a duplicate when AE2 reports it is already awaiting matching production. Any matching scheduled job suppresses a new one regardless of quantity or requester. Jobs on a different grid do not. Inspecting a player's unsubmitted crafting preview is not supported by the public scheduled-job API and is not treated as a scheduled job.

A calculation future is pending work from the initial opportunity, not a running AE2 job. Never block the server waiting for it. When ready, perform submission on the server thread only while its original paid run, original attached hatch, and original grid remain valid. Recheck supply and matching jobs immediately before submission: skip if supply is now sufficient or matching production appeared while calculation ran. This is a guard on the original attempt, not permission to calculate or submit again.

Bind pending work to the actual fresh run instance/lifecycle, not just item identity: two successive demands can be identical. Discard stale or unloaded calculations without replay. If supply has partially changed, do not restart planning; at most the original startup deficit is requested, with ordinary AE2 batch surplus left in storage. No network search, recalculation, or job polling continues once the initial opportunity settles.

Alternative rejected: using `isRequesting` alone misses scheduled final products; keeping a custom global job registry adds ownership and reconciliation that the user explicitly does not want.

### 5. Submit as an ordinary storage-bound craft, then forget it

Use the native machine action source and submit with no requesting machine and no explicitly selected CPU. Let AE2 select an eligible CPU under its normal policy. Do not implement `ICraftingRequester`, keep a job link, persist a job id, or monitor completion/failure. A fired job remains AE2's work even after manual payment, a new demand, timeout, frame break, hatch removal, or unload.

Only the hatch's current request is cleared. Matching items subsequently arriving in ordinary ME storage can satisfy an active demand through normal payment; late items do not pay an ended run, produce an extra reward, or recreate its request. Other jobs may send their output to another requester, which is an accepted consequence of the user's unconditional duplicate-suppression rule.

### 6. Persist the decision, not asynchronous or network ownership

Add minimal controller-owned saved bookkeeping to the existing demand data indicating that the scheduling opportunity was considered, including deliberate skips and failures. Mark it before launching a calculation. Reconstruct the read-only hatch request from the controller after attachment/reload; never restore a free-standing hatch request as authority.

Pending calculation futures are transient. Save/load or real chunk eviction abandons an unfinished calculation and does not create a replacement; a submitted job continues independently. An absent or invalid automation-only marker in an otherwise valid legacy run is treated conservatively as already considered, without cancelling that valid demand or revoking progression. New paid startup is the only path that opens a fresh opportunity. Existing invalid core demand data still follows existing harmless cancellation rules.

Alternative rejected: persisting and reconnecting crafting links contradicts fire-and-forget ownership and is unnecessary for storage polling.

### 7. Keep acquisition and optional integration native

Use one vanilla shapeless recipe, emitted through the existing recipe provider, consuming `GTAEMachines.STOCKING_IMPORT_BUS_ME` and four normal ingots obtained through `ChemicalHelper` for `GreggyItems.TAMED_WORLD_EATER_ESSENSE`. Produce one `gregecore:me_demand_hatch`. Do not guess the generated ingot item id or introduce substitute/tag-wide essence ingredients, a machine recipe, research, fluids, or power.

Pin compile/development integration to AE2 Forge 15.0.18, the version used by the pinned GTCEu release, without changing Minecraft/Forge/GTCEu/LDLib. Keep production AE2 optional: gate hatch registration, creative exposure, frame predicate, recipe publication, and AE2-specific fixtures before resolving optional classes. Add matching optional loader metadata. Provide an isolated development/test runtime switch for the no-AE2 check rather than changing the user's installed mods. Keep common demand logic's externally visible signatures free of AE2 types.

Reuse the current native tiered part model and formed casing appearance, with a distinguishable demand overlay and readable name. Add read-only demand/quantity and ME connection state plus concise tooltips explaining one-shot behavior and failure risk. Do not add a job dashboard or configurable modes.

## Risks / Trade-offs

- [One opportunity can be missed while the network is offline, ingredients unavailable, or all CPUs busy] -> Document the deliberate no-retry policy; preserve manual fulfillment and unchanged timeout consequences.
- [An existing job is too small or delivers to another requester] -> Suppress duplicates exactly as requested; do not promise reservation or guaranteed fulfillment.
- [Calculation completes after run replacement or a grid change] -> Bind transient work to the original lifecycle and revalidate immediately before the single submission.
- [Partial new supply arrives during planning, or AE2 crafts in batches] -> Do not recalculate; consume only the tamer's exact demand and leave extra products in ME.
- [Several input handlers expose the same ME inventory] -> Preserve duplicate-stock safeguards and test actual network extraction, split local/ME supply, and distinct-bus configurations; never validate only cached counts.
- [Optional AE2 types leak into common registration or test discovery] -> Exercise actual no-AE2 startup and existing non-AE2 fixtures, not only conditional recipe visibility.
- [Current canonical casing prose says formed parts stay black, while current controller code synchronizes the dominant casing color] -> Do not resolve that unrelated discrepancy here; the new hatch follows the current controller-provided native appearance.
- [Simulation is not a distributed transaction against arbitrary third-party handlers or sudden process termination] -> Preserve native server-thread payment guarantees, test ordinary persistence/lifecycle paths, and do not claim crash-atomic inventory/job commits.

## Migration Plan

No existing block, material, recipe id, or structure layout is removed. Existing worlds without the hatch continue unchanged; legacy running demands gain no retrospective crafting attempt. Test both AE2-present and AE2-absent startup before distributing the implementation.

Removing the new implementation or AE2 after players place the hatch can leave missing blocks/items and invalidate frames; back up worlds and remove/recover new hatches before such a downgrade. The core mod's continued ability to load without AE2 is not a guarantee that AE2 blocks survive uninstalling AE2.

Implemented on the existing branch through the requested apply workflow. See `verification.md` for executed build, dedicated-server, optional-dependency and native-client evidence. Canonical specs remain unchanged until explicit synchronization or archival.
