# Suitability Engine Makefile
.PHONY: help build test lint clean run dev docker-build docker-run docker-stop ci cd

# Variables
APP_NAME := suitability-engine
VERSION := $(shell cat pom.xml | grep -oP '(?<=<version>)[^<]+' | head -1)
DOCKER_REGISTRY := 123456789.dkr.ecr.sa-east-1.amazonaws.com
DOCKER_IMAGE := $(DOCKER_REGISTRY)/investments/$(APP_NAME)

# Default target
help:
	@echo "Suitability Engine - Available commands:"
	@echo ""
	@echo "Build & Test:"
	@echo "  make build        - Compile and package (skip tests)"
	@echo "  make test         - Run unit tests"
	@echo "  make test-it      - Run integration tests"
	@echo "  make test-all     - Run all tests (unit + integration)"
	@echo "  make lint         - Run static analysis (Checkstyle, SpotBugs, PMD)"
	@echo "  make verify       - Run full verification (build + test + lint)"
	@echo ""
	@echo "Development:"
	@echo "  make run          - Run application locally (with docker-compose)"
	@echo "  make dev          - Start dev environment (docker-compose up)"
	@echo "  make stop         - Stop dev environment"
	@echo "  make logs         - Follow logs"
	@echo "  make clean        - Clean build artifacts"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-build - Build Docker image"
	@echo "  make docker-run   - Run Docker container"
	@echo "  make docker-push  - Push Docker image to registry"
	@echo ""
	@echo "CI/CD:"
	@echo "  make ci           - Run CI pipeline locally (build + test + lint)"
	@echo "  make cd           - Deploy to staging (requires Harness CLI)"
	@echo ""
	@echo "Database:"
	@echo "  make db-migrate   - Run Flyway migrations"
	@echo "  make db-reset     - Drop and recreate database"
	@echo ""
	@echo "Variables:"
	@echo "  VERSION=$(VERSION)"

# Build targets
build:
	./mvnw clean package -DskipTests -DskipITs=true -B

test:
	./mvnw test -B

test-it:
	./mvnw verify -DskipTests=false -DskipUnitTests=true -DtestIntelligence=true -B

test-all:
	./mvnw verify -DskipTests=false -DtestIntelligence=true -B

lint:
	./mvnw checkstyle:check spotbugs:check pmd:check -B

verify: build test lint

clean:
	./mvnw clean -B

# Development
run: dev

dev:
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	@sleep 10
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

stop:
	docker-compose down -v

logs:
	docker-compose logs -f

# Database
db-migrate:
	./mvnw flyway:migrate -B

db-reset:
	./mvnw flyway:clean flyway:migrate -B

# Docker
docker-build:
	docker build -t $(APP_NAME):$(VERSION) -t $(APP_NAME):latest .
	docker tag $(APP_NAME):$(VERSION) $(DOCKER_IMAGE):$(VERSION)
	docker tag $(APP_NAME):latest $(DOCKER_IMAGE):latest

docker-run:
	docker run -d \
		--name $(APP_NAME) \
		-p 8080:8080 \
		-e SPRING_PROFILES_ACTIVE=prod \
		-e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/suitability \
		-e DATABASE_USER=suitability \
		-e DATABASE_PASSWORD=suitability \
		$(APP_NAME):$(VERSION)

docker-push:
	docker push $(DOCKER_IMAGE):$(VERSION)
	docker push $(DOCKER_IMAGE):latest

# CI/CD
ci: verify
	@echo "CI pipeline completed successfully"

cd:
	@echo "Deploying to staging via Harness..."
	@harness pipeline run --project suitability-engine --pipeline suitability_ci_cd --build-number $(shell git rev-parse --short HEAD)

# Generate OpenAPI spec
openapi:
	./mvnw spring-boot:run -Dspring-boot.run.arguments="--springdoc.api-docs.enabled=true" &
	sleep 10
	curl -s http://localhost:8080/v3/api-docs > openapi.json
	pkill -f spring-boot:run

# Generate dependency graph
deps:
	./mvnw dependency:tree -Dverbose -Dincludes=br.com.itau > dependency-tree.txt

# Check for vulnerable dependencies
vuln-check:
	./mvnw org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7 -B

# License check
license-check:
	./mvnw license:check -B

# Update dependencies
update-deps:
	./mvnw versions:display-dependency-updates versions:display-plugin-updates -B

# Format code (requires google-java-format)
format:
	./mvnw com.coveo:fmt-maven-plugin:format -B

# Quick health check
health:
	curl -f http://localhost:8080/actuator/health/liveness

# Metrics
metrics:
	curl -s http://localhost:8080/actuator/prometheus | head -50

# Thread dump
threaddump:
	curl -f http://localhost:8080/actuator/threaddump

# Heap dump
heapdump:
	curl -f http://localhost:8080/actuator/heapdump -o heapdump.hprof