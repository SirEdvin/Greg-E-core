## MODIFIED Requirements

### Requirement: Live scan feedback

The native machine UI SHALL NOT display scan chunk coordinates; scan progress is reported only through the next-step hint. Every newly counted eligible machine SHALL emit one purple particle from the center of its block to the geometric center of the structure. Ordinary blocks, excluded parts, failed batch preflights, suspension and completed report delivery SHALL NOT emit discovery particles. The cosmetic effect SHALL use native particle networking without loading chunks or introducing client-only classes into server execution.

#### Scenario: Pending chunk feedback
- **WHEN** a survey is running
- **THEN** the native UI hint reports survey progress and no chunk coordinates

#### Scenario: Discovery effect
- **WHEN** the server scan counts an eligible machine
- **THEN** nearby clients receive one purple particle beginning at that machine and traveling to the structure's geometric center
- **WHEN** the scan visits an ordinary block, an excluded hatch, or its own controller
- **THEN** it emits no discovery particle for that block

## ADDED Requirements

### Requirement: One next-step hint in the controller UI

Below the native GTCEu display lines, a formed tamer's UI SHALL show exactly one localized line naming the player's next step toward real runs, and no other tamer-specific line. An unformed tamer SHALL show no hint. Steps in priority order: free output space for a waiting reward or report; deliver the open demand (count, item, seconds left); DORMANT survey progress or IV Sensor; casing upgrade below tier 2; intention hatch; Desarium (ACTIVE) or Implementium (PREPARED).

#### Scenario: Tier-1 ACTIVE tamer
- **WHEN** an ACTIVE tamer is formed with a black inner-ring casing
- **THEN** its only tamer-specific line tells the player to replace the middle casings with colored ones to upgrade to tier 2

#### Scenario: Ready for real runs
- **WHEN** a PREPARED tier-2 tamer has an intention hatch and no open demand
- **THEN** its hint tells the player to insert Implementium for real runs
