# Contributing to SmartJARVIS

Thank you for your interest in contributing to SmartJARVIS! This document provides guidelines for contributing to the project.

## Development Setup

### Prerequisites
- **Java**: JDK 17+ (Temurin recommended)
- **Node.js**: 18+ (LTS recommended)
- **Python**: 3.10+ (3.11 recommended)
- **Docker**: Latest stable
- **Git**: 2.30+

### Local Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/gajdukiewicz00/smart-jarvis.git
   cd smart-jarvis
   ```

2. **Install dependencies**
   ```bash
   # Java modules
   mvn clean install -DskipTests
   
   # Node.js services
   cd nlp-engine && npm ci && cd ..
   
   # Python services
   cd speech-service && pip install -r requirements.txt && cd ..
   ```

3. **Run tests locally**
   ```bash
   # All tests
   bash ./ci/run_all_tests.sh
   
   # Individual services
   mvn test                    # Java
   npm test                    # Node.js
   pytest                      # Python
   ```

## Code Quality

### Linting & Formatting

- **Python**: Use `ruff` and `flake8`
  ```bash
  ruff check .
  flake8 .
  ```

- **TypeScript/JavaScript**: Use `eslint`
  ```bash
  npm run lint
  ```

- **Java**: Follow Maven checkstyle
  ```bash
  mvn checkstyle:check
  ```

### Testing

- **Unit tests**: Required for all new features
- **Integration tests**: Required for API changes
- **E2E tests**: Required for critical user flows

### Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description

feat(auth): add OAuth2 authentication
fix(api): resolve timeout issue in user service
docs(readme): update installation instructions
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

## Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feat/your-feature-name
   ```

2. **Make your changes**
   - Write tests for new functionality
   - Update documentation as needed
   - Ensure all tests pass locally

3. **Submit a Pull Request**
   - Use the PR template
   - Link any related issues
   - Ensure CI passes

### PR Checklist

- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] CI passes (lint + test + build)
- [ ] No breaking changes without migration guide
- [ ] Conventional commit format used

## Docker Development

### Build Services
```bash
# Individual services
docker build -t speech-service:dev speech-service/
docker build -t nlp-engine:dev nlp-engine/
docker build -t jarvis-desktop:dev jarvis-desktop/
docker build -t task-service:dev task-service/

# All services
docker compose build
```

### Run Services
```bash
# Start all services
docker compose up -d

# Check health
docker compose ps
curl http://localhost:8080/health

# View logs
docker compose logs -f service-name
```

## Architecture Guidelines

- **Microservices**: Each service should be independently deployable
- **API-First**: Design APIs before implementation
- **Event-Driven**: Use events for loose coupling
- **Observability**: Include logging, metrics, and tracing

## Getting Help

- **Issues**: Use GitHub Issues for bug reports and feature requests
- **Discussions**: Use GitHub Discussions for questions and ideas
- **Code Review**: All PRs require review before merging

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.
