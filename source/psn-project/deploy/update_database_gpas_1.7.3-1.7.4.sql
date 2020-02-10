ALTER TABLE `psn_projects`
ADD COLUMN `parentDomain` varchar(255) DEFAULT NULL;

ALTER TABLE `psn_projects`
ADD KEY `FK_PARENT_DOMAIN` (`parentDomain`);

ALTER TABLE `psn_projects`
ADD CONSTRAINT `FK_PARENT_DOMAIN` FOREIGN KEY (`parentDomain`) REFERENCES `psn_projects` (`domain`);