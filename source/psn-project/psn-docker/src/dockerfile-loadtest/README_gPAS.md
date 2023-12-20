![context](https://user-images.githubusercontent.com/12081369/49164566-a5794200-f32f-11e8-8d3a-96244ea00832.png)

Current Docker-Version of gPAS: ${GPAS_VERSION} (${build.monthYear})

---
**Hinweis:** Diese README beschäftigt sich nur mit den Bauen und Ausführen eines eigenen gPAS-Image (harbor.miracum.org/gpas/gpas:${GPAS_VERSION}), welches sich auch im [Miracum-Harbor](https://harbor.miracum.org/harbor/sign-in) befindet.


---
## Inhaltsverzeichnis
1. Übersicht der Verzeichnisstruktur
1. Voraussetzungen
1. Nutzung
    1. gPAS-Image Bauen
    1. gPAS-Image Starten mit ENV-Variablen
    1. gPAS-Image Starten mit ENV-Datei
1. Logging
1. Fehlersuche
1. Additional Information

**Hinweis:** An vielen Stellen werden die Abkürzungen g**P**AS und g**R**AS direkt nebeneinander verwendet. Dies kann zu Verwechslungen/Irritationen führen. Bitte genau hinschauen.


---
## 1. Übersicht der Verzeichnisstruktur

```
____dockerfile-loadtest/
  |____deployments/
  |  |____gpas-VERSION.ear
  |  |____gpas-web-VERSION.war
  |____jboss/
  |  |____configure_wildfly_commons.cli
  |  |____configure_wildfly_gpas.cli
  |____jmeter/
  |  |____gPAS-LoadTest.jmx
  |____Dockerfile
  |____LICENSE.txt
  |____README_gPAS.md
```


---
## 2. Voraussetzungen
* Linux-Server für den Einsatz von Docker<br>
  -für WildFly: min. 4 GB RAM<br>
  -für MySQL (abhängig vom Datenumfang): 2 GB RAM, 10 GB HDD (reicht für 20 Mio. Pseudonyme)<br><br>

* Docker (v1.13.1 oder höher).<br>
  Prüfen mit: `sudo docker -v`<br><br>

* Zum Ausführen von Docker(-Compose) werden die Rechte von Super-User (su) benötigt.<br>
  Entweder mit `sudo su` wechseln, oder vor jedem Befehl `sudo` schreiben.<br><br>


---
## 3. Nutzung

#### 3.1. gPAS-Image Bauen
**Hinweis:** Dieser Schritt kann übersprungen werden, wenn Sie auf das Miracum-Harbor zugreifen können. Dort ist das gPAS-Image bereits vorhanden.

Um das gPAS-Image bauen zu können, müssen Sie zunächst in das Verzeichnis wechseln, wo sich auch die Datei `Dockerfile` befindet.<br>
Da das Image auf ein von uns vorbereitetes WildFly-Image ([mosaicgreifswald/wildfly](https://hub.docker.com/r/mosaicgreifswald/wildfly)) basiert, wird dies zunächst von Docker-Hub heruntergeladen. Im nächsten Schritt werden aus den Verzeichnissen `deployments` und `jboss` die notwendigen Dateien in das gPAS-Image kopiert.

```sh
docker build --tag=harbor.miracum.org/gpas/gpas-test:${GPAS_VERSION} .
```


#### 3.2. gPAS-Image Starten mit ENV-Variablen
Im Anschluss kann das Image mit den Datenbank-Parametern wie folgt gestartet werden:

```sh
docker run --detach \
           --env TTP_GPAS_DB_HOST=host_or_ip \
           --env TTP_GPAS_DB_USER=gpas_user \
           --env TTP_GPAS_DB_PASS=gpas_password \
           --publish 8080:8080 \
           --name gpas-wildfly \
           harbor.miracum.org/gpas/gpas-test:${GPAS_VERSION}
```

Ist der WildFly mit dem gPAS fertig hochgefahren, kann die gPAS-Web-Oberfläche mit dieser Adresse geöffnet werden:
**[http://localhost:8080/gpas-web](http://localhost:8080/gpas-web/html/public/index.xhtml)**


#### 3.3. gPAS-Image Starten mit ENV-Datei
Die ENV-Variablen können, wie in diesem Beispiel zu sehen, in eine ENV-Datei ausgelagert werden.

```sh
docker run --detach \
           --env-file ttp_gpas.env \
           --env-file ttp_commons.env \
           --publish 8080:8080 \
           --name gpas-wildfly \
           harbor.miracum.org/gpas/gpas-test:${GPAS_VERSION}
```


---
## 4. Logging
Wem die Standard-Log-Einstellungen nicht genügen, kann diese mit kleinen Anpassungen ändern.<br>
Zum einen kann mit der ENV-Variable `WF_CONSOLE_LOG_LEVEL` der Log-Level für den Console-Handler geändert werden (Default ist *info*):

```sh
docker run --detach \
           --env WF_CONSOLE_LOG_LEVEL=debug \
           --publish 8080:8080 \
           --name gpas-wildfly \
           harbor.miracum.org/gpas/gpas-test:${GPAS_VERSION}
```
Zum anderen kann mit `TTP_GPAS_LOG_TO_FILE` *on* eine separate Log-Datei für den gPAS angelegt werden. Diese wird im WildFly-Container unter `${docker.wildfly.logs}` abgelegt und kann wie folgt gemountet werden.

```sh
docker run --detach \
           --volume logs:${docker.wildfly.logs} \
           --env TTP_GPAS_LOG_TO_FILE=on \
           --publish 8080:8080 \
           --name gpas-wildfly \
           harbor.miracum.org/gpas/gpas-test:${GPAS_VERSION}
```


---
## 5. Fehlersuche
* `Failed to load URLs from .../.well-known/openid-configuration`<br>
  Die Keycloak-Konfiguration verweist möglicherweise auf einen falschen Realm-Eintrag. Dadurch kann die OpenId-Konfiguration nicht abgerufen werden.<br><br>

* `Unable to find valid certification path to requested target`<br>
  Der Zugriff auf den Keycloak-Server soll per https erfolgen. Dies erfordert ein passendes Zertifikat. Folgen Sie
  den [Tipps zur Generierung](https://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed/) und legen Sie das generierte Zertifikat im Root des Docker-Compose-Verzeichnisses ab.<br><br>

* `Conversation context is already active, most likely it was not cleaned up properly during previous request processing`<br>
  Der verwendete Keycloak-Nutzer wurde bei der letzten Sitzung nicht korrekt am Keycloak-Server abgemeldet. Manuell abmelden und neu versuchen.<br><br>

* Wenn man [Windows Docker Desktop](https://docs.docker.com/desktop/windows/wsl/) mit [WSL 2](https://docs.microsoft.com/de-de/windows/wsl/compare-versions) Backend verwendet, werden die Deployment-Artefakte in einer Endlosschleife neugeladen.
  Eine ausführliche Analyse des Problems findet man im [Repository des WildFly Docker Image auf github](https://github.com/jboss-dockerfiles/wildfly/issues/144).
  Das Problem tritt nicht auf, wenn man die Deployment-Artefakte in den Linux-Container kopiert, so dass die ensprechenden Markerfiles beim Start nicht mehr direkt in den Windows-Mount geschrieben werden.
  Dies passiert automatisch, wenn man in der `wildfly.env` die Variable `WILDFLY_MARKERFILES` auf *false* setzt.
  **Intern**: Siehe auch [Endlose Redeploy Loops von compose wildfly in Windows Docker Desktop mit WSL2 Backend](https://git.icm.med.uni-greifswald.de/ths/docker/-/wikis/problems/Endlose-Redeploy-Loops-von-compose-wildfly-in-Windows-Docker-Desktop-mit-WSL2-Backend) im Docker-Wiki.


---
## 6. Additional Information
The gPAS was developed by the University Medicine Greifswald and published in 2013 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "") (funded by the DFG HO 1937/2-1).

Selected functionalities of gPAS were developed as part of the following research projects:
- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)

## Credits ##
Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke, F.M. Moser

Docker: R. Schuldt

TTP-FHIR Gateway für gPAS: M. Bialke, P. Penndorf, L. Geidel, S. Lang

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright: 2013 - ${build.year} University Medicine Greifswald

Contact: https://www.ths-greifswald.de/kontakt/

## Publications ##
https://dx.doi.org/10.3414/ME14-01-0133

https://dx.doi.org/10.1186/s12967-015-0545-6

# Supported languages #
German, English