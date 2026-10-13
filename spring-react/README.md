# Spring Boot + React Task-Demo

Das Projekt demonstriert eine kleine Task-Verwaltung:

- Tasks laden: `GET /api/tasks`
- Task anlegen: `POST /api/tasks`
- Task löschen: `DELETE /api/tasks/{id}`
- React + TypeScript + Vite
- Spring Boot mit Java-25-Toolchain
- Vollständiger Build mit Gradle Kotlin DSL
- Integration des gebauten React-Frontends in das Spring-Boot-JAR

## Build

Voraussetzungen: JDK 25 und Gradle 8.14 oder neuer. Node.js wird vom Gradle-Node-Plugin bereitgestellt.

```bash
gradle clean build
```

Das ausführbare JAR entsteht unter:

```text
backend/build/libs/backend-1.0.0-SNAPSHOT.jar
```

Start:

```bash
java -jar backend/build/libs/backend-1.0.0-SNAPSHOT.jar
```

Anwendung: `http://localhost:8080`

## Entwicklungsmodus

Backend:

```bash
gradle :backend:bootRun
```

Frontend mit Vite und Proxy auf Port 8080:

```bash
gradle :frontend:npmDev
```

Frontend: `http://localhost:5173`

# React-Frontend: Funktionsweise & Referenz

Analyse des Vite/React/TypeScript-Frontends unter `gradle-projects/misc/spring-boot-react/frontend`,
das zusammen mit dem Gradle-Multiprojekt-Modul `backend` (Spring Boot 4, Java 25) eine Task-Demo-App bildet.

## 1. Gesamtarchitektur

- **Frontend**: React 19 + TypeScript, gebaut mit Vite 7. SPA ohne Router (nur eine Seite/Komponente).
- **Backend**: Spring Boot (WebMVC) liefert unter `/api/tasks` eine REST-API für ein einfaches Task-CRUD (GET/POST/DELETE).
- **Integration**:
    - Im Dev-Betrieb läuft das Frontend über den Vite-Devserver (Port `5173`) und proxyt `/api/*`-Requests an das Backend (`localhost:8080`).
    - Im Produktions-Build wird `npm run build` (via Gradle-Task `npmBuild`) ausgeführt, das Ergebnis landet in `frontend/dist/` und wird vom Backend-Gradle-Modul
      (`backend/build.gradle.kts`, Task `processResources`) als statische Ressource (`static/`) in das Spring-Boot-JAR kopiert. Zur Laufzeit liefert Spring Boot dann sowohl die
      SPA-Dateien als auch die REST-API aus einem einzigen JAR/Prozess.
    - Das Gradle-Modul `frontend` ist rein "nativ Node/npm"-basiert und wird über das Plugin `com.github.node-gradle.node` in den Gradle-Build eingebunden (kein separates Node/npm
      muss vorinstalliert sein).

## 2. Dateien und ihre Funktion

### `frontend/index.html`

Einstiegspunkt für Vite. Enthält:

- `<div id="root">` als Mount-Punkt für React.
- `<script type="module" src="/src/main.tsx">` – Vite bindet damit den TypeScript-Einstiegspunkt direkt ein (kein manuelles Bundling nötig im Dev-Modus).
- Meta-Tags (Charset, Viewport, Beschreibung) und Titel der Seite.

### `frontend/src/main.tsx`

Bootstrap der React-Anwendung:

- `createRoot(...)` (React 19 API) rendert die `<App />`-Komponente in das `#root`-Element.
- `<StrictMode>` aktiviert zusätzliche Entwicklungs-Checks (doppelte Effekt-Ausführung, Warnungen bei veralteten APIs).
- Importiert global `./styles.css`.

### `frontend/src/App.tsx`

Die einzige fachliche Komponente der App – eine Task-Liste mit CRUD-Funktionalität:

- **State** (via `useState`):
    - `tasks: Task[]` – geladene Aufgaben (`{ id: number, title: string }`).
    - `title` – Eingabewert des Formularfelds für neue Tasks.
    - `loading` / `saving` – UI-Zustände für Ladeanzeige bzw. Button-Disable während Requests.
    - `error` – Fehlermeldung, die im UI als Alert angezeigt wird.
