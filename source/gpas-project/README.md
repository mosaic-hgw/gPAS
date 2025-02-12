${ttp.readme.header}

# About #
The use of pseudonyms is a privacy-enhancing technique supporting privacy-by-design and ensuring non-attribution. Pseudonymisation allows storing directly person identifying data separately and securely from medical data and supports the data controller to meet the GDPR’s data security requirements (Art. 32 lit. 1 EU GDPR).

To facilitate the generation and administration of appropriate pseudonyms the Institute for Community Medicine of the University Medicine Greifswald (UMG) developed the web-service-based gPAS.

The use of pseudonymization domains, the specification of individual alphabets and generator algorithms allow for the free generation of different pseudonyms per data source, application context or study site.

![context](https://www.ths-greifswald.de/wp-content/uploads/2019/01/gPAS-Screenshot-Pseudonymisieren.png)

# Download #

[Latest Docker-compose version of gPAS](https://www.ths-greifswald.de/gpas/#_download "")

# Source #

https://github.com/mosaic-hgw/gPAS/tree/master/source

## Live-Demo and more information ##

Try out gPAS from https://demo.ths-greifswald.de

or visit https://ths-greifswald.de/gpas for more information.

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

${ttp.readme.footer}
