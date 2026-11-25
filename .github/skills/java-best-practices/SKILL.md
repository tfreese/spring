---
name: java-best-practices
description: 'Sicherstellen, dass Java-/Spring-Boot-Code den bewährten Vorgehensweisen der jeweiligen Lösung und des Projekts entspricht.'
---

# Best Practices für Java und Spring Boot

Stelle sicher, dass der Java-Code in ${selection} die für diese Lösung und dieses Projekt geltenden Best Practices erfüllt. Dazu gehören:

## Dokumentation und Struktur

- Für alle öffentlichen Klassen, Interfaces, Methoden und Felder umfassende Javadoc-Kommentare erstellen.
- In Javadoc die Tags `@param`, `@return` und `@throws` verwenden.
- Die vorgegebene Paketstruktur einhalten: `{model|data|service|web}.{feature}`.
- Jede Klassendatei auf eine einzige Zuständigkeit ausrichten.

## Entwurfsmuster und Architektur

- Für alle von Spring verwalteten Abhängigkeiten Konstruktorinjektion verwenden (Feldinjektion mit `@Autowired` vermeiden).
- Injizierte Felder vorzugsweise als `final` deklarieren, damit sie nach der Konstruktion unveränderlich sind.
- Das Interface-Segregation-Prinzip mit eindeutigen Namenskonventionen anwenden; Interfaces nach ihren Fähigkeiten benennen, zum Beispiel `TransactionRepository` oder `BudgetService`.
- Für die komplexe Objekterzeugung das Factory-Muster verwenden.
- Wo sinnvoll, `record`-Typen (ab Java 16) für unveränderliche Datenträger und DTOs verwenden.

## Abhängigkeitsinjektion und Services

- Konstruktorbasierte Abhängigkeitsinjektion verwenden — Spring erkennt einzelne Konstruktoren automatisch, `@Autowired` ist dafür nicht erforderlich.
- Services mit passenden Spring-Scopes registrieren (`@Singleton` als Standard, `@RequestScope`, `@SessionScope`).
- Spring-Stereotypen wie `@Service`, `@Repository` und `@Component` korrekt verwenden.
- Service-Interfaces zur Unterstützung der Testbarkeit implementieren.

## Spring Data JPA

- Spring-Data-JPA-Repositories direkt verwenden — keine zusätzlichen Repository-Abstraktionsschichten einführen.
- Abfragelogik in Repositories oder JPQL-`@Query`-Annotationen halten; Geschäftslogik in Services belassen.
- Für Abfragen einzelner Entitäten `Optional<T>` als Rückgabetyp verwenden.
- `@Transactional` für Schreiboperationen auf der Service-Schicht einsetzen.
- N+1-Abfragen vermeiden — bei Bedarf `JOIN FETCH` oder `@EntityGraph` zum Laden verwenden.

## Asynchrone Muster

- Für E/A-gebundene und lang laufende Vorgänge `CompletableFuture<T>` oder Spring-`@Async` verwenden.
- Eine benannte `ThreadPoolTaskExecutor`-Bean konfigurieren; sich nicht auf `SimpleAsyncTaskExecutor` verlassen.
- Von `@Async`-Methoden `CompletableFuture<T>` zurückgeben, damit Aufrufer Komposition und Fehlerbehandlung durchführen können.
- Für E/A-gebundene Verarbeitung mit hohem Durchsatz virtuelle Threads aus Java 21 verwenden (`Executors.newVirtualThreadPerTaskExecutor()`).

## Teststandards

- JUnit 5 verwenden (`@Test`, `@BeforeEach`, `@AfterEach`, `@ExtendWith`).
- Mockito zum Mocken von Abhängigkeiten verwenden (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`).
- AssertJ für ausdrucksstarke Assertions verwenden (`assertThat(result).isEqualTo(expected)`).
- Dem AAA-Muster folgen (Arrange, Act, Assert).
- Erfolgs- und Fehlerfälle testen.
- Für Integrationstests `@SpringBootTest` und für Unit-Tests direktes JUnit mit Mockito verwenden.
- Für öffentliche Methoden Tests mit Null- und Grenzwerten einschließen.

## Konfiguration und Einstellungen

- `@ConfigurationProperties` mit einem `record` oder POJO für typsichere Konfiguration verwenden.
- Einstellungen aus `application.properties` oder `application.yml` binden.
- Konfiguration mit Bean-Validation-Annotationen wie `@NotNull`, `@NotBlank` und `@Min` an `@ConfigurationProperties`-Klassen validieren.
- `@Profile` verwenden, um umgebungsspezifische Beans zu trennen.

## Geldbeträge und Finanzwerte

- Für Geldbeträge immer `BigDecimal` verwenden — niemals `double` oder `float`.
- Skalierung und Rundungsmodus explizit festlegen: `amount.setScale(2, RoundingMode.HALF_UP)`.
- `BigDecimal.ZERO`, `BigDecimal.ONE` usw. statt `new BigDecimal(0)` verwenden.
- Geldbeträge im Datenbankschema als `DECIMAL(19,4)` speichern.

## Fehlerbehandlung und Protokollierung

- SLF4J mit Logback (dem Spring-Boot-Standard) verwenden — über `LoggerFactory.getLogger(MyClass.class)` oder Lomboks `@Slf4j` einbinden.
- Parametrisierte Logmeldungen verwenden: `log.info("Processing order {}", id)` — niemals Zeichenketten verketten.
- Spezifische, aussagekräftige Ausnahmen auslösen; bei Bedarf eigene Anwendungsausnahmen definieren, die von `RuntimeException` abgeleitet sind.
- Ausnahmen auf der Web-Schicht mit `@ControllerAdvice` und `@ExceptionHandler` behandeln.
- Aussagekräftigen Kontext in Ausnahmemeldungen aufnehmen.

## Leistung und Sicherheit

- Funktionen aus Java 21 verwenden, sofern passend: Records, sealed classes, Pattern Matching und virtuelle Threads.
- Alle externen Eingaben an Systemgrenzen validieren — bei Controller-Methodenparametern Bean Validation mit `@Valid` verwenden.
- Parametrisierte JPA-Abfragen oder Spring-Data-Methoden verwenden — niemals Benutzereingaben an JPQL- oder SQL-Zeichenketten anhängen.
- Große Beziehungen standardmäßig nicht eager laden — stattdessen Lazy Loading und bei Bedarf explizite Fetch Joins bevorzugen.
- Ressourcen mit try-with-resources schließen.

## Codequalität

- SOLID-Prinzipien einhalten.
- Code-Duplikate durch Basisklassen, Utility-Klassen und Default-Methoden in Interfaces vermeiden.
- Aussagekräftige Namen verwenden, die Fachkonzepte ausdrücken.
- Methoden kurz und fokussiert halten — jede Methode sollte eine Aufgabe erfüllen.
- `Optional<T>` bevorzugen, statt aus Methoden `null` zurückzugeben.
- `var` (ab Java 10) für lokale Typinferenz verwenden, wenn der Typ aus dem Kontext eindeutig hervorgeht.
