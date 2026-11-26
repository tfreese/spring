---
description: Startet einen strukturierten Multi-Agenten-Workflow fuer ein Feature.
# Funktioniert nicht zuverlässig.
#agent: Software-Orchestrator
---

Verwende den Agenten `Software-Orchestrator` als koordinierende Instanz fuer diese Aufgabe.
Waehle ihn in Copilot Chat als aktiven Agenten aus, bevor du diesen Prompt ausfuehrst.
Dieser Prompt allein wechselt den aktiven Agenten nicht.

Bearbeite die folgende Anforderung als koordinierter Softwareentwicklungsprozess:

${input:anforderung:Beschreibe das Feature oder die Aenderung}

Vorgehen:

1. Zuerst Code-Analyse und Auswirkungen bestimmen.
2. Messbare Akzeptanzkriterien definieren.
3. Getrennte Arbeitspakete fuer Backend und UI erstellen, sofern relevant.
4. API-Vertrag vor einer Full-Stack-Implementierung festlegen.
5. Implementierung an die jeweils fachlich passende Rolle delegieren oder konkrete Handoff-Prompts erzeugen.
6. Relevante Builds, Tests und statische Analysen ausfuehren.
7. Zum Abschluss geaenderte Dateien, Kommandos, Testergebnisse, Risiken und offene Punkte nennen.
