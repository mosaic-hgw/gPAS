CREATE  TABLE IF NOT EXISTS `stat_entry` (
  `STAT_ENTRY_ID` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `ENTRYDATE` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`STAT_ENTRY_ID`)   )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE  TABLE IF NOT EXISTS `stat_value` (
  `stat_value_id` BIGINT(20) NULL DEFAULT NULL ,
  `stat_value` VARCHAR(255) NULL DEFAULT NULL ,
  `stat_attr` VARCHAR(50) NULL DEFAULT NULL ,
  INDEX `FK_stat_value_stat_value_id` (`stat_value_id` ASC) ,
  CONSTRAINT `FK_stat_value_stat_value_id`
    FOREIGN KEY (`stat_value_id` )
    REFERENCES `stat_entry` (`STAT_ENTRY_ID` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

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

grant all on gpas.* to 'gpas_user'@'localhost' identified by 'gpas_2014';
