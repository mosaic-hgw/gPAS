ALTER TABLE
    `psn_projects` RENAME `domain`;

ALTER TABLE `domain`
    DROP INDEX `FK_PARENT_DOMAIN`,
    DROP FOREIGN KEY `FK_PARENT_DOMAIN`;
ALTER TABLE `psn`
	DROP FOREIGN KEY `FK_DOMAIN`;
ALTER TABLE `psn`
CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE `domain`
CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE `domain`
    CHANGE COLUMN `domain` `name` VARCHAR(255) NOT NULL COLLATE 'utf8_bin' FIRST;
ALTER TABLE `psn`
	ADD CONSTRAINT `FK_DOMAIN` FOREIGN KEY (`domain`) REFERENCES `domain` (`name`);

ALTER TABLE `domain`
    CHANGE COLUMN `properties` `properties` VARCHAR(1023) DEFAULT NULL COLLATE 'utf8_bin';
ALTER TABLE `domain`
    ADD COLUMN `label` VARCHAR(255) NULL DEFAULT NULL AFTER `name`;
UPDATE `domain` d
    SET `label` = (SELECT `d2`.`name` FROM (SELECT `name` FROM `domain`) d2 WHERE `d`.`name` = `d2`.`name`);

ALTER TABLE `psn` DROP KEY `fk_domain`;

CREATE TABLE `domain_parents` (
    `domain` varchar(255) NOT NULL,
    `parentDomain` varchar(255) NOT NULL,
    PRIMARY KEY (`domain`,`parentDomain`),
    KEY `FK_domain_parents_domain_2` (`parentDomain`),
    CONSTRAINT `FK_domain_parents_domain` FOREIGN KEY (`domain`) REFERENCES `domain` (`name`),
    CONSTRAINT `FK_domain_parents_domain_2` FOREIGN KEY (`parentDomain`) REFERENCES `domain` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

INSERT INTO `domain_parents` (`domain`, `parentDomain`)
    SELECT `name`, `parentDomain` FROM `domain` d WHERE d.`parentDomain` IS NOT NULL;

ALTER TABLE `domain`
    DROP COLUMN `parentDomain`;

DROP VIEW IF EXISTS `psn_domain_count`;

CREATE VIEW `psn_domain_count` AS
    SELECT
        CONCAT('pseudonyms_per_domain.', `t1`.`name`) AS `attribut`,
        COUNT(`t2`.`pseudonym`) AS `value`
    FROM
        (`domain` `t1`
        JOIN `psn` `t2` ON ((`t2`.`domain` = `t1`.`name`)))
    WHERE
        (NOT ((`t2`.`pseudonym` LIKE '%anonym%')))
    GROUP BY `t1`.`name`;

CREATE TABLE healthcheck
(
   NAME VARCHAR(16) PRIMARY KEY NOT NULL
);

INSERT INTO healthcheck VALUES ('healthcheck');

drop procedure if exists `updateStats`;

delimiter ?

CREATE PROCEDURE `updateStats`()
begin
  INSERT INTO
    stat_entry (entrydate) values (NOW());

  SET @id = (select max(stat_entry_id) from stat_entry);

  INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'domains',
    (SELECT count(*) FROM domain where name!='internal_anonymisation_domain'));

    INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'anonyms',
    (SELECT count(*) FROM psn as psn_table where psn_table.domain='internal_anonymisation_domain'));

    INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'pseudonyms',
    (SELECT count(*) FROM psn as psn_table where psn_table.domain!='internal_anonymisation_domain'));

  INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) SELECT @id, psncount.* FROM psn_domain_count as psncount order by psncount.attribut asc;


  SELECT t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value
    FROM stat_entry AS t1, stat_value AS t2
    WHERE t1.stat_entry_id = t2.stat_value_id;
end?

delimiter ;
