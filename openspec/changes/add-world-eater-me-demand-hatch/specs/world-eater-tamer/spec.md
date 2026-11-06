# Spec Delta

## MODIFIED Requirements

### Requirement: Initial tamer structure

The system SHALL expose a multiblock named `Incomplete World Eater tamer` with outer dimensions 5×5×5 and an edge-only skeleton: the 12 cube edges including their corners, totaling 44 blocks. The controller SHALL occupy the middle of the lower front edge. Face panels and the 3×3×3 interior SHALL be unrestricted. The frame SHALL use dedicated World Eater Tamer Casings subject to its casing-color requirements and allow up to five of EACH of item input buses, item output buses, fluid input hatches, and fluid output hatches. Zero or one Safety or Bravery intention hatch SHALL be accepted at the middle of the lower rear edge, subject to its position and selection requirements. The frame SHALL additionally accept zero or one ME Demand Hatch at an ordinary I/O substitution position under its separate demand-hatch allowance. Energy hatches SHALL NOT be accepted as frame substitutions or be required for processing. This change SHALL NOT introduce the future 15×15×15 variant.

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

At either casing tier and in DORMANT, ACTIVE, or PREPARED, the tamer SHALL retain independent limits of five each for item input buses, item output buses, fluid input hatches, and fluid output hatches, plus zero or one standard parallel hatch. It SHALL additionally accept zero or one intention hatch only at its designated rear-center position and zero or one ME Demand Hatch at an ordinary I/O frame position. The demand hatch SHALL have a separate allowance and SHALL NOT consume or increase any ordinary item/fluid, parallel, or intention allowance. Energy hatches and other unsupported substitutions SHALL remain rejected. No tamer power requirement SHALL be introduced.

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

#### Scenario: Demand hatch has its own allowance
- **WHEN** an otherwise valid frame has five of each ordinary item/fluid category, one parallel hatch, its correctly placed intention hatch, and one ME Demand Hatch
- **THEN** the structure forms without a tamer energy hatch
- **AND** the demand hatch neither consumes an ordinary part allowance nor lets a sixth ordinary item/fluid part or second special hatch bypass its limit

## ADDED Requirements

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
