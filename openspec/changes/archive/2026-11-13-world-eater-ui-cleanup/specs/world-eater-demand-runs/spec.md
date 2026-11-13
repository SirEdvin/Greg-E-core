## MODIFIED Requirements

### Requirement: Demand is visible inside the structure

During a pending challenge, clients SHALL see the demanded item's icon, readable display name, and numerical quantity together inside the structure at its center. The display SHALL reflect server-selected data, remain consistent through suspension and reconnect, and clear after payment, timeout, or cancellation. The native machine UI hint SHALL report the demand and remaining deadline, or the pending reward, when applicable.

#### Scenario: Readable world-space demand
- **WHEN** a player views a pending challenge from supported horizontal machine orientations
- **THEN** the correct item icon, readable item name, and numerical quantity are visibly rendered together inside the frame, not only in the controller GUI
- **AND** reconnecting or reloading resources does not change the selected demand or leave a stale completed demand visible

### Requirement: Recipes and help explain the challenge

Recipe viewers SHALL distinguish the two tokens, ACTIVE test and PREPARED real gates, tier-2/intention requirements, random demand payment, immediate success and tier-scaled deadlines. Machine and hatch help SHALL be short and localized, and SHALL NOT imply a fixed demanded component, collateral explosions, or punishment for cancellation. Preparation tests SHALL NOT be presented as automated by the ME Demand Hatch.

#### Scenario: Discover the new progression step
- **WHEN** a player inspects the tamer recipes and intention-hatch help
- **THEN** the test shows a Desarium gem and preparation outcome, and the real run shows an Implementium gem and one essence ingot
- **AND** the help names each hatch's timeout consequence without clipped or internal-only captions

#### Scenario: Understand the automation upgrade
- **WHEN** a player inspects the ME Demand Hatch recipe and help
- **THEN** the hatch shows one ME Stocking Input Bus plus four normal Tamed world eater essense ingots in a shapeless recipe
- **AND** the help states the single autocrafting attempt without retries
