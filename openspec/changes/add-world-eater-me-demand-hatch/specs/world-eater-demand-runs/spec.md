# Spec Delta

## MODIFIED Requirements

### Requirement: Immediate exact demand payment

On each enabled processing tick, the tamer SHALL check attached item inputs for the exact demanded item identity and full quantity, including amounts split across slots or buses. Once available it SHALL consume exactly that quantity and immediately settle success. It SHALL leave surplus and unrelated items untouched and SHALL NOT partially consume an insufficient demand. Demand detection on the deadline tick SHALL precede timeout failure. For a real run, the attached ME Demand Hatch SHALL contribute actually extractable matching ME stock to this same payment check; its displayed request SHALL NOT count as inventory. Shared network stock SHALL NOT be counted twice through multiple views. Payment SHALL clear the hatch request before any blocked reward delivery can wait or another run can begin.

#### Scenario: Full demand before expiry
- **WHEN** a run demanding three HV sensors finds those sensors across attached input slots or buses before or on its deadline tick
- **THEN** that tick consumes exactly three and ends the challenge successfully without waiting for the remaining deadline
- **AND** any extra sensors and unrelated items remain unchanged

#### Scenario: Incomplete or incorrect demand
- **WHEN** fewer than the full requested quantity is available, or a different component, material, form, or tier is supplied
- **THEN** none of those items is consumed as demand payment
- **AND** the pending challenge continues until fulfilled, interrupted, or timed out

#### Scenario: Mixed local and ME payment
- **WHEN** a real demand requires three matching items, one is in an ordinary input bus, and two are extractable through the demand hatch's ME connection
- **THEN** the next enabled payment check consumes exactly those three items and settles the run once
- **AND** surplus ME stock, unrelated local items, and the cleared ghost request are not consumed again

#### Scenario: Same network exposed through more than one input
- **WHEN** multiple attached input views expose the same insufficient ME stock, including a configured ordinary stocking bus alongside the demand hatch
- **THEN** those views do not multiply the available quantity or allow partial payment of an otherwise insufficient demand
- **AND** sufficient actual supply later permits exactly one full payment

#### Scenario: Fresh stock on the deadline tick
- **WHEN** the full remaining demand becomes extractable in ME before the enabled payment check on the final allowed tick
- **THEN** it is paid before timeout even if an earlier hatch display cache showed insufficient stock

#### Scenario: Paid demand with blocked essence output
- **WHEN** ME-backed demand payment succeeds while essence output is blocked
- **THEN** the hatch request clears immediately and no further ME items are removed for that run
- **AND** the existing single pending reward survives normal reload and is delivered once when capacity returns

### Requirement: Demand persistence does not create rewards or punishment

Normal save/load SHALL preserve the run kind, demanded item, quantity, tier, remaining processing time, paid-input state, and any earned pending reward. It SHALL NOT reroll, repay, duplicate output, or repeat a completed consequence. Invalid or missing demand data SHALL cancel that run harmlessly without granting a reward or discarding a separately valid earned progression state. For a real run, persistence SHALL also preserve that its single autocrafting opportunity was considered, whether skipped, failed, or submitted. Reload SHALL reconstruct only a still-pending request, never a new scheduling opportunity or hatch-owned crafting job. Missing or invalid automation-only bookkeeping in an otherwise valid older run SHALL conservatively suppress autocrafting without cancelling that valid run.

#### Scenario: Resume paid work
- **WHEN** a paid challenge is normally saved and restored with its valid structure and demanded item still available in the registry
- **THEN** it resumes the same remaining challenge without consuming another token

#### Scenario: Optional item disappears from a saved run
- **WHEN** a saved demand references an optional component absent from the current registry, or its quantity, tier, or deadline data is invalid
- **THEN** the run is cancelled without punishment or reward
- **AND** a valid saved ACTIVE or PREPARED progression remains unchanged

