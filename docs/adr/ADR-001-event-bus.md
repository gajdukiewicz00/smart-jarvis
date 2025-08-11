### ADR-001: Event Bus (Kafka + Avro + Schema Registry)

- Decision: Use Kafka (KRaft single-node) with Confluent Schema Registry
- Contracts: Avro, namespace `ai.smartjarvis.v1`, topics prefixed `sj.*.v1`
- Keys: partition by `sessionId`, carry `correlationId` in headers and payload
- Rationale: decoupling, evolvable contracts, compatibility checks in CI
