# world-eater-tamer Specification

## Purpose

Provide the initial World Eater tamer progression machine with persistent state-gated processing, tick-distributed surrounding-machine discovery, and readable success or failure reports without loading surrounding chunks.

## Requirements

### Requirement: Persistent state-gated recipes

The tamer SHALL initially be `DORMANT` and support persistent `ACTIVE` and `PREPARED` states. Its state SHALL be stored with its recipe logic, remain distinct from whether a recipe is currently running, and survive save/load and structure reformation. All recipes SHALL use `world_eater_tamer` and declare a valid required state. DORMANT surveys, ACTIVE preparation tests, and PREPARED real runs SHALL retain their distinct gates. PREPARED SHALL additionally satisfy the existing ACTIVE casing-conversion and Bravery-acquisition gates, but not the preparation-test gate. Missing, unknown, or otherwise mismatched states SHALL NOT consume inputs or start.

#### Scenario: New machine
- **WHEN** a newly created tamer is inspected
- **THEN** its progression state is DORMANT

#### Scenario: Saved active machine
- **WHEN** an ACTIVE tamer is saved and loaded or its structure is invalidated and reformed
- **THEN** its progression state remains ACTIVE

#### Scenario: Recipe state mismatch
- **WHEN** an ACTIVE or PREPARED tamer is supplied the DORMANT-only information collection recipe
- **THEN** that recipe does not start and does not consume the sensor

#### Scenario: Missing required state
- **WHEN** a world_eater_tamer recipe has no valid explicit required state
- **THEN** it cannot start or consume inputs

#### Scenario: Prepared is a permanent extension
- **WHEN** an ACTIVE tamer completes a successful preparation test
- **THEN** it becomes PREPARED and can perform real runs while retaining the existing casing-conversion and Bravery-acquisition recipes
- **AND** real-run success, timeout, cancellation, and ordinary save/load do not downgrade that progression

#### Scenario: Preparation does not bypass unrelated gates
- **WHEN** a PREPARED tamer is unformed, has tier 1, or lacks an intention hatch
- **THEN** real-run startup remains rejected without payment
- **AND** previously available recipes still apply their own hardware and input/output requirements

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

The native machine UI SHALL NOT display scan chunk coordinates; scan progress is reported only through the next-step hint. Every newly counted eligible machine SHALL emit one purple particle from the center of its block to the geometric center of the structure. Ordinary blocks, excluded parts, failed batch preflights, suspension and completed report delivery SHALL NOT emit discovery particles. The cosmetic effect SHALL use native particle networking without loading chunks or introducing client-only classes into server execution.

#### Scenario: Pending chunk feedback
- **WHEN** a survey is running
- **THEN** the native UI hint reports survey progress and no chunk coordinates

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

### Requirement: Optional single standard parallel hatch

The tamer SHALL accept zero or one standard GTCEu parallel hatch as a frame part substitution in DORMANT, ACTIVE, and PREPARED. A second parallel hatch SHALL prevent structure formation. The hatch SHALL NOT introduce an energy requirement or replace the existing independent item/fluid part limits.

#### Scenario: Hatch count limits
- **WHEN** a valid black frame contains zero or one standard parallel hatch
- **THEN** the structure forms without power
- **WHEN** a second standard parallel hatch is installed
- **THEN** the structure does not form

### Requirement: ACTIVE casing recipe parallel processing

An ACTIVE or PREPARED tamer SHALL apply the installed standard hatch's normal parallel capacity to colored-casing recipes, constrained by available inputs and output capacity. Each parallel SHALL consume one complete recipe's inputs and produce one casing. Parallel processing SHALL NOT shorten the 5-second processing duration or change progression state. Without a hatch, a run SHALL process one recipe at a time. This compatibility SHALL NOT extend parallel processing to demand runs.

