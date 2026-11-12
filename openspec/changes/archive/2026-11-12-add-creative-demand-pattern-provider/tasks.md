# Tasks

Work on a new branch off the current branch. Endgame parts live under `src/main/java/net/cu5tmtp/GregECore/gregstuff/GregMachines/parts/endgame/`; tests extend the existing AE2 fixtures in `src/gameTest/java/net/cu5tmtp/GregECore/gametest/` (reuse `WorldEaterMETests.fixture`/`awaitNetwork`). Run Gradle with `--no-daemon` and reproduce single tests with `-PgameTests=<method>`.

## 1. Creative-only registration

- [x] 1.1 Register `gregecore:creative_demand_pattern_provider` (Creative Demand Pattern Provider) as an `MEBusPartMachine` subclass with the `ICraftingProvider` service, no `PartAbility`, a tooltip stating testing use / 1 Iron Ingot / 2 s delivery, and an ME-style overlay model; call `init()` from `GregEStuffInit.initGregEParts` and add the creative tab entry, both behind `GTCEu.Mods.isAE2Loaded()`, with no recipe in `GregEGTAddon`. Verify with `compileJava compileGameTestJava`, a registration GameTest in `WorldEaterMERegistrationTests` (registry id, name, creative listing, no recipe produces it, no missing model), and the existing no-AE2 loading check still passing.

## 2. Patterns and delayed delivery

- [x] 2.1 Expose one native processing pattern per `WorldEaterDemands.pool()` entry (1 Iron Ingot to 1 item), built lazily and cached, and request a provider update when the node comes online. Verify with a GameTest that every pool item is `isCraftable` on a powered network holding only the provider, each pattern has one iron input and one item output, an item outside the pool is not craftable, and an unpowered provider offers nothing.
- [x] 2.2 Implement never-busy `pushPattern` that voids inputs and queues outputs due 40 ticks later, plus a tick that inserts due outputs into network storage and keeps uninserted remainders. Verify with GameTests that submit real jobs through a crafting CPU: output absent at tick 39 after the push and present after 40, job completes, N items consume exactly N iron, a plan without iron reports missing iron, several overlapping pushes each arrive on time, and a delivery due while the network is unpowered arrives once after power returns.
- [x] 2.3 Persist the pending queue in machine NBT and discard it on removal without drops. Verify with GameTests that a reload (fresh `CompoundTag.copy()` replay as in `WorldEaterDemandTests.reload`) mid-delivery delivers exactly once and completes the job, and that breaking the provider with pending outputs delivers nothing and spawns no item entities (assert `isPositionEntityTicking` first).

## 3. ME Demand Hatch integration

- [x] 3.1 Add one GameTest where a real paid run with an attached ME Demand Hatch, a crafting CPU, stocked iron and the provider (no matching stock) submits one job, pays the demand no earlier than 40 ticks after submission, and leaves iron reduced by the demand amount. Verify that test and the existing `WorldEaterMETests` pass unchanged, then run the full GameTest suite once and report counts.

## Workflow follow-up

- Commit, push the branch and open a PR.
- Archive the change after review.
