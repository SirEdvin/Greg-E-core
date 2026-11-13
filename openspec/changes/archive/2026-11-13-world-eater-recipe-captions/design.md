# Design

- Two fixed `addDataInfo` lines, both read `gregecore.world_eater.recipe.<operation>.<line>`; a missing key renders blank, so operations without a second line keep the layout stable.
- Captions are resolved with `Component.translatable(key).getString()`, which works in the client recipe viewer and in server-side GameTests (Forge loads mod lang files on the server).
- Lines stay within the 150-pixel minimum recipe-viewer width checked by `WorldEaterDemandClientChecks`.