#### Scenario: Available parallel batch
- **WHEN** an ACTIVE or PREPARED tamer has a hatch supporting at least two parallels, two black casings, 32 matching terracotta, and sufficient output space
- **THEN** one 5-second run consumes both black casings and all 32 terracotta and produces two matching casings without power

#### Scenario: Limited inputs and output space
- **WHEN** an ACTIVE or PREPARED tamer has a higher parallel limit than its complete input sets or usable output capacity permit
- **THEN** its selected parallel count does not exceed either constraint and no incomplete parallel consumes materials

#### Scenario: No parallel hatch
- **WHEN** an ACTIVE or PREPARED tamer without a parallel hatch has multiple complete recipe input sets
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

Every tamer recipe SHALL declare a valid required progression state accepted by the operation-specific state rules. Missing, unknown, and incompatible states SHALL reject processing without input consumption. ACTIVE/PREPARED manufacturing and demand runs SHALL NOT enter survey bookkeeping. The DORMANT survey SHALL retain its scan coverage, chunk retry, suspension, report delivery, persistence, single-run duration, and harmless-failure behavior.

#### Scenario: Invalid state metadata
- **WHEN** a candidate recipe omits a required state, declares an unknown state, or declares a state incompatible with that operation and the tamer
- **THEN** it does not start and consumes nothing

#### Scenario: Existing activation threshold remains protected
- **WHEN** a survey completes below 500 machines, fails chunk retries, or awaits blocked report delivery
- **THEN** the tamer remains DORMANT
- **WHEN** a successful complete survey of at least 500 machines delivers its report
- **THEN** the machine becomes ACTIVE once and can subsequently manufacture casings

#### Scenario: Reload and rebuild preserve state
- **WHEN** a controller with valid saved progression is reloaded or its old frame is rebuilt with black casing
- **THEN** its progression is preserved
- **AND** paused surveys and pending reports retain the existing non-duplication and non-loading guarantees

### Requirement: Reversible casing-derived tier

A formed tamer SHALL have tier 1 if any tier-ring casing is black, and tier 2 if every tier-ring casing is colored. Colored variants SHALL be freely mixable and equivalent for tier calculation. Frame casings, the controller, permitted parts, and unrestricted positions SHALL NOT contribute to the casing tier. Tier SHALL reflect current composition, not a permanent unlock.

#### Scenario: Partial upgrade
- **WHEN** one or more black casings remain in the tier ring of an otherwise valid structure, including a mixture with colored casings
- **THEN** its tier is 1

#### Scenario: Last black casing is replaced
- **WHEN** the final black tier-ring casing is replaced with any allowed colored casing and the structure validates
- **THEN** its tier becomes 2
- **AND** no activation recipe or additional item payment is required for the tier change itself

#### Scenario: Downgrade and subsequent upgrade
- **WHEN** any tier-ring casing of a tier-2 structure is replaced with black and the structure validates
- **THEN** its tier becomes 1
- **WHEN** that last black casing is replaced with an allowed colored casing and the structure validates again
- **THEN** its tier returns to 2

#### Scenario: Color and non-casing independence
- **WHEN** every tier-ring casing is colored, while the frame, permitted parts and unrestricted positions hold black casing
- **THEN** the structure remains tier 2
- **WHEN** one colored tier-ring casing is replaced with another allowed color and the structure validates
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

Valid DORMANT/ACTIVE/PREPARED progression SHALL remain independent of casing tier. While the controller is retained, casing replacement, upgrade, downgrade, frame invalidation, reformation, and save/load SHALL NOT reset or advance that progression. The existing successful-survey activation transition SHALL remain valid. Only a successful preparation test SHALL advance ACTIVE to PREPARED. Fresh controllers SHALL still start DORMANT; invalid saved progression SHALL not grant an unlock.

#### Scenario: Active controller survives rebuilding
- **WHEN** an ACTIVE controller's frame is partially dismantled, upgraded to tier 2, downgraded to tier 1, or rebuilt
- **THEN** it remains ACTIVE throughout these structural changes
- **AND** it does not require another activation survey

