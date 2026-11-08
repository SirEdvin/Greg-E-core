## ADDED Requirements

### Requirement: Large tamer structure

The system SHALL expose a multiblock named `Incomplete World Eater tamer` with the fixed 15×15×15 layout defined by the controller's shared shape. The controller SHALL replace the assembly engine intake at the front-face center. Every other non-empty layout position SHALL require its listed block; empty layout positions SHALL be unrestricted. Parts SHALL only replace World Eater casing positions, and energy hatches SHALL NOT be accepted or required.

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
- **WHEN** a permitted part replaces any other required block
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

### Requirement: Dormant casing tier 2

A formed tamer SHALL have casing tier 1. Because the frame accepts only black casing, casing tier 2 SHALL be unreachable; its gates and dependent operations (Bravery acquisition, preparation tests and real demand runs) SHALL remain in place but dormant, and SHALL keep rejecting startup with their existing reasons. The controller, permitted parts, and unrestricted positions SHALL NOT contribute to the casing tier.

#### Scenario: Black frame is tier 1
- **WHEN** a valid frame forms
- **THEN** its tier is 1

#### Scenario: Tier-2 operations stay rejected
- **WHEN** a qualifying Bravery book is offered to a DORMANT or ACTIVE tier-1 tamer
- **THEN** it does not start, the book is unchanged, and the native rejection explains the missing state or tier

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

World Eater casing positions SHALL accept only the black World Eater Tamer Casing, or a permitted part. Dark purple, dark green, and very dark orange casings, UHV casing and unrelated blocks SHALL NOT form the frame. The controller and formed parts SHALL use the black casing appearance and keep readable overlays.

#### Scenario: Colored or unrelated casing
- **WHEN** a World Eater casing position holds a colored casing, UHV casing, or an unrelated block
- **THEN** the structure does not form

#### Scenario: Consistent machine appearance
- **WHEN** the controller and attached parts are inspected on a formed tamer
- **THEN** their casing appearance uses the black casing while retaining readable machine and part overlays

## REMOVED Requirements

### Requirement: Mixed-color small tamer frame

**Reason**: The small 5×5×5 tamer is replaced by the 15×15×15 layout, whose frame accepts only black casing.

**Migration**: Rebuild existing tamers in the new layout with black World Eater Tamer Casing; saved progression is kept.

### Requirement: Initial tamer structure

**Reason**: Replaced by "Large tamer structure"; the 5×5×5 edge skeleton no longer exists.

**Migration**: Rebuild the tamer in the 15×15×15 layout; saved progression is kept.

### Requirement: Existing part substitutions remain valid

**Reason**: Replaced by "Part substitutions on World Eater casing"; colored frames no longer exist.

**Migration**: Place parts on black World Eater casing positions.

### Requirement: Reversible casing-derived tier

**Reason**: Colored casings no longer form the frame, so tier 2 cannot be reached; see "Dormant casing tier 2".

**Migration**: None; tier-2 operations stay unavailable until colored frames return.

### Requirement: Optional single fixed-position intention hatch

**Reason**: Replaced by "Rear-face intention hatch slot" for the new layout.

**Migration**: Move an installed Safety or Bravery Hatch to the rear-face center.
