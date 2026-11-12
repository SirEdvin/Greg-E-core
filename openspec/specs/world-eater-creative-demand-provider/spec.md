# world-eater-creative-demand-provider Specification

## Purpose
Provide a creative-only AE2 pattern provider that can autocraft every World Eater real-run demand item from one Iron Ingot after a short delay, so demand automation can be tested without building processing chains.

## Requirements

### Requirement: Creative-only provider availability

The addon SHALL provide a placeable machine named Creative Demand Pattern Provider with registry identity `gregecore:creative_demand_pattern_provider`. With AE2 available it SHALL appear in the creative inventory. It SHALL have no crafting recipe, and it SHALL NOT be registered or listed when AE2 is absent.

#### Scenario: Obtain in creative
- **WHEN** AE2 is installed and a player opens the creative inventory
- **THEN** the Creative Demand Pattern Provider is listed, can be placed, and renders without missing assets

#### Scenario: Not obtainable through crafting
- **WHEN** recipes are loaded and a player searches the recipe viewer or any crafting grid for the provider
- **THEN** no recipe produces it

#### Scenario: AE2 absent
- **WHEN** the addon loads without AE2
- **THEN** loading succeeds and the provider is neither registered nor listed

### Requirement: Patterns cover the real-run demand pool

While connected to an online ME network, the provider SHALL expose exactly one processing pattern per item in the World Eater real-run demand pool. Each pattern SHALL require exactly one Iron Ingot and produce exactly one of that demand item. It SHALL expose no patterns for items outside the pool.

#### Scenario: Every demand item is craftable
- **WHEN** the provider is connected to a powered network
- **THEN** every item in the real-run demand pool is reported craftable by that network
- **AND** each pattern lists one Iron Ingot as its only input and one demand item as its only output

#### Scenario: Unrelated items are not offered
- **WHEN** the network is asked whether an item outside the demand pool is craftable, and no other provider offers it
- **THEN** the provider contributes no pattern for it

#### Scenario: Disconnected provider
- **WHEN** the provider is unpowered or disconnected from the network
- **THEN** its patterns are not available to that network

### Requirement: Iron is the only cost

A crafting job using the provider's patterns SHALL consume one Iron Ingot from the network per demand item produced and no other resources. Supplied iron SHALL be destroyed, not returned. Without enough iron, AE2 SHALL report the job as missing iron.

#### Scenario: Iron consumed per item
- **WHEN** a crafting job for N of a demand item completes through the provider
- **THEN** network iron decreases by exactly N and network stock of that item increases by N

#### Scenario: No iron available
- **WHEN** a crafting job for a demand item is calculated on a network without enough Iron Ingots
- **THEN** the plan reports the iron shortage and cannot be submitted as complete

### Requirement: Delayed delivery into network storage

The provider SHALL accept every pattern pushed to it and SHALL insert the pattern output into its connected ME network 40 ticks after that push. Outputs SHALL be delivered through ordinary network insertion, so a waiting crafting job receives them normally.

#### Scenario: Output arrives after two seconds
- **WHEN** a crafting CPU pushes one pattern to the provider
- **THEN** the output is not in the network before 40 ticks have passed
- **AND** it is inserted into the network once 40 ticks have passed, and the waiting job completes

#### Scenario: Never busy
- **WHEN** several pushes arrive before earlier deliveries finish
- **THEN** the provider accepts each one and delivers each output 40 ticks after its own push

#### Scenario: Network unavailable at delivery time
- **WHEN** a delivery becomes due while the provider cannot insert into its network
- **THEN** the undelivered output stays pending and is inserted once insertion succeeds, without loss or duplication

### Requirement: Pending deliveries survive reload

Accepted but undelivered outputs SHALL persist across chunk unload, world save, and reload, and SHALL be delivered exactly once afterwards. Removing the provider SHALL discard its pending deliveries.

#### Scenario: Reload during a delivery
- **WHEN** the world reloads while a pushed pattern's output is still pending
- **THEN** the output is delivered once after reload and the crafting job can complete

#### Scenario: Provider removed
- **WHEN** the provider is broken while deliveries are pending
- **THEN** those outputs are never delivered and no pending output drops as an item

### Requirement: Works with the ME Demand Hatch

The provider SHALL be usable by the ME Demand Hatch's single startup autocrafting attempt without any change to that hatch's behavior.

#### Scenario: Demand paid through the provider
- **WHEN** a real run starts with an attached ME Demand Hatch on a network holding enough Iron Ingots, a crafting CPU, and the provider, and the demand item is not in stock
- **THEN** the hatch's single attempt submits a job for the missing quantity
- **AND** the demand is paid from the delivered items no earlier than 40 ticks after the job starts
