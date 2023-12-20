![context](https://user-images.githubusercontent.com/12081369/49164566-a5794200-f32f-11e8-8d3a-96244ea00832.png)

Current Docker-Version of gPAS: ${GPAS_VERSION} (${build.monthYear})

---
**Hinweis:** Diese README beschäftigt sich nur mit den Ausführen des gPAS`s ohne vorher ein eigenes gPAS-Image zu bauen. Zum Einsatz kommt dafür nur Docker-Compose mit gemounteten Volumes.


---
## Inhaltsverzeichnis
1. Übersicht der Verzeichnisstruktur
1. Nutzung
    1. Berechtigungen setzen
    1. Starten mit Docker-Compose
    1. Verwenden von .env-Dateien
1. Logging
1. Authentifizierung gPAS-Web
    1. gras
    1. keycloak
    1. keycloak-json (alternative)
1. Externe gPAS-Datenbank einbinden
1. Fehlersuche
1. Alle verfügbaren Enviroment-Variablen
1. Additional Information

**Hinweis:** An vielen Stellen werden die Abkürzungen g**P**AS und g**R**AS direkt nebeneinander verwendet. Dies kann zu Verwechslungen/Irritationen führen. Bitte genau hinschauen.

---
## 1. Übersicht der Verzeichnisstruktur

```
____compose/
  |____deployments/
  |  |____gpas-VERSION.ear
  |  |____gpas-web-VERSION.war
  |____jboss/
  |  |____configure_wildfly_commons.cli
  |  |____configure_wildfly_gpas.cli
  |  |____configure_wildfly_gras.cli
  |  |____gpas_gras_jboss-web.xml
  |  |____gpas_gras_web.xml
  |  |____gpas_oidc_web.xml
  |  |____oidc.json
  |____jmeter/
  |  |____gPAS-LoadTest.jmx
  |____logs/
  |____sqls/
  |  |____create_database_gpas.sql
  |  |____create_database_gras.sql
  |  |____init_database_gras_for_gpas.sql
  |  |____...
  |____ABOUT_gPAS.md (oder .pdf)
  |____docker-compose.yml
  |____LICENSE.txt
  |____mysql.env
  |____README_gPAS.md (oder .pdf)
  |____ReleaseNotes_gPAS.md (oder .pdf)
  |____ttp_commons.env
  |____ttp_gpas.env
  |____ttp_gras.env
  |____ttp_loadtest.env
```

---
## 2. Nutzung
Sowohl in der Nutzung mit Docker-Compose, als auch in der beschriebenen Nutzung mit Docker-Run wird ein WildFly-Image aus dem Docker-Hub von [mosaicgreifswald/wildfly](https://hub.docker.com/r/mosaicgreifswald/wildfly) heruntergeladen, welches wir für die gPAS-Nutzung vorbereitet haben. Im Gegensatz zu anderen WildFly-Images kann dieses mittels Einbindung von verschiedenen Volumes direkt genutzt werden und muss nicht erst gebaut werden (bauen ist natürlich trotzdem möglich).

Egal wie der gPAS gestartet wird, im Anschluss wird die gPAS-Web-Oberfläche mit dieser Adresse geöffnet: **[http://localhost:8080/gpas-web](http://localhost:8080/gpas-web/html/public/index.xhtml)**

---
#### 2.1. Berechtigungen setzen
Bevor der gPAS gestartet werden kann, müssen Berechtigungen auf den Ordnern geändert werden. Diese sind notwendig, damit der Container nicht nur die Ordner lesen, sondern auch beschreiben kann.

```sh
# für den MySQL-Container
chown -R 999:999 sqls

# für den WildFly-Container
chown -R 1000:1000 deployments jboss logs
```

---
#### 2.2. Starten mit Docker-Compose
Die einfachste und schnellste Variante ist auf einem geeignetem System docker-compose zu starten.<br>
In der Grundeinstellung wird je ein Container für MySQL, WildFly und für den jMeter erstellt und gestartet.<br>
Dafür muss zunächst in das Verzeichnis gewechselt werden, in dem sich die Datei `docker-compose.yml` befindet.<br>
Der Parameter `--abort-on-container-exit` bewirkt, das alle Container beendet werden, sobald jMeter mit seinen Test fertig ist.

```sh
# Anlegen und Starten
docker-compose up --abort-on-container-exit

# Stoppen und Löschen
docker-compose down
```
Der erste Start dauert bis zu 5 Minuten, da die Datenbank und der Wildfly konfiguriert werden.

---
#### 2.3. Verwenden von .env-Dateien
.env-Dateien ermöglichen das Auslagern von Enviroment-Variablen aus der .yml-Datei und werden wie folgt verwendet. Zusätzlich können Enviroment-Variablen auch in die .yml-Datei geschrieben werden. Die .env-Dateien enthalten schon alle relevanten Variablen, die zum Teil nur einkommentiert, bzw. angepasst werden müssen.

```yml
services:
  mysql:
    env_file:
      - mysql.env
    ...
  wildfly:
    env_file:
      - ttp_commons.env
      - ttp_gpas.env
      - ttp_gras.env
    ...
  jmeter:
    env_file:
      - ttp_loadtest.env
```

---
## 3. Logging
Wem die Standard-Log-Einstellungen nicht genügen, kann diese ändern.<br>
Zum einen kann mit der ENV-Variable `WF_CONSOLE_LOG_LEVEL` der Log-Level für den Console-Handler geändert werden (Default ist *info*), zum anderen kann mit `TTP_GPAS_LOG_TO_FILE` *true* eine separate Log-Datei für den gPAS angelegt werden. Die Log-Datei wird im WildFly-Container unter `${docker.wildfly.logs}` abgelegt und kann wie folgt gemountet werden.

```ini
WF_CONSOLE_LOG_LEVEL=debug
TTP_GPAS_LOG_TO_FILE=true
TTP_GPAS_LOG_LEVEL=info
```

docker-compose.yml:

```yml
services:
  wildfly:
    volumes:
      - ./logs:${docker.wildfly.logs}
    ...
  jmeter:
    volumes:
      - ./logs:/entrypoint-jmeter-logs
```

---
## 4. Authentifizierung gPAS-Web
In der Standard-Ausgabe vom gPAS ist keine Authentifizierung notwendig. Möchte man den gPAS jedoch nur für bestimmte Nutzergruppen zugänglich machen, oder sogar das Anlegen von neuen Domänen beschränken, können zwei Authentifizierungsverfahren angewendet werden. `gRAS` und `KeyCloak`, wobei es für KeyCloak zwei verschiedene Varianten gibt.

---
#### 4.1. gRAS-Authentifizierung
Um diese Variante zu nutzen, muss die ENV-Variable `TTP_GPAS_WEB_AUTH_MODE` den Wert *gras* bekommen:

```ini
TTP_GPAS_WEB_AUTH_MODE=gras
```

**Hinweis:** Befindet sich die gRAS-Datenbank nicht im lokalen Docker-Compose-Netzwerk, müssen die Variablen für die DB-Verbindung ebenfalls angepasst werden.


---
#### 4.2. KeyCloak-Authentifizierung
Statt gRAS kann auch eine KeyCloak-Authentifizierung eingesetzt werden.<br>
Neben der ENV-Variable `TTP_GPAS_WEB_AUTH_MODE` mit den Wert *keycloak*, müssen weitere Variablen für die KeyCloak-Credentials hinzugefügt werden.

```ini
TTP_GPAS_WEB_AUTH_MODE=keycloak
TTP_KEYCLOAK_SERVER_URL=<PROTOCOL://HOST_OR_IP:PORT/auth/>
TTP_KEYCLOAK_SSL_REQUIRED=<none|external|all>
TTP_KEYCLOAK_REALM=<REALM>
# TTP_KEYCLOAK_CLIENT_ID is the new alias from KEYCLOAK_RESOURCE=<RESOURCE>
TTP_KEYCLOAK_CLIENT_ID=<CLIENT_ID>
TTP_KEYCLOAK_CLIENT_SECRET=<CLIENT_SECRET>
TTP_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS=<true|false>
TTP_KEYCLOAK_CONFIDENTIAL_PORT=<CONFIDENTIAL_PORT>
```
**Hinweis:** Konfiguration des Keycloak-Server unter https://www.ths-greifswald.de/ttp-tools/keycloak

**Hinweise dazu aus der offiziellen [Keycloak Dokumentation](https://www.keycloak.org/docs/latest/securing_apps/index.html#_java_adapter_config):**

*use-resource-role-mappings*: If set to true, the adapter will look inside the token for application level role mappings for the user. If false, it will look at the realm level for user role mappings.
This is OPTIONAL. The default value is false.

*confidential-port*: The confidential port used by the Keycloak server for secure connections over SSL/TLS. This is OPTIONAL. The default value is 8443.

*ssl-required*: Ensures that all communication to and from the Keycloak server is over HTTPS. In production this should be set to all. This is OPTIONAL. The default value is external meaning that
HTTPS is required by default for external requests. Valid values are 'all', 'external' and 'none'.

---
#### 4.3. KeyCloak-Authentifizierung (die JSON-Alternative)
Für diese Variante muss eine JSON-Datei `oidc.json` im jboss-Verzeichnis angepasst werden, dessen Werte aus der lokalen KeyCloak-Instanz entnommen werden können.

```json
{
  "client-id": "<RESOURCE>",
  "provider-url": "<PROTOCOL>://<HOST_OR_IP>:<PORT>/auth/realms/<REALM>",
  "ssl-required": "<none|external|all>",
  "verify-token-audience": true,
  "credentials": {
    "secret": "<CLIENT_SECRET>"
  },
  "use-resource-role-mappings": false,
  "confidential-port": 8443
}
```
**Hinweis:** Wenn `use-resource-role-mappings` gleich *true* ist, müssen die Rollen am Client definiert sein.

Zusätzlich braucht nur der Wert der ENV-Variable `TTP_GPAS_WEB_AUTH_MODE` auf *keycloak-json* gesetzt werden und der WildFly bezieht die KeyCloak-Credentials aus der JSON-Datei.

```ini
TTP_GPAS_WEB_AUTH_MODE: keycloak-json
```

#### 4.4. KeyCloak-Authentifizierung TTP-FHIR Gateway
Ab TTP-FHIR Gateway Version 2.0.0 ist eine Absicherung der TTP-FHIR-Gateway-Schnittstelle je Endpunkt, wie zum Beispiel gPAS, vorgesehen und nach Bedarf konfigurierbar.

Alle erforderlichen Informationen werden in der separat bereitgestellten Dokumentation erläutert.

https://www.ths-greifswald.de/ttpfhirgateway/keycloak (pdf)

Diese Dokumentation umfasst:

- Installation und Einrichtung von Keycloak
- Testung der Keycloak-Konfiguration
- Einrichtung des TTP-FHIR-Gateways für Keycloak-Authentifizierung
- Test und Benutzung des TTP-FHIR-Gateways mit Keycloak-Authentifizierung anhand von Beispielen

---
## 5. Externe gPAS-Datenbank einrichten
Es ist möglich den gPAS mit einer existierenden Datenbank zu verbinden. Wenn sichergestellt ist, dass die DB vom Docker-Host erreichbar ist, müssen folgende ENV-Variablen angepasst werden:

```ini
TTP_GPAS_DB_HOST=<HOST_OR_IP>
TTP_GPAS_DB_PORT=<PORT>
TTP_GPAS_DB_NAME=<DB_NAME>
TTP_GPAS_DB_USER=<DB_USER>
TTP_GPAS_DB_PASS=<DB_PASSWORD>
```

Zusätzlich muss die .yml-Datei angepasst werden. Da die externe DB in meisten Fällen bereits läuft, muss der WildFly-Container nicht warten, bis der MySQL-Port *3306* verfügbar ist. Aus diesem Grund können die Werte für `depends_on`, `entrypoint` und `command` entfernt oder auskommentiert werden:

```yml
services:
  wildfly:
#    depends_on:
#      - mysql
#    entrypoint: /bin/bash
#    command: -c "./wait-for-it.sh mysql:3306 -t 120 && ./run.sh"
```

Beim Start der docker-compose, darauf achten, das jetzt nur noch der Service *wildfly* gestartet wird. Alternativ kann der Service für *mysql* auch der Compose-Datei entfernt werden.

```sh
# Nur den WildFly-Service starten
docker-compose up wildfly
```

---
## 6. Fehlersuche
* Validierung Zugriff auf KeyCloak<br>
  `curl <PROTOCOL>://<HOST_OR_IP>:<PORT>/auth/realms/<REALM>/.well-known/openid-configuration`

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

---
## 7. Alle verfügbaren Enviroment-Variablen
In den env-Dateien stehen weitere Details zu den einzelnen Variablen.

#### ttp_gpas.env **<-- ehemals gpas.env**
| Kategorie | Variable                                                | verfügbare Werte oder Schema           | default                                             |
|-----------|---------------------------------------------------------|----------------------------------------|-----------------------------------------------------|
| Logging   | TTP_GPAS_LOG_TO_FILE **<-- Alias von GPAS_FILE_LOG**    | true, false                            | false                                               |
| Logging   | TTP_GPAS_LOG_LEVEL **<-- Alias von GPAS_LOG_LEVEL**     | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO                                                |
| Database  | TTP_GPAS_DB_HOST **<-- Alias von GPAS_DB_HOST**         | \<STRING\>                             | mysql                                               |
| Database  | TTP_GPAS_DB_PORT **<-- Alias von GPAS_DB_PORT**         | 0-65535                                | 3306                                                |
| Database  | TTP_GPAS_DB_NAME **<-- Alias von GPAS_DB_NAME**         | \<STRING\>                             | gpas                                                |
| Database  | TTP_GPAS_DB_USER **<-- Alias von GPAS_DB_USER**         | \<STRING\>                             | gpas_user                                           |
| Database  | TTP_GPAS_DB_PASS **<-- Alias von GPAS_DB_PASS**         | \<STRING\>                             | gpas_password                                       |
| Security  | TTP_GPAS_WEB_AUTH_MODE **<-- Alias von GPAS_AUTH_MODE** | gras, keycloak, keycloak-json          | -                                                   |
| Security  | TTP_GPAS_SOAP_KEYCLOAK_ENABLE **<-- neu**               | true, false                            | -                                                   |
| Security  | TTP_GPAS_SOAP_ROLE_USER_NAME **<-- neu**                | \<STRING\>                             | role.gpas.user                                      |
| Security  | TTP_GPAS_SOAP_ROLE_USER_SERVICES **<-- neu**            | \<STRING\>                             | /gpas/gpasService,/gpas/gpasServiceWithNotification |
| Security  | TTP_GPAS_SOAP_ROLE_ADMIN_NAME **<-- neu**               | \<STRING\>                             | role.gpas.admin                                     |
| Security  | TTP_GPAS_SOAP_ROLE_ADMIN_SERVICES **<-- neu**           | \<STRING\>                             | /gpas/DomainService                                 |

#### ttp_noti.env **<-- ehemals noti.env**
| Kategorie | Variable                                      | verfügbare Werte oder Schema | default              |
|-----------|-----------------------------------------------|------------------------------|----------------------|
| Database  | TTP_NOTI_DB_HOST **<-- Alias von NOTI_DB_HOST | \<STRING\>                   | mysql                |
| Database  | TTP_NOTI_DB_PORT **<-- Alias von NOTI_DB_PORT | 0-65535                      | 3306                 |
| Database  | TTP_NOTI_DB_NAME **<-- Alias von NOTI_DB_NAME | \<STRING\>                   | notification_service |
| Database  | TTP_NOTI_DB_USER **<-- Alias von NOTI_DB_USER | \<STRING\>                   | noti_user            |
| Database  | TTP_NOTI_DB_PASS **<-- Alias von NOTI_DB_PASS | \<STRING\>                   | noti_password        |

#### ttp_fhir.env **<-- ehemals fhir.env**
| Kategorie   | Variable                                     | verfügbare Werte oder Schema         | default         |
|-------------|----------------------------------------------|--------------------------------------|-----------------|
| Security    | TTP_FHIR_KEYCLOAK_ENABLE                     | true, false                          | false           |
| Security    | TTP_FHIR_KEYCLOAK_REALM                      | \<STRING\>                           | ttp             |
| Security    | TTP_FHIR_KEYCLOAK_CLIENT_ID                  | \<STRING\>                           | fhir            |
| Security    | TTP_FHIR_KEYCLOAK_SSL_REQUIRED               | none, external, all                  | all             |
| Security    | TTP_FHIR_KEYCLOAK_SERVER_URL                 | \<PROTOCOL://HOST_OR_IP:PORT/auth/\> | -               |
| Security    | TTP_FHIR_KEYCLOAK_CLIENT_SECRET              | \<STRING\>                           | -               |
| Security    | TTP_FHIR_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS | true, false                          | false           |
| Security    | TTP_FHIR_KEYCLOAK_CONFIDENTIAL_PORT          | 0-65535                              | 8443            |
| Security    | TTP_FHIR_KEYCLOAK_ROLE_GPAS_USER             | \<STRING\>                           | role.gpas.user  |
| Security    | TTP_FHIR_KEYCLOAK_ROLE_GPAS_ADMIN            | \<STRING\>                           | role.gpas.admin |

#### ttp_gras.env **<-- neu, Werte aus ttp_commons.env ausgelagert**
| Kategorie | Variable                                      | verfügbare Werte oder Schema | default       |
|-----------|-----------------------------------------------|------------------------------|---------------|
| Database  | TTP_GRAS_DB_HOST **<-- Alias von GRAS_DB_HOST | \<STRING\>                   | mysql         |
| Database  | TTP_GRAS_DB_PORT **<-- Alias von GRAS_DB_PORT | 0-65535                      | 3306          |
| Database  | TTP_GRAS_DB_NAME **<-- Alias von GRAS_DB_NAME | \<STRING\>                   | gras          |
| Database  | TTP_GRAS_DB_USER **<-- Alias von GRAS_DB_USER | \<STRING\>                   | gras_user     |
| Database  | TTP_GRAS_DB_PASS **<-- Alias von GRAS_DB_PASS | \<STRING\>                   | gras_password |

#### ttp_commons.env **<-- ehemals wildfly.env**
| Kategorie     | Variable                                                                                      | verfügbare Werte oder Schema           | default          |
|---------------|-----------------------------------------------------------------------------------------------|----------------------------------------|------------------|
| Logging       | WF_CONSOLE_LOG_LEVEL **<-- ehemals CONSOLE_LOG_LEVEL**                                        | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO             |
| WF-Admin      | WF_NO_ADMIN **<-- ehemals NO_ADMIN**                                                          | true, false                            | false            |
| WF-Admin      | WF_ADMIN_USER **<-- ehemals ADMIN_USER**                                                      | \<STRING\>                             | admin            |
| WF-Admin      | WF_ADMIN_PASS **<-- ehemals WILDFLY_PASS**                                                    | \<STRING\>                             | wildfly_password |
| Security      | TTP_KEYCLOAK_SERVER_URL **<-- Alias von KEYCLOAK_SERVER_URL**                                 | \<PROTOCOL://HOST_OR_IP:PORT/auth/\>   | -                |
| Security      | TTP_KEYCLOAK_SSL_REQUIRED **<-- Alias von KEYCLOAK_SSL_REQUIRED**                             | none, external, all                    | all              |
| Security      | TTP_KEYCLOAK_REALM **<-- Alias von KEYCLOAK_REALM**                                           | \<STRING\>                             | -                |
| Security      | TTP_KEYCLOAK_CLIENT_ID **<-- Alias von KEYCLOAK_RESOURCE**                                    | \<STRING\>                             | -                |
| Security      | TTP_KEYCLOAK_CLIENT_SECRET **<-- Alias von KEYCLOAK_CLIENT_SECRET**                           | \<STRING\>                             | -                |
| Security      | TTP_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS **<-- Alias von KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS** | true, false                            | false            |
| Security      | TTP_KEYCLOAK_CONFIDENTIAL_PORT **<-- Alias von KEYCLOAK_CONFIDENTIAL_PORT**                   | 0-65535                                | 8443             |
| Web-Security  | TTP_WEB_KEYCLOAK_REALM **<-- neu**                                                            | \<STRING\>                             | ttp              |
| Web-Security  | TTP_WEB_KEYCLOAK_CLIENT_ID **<-- neu**                                                        | \<STRING\>                             | ths              |
| Web-Security  | TTP_WEB_KEYCLOAK_SERVER_URL **<-- neu**                                                       | \<PROTOCOL://HOST_OR_IP:PORT/auth/\>   | -                |
| Web-Security  | TTP_WEB_KEYCLOAK_SSL_REQUIRED **<-- neu**                                                     | none, external, all                    | all              |
| Web-Security  | TTP_WEB_KEYCLOAK_CLIENT_SECRET **<-- neu**                                                    | \<STRING\>                             | -                |
| Web-Security  | TTP_WEB_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS **<-- neu**                                       | true, false                            | false            |
| Web-Security  | TTP_WEB_KEYCLOAK_CONFIDENTIAL_PORT **<-- neu**                                                | 0-65535                                | 8443             |
| SOAP-Security | TTP_SOAP_KEYCLOAK_REALM **<-- neu**                                                           | \<STRING\>                             | ttp              |
| SOAP-Security | TTP_SOAP_KEYCLOAK_CLIENT_ID **<-- neu**                                                       | \<STRING\>                             | ths              |
| SOAP-Security | TTP_SOAP_KEYCLOAK_SERVER_URL **<-- neu**                                                      | \<PROTOCOL://HOST_OR_IP:PORT/auth/\>   | -                |
| SOAP-Security | TTP_SOAP_KEYCLOAK_SSL_REQUIRED **<-- neu**                                                    | none, external, all                    | all              |
| SOAP-Security | TTP_SOAP_KEYCLOAK_CLIENT_SECRET **<-- neu**                                                   | \<STRING\>                             | -                |
| SOAP-Security | TTP_SOAP_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS **<-- neu**                                      | true, false                            | false            |
| SOAP-Security | TTP_SOAP_KEYCLOAK_CONFIDENTIAL_PORT **<-- neu**                                               | 0-65535                                | 8443             |
| Quality       | WF_HEALTHCHECK_URLS **<-- ehemals HEALTHCHECK_URLS**                                          | \<SPACE-SEPARATED-URLs\>               | -                |
| Optimizing    | WF_ADD_CLI_FILTER **<-- neu**                                                                 | \<SPACE-SEPARATED-STRING\>             | -                |
| Optimizing    | WF_MAX_POST_SIZE **<-- Alias von MAX_POST_SIZE**                                              | \<BYTES\>                              | 10485760         |
| Optimizing    | WF_MAX_CHILD_ELEMENTS **<-- Alias von MAX_CHILD_ELEMENTS**                                    | \<INTEGER\>                            | 50000            |
| Optimizing    | WF_BLOCKING_TIMEOUT **<-- neu**                                                               | \<SECONDS\>                            | 300              |
| Optimizing    | WF_TRANSACTION_TIMEOUT **<-- neu**                                                            | \<SECONDS\>                            | 300              |
| Optimizing    | WF_DISABLE_HTTP2 **<-- neu**                                                                  | true, false                            | false            |
| Optimizing    | WF_MARKERFILES **<-- ehemals WILDFLY_MARKERFILES**                                            | true, false, auto                      | auto             |
| Optimizing    | TZ                                                                                            | \<STRING\>                             | Europe/Berlin    |
| Optimizing    | JAVA_OPTS                                                                                     | \<STRING\>                             | -                |

#### mysql.env
| Kategorie  | Variable            | verfügbare Werte oder Schema | default       |
|------------|---------------------|------------------------------|---------------|
| Security   | MYSQL_ROOT_PASSWORD | \<STRING\>                   | root          |
| Optimizing | TZ **<-- neu**      | \<STRING\>                   | Europe/Berlin |

---
## 8. Additional Information
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