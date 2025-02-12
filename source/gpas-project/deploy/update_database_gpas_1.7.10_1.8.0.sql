drop view if exists `psn_domain_count`;

CREATE     
VIEW `psn_domain_count` AS
    SELECT 
        CONCAT('pseudonyms_per_domain.', `t1`.`domain`) AS `attribut`,
        COUNT(`t2`.`pseudonym`) AS `value`
    FROM
        (`psn_projects` `t1`
        JOIN `psn` `t2` ON ((`t2`.`domain` = `t1`.`domain`)))
    WHERE
        (NOT ((`t2`.`pseudonym` LIKE '%anonym%')))
    GROUP BY `t1`.`domain`;
    
drop procedure if exists `updateStats`;

delimiter ?
    
CREATE PROCEDURE `updateStats`()
begin
	INSERT INTO 
		stat_entry (entrydate) values (NOW());

	SET @id = (select max(stat_entry_id) from stat_entry);

	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'domains',
		(SELECT count(domain) FROM psn_projects where domain!='internal_anonymisation_domain'));
	
    INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'anonyms',
		(SELECT count(pseudonym) FROM psn as psn_table where psn_table.domain='internal_anonymisation_domain'));
	
    INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'pseudonyms',
		(SELECT count(pseudonym) FROM psn as psn_table where psn_table.domain!='internal_anonymisation_domain'));
	
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) SELECT @id, psncount.* FROM psn_domain_count as psncount order by psncount.attribut asc;
                

	SELECT t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value 
		FROM stat_entry AS t1, stat_value AS t2
		WHERE t1.stat_entry_id = t2.stat_value_id;
end?

delimiter ;