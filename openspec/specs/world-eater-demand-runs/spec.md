# world-eater-demand-runs Specification

## Purpose
Provide World Eater preparation and essence-production challenges with visible randomized delivery demands, immediate exact payment, persistent outcomes, and narrowly contained timeout consequences.

## Requirements

### Requirement: Native token and reward materials

The addon SHALL register `gregecore:desarium` and `gregecore:implementium` as gem materials and `gregecore:tamed_world_eater_essense` as an ingot material. Desarium SHALL be purple with DIAMOND appearance, Implementium green with EMERALD appearance, and the reward dark purple with SHINY appearance. The reward's material display name SHALL preserve `Tamed world eater essense`.

#### Scenario: Identify the primary forms
- **WHEN** the player inspects the materials and creative inventory
- **THEN** normal Desarium and Implementium gems and the essence ingot are registered, visible, named, and rendered without missing assets
- **AND** they are generated material forms, not duplicate standalone token items

### Requirement: Token acquisition belongs to the modpack

The change SHALL NOT add acquisition recipes or world generation for either token material. The modpack SHALL remain responsible for supplying Desarium and Implementium. Ordinary material-form conversions SHALL NOT be treated as new sources of token material.

#### Scenario: Registration without token crafting balance
- **WHEN** the addon is loaded without modpack token recipes
- **THEN** both tokens exist for creative, command, and scripting use
- **AND** no addon-authored recipe or world-generation feature supplies their material from other resources

### Requirement: State and hardware gates precede payment

Both demand recipes SHALL require a formed tier-2 tamer with an attached Safety or Bravery Hatch. The test SHALL require ACTIVE and one normal Desarium gem; the real run SHALL require PREPARED and one normal Implementium gem. Invalid state, structure, tier, intention, recipe metadata, or missing token SHALL reject startup without payment or punishment.

#### Scenario: Valid test and real startup
- **WHEN** an ACTIVE tier-2 tamer with either intention receives a Desarium gem
- **THEN** it can start a test and consume exactly one gem
- **WHEN** a PREPARED tier-2 tamer with either intention receives an Implementium gem and usable reward output capacity
- **THEN** it can start a real run and consume exactly one gem

#### Scenario: Rejected startup
- **WHEN** a token is offered to a DORMANT machine, an unformed machine, a tier-1 frame, a hatch-free frame, or the wrong recipe state
- **THEN** no demand run starts, no token or other item is consumed, and no consequence occurs
- **AND** a PREPARED machine does not repeat the ACTIVE-only preparation test

### Requirement: Single power-free demand run

Each demand run SHALL consume one token, request one item identity, and require no power or fluids. Parallel, batch, subtick, or overclock processing SHALL NOT multiply its token, demand, reward, or deadline. A pre-multiplied demand recipe SHALL be rejected before payment.

#### Scenario: Parallel hatch cannot multiply a challenge
- **WHEN** a qualifying tamer has a parallel hatch, several tokens, and ample output capacity
- **THEN** one startup pays one token and creates one demand
- **AND** surplus tokens remain untouched by that run

### Requirement: Tiered component demand families

The component pool SHALL comprise electric motors, electric pumps, conveyor modules, electric pistons, robot arms, sensors, emitters, and field generators at LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV, and UEV. Optional availability SHALL constrain actual entries. Circuits, cables, machine blocks, and tiers above UEV SHALL NOT enter this pool.

#### Scenario: Fixed family and tier boundary
- **WHEN** all permitted component items are registered
- **THEN** every listed family and tier is eligible
- **AND** no unrelated family or tier above UEV is eligible

### Requirement: Casing-material part demand families

The pool SHALL additionally contain normal gears, long rods, and rotors for each material/tier pair in the mapping scenario. These parts SHALL inherit the mapped tier's deadline. Required native forms SHALL be made available without introducing substitute materials. Small gears, short rods, turbine-rotor items, and UEV material entries SHALL NOT be included.

#### Scenario: Confirmed material mapping
- **WHEN** the material-part pool is assembled
- **THEN** each row supplies a normal gear, long rod, and rotor at the stated tier

| Tier | Material |
| --- | --- |
| LV | Steel |
| MV | Aluminium |
| HV | Stainless Steel |
| EV | Titanium |
| IV | Tungstensteel |
| LuV | Rhodium-Plated Palladium |
| ZPM | Naquadah Alloy |
| UV | Darmstadtium |
| UHV | Neutronium |

#### Scenario: Native form support
- **WHEN** the pinned dependency does not generate a required form by default
- **THEN** the addon enables that native material form and its normal processing routes
- **AND** the requested pool is not silently replaced with another material or an empty item

### Requirement: Optional components depend on actual registration

The demand pool SHALL contain only nonempty, registered items. Missing UHV or UEV components SHALL be omitted individually without forcing high-tier registration or changing configuration. Available entries at other tiers and material-part entries SHALL remain usable. An empty valid pool SHALL reject startup before consuming a token.

#### Scenario: High-tier content absent
- **WHEN** UHV and UEV component registrations are absent
- **THEN** startup and selection still work using available entries
- **AND** missing components cannot be selected, rendered, or dereferenced
- **AND** the addon does not enable high-tier content or author acquisition recipes for those components

