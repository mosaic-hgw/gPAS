-- Fix default for stat_entry in gpas installations before version 1.11
ALTER TABLE stat_entry MODIFY COLUMN ENTRYDATE timestamp(3) DEFAULT CURRENT_TIMESTAMP(3)  NOT NULL; 