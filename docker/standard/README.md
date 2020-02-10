
![context](https://user-images.githubusercontent.com/12081369/49164566-a5794200-f32f-11e8-8d3a-96244ea00832.png)

Current Docker-Version of gPAS: 1.9.1 (Feb 2020)

# Installing the standard-version of gPAS with docker-compose #

Note: your account needs administrative privileges to use docker
change to super user (su) or run the following commands with sudo

Download files

```git clone https://github.com/mosaic-hgw/gPAS```

grant read/write permissission to contained sub-folders

```sudo chmod -R 777 gPAS/docker```

change to gPAS folder

```sudo cd gPAS/docker/standard ```

if applicable: stop runnging mysql services on port 3306 

```sudo service mysql stop```

check docker version (required 1.13.1 or above)

```sudo docker -v```

check docker-compose version (required 1.8.0 or above)

```sudo docker-compose -v```

Note: The default publishing port of the application server is 8080. Modify if necessary in jboss/configure_wildfly_gpas.cli

run docker-compose to pull wildfly and mysql images and to configure gPAS

```sudo docker-compose up```

this will start pulling and configuration of mysql and jboss wildfly and automatically deployment of gPAS in the current version.

installation process takes up to 7 minutes (depending on your internet connection) and succeeded if the following output is shown

open browser and try out the gPAS from http://YOURIPADDRESS:8080/gpas-web

finish and close gPAS application server with CTRL+C

# Additional Information #

The gPAS was developed by the University Medicine Greifswald  and published in 2014 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1).

## Credits ##
Concept and implementation: L. Geidel
Web-Client: A. Blumentritt

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright: 2014 - 2020 University Medicine Greifswald

Contact: https://www.ths-greifswald.de/kontakt/