#### Scenario: Partial optional availability
- **WHEN** one optional item is absent but another permitted optional item is present
- **THEN** only the absent entry is omitted
- **AND** the registered entry retains its own voltage-tier deadline

### Requirement: One random demand per paid startup

Successful startup SHALL choose one entry from the eligible pool and an integer quantity from 1 through 5. Each eligible entry SHALL have an equal selection chance; each quantity SHALL have an equal chance independent of the entry. Selection SHALL occur once per paid run, not during candidate recipe checks, ordinary waiting, or reload.

#### Scenario: Stable demand within one run
- **WHEN** a paid run is inspected repeatedly, suspended, or normally saved and loaded
- **THEN** its item, quantity, tier, and deadline remain unchanged
- **AND** no additional token is charged or demand rerolled

### Requirement: Tier-scaled completion deadline

A demand SHALL have 30 seconds per voltage-tier index, starting with LV as index 1, measured in enabled processing ticks at the normal game tick rate. Quantity SHALL NOT change the deadline. The deadline SHALL NOT impose a minimum completion time. Suspension and normal unloading SHALL NOT advance it.

#### Scenario: Deadline table
- **WHEN** a component or material part is selected at a listed tier
- **THEN** the run uses the corresponding deadline regardless of its demanded quantity

| Tier | Deadline in seconds |
| --- | --- |
| LV | 30 |
| MV | 60 |
| HV | 90 |
| EV | 120 |
| IV | 150 |
| LuV | 180 |
| ZPM | 210 |
| UV | 240 |
| UHV | 270 |
| UEV | 300 |

#### Scenario: Pause without punishment
- **WHEN** processing is suspended or the running controller is normally unloaded
- **THEN** the remaining processing time is retained and no timeout punishment occurs during that interval

### Requirement: Immediate exact demand payment

On each enabled processing tick, the tamer SHALL check attached item inputs for the exact demanded item identity and full quantity, including amounts split across slots or buses. Once available it SHALL consume exactly that quantity and immediately settle success. It SHALL leave surplus and unrelated items untouched and SHALL NOT partially consume an insufficient demand. Demand detection on the deadline tick SHALL precede timeout failure.

#### Scenario: Full demand before expiry
- **WHEN** a run demanding three HV sensors finds those sensors across attached input slots or buses before or on its deadline tick
- **THEN** that tick consumes exactly three and ends the challenge successfully without waiting for the remaining deadline
- **AND** any extra sensors and unrelated items remain unchanged

#### Scenario: Incomplete or incorrect demand
- **WHEN** fewer than the full requested quantity is available, or a different component, material, form, or tier is supplied
- **THEN** none of those items is consumed as demand payment
- **AND** the pending challenge continues until fulfilled, interrupted, or timed out

### Requirement: Success rewards and permanent preparation

A successful test SHALL produce no item output and change ACTIVE to PREPARED. A successful real run SHALL produce exactly one essence ingot and retain PREPARED. Demand payment SHALL occur once. An earned real reward blocked by output capacity SHALL remain pending without further demand checks, timeout punishment, repayment, or duplication, including over save/load.

#### Scenario: Preparation succeeds immediately
- **WHEN** a valid test receives its full demand
- **THEN** the demand is consumed once, the tamer becomes PREPARED on that tick, and no item reward is produced

#### Scenario: Real reward and blocked delivery
- **WHEN** a real run receives its full demand after its output bus has become blocked
- **THEN** it pays the demand once, ends the challenge successfully, and retains one earned ingot pending delivery
- **WHEN** output capacity becomes available, including after a save/load
- **THEN** the ingot is delivered exactly once without another token or demand payment and the tamer remains PREPARED

### Requirement: Only unmet normal expiry triggers punishment

An unmet test deadline SHALL end the run without output, state change, or damage. An unmet real deadline SHALL end the run without output and with its intention-specific consequence, retaining PREPARED. Broken structures, removed hatches, cancellation, invalid run data, lost optional items, and other non-timeout failures SHALL cause no punishment. Spent tokens SHALL NOT be refunded and unpaid demand items SHALL remain untouched.

#### Scenario: Test timeout is harmless
- **WHEN** a test reaches its deadline with the demand unmet
- **THEN** it ends, remains ACTIVE, produces nothing, and destroys neither hatch nor surrounding machine

#### Scenario: Real timeout only
- **WHEN** a real run reaches its deadline normally with the demand unmet and its structure and intention still valid
- **THEN** it produces nothing, remains PREPARED, and executes only the installed intention's consequence once

#### Scenario: Deliberate escape is allowed
- **WHEN** the player breaks any required multiblock part, removes the intention hatch, or otherwise cancels a running challenge before normal expiry
- **THEN** the challenge is interrupted without output or punishment
- **AND** progression is preserved, the already spent token is not refunded, and unpaid demand items are not consumed

#### Scenario: Stale completion callbacks
- **WHEN** completion or reset callbacks are invoked again after a run has already settled or been interrupted
- **THEN** no additional reward, input payment, or destructive consequence occurs

