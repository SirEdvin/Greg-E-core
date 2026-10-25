# world-eater-tamer Specification

## Purpose

Provide the initial World Eater tamer progression machine with persistent state-gated processing, tick-distributed surrounding-machine discovery, and readable success or failure reports without loading surrounding chunks.

## Requirements

### Requirement: Initial tamer structure

The system SHALL expose a multiblock named `Incomplete World Eater tamer` with outer dimensions 5×5×5 and an edge-only skeleton: the 12 cube edges including their corners, totaling 44 blocks. The controller SHALL occupy the middle of the lower front edge. Face panels and the 3×3×3 interior SHALL be unrestricted. The frame SHALL use dedicated World Eater Tamer Casings subject to its casing-color requirements and allow up to five of EACH of item input buses, item output buses, fluid input hatches, and fluid output hatches. Energy hatches SHALL NOT be accepted as frame substitutions or be required for processing. This change SHALL NOT introduce the future 15×15×15 variant.

#### Scenario: Skeleton frame
- **WHEN** the player builds only the cube edges with the controller at the middle of the lower front edge, leaving face panels and the interior empty
- **THEN** the structure forms
- **WHEN** any required frame edge or corner block is missing
- **THEN** the structure does not form

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

### Requirement: Live scan feedback

The native machine UI SHALL display the pending scan chunk's chunk-grid X/Z coordinates. It SHALL retain the unavailable chunk during retries and the pending chunk during suspension, reconstruct it from existing persisted scan progress on reload, and clear the display when idle or awaiting report delivery. Every newly counted eligible machine SHALL emit one purple particle from the center of its block to the geometric center of the structure. Ordinary blocks, excluded parts, failed batch preflights, suspension and completed report delivery SHALL NOT emit discovery particles. The cosmetic effect SHALL use native particle networking without loading chunks or introducing client-only classes into server execution.

#### Scenario: Pending chunk feedback
- **WHEN** a running scan advances into another chunk or waits for an unavailable chunk
- **THEN** the native client UI displays the corresponding X/Z chunk coordinates
- **WHEN** processing is suspended or saved and resumed
- **THEN** the pending chunk remains consistent with the scan cursor and retry state
- **WHEN** scanning finishes or is cancelled
- **THEN** no chunk is displayed as currently scanning

#### Scenario: Discovery effect
- **WHEN** the server scan counts an eligible machine
- **THEN** nearby clients receive one purple particle beginning at that machine and traveling to the structure's geometric center
- **WHEN** the scan visits an ordinary block, an excluded hatch, or its own controller
- **THEN** it emits no discovery particle for that block

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

### Requirement: Mixed-color small tamer frame

The small tamer SHALL accept black, dark purple, dark green, and very dark orange World Eater Tamer Casings in any mixture at required casing positions. Its 5x5x5 edge-only skeleton, controller at the middle of the lower front edge, and unrestricted face panels and interior SHALL remain unchanged. UHV and unrelated blocks SHALL NOT become valid casing substitutes. The controller and formed parts SHALL retain black casing appearance and readable overlays.

#### Scenario: Existing black frame
- **WHEN** an existing valid black frame is checked after the update
- **THEN** it remains valid without replacing its controller or moving blocks

#### Scenario: Freely mixed casing variants
- **WHEN** a complete frame uses any mixture of the four allowed casing variants, with permitted parts and the correctly positioned controller
- **THEN** it forms in each supported horizontal orientation
- **AND** a frame made from one colored variant or a mixture of colored variants also forms

#### Scenario: Missing or unrelated frame block
- **WHEN** a required edge or corner is missing or replaced with UHV casing or an unrelated block
- **THEN** the structure does not form
- **AND** an existing UHV frame is not silently converted to tamer casing

#### Scenario: Consistent machine appearance
- **WHEN** the controller and attached parts are inspected on a formed tamer
- **THEN** their casing appearance uses the black casing rather than UHV casing while retaining readable machine and part overlays

### Requirement: Optional single standard parallel hatch

The tamer SHALL accept zero or one standard GTCEu parallel hatch as a frame part substitution in either progression state. A second parallel hatch SHALL prevent structure formation. The hatch SHALL NOT introduce an energy requirement or replace the existing independent item/fluid part limits.

#### Scenario: Hatch count limits
- **WHEN** a valid black frame contains zero or one standard parallel hatch
- **THEN** the structure forms without power
- **WHEN** a second standard parallel hatch is installed
- **THEN** the structure does not form

### Requirement: ACTIVE casing recipe parallel processing

