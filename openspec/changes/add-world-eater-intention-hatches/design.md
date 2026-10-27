# Design

## Context

See `proposal.md` for motivation and the two delta specs for the behavioral contract.

The project is pinned to Minecraft 1.20.1, Forge 47.4.10, GTCEu 7.5.2, and Java 17. `IncompleteWorldEaterTamer` already uses an orientation-relative five-aisle edge frame, a shared casing/part predicate, and native formation callbacks. Its casing tier is derived from validated structure data, while DORMANT/ACTIVE progression lives independently in `WorldEaterTamerRecipeLogic`.

Machine parts register through `GregEStuffInit.initGregEParts()` before multiblocks. `ThreadT1PartMachine` is an existing non-inventory tiered-part registration example; do not copy its unrelated UI. `ModCreativeModTabs` explicitly lists machine items, and `GregEGTAddon.addRecipes` is the existing recipe entry point. The repository has no existing custom ingredient serializer.

Pinned upstream source inspection established the relevant constraints:

- `GTRecipeTypes.ASSEMBLY_LINE_RECIPES` has sixteen item inputs, one item output, and four fluid inputs. `GTValues.VA[GTValues.LuV]` is 30,720 EU/t; 60 seconds is 1,200 ticks.
- `AssemblyLineMachine.checkItemInputs` associates each recipe input with the first nonempty stack of its corresponding ordered input handler. `consumeItemContents` simulates all corresponding payments before executing them.
- GTCEu's `ItemRecipeCapability` supports custom ingredient lookup through `CustomMapIngredient`, which retains the ingredient predicate and hashes candidate items independently of their NBT. A representative written-book stack is therefore necessary for lookup and recipe-viewer display; its metadata must not become an acceptance constraint.
- Forge 47.4.10 supplies `AbstractIngredient`, `IIngredientSerializer`, and `CraftingHelper.register`. GTCEu's circuit ingredient demonstrates custom matching and JSON/network serialization. Strict or partial NBT matching does not express decoded first-page text independently of serialization and other metadata.
- Assembly Line research generation iterates explicitly configured research entries. Safety adds none; verify ordinary recipe lookup with research enabled rather than assuming a lack of research entries alone proves runtime discoverability.

All sixteen selected LuV machine registrations and their item assets were checked against the pinned GTCEu source/JAR. This is source inspection, not a successful native crafting run.

## Goals / Non-Goals

**Goals:**
- Reuse the existing registration, part, pattern, ingredient, recipe, and rendering mechanisms with a small additive change.
- Represent intention as installed hardware, independent of persisted progression and casing tier.
- Support independently authored books through real tamer lookup, one-book payment, state/tier validation, serialization, and display, not just a standalone predicate test.

**Non-Goals:**
- No failure framework, recipe-condition framework, configuration surface, mode-switch screen, or ticking hatch behavior.
- No global changes to ingredient lookup, Assembly Line slot limits, input ordering, research settings, or overclocking.
- No new libraries, mixins, access transformers, or edits to existing tamer recipe logic merely to prepare future effects. Extend it only for the requested Bravery craft.

## Decisions

### One small part implementation with two fixed identities

Add a proposed `IntentionHatchPartMachine` under the existing `GregMachines/parts/endgame` package, using the native non-inventory part base and two machine definitions: `gregecore:safety_intention_hatch` and `gregecore:bravery_intention_hatch`. Store the fixed variant as constructor/definition data, not mutable saved mode. Use one intention part ability, exposed only where the tamer pattern accepts it. Wire initialization and creative entries through the existing registries.

Reuse the native part appearance pipeline and black tamer casing. Use distinct lightweight front overlays and localized Safety Hatch/Bravery Hatch labels; exact artwork is an implementation detail, not a new art system. Tooltips explain the optional placement and future-policy purpose without promising protection that does not exist yet.

Two duplicated part classes, inventory-bearing bus subclasses, or a configurable hatch introduce behavior and maintenance cost without meeting any additional requirement.

### Reserve one pattern coordinate while retaining the existing predicate

Name the center character in the final aisle's bottom row separately from the other frame positions. Its relative coordinate is four blocks behind the controller with no lateral or vertical displacement. Keep the current casing/standard-part predicate shared between ordinary frame positions and this special position, then add the intention-hatch alternative only to the special position.

Do not construct independent item/fluid/parallel limit predicates for the special position: the limits must count parts globally across both kinds of position. Do not add intention hatches to the general frame predicate. A single eligible coordinate inherently limits attached intention hatches to one across both variants. Unrestricted face/interior positions remain unrestricted and do not attach extra intention parts.

A broad frame allowance plus a separate world scan would duplicate native validation and risk accepting a misplaced or stale hatch. Replacing the rear predicate with only casing-or-intention would unnecessarily invalidate existing rear item/fluid/parallel installations.

### Derive a small three-way selection from formed parts

Expose a read-only controller selection with `NONE`, `SAFETY`, and `BRAVERY`. Derive it from the attached intention part only while the controller is formed and its native structure state is valid; otherwise return `NONE`. Prefer reading the native attached-part list instead of adding another persisted field or world scan. Reformation refreshes attachments, so removal, replacement, and reload naturally follow current hardware.

`NONE` means no declared intention. This change does not decide which consequence a future recipe should choose in that case. Existing recipes do not consult the selection, and hatch appearance contributes nothing to casing tier. No new promise is made about unfinished recipe recovery across a structural break.

### Use machine-specific acquisition recipes

