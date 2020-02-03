create database gpas;

--disconnect and reconnect to database gpas

CREATE TABLE psn_projects (
  domain varchar(255) NOT NULL,
  alphabet varchar(255) DEFAULT NULL,
  comment varchar(255) DEFAULT NULL,
  generatorClass varchar(255) DEFAULT NULL,
  properties varchar(255) DEFAULT NULL,
  parentDomain varchar(255) DEFAULT NULL,
  PRIMARY KEY (domain),
  CONSTRAINT FK_PARENT_DOMAIN FOREIGN KEY (parentDomain) REFERENCES psn_projects (domain)
);

CREATE TABLE psn (
  originalValue varchar(255) NOT NULL,
  pseudonym varchar(255) DEFAULT NULL,
  domain varchar(255) NOT NULL,
  PRIMARY KEY (domain,originalValue),
  UNIQUE (domain,pseudonym),
  CONSTRAINT FK_DOMAIN FOREIGN KEY (domain) REFERENCES psn_projects (domain)
);

CREATE  TABLE IF NOT EXISTS stat_entry (
  STAT_ENTRY_ID SERIAL,
  ENTRYDATE VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (STAT_ENTRY_ID)   
);

CREATE  TABLE IF NOT EXISTS stat_value (
  stat_value_id BIGINT NULL DEFAULT NULL,
  stat_value VARCHAR(255) NULL DEFAULT NULL,
  stat_attr VARCHAR(50) NULL DEFAULT NULL,
  CONSTRAINT FK_stat_value_stat_value_id FOREIGN KEY (stat_value_id ) REFERENCES stat_entry (STAT_ENTRY_ID)
);
CREATE INDEX FK_stat_value_stat_value_id ON stat_value (stat_value_id);

CREATE TABLE sequence (
   seq_name varchar(50) PRIMARY KEY NOT NULL,
   seq_count decimal(38,0)
);

CREATE OR REPLACE FUNCTION updateStats() 
--RETURNS void 
RETURNS TABLE (
 id BIGINT,
 "timestamp" VARCHAR(255),
 attribut VARCHAR(50),
 value VARCHAR(255)    
) 
AS $$
DECLARE 
    id BIGINT;
BEGIN
	INSERT INTO
		stat_entry (entrydate) values (NOW());
		
	id := (select max(stat_entry_id) from stat_entry);

	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'pseudonyms',
		(SELECT count(pseudonym) FROM psn as psn_table where psn_table.domain!='internal_anonymisation_domain'));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'anonyms',
		(SELECT count(pseudonym) FROM psn as psn_table where psn_table.domain='internal_anonymisation_domain'));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'domains',
		(SELECT count(domain) FROM psn_projects where domain!='internal_anonymisation_domain'));

	--PERFORM t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value
    RETURN QUERY SELECT t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value
		FROM stat_entry AS t1, stat_value AS t2
		WHERE t1.stat_entry_id = t2.stat_value_id;
END;
$$ LANGUAGE plpgsql;

create user gpas_user PASSWORD 'gpas_2016';

grant all on DATABASE gpas to gpas_user;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO gpas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO gpas_user;

--grant all privileges on DATABASE gpas to gpas_user;
--grant all privileges on DATABASE gpas to gpas_user;