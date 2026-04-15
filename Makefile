.PHONY: dev-db backend frontend start stop logs

dev-db:
	docker compose -f docker-compose.dev.yml up -d --wait

backend:
	cd backend && ./mvnw spring-boot:run

frontend:
	cd frontend && npm run dev

start:
	docker compose up --build

stop:
	docker compose down
	docker compose -f docker-compose.dev.yml down

logs:
	docker compose logs -f
