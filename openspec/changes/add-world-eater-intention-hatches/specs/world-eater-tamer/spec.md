# Spec Delta

## MODIFIED Requirements

### Requirement: Initial tamer structure

The system SHALL expose a multiblock named `Incomplete World Eater tamer` with outer dimensions 5×5×5 and an edge-only skeleton: the 12 cube edges including their corners, totaling 44 blocks. The controller SHALL occupy the middle of the lower front edge. Face panels and the 3×3×3 interior SHALL be unrestricted. The frame SHALL use dedicated World Eater Tamer Casings subject to its casing-color requirements and allow up to five of EACH of item input buses, item output buses, fluid input hatches, and fluid output hatches. Zero or one Safety or Bravery intention hatch SHALL be accepted at the middle of the lower rear edge, subject to its position and selection requirements. Energy hatches SHALL NOT be accepted as frame substitutions or be required for processing. This change SHALL NOT introduce the future 15×15×15 variant.

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

#### Scenario: Optional intention slot does not require rebuilding
- **WHEN** an existing valid tamer has no intention hatch and its rear-center position contains an allowed casing or an otherwise permitted existing part
- **THEN** it remains valid without moving or replacing that block

### Requirement: Existing part substitutions remain valid

At either casing tier and in either progression state, the tamer SHALL retain independent limits of five each for item input buses, item output buses, fluid input hatches, and fluid output hatches, plus zero or one standard parallel hatch. It SHALL additionally accept zero or one intention hatch only at its designated rear-center position. Energy hatches and other unsupported substitutions SHALL remain rejected. No tamer power requirement SHALL be introduced.

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

## ADDED Requirements

### Requirement: Optional single fixed-position intention hatch

The only eligible intention-hatch position SHALL be the center of the lower rear edge, directly opposite the controller at the same Y level. The position SHALL rotate with the controller's horizontal orientation. It SHALL accept either intention variant or any previously valid substitution. Intention hatches SHALL NOT substitute for any other required frame block. The maximum SHALL be one attached intention hatch total, not one per variant.

#### Scenario: Either variant in each orientation
- **WHEN** an otherwise valid tamer facing north, south, east, or west has one Safety or Bravery Hatch in the designated rear-center position
- **THEN** it forms and attaches that hatch
- **AND** it also forms without an intention hatch when an existing permitted block occupies that position

#### Scenario: Wrong frame position or height
- **WHEN** an intention hatch replaces a different required frame block, including a rear edge block at a different Y level
- **THEN** the structure does not form

#### Scenario: Additional attached intention hatch
- **WHEN** one intention hatch occupies the designated position and another Safety or Bravery Hatch replaces a different required frame block
- **THEN** the structure does not form, whether the variants match or differ

#### Scenario: Unrestricted positions do not select intention
- **WHEN** extra intention hatch blocks stand in unrestricted face-panel or interior positions while all required frame positions are valid
- **THEN** those blocks neither prevent formation nor attach as intention parts or affect the selected intention

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

Both intention variants and their absence SHALL preserve existing casing-tier derivation, DORMANT/ACTIVE progression, and recipe rules. An intention hatch SHALL be excluded from casing-tier calculation like other parts. Installing, exchanging, or removing one SHALL NOT reset progression or add failure effects. Existing interruption and pending-report behavior SHALL remain unchanged; Bravery acquisition SHALL NOT introduce a consequence policy.

#### Scenario: Hatch casing appearance does not lower tier
- **WHEN** a valid all-colored frame includes an intention hatch with black casing appearance
- **THEN** the frame remains tier 2
- **WHEN** any actual black casing remains in the frame
- **THEN** its tier remains 1 regardless of intention

#### Scenario: Existing processing is independent of intention
- **WHEN** a valid tamer with Safety, Bravery, or no hatch processes an existing recipe in its required progression state
- **THEN** the survey or casing conversion retains its input costs, duration, power-free behavior, output, and parallel rules
- **AND** missing, unknown, or mismatched required progression state still prevents startup without input consumption

#### Scenario: Replacing a hatch preserves progression and pending reports
- **WHEN** the controller remains while its intention hatch is installed, exchanged, or removed and the frame is revalidated
- **THEN** earned DORMANT/ACTIVE progression is preserved
- **AND** a completed pending survey report retains its existing exactly-once delivery and activation behavior
- **AND** unfinished recipes retain existing structural-interruption behavior rather than gaining new recovery guarantees

#### Scenario: Survey failure does not gain consequences
- **WHEN** an existing DORMANT survey exhausts its unavailable-chunk retries with either intention variant or no hatch
- **THEN** it produces the existing failure report, remains DORMANT, and triggers no new consequence

### Requirement: ACTIVE tier-2 Bravery acquisition

The tamer SHALL process the Bravery acquisition recipe only while formed with validated casing tier 2 and ACTIVE progression. Its one-book payment and power-free 60-second output SHALL follow the intention-hatch acquisition contract. It SHALL use ordinary crafting rather than survey processing and SHALL NOT change progression, scan machines, generate a report, or consult installed intention.

#### Scenario: Both gates are required before payment
- **WHEN** a qualifying book is offered to DORMANT tier 1, DORMANT tier 2, ACTIVE tier 1, or an unvalidated tamer
- **THEN** Bravery acquisition does not start and the book is unchanged
- **WHEN** it is offered to an ACTIVE tier-2 tamer with output capacity
- **THEN** the recipe can start without any installed intention hatch or power supply

#### Scenario: Revalidation does not grant progression
- **WHEN** an ACTIVE tamer is downgraded to tier 1 and later rebuilt at tier 2
- **THEN** Bravery startup is rejected at tier 1 and permitted again after tier-2 validation
- **AND** earned ACTIVE progression and existing structural-interruption behavior remain unchanged

#### Scenario: Ordinary paid output retention
- **WHEN** output capacity becomes blocked after the book was consumed
- **THEN** the completed Bravery craft waits for capacity, including across save/load, and delivers exactly one hatch without charging another book
- **AND** no survey report or progression transition occurs