#### Scenario: Skipped and failed opportunities survive reload
- **WHEN** a real run reloads after skipping crafting for existing supply, an existing job, absent hardware, an offline network, or a failed attempt
- **THEN** the same pending demand can resume storage-based or manual fulfillment without another autocrafting attempt
- **AND** repairing the original cause of the skip or failure does not reopen scheduling for that run

#### Scenario: Submitted craft survives independently
- **WHEN** a real run with a successfully submitted craft unloads and later reloads
- **THEN** it does not submit another job or reconnect as the job's owner
- **AND** ordinary ME stock can still satisfy the original pending demand without rerolling or extending its deadline

#### Scenario: Calculation was not submitted before unload
- **WHEN** a real run unloads while its initial crafting calculation is still pending and no job was submitted
- **THEN** reload does not restart that calculation or grant a replacement attempt
- **AND** the valid demand can still be fulfilled from storage or ordinary input until its existing deadline

#### Scenario: Upgrade while a legacy demand is running
- **WHEN** a valid saved real run predates demand-hatch scheduling bookkeeping, or only its automation bookkeeping is invalid
- **THEN** its existing demand, progression, and earned reward remain governed by their valid saved state
- **AND** that already-started run receives no retrospective autocrafting attempt

### Requirement: Recipes and help explain the challenge

Recipe viewers and machine/hatch help SHALL distinguish the two tokens, ACTIVE test and PREPARED real gates, tier-2/intention requirements, power-free single-run behavior, random demand payment, immediate success, tier-scaled deadlines, and timeout-only consequences. They SHALL NOT imply a fixed demanded component, a mandatory 30-second wait, collateral explosions, or punishment for cancellation. Tamer and ME Demand Hatch help SHALL additionally explain the optional real-run automation, its one startup opportunity, duplicate suppression, fire-and-forget crafting, and unchanged timeout risk. Preparation tests SHALL NOT be presented as automated by this hatch.

#### Scenario: Discover the new progression step
- **WHEN** a player inspects the tamer recipes and intention-hatch help
- **THEN** the test shows a Desarium gem and preparation outcome, and the real run shows an Implementium gem and one essence ingot
- **AND** the dynamic demand, deadline rule, Safety sacrifice, single Bravery victim, and harmless interruption behavior are explained without clipped or internal-only captions

#### Scenario: Understand the automation upgrade
- **WHEN** a player inspects the ME Demand Hatch recipe and tamer automation help
- **THEN** the hatch shows one ME Stocking Input Bus plus four normal Tamed world eater essense ingots in a shapeless recipe
- **AND** the help distinguishes one startup attempt from continuous retries and explains that late job results remain ME stock
- **AND** existing intention requirements, manual delivery, and timeout consequences are not hidden or described as bypassed

## ADDED Requirements

### Requirement: Automation preserves challenge gates and outcomes

An installed ME Demand Hatch SHALL NOT become a prerequisite for manual runs, bypass existing startup gates, or alter tokens, random selection, demand quantity, parallel limits, progression, rewards, deadlines, or timeout consequences. An offline or unsuccessful automation path SHALL NOT cancel or extend an otherwise valid demand merely to avoid its normal outcome.

#### Scenario: Unavailable automation is not a timeout escape
- **WHEN** a valid real run remains formed with its intention hatch but its ME connection or crafting attempt cannot supply the demand
- **THEN** processing retains its existing deadline and normal unmet expiry applies the same intention-specific consequence once
- **AND** matching manual delivery before expiry can still settle success

#### Scenario: Other recipe gates remain unchanged
- **WHEN** an ME Demand Hatch is installed on a DORMANT, ACTIVE, PREPARED, tier-1, or tier-2 frame
- **THEN** each recipe retains its existing progression, casing-tier, intention, token, and output-capacity gates
- **AND** preparation tests, surveys, ordinary manufacturing, and parallel restrictions do not acquire new automation behavior
