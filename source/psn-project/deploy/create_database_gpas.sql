DROP SCHEMA IF EXISTS `gpas` ;
create database gpas collate utf8mb4_unicode_ci;

use gpas;

CREATE TABLE `domain` (
  `name` varchar(255) NOT NULL,
  `label` varchar(255) DEFAULT NULL,
  `alphabet` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `generatorClass` varchar(255) DEFAULT NULL,
  `properties` varchar(1023) DEFAULT NULL,
  create_timestamp timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  update_timestamp timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

CREATE TABLE `domain_parents` (
    `domain` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    `parentDomain` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
    PRIMARY KEY (`domain`,`parentDomain`),
    KEY `FK_domain_parents_domain_2` (`parentDomain`),
    CONSTRAINT `FK_domain_parents_domain` FOREIGN KEY (`domain`) REFERENCES `domain` (`name`),
    CONSTRAINT `FK_domain_parents_domain_2` FOREIGN KEY (`parentDomain`) REFERENCES `domain` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

CREATE TABLE `psn` (
  `originalValue` varchar(255) NOT NULL,
  `pseudonym` varchar(255) DEFAULT NULL,
  `domain` varchar(255) NOT NULL,
  PRIMARY KEY (`domain`,`originalValue`),
  UNIQUE KEY `domain_pseudonym` (`domain`,`pseudonym`),
  CONSTRAINT `FK_DOMAIN` FOREIGN KEY (`domain`) REFERENCES `domain` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

CREATE  TABLE IF NOT EXISTS `stat_entry` (
  `STAT_ENTRY_ID` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `ENTRYDATE` TIMESTAMP(3) NULL DEFAULT NULL,
  PRIMARY KEY (`STAT_ENTRY_ID`)   )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE  TABLE IF NOT EXISTS `stat_value` (
  `stat_value_id` BIGINT(20) NULL DEFAULT NULL,
  `stat_value` BIGINT(20) NULL DEFAULT NULL,
  `stat_attr` VARCHAR(255) NULL DEFAULT NULL,
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

drop view if exists `psn_domain_count`;

CREATE VIEW `psn_domain_count` AS
    SELECT
        CONCAT('pseudonyms_per_domain.', `t1`.`name`) AS `attribut`,
        COUNT(`t2`.`pseudonym`) AS `value`
    FROM
        `domain` `t1`
        JOIN `psn` `t2` ON ((`t2`.`domain` = `t1`.`name`))
    WHERE
    	`t1`.name != 'internal_anonymisation_domain'
    GROUP BY `t1`.`name`;

create user 'gpas_user'@'%' identified by 'gpas_password';
grant all on gpas.* to 'gpas_user'@'%';
