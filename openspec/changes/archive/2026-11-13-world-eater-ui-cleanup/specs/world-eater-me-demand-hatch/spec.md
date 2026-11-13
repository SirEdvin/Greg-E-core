## MODIFIED Requirements

### Requirement: Hatch help explains automation boundaries

The hatch's UI SHALL show only its ME connection state and its read-only demanded item and quantity, or that no real-run demand is present. Short localized help SHALL explain optional placement, real-run-only mirroring, the single autocrafting attempt and that there are no retries. It SHALL NOT promise guaranteed delivery, continuous retry, or automatic timeout protection.

#### Scenario: Inspect the automation hatch
- **WHEN** a player inspects the hatch and its help while online, offline, idle, and demanding an item
- **THEN** the displayed demand and connection state are accurate and readable
- **AND** the help states the single attempt without retries

## ADDED Requirements

### Requirement: Any-side ME connection

The hatch SHALL accept AE2 cable connections on every face, regardless of and after changing its facing.

#### Scenario: Cable on a non-front face
- **WHEN** an ME cable touches the hatch on any face other than the front
- **THEN** the hatch joins that ME network