- **`loadTasks` (useCallback)**: GET `/api/tasks`, befüllt `tasks`. Wird per `useEffect` beim ersten Rendern aufgerufen sowie durch den "Neu laden"-Button.
- **`createTask`**: POST `/api/tasks` mit JSON-Body `{ title }`. Bei Erfolg wird der neu angelegte Task lokal an die Liste angehängt (kein komplettes Neuladen nötig) und das
  Eingabefeld geleert.
- **`deleteTask`**: DELETE `/api/tasks/{id}`, entfernt den Task bei Erfolg lokal aus dem State.
- Alle Fetch-Aufrufe prüfen `response.ok` und werfen andernfalls einen `Error` mit HTTP-Status, der im `catch`-Block in `error` landet und im UI angezeigt wird.
- Fetch-Pfade sind relative Pfade (`/api/tasks`) – funktionieren sowohl über den Vite-Proxy (Dev) als auch direkt vom Spring-Boot-Server (Prod), da beide auf denselben Origin
  abzielen bzw. der Proxy das transparent macht.

### `frontend/src/styles.css`

Reines CSS (kein CSS-Modul/Framework), global über `main.tsx` eingebunden:

- Definiert Basis-Typografie (`Inter`/System-Fonts), Farbschema, Card-Layout für Formular und Task-Liste.
- Responsive Anpassung via `@media (max-width: 560px)` für mobile Ansicht (Formular- und Listenelemente werden vertikal gestapelt).

### `frontend/package.json`

NPM-Manifest:

- `"type": "module"` – Projekt nutzt ESM (relevant für Node-Skripte/Konfigurationsdateien wie `frontend/vite.config.ts`).
- **Scripts**:
    - `dev` → startet Vite-Devserver (Hot Module Reload).
    - `build` → `tsc -b` (TypeScript-Projektreferenzen-Build, reine Typprüfung/Emit von Deklarationsartefakten falls konfiguriert) gefolgt von `vite build` (produktives Bundling
      nach `frontend/dist`).
    - `preview` → startet einen lokalen Server, der den `dist`-Build wie in Produktion ausliefert (zum manuellen Testen des Builds).
- **dependencies**: Laufzeitabhängigkeiten – `react`, `react-dom` (19.1.1), `vite` + `@vitejs/plugin-react` (JSX/Fast-Refresh-Unterstützung für Vite), `typescript` (wird hier
  bewusst als "dependency" statt "devDependency" geführt, da `tsc` im Build-Skript verwendet wird).
- **devDependencies**: Nur Typdefinitionen `@types/react`, `@types/react-dom` (für die TypeScript-Prüfung, nicht zur Laufzeit benötigt).

### `frontend/tsconfig.json`

TypeScript-Compiler-Konfiguration für Vite/React:

- `target: ES2022`, `lib: ["ES2022","DOM","DOM.Iterable"]` – Zielsprachniveau inkl. Browser-APIs.
- `module: ESNext` + `moduleResolution: Bundler` – passend für Vite, das ESM nativ aushandelt (kein CommonJS-Interop-Zwang zur Laufzeit).
- `jsx: react-jsx` – neue JSX-Transform (React 17+), kein manuelles `import React from 'react'` in `.tsx`-Dateien nötig.
- `strict: true` – volle strikte Typprüfung (u. a. `strictNullChecks`, `noImplicitAny`).
- `noEmit: true` + `isolatedModules: true` – TypeScript wird hier nur zur Typprüfung genutzt; das eigentliche Transpilieren/Bundling übernimmt Vite (esbuild/Rollup).
  `isolatedModules` stellt sicher, dass jede Datei unabhängig transpilierbar ist (Voraussetzung für datei-basierte Transpiler wie esbuild).
- `useDefineForClassFields: true` – korrektes ES2022-Klassenfeld-Verhalten (relevant falls Klassenkomponenten/Decorators verwendet würden).
- `skipLibCheck: true` – überspringt Typprüfung in `.d.ts`-Dateien von Bibliotheken (schnellerer Build, vermeidet Fehler durch inkompatible Fremd-Typdefinitionen).
- `include: ["src", "vite.config.ts"]` – nur diese Pfade werden von `tsc -b` geprüft.

### `frontend/vite.config.ts`

Zentrale Vite-Konfiguration:

- `plugins: [react()]` – aktiviert JSX-Transform und React Fast Refresh im Dev-Modus.
- `server.port: 5173` – Port des Dev-Servers.
- `server.proxy: { '/api': 'http://localhost:8080' }` – alle Frontend-Requests an `/api/**` werden im Dev-Modus an das lokal laufende Spring-Boot-Backend weitergeleitet.
  **Wichtig**: Backend muss separat gestartet sein (z. B. via `./gradlew :backend:bootRun`), damit die API im Dev-Modus erreichbar ist.
- `build.outDir: 'dist'` – Zielverzeichnis für den Produktionsbuild (wird vom Backend-Gradle-Modul referenziert).
- `build.emptyOutDir: true` – löscht `frontend/dist` vor jedem Build (verhindert veraltete Artefakte).
- `build.sourcemap: true` – erzeugt Sourcemaps für Debugging des produktiven Bundles im Browser.

### `build.gradle.kts` (Gradle-Modul `frontend`)

Bindet das npm/Vite-Projekt in den Gradle-Multiprojekt-Build ein:

- **Plugins**: `base` (liefert Standard-Lifecycle-Tasks wie `build`/`clean`), `com.github.node-gradle.node` (Version 7.1.0) – lädt bei Bedarf eine eigene Node/npm-Distribution
  herunter, sodass kein global installiertes Node erforderlich ist.
- **`node { ... }`-Block**:
    - `download.set(true)` – Node wird automatisch heruntergeladen (reproduzierbarer Build, unabhängig vom Entwicklerrechner).
    - `version.set("22.14.0")` – feste Node-Version.
    - `npmVersion.set("10.9.2")` – feste npm-Version.
    - `nodeProjectDir.set(projectDir)` – Arbeitsverzeichnis für npm-Befehle (hier: das `frontend`-Modul selbst).
- **Task `npmDependencies`** (eigener Task, custom benannt): Installiert Abhängigkeiten.
    - Nutzt `npm ci`, falls `frontend/package-lock.json` existiert (reproduzierbare, schnelle Installation), sonst Fallback auf `npm install`.
    - `inputs`/`outputs` sind für Gradle-Up-to-date-Checks definiert (`frontend/package.json`, ggf. Lockfile → `node_modules`), damit der Task bei unveränderten Eingaben
      übersprungen wird (Caching).
- **Task `npmBuild`**: Führt `npm run build` aus (→ `tsc -b && vite build`).
    - Abhängig von `npmDependencies`.
    - `inputs`: Quellverzeichnis `src`, sowie `frontend/index.html`, `package.json`, `tsconfig.json`, `vite.config.ts`.
    - `outputs.dir("dist")` – ermöglicht Gradle-Caching/Incremental Build.
- **Task `npmDev`**: Startet `npm run dev` (Vite-Devserver) über Gradle (z. B. `./gradlew :frontend:npmDev`).
- **`tasks.named("build") { dependsOn(npmBuild) }`**: Hängt den Frontend-Build in den Standard-Gradle-Lifecycle `build` ein.
- **`tasks.named<Delete>("clean") { delete("dist") }`**: Ergänzt den Standard-`clean`-Task um das Löschen von `frontend/dist`.

## 3. Zusammenspiel mit dem Backend

`backend/build.gradle.kts` (Auszug, relevant für Frontend-Integration):

```kotlin
tasks.processResources {
    dependsOn(":frontend:npmBuild")
    from(project(":frontend").layout.projectDirectory.dir("dist")) {
        into("static")
    }
}
```

- Vor dem Bauen der Backend-Ressourcen wird zwingend `:frontend:npmBuild` ausgeführt.
- Der Inhalt von `frontend/dist/` wird als `static/`-Ressource in das Spring-Boot-JAR übernommen → Spring Boot liefert die SPA automatisch über den eingebauten statischen
  Ressourcen-Handler aus (Root-Pfad `/`).
