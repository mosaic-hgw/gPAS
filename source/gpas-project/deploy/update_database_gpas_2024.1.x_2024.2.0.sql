CREATE TABLE `mpsn` (
  `originalValue` varchar(255) NOT NULL,
  `pseudonym` varchar(255) NOT NULL,
  `domain` varchar(255) NOT NULL,
  PRIMARY KEY (`domain`,`originalValue`,`pseudonym`),
  UNIQUE KEY `domain_pseudonym` (`domain`,`pseudonym`),
  INDEX `domain_originalValue` (`domain`,`originalValue`),
  CONSTRAINT `FK_DOMAIN_MPSN` FOREIGN KEY (`domain`) REFERENCES `domain` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

DROP VIEW IF EXISTS `psn_domain_count`;
CREATE VIEW `psn_domain_count` AS
SELECT
    CONCAT('pseudonyms_per_domain.', `d`.`name`) AS `attribut`,
    (COUNT(`p`.`pseudonym`) + COUNT(`m`.`pseudonym`)) AS `value`
FROM
    `domain` `d`
        LEFT JOIN `psn` `p` ON `p`.`domain` = `d`.`name`
        LEFT JOIN `mpsn` `m` ON `m`.`domain` = `d`.`name`
WHERE
    `d`.`name` != 'internal_anonymisation_domain'
GROUP BY `d`.`name`;