An ACTIVE tamer SHALL apply the installed standard hatch's normal parallel capacity to colored-casing recipes, constrained by available inputs and output capacity. Each parallel SHALL consume one complete recipe's inputs and produce one casing. Parallel processing SHALL NOT shorten the 5-second processing duration or change progression state. Without a hatch, a run SHALL process one recipe at a time.

#### Scenario: Available parallel batch
- **WHEN** an ACTIVE tamer has a hatch supporting at least two parallels, two black casings, 32 matching terracotta, and sufficient output space
- **THEN** one 5-second run consumes both black casings and all 32 terracotta and produces two matching casings without power

#### Scenario: Limited inputs and output space
- **WHEN** an ACTIVE tamer has a higher parallel limit than its complete input sets or usable output capacity permit
- **THEN** its selected parallel count does not exceed either constraint and no incomplete parallel consumes materials

#### Scenario: No parallel hatch
- **WHEN** an ACTIVE tamer without a parallel hatch has multiple complete recipe input sets
- **THEN** each 5-second run consumes one input set and produces one casing

### Requirement: Activation survey remains single-run

The DORMANT information-collection recipe SHALL remain one sensor, one survey, and one report per run regardless of the installed parallel hatch. Its uninterrupted duration SHALL remain 120 seconds. Parallel, batch, or subtick multiplication SHALL NOT accelerate or multiply it; a recipe already carrying such multiplication SHALL NOT start or consume inputs.

#### Scenario: Parallel hatch during activation
- **WHEN** a DORMANT tamer with a high-capacity parallel hatch and multiple IV sensors starts information collection
- **THEN** it consumes exactly one sensor, performs the unchanged 120-second survey, and produces one report
- **AND** surplus sensors remain unconsumed for that run

#### Scenario: Pre-multiplied survey is rejected
- **WHEN** an information-collection recipe with parallel, batch, or subtick multiplication above one is supplied
- **THEN** it does not start or consume any sensor

### Requirement: State gating and survey isolation

Every tamer recipe SHALL declare a valid required progression state matching the machine. Missing, unknown, and mismatched states SHALL reject processing without input consumption. ACTIVE manufacturing SHALL NOT enter survey bookkeeping, and the DORMANT survey SHALL retain its existing scan coverage, chunk retry, suspension, report delivery, and persistence behavior.

#### Scenario: Invalid state metadata
- **WHEN** a candidate recipe omits a required state, declares an unknown state, or declares a different state from the tamer
- **THEN** it does not start and consumes nothing

#### Scenario: Existing activation threshold remains protected
- **WHEN** a survey completes below 500 machines, fails chunk retries, or awaits blocked report delivery
- **THEN** the tamer remains DORMANT
- **WHEN** a successful complete survey of at least 500 machines delivers its report
- **THEN** the machine becomes ACTIVE once and can subsequently manufacture casings

#### Scenario: Reload and rebuild preserve state
- **WHEN** a controller with valid saved progression is reloaded or its old frame is rebuilt with black casing
- **THEN** its progression state is preserved
- **AND** paused surveys and pending reports retain the existing non-duplication and non-loading guarantees

### Requirement: Existing part substitutions remain valid

At either casing tier and in either progression state, the tamer SHALL retain independent limits of five each for item input buses, item output buses, fluid input hatches, and fluid output hatches, plus zero or one standard parallel hatch. Energy hatches and other unsupported substitutions SHALL remain rejected. No power requirement SHALL be introduced.

#### Scenario: Permitted parts in a colored frame
- **WHEN** an otherwise valid colored frame includes up to five of each allowed item/fluid part category and zero or one standard parallel hatch
- **THEN** these substitutions do not prevent formation or require energy

#### Scenario: Part limits are not weakened
- **WHEN** a frame has six parts of any one limited item/fluid category, a second parallel hatch, or an energy hatch
- **THEN** the structure does not form, regardless of its casing colors or progression state

### Requirement: Reversible casing-derived tier

A formed small tamer SHALL have tier 1 if any actual frame casing is black, and tier 2 if every actual frame casing is colored. Colored variants SHALL be freely mixable and equivalent for tier calculation. The controller, permitted parts, unrestricted face panels, and interior SHALL NOT contribute to the casing tier. Tier SHALL reflect current composition, not a permanent unlock.

#### Scenario: Partial upgrade
- **WHEN** one or more black casings remain in an otherwise valid frame, including a mixture with colored casings
- **THEN** its tier is 1

#### Scenario: Last black casing is replaced
- **WHEN** the final black frame casing is replaced with any allowed colored casing and the structure validates
- **THEN** its tier becomes 2
- **AND** no activation recipe or additional item payment is required for the tier change itself

