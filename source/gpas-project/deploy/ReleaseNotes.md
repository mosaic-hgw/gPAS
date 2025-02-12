${ttp.readme.header}

# gPAS 2024.3.0

## Improvements
*  Fehlermeldung in der Weboberfläche bei der Eingabe von zu großen Originalwerten
*  Allgemeine Verbesserungen in der Weboberfläche
*  Beschleunigter Aufruf des Dashboards
*  Export von Domänen mit über 100.000 Einträgen ohne Anpassung des Wildfly

## Bug Fixes
*  Fehlende Fehlermeldung in der Weboberfläche bei Überschreitung des Grenzwertes für useLastCharAsDelimiterAfterXChars
*  Überschreitung der maximalen Spaltengröße der Datenbank bei Verwendung langer Pseudonyme und Prüfziffern
*  Fehlerhafte Berechnung der erwarteten Pseudonymlänge
*  Korrektur der Bezeichnungen und Hilfetexte bei benutzerdefinierten Alphabeten
*  Import von Multi-PSN importiert nur ein Pseudonym je Originalwert


# gPAS 2024.2.2

## Bug Fixes
*  Pseudonyme werden bei unerwarteter Exception möglicherweise trotzdem erzeugt
*  Pseudonyme werden bei Verwendung eines ErrorStrings als Originalwert trotz Fehler erzeugt
*  Pseudonyme werden bei Überprüfung von Regeln für Elterndomänen trotz Fehler erzeugt


# gPAS 2024.2.1

## Improvements
*  Aktualisierung von Dependencies


# gPAS 2024.2.0

## New Features
*  Multiple Pseudonyme pro Originalwert innerhalb einer Domäne

## Improvements
*  Unterstützung für Pseudonyme bis 32 Byte Länge


# gPAS 2024.1.0

## New Features
*  Import von Daten (Originalwert-Pseudonym Paare) über die Weboberfläche
*  Konfiguration zur Validierung von Originalwerten mittels Eltern-Domäne in der Weboberfläche

## Improvements
*  Möglichkeit zur nachträglichen Änderung der Eltern-Domänen
*  Vorschau der Anzahl möglicher Pseudonyme bei gewählter Konfiguration
*  Dashboard: Beschränkung der maximal angezeigten der Domänen in der Legende
*  Allgemeine Verbesserungen in der Weboberfläche

## Bug Fixes
*  Pseudonymsuche zeigt beim Aufruf via Link ähnliche statt exakte Treffer
*  Dashboard: Optionale Anzeige von gelöschten Domänen im Verlaufsgraphen

## Docker
*  Geänderte Logging-Variable: TTP_GPAS_LOG_TO_FILE zu TTP_GPAS_LOG_TO


# gPAS 2023.2.1

## Bug Fixes
*  Dashboard: Fehler bei leerer Datenbank


# gPAS 2023.2.0

## Improvements
*  Lokalisierter Kalender in Datumsauswahl
*  Verwendung von JakartaEE statt JavaEE
*  Anpassbarer Context Root der Weboberfläche und SOAP-Schnittstelle
*  Vorauswahl des Nein Buttons beim Löschen oder Anonymisieren
*  Wählbarer Start und Endzeitpunkt in Statistik-Diagrammen

## Docker
*  Anpassbarer Context Root der Weboberfläche und SOAP-Schnittstelle


# gPAS 2023.1.2

## Bug Fixes
*  Mögliche NullPointerException bei Benutzung des SOAP-Interfaces ohne Authentifizierung

# gPAS 2023.1.1

## Improvements
*  Besseres Feedback bei Überschreitung der maximalen Länge von PSN, Prefix und Suffix im Web

## Bug Fixes
*  Pseudonyme verschwinden bei >1 Klick auf Domäne
*  Aktualisierung von slf4j-log4j12 in ths-notification-client aufgrund von Vulnerabilities in log4j 1.2.16
*  Validierung der Maximallänge von PSNs im Backend ist fehlerhaft


# gPAS 2023.1.0

## New Features
*  Löschung von Domänen mit Pseudonymen
*  Bearbeitung von Domänen mit Pseudonymen

