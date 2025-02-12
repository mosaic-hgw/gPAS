USE gpas;

-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server Version:               8.0.14 - MySQL Community Server - GPL
-- Server Betriebssystem:        Linux
-- HeidiSQL Version:             10.1.0.5464
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Exportiere Daten aus Tabelle gpas.domain: ~13 rows (ungefähr)
/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` (`name`, `label`, `alphabet`, `comment`, `generatorClass`, `properties`, `create_timestamp`, `update_timestamp`) VALUES
	('Biolabor', 'Biolabor', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Labor zur Verwaltung von Biomaterial', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=8;PSN_PREFIX=bio_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Herausgabe 1', 'Herausgabe 1', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Herausgabe fÃ¼r externes Forschungsprojekt 1', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=4;PSN_PREFIX=case1_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Herausgabe 2', 'Herausgabe 2', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Herausgabe fÃ¼r externes Forschungsprojekt 2', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=4;PSN_PREFIX=case2_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Herausgabe 3', 'Herausgabe 3', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Herausgabe fÃ¼r externes Forschungsprojekt 3', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=8;PSN_PREFIX=case3_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Herausgabe 4', 'Herausgabe 4', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Herausgabe fÃ¼r externes Forschungsprojekt 4', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=4;PSN_PREFIX=case4_;PSN_SUFFIX=;PSNS_DELETABLE=false;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Studie A', 'Studie A', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Demostudie A', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=8;PSN_PREFIX=sta_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Studie B', 'Studie B', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Demostudie B', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=8;PSN_PREFIX=stb_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Transferstelle A', 'Transferstelle A', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Transferstelle der Studie A zur Herausgabe medizinischer Daten', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=8;PSN_PREFIX=transa_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('Transferstelle B', 'Transferstelle B', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Transferstelle der Studie B zur Herausgabe medizinischer Daten', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=8;PSN_PREFIX=transb_;PSN_SUFFIX=;PSNS_DELETABLE=false;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('demo.study.demo', 'demo.study.demo', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Pseudonyme der Demostudie im Dispatcher.', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=10;PSN_PREFIX=demo_;PSN_SUFFIX=;PSNS_DELETABLE=false;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('demo.study.demo-mii', 'demo.study.demo-mii', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Pseudonyme der MII Demostudie im Dispatcher.', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=10;PSN_PREFIX=mii_;PSN_SUFFIX=;PSNS_DELETABLE=true;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('demo.system.mdat', 'demo.system.mdat', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers', 'Pseudonyme des Demosystems im Dispatcher.', 'org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=5;PSN_PREFIX=mdat_;PSN_SUFFIX=;PSNS_DELETABLE=false;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008'),
	('internal_anonymisation_domain', 'internal_anonymisation_domain', 'org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31', 'internal domain used to create unique anonyms', 'org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange', 'FORCE_CACHE=DEFAULT;INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION=false;INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION=false;MAX_DETECTED_ERRORS=2;PSN_LENGTH=10;PSN_PREFIX=###_anonym_###_;PSN_SUFFIX=_###_anonym_###;PSNS_DELETABLE=false;USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS=0;', '2021-10-26 10:39:54.979', '2021-10-26 10:39:55.008');
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;

-- Exportiere Daten aus Tabelle gpas.domain_parents: ~0 rows (ungefähr)
/*!40000 ALTER TABLE `domain_parents` DISABLE KEYS */;
INSERT INTO `domain_parents` (`domain`, `parentDomain`) VALUES
	('Biolabor', 'Studie A'),
	('Transferstelle A', 'Studie A'),
	('Biolabor', 'Studie B'),
	('Transferstelle B', 'Studie B'),
	('Herausgabe 1', 'Transferstelle A'),
	('Herausgabe 2', 'Transferstelle A'),
	('Herausgabe 3', 'Transferstelle B'),
	('Herausgabe 4', 'Transferstelle B'),
	('demo.system.mdat', 'demo.study.demo'),
	('demo.system.mdat', 'demo.study.demo-mii');
/*!40000 ALTER TABLE `domain_parents` ENABLE KEYS */;

-- Exportiere Daten aus Tabelle gpas.psn: ~0 rows (ungefähr)
/*!40000 ALTER TABLE `psn` DISABLE KEYS */;
INSERT INTO `psn` (`originalValue`, `pseudonym`, `domain`) VALUES
	('sta_022259430', 'bio_222504743', 'Biolabor'),
	('sta_745715367', 'bio_358822846', 'Biolabor'),
	('stb_930502041', 'bio_439224510', 'Biolabor'),
	('stb_193594411', 'bio_756189251', 'Biolabor'),
	('transa_398837099', 'case1_12165', 'Herausgabe 1'),
	('transa_682719299', 'case1_96459', 'Herausgabe 1'),
	('transa_398837099', 'case2_16348', 'Herausgabe 2'),
	('transb_742367979', 'case3_484881704', 'Herausgabe 3'),
	('transb_355882834', 'case4_15552', 'Herausgabe 4'),
	('transb_742367979', 'case4_60587', 'Herausgabe 4'),
	('0002', 'sta_022259430', 'Studie A'),
	('0001', 'sta_745715367', 'Studie A'),
	('0003', 'stb_193594411', 'Studie B'),
	('0001', 'stb_930502041', 'Studie B'),
	('sta_022259430', 'transa_398837099', 'Transferstelle A'),
	('sta_745715367', 'transa_682719299', 'Transferstelle A'),
	('stb_930502041', 'transb_355882834', 'Transferstelle B'),
	('stb_193594411', 'transb_742367979', 'Transferstelle B'),
    ('1001000000011', 'demo_56919218148', 'demo.study.demo'),
    ('demo_56919218148', 'mdat_969292', 'demo.system.mdat');

/*!40000 ALTER TABLE `psn` ENABLE KEYS */;

-- Exportiere Daten aus Tabelle gpas.sequence: ~1 rows (ungefähr)
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` (`SEQ_NAME`, `SEQ_COUNT`) VALUES
	('statistic_index', 50);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;

