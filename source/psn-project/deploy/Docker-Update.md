![context](https://www.ths-greifswald.de/wp-content/uploads/2019/01/Design-Logo-THS-deutsch-542.png)

Stand: April 2022

# Aktualisierung der THS-Tools per Docker

## Hintergrund

**Am folgenden Beispiel wird die Aktualisierung der Docker-Container vom gPAS gezeigt. Grundsätzlich findet die Aktualisierung aller THS-Tools (E-PIX, gPAS, gICS, Dispatcher) auf dieselbe Weise statt.**

Im Beispiel wird die bestehende und laufende Instanz vom gPAS als `<gpas-old>` bezeichnet. Die existierende Version (`<old-version>`) soll gesichert und ein Update auf eine neue Version vom gPAS (`<gpas-new>`, `<new-version>`) durchgeführt werden, ohne die bereits vorhandenen Daten in der MySQL-Datenbank zu verändern.

Ob die Instanzen vom gPAS laufen, kann mit folgenden Befehl geprüft werden:
```
docker ps -a
```

## Handlungsanweisung

### Neue  Tool-Version von der THS-Webseite herunterladen

Die aktuelle Version von [ths-greifswald.de/gpas](https://www.ths-greifswald.de/gpas) herunterladen und entpacken, sowie auf das Host-System kopieren und sicherstellen, dass entsprechende Berechtigungen zum Ausführen der Dateien gesetzt sind.

```
CHMOD -R 755 /PFAD
```

### Sichern der aktuellen Docker-Konfiguration

Um auf dem Host-System den derzeitigen Stand der gPAS-Konfiguration (Wildfly-Skripte, etc.) zu sichern, den entsprechenden Ordner per TAR-Archiv sichern:

```
tar czf backup-gpas-2022-03-31.tgz <gpas-old>/
```

### Sichern der existierenden Datenbank

Um zusätzlich die Sicherung der existierenden Datenbank durchzuführen, wird ein MySQL-Dump über die Docker-Konsole angestoßen und die resultierende Export-Datei im Dateisystem vom Host abgelegt.

```
sudo docker exec gpas-<old-version>-mysql /usr/bin/mysqldump -u gpas_user -p gpas > backup-gpas-<old-version>-2022-03-31.sql
```

Der Name der bestehenden MySQL-Instanz muss entsprechend angepasst werden.

### Aktualisieren der Datenbank

Für alle Versionen sind die Datenbank-Aktualisierungsskripte jeweils im Docker-Verzeichnis unter *`<gpas-new>/update_scripts`* zu finden. Die Update-Skripte müssen in den Docker-Container kopiert werden, wobei nur die Skripte erforderlich sind, welche die Version zwischen `<gpas-old>` zu `<gpas-new>` betreffen.

```
docker cp <gpas-new>/update_scripts/ gpas-<old-version>-mysql:/update-files/

```

Je nachdem von welcher Version aus gPAS aktualisiert werden soll, müssen die relevanten SQL-Skripte *chronologisch durchlaufen* werden.

**Beispiel:** Für ein Update von Version 1.10.0 auf 1.12.0 sind demzufolge die Skripte *update_database_gpas_1.10.x-1.11.0.sql* und *update_database_gpas_1.11.x-2.12.0.sql* auszuführen.

Dazu per MySQL Client mit der bestehenden Datenbank verbinden und die Update-Skripte nacheinander durchlaufen. Dies kann per *docker* realisiert werden (Nutzernamen und Passwörter ggf. anpassen).

**Beispiel:**

```
docker exec -it gpas-1.10.0-mysql /usr/bin/mysql -u gpas_user -p -e "USE gpas; $(cat gpas-new/standard/update_database_gpas_1.10.x-1.11.0.sql)"
docker exec -it gpas-1.10.0-mysql /usr/bin/mysql -u gpas_user -p -e "USE gpas; $(cat gpas-new/standard/update_database_gpas_1.11.x-2.12.0.sql)"
```

### Aktualisierung der Deployments und Wildfly-Konfiguration

Den Datenbank-Container nun herunterfahren

```
docker gpas-<old-version>-mysql down
```

Die Deployments im <gpas-old> Verzeichnis auf dem Host-System löschen und die neuen Deployments hinein kopieren

```
rm -f <gpas-old>/deployments/* 
cp -R <gpas-new>/deployments/ <gpas-old>/deployments/
```

Aktualisierung der Bezeichnung des MySQL Containers

```
sudo docker rename gpas-<old-version>-mysql gpas-<new-version>-mysql
```

JBOSS Konfiguration aktualisieren

```
cp -R <gpas-new>/jboss/ <gpas-old>/jboss/
```

Docker-Compose-Konfiguration aktualisieren

```
cp -R <gpas-new>/docker-compose.yml <gpas-old>/docker-compose.yml
```

Anpassen des Eigentümer-Benutzers

```
chown 999 <gpas-new>/sqls
chown 1000 <gpas-new>/deployments
chown 1000 <gpas-new>/logs
chown 1000 <gpas-new>/jboss
```

### Starten des aktualisierten Containers

Den aktualisierten Container mittels folgendem Befehl starten (-d um Container im Hintergrund zu starten):

```
docker-compose up -d
```

Den Erfolg der Aktualisierung prüfen durch Aufruf des Web-Frontends unter [http://IPADDRESS:8080/gpas-web](http://ipaddress:8080/gpas-web).

## Im Fehlerfall: Wiederherstellung der Datenbank

Im Fehlerfall, kann die bisherige Datenbank wiederhergestellt werden (sofern die Anleitung befolgt wurde). Nutzernamen und Passwort ggf. anpassen.

```
docker exec -it gpas-<new-version>-mysql /usr/bin/mysql -u gpas_user -p -e "USE gpas; $(cat backup-gpas-2022-03-31.sql)"
```

# Additional Information #

The gPAS was developed by the University Medicine Greifswald  and published in 2013 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1).

Selected functionalities of gPAS were developed as part of the following research projects:
- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)

## Credits ##
Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke, F.M. Moser

Docker: R. Schuldt

TTP-FHIR Gateway für gPAS: M. Bialke, P. Penndorf, L. Geidel, S. Lang

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright: 2013 - 2022 University Medicine Greifswald

Contact: https://www.ths-greifswald.de/kontakt/

## Publications ##
https://dx.doi.org/10.3414/ME14-01-0133

https://dx.doi.org/10.1186/s12967-015-0545-6

# Supported languages #
German, English