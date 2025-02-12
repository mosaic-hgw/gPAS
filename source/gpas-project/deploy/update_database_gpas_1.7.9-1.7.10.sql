CREATE TABLE sequence(SEQ_NAME varchar(50) PRIMARY KEY NOT NULL, SEQ_COUNT decimal(38,0));
insert into sequence(seq_name, seq_count) values
("statistic_index", (select ifnull((select max(stat_entry_id) from stat_entry), 0)));
