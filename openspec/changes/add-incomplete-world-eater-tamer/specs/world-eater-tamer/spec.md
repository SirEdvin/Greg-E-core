# Spec Delta

## Purpose

Provide the initial World Eater tamer progression machine with persistent state-gated processing, tick-distributed surrounding-machine discovery, and readable success or failure reports without loading surrounding chunks.

## ADDED Requirements

### Requirement: Initial tamer structure

The system SHALL expose a multiblock named `Incomplete World Eater tamer` with outer dimensions 5×5×5, a one-block-thick casing shell, and an unrestricted 3×3×3 interior. The shell SHALL use temporary high-progression casing and allow up to five of EACH of item input buses, item output buses, fluid input hatches, and fluid output hatches. Energy hatches SHALL NOT be accepted as shell substitutions or be required for processing. This change SHALL NOT introduce the future 15×15×15 variant.

#### Scenario: Interior contains blocks
- **WHEN** the player completes the valid shell with arbitrary blocks inside
- **THEN** the interior contents do not prevent formation

#### Scenario: Independent hatch limits
- **WHEN** the shell contains five item input buses, five item output buses, five fluid input hatches, and five fluid output hatches
- **THEN** those per-type counts are permitted
- **WHEN** any one permitted type has six parts
- **THEN** the structure does not form

#### Scenario: Energy hatch rejection
- **WHEN** an energy hatch replaces a required shell casing
- **THEN** the structure does not form

### Requirement: Persistent state-gated recipes

The tamer SHALL initially be `DORMANT` and support a persistent `ACTIVE` state. Its state SHALL be stored with its recipe logic, remain distinct from whether a recipe is currently running, and survive save/load and structure reformation. All recipes for this machine SHALL use recipe type `world_eater_tamer` and explicitly declare their required state. Recipes with a missing, unknown, or mismatched required state SHALL NOT consume inputs or start.

#### Scenario: New machine
- **WHEN** a newly created tamer is inspected
- **THEN** its progression state is DORMANT

#### Scenario: Saved active machine
- **WHEN** an ACTIVE tamer is saved and loaded or its structure is invalidated and reformed
- **THEN** its progression state remains ACTIVE

#### Scenario: Recipe state mismatch
- **WHEN** an ACTIVE tamer is supplied the DORMANT-only information collection recipe
- **THEN** that recipe does not start and does not consume the sensor

#### Scenario: Missing required state
- **WHEN** a world_eater_tamer recipe has no valid explicit required state
- **THEN** it cannot start or consume inputs

### Requirement: Initial information collection recipe

The system SHALL provide an information collection recipe requiring DORMANT, consuming exactly one IV sensor per run, and consuming no EU or other power. A successful run with continuously available scan chunks SHALL perform exactly 120 seconds of scanning at the normal game tick rate, without overclocking or parallel processing shortening its duration. Scanning SHALL only advance while the run is enabled and the structure supports recipe processing.

#### Scenario: Power-free recipe
- **WHEN** a DORMANT tamer has one IV sensor and usable item output capacity but no energy hatches or power
- **THEN** information collection can start and consumes exactly that sensor

#### Scenario: Normal duration
- **WHEN** all required scan chunks remain available throughout an enabled run
- **THEN** the scan completes on its final tick of the 120-second recipe, not earlier or later

#### Scenario: Disabled processing
- **WHEN** recipe processing is disabled during a scan
- **THEN** scanning and accumulated machine counts do not advance until processing resumes

### Requirement: Incremental full-height scan coverage

The run SHALL scan every block position in the controller's chunk and all chunks within four chunks in both horizontal directions: the inclusive 9×9 square in the controller's dimension, across that dimension's full build-height range. It SHALL distribute deterministic, bounded sections of this work across recipe ticks, rather than scanning the entire area or a whole full-height chunk in a single tick. On successful runs every position SHALL be visited once, with the last section visited on the final scanning tick. Retry delays SHALL NOT shift, duplicate, or omit sections.

#### Scenario: Boundary coverage
- **WHEN** eligible machines are placed in the outermost included chunks at the lowest and highest valid build heights and another is placed outside the square
- **THEN** the included machines are counted and the outside machine is not

#### Scenario: Final section
- **WHEN** all chunks are available and an eligible machine occupies the final scheduled scan section
- **THEN** it is included when that final section is scanned on the final recipe tick

#### Scenario: Extended duration preserves coverage
- **WHEN** a chunk temporarily becomes unavailable and later becomes available during retries
- **THEN** scanning resumes with the same pending section and completes all sections without duplicate counts

### Requirement: Interface eligibility and grouping

The system SHALL centralize the decision whether a scanned block qualifies as a countable interface so future interface categories can be added without replacing scan scheduling. Initially eligible interfaces SHALL be standalone GregTech machines and other GregTech multiblock controllers. Multiblock buses, hatches, and other parts SHALL NOT count independently, and this tamer's controller and attached parts SHALL be excluded. Counts SHALL be grouped by machine identity, displayed with the corresponding machine name, and totaled across all eligible identities.