### Requirement: Safety sacrifice is confined to the attached hatch

A real timeout with Safety SHALL destroy only the attached Safety Hatch and show an explosion effect there. It SHALL NOT target a surrounding machine or damage another block or entity. The controller SHALL retain PREPARED even when hatch removal invalidates the frame. Subsequent real runs SHALL require a repaired tier-2 frame and an installed intention hatch.

#### Scenario: One sacrificed Safety Hatch
- **WHEN** a valid Safety-protected real run times out
- **THEN** only its attached Safety Hatch is destroyed by the consequence
- **AND** the controller, frame casings, other parts, nearby machines, terrain, and entities receive no collateral damage
- **AND** invalidation caused by that sacrifice does not repeat the consequence or reset PREPARED

### Requirement: Bravery searches outward from a random origin

A Bravery timeout SHALL choose one random block position within the controller's full-height 9x9-chunk survey region, then select the nearest eligible loaded machine around that origin. Selection SHALL stay inside the region, reuse survey eligibility, exclude this controller and multiblock parts, and never load chunks or retry an empty region indefinitely. It SHALL NOT uniformly choose directly from all machines.

#### Scenario: Random origin governs selection
- **WHEN** eligible loaded machines lie at different distances from the chosen origin
- **THEN** the closest eligible one is selected, not necessarily the closest to the tamer
- **AND** equal-distance ties resolve consistently for the same origin and world contents

#### Scenario: Region and eligibility boundaries
- **WHEN** standalone GT machines and other multiblock controllers are present alongside parts, this tamer, unrelated blocks, unloaded chunks, and machines outside the survey square
- **THEN** only eligible machines in loaded chunks within the square and build-height bounds can be selected
- **AND** no chunk ticket or load request is created to search for a target

#### Scenario: No available victim
- **WHEN** the bounded search finds no eligible loaded machine
- **THEN** the run ends with no reward and no destroyed block, and does not schedule a later attack

### Requirement: Bravery destroys only its selected machine

A Bravery timeout SHALL destroy at most one selected machine block, with an explosion effect at that block. It SHALL NOT destroy the Bravery Hatch, this tamer, other machine blocks, attached parts of the victim, or surrounding terrain, and SHALL NOT inflict collateral entity damage or fire. An unavailable or no-longer-eligible target SHALL cause no deferred destruction.

#### Scenario: One machine rather than an area explosion
- **WHEN** a Bravery timeout selects an eligible machine next to other machines, terrain, and entities
- **THEN** only the selected machine block is destroyed by the consequence
- **AND** its neighbors and entities receive no blast damage
- **AND** selecting a multiblock controller does not directly remove its other parts, although that multiblock can become unformed

### Requirement: Demand persistence does not create rewards or punishment

Normal save/load SHALL preserve the run kind, demanded item, quantity, tier, remaining processing time, paid-input state, and any earned pending reward. It SHALL NOT reroll, repay, duplicate output, or repeat a completed consequence. Invalid or missing demand data SHALL cancel that run harmlessly without granting a reward or discarding a separately valid earned progression state.

#### Scenario: Resume paid work
- **WHEN** a paid challenge is normally saved and restored with its valid structure and demanded item still available in the registry
- **THEN** it resumes the same remaining challenge without consuming another token

#### Scenario: Optional item disappears from a saved run
- **WHEN** a saved demand references an optional component absent from the current registry, or its quantity, tier, or deadline data is invalid
- **THEN** the run is cancelled without punishment or reward
- **AND** a valid saved ACTIVE or PREPARED progression remains unchanged

### Requirement: Demand is visible inside the structure

During a pending challenge, clients SHALL see the demanded item's icon, readable display name, and numerical quantity together inside the structure at its center. The display SHALL reflect server-selected data, remain consistent through suspension and reconnect, and clear after payment, timeout, or cancellation. The native machine UI SHALL report the demand, remaining deadline, progression, and pending-reward status when applicable.

#### Scenario: Readable world-space demand
- **WHEN** a player views a pending challenge from supported horizontal machine orientations
- **THEN** the correct item icon, readable item name, and numerical quantity are visibly rendered together inside the frame, not only in the controller GUI
- **AND** reconnecting or reloading resources does not change the selected demand or leave a stale completed demand visible

### Requirement: Recipes and help explain the challenge

Recipe viewers and machine/hatch help SHALL distinguish the two tokens, ACTIVE test and PREPARED real gates, tier-2/intention requirements, power-free single-run behavior, random demand payment, immediate success, tier-scaled deadlines, and timeout-only consequences. They SHALL NOT imply a fixed demanded component, a mandatory 30-second wait, collateral explosions, or punishment for cancellation.

#### Scenario: Discover the new progression step
- **WHEN** a player inspects the tamer recipes and intention-hatch help
- **THEN** the test shows a Desarium gem and preparation outcome, and the real run shows an Implementium gem and one essence ingot
- **AND** the dynamic demand, deadline rule, Safety sacrifice, single Bravery victim, and harmless interruption behavior are explained without clipped or internal-only captions