#### Scenario: Colored frame does not awaken a dormant controller
- **WHEN** a DORMANT controller forms a tier-2 frame or changes between tiers
- **THEN** it remains DORMANT unless the existing successful-survey activation condition is fulfilled

#### Scenario: Save while the frame is broken
- **WHEN** a controller with valid DORMANT, ACTIVE, or PREPARED progression is saved and reloaded while its frame is incomplete, then repaired
- **THEN** its progression is preserved before and after reformation
- **AND** its casing tier is derived independently from the repaired frame

#### Scenario: New controller has no inherited activation
- **WHEN** a fresh controller without prior saved progression is placed in an existing colored frame
- **THEN** it starts DORMANT and can form at tier 2

#### Scenario: Prepared survives loss of hardware eligibility
- **WHEN** a PREPARED controller loses its intention hatch or is downgraded to tier 1 while remaining placed
- **THEN** it retains PREPARED but cannot start real runs until the tier-2 and intention requirements are restored
- **AND** it needs no new preparation test after repair

### Requirement: Existing processing remains independent of casing tier

DORMANT surveys and ACTIVE/PREPARED casing conversions SHALL remain available at either formed tier under their existing input, output, duration, power-free, and parallel rules. The tier-2 gates for Bravery acquisition and demand runs SHALL NOT restrict those older operations. Progression retention SHALL NOT recover unfinished recipes after a structural break. Completed pending survey reports SHALL retain their existing delivery guarantees.

#### Scenario: Existing recipes work at either tier
- **WHEN** a valid tier-1 or tier-2 frame is supplied the existing survey or casing-conversion recipe with an accepted progression state and required inputs/output capacity
- **THEN** the recipe operates with its unchanged payment, duration, and parallel behavior
- **AND** missing, unknown, or incompatible required progression state still prevents startup and input consumption

#### Scenario: Pending activation report survives an upgrade
- **WHEN** a DORMANT controller has a completed but undelivered successful survey report, and its frame is broken and rebuilt with colored casings
- **THEN** the same report remains pending without rescanning or consuming another sensor
- **WHEN** normal processing and output capacity are available again
- **THEN** the report is delivered once and only the existing survey result determines whether the controller becomes ACTIVE

#### Scenario: Structural interruption is not progression loss
- **WHEN** a frame break interrupts an unfinished scan or casing craft
- **THEN** the existing recipe-cancellation behavior remains unchanged
- **AND** the controller's earned progression is not reset by that cancellation

### Requirement: Intention is derived from the validated structure

A successfully validated formed tamer SHALL expose Safety, Bravery, or no declared intention according to its attached intention hatch. Missing, invalid, and not-yet-validated structures SHALL expose no operative intention. Reformation and reload SHALL derive selection from current attached hardware, not retain a stale selection. Absence of a hatch SHALL NOT implicitly select Safety or Bravery.

#### Scenario: Select and change intention
- **WHEN** a valid frame forms with Safety, is rebuilt with Bravery in the designated position, and is then rebuilt with an ordinary permitted block there
- **THEN** its intention is respectively Safety, Bravery, and none after each successful validation

#### Scenario: Invalidation clears operative selection
- **WHEN** a frame with a selected intention becomes invalid, or a structure check fails after encountering a hatch
- **THEN** it exposes no operative intention
- **AND** a later valid hatch-free structure does not inherit the earlier selection

#### Scenario: Reload derives current hardware
- **WHEN** a controller reloads, including one saved before intention hatches existed or whose frame changed while unloaded
- **THEN** it has no operative selection until successful validation
- **AND** its resulting selection matches the actual attached hatch or absence of a hatch

### Requirement: Intention selection preserves existing tamer behavior

Both intention variants and their absence SHALL preserve existing casing-tier derivation, valid DORMANT/ACTIVE/PREPARED progression, and older recipe rules. An intention hatch SHALL be excluded from casing-tier calculation like other parts. Installing, exchanging, or removing one SHALL NOT reset progression or trigger punishment. Existing interruption and pending-report behavior SHALL remain unchanged; only a new real demand run's normal unmet deadline SHALL invoke the new consequence policy.

