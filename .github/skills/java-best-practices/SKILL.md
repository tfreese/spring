---
name: java-best-practices
description: 'Sicherstellen, dass Java-/Spring-Boot-Code den bewährten Vorgehensweisen der jeweiligen Lösung und des Projekts entspricht.'
---

# Best Practices für Java und Spring Boot

Stelle sicher, dass der Java-Code in ${selection} die für diese Lösung und dieses Projekt geltenden Best Practices erfüllt.
Plattformziel: Java 26 und Spring Boot 4.2.x.
Dazu gehören:

## Dokumentation und Struktur

- Javadoc gezielt fuer oeffentliche oder geschützte API-Typen und Methoden einsetzen, deren Vertrag nicht aus Namen und Signatur eindeutig hervorgeht. Fuer unveränderliche
  Trivial-DTOs, offensichtliche private Methoden oder einfache Getter/Setter keine flächendeckenden Kommentare erzwingen.
- In Javadoc nur tatsächlich relevante `@param`, `@return` und `@throws`-Informationen aufnehmen; Übersetzungs-/Dokumentationsfehler vermeiden.
- Die vorgegebene Paketstruktur einhalten: `{model|data|service|web}.{feature}`.
- Jede Klassendatei auf eine einzige Zuständigkeit ausrichten.

## Entwurfsmuster und Architektur

- Für alle von Spring verwalteten Abhängigkeiten Konstruktorinjektion verwenden (Feldinjektion mit `@Autowired` vermeiden).
- Injizierte Felder als `final` deklarieren, damit sie nach der Konstruktion unveränderlich sind.
- Das Interface-Segregation-Prinzip mit eindeutigen Namenskonventionen anwenden; Interfaces nach ihren Fähigkeiten benennen, zum Beispiel `TransactionRepository` oder
  `BudgetService`.
- Für die komplexe Objekterzeugung das Factory-Muster verwenden.
- Für unveränderliche Datenträger und DTOs `record`-Typen verwenden, wo das Projektmodell dies unterstützt.

## Abhängigkeitsinjektion und Services

- Konstruktorbasierte Abhängigkeitsinjektion verwenden — Spring erkennt einzelne Konstruktoren automatisch, `@Autowired` ist dafür nicht erforderlich.
- Spring-Beans sind standardmäßig Singleton. Für Spring-Scopes gezielt `@Scope`, `@RequestScope`, `@SessionScope` oder einen passenden Custom Scope verwenden.
- `@Singleton` ausschließlich für Jakarta-EE-/CDI-Semantik interpretieren, nicht als Ersatz für eine Spring-Bean-Scope-Konfiguration.
- Spring-`@Scope`-Proxies nicht mit Jakarta-Scopes vermischen. Eine Proxy-Konfiguration nur dort verwenden, wo ein kurzes Scope-Objekt korrekt in ein langlebiges Objekt injiziert
  werden muss.
- Spring-Stereotypen wie `@Service`, `@Repository` und `@Component` korrekt verwenden.
- Service-Interfaces zur Unterstützung der Testbarkeit verwenden, sofern Abstraktion im Projekt einen Mehrwert bietet; keine künstlichen Repository-Schichten hinzufügen.

## Spring Data JPA

- Spring-Data-JPA-Repositories direkt verwenden — keine zusätzlichen Repository-Abstraktionsschichten einführen.
- Abfragelogik in Repositories oder JPQL-`@Query`-Annotationen halten; Geschäftslogik in Services belassen.
- Für Abfragen einzelner Entitäten `Optional<T>` als Rückgabetyp verwenden.
- `@Transactional` mit einer eindeutigen fachlichen Service-Grenze einsetzen. Schreiboperationen nicht aus Controllern starten und nicht in View-/Serialisierungscode hineinziehen.
- Commit-/Rollback-Grenzen verstehen; nach einem Commit keine Datenänderungen derselben Transaktion unterstellen.
- Oracle-SQL mit Bind-Variablen und typisierten Parametern verwenden. Benutzereingaben niemals in SQL- oder JPQL-Zeichenketten konkatenieren.
- N+1-Abfragen vermeiden — bei Bedarf `JOIN FETCH` oder `@EntityGraph` zum Laden verwenden.

## Asynchrone Muster und Runtime

- Keine unmanaged `ExecutorService`-Instanzen in Jakarta- oder Liberty-Container-Code erzeugen.
- Für Embedded-Tomcat lokale Spring-Ausfuehrung nutzen; für Enterprise-Deployment IBM Liberty mit Semeru und den containerverwalteten Threading-Mechanismen der Plattform einsetzen.
- Virtual Threads in der lokalen Tomcat-/Spring-Ausfuehrung nur verwenden, wenn Project- und Boot-Version sie unterstützen und die Projektkonfiguration dies bestätigt. Keine nicht
  verifizierten Property-Namen oder APIs erfinden.
- Virtual Threads behandeln Task-Lifecycle, Shutdown und Backpressure nicht automatisch. Timeouts, Task-Grenzen, Queue-/Durchsatzbegrenzung und deterministischen Shutdown
  festlegen.