## Improvements
*  [Stored XSS Vulnerability in der Weboberfläche](https://github.com/mosaic-hgw/gICS/issues/2)
*  Anzeige des lesbaren Benutzernamens bei Login via OIDC (Keycloak)

## Bug Fixes
*  Erhöhte Geschwindigkeit des Frontends bei großer Anzahl von Pseudonymen
*  Fehlerhafte Darstellung von Sonderzeichen im Dateinamen des Exports
*  CSV mit Kombination aus Anführungszeichen und Trennzeichen führt zu fehlerhaftem Export nach Import
*  Änderung des Encodings beim Import führt zu duplizierten Spalten
*  Anzeige des lesbaren Benutzernamens bei Login via OIDC (Keycloak)

## Docker
*  Fail-Fast-Strategie für Docker-CLI-Skripte


# gPAS 1.13.1

## Improvements
*  Beschleunigter Start bei Verwendung großer Domänen ohne Cache

## Bug Fixes
*  Langer Dateiname beim gleichzeitigen Export vieler Domänen

# gPAS 1.13.0

## New Features
*  Keycloak-basierte Absicherung der SOAP-Requests
*  Notificationunterstützung für Pseudonymisierungs-Methoden
*  Öffnen eines Pseudonym-Baums über GET-Parameter

## Improvements
*  Upgrade auf Java 17

## Bug Fixes
*  Exception im Log bei Statistikaufruf von leerem Projekt
*  Fehler im Frontend bei Domain-Anlage via SOAP ohne Label
*  Fehler in der Validierung von Pseudonymen, wenn sowohl includePrefixInCheckDigitCalculation als auch includeSuffixInCheckDigitCalculation gesetzt sind

## Docker
*  Docker Upgrade auf Wildfly 26
*  Erhöhung von MAX_ALLOWED_PACKETSIZE für MySQL8 in Docker auf 10MB
*  Vereinfachung Zusammenführung der separaten Docker-Compose-Pakete der einzelnen Tools
*  OIDC-Compliance: Unterstützung KeyCloak 19 für ALLE Schnittstellen
*  Vereinheitlichung der Konfiguration der Keycloak-basierten Authentifizierung für alle Schnittstellen
*  Unterstützung Client-basierter Rollen in KeyCloak

# gPAS 1.12.1

## Bug Fixes

* Textfehler beim Erstellen/Bearbeiten von Domänen
* Pseudonymbaum zeigt Schlüssel statt Bezeichnung der Domäne an
* Ungültige Datumsangaben im Web werden akzeptiert und automatisch umgerechnet

# gPAS 1.12.0

## Improvements
*  Erhöhte Geschwindigkeit des Frontends bei großer Anzahl von Pseudonymen
*  Allgemeine Verbesserungen im Frontend

## Bug Fixes
*  NPE wenn eine Domäne kein Label enthält
*  Beim Wechsel der Domäne wird die Pagination der PSN-Liste nicht zurückgesetzt

## Docker

* Anpassung und Umstrukturierung der ENV-Files. Details und Änderungsübersicht in beiliegender ReadMe.MD

# gPAS 1.11.0

## New Features
*  Arbeiten im Pseudonym-Baum
*  Dashboard

## Improvements
*  Erstellungs- und Änderungszeitstempel von Domänen
*  Validierung von Originalwerten in Kind-Domäne anhand der Eltern-Domäne
*  Verwendung des Preferred Username bei Keycloak Authentifizierung in Docker
*  Bestimmung der Anzahl der Anonyme einer Domäne
*  Fehlertoleranz bei insertValuePseudonymPairs
*  Behandlung von Fehlern bei  der Batchverarbeitung
*  Laufzeitverbesserung bei Abfrage und Import großer Mengen von Pseudonymen
*  Auslagerung von Docker in separates Modul
*  Einheitliche Sortierung von Domänen Frontend
*  Erhöhte Geschwindigkeit des Frontends bei großer Anzahl von Pseudonymen
*  Reduzierung der Datenpunkten im Pseudonym-Verlauf Diagram
*  Bestimmung ob ein Wert anonymisiert ist.

## Bug Fixes
*  Zeichenlängenbeschränkung der Pseudonyme bei einer Batchverarbeitung

## TTP-FHIR Gateway 2.0.2
* Fehlerhafte Auswertung Keycloak-Request

# gPAS 1.10.3

## Bug Fixes
*  Flexiblere Längenprüfung von Pseudonymen

# gPAS 1.10.2

## Bug Fixes
*  Kompatibilität mit JDK 8
*  Erneute Aufforderung zur Angabe einer Zieldomäne beim Download des Ergebnis der Listenverarbeitung

## Updated FHIR Gateway Support
* Wechsel von GET-Operations zu POST-Operations für folgende Funktionen: pseudonymize, de-pseudonymize, pseudonymize-allow-create
* Rückgabewert und Fehlermeldung auf MultiPart-Parameters umgestellt.

# gPAS 1.10.1

## Improvements
*  Abwärtskompatibler Konstruktor für DomainConfig
*  Allgemeine Verbesserungen im Frontend

## Bug Fixes
*  Fehlerhafte Erzeugung von Pseudonymen mit Trennzeichen

${ttp.readme.footer}