create database gpas;

use gpas;

CREATE TABLE `psn_projects` (
  `domain` varchar(255) NOT NULL,
  `alphabet` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `generatorClass` varchar(255) DEFAULT NULL,
  `properties` varchar(255) DEFAULT NULL,
  `parentDomain` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`domain`),
  KEY `FK_PARENT_DOMAIN` (`parentDomain`),
  CONSTRAINT `FK_PARENT_DOMAIN` FOREIGN KEY (`parentDomain`) REFERENCES `psn_projects` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `psn` (
  `originalValue` varchar(255) NOT NULL,
  `pseudonym` varchar(255) DEFAULT NULL,
  `domain` varchar(255) NOT NULL,
  PRIMARY KEY (`domain`,`originalValue`),
  UNIQUE KEY `domain_pseudonym` (`domain`,`pseudonym`),
  KEY `FK_DOMAIN` (`domain`),
  CONSTRAINT `FK_DOMAIN` FOREIGN KEY (`domain`) REFERENCES `psn_projects` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE  TABLE IF NOT EXISTS `stat_entry` (
  `STAT_ENTRY_ID` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `ENTRYDATE` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`STAT_ENTRY_ID`)   )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE  TABLE IF NOT EXISTS `stat_value` (
  `stat_value_id` BIGINT(20) NULL DEFAULT NULL,
  `stat_value` VARCHAR(255) NULL DEFAULT NULL,
  `stat_attr` VARCHAR(50) NULL DEFAULT NULL,
  INDEX `FK_stat_value_stat_value_id` (`stat_value_id` ASC),
  CONSTRAINT `FK_stat_value_stat_value_id`
    FOREIGN KEY (`stat_value_id` )
    REFERENCES `stat_entry` (`STAT_ENTRY_ID` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE sequence
(
   SEQ_NAME varchar(50) PRIMARY KEY NOT NULL,
   SEQ_COUNT decimal(38,0)
)
;

DROP procedure IF EXISTS `updateStats`;

DELIMITER $$
CREATE PROCEDURE `updateStats`()
begin
	INSERT INTO 
		stat_entry (entrydate) values (NOW());

	SET @id = (select max(stat_entry_id) from stat_entry);

	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'pseudonyms',
		(SELECT count(pseudonym) FROM psn as psn_table where psn_table.domain!='internal_anonymisation_domain'));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'anonyms',
		(SELECT count(pseudonym) FROM psn as psn_table where psn_table.domain='internal_anonymisation_domain'));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'domains',
		(SELECT count(domain) FROM psn_projects where domain!='internal_anonymisation_domain'));

	SELECT t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value 
		FROM stat_entry AS t1, stat_value AS t2
		WHERE t1.stat_entry_id = t2.stat_value_id;
end$$

DELIMITER ;

create user 'gpas_user'@'localhost' identified by 'gpas_2014';

grant all on gpas.* to 'gpas_user'@'localhost' identified by 'gpas_2014';