- In Liberty keine eigenen Thread-Pools oder virtuellen Threads verwenden, wenn ein Managed Executor oder Managed Thread Factory der Plattform vorgesehen ist.
- `@Async` nur dort einsetzen, wo Spring Lifecycle, Fehler- und Completion-Semantik passend abgebildet werden. Aufrufer müssen Fehler und Completion eindeutig behandeln.

## Teststandards

- JUnit 5 verwenden (`@Test`, `@BeforeEach`, `@AfterEach`, `@ExtendWith`).
- Mockito zum Mocken von Abhängigkeiten verwenden, sofern Unit-Test-Isolation erforderlich ist.
- AssertJ für ausdrucksstarke Assertions verwenden.
- Dem AAA-Muster folgen (Arrange, Act, Assert).
- Erfolgs-, Fehler-, Grenz- und Transaktionsfälle testen.
- Für Integrationstests `@SpringBootTest` passend zur Spring-Boot-4.2-Testunterstützung verwenden; für Unit-Tests direkte JUnit-/Mockito-Tests verwenden.
- Container-, Datenbank- oder HTTP-Integrationstools nur einsetzen, wenn sie bereits von Projektdependencies und Zielruntime unterstützt werden.
- Testabdeckung auf Java 26, die gewählte Spring-Boot-4.2-Konfiguration und auf Tomcat beziehungsweise Liberty/Semeru abstimmen.

## Konfiguration und Einstellungen

- `@ConfigurationProperties` mit einem `record` oder POJO für typsichere Konfiguration verwenden.
- Konfigurationswerte an einer `@ConfigurationProperties`-Klasse oder einem Record mit `@Validated` validieren; Bean-Validation-Annotationen wie `@NotNull`, `@NotBlank` oder `@Min`
  nur am geeigneten Grenzwert verwenden.
- Einstellungen aus `application.properties` oder `application.yml` binden.
- `@Profile` gezielt verwenden, um nachweislich getrennte Umgebungsbeans zu konfigurieren.
- Neue Gradle-Konfiguration in Groovy DSL (`build.gradle`, `settings.gradle`) erstellen. Bestehendes Kotlin-DSL nicht pauschal migrieren; vorhandene `.gradle.kts`-Dateien nur
  gezielt konsistent pflegen.
- Spring-Boot-4.2-, Container- und Semeru-spezifische Features nur anhand der tatsächlich geladenen Projektversion und dokumentierter Projektkonventionen verwenden; keine
  erfundenen APIs oder Property-Namen ergänzen.

## Geldbeträge und Finanzwerte

- Für Geldbeträge immer `BigDecimal` verwenden — niemals `double` oder `float`.
- Skalierung und Rundungsmodus explizit festlegen, zum Beispiel `amount.setScale(2, RoundingMode.HALF_UP)`.
- `BigDecimal.ZERO`, `BigDecimal.ONE` usw. statt `new BigDecimal(0)` verwenden.
- Geldbeträge im Datenbankschema als `DECIMAL(19,4)` speichern, sofern das Datenmodell keine andere fachliche Skala verlangt.

## Fehlerbehandlung und Protokollierung

- SLF4J mit Logback (dem Spring-Boot-Standard) verwenden — über `LoggerFactory.getLogger(MyClass.class)` oder projekttypische Logger-Integration einbinden.
- Parametrisierte Logmeldungen verwenden: `log.info("Processing order {}", id)` — niemals Zeichenketten verketten.
- Spezifische, aussagekräftige Ausnahmen auslösen; bei Bedarf eigene Anwendungsausnahmen definieren, die von `RuntimeException` abgeleitet sind.
- Für reine JSON-REST-APIs Ausnahmen in der Web-Schicht mit `@RestControllerAdvice` und `@ExceptionHandler` behandeln. Nur bei bewusstem MVC-/View-Anwendungsfall
  `@ControllerAdvice` erwägen.
- Aussagekräftigen Kontext in Ausnahmemeldungen aufnehmen.

## Leistung und Sicherheit

- Java-26-Funktionen nur verwenden, wenn Projektkompatibilität und Runtime-Unterstützung tatsächlich gegeben sind.
- Alle externen Eingaben an Systemgrenzen validieren — bei Controller-Methodenparametern Bean Validation mit `@Valid` verwenden.
- Parametrisierte JPA-Abfragen oder Spring-Data-Methoden verwenden — niemals Benutzereingaben an JPQL- oder SQL-Zeichenketten anhängen.
- Große Beziehungen standardmäßig nicht eager laden — stattdessen Lazy Loading und bei Bedarf explizite Fetch Joins bevorzugen.
- Ressourcen mit try-with-resources oder deterministischen Container-Lifecycle-Hooks schließen.

## Codequalität

- SOLID-Prinzipien einhalten.
- Code-Duplikate vermeiden, ohne unverhältnismäßig komplexe Basisklassen einzuführen.
- Aussagekräftige Namen verwenden, die Fachkonzepte ausdrücken.
- Methoden kurz und fokussiert halten; jede Methode sollte eine klar abgegrenzte Aufgabe erfüllen.
- `Optional<T>` bevorzugen, statt aus Methoden `null` zurückzugeben.
- `var` für lokale Typinferenz verwenden, wenn der Typ aus dem Kontext eindeutig hervorgeht.