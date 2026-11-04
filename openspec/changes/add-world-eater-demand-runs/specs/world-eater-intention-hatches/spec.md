# Spec Delta

## MODIFIED Requirements

### Requirement: Bravery tamer crafting parameters

Bravery SHALL have one recipe in the World Eater tamer, requiring ACTIVE or PREPARED progression and casing tier 2. It SHALL produce one Bravery Hatch in 60 seconds without power, fluids, research, or a research data item. Bravery SHALL NOT have an Assembly Line recipe. Crafting SHALL NOT require any intention hatch to be installed, create a demand challenge, change progression, or punish failure.

#### Scenario: Power-free tier-2 craft
- **WHEN** an ACTIVE or PREPARED tier-2 tamer receives one qualifying book with usable output capacity
- **THEN** it consumes that book and produces one Bravery Hatch after 1,200 processing ticks without power, fluids, or research
- **AND** Safety, Bravery, or no installed intention hatch has no effect on acquisition

#### Scenario: Wrong progression or tier
- **WHEN** a tamer is DORMANT, has casing tier 1, or lacks a validated structure
- **THEN** it cannot start the Bravery recipe and consumes no book

### Requirement: Reload-safe recipes and understandable presentation

Both hatch recipes SHALL retain matching behavior after recipe reload and server-to-client synchronization. Recipe viewers SHALL display the correct machine, output, counts, duration, power, and input order. Bravery SHALL show its ACTIVE-or-PREPARED tier-2 requirement and first-page phrase without implying a required title or author. The native structure preview SHALL identify the optional hatch position and both variants.

#### Scenario: Reload and synchronization preserve book matching
- **WHEN** recipes reload and a client reconnects to the server
- **THEN** both hatch recipes remain available and Bravery still accepts valid books with differing metadata while rejecting invalid books

#### Scenario: Recipe viewer and structure preview
- **WHEN** a player views Safety's Assembly Line recipe, Bravery's World Eater tamer recipe, and the tamer's structure preview
- **THEN** Safety shows its sixteen counted machine inputs and LuV power, while Bravery shows one qualifying book, its acknowledgement rule, ACTIVE-or-PREPARED progression, casing tier 2, and power-free processing
- **AND** both show a 60-second duration and one matching hatch output
- **AND** the structure preview presents both intention variants only at the optional rear-center position while retaining that position's existing alternatives

## ADDED Requirements

### Requirement: Timeout-only intention help

Hatch help SHALL explain that a normally expiring unmet real demand destroys only the attached Safety Hatch or, with Bravery, at most one eligible nearby machine. It SHALL describe no collateral damage and no punishment for preparation-test failure or interrupted recipes. Placement, fixed variant identity, and acquisition instructions SHALL remain available.

#### Scenario: Players understand the installed consequence
- **WHEN** a player inspects either intention hatch's help
- **THEN** it explains the implemented real-timeout behavior rather than promising only a future policy
- **AND** it does not describe five victims, a damaging area explosion, or punishment for frame breaks or other cancellations