-- Exportiere Daten aus Tabelle gpas.stat_entry: ~0 rows (ungefähr)
/*!40000 ALTER TABLE `stat_entry` DISABLE KEYS */;
INSERT INTO `stat_entry` (`STAT_ENTRY_ID`, `ENTRYDATE`) VALUES
	(1, '2021-10-26 10:47:48.571');
/*!40000 ALTER TABLE `stat_entry` ENABLE KEYS */;

-- Exportiere Daten aus Tabelle gpas.stat_value: ~0 rows (ungefähr)
/*!40000 ALTER TABLE `stat_value` DISABLE KEYS */;
INSERT INTO `stat_value` (`stat_value_id`, `stat_value`, `stat_attr`) VALUES
	(1, 0, 'anonyms_per_domain.demo.system.mdat'),
	(1, 1, 'pseudonyms_per_domain.Herausgabe 2'),
	(1, 0, 'anonyms_per_domain.demo.study.demo-mii'),
	(1, 2, 'pseudonyms_per_domain.Herausgabe 1'),
	(1, 2, 'pseudonyms_per_domain.Herausgabe 4'),
	(1, 0, 'anonyms_per_domain.Biolabor'),
	(1, 1, 'pseudonyms_per_domain.Herausgabe 3'),
	(1, 0, 'anonyms_per_domain.demo.study.demo'),
	(1, 2, 'pseudonyms_per_domain.Studie A'),
	(1, 2, 'pseudonyms_per_domain.Transferstelle B'),
	(1, 18, 'pseudonyms'),
	(1, 2, 'pseudonyms_per_domain.Studie B'),
	(1, 2, 'pseudonyms_per_domain.Transferstelle A'),
	(1, 0, 'anonyms'),
	(1, 0, 'pseudonyms_per_domain.demo.study.demo-mii'),
	(1, 0, 'anonyms_per_domain.Studie B'),
	(1, 4, 'pseudonyms_per_domain.Biolabor'),
	(1, 0, 'anonyms_per_domain.Studie A'),
	(1, 0, 'calculation_time'),
	(1, 13, 'domains'),
	(1, 0, 'anonyms_per_domain.Transferstelle B'),
	(1, 0, 'anonyms_per_domain.Transferstelle A'),
	(1, 0, 'anonyms_per_domain.Herausgabe 1'),
	(1, 0, 'anonyms_per_domain.Herausgabe 2'),
	(1, 0, 'anonyms_per_domain.Herausgabe 3'),
	(1, 0, 'anonyms_per_domain.Herausgabe 4'),
	(1, 0, 'pseudonyms_per_domain.demo.system.mdat'),
	(1, 0, 'pseudonyms_per_domain.demo.study.demo');
/*!40000 ALTER TABLE `stat_value` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;