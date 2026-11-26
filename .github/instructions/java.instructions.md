---
applyTo: "**/*.java,**/*.gradle,**/build.gradle,**/build.gradle.kts,**/settings.gradle,**/settings.gradle.kts"
---

# Java- und Build-Regeln

- Java 26 und Spring Boot 4.2.x als Ziel verwenden.
- Embedded Tomcat fuer lokale Entwicklung und Standardausfuehrung verwenden. IBM Liberty und IBM Semeru Runtime fuer Enterprise-Deployment beruecksichtigen; Laufzeitannahmen nicht vermischen.
- Jakarta-EE-Lifecycle und containerverwaltete Ressourcen beachten.
- Neue Gradle-Konfiguration in Groovy DSL (`build.gradle`, `settings.gradle`) erstellen. Bestehendes Kotlin-DSL nicht pauschal migrieren; bestehende `.gradle.kts`-Dateien gezielt konsistent pflegen.
- Bei Concurrency Thread-Safety, Lebenszyklus, Shutdown und Backpressure explizit bewerten. Keine unmanaged ExecutorServices in Container-Code erzeugen.
- In Liberty containerverwaltetes Threading, Managed Executors oder Managed Thread Factories verwenden. Lokale virtuelle Threads nur verwenden, wenn JDK 26, Projekt und Spring-Boot-4.2-Konfiguration dies unterstuetzen und die Projektkonventionen dies bestaetigen; keine erfundenen Property-Namen oder APIs verwenden.
- Spring-Scopes korrekt einsetzen: Spring-Beans sind standardmaessig Singleton. `@Singleton` nur fuer Jakarta-EE-/CDI-Semantik verwenden und nicht mit Spring-`@Scope`-Proxies vermischen.
- Reine JSON-REST-APIs mit `@RestControllerAdvice` und `@ExceptionHandler` behandeln.
- `@ConfigurationProperties` an Klassen oder Records mit `@Validated` und passender Bean-Validation kennzeichnen.
- Fuer Oracle SQL Bind-Variablen und eindeutige Transaktionsgrenzen an fachlichen Service-Grenzen verwenden; keine Benutzerdaten in SQL-/JPQL-Zeichenketten einbauen.
- Boot-4.2-, Liberty-, Container- und Semeru-spezifische APIs nur verwenden, wenn sie durch die vorhandenen Dependencies bzw. Projektkonfiguration verifiziert sind.
- Unit-, Service-, Integration- und relevante Container-/Runtime-Tests fuer Java 26 und die jeweils ausgewaehlte Spring-/Container-Kombination ergaenzen oder anpassen.
- Javadoc gezielt fuer oeffentliche oder geschützte API-Vertraege einsetzen, deren Verhalten oder Randfaelle nicht aus Signatur und Namen hervorgehen; keine umfassende Javadoc-Pflicht fuer triviale Typen oder Methoden erzwingen.