#### Scenario: Downgrade and subsequent upgrade
- **WHEN** any actual casing in a tier-2 frame is replaced with black and the structure validates
- **THEN** its tier becomes 1
- **WHEN** that last black casing is replaced with an allowed colored casing and the structure validates again
- **THEN** its tier returns to 2

#### Scenario: Color and non-casing independence
- **WHEN** every actual frame casing is colored, permitted parts retain their black appearance, and black casings are present only in unrestricted face panels or the interior
- **THEN** the structure remains tier 2
- **WHEN** one colored frame casing is replaced with another allowed color and the structure validates
- **THEN** it remains tier 2

### Requirement: Tier belongs to a successfully validated structure

Only a successfully validated formed structure SHALL provide an operative casing tier. An invalid or not-yet-validated structure SHALL NOT retain a usable tier from an earlier formation or partial check. Reformation and reload SHALL derive the tier from the current frame, rather than restoring a permanent or stale tier value.

#### Scenario: Broken frame is not a tier-2 machine
- **WHEN** a required block is removed from a tier-2 frame while its controller remains
- **THEN** the machine is unformed and cannot process recipes or expose tier 2 as an operative structure tier
- **WHEN** the missing block is restored with black casing and the frame validates
- **THEN** the machine forms at tier 1

#### Scenario: Repeated checks do not leak composition
- **WHEN** a failed check encounters black casing, followed by a successful check of a fully colored frame
- **THEN** the successful frame has tier 2
- **AND** repeated validation of an unchanged valid frame produces the same tier

#### Scenario: Reload uses actual frame contents
- **WHEN** a retained controller is unloaded and reloaded and its frame is validated
- **THEN** its operative tier matches the frame currently present
- **AND** this also holds when the frame changed while the controller was unavailable or when the saved controller predates casing tiers

### Requirement: Progression survives casing-tier changes

Valid DORMANT/ACTIVE progression SHALL remain independent of casing tier. While the controller is retained, casing replacement, upgrade, downgrade, frame invalidation, reformation, and save/load SHALL NOT reset or advance that progression. The existing successful-survey activation transition SHALL remain valid. Fresh controllers SHALL still start DORMANT, and invalid saved-data handling SHALL remain unchanged.

#### Scenario: Active controller survives rebuilding
- **WHEN** an ACTIVE controller's frame is partially dismantled, upgraded to tier 2, downgraded to tier 1, or rebuilt
- **THEN** it remains ACTIVE throughout these structural changes
- **AND** it does not require another activation survey

#### Scenario: Colored frame does not awaken a dormant controller
- **WHEN** a DORMANT controller forms a tier-2 frame or changes between tiers
- **THEN** it remains DORMANT unless the existing successful-survey activation condition is fulfilled

#### Scenario: Save while the frame is broken
- **WHEN** a controller with valid DORMANT or ACTIVE progression is saved and reloaded while its frame is incomplete, then repaired
- **THEN** its progression is preserved before and after reformation
- **AND** its casing tier is derived independently from the repaired frame

#### Scenario: New controller has no inherited activation
- **WHEN** a fresh controller without prior saved progression is placed in an existing colored frame
- **THEN** it starts DORMANT and can form at tier 2

### Requirement: Existing processing remains independent of casing tier

This change SHALL add no recipes or tier-based recipe gates. Existing DORMANT surveys and ACTIVE casing conversions SHALL remain available at either formed tier under their existing state, input, output, duration, power-free, and parallel rules. Progression retention SHALL NOT add recovery of unfinished recipes after a structural break. Completed pending survey reports SHALL retain their existing delivery guarantees.

#### Scenario: Existing recipes work at either tier
- **WHEN** a valid tier-1 or tier-2 frame is supplied the existing survey or casing-conversion recipe with its matching progression state and required inputs/output capacity
- **THEN** the recipe operates with its unchanged payment, duration, and parallel behavior
- **AND** missing, unknown, or mismatched required progression state still prevents startup and input consumption

#### Scenario: Pending activation report survives an upgrade
- **WHEN** a DORMANT controller has a completed but undelivered successful survey report, and its frame is broken and rebuilt with colored casings
- **THEN** the same report remains pending without rescanning or consuming another sensor
- **WHEN** normal processing and output capacity are available again
- **THEN** the report is delivered once and only the existing survey result determines whether the controller becomes ACTIVE

#### Scenario: Structural interruption is not progression loss
- **WHEN** a frame break interrupts an unfinished scan or casing craft
- **THEN** the existing recipe-cancellation behavior remains unchanged
- **AND** the controller's earned progression is not reset by that cancellation
