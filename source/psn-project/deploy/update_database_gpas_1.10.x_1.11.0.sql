-- attention! dependent on your sql-client it may be neccessary to change "modify" with "change column"
alter table domain add create_timestamp timestamp(3) not null default current_timestamp(3);
alter table domain add update_timestamp timestamp(3) not null default current_timestamp(3);

update `sequence` set `SEQ_COUNT` = (select max(`STAT_ENTRY_ID`) from `stat_entry`) where `SEQ_NAME` = 'statistic_index';
alter table `stat_value` modify `stat_value` bigint(20);
alter table `stat_value` modify `stat_attr` varchar(255);
alter table `stat_entry` modify `ENTRYDATE` timestamp(3);

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

  INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) SELECT @id, psncount.* FROM psn_domain_count as psncount order by psncount.attribut asc;

  INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'pseudonyms',
    (select x.summe from (select sum(stat_value) as summe from stat_value where stat_value_id = @id and stat_attr like 'pseudonyms_per%')x));

  SELECT t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value
    FROM stat_entry AS t1, stat_value AS t2
    WHERE t1.stat_entry_id = t2.stat_value_id;
end?

delimiter ;

DROP TABLE healthcheck;