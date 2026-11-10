# world-eater-me-demand-hatch Specification

## Purpose
Provide an essence-funded ME Demand Hatch that fulfills a World Eater tamer's real-run demand from ME storage and can initiate one duplicate-aware, fire-and-forget autocrafting attempt at startup.

## Requirements

### Requirement: Distinct ME Demand Hatch

The addon SHALL provide a placeable part named ME Demand Hatch with registry identity `gregecore:me_demand_hatch`. With AE2 available it SHALL appear in the creative inventory, retain its identity under normal recovery, and have a distinguishable overlay using the formed controller's native casing appearance.

#### Scenario: Obtain and identify the hatch
- **WHEN** AE2 is installed and a player obtains, places, attaches, and normally recovers the hatch
- **THEN** it is named ME Demand Hatch, renders without missing assets, and recovers as the same hatch
- **AND** its overlay remains identifiable on the formed tamer

### Requirement: Four-ingot shapeless upgrade

One shapeless crafting recipe SHALL consume exactly one GTCEu ME Stocking Input Bus and four normal Tamed world eater essense ingots to produce one ME Demand Hatch. It SHALL require no crafting machine, fluids, research, or power and SHALL NOT accept other bus variants or other material forms as substitutes.

#### Scenario: Successful shapeless acquisition
- **WHEN** a crafting grid contains one ME Stocking Input Bus and four normal Tamed world eater essense ingots in any valid arrangement
- **THEN** taking the result consumes that bus and four ingots and produces exactly one ME Demand Hatch

#### Scenario: Incomplete or substituted ingredients
- **WHEN** fewer than four ingots are supplied, or the bus, material, or ingot form is replaced with a different item
- **THEN** the recipe does not produce a hatch or consume those ingredients

#### Scenario: Reloaded recipe and surplus
- **WHEN** recipes reload and a player crafts with surplus stacks of the required ingredients
- **THEN** the shapeless recipe remains available and one craft consumes only one bus and four ingots
- **AND** the recipe viewer shows the correct counted ingredients and output

### Requirement: Controller-owned real-run request

The attached hatch SHALL automatically mirror the exact item identity and quantity of its tamer's pending paid real-run demand. The request SHALL be read-only, contain no real items merely by being displayed, and SHALL NOT independently request tokens, fluids, or inputs for other operations. Unattached hatches SHALL have no operative demand.

#### Scenario: Paid real demand appears
- **WHEN** a valid real run consumes its Implementium token and selects a demand
- **THEN** its attached hatch displays that demand without player configuration
- **AND** the displayed request alone does not count as inserted items or successful payment

#### Scenario: Other operations do not configure the hatch
- **WHEN** the tamer is idle, scanning, manufacturing casings or Bravery, running a preparation test, or rejecting a candidate recipe
- **THEN** the hatch creates no request for that operation and initiates no autocrafting

#### Scenario: Request cannot be edited into general stocking
- **WHEN** a player interacts with the hatch or attempts to copy ordinary stocking-bus configuration onto it
- **THEN** the player cannot replace its controller-selected request or enable independent continuous stocking

### Requirement: Passive fulfillment from accessible ME storage

While a real demand is pending, the hatch SHALL expose matching, actually extractable items from its connected ME storage as tamer input. It SHALL respect native network connectivity, power, channel, and access rules. Availability SHALL NOT require the hatch to own or track a crafting job, and item consumption SHALL obey the tamer's full-demand payment contract.

#### Scenario: Stock arrives after startup
- **WHEN** the demanded items become extractable in connected ME storage while the real run is enabled
- **THEN** they can satisfy the demand through the same payment rules as ordinary item inputs
- **AND** their origin can be an existing job, the hatch's one submitted job, or unrelated item delivery

#### Scenario: Disconnection does not invent supply
- **WHEN** the connected network is offline, inaccessible, or cannot supply the complete remaining demand
- **THEN** the hatch contributes only actually available supply and does not fake fulfillment or consume an insufficient demand
- **WHEN** access returns before the run ends
- **THEN** storage-based fulfillment resumes without a new autocrafting attempt

### Requirement: Single startup autocrafting opportunity

Each new paid real demand SHALL consider autocrafting only once when the demand appears. It SHALL use available matching supply first and, only for a shortage with a usable attached hatch and no matching scheduled production, attempt one craft for the missing quantity. A skipped, failed, or successful consideration SHALL exhaust that run's scheduling opportunity.

#### Scenario: Sufficient startup supply
- **WHEN** matching items already available through the attached inputs and connected ME storage cover the full demand
- **THEN** no autocrafting is initiated
- **AND** later removal of that supply does not open a crafting opportunity for the same run

#### Scenario: Only the shortage is requested
- **WHEN** a new demand lacks some of its required quantity and the connected network has no matching scheduled production
- **THEN** the single attempt requests only the missing quantity, counting each available item once
- **AND** surplus caused by native crafting batch sizes remains network stock rather than increasing tamer payment

#### Scenario: Unavailable startup cannot defer the first attempt
- **WHEN** a demand starts without a hatch or usable network connection
- **THEN** that run does not wait for a later connection to initiate autocrafting
- **AND** manual input and later storage availability remain valid ways to fulfill it

#### Scenario: Failed scheduling is not retried
- **WHEN** the initial attempt cannot obtain a usable pattern, ingredients, or suitable free CPU, or calculation or submission fails
- **THEN** no later tick, supply change, reconnect, or reload retries autocrafting for that demand
- **AND** the existing deadline continues under normal processing rules