#### Scenario: Hatch casing appearance does not lower tier
- **WHEN** a valid all-colored frame includes an intention hatch
- **THEN** the frame remains tier 2
- **WHEN** any actual black casing remains in the frame
- **THEN** its tier remains 1 regardless of intention

#### Scenario: Existing processing is independent of intention
- **WHEN** a valid tamer with Safety, Bravery, or no hatch processes an older recipe in an accepted progression state
- **THEN** the survey or casing conversion retains its input costs, duration, power-free behavior, output, and parallel rules
- **AND** missing, unknown, or incompatible required progression state still prevents startup without input consumption

#### Scenario: Replacing a hatch preserves progression and pending reports
- **WHEN** the controller remains while its intention hatch is installed, exchanged, or removed and the frame is revalidated
- **THEN** earned progression is preserved
- **AND** a completed pending survey report retains its existing exactly-once delivery and activation behavior
- **AND** unfinished recipes retain structural-interruption behavior without gaining punishment or recovery guarantees

#### Scenario: Survey failure does not gain consequences
- **WHEN** an existing DORMANT survey exhausts its unavailable-chunk retries with either intention variant or no hatch
- **THEN** it produces the existing failure report, remains DORMANT, and triggers no new consequence

### Requirement: ACTIVE tier-2 Bravery acquisition

The tamer SHALL process the Bravery acquisition recipe only while formed with validated casing tier 2 and ACTIVE or PREPARED progression. Its one-book payment and power-free 60-second output SHALL follow the intention-hatch acquisition contract. It SHALL use ordinary crafting rather than survey or demand processing and SHALL NOT change progression, scan machines, generate a report, request a component, punish failure, or consult installed intention.

#### Scenario: Both gates are required before payment
- **WHEN** a qualifying book is offered to DORMANT tier 1, DORMANT tier 2, ACTIVE tier 1, PREPARED tier 1, or an unvalidated tamer
- **THEN** Bravery acquisition does not start and the book is unchanged
- **WHEN** it is offered to an ACTIVE or PREPARED tier-2 tamer with output capacity
- **THEN** the recipe can start without any installed intention hatch or power supply

#### Scenario: Revalidation does not grant progression
- **WHEN** an ACTIVE or PREPARED tamer is downgraded to tier 1 and later rebuilt at tier 2
- **THEN** Bravery startup is rejected at tier 1 and permitted again after tier-2 validation
- **AND** earned progression and existing structural-interruption behavior remain unchanged

#### Scenario: Ordinary paid output retention
- **WHEN** output capacity becomes blocked after the book was consumed
- **THEN** the completed Bravery craft waits for capacity, including across save/load, and delivers exactly one hatch without charging another book
- **AND** no survey report, demand challenge, consequence, or progression transition occurs

### Requirement: Optional single freely positioned ME Demand Hatch

At either casing tier and in any progression state, the tamer SHALL accept zero or one ME Demand Hatch at any required frame position that accepts ordinary item/fluid I/O parts. A second attached demand hatch SHALL prevent formation. The hatch SHALL be excluded from casing-tier calculation, SHALL NOT select intention, and SHALL NOT change existing progression or recipe gates.

#### Scenario: Every ordinary I/O location and orientation
- **WHEN** one ME Demand Hatch substitutes for a permitted frame I/O position in each supported horizontal controller orientation
- **THEN** the tamer forms and attaches that hatch
- **AND** this includes the lower-rear center when it is not occupied by an intention hatch

#### Scenario: Optional means no existing frame migration
- **WHEN** an existing valid frame has no ME Demand Hatch
- **THEN** it remains valid and manual demand delivery remains available