- `settings.gradle.kts` bindet beide Module via `include("backend", "frontend")` ein.

## 4. Typische Befehle

| Zweck                           | Befehl                                                                             |
|---------------------------------|------------------------------------------------------------------------------------|
| Nur Frontend-Dev-Server starten | `./gradlew :frontend:npmDev` (oder direkt `npm run dev` im `frontend`-Verzeichnis) |
| Backend im Dev-Modus starten    | `./gradlew :backend:bootRun`                                                       |
| Kompletten Produktions-Build    | `./gradlew build` (baut Frontend, kopiert `frontend/dist` ins Backend-JAR)         |
| Nur Frontend bauen              | `./gradlew :frontend:npmBuild`                                                     |
| Frontend-Build lokal ansehen    | `npm run preview` (im `frontend`-Verzeichnis, nach `npm run build`)                |
| Aufräumen                       | `./gradlew clean` (löscht u. a. `frontend/dist`)                                   |

## 5. Wichtige Konfigurationsparameter im Überblick

| Datei                         | Parameter                          | Bedeutung                                                                                      |
|-------------------------------|------------------------------------|------------------------------------------------------------------------------------------------|
| `frontend/vite.config.ts`     | `server.port`                      | Port des Dev-Servers (Standard hier: 5173)                                                     |
| `frontend/vite.config.ts`     | `server.proxy['/api']`             | Ziel-Backend-URL für API-Proxy im Dev-Modus                                                    |
| `frontend/vite.config.ts`     | `build.outDir`                     | Ausgabeverzeichnis des Produktions-Builds (muss mit Backend-`build.gradle.kts` übereinstimmen) |
| `frontend/vite.config.ts`     | `build.sourcemap`                  | Erzeugung von Sourcemaps für Debugging                                                         |
| `frontend/tsconfig.json`      | `strict`                           | Aktiviert strikte TypeScript-Typprüfung                                                        |
| `frontend/tsconfig.json`      | `jsx`                              | JSX-Transform-Modus (`react-jsx` = automatischer Import)                                       |
| `frontend/tsconfig.json`      | `moduleResolution`                 | Muss zu Vite/Bundler-Umgebung passen (`Bundler`)                                               |
| `build.gradle.kts` (frontend) | `node.version` / `node.npmVersion` | Reproduzierbare Node/npm-Version für den Build, unabhängig vom Host-System                     |
| `build.gradle.kts` (frontend) | `node.download`                    | Steuert, ob Node automatisch heruntergeladen wird                                              |
| `frontend/package.json`       | `scripts.dev/build/preview`        | Einstiegspunkte für npm/Gradle-Tasks                                                           |

## 6. Hinweise / Fallstricke

- Der Vite-Proxy greift **nur im Dev-Modus** (`npm run dev`); im Produktionsbuild gibt es keinen Proxy mehr – dort müssen Frontend und Backend zwingend vom selben Origin (Spring
  Boot liefert beides aus einem Prozess) bedient werden, sonst schlagen die relativen `/api`-Fetch-Aufrufe fehl (CORS).
- `npm ci` wird nur verwendet, wenn eine `frontend/package-lock.json` existiert. Falls diese fehlt oder nicht eingecheckt ist, sollte sie für reproduzierbare Builds erzeugt und
  versioniert
  werden (`npm install` erzeugt sie).
- React 19 + `createRoot` erfordert, dass `#root` im HTML existiert (`document.getElementById('root')!` – der Non-Null-Assertion-Operator setzt das voraus; fehlt das Element, gibt
  es zur Laufzeit einen Fehler).
- `StrictMode` führt in der Entwicklung Effekte/Renderfunktionen teils doppelt aus – kein Bug, sondern gewolltes Verhalten zur Fehlererkennung (z. B. Seiteneffekte in `useEffect`).
- Es gibt keinen State-Management-Layer (Redux/Zustand/Context) und keinen Router – die App ist bewusst minimal als Demo für die Gradle/Spring-Boot/React-Integration gehalten.
