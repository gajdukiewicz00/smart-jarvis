### ADR-003: Correlation and Tracing

- Correlation: `correlationId` per request, `sessionId` per conversation
- OTel resource attrs: service.name, service.version, deployment.environment
- Span attributes include session.id and correlation.id
