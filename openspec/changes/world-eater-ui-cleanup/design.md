# Design

## Decisions

- **One hint method.** `WorldEaterTamerRecipeLogic.getCustomProgressLine()` returns the hint in priority order: earned reward waiting for output, survey report waiting for output, open demand (count, item, seconds left), then by progression. DORMANT shows survey progress while scanning, otherwise the IV Sensor instruction. ACTIVE/PREPARED show the casing upgrade until tier 2, then the intention-hatch instruction, then Desarium (ACTIVE) or Implementium (PREPARED). The panel is server-fed, so casing tier and intention are read on the server.
- **Lang keys in the generated lang file.** Existing manual keys (`gregecore.world_eater.*`) already live in `src/generated/resources/assets/gregecore/lang/en_us.json`; new keys follow that convention.
- **Any-side AE2.** GTCEu's `GridNodeHolder` exposes front-facing machines only on their front, and `MEBusPartMachine.onRotated` resets that. Both machines set `EnumSet.allOf(Direction.class)` after construction and after rotation.
- **Safety in the Assembler.** The GTCEu 7.5.2 Assembler accepts at most nine item inputs, so the recipe keeps the first eight machines in the old order (Electric Furnace through Brewery), eight each, at LuV power for 60 seconds.
