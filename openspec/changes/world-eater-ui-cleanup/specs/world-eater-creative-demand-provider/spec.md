## ADDED Requirements

### Requirement: Any-side ME connection

The provider SHALL accept AE2 cable connections on every face, regardless of and after changing its facing. Its UI SHALL show only its ME connection state and pending delivery count.

#### Scenario: Cable on a non-front face
- **WHEN** an ME cable touches the provider on any face other than the front
- **THEN** the provider joins that ME network and offers its patterns