#### Scenario: Standalone and multiblock counts
- **WHEN** a scanned area contains two identical standalone machines and another multiblock with one controller and several parts
- **THEN** the standalone machine entry has count two and the other multiblock contributes one controller, not its parts

#### Scenario: Self exclusion
- **WHEN** the scan encounters its own controller and attached parts
- **THEN** none contribute to its machine count

#### Scenario: Non-GregTech blocks
- **WHEN** a section contains ordinary blocks or non-GregTech block entities
- **THEN** those blocks do not contribute to its machine count

### Requirement: Non-loading chunk retries and early failure

The scan SHALL NOT load or force-load chunks or create chunk tickets. If a chunk required by the pending section is unavailable, the run SHALL keep that section pending, add one second to its duration, and spend that second without scanning. It SHALL then retry the required chunk. A continuous unavailable-chunk episode SHALL allow five additional attempts, each separated by one second of non-scanning delay, after the initial attempt. If the fifth retry still finds the chunk unavailable, the run SHALL end early with a vanilla written failure book and remain DORMANT regardless of partial counts. A successfully recovered episode SHALL reset the retry allowance for later unavailable-chunk episodes. The failure report SHALL identify the unavailable chunk area using ordinary block X/Z coordinates, not chunk-grid coordinates.

#### Scenario: Recovery after one delay
- **WHEN** the pending chunk is unavailable on the initial attempt and becomes available within the next second
- **THEN** no scanning occurs during that second, the duration is extended by one second, and scanning resumes at the unchanged pending section on retry

#### Scenario: Retry exhaustion
- **WHEN** the same pending chunk remains unavailable through the initial attempt and all five one-second retries
- **THEN** scanning terminates immediately after the fifth retry without waiting for the remaining nominal recipe time
- **AND** the output book reports scan failure and identifies the unavailable area in block X/Z coordinates

#### Scenario: Partial counts cannot activate
- **WHEN** the scan has already counted at least 500 machines but ends with an unavailable-chunk failure
- **THEN** the tamer remains DORMANT and produces a failure report rather than a successful activation report

#### Scenario: No chunk loading side effect
- **WHEN** an unavailable chunk is encountered
- **THEN** retries only test availability and do not request or retain a loading ticket

### Requirement: Save and resume scan bookkeeping

Recipe progress SHALL determine the pending scan section across save/load. Accumulated counts, the scan domain, elapsed non-scanning delay, the pending retry and its remaining delay, and any completed-but-undelivered report SHALL persist with the recipe logic. Reloading SHALL NOT restart the scan, repeat previously counted sections, skip work, replenish an in-progress retry allowance, or consume another sensor. Completed recipe bookkeeping SHALL be cleared before a new run without resetting the progression state.

#### Scenario: Reload during scanning
- **WHEN** a partially completed run is saved, unloaded, and loaded with its structure available
- **THEN** it resumes from its saved progress with its accumulated counts and does not consume another sensor or rescan completed sections

#### Scenario: Reload during retry
- **WHEN** a run is saved during a one-second retry delay
- **THEN** it resumes the remaining delay and existing retry count rather than starting a fresh retry allowance

#### Scenario: Repeated dormant runs
- **WHEN** a successful run below the activation threshold completes and another run starts
- **THEN** the new run consumes a new sensor and starts with empty counts and fresh retry bookkeeping

### Requirement: Report books and activation threshold

Every successfully completed or retry-exhausted information collection run SHALL produce one vanilla written book. A successful report SHALL contain paginated `<machine name> - <count>` lines and the total count; a zero-machine report SHALL remain a valid readable book with total zero. A successful run with total count at least 500 SHALL change the tamer from DORMANT to ACTIVE only as part of successful report delivery. Totals below 500 and every failed scan SHALL leave it DORMANT. Output blockage SHALL retain the completed report for delivery rather than discarding it, duplicating it, rescanning, or consuming additional input.

#### Scenario: Below threshold
- **WHEN** a complete scan counts 499 machines
- **THEN** it outputs a successful written report and remains DORMANT

#### Scenario: Exact threshold
- **WHEN** a complete scan counts exactly 500 machines and the report is delivered
- **THEN** it outputs the report and becomes ACTIVE

#### Scenario: Above threshold
- **WHEN** a complete scan counts more than 500 machines and the report is delivered
- **THEN** it outputs the report and becomes ACTIVE

#### Scenario: Empty scan
- **WHEN** a complete scan finds no eligible machines
- **THEN** it outputs a valid written book with total zero and remains DORMANT

#### Scenario: Multi-page output
- **WHEN** the counted machine names and counts do not fit on one page
- **THEN** the book uses additional valid pages without losing count entries

#### Scenario: Output blocked at completion
- **WHEN** report delivery is blocked after scanning has completed
- **THEN** the run retains the exact completed report and does not activate yet
- **WHEN** item output capacity becomes available
- **THEN** it delivers the report once and applies the successful scan's threshold transition

#### Scenario: Reload before delivery
- **WHEN** a completed report is saved and reloaded before it can be delivered
- **THEN** the saved report remains pending and is delivered once without another scan or sensor consumption
