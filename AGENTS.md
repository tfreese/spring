# AGENTS — How to work with this repository

Kurzcheckliste
- Projekt-Build: `./gradlew build` (root) — einzelne Module: `./gradlew -p <module> bootRun` oder `./gradlew :<project>:bootRun`
- Container/infra: `kubernetes/docker-build.sh`, `kubernetes/kubernetes-deploy.sh`, `spring-resilience/buildAndStart.sh`
- AI modules: `spring-ai/ai-server`, `spring-ai/mcp-server`, `spring-ai/spring-ai-chatbot`

Schnellüberblick (Big Picture)
- Multi-project Gradle-Repo; die enthaltenen Module stehen in `settings.gradle`. Wichtige areas:
  - `spring-ai/` — AI demo & servers (MCP, ai-server, chatbot)
  - `kubernetes/` — manifests, Dockerfiles und deploy-Skripte für local/minikube
  - `spring-cloud/` — cloud samples (docking via `docker-compose.yml`)
  - viele `spring-*` Module — einzelne demos, meist eigenständige Spring Boot Apps

Wichtige Dateien zum Verstehen
- `settings.gradle` — welche Subprojekte existieren und wie das Multi-project aufgebaut ist.
- `build.gradle` (root) — gemeinsame Konventionen/DependencyManagement für Subprojects.
- `kubernetes/*` — Deployment-Pattern: PV/PVC, Secrets, Service/Deployment separation; Beispiel: `50_backend_deployment.yml` + `51_backend_service.yml`.
- `kubernetes/backend/Dockerfile` und `spring-resilience/Dockerfile` — Runtime-IMAGE pattern: alpine JRE, COPY jar, ENV VM_PARAMS, ENTRYPOINT `java -jar`.
- `spring-ai/ai-server/src/main/resources/application.yml` — zeigt AI-spezifische Konfiguration: `spring.ai`, `openai.api-key`/`base-url` und verwendete Modelle (z.B. `text-embedding-3-large`).

Developer Workflows & Commands (konkret)
- Build gesamtes Repo: `./gradlew build`
- Baue und starte ein Modul lokal: im Repo-Root `./gradlew -p spring-ai/ai-server bootRun` oder von Modul-Ordner `./gradlew bootRun`.
- Docker-Image build & push (backend): `./kubernetes/docker-build.sh` — baut Backend-Module und versieht images/tags.
- Deployment (minikube/local k8s): `./kubernetes/kubernetes-deploy.sh` — lädt Images in minikube, apply/delete der manifests, führt abschließenden curl check aus.
- Docker Compose (spring-cloud): `./spring-cloud/docker-compose.yml` + `./spring-cloud/docker-build.sh` (beachte: spring-cloud uses maven in script).
- Start resilient demo: `./spring-resilience/buildAndStart.sh` (build + docker-compose up)
- Schlüssel erzeugen (JWT demo): `./spring-jwt/createKeys.sh` (openssl commands)

Project-specific conventions & patterns
- Mixed build tools: predominantly Gradle, aber some `spring-cloud` scripts use Maven (`mvn package`) — check per-module scripts before assuming Gradle.
- Docker images rely on `VM_PARAMS`/`JAR_FILE` build-args and `ENTRYPOINT` `java -jar /app/app.jar` — search for `VM_PARAMS` when tuning JVM args.
- K8s manifests reference secrets and PVCs under `kubernetes/` — credentials are wired via secrets; do not hardcode local secrets into agent changes.
- AI configuration lives under `spring-ai/*/src/main/resources/application.yml` — agents modifying AI features must update these files and consider model/endpoint properties.

Integration points & external deps to watch
- Kubernetes (minikube) — `kubernetes/*.yml` and deploy scripts expect a local k8s environment.
- Docker registry flows are present (tagging/pushing in scripts) — CI may expect images to be pushed to a registry.
- External AI runtimes: README in `spring-ai/` references Ollama usage and model serving — local model endpoints may be required for experiments.

Where to look first (recommended order for new agents)
1. `settings.gradle` and root `build.gradle` — understand subprojects and shared conventions
2. Module README (`spring-ai/README.adoc`, `spring-resilience/README.adoc`) for run-order and infra notes
3. `kubernetes/` scripts & manifests — deployment pattern, secrets, DB PVCs
4. `spring-ai/*/src/main/resources/application.yml` — AI config and sample models
5. Dockerfiles under `kubernetes/` and `spring-resilience/` for runtime assumptions

Quick examples to copy-paste
- Build root: `./gradlew build`
- Run ai-server: `./gradlew -p spring-ai/ai-server bootRun` (or cd into module + `./gradlew bootRun`)
- Deploy locally to minikube: `./kubernetes/docker-build.sh && ./kubernetes/kubernetes-deploy.sh`

Notes for agents
- Prefer reading `README.adoc` files in each module before modifying behavior — they contain run-order and external dependency hints.
- Do not add credentials into commits; use k8s Secrets or local environment vars referenced in `application.yml`.
- When changing AI model defaults, update `spring-ai/*/application.yml` and note model names and embedding dims (documented inline).

End of AGENTS.md

