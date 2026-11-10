## ADDED Requirements

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

## MODIFIED Requirements

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

## REMOVED Requirements

### Requirement: Mixed-color small tamer frame

**Reason**: The small 5×5×5 tamer is replaced by the 15×15×15 layout: its frame accepts only black casing and casing color moves to the tier ring.

**Migration**: Rebuild existing tamers in the new layout; place colored casings in the tier ring for tier 2. Saved progression is kept.

### Requirement: Initial tamer structure

**Reason**: Replaced by "Large tamer structure"; the 5×5×5 edge skeleton no longer exists.

**Migration**: Rebuild the tamer in the 15×15×15 layout; saved progression is kept.

### Requirement: Existing part substitutions remain valid

**Reason**: Replaced by "Part substitutions on World Eater casing"; parts now sit only on black frame casing.

**Migration**: Place parts on black World Eater casing positions.

### Requirement: Optional single fixed-position intention hatch

**Reason**: Replaced by "Rear-face intention hatch slot" for the new layout.

**Migration**: Move an installed Safety or Bravery Hatch to the rear-face center.
