# Spec Delta

## Purpose

Let players upgrade the small World Eater tamer through its casing composition while preserving earned controller progression independently of the current structure tier.

## ADDED Requirements

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

## REMOVED Requirements

### Requirement: Dedicated black casing frame

**Reason**: Mixed-color frame acceptance supersedes the black-only restriction; the unchanged geometry, part limits, and black machine appearance remain required.
**Migration**: Use the mixed-color small tamer frame and existing part-substitution requirements. Existing black frames remain valid at tier 1; colored frame casings are now accepted without changing the controller or automatically converting UHV blocks.