#### Scenario: A second demand hatch is rejected
- **WHEN** two ME Demand Hatches occupy required frame positions
- **THEN** the structure does not form
- **AND** neither can bypass this limit by being treated as an ordinary input or output bus

#### Scenario: Unrestricted positions are not attachments
- **WHEN** additional ME Demand Hatches are placed only in unrestricted face-panel or interior positions
- **THEN** those blocks do not attach to the tamer, receive its demand, or initiate crafting for it
- **AND** their presence does not invalidate an otherwise valid frame

#### Scenario: No inherited tier or intention
- **WHEN** an otherwise valid all-colored frame includes an ME Demand Hatch
- **THEN** that hatch does not reduce its casing tier or change earned progression
- **WHEN** the demand hatch occupies the rear-center slot instead of Safety or Bravery
- **THEN** it provides no intention and does not bypass the real-run intention gate

#### Scenario: Replacement preserves earned state
- **WHEN** the controller remains while its ME Demand Hatch is installed, removed, or replaced and the frame is revalidated
- **THEN** earned progression and existing pending-reward/report retention remain unchanged
- **AND** an unfinished run follows the existing structural-interruption rules without punishment or token refund

### Requirement: A demand hatch has one attached controller

An ME Demand Hatch SHALL serve at most one formed tamer at a time. It SHALL NOT merge requests, choose implicitly between controllers, or allow shared attachment to multiply a run's scheduling opportunity.

#### Scenario: Overlapping structures try to share a hatch
- **WHEN** two tamers would attach the same ME Demand Hatch as a frame part
- **THEN** the second attachment is rejected while the first attachment remains valid
- **AND** no request or crafting decision is transferred between those controllers

### Requirement: Large tamer structure

The system SHALL expose a multiblock named `Incomplete World Eater tamer` with the fixed 15×15×15 layout defined by the controller's shared shape. The controller SHALL replace the front-face center intake. Other non-empty positions SHALL require their listed block, including 44 tier-ring World Eater casings; empty positions SHALL be unrestricted. Parts SHALL only replace frame casing positions, and energy hatches SHALL NOT be accepted or required.

#### Scenario: Complete layout
- **WHEN** the player builds every required block of the layout with the controller at the front-face center, leaving unrestricted positions empty or filled with arbitrary blocks
- **THEN** the structure forms
- **WHEN** any required block is missing
- **THEN** the structure does not form

#### Scenario: Independent hatch limits
- **WHEN** World Eater casing positions contain five item input buses, five item output buses, five fluid input hatches, and five fluid output hatches
- **THEN** those per-type counts are permitted
- **WHEN** any one permitted type has six parts
- **THEN** the structure does not form

#### Scenario: Parts only replace World Eater casing
- **WHEN** a permitted part replaces any other required block, including a tier-ring casing
- **THEN** the structure does not form

#### Scenario: Energy hatch rejection
- **WHEN** an energy hatch replaces a required World Eater casing
- **THEN** the structure does not form

#### Scenario: Optional intention slot does not require rebuilding
- **WHEN** a valid tamer has no intention hatch and its rear-face center contains the assembly engine intake
- **THEN** it remains valid without moving or replacing that block

### Requirement: Part substitutions on World Eater casing

In every progression state, World Eater casing positions SHALL accept up to five each of item input buses, item output buses, fluid input hatches and fluid output hatches, zero or one standard parallel hatch, and zero or one ME Demand Hatch with its own allowance. Zero or one intention hatch SHALL be accepted only at the rear-face center. Energy hatches and other substitutions SHALL be rejected, and no tamer power requirement SHALL be introduced.

#### Scenario: Permitted parts in the frame
- **WHEN** an otherwise valid frame includes up to five of each allowed item/fluid part category and zero or one standard parallel hatch
- **THEN** these substitutions do not prevent formation or require energy

#### Scenario: Part limits are not weakened
- **WHEN** a frame has six parts of any one limited item/fluid category, a second parallel hatch, or an energy hatch
- **THEN** the structure does not form, regardless of progression state

