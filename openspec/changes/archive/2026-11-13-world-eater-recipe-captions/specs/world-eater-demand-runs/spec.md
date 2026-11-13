## MODIFIED Requirements

### Requirement: Recipes and help explain the challenge

Recipe viewers SHALL show at most two short localized caption lines per tamer recipe: the required progression, including the ACTIVE → PREPARED transition of the preparation test, and any tier-2, intention-hatch and demand requirement. Live demand quantity and deadline SHALL be shown by the controller hint, not repeated in recipe viewers. Machine and hatch help SHALL be short and localized, and SHALL NOT imply a fixed demanded component, collateral explosions, or punishment for cancellation. Preparation tests SHALL NOT be presented as automated by the ME Demand Hatch.

#### Scenario: Discover the new progression step
- **WHEN** a player inspects the tamer recipes and intention-hatch help
- **THEN** the test shows a Desarium gem and the ACTIVE → PREPARED transition, and the real run shows an Implementium gem and one essence ingot
- **AND** both demand recipes name the tier-2, hatch and demand requirement without clipped or internal-only captions

#### Scenario: Understand the automation upgrade
- **WHEN** a player inspects the ME Demand Hatch recipe and help
- **THEN** the hatch shows one ME Stocking Input Bus plus four normal Tamed world eater essense ingots in a shapeless recipe
- **AND** the help states the single autocrafting attempt without retries
