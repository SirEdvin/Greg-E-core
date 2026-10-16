# Spec Delta

## Purpose

Give the World Eater tamer a distinctive industrial casing family and useful ACTIVE-state manufacturing with predictable visual and material behavior.

## ADDED Requirements

### Requirement: Four dedicated industrial casing variants

The system SHALL provide black, dark purple, dark green, and very dark orange casing blocks with matching item forms and readable localized names. Their pixel-edited textures SHALL resemble GTCEu's Industrial Steam Casing through framed plates and corner bolts. The black variant SHALL use a black-based palette, not bronze or another colored base.

#### Scenario: Casing family presentation
- **WHEN** a player inspects the four casings as inventory items and placed blocks
- **THEN** all four render without missing textures and share the reference's framed, bolted plate design
- **AND** black remains visually black while the other three remain distinguishable by their dark color palettes

### Requirement: Periodic emissive shimmer

Black casing SHALL display an emissive shimmer once per 10-second animation cycle. Each colored casing SHALL display one once per 2-second cycle. These intervals SHALL be measured at the normal game tick rate. Only the shimmer details SHALL be emissive; the base plates SHALL retain their dark appearance. Shimmer SHALL NOT require an operating tamer or optional shader installation.

#### Scenario: Independent visual cycles
- **WHEN** black and colored casings are placed independently of a machine and observed over complete animation cycles
- **THEN** black repeats its shimmer every 10 seconds and each colored casing repeats every 2 seconds

#### Scenario: Emissive appearance in darkness
- **WHEN** casings are viewed in a dark scene without optional shaders
- **THEN** shimmer details remain visibly self-lit while the base plate retains ordinary shaded rendering
- **AND** the effect does not add or animate world light levels

### Requirement: Colored casing shadow movement

Each colored casing SHALL subtly shift its painted shadows over a looping texture animation. The plate boundary and bolts SHALL remain visually stable. The effect SHALL be cosmetic texture movement rather than changes to geometry, collision, or world lighting.

#### Scenario: Moving shade on a stable plate
- **WHEN** a colored casing is observed across its animation loop
- **THEN** its shadow pixels visibly shift slightly without moving the plate outline or bolt positions
- **AND** block collision and surrounding light levels do not change

### Requirement: Creative access and normal block lifecycle

All four casing items SHALL be available in the mod's creative inventory and place as their corresponding blocks. The black casing SHALL have no survival acquisition recipe in this change. Normally breaking a casing with an appropriate tool SHALL return that casing item rather than a differently colored variant.

#### Scenario: Black casing acquisition boundary
- **WHEN** a player examines creative inventory and loaded recipes
- **THEN** black casing is available in creative inventory and no survival recipe produces it

#### Scenario: Place and recover each color
- **WHEN** a player places and normally mines any casing with the appropriate tool
- **THEN** the placed block and recovered item retain that casing's variant

### Requirement: Exact ACTIVE casing conversion recipes

The tamer SHALL provide three ACTIVE-only, power-free recipes, each consuming one black casing and 16 vanilla terracotta blocks of the corresponding color to produce one colored casing. Purple terracotta SHALL produce dark purple, green SHALL produce dark green, and orange SHALL produce very dark orange. Each unmodified run SHALL take 5 seconds at the normal game tick rate and require no fluids.

#### Scenario: Each confirmed color conversion
- **WHEN** an ACTIVE tamer without a parallel hatch processes one black casing and 16 purple, green, or orange terracotta
- **THEN** after 5 seconds it produces exactly one matching colored casing and consumes exactly those inputs without EU or fluids

#### Scenario: Wrong or insufficient materials
- **WHEN** the available inputs have no black casing, fewer than 16 matching terracotta, or only a different terracotta color for the selected recipe
- **THEN** that recipe does not start or consume inputs

#### Scenario: Dormant tamer cannot manufacture
- **WHEN** a DORMANT tamer receives otherwise sufficient colored-casing recipe inputs
- **THEN** no colored-casing recipe starts or consumes those inputs

### Requirement: Safe manufacturing completion and persistence

Colored-casing processing SHALL retain ACTIVE progression and use ordinary machine processing behavior for output blockage, suspension, and save/load. It SHALL NOT create a scan, discovery particles, survey report, or progression transition. Save/load SHALL NOT charge a running craft twice or duplicate its output.

#### Scenario: Blocked manufacturing output
- **WHEN** output capacity is unavailable before or during processing
- **THEN** the machine waits according to normal processing rules without losing or duplicating paid-for casing output
- **AND** processing can complete when output capacity becomes available

#### Scenario: Resume an ACTIVE craft
- **WHEN** an ACTIVE casing run is suspended or saved and reloaded, then processing resumes
- **THEN** its progress and paid input state are retained under normal machine behavior
- **AND** it delivers the correct output once, remains ACTIVE, and never displays a current scan chunk or produces a survey book
