# Spec Delta

## MODIFIED Requirements

### Requirement: Exact ACTIVE casing conversion recipes

The tamer SHALL provide three power-free recipes requiring ACTIVE or PREPARED, each consuming one black casing and 16 vanilla terracotta blocks of the corresponding color to produce one colored casing. Purple terracotta SHALL produce dark purple, green SHALL produce dark green, and orange SHALL produce very dark orange. Each unmodified run SHALL take 5 seconds at the normal game tick rate and require no fluids. Preparation SHALL NOT change recipe inputs, outputs, or casing-tier independence.

#### Scenario: Each confirmed color conversion
- **WHEN** an ACTIVE or PREPARED tamer without a parallel hatch processes one black casing and 16 purple, green, or orange terracotta
- **THEN** after 5 seconds it produces exactly one matching colored casing and consumes exactly those inputs without EU or fluids

#### Scenario: Wrong or insufficient materials
- **WHEN** the available inputs have no black casing, fewer than 16 matching terracotta, or only a different terracotta color for the selected recipe
- **THEN** that recipe does not start or consume inputs

#### Scenario: Dormant tamer cannot manufacture
- **WHEN** a DORMANT tamer receives otherwise sufficient colored-casing recipe inputs
- **THEN** no colored-casing recipe starts or consumes those inputs

### Requirement: Safe manufacturing completion and persistence

Colored-casing processing SHALL retain its starting ACTIVE or PREPARED progression and use ordinary machine processing behavior for output blockage, suspension, and save/load. It SHALL NOT create a scan, discovery particles, survey report, demand challenge, timeout consequence, or progression transition. Save/load SHALL NOT charge a running craft twice or duplicate its output.

#### Scenario: Blocked manufacturing output
- **WHEN** output capacity is unavailable before or during processing
- **THEN** the machine waits according to normal processing rules without losing or duplicating paid-for casing output
- **AND** processing can complete when output capacity becomes available

#### Scenario: Resume an ACTIVE craft
- **WHEN** an ACTIVE or PREPARED casing run is suspended or saved and reloaded, then processing resumes
- **THEN** its progress and paid input state are retained under normal machine behavior
- **AND** it delivers the correct output once, keeps its starting progression, and never displays a current scan chunk, produces a survey book, or enters a demand challenge
