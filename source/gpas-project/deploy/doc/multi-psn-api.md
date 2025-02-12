# Änderungen der API in `PSNManager` für Multi-PSNs

### API-Design

Egal, ob die Interface-Methoden für **Single-PSN-Domänen (SPD)** oder **Multi-PSN-Domänen (MPD)** aufgerufen werden,
ihr Verhalten soll der intuitiven Erwartung entsprechen. Die Fehlerbehandlung folgt dem Prinzip: 
**so strikt wie nötig, so tolerant wie möglich**. So können z.B. allgemein geschriebene Skripte in beiden Kontexten 
verwendet werden.

Die **API wird nicht aufgetrennt** in API für MPD und SPD, weder durch Trennung der Interfaces 
(z.B. `SPSNManager` vs. `MPSNManager`), noch durch Methodenpaarungen, die entweder nur
für SPD oder für MPD verwendet werden "dürfen" (z.B. `getOrCreateSinglePseudonymFor` vs. `getOrCreateMultiPseudonymFor`).
Alle Methoden sind prinzipiell für beide Domänenarten verfügbar.  Nur "unsinnige" Zustände und Kombinationen 
werden mit Fehlermeldungen quittiert, z.B. wenn die Return-Werte der Methoden im konkreten Fall 
keine multiplen PSNs abbilden können oder für SPD multiple PSNs angefordert werden. 

Dieses Prinzip wird nur gebrochen bei den Methoden 
`deleteEntry`, `deleteEntries`, `anonymizeEntry` und `anonymizeEntries`, 
da unklar ist, wie `Entry` interpretiert werden soll, als PSN oder als Value (mit potenziell multiplen PSNs).
Hier wird schon bei der Benutzung der API für MPSN-Domänen eine Exception geworfen. Für Testzwecke kann dies umgangen werden,
wenn die Java-System-Property `ttp.gpas.mpsn.allowEntryMethods=true` oder die Umgebungsvariable `TTP_GPAS_MPSN_ALLOW_ENTRY_METHODS=true`
gesetzt wird. Dann wird `Entry` für MPD als Value inklusive multipler PSNs interpretiert.

Für MPD notwendige oder sinnvolle aber fehlende API wird ergänzt.

### Performance

Für SPD wird als Primary-Key (PK) in der DB Tabelle (`psn`) `domain + value` verwendet. Für MPD wird als PK `domain + value + psn` benötigt.
Dadurch sind alle Queries mit `domain + value` in MPD sehr viel (z.T. mehr als zehnfach) langsamer als in SPD.
Damit für SPD keine Performanzeinbußen in Kauf genommen werden müssen, wird für MPD eine Extratabelle (`mpsn`) eingeführt.
Abhängig von `domain.getConfig().isMultiPsnDomain()` wird deshalb "unter der Haube" in der Tabelle `psn` oder `mpsn` gelesen oder geschrieben.

## Get

### Alt (unverändert)

```java
String getPseudonymFor(String value, String domainName)
```

- liefert **genau ein Pseudonym** zum angefragten Value
- Fehlermeldung in **SPD und MPD**, wenn zum angefragten Value **nicht genau ein Pseudonym** existiert

```java
Map<String, String> getPseudonymForList(Set<String> values, String domainName)
Map<String, String> getPseudonymForValuePrefix(String valuePrefix, String domainName)
```

- liefert **genau ein Pseudonym** pro (passendem) Value oder Fehlerinfo `*** VALUE NOT FOUND ***`, wenn **noch keines** existiert
- Fehlerinfo `*** MULTIPLE_PSNS ***` pro (passendem) Value in **SPD und MPD**, wenn **mehr als ein Pseudonym** existiert

### Neu (ergänzt)

```java
List<String> getPseudonymsFor(String value, String domainName)
```
- liefert **alle Pseudonyme** zum angefragten Value
- Fehlermeldung in **SPD und MPD**, wenn zum angefragten Value **kein Pseudonym** existiert
- Fehlermeldung in **SPD**, wenn zum angefragten Value **mehr als ein Pseudonym** existiert

```java
Map<String, List<String>> getPseudonymsForList(Set<String> values, String domainName)
Map<String, List<String>> getPseudonymsForValuePrefix(String valuePrefix, String domainName)
```

- liefert **alle Pseudonyme** pro (passendem) Value
- Fehlerinfo `*** VALUE NOT FOUND ***` pro Value in **SPD und MPD**, wenn zum angefragten Value **kein Pseudonym** existiert
- Fehlerinfo `*** MULTIPLE_PSNS ***` pro Value in **SPD**, wenn zum angefragten Value **mehr als ein Pseudonym** existiert

## GetOrCreate

### Alt (unverändert)

```java
String getOrCreatePseudonymFor(String value, String domainName)
```

- liefert **genau ein Pseudonym** und erzeugt eines, wenn **noch keines** existiert
- Fehlermeldung in **SPD und MPD**, wenn **mehr als ein Pseudonym** zum angefragten Value existiert

```java
Map<String, String> getOrCreatePseudonymForList(Set<String> values, String domainName)
```

- liefert **genau ein Pseudonym** pro Value, erzeugt jeweils eines, wenn **noch keines** existiert
- Fehlerinfo `*** MULTIPLE_PSNS ***` pro Value in **SPD und MPD**, wenn **mehr als ein Pseudonym** zum angefragten Value existiert

