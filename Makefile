.PHONY: dev stop logs db setup

setup:
	docker-compose pull
	cd frontend && npm install

dev:
	docker-compose up -d
	@echo "Waiting for MySQL..."
	@sleep 8
	JAVA_HOME=/usr/lib/jvm/java-21-openjdk backend/mvnw -f backend/pom.xml spring-boot:run &
	cd frontend && npm run dev

stop:
	docker-compose down
	@pkill -f "spring-boot:run" 2>/dev/null || true
	@pkill -f "vite" 2>/dev/null || true

logs:
	docker-compose logs -f db

db:
	docker-compose up -d
