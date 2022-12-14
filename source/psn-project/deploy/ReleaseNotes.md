![context](https://user-images.githubusercontent.com/12081369/49164566-a5794200-f32f-11e8-8d3a-96244ea00832.png)

Current Docker-Version of gPAS: 1.12.0 (March 2022)

Current Docker-Version of TTP-FHIR-Gateway: 2.1.1 (March 2022), Details from [ReleaseNotes](https://www.ths-greifswald.de/fhirgw/releasenotes/2-1-1)

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
Wechsel von GET-Operations zu POST-Operations für folgende Funktionen

* https://simplifier.net/guide/ttp-fhir-gateway-ig/pseudonymize
* https://simplifier.net/guide/ttp-fhir-gateway-ig/de-pseudonymize
* https://simplifier.net/guide/ttp-fhir-gateway-ig/pseudonymize-allow-create

Rückgabewert und Fehlermeldung auf MultiPart-Parameters umgestellt.

# gPAS 1.10.1

## Improvements
*  Abwärtskompatibler Konstruktor für DomainConfig
*  Allgemeine Verbesserungen im Frontend

## Bug Fixes
*  Fehlerhafte Erzeugung von Pseudonymen mit Trennzeichen

# Additional Information
The gPAS was developed by the University Medicine Greifswald and published in 2013 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "") (funded by the DFG HO 1937/2-1).

Selected functionalities of gPAS were developed as part of the following research projects:
- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)

## Credits ##
Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke, F.M. Moser

Docker: R. Schuldt

TTP-FHIR Gateway für gICS: M. Bialke, P. Penndorf, L. Geidel, S. Lang

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright: 2013 - 2022 University Medicine Greifswald

Contact: https://www.ths-greifswald.de/kontakt/

## Publications ##
https://dx.doi.org/10.3414/ME14-01-0133

https://dx.doi.org/10.1186/s12967-015-0545-6

# Supported languages #
German, English
