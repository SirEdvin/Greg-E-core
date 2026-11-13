# Proposal

## Why

World Eater tamer recipes show up to six hard-coded English caption lines in recipe viewers (state, operation name, power, payment, deadline, outcome). Most repeat what the recipe slots or the controller's next-step hint already show.

## What Changes

- Each tamer recipe shows at most two localized caption lines: the required progression (with the ACTIVE → PREPARED transition for the preparation test) and, where relevant, the extra requirement (survey threshold, tier 2, intention hatch, demand).
- Demand quantity, deadline and success timing are no longer repeated in recipe viewers; the controller hint shows the live demand and remaining seconds.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `world-eater-demand-runs`: recipe viewers show only gates and the demand requirement.

## Impact

- `GregERecipeTypes` captions and `src/generated/resources/assets/gregecore/lang/en_us.json`.
- GameTests asserting the old caption phrases.
