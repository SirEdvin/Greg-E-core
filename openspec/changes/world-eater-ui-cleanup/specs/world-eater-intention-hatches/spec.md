## MODIFIED Requirements

### Requirement: Safety Assembly Line crafting parameters

Safety SHALL have one recipe in the standard Assembler producing exactly one hatch with a base duration of 60 seconds and LuV recipe power of 30,720 EU/t. It SHALL NOT require fluids, research, or a research data item, and SHALL have no Assembly Line recipe. Ordinary Assembler power and overclocking behavior SHALL remain unchanged.

#### Scenario: Base crafting parameters
- **WHEN** the Safety recipe runs continuously at its base voltage without overclocking, with valid inputs and usable output capacity
- **THEN** it consumes the specified inputs and completes after 1,200 processing ticks to produce one matching hatch
- **AND** its power requirement is 30,720 EU/t

#### Scenario: No fluid or research payment
- **WHEN** a valid Assembler is supplied the required items and power, with research enabled but without fluids or a research data item
- **THEN** the Safety recipe can be discovered and started without completing research

#### Scenario: Normal power requirement
- **WHEN** the Assembler cannot supply the required recipe power
- **THEN** the hatch recipe does not bypass normal power checks or progress as a power-free recipe

### Requirement: Fixed Safety machine payment

The Safety recipe SHALL consume exactly eight of each of the eight LuV single-block machine identities listed in its payment scenario, with no substitutions by another machine type or voltage tier and no additional item payment.

#### Scenario: Complete Safety payment
- **WHEN** the Assembler receives eight of every machine below
- **THEN** one Safety Hatch craft consumes exactly those 64 machines

| Order | Machine | Required item | Count |
| --- | --- | --- | --- |
| 1 | Electric Furnace | `gtceu:luv_electric_furnace` | 8 |
| 2 | Macerator | `gtceu:luv_macerator` | 8 |
| 3 | Alloy Smelter | `gtceu:luv_alloy_smelter` | 8 |
| 4 | Arc Furnace | `gtceu:luv_arc_furnace` | 8 |
| 5 | Assembler | `gtceu:luv_assembler` | 8 |
| 6 | Autoclave | `gtceu:luv_autoclave` | 8 |
| 7 | Bender | `gtceu:luv_bender` | 8 |
| 8 | Brewery | `gtceu:luv_brewery` | 8 |

#### Scenario: Incomplete or substituted Safety payment
- **WHEN** any required machine has fewer than eight available, is replaced by another machine or tier, or is absent
- **THEN** the recipe does not start or consume any of the otherwise valid inputs

#### Scenario: Surplus does not alter payment
- **WHEN** more than eight of a required machine are supplied for one base craft
- **THEN** only eight of that identity are consumed by that craft

### Requirement: Reload-safe recipes and understandable presentation

Both hatch recipes SHALL retain matching behavior after recipe reload and server-to-client synchronization. Recipe viewers SHALL display the correct machine, output, counts, duration and power. Bravery SHALL show its ACTIVE-or-PREPARED tier-2 requirement and first-page phrase without implying a required title or author. The native structure preview SHALL identify the optional hatch position and both variants.

#### Scenario: Reload and synchronization preserve book matching
- **WHEN** recipes reload and a client reconnects to the server
- **THEN** both hatch recipes remain available and Bravery still accepts valid books with differing metadata while rejecting invalid books

#### Scenario: Recipe viewer and structure preview
- **WHEN** a player views Safety's Assembler recipe, Bravery's World Eater tamer recipe, and the tamer's structure preview
- **THEN** Safety shows its eight counted machine inputs and LuV power, while Bravery shows one qualifying book, its acknowledgement rule, ACTIVE-or-PREPARED progression, casing tier 2, and power-free processing
- **AND** both show a 60-second duration and one matching hatch output
- **AND** the structure preview presents both intention variants only at the optional rear-center position while retaining that position's existing alternatives

### Requirement: Timeout-only intention help

Short localized hatch help SHALL state the rear-face-center placement, that a real demand timeout destroys only the attached Safety Hatch or, with Bravery, one nearby machine of the owner's team, and that there is no collateral damage and no punishment for test failures or cancellations.

#### Scenario: Players understand the installed consequence
- **WHEN** a player inspects either intention hatch's help
- **THEN** it explains placement and the implemented real-timeout behavior
- **AND** it does not describe five victims, a damaging area explosion, or punishment for frame breaks or other cancellations
