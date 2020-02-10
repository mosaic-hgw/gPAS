
![context](https://user-images.githubusercontent.com/12081369/49164566-a5794200-f32f-11e8-8d3a-96244ea00832.png)

# About #
The use of pseudonyms is a privacy-enhancing technique supporting privacy-by-design and ensuring non-attribution. Pseudonymisation allows storing directly person identifying data separately and securely from medical data and supports the data controller to meet the GDPR’s data security requirements (Art. 32 lit. 1 EU GDPR).

To facilitate the generation and administration of appropriate pseudonyms the Institute for Community Medicine of the University Medicine Greifswald (UMG) developed the web-service-based gPAS.

The use of pseudonymization domains, the specification of individual alphabets and generator algorithms allow for the free generation of different pseudonyms per data source, application context or study site.

![context](https://github.com/mosaic-hgw/Dockerbank/blob/master/gPAS/screenshots/psn-overview.png)

## Live-Demo and more information ##

Try out gPAS from https://demo.ths-greifswald.de 

or visit https://ths-greifswald.de/gpas for more information.

# Versions and documentation #

[Source-Code](https://github.com/mosaic-hgw/gPAS/source "")

[Docker-compose version of gPAS (Standard)](https://github.com/mosaic-hgw/gPAS/docker/standard "")

[Docker-compose version of gPAS (Web-Auth)](https://github.com/mosaic-hgw/gPAS/docker/web-auth "")

# Web-based interfaces
All functionalities of the gPAS are provided for external use via SOAP-interfaces. 

[gPAS DomainManager Interface-Description (JavaDoc)](https://www.ths-greifswald.de/wp-content/uploads/tools/gpas/doc/1-9-0/org/emau/icmvc/ganimed/ttp/psn/DomainManager.html)

The WSDL URL is <strong>http://<YOUR IPADDRESS>:8080/gpas/DomainService?wsdl</strong>

[gPAS PSNManager Service Interface-Description (JavaDoc)](https://www.ths-greifswald.de/wp-content/uploads/tools/gpas/doc/1-9-0/org/emau/icmvc/ganimed/ttp/psn/PSNManager.html "")

The WSDL URL is <strong>http://<YOUR IPADDRESS>:8080/gpas/gpasService?wsdl</strong>

Use SOAP-UI to create sample requests.

# IT-Security Recommendations #
For the operation of gPAS at least following IT-security measures are recommended:
* operation in a separate network-zone
* use of firewalls and IP-filters
* access restriction to the gPAS-Servers with basic authentication (e.g. with nginx or apache)

# Additional Information #

The gPAS was developed by the University Medicine Greifswald  and published in 2014 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1).

## Credits ##
Concept and implementation: L. Geidel

Web-Client: A. Blumentritt

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright: 2014 - 2020 University Medicine Greifswald

Contact: https://www.ths-greifswald.de/kontakt/

## Publications ##
https://dx.doi.org/10.3414/ME14-01-0133

https://dx.doi.org/10.1186/s12967-015-0545-6

# Screenshots #

Domain Configuration

![context](https://github.com/mosaic-hgw/Dockerbank/blob/master/gPAS/screenshots/add_domain.png)

List processing

![context](https://github.com/mosaic-hgw/Dockerbank/blob/master/gPAS/screenshots/list-processing.png)

Show Pseudonym trees

![context](https://github.com/mosaic-hgw/Dockerbank/blob/master/gPAS/screenshots/psn-tree.png)
