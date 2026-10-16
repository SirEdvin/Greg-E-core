# Spec Delta

## Purpose

Define the World Eater tamer's dedicated frame and state-sensitive parallel processing while preserving its existing power-free activation survey.

## ADDED Requirements

### Requirement: Dedicated black casing frame

The tamer SHALL retain its 5×5×5 edge-only skeleton, controller at the middle of the lower front edge, and unrestricted face panels and interior. Required frame casing positions SHALL accept only the dedicated black casing, subject to allowed part substitutions. UHV and colored casings SHALL NOT substitute for black. The controller and formed part casing appearance SHALL match the new black casing.

#### Scenario: Black skeleton forms
- **WHEN** the skeleton is built from black casing with the correctly positioned controller and permitted parts
- **THEN** it forms in each supported horizontal orientation with open or arbitrarily filled faces and interior

#### Scenario: Reject old and colored frame casing
- **WHEN** UHV casing or any of the three colored casings replaces a required black frame casing
- **THEN** the structure fails to form
- **AND** an existing UHV frame is not silently converted to black casing

#### Scenario: Preserve existing frame limits
- **WHEN** any required edge or corner is absent, an energy hatch replaces a casing, or any item/fluid input/output part category exceeds its existing limit of five
- **THEN** the structure fails to form

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
