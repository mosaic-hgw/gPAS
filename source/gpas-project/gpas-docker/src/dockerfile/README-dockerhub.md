${ttp.readme.header}


# About
The use of pseudonyms is a privacy-enhancing technique supporting privacy-by-design and ensuring non-attribution.
Pseudonymisation allows storing directly person identifying data separately and securely from medical data and
supports the data controller to meet the GDPR’s data security requirements (Art. 32 lit. 1 EU GDPR).

To facilitate the generation and administration of appropriate pseudonyms the Institute for
Community Medicine of the University Medicine Greifswald (UMG) developed the web-service-based gPAS.

The use of pseudonymization domains, the specification of individual alphabets and generator algorithms
allow for the free generation of different pseudonyms per data source, application context or study site.

## General
This gPAS image is completely identical to the image [mosaicgreifswald/wildfly:26](https://hub.docker.com/r/mosaicgreifswald/wildfly),<br/>
except that it already contains the deployments and JBoss-CLIs from our [gPAS](https://www.ths-greifswald.de/en/gpas).

# API

## SOAP

All functionalities of the gPAS are provided for external use via SOAP-interfaces.
The [JavaDoc specs for the Services](https://www.ths-greifswald.de/gpas/doc "")
are available online (see package `org.emau.icmvc.ganimed.ttp.psn`).

Use SOAP-UI to create sample requests based on the WSDL files.

### Service-Interface for PSN Management

The WSDL URL is [http://&lt;YOUR IPADDRESS&gt;:8080/gpas/gpasService?wsdl](https://demo.ths-greifswald.de/gpas/gpasService?wsdl)

### Service-Interface  for PSN Management with Notifications

The WSDL URL is [http://&lt;YOUR IPADDRESS&gt;:8080/gpas/gpasServiceWithNotification?wsdl](https://demo.ths-greifswald.de/gpas/gpasServiceWithNotification?wsdl)

### Service-Interface for Configuration and Domain Management

The WSDL URL is [http://&lt;YOUR IPADDRESS&gt;:8080/gpas/DomainService?wsdl](https://demo.ths-greifswald.de/gpas/DomainService?wsdl)

## FHIR

More details from https://www.ths-greifswald.de/gpas/fhir

# IT-Security Recommendations #
Access to relevant application and database servers of the Trusted Third Party tools should only be possible for authorised personnel and via authorised end devices. We therefore recommend additionally implementing the following IT security measures:

* Operation of the relevant servers in separate network zones (separate from the research and supply network).
* Use of firewalls and IP filters
* Access restriction at URL level with Basic Authentication (e.g. with NGINX or Apache)
* use of Keycloak to restrict access to Web-Frontends and technical interfaces


# Relevant for this Image

## ENV-Variables and defaults
| Category   | Variable                          | Available values or scheme             | Default                                              | Purpose                                                                             |
|------------|-----------------------------------|----------------------------------------|------------------------------------------------------|-------------------------------------------------------------------------------------|
| Optimizing | TZ                                | \<STRING\>                             | Europe/Berlin                                        |                                                                                     |
| WF-Admin   | WF_NO_ADMIN                       | true \| false                          | false                                                | set `true` if you don't need wildfly-admin                                          |
| WF-Admin   | WF_ADMIN_USER                     | \<STRING\>                             | admin                                                | define username for wildfly-admin                                                   |
| WF-Admin   | WF_ADMIN_PASS                     | \<STRING\>                             | -random-                                             | to set password for wildfly-admin                                                   |
| Quality    | WF_HEALTHCHECK_URLS               | \<NEWLINE-SEPARATED-URLs\>             | -                                                    | contain a list of urls to check the health of this container                        |
| Optimizing | JAVA_OPTS                         | \<STRING\>                             | -                                                    | you need more memory? then give yourself more memory or define any system-variables |
| Debugging  | WF_DEBUG                          | true \| false                          | false                                                | set `true` to enable debug-mode in wildfly                                          |
| Debugging  | DEBUG_PORT                        | \<IP\>:\<PORT\>                        | *:8787                                               | for debugging you can change the ip:port                                            |
| Logging    | TTP_GPAS_LOG_TO_FILE              | true, false                            | false                                                |
| Logging    | TTP_GPAS_LOG_LEVEL                | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO                                                 |
| Database   | TTP_GPAS_DB_HOST                  | \<STRING\>                             | mysql                                                |
| Database   | TTP_GPAS_DB_PORT                  | 0-65535                                | 3306                                                 |
| Database   | TTP_GPAS_DB_NAME                  | \<STRING\>                             | gpas                                                 |
| Database   | TTP_GPAS_DB_USER                  | \<STRING\>                             | gpas_user                                            |
| Database   | TTP_GPAS_DB_PASS                  | \<STRING\>                             | gpas_password                                        |
| Security   | TTP_GPAS_WEB_AUTH_MODE            | gras, keycloak                         | -                                                    | 
| Security   | TTP_GPAS_SOAP_KEYCLOAK_ENABLE     | true, false                            | -                                                    |
| Security   | TTP_GPAS_SOAP_ROLE_USER_NAME      | \<STRING\>                             | role.gpas.user                                       |
| Security   | TTP_GPAS_SOAP_ROLE_USER_SERVICES  | \<STRING\>                             | /gpas/gpasService,/gpas/gpasServiceWithNotification  |
| Security   | TTP_GPAS_SOAP_ROLE_ADMIN_NAME     | \<STRING\>                             | role.gpas.admin                                      |
| Security   | TTP_GPAS_SOAP_ROLE_ADMIN_SERVICES | \<STRING\>                             | /gpas/DomainService                                  |


## Entrypoints
| Path                     | ref. ENV-Variable  | Type   | Purpose                                                                                                                            |
|--------------------------|--------------------|--------|------------------------------------------------------------------------------------------------------------------------------------|
| /entrypoint-java-cacerts | ENTRY_JAVA_CACERTS | file   | The entrypoint can be used to store its own cacerts, e.g. containing public-keys of server-certificates for specific web requests. |
| /entrypoint-wildfly-logs | ENTRY_WILDFLY_LOGS | folder | to export all available log-files (read/write access)                                                                              |


# Usage
You need a **prepared** MySQL database to start the gPAS successfully.
```shell
# with external db
> docker run --rm -it \
    -e TTP_GPAS_DB_HOST=host_or_ip \
    -e TTP_GPAS_DB_PORT=3306 \
    -e TTP_GPAS_DB_USER=gpas \
    -e TTP_GPAS_DB_PASS=top-secret \
    -p 8080:8080 \
    mosaicgreifswald/gpas:2023.1.2
```


## Usage with docker compose
over docker-compose with dependent on mysql-db (example)
```yml
# docker-compose.yml

version: '3'
services:
  mysql:
    image: mysql
    environment:
      MYSQL_ROOT_PASSWORD: top-secret
    volumes:
      - /path/to/your/init-sql-files:/docker-entrypoint-initdb.d
  wildfly:
    image: mosaicgreifswald/gpas:2023.1.2
    ports:
      - 8080:8080
      - 9990:9990
    depends_on:
      - mysql
    environment:
      WF_ADMIN_PASS: top-secret
      WF_HEALTHCHECK_URLS: http://localhost:8080
    entrypoint: /bin/bash
    command: -c "./wait-for-it.sh mysql:3306 -t 60 && ./run.sh"
```


"versions" shows all installed tools and components, with their versions.
```shell
> docker run --rm mosaicgreifswald/gpas:2023.1.2 versions
  last updated               : 2023-10-30 08:56:54
  Distribution               : Debian GNU/Linux 12.2
  zulu-jre                   : 17.0.9
  WildFly                    : 26.1.3.Final
  MySQL-Connector            : 8.0.33
  EclipseLink                : 2.7.12
  KeyCloak-Client            : 19.0.2
  gPAS                       : 2023.1.2
  FHIR-Gateway               : 2023.1.2
  Notification-Service       : 2023.1.1
```


## Current Software-Versions on this Image
| Date                                       | Tags                                               | Changes                                                                                                                                                                                                                     |
|--------------------------------------------|----------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 2023-11-27<br><br><br><br><br><br><br><br> | `2023.1.2`, `2023`<br><br><br><br><br><br><br><br> | **Debian** 12.2 "bookworm"<br>**openJRE** 17.0.9<br>**WildFly** 26.1.3.Final<br>**EclipseLink** 2.7.12<br>**KeyCloak-Client** 19.0.2<br>**gPAS** 2023.1.2<br>**FHIR-Gateway** 2023.1.2<br>**Notification-Service** 2023.1.1 |


${ttp.readme.footer}