### Neu (ergänzt)

```java
List<String> getOrCreatePseudonymsFor(String value, String domainName, int minNumber)
```

- liefert **alle existierenden**, aber **mindestens `minNumber` Pseudonyme**, erzeugt die fehlenden
- für SPD wird immer eine Liste mit genau einem Pseudonym zurückgegeben
- Fehlermeldung in **SPD**, wenn **mehr als ein Pseudonym** zum angefragten Value **angefordert** wird

```java
Map<String, List<String>> getOrCreatePseudonymsForList(Set<String> values, String domainName, int minNumber)
```

- liefert **alle existierenden**, aber **mindestens `minNumber` Pseudonyme** pro Value, erzeugt die fehlenden
- Fehlermeldung in **SPD**, wenn **mehr als ein Pseudonym** zum angefragten Value **angefordert** wird
- Fehlerinfo `*** MULTIPLE_PSNS ***` pro Value in **SPD**, wenn **mehr als ein Pseudonym** zum angefragten Value existiert

## Create

### Neu (ergänzt)

```java
String createPseudonymFor(String value, String domainName);
String createPseudonymsFor(String value, String domainName, int number);
```

- erzeugt ein oder mehrere **neue** Pseudonyme
- Fehlermeldung in **SPD**, wenn **mehr als ein Pseudonym** zum angefragten Value **angefordert** wird
- Fehlermeldung in **SPD**, wenn schon **ein Pseudonym** zum angefragten Value existiert

```java
Map<String, String> createPseudonymForList(Set<String> values, String domainName)
```

- erzeugt ein **neues** Pseudonym pro Value
- Fehlerinfo `*** MULTIPLE_PSNS ***` pro Value in **SPD**, wenn zum angefragten Value **schon ein Pseudonym** existiert

```java
Map<String, List<String>> createPseudonymsForList(Set<String> values, String domainName, int number)
```

- erzeugt ein oder mehrere **neue** Pseudonyme pro Value
- Fehlermeldung in **SPD**, wenn **mehr als ein Pseudonym** pro Value **angefordert** wird
- Fehlerinfo `*** MULTIPLE_PSNS ***` pro Value in **SPD**, wenn zum angefragten Value **schon ein Pseudonym** existiert

## Anonymise & Delete

### Alt (unverändert)

```java
void anonymiseEntry(String value, String domainName)
void deleteEntry(String value, String domainName)
```

- anonymisiert bzw. löscht **genau ein Pseudonym** zum angefragten Value
- Fehlermeldung in **MPD**
- Fehlermeldung in **SPD**, wenn zum angefragten Value **nicht genau ein Pseudonym** existiert

```java
Map<String, AnonymisationResult> anonymiseEntries(Set<String> values, String domainName)
Map<String, DeletionResult> deleteEntries(Set<String> values, String domainName)
```

- anonymisiert bzw. löscht **genau ein Pseudonym** zu den angefragten Values
- Fehlermeldung in **MPD**
- liefert `SUCCESS`, wenn **genau ein Pseudonym** gelöscht oder anonymisiert wurde
- liefert `NOT_FOUND`, wenn **kein ein Pseudonym** zum Value gefunden wurde
- liefert `ERROR`, wenn **mehrere Pseudonyme** zum Value gefunden wurde
- liefert `ALREADY_ANONYMISED` beim Anonymisiern, wenn der Value schon anonymisiert ist

### Neu (ergänzt)

```java
void anonymiseAllEntriesForValue(String value, String domainName)
void deleteAllEntriesForValue(String value, String domainName)
```

- anonymisiert bzw. löscht **alle Pseudonyme** zum angefragten Value
- Fehlermeldung in **SPD und MPD**, wenn zum angefragten Value **kein Pseudonym** existiert

```java
Map<String, AnonymisationResult> anonymiseAllEntriesForValues(Set<String> values, String domainName)
Map<String, DeletionResult> deleteAllEntriesForValues(Set<String> values, String domainName)
```

- anonymisiert bzw. löscht **alle Pseudonyme** zu den angefragten Values
- liefert `SUCCESS`, wenn **mindestens ein Pseudonym** gelöscht oder anonymisiert wurde
- liefert `NOT_FOUND`, wenn **kein Pseudonym** zum Value gefunden wurde
- liefert `ALREADY_ANONYMISED` beim Anonymisiern, wenn der Value schon anonymisiert ist

```java
void anonymisePseudonym(String psn, String domainName)
void deletePseudonym(String psn, String domainName)
```

- anonymisiert bzw. löscht das angefragte Pseudonym
- Fehlermeldung in **SPD und MPD**, wenn das angefragte Pseudonym nicht existiert

```java
Map<String, AnonymisationResult> anonymisePseudonyms(Set<String> psns, String domainName)
Map<String, DeletionResult> deletePseudonyms(Set<String> psns, String domainName)
```

- anonymisiert bzw. löscht die angefragten Pseudonyme
- liefert `SUCCESS`, wenn das Pseudonym gelöscht oder anonymisiert wurde
- liefert `NOT_FOUND`, wenn das Pseudonym nicht gefunden wurde
- liefert `ALREADY_ANONYMISED` beim Anonymisiern, wenn der Value zum Pseudonym schon anonymisiert ist
