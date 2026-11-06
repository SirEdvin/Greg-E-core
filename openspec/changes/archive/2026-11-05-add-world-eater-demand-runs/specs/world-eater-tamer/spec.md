# Spec Delta

## MODIFIED Requirements

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

### Requirement: Existing part substitutions remain valid

At either casing tier and in DORMANT, ACTIVE, or PREPARED, the tamer SHALL retain independent limits of five each for item input buses, item output buses, fluid input hatches, and fluid output hatches, plus zero or one standard parallel hatch. It SHALL additionally accept zero or one intention hatch only at its designated rear-center position. Energy hatches and other unsupported substitutions SHALL remain rejected. No tamer power requirement SHALL be introduced.

#### Scenario: Permitted parts in a colored frame
- **WHEN** an otherwise valid colored frame includes up to five of each allowed item/fluid part category and zero or one standard parallel hatch
- **THEN** these substitutions do not prevent formation or require energy

#### Scenario: Part limits are not weakened
- **WHEN** a frame has six parts of any one limited item/fluid category, a second parallel hatch, or an energy hatch
- **THEN** the structure does not form, regardless of its casing colors or progression state

#### Scenario: Intention does not consume another part allowance
- **WHEN** an otherwise valid frame contains five of each allowed item/fluid category, one parallel hatch, and one intention hatch in its designated position
- **THEN** the structure forms without power
- **AND** the intention hatch does not count against the item/fluid or parallel-hatch allowances

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
