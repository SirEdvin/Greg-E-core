# Design

## Context

See `proposal.md` for motivation. Observed state:

- `MEDemandHatchPartMachine` (AE2-guarded, registered in `GregEStuffInit.initGregEParts`, listed in `ModCreativeModTabs`) runs one `beginCraftingCalculation` per paid run and submits with no requester. It finds providers through the ordinary AE2 crafting service, so any network provider works.
- `WorldEaterDemands.pool()` is the authoritative real-run demand list; it is built lazily from registered items.
- GTCEu 7.5.2 `MEPatternBufferPartMachine` extends `MEBusPartMachine`, implements `ICraftingProvider`, and calls `getMainNode().addService(ICraftingProvider.class, this)` in its constructor; `ICraftingProvider.requestUpdate(mainNode)` refreshes the network's pattern list.
- AE2 15.0.18 (`d75ed36f`): `PatternDetailsHelper.encodeProcessingPattern` rejects empty inputs, which is why each pattern costs one Iron Ingot. `ExecutingCraftingJob` re-decodes tasks with `PatternDetailsHelper.decodePattern(key, level)` on CPU load, and `AEPatternDecoder` handles native processing patterns. `CraftingService` registers `CraftingServiceStorage` as a global storage provider that routes network inserts to CPUs' `waitingFor` (`insertIntoCpus`), so storage insertion completes a waiting job.
- `CraftingCpuLogic` pushes at most `coProcessors + 1` patterns per tick per CPU.

## Goals / Non-Goals

**Goals:** smallest provider that reuses GTCEu's grid-node lifecycle and AE2's native pattern format; deterministic 40-tick delivery for tests.

**Non-Goals:** a UI, configurable delay or input, fluids, patterns outside the demand pool, being usable as a tamer part, any change to the ME Demand Hatch.

## Decisions

1. **Base class: `MEBusPartMachine` with the `ICraftingProvider` service.** Same approach as `MEPatternBufferPartMachine`: GTCEu supplies node creation, persistence, power and channel status, and AE cable connection. Alternative: an AE2 `AEBaseBlockEntity` with its own block, which would need a separate Forge block registration path the addon doesn't use. It registers no `PartAbility`, so tamer frames can't accept it.

2. **Native processing patterns, built once.** For each pool entry, `decodePattern(encodeProcessingPattern([1 iron ingot], [1 item]), level)` yields an `AEProcessingPattern`. Build the list lazily on first `getAvailablePatterns()` (the pool needs registries) and cache it in a static. Alternative: a custom `IPatternDetails` with no inputs, rejected because it needs a decoder registration for CPU reloads.

3. **Delivery queue.** `pushPattern` voids the input holder, appends `(outputs, dueTick)` with `dueTick = level.getGameTime() + 40`, and returns true. `isBusy()` always returns false. A server tick subscription, active only while the queue is non-empty, inserts due outputs through `getMainNode().getGrid().getStorageService().getInventory().insert(..., MODULATE, actionSource)` and keeps any uninserted remainder queued. Game time keeps counting while the chunk is unloaded, so a delivery that falls due during the unload goes out on the first tick after reload.

4. **Persistence.** Store the queue in machine NBT as `GenericStack` tags plus the due tick, saved through the machine's existing `saveCustomPersistedData`/`loadCustomPersistedData` (or an equivalent persisted field). No item drops for queued outputs: `clearInventory` is not extended.

5. **Registration.** Follow the hatch: `REGISTRATE.machine(...)` with `langValue`, tooltip stating creative/testing use and the 1-iron / 2-second behavior, a GTCEu ME-style overlay model, `init()` called from `initGregEParts` and creative tab entry both behind `GTCEu.Mods.isAE2Loaded()`. No entry in `GregEGTAddon` recipes.

## Risks / Trade-offs

- [Free item source on servers] → creative inventory only, no recipe, tooltip says it's for testing. Operators control creative access.
- [Delivery rate is capped by the CPU, not the provider] → a demand of N items arrives over roughly N / (coProcessors + 1) ticks after the first 40; tests assert "no earlier than 40 ticks" and completion, not an exact finish tick.
- [Pool changes after the pattern cache is built] → the pool is built once from static registries, so the cache matches it for the session.
- [Pattern-access terminal display of non-item patterns] → not required; crafting and craftability are what matter.
