-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: hy_compliance_db
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `fishing_ban_rule`
--

DROP TABLE IF EXISTS `fishing_ban_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fishing_ban_rule` (
  `rule_id` int NOT NULL AUTO_INCREMENT COMMENT '规则ID，自增主键',
  `sea_id` int NOT NULL COMMENT '关联海域ID，对应sea_area.sea_id',
  `species_id` int NOT NULL COMMENT '关联物种ID，对应species.species_id',
  `ban_start_time` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '禁渔开始时间，如：5月1日、6月15日',
  `ban_end_time` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '禁渔结束时间，如：9月16日、8月31日',
  `spec_require` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '规格要求，如：网目规格≥10cm、单尾重量≥500g',
  `punish_desc` varchar(300) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '违规处罚说明，如：没收渔获，罚款2000-5000元',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_sea_species` (`sea_id`,`species_id`),
  KEY `species_id` (`species_id`),
  CONSTRAINT `fishing_ban_rule_ibfk_1` FOREIGN KEY (`sea_id`) REFERENCES `sea_area` (`sea_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fishing_ban_rule_ibfk_2` FOREIGN KEY (`species_id`) REFERENCES `species` (`species_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='禁渔规则表-关联海域和物种，存储禁渔时间、规格要求';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fishing_ban_rule`
--

LOCK TABLES `fishing_ban_rule` WRITE;
/*!40000 ALTER TABLE `fishing_ban_rule` DISABLE KEYS */;
INSERT INTO `fishing_ban_rule` VALUES (1,3,1,'5月1日','9月16日','网目规格≥10cm，禁止密眼网捕捞','没收渔获及渔具，罚款2000-5000元','2026-02-01 14:31:02','2026-02-01 14:31:02');
/*!40000 ALTER TABLE `fishing_ban_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fishing_gear`
--

DROP TABLE IF EXISTS `fishing_gear`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fishing_gear` (
  `gear_id` int NOT NULL AUTO_INCREMENT COMMENT '渔具ID（主键自增）',
  `gear_name` varchar(50) NOT NULL COMMENT '渔具名称',
  `is_allow` tinyint NOT NULL COMMENT '是否允许（1=允许，0=禁用）',
  `remark` varchar(200) DEFAULT '' COMMENT '备注/规格要求',
  `punish_desc` varchar(200) DEFAULT '' COMMENT '违规处罚',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（自动填充）',
  PRIMARY KEY (`gear_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渔具合规表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fishing_gear`
--

LOCK TABLES `fishing_gear` WRITE;
/*!40000 ALTER TABLE `fishing_gear` DISABLE KEYS */;
INSERT INTO `fishing_gear` VALUES (1,'单层刺网',1,'网目≥50mm，近海非禁渔期允许','网目不符处2000-5000元罚款','2026-02-04 14:17:34'),(2,'底拖网',0,'全国近海全年禁止使用','违规没收渔具，处5000-20000元罚款','2026-02-04 14:17:34'),(3,'手钓竿',1,'无规格限制，所有海域允许','无违规处罚','2026-02-04 14:17:34'),(4,'围网',0,'禁渔期内禁止使用，需提前备案','禁渔期使用处3000-10000元罚款','2026-02-04 14:17:34');
/*!40000 ALTER TABLE `fishing_gear` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fishing_log`
--

DROP TABLE IF EXISTS `fishing_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fishing_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID，自增主键（用BIGINT避免数据量大时主键溢出）',
  `user_id` int NOT NULL COMMENT '关联用户ID，对应user.user_id',
  `sea_id` int NOT NULL COMMENT '关联海域ID，对应sea_area.sea_id',
  `fishing_date` date NOT NULL COMMENT '捕捞日期，格式：yyyy-MM-dd',
  `fishing_gear` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '渔具类型，如：拖网、刺网、钓竿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_compliant` tinyint(1) DEFAULT '1' COMMENT '合规状态：1=合规，0=违规',
  `uncompliant_reason` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '无' COMMENT '违规原因汇总',
  PRIMARY KEY (`log_id`),
  KEY `sea_id` (`sea_id`),
  KEY `idx_user_date` (`user_id`,`fishing_date`),
  CONSTRAINT `fishing_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fishing_log_ibfk_2` FOREIGN KEY (`sea_id`) REFERENCES `sea_area` (`sea_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='捕捞日志主表-存储基础捕捞信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fishing_log`
--

LOCK TABLES `fishing_log` WRITE;
/*!40000 ALTER TABLE `fishing_log` DISABLE KEYS */;
INSERT INTO `fishing_log` VALUES (1,2,3,'2026-02-07','拖网','2026-02-07 15:51:16','2026-02-07 15:51:16',1,'无'),(2,2,3,'2026-02-10','拖网','2026-02-10 13:45:57','2026-02-10 13:45:57',0,'该物种为禁捕物种；'),(4,2,3,'2025-05-02','拖网','2026-02-10 13:56:28','2026-02-10 13:56:28',0,'该物种为禁捕物种；'),(5,2,3,'2025-09-01','拖网','2026-02-10 13:57:12','2026-02-10 13:57:12',0,'当前海域该物种处于禁渔期（5月1日-9月16日）；');
/*!40000 ALTER TABLE `fishing_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fishing_log_species`
--

DROP TABLE IF EXISTS `fishing_log_species`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fishing_log_species` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID，自增主键',
  `log_id` bigint NOT NULL COMMENT '关联捕捞日志ID，对应fishing_log.log_id',
  `species_id` int NOT NULL COMMENT '关联物种ID，对应species.species_id',
  `catch_num` int NOT NULL DEFAULT '0' COMMENT '捕捞数量（尾/斤）',
  `catch_spec` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '捕捞规格，如：单尾10cm、平均500g',
  `is_compliant` tinyint NOT NULL COMMENT '本物种捕捞是否合规：1=合规，0=违规',
  `uncompliant_reason` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '违规原因，如：当前海域处于禁渔期、捕捞规格不达标',
  PRIMARY KEY (`detail_id`),
  UNIQUE KEY `uk_log_species` (`log_id`,`species_id`),
  KEY `species_id` (`species_id`),
  CONSTRAINT `fishing_log_species_ibfk_1` FOREIGN KEY (`log_id`) REFERENCES `fishing_log` (`log_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fishing_log_species_ibfk_2` FOREIGN KEY (`species_id`) REFERENCES `species` (`species_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='捕捞日志物种明细表-处理日志与物种的一对多关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fishing_log_species`
--

LOCK TABLES `fishing_log_species` WRITE;
/*!40000 ALTER TABLE `fishing_log_species` DISABLE KEYS */;
INSERT INTO `fishing_log_species` VALUES (1,1,3,2,'15cm',0,'该物种为禁捕物种；'),(2,2,3,2,'15cm',0,'该物种为禁捕物种；'),(4,4,3,1,'19cm',0,'该物种为禁捕物种；'),(5,5,1,1,'19cm',0,'当前海域该物种处于禁渔期（5月1日-9月16日）；');
/*!40000 ALTER TABLE `fishing_log_species` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fishing_policy`
--

DROP TABLE IF EXISTS `fishing_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fishing_policy` (
  `policy_id` int NOT NULL AUTO_INCREMENT COMMENT '政策ID（主键自增）',
  `policy_title` varchar(100) NOT NULL COMMENT '政策标题',
  `publish_unit` varchar(50) NOT NULL COMMENT '发布单位',
  `publish_time` date NOT NULL COMMENT '发布时间（仅日期）',
  `official_content` text NOT NULL COMMENT '官方条文（长文本）',
  `simple_content` text NOT NULL COMMENT '口语化解读（长文本）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（自动填充）',
  PRIMARY KEY (`policy_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渔业政策表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fishing_policy`
--

LOCK TABLES `fishing_policy` WRITE;
/*!40000 ALTER TABLE `fishing_policy` DISABLE KEYS */;
INSERT INTO `fishing_policy` VALUES (1,'2026年近海禁渔期管理规定','农业农村部渔业局','2026-01-20','全国近海禁渔期为5月1日至9月16日，期间禁止除休闲垂钓外的所有捕捞作业，违规捕捞没收渔获物及渔具，并处2-5倍罚款。','5-9月近海只能钓鱼，不能用网/船捕鱼，违规要没收渔具还罚款','2026-02-04 14:17:45'),(2,'海洋渔具合规技术规范2026','国家渔业技术推广总站','2026-02-01','所有刺网类渔具网目尺寸不得小于50mm，拖网、炸鱼、毒鱼等方式全面禁止，违规使用不合格渔具责令整改，逾期不改处2000-20000元罚款。','渔网孔不能小于50mm，拖网/炸鱼不能弄，用不合格渔网先整改，不改就罚款','2026-02-04 14:17:45');
/*!40000 ALTER TABLE `fishing_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sea_area`
--

DROP TABLE IF EXISTS `sea_area`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sea_area` (
  `sea_id` int NOT NULL AUTO_INCREMENT COMMENT '海域ID，自增主键',
  `sea_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '海域名称，如：东海、黄海、浙江省舟山东海片区',
  `parent_sea_id` int DEFAULT '0' COMMENT '上级海域ID，0表示顶级海域（如四大海），关联本桌sea_id',
  `province` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属省份，如：浙江省、山东省、福建省',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sea_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='海域表-存储海域层级、所属省份信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sea_area`
--

LOCK TABLES `sea_area` WRITE;
/*!40000 ALTER TABLE `sea_area` DISABLE KEYS */;
INSERT INTO `sea_area` VALUES (1,'渤海',0,'河北省','2026-02-01 14:29:06','2026-02-01 14:29:06'),(2,'黄海',0,'山东省','2026-02-01 14:29:06','2026-02-01 14:29:06'),(3,'东海',0,'浙江省','2026-02-01 14:29:06','2026-02-01 14:29:06'),(4,'南海',0,'广东省','2026-02-01 14:29:06','2026-02-01 14:29:06');
/*!40000 ALTER TABLE `sea_area` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `species`
--

DROP TABLE IF EXISTS `species`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `species` (
  `species_id` int NOT NULL AUTO_INCREMENT COMMENT '物种ID，自增主键',
  `species_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物种名称，如：带鱼、中华鲟、大黄鱼',
  `protect_level` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '保护级别，如：国家一级、国家二级、无保护',
  `is_catch` tinyint NOT NULL COMMENT '是否可捕：1=可捕，0=禁止捕捞',
  `recognize_feature` text COLLATE utf8mb4_general_ci COMMENT '物种识别特征，如：纺锤形、蓝黑背、吻部尖长',
  `emergency_guide` text COLLATE utf8mb4_general_ci COMMENT '误捕应急指引，如：立即原地放生并联系当地渔政',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`species_id`),
  UNIQUE KEY `species_name` (`species_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物种表-存储保护级别、识别特征、应急指引';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `species`
--

LOCK TABLES `species` WRITE;
/*!40000 ALTER TABLE `species` DISABLE KEYS */;
INSERT INTO `species` VALUES (1,'带鱼','无保护',1,'身体呈带状，侧扁，银灰色，头尖口大','无特殊指引','2026-02-01 14:30:44','2026-02-01 14:30:44'),(2,'大黄鱼','国家二级',0,'体侧扁，金黄色，口裂大，尾柄细长','立即原地放生，联系当地渔政部门','2026-02-01 14:30:44','2026-02-01 14:30:44'),(3,'中华鲟','国家一级',0,'纺锤形，吻部尖长，背部青灰色，腹面白色','严禁触碰，立即放生并上报渔政，违者追究刑事责任','2026-02-01 14:30:44','2026-02-01 14:30:44');
/*!40000 ALTER TABLE `species` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID，自增主键',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号，唯一',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码（生产需用MD5/SHA256加密，此处存密文）',
  `real_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '真实姓名，渔民/管理员姓名',
  `role` tinyint NOT NULL COMMENT '用户角色：1=渔民，2=管理员',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '联系电话',
  `is_enable` tinyint DEFAULT '1' COMMENT '是否启用：1=启用，0=禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表-区分渔民/管理员，存储账号信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'yumin001','123456','徐露凝',1,'13248049936',1,'2026-02-01 14:30:23','2026-02-01 14:30:23'),(2,'admin','123456','系统管理员',2,'13900139000',1,'2026-02-01 14:30:23','2026-02-01 14:30:23');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-23 21:26:53
