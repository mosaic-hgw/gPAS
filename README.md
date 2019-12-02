![context](https://user-images.githubusercontent.com/12081369/49164566-a5794200-f32f-11e8-8d3a-96244ea00832.png)

Current Source Code Version: 1.7.10

The gPAS generates and administers appropriate pseudonyms using non-deterministic pseudonyms for arbitrary alphanumeric sequences. Additionally it allows defining domain-specific alphabets and generator algorithms as required and offers functions for de-pseudonymisation and anonymisation.

# Docker and source code
This repository does not provide the latest version of gpas. Please find the latest versions of gPAS here:
* docker-compose: https://github.com/mosaic-hgw/Dockerbank/tree/master/gPAS
* source code: https://www.ths-greifswald.de/kontakt/ 

# License
This Software was developed by the Institute for Community Medicine of the University Medicine Greifswald. It it licensed under AGPLv3 and provided by the DFG-funded MOSAIC-Project (grant number HO 1937/2-1).

# Build
To build gPAS with maven use the goals "clean install".

# Web-based Interface
All functionalities of the gPAS are provided for external use via a SOAP-Interface. Use SOAP-UI to create sample requests. (Please modify IP Address and Port accordingly).

[gPAS DomainManager Interface-Description (JavaDoc)](https://www.ths-greifswald.de/wp-content/uploads/tools/gpas/doc/1-7-10/org/emau/icmvc/ganimed/ttp/psn/DomainManager.html  "gPAS Domainmanager Service Interface Description")

The WSDL URL is ``http://<YOUR IPADDRESS>:8080/gpas/DomainService?wsdl``

[gPAS PSNManager Service Interface-Description (JavaDoc)](https://www.ths-greifswald.de/wp-content/uploads/tools/gpas/doc/1-7-10/org/emau/icmvc/ganimed/ttp/psn/PSNManager.html "gPAS PSNManager Service Interface Description")

 The WSDL URL is ``http://<YOUR IPADDRESS>:8080/gpas/gpasService?wsdl``

# More Information
Visit https://www.ths-greifswald.de/gpas
