/*
 Navicat Premium Data Transfer

 Source Server         : poker-game-v2
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : 172.16.88.58:3306
 Source Schema         : poker_game

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 24/11/2022 16:37:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for item
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item`  (
  `userid` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户唯一主键',
  `name` enum('POKER_COUNTER','SUPER_DOUBLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '道具名称/类别',
  `count` int(0) NULL DEFAULT NULL COMMENT '道具数量',
  PRIMARY KEY (`userid`, `name`) USING BTREE,
  CONSTRAINT `item_clear` FOREIGN KEY (`userid`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户唯一主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatarUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户头像地址',
  `nickName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别',
  `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户所在国家',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户所在省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户所在城市',
  `language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户所用语言',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_data
-- ----------------------------
DROP TABLE IF EXISTS `user_data`;
CREATE TABLE `user_data`  (
  `userid` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '玩家id(主,外键)',
  `freeMoney` bigint(0) NULL DEFAULT 5000 COMMENT '游戏普通货币',
  `payMoney` bigint(0) NULL DEFAULT NULL COMMENT '游戏收费货币',
  `winCount` bigint(0) NULL DEFAULT NULL COMMENT '玩家普通游戏胜场',
  `loseCount` bigint(0) NULL DEFAULT NULL COMMENT '玩家普通游戏败场',
  `exp` bigint(0) NULL DEFAULT NULL COMMENT '玩家经验值',
  PRIMARY KEY (`userid`) USING BTREE,
  CONSTRAINT `data_clear` FOREIGN KEY (`userid`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Triggers structure for table user
-- ----------------------------
DROP TRIGGER IF EXISTS `init_item`;
delimiter ;;
CREATE TRIGGER `init_item` AFTER INSERT ON `user` FOR EACH ROW begin
insert into item values(new.id,'POKER_COUNTER',3);
insert into item values(new.id,'SUPER_DOUBLED',3);
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user
-- ----------------------------
DROP TRIGGER IF EXISTS `init_data`;
delimiter ;;
CREATE TRIGGER `init_data` AFTER INSERT ON `user` FOR EACH ROW begin
insert into user_data values(new.id,5000,0,0,0,0);
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