#### Scenario: Intention does not consume another part allowance
- **WHEN** an otherwise valid frame contains five of each allowed item/fluid category, one parallel hatch, and one intention hatch in its designated position
- **THEN** the structure forms without power
- **AND** the intention hatch does not count against the item/fluid or parallel-hatch allowances

#### Scenario: Demand hatch has its own allowance
- **WHEN** an otherwise valid frame has five of each ordinary item/fluid category, one parallel hatch, its correctly placed intention hatch, and one ME Demand Hatch
- **THEN** the structure forms without a tamer energy hatch
- **AND** the demand hatch neither consumes an ordinary part allowance nor lets a sixth ordinary item/fluid part or second special hatch bypass its limit

### Requirement: Rear-face intention hatch slot

The only eligible intention-hatch position SHALL be the center of the rear face, directly opposite the controller at the same Y level, replacing that face's assembly engine intake. The position SHALL rotate with the controller's horizontal orientation. It SHALL accept either intention variant or the assembly engine intake, and nothing else. Intention hatches SHALL NOT substitute for any other required block. The maximum SHALL be one attached intention hatch total, not one per variant.

#### Scenario: Either variant in each orientation
- **WHEN** an otherwise valid tamer facing north, south, east, or west has one Safety or Bravery Hatch at the rear-face center
- **THEN** it forms and attaches that hatch
- **AND** it also forms without an intention hatch when the assembly engine intake occupies that position

#### Scenario: Wrong position or substitution
- **WHEN** an intention hatch replaces a different required block, or an ordinary part, parallel hatch or energy hatch replaces the rear-face center
- **THEN** the structure does not form

#### Scenario: Additional attached intention hatch
- **WHEN** one intention hatch occupies the designated position and another Safety or Bravery Hatch replaces a different required block
- **THEN** the structure does not form, whether the variants match or differ

#### Scenario: Unrestricted positions do not select intention
- **WHEN** extra intention hatch blocks stand in unrestricted positions while all required positions are valid
- **THEN** those blocks neither prevent formation nor attach as intention parts or affect the selected intention

### Requirement: Black-only tamer frame

Frame casing positions SHALL accept only the black World Eater Tamer Casing, or a permitted part. Dark purple, dark green, and very dark orange casings, UHV casing and unrelated blocks SHALL NOT form the frame. Tier-ring positions SHALL accept black, dark purple, dark green, and very dark orange casings in any mixture, and nothing else. The controller and formed parts SHALL always use the black casing appearance with readable overlays; tier-ring colors SHALL NOT change it.

#### Scenario: Colored or unrelated frame casing
- **WHEN** a frame casing position holds a colored casing, UHV casing, or an unrelated block
- **THEN** the structure does not form

#### Scenario: Upgradable tier ring
- **WHEN** tier-ring positions hold any mixture of the four casing variants
- **THEN** the structure forms in each horizontal orientation
- **WHEN** a tier-ring position holds UHV casing, an unrelated block, or a part
- **THEN** the structure does not form

### Requirement: One next-step hint in the controller UI

Below the native GTCEu display lines, a formed tamer's UI SHALL show exactly one localized line naming the player's next step toward real runs, and no other tamer-specific line. An unformed tamer SHALL show no hint. Steps in priority order: free output space for a waiting reward or report; deliver the open demand (count, item, seconds left); DORMANT survey progress or IV Sensor; casing upgrade below tier 2; intention hatch; Desarium (ACTIVE) or Implementium (PREPARED).

#### Scenario: Tier-1 ACTIVE tamer
- **WHEN** an ACTIVE tamer is formed with a black inner-ring casing
- **THEN** its only tamer-specific line tells the player to replace the middle casings with colored ones to upgrade to tier 2

#### Scenario: Ready for real runs
- **WHEN** a PREPARED tier-2 tamer has an intention hatch and no open demand
- **THEN** its hint tells the player to insert Implementium for real runs