Register both recipes through `GregEGTAddon.addRecipes`, using addon-owned builder IDs and `.duration(1200)`. Safety uses the Assembly Line and `.EUt(VA[LuV])`; Bravery uses `WORLD_EATER_TAMER` with no energy capability. Add no research entries, research condition, fluid payment, circuit, or extra item. Keep Safety's native Assembly Line overclock behavior; its 60 seconds is the unmodified base duration.

Safety has exactly sixteen input entries in the spec's listed order, each containing eight of the explicitly selected LuV machine definition. Use a fixed list, not registry enumeration or a machine tag that admits arbitrary substitutes.

Bravery has one custom ingredient entry requiring one qualifying written book, with `required_state=ACTIVE`, `required_casing_tier=2`, and a dedicated ordinary-crafting operation. Extend the tamer's existing acceptance boundary to require the validated casing tier before payment. Reuse its ordinary processing, saved-work, and blocked-output path without initializing a scan or changing progression. Preserve existing casing-only parallel modifiers and survey restrictions; this request does not expand parallel support to another operation.

The former eight-input Assembly Line design is replaced, not retained as an alternate recipe. One tamer input removes the repeated-input lookup issue without an indexing mixin. Manual inventory extraction, a global lookup change, or a generic recipe-condition framework is unnecessary.

### Add one serializable, non-simple acknowledgement ingredient

Add a narrowly scoped proposed `BraveryBookIngredient` beside the addon's recipe integration code. It accepts only vanilla signed written books with a valid first-page component whose decoded text exactly equals `I know what I am doing`. Check data types and normal book/page bounds, parse without executing or resolving interactive content, catch malformed data, and return false without changing the input. Do not trim, case-fold, compare raw serialized JSON, inspect later pages for acceptance, or compare title/author/copy metadata.

Use Forge's non-simple custom ingredient extension with one addon-owned serializer ID. Keep the acknowledgement text fixed rather than designing a generic book-expression language. Supply a valid example signed book for candidate lookup and display, while ignoring its example title/author in `test`. Implement JSON and network round-trips and register the serializer on both logical sides through the existing common setup lifecycle before recipes load. Keep registration in `GregECore`; do not introduce another initialization framework.

A plain written-book ingredient accepts invalid acknowledgements. An exact NBT ingredient rejects valid titles, authors, copies, or alternate component serialization. A recipe condition inspecting nearby inventories would bypass the actual ingredient/payment boundary. The custom ingredient is the smallest mechanism that preserves the agreed semantics through recipe reload and client synchronization.

### Extend existing tests and presentation instead of creating a separate harness

Extend `WorldEaterStructureTests` and existing run/casing fixtures for placement, global part limits, casing-tier exclusion, invalidation/reformation, reload, and unchanged progression/processing. Keep existing no-hatch fixtures valid instead of silently installing a hatch everywhere. Exercise native block-change callbacks as well as direct pattern checks.

Add focused ingredient, native Safety Assembly Line, and native Bravery tamer GameTests. A correct `test()` result alone cannot prove recipe discoverability or payment. Safety uses normal ordered buses, power, outputs, and research enabled without a data item. Bravery uses normal tamer buses without power and covers both progression states and both casing tiers, unvalidated structures, authored books and copies, wrong or malformed pages, mixed-validity inputs, exact one-book payment, surplus retention, blocked outputs, save/load, and revalidation. Keep wrong Safety tiers, insufficient quantities, and unchanged inventories on rejection. Verify serializer round-trips, actual recipe reload, and representative candidate stacks.

Extend the existing test-only client/JEI bridge and structure-preview checks. Open Safety in the Assembly Line category and Bravery in the tamer category. Inspect counts/order, duration, Safety power and Bravery's power-free ACTIVE/tier-2 requirements. Make the first-page acknowledgement visible through the example book tooltip. Verify names, variant overlays, item/placed/formed appearance, and the special rear-slot alternatives. Use the existing isolated verification world convention rather than a player's world.

## Risks / Trade-offs

- Metadata-sensitive book matching may work in isolation but disappear from native recipe search. Mitigation: use the custom-ingredient lookup path and require real recipe discovery, payment, reload, and client-sync tests.
- The existing ACTIVE logic accepts only casing conversion. Mitigation: explicitly route Bravery through the ordinary-craft lifecycle and test lookup, state/tier rejection before payment, completion, blocked output, and save/load without survey side effects.
- A malformed item can reach server recipe matching. Mitigation: read-only, bounded decoding that rejects malformed data without exceptions or side effects.
- Separately constructed predicates can split global hatch limits. Mitigation: reuse the same limited predicates and test limits with the rear slot occupied by each previously permitted part type.
- Adding a new rear pattern symbol can break existing preview fixtures that assumed one shared casing predicate. Mitigation: adjust selectors by position/meaning and retain both old rear substitutions and new hatch candidates.
- No hatch has intentionally unspecified future consequence behavior. Mitigation: expose `NONE` without mapping it to a policy, and leave every current recipe unchanged.
- New machine IDs become saved-world content once implemented. Mitigation: do not rename existing IDs, and back up worlds before downgrading after new hatches have been placed.

## Migration Plan

Add registrations, assets, recipes, matching, and tests without changing the existing controller ID or its saved progression schema. Existing hatch-free structures remain valid, including those with ordinary parts at the rear-center position; no world migration is needed. Remove the unshipped Assembly Line Bravery registration in favor of the tamer recipe.

Verify production and test compilation, datagen and packaged resources, fresh native GameTest reports, and the isolated client recipe/structure presentation before delivery. Run Gradle with `--no-daemon` and discover available tasks before invoking them. Rollback before new items are used is code-only; after use, restore a world backup or remove/recover new content deliberately before downgrading. Do not imply that removing the addon can preserve its new block/item IDs.
