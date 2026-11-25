# Spring Boot + Angular Demo

Neues Gradle-Multiprojekt mit:

- `backend`: Spring Boot (Java 25)
- `frontend`: Angular (aktuelle stabile Major-Version 20)
- Frontend-Build wird in `backend` als statische Ressourcen eingebettet und vom Spring-Boot-Server ausgeliefert.

## Build

```bash
./gradlew clean build
```

Artefakt:

```text
backend/build/libs/backend-0.0.1-SNAPSHOT.jar
```

## Start

```bash
java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar
```

App: `http://localhost:8080`

## Entwicklung

Backend:

```bash
./gradlew :backend:bootRun
```

Frontend (Angular Dev-Server mit API-Proxy):

```bash
./gradlew :frontend:npmDev
```

Frontend Dev: `http://localhost:5173`

## Beispiel-API

- `GET /api/tasks`
- `POST /api/tasks`
- `DELETE /api/tasks/{id}`

