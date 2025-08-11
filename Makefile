DC=docker compose -f platform/docker/compose.yml

.PHONY: up-core up-voice down logs rebuild demo

up-core:
	$(DC) --profile core up -d

up-voice:
	$(DC) --profile voice up -d

down:
	$(DC) down

logs:
	$(DC) logs -f | cat

rebuild:
	$(DC) build --no-cache

demo: up-voice