#### Scenario: Next paid run has a new opportunity
- **WHEN** a run ends and a new token is paid for another real run
- **THEN** the new demand receives its own single scheduling opportunity even if its item identity matches the previous demand

### Requirement: Suppress already scheduled matching production

Before initiating or submitting a craft, the hatch SHALL check its connected network for jobs whose final requested output matches the demanded item, and matching production that AE2 is already awaiting. Any such scheduled production SHALL suppress a new job regardless of its quantity or requester. Jobs on other networks SHALL NOT suppress this network's attempt.

#### Scenario: Matching final-output job is already scheduled
- **WHEN** a CPU is executing a job for the demanded item, even before its final production step has been dispatched
- **THEN** the hatch does not schedule a duplicate and only monitors ME storage

#### Scenario: Existing job is small or belongs to another requester
- **WHEN** matching scheduled production is insufficient for the full demand or was requested by another player or machine
- **THEN** the hatch still skips its crafting attempt
- **AND** later cancellation, completion, or insufficient delivery from that job does not cause a retry during this run

#### Scenario: Awaited production and different items
- **WHEN** AE2 is already awaiting production of the exact demanded item
- **THEN** that production suppresses a duplicate
- **WHEN** only a different item, tier, or item identity is scheduled on the connected network
- **THEN** those unrelated jobs do not suppress the demanded item's one opportunity

### Requirement: Asynchronous planning remains one guarded attempt

Crafting calculation SHALL NOT block server processing or extend the deadline. Its completion SHALL belong only to the original paid demand, hatch attachment, and network. Before the single submission, sufficient new supply or matching scheduled production SHALL cause the attempt to be skipped. A stale or discarded calculation SHALL NOT be replayed or replaced.

#### Scenario: Another job appears during calculation
- **WHEN** matching production is scheduled after the hatch begins its calculation but before submission
- **THEN** the hatch skips submission and continues storage monitoring without retrying

#### Scenario: Manual fulfillment or ended demand during calculation
- **WHEN** the demand is fulfilled, cancelled, or timed out before its calculation can be submitted
- **THEN** that calculation cannot start a job for the ended demand or a later run

#### Scenario: Original attachment or network is lost
- **WHEN** the hatch is removed, its original attachment is lost, its network changes, or an unfinished calculation is lost on unload/reload
- **THEN** that pending calculation is not submitted through a replacement attachment or network and is not restarted
- **AND** any valid remaining demand retains passive fulfillment without another scheduling opportunity

### Requirement: Submitted jobs are fire-and-forget

A successfully submitted craft SHALL deliver through ordinary ME storage without a hatch-owned delivery link. After submission the hatch SHALL NOT monitor, cancel, modify, or resubmit that job. Run success, interruption, timeout, unload, or hatch removal SHALL NOT cancel it. AE2 and its other users SHALL remain free to manage the job normally.

#### Scenario: Job outlives the run
- **WHEN** a submitted job is still running after manual demand payment, timeout, frame break, or hatch removal
- **THEN** the hatch leaves that job alone
- **AND** late products remain ordinary network inventory without rewarding the ended run

#### Scenario: Job is cancelled externally
- **WHEN** a player or AE2 cancels a job the hatch previously submitted
- **THEN** the hatch neither repairs nor resubmits it and continues only to monitor storage for any still-pending demand

### Requirement: Request cleanup follows the demand

Payment, timeout, or cancellation SHALL clear the hatch's request and stop further demand extraction. An earned reward waiting for output space SHALL NOT retain or recreate the request. A later real run SHALL display only its own demand. Saved hatch configuration SHALL NOT independently resurrect a request.

#### Scenario: Reward output is blocked
- **WHEN** the full demand is paid while the essence output is blocked
- **THEN** the request clears immediately and no further matching items are consumed
- **AND** the controller retains its single earned reward under the existing delivery rules

#### Scenario: Reconnect after completion
- **WHEN** the hatch or client reconnects after a demand has ended
- **THEN** no stale request or ongoing stocking of that demand appears

### Requirement: Hatch help explains automation boundaries

The hatch's UI SHALL show its read-only demanded item and quantity when present and its ME connection state. Help SHALL explain placement, the four-ingot recipe, real-run-only use, one startup attempt, duplicate suppression, passive waiting, and fire-and-forget jobs. It SHALL NOT promise guaranteed delivery, continuous retry, or automatic timeout protection.

#### Scenario: Inspect the automation hatch
- **WHEN** a player inspects the hatch and its help while online, offline, idle, and demanding an item
- **THEN** the displayed demand and connection state are accurate and readable
- **AND** the player can discover that busy CPUs, missing patterns, and existing jobs can leave the demand unmet without retrying

### Requirement: AE2 remains an optional integration

Without AE2 installed, the addon and existing tamer behavior SHALL remain usable without loading AE2-only classes or publishing an unusable hatch recipe. With the supported AE2 integration installed, the hatch, acquisition recipe, and automation SHALL be available on both client and dedicated server under normal network rules.

#### Scenario: Launch without AE2
- **WHEN** the addon starts without AE2
- **THEN** existing machines, manual demand runs, and recipes remain available
- **AND** no missing-class error, dangling hatch recipe, or invalid creative entry is introduced by this feature

#### Scenario: Launch with AE2
- **WHEN** client and dedicated server start with the supported AE2 integration
- **THEN** the hatch registers and its crafting and network behavior are available without client-only classes entering server execution
