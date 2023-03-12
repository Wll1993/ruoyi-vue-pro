/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1 MySQL
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : ruoyi-vue-pro

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 07/02/2023 22:00:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for report_go_view_project
-- ----------------------------
DROP TABLE IF EXISTS `report_go_view_project`;
CREATE TABLE `report_go_view_project`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `pic_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览图片 URL',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '报表内容',
  `status` tinyint NOT NULL COMMENT '发布状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '项目备注',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'GoView 项目表';

-- ----------------------------
-- Records of report_go_view_project
-- ----------------------------
BEGIN;
INSERT INTO `report_go_view_project` (`id`, `name`, `pic_url`, `content`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (6, 'HTTP示例', 'http://test.yudao.iocoder.cn/7c0cb26b09cfa97cae0b3e5d19b210978eed0ab184dd0bff1b66b3daf70a01fc.png', '{\n  \"editCanvasConfig\": {\n    \"projectName\": \"HTTP示例\",\n    \"width\": 1920,\n    \"height\": 1080,\n    \"filterShow\": false,\n    \"hueRotate\": 0,\n    \"saturate\": 1,\n    \"contrast\": 1,\n    \"brightness\": 1,\n    \"opacity\": 1,\n    \"rotateZ\": 0,\n    \"rotateX\": 0,\n    \"rotateY\": 0,\n    \"skewX\": 0,\n    \"skewY\": 0,\n    \"blendMode\": \"normal\",\n    \"background\": null,\n    \"backgroundImage\": null,\n    \"selectColor\": true,\n    \"chartThemeColor\": \"dark\",\n    \"chartThemeSetting\": {\n      \"title\": {\n        \"show\": true,\n        \"textStyle\": {\n          \"color\": \"#BFBFBF\",\n          \"fontSize\": 18\n        },\n        \"subtextStyle\": {\n          \"color\": \"#A2A2A2\",\n          \"fontSize\": 14\n        }\n      },\n      \"xAxis\": {\n        \"show\": true,\n        \"name\": \"\",\n        \"nameGap\": 15,\n        \"nameTextStyle\": {\n          \"color\": \"#B9B8CE\",\n          \"fontSize\": 12\n        },\n        \"inverse\": false,\n        \"axisLabel\": {\n          \"show\": true,\n          \"fontSize\": 12,\n          \"color\": \"#B9B8CE\",\n          \"rotate\": 0\n        },\n        \"position\": \"bottom\",\n        \"axisLine\": {\n          \"show\": true,\n          \"lineStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"width\": 1\n          },\n          \"onZero\": true\n        },\n        \"axisTick\": {\n          \"show\": true,\n          \"length\": 5\n        },\n        \"splitLine\": {\n          \"show\": false,\n          \"lineStyle\": {\n            \"color\": \"#484753\",\n            \"width\": 1,\n            \"type\": \"solid\"\n          }\n        }\n      },\n      \"yAxis\": {\n        \"show\": true,\n        \"name\": \"\",\n        \"nameGap\": 15,\n        \"nameTextStyle\": {\n          \"color\": \"#B9B8CE\",\n          \"fontSize\": 12\n        },\n        \"inverse\": false,\n        \"axisLabel\": {\n          \"show\": true,\n          \"fontSize\": 12,\n          \"color\": \"#B9B8CE\",\n          \"rotate\": 0\n        },\n        \"position\": \"left\",\n        \"axisLine\": {\n          \"show\": true,\n          \"lineStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"width\": 1\n          },\n          \"onZero\": true\n        },\n        \"axisTick\": {\n          \"show\": true,\n          \"length\": 5\n        },\n        \"splitLine\": {\n          \"show\": true,\n          \"lineStyle\": {\n            \"color\": \"#484753\",\n            \"width\": 1,\n            \"type\": \"solid\"\n          }\n        }\n      },\n      \"legend\": {\n        \"show\": true,\n        \"top\": \"5%\",\n        \"textStyle\": {\n          \"color\": \"#B9B8CE\"\n        }\n      },\n      \"grid\": {\n        \"show\": false,\n        \"left\": \"10%\",\n        \"top\": \"60\",\n        \"right\": \"10%\",\n        \"bottom\": \"60\"\n      },\n      \"dataset\": null\n    },\n    \"previewScaleType\": \"fit\"\n  },\n  \"componentList\": [\n    {\n      \"id\": \"4vxgys8nh6g000\",\n      \"isGroup\": false,\n      \"attr\": {\n        \"x\": 7,\n        \"y\": 11,\n        \"w\": 1898,\n        \"h\": 1066,\n        \"offsetX\": 0,\n        \"offsetY\": 0,\n        \"zIndex\": -1\n      },\n      \"styles\": {\n        \"filterShow\": false,\n        \"hueRotate\": 0,\n        \"saturate\": 1,\n        \"contrast\": 1,\n        \"brightness\": 1,\n        \"opacity\": 1,\n        \"rotateZ\": 0,\n        \"rotateX\": 0,\n        \"rotateY\": 0,\n        \"skewX\": 0,\n        \"skewY\": 0,\n        \"blendMode\": \"normal\",\n        \"animations\": []\n      },\n      \"status\": {\n        \"lock\": false,\n        \"hide\": false\n      },\n      \"request\": {\n        \"requestDataType\": 1,\n        \"requestHttpType\": \"get\",\n        \"requestUrl\": \"http://127.0.0.1:48080/admin-api/report/go-view/data/get-by-http\",\n        \"requestInterval\": null,\n        \"requestIntervalUnit\": \"second\",\n        \"requestContentType\": 0,\n        \"requestParamsBodyType\": \"none\",\n        \"requestSQLContent\": {\n          \"sql\": \"select * from  where\"\n        },\n        \"requestParams\": {\n          \"Body\": {\n            \"form-data\": {},\n            \"x-www-form-urlencoded\": {},\n            \"json\": \"\",\n            \"xml\": \"\"\n          },\n          \"Header\": {},\n          \"Params\": {}\n        }\n      },\n      \"filter\": null,\n      \"events\": {\n        \"baseEvent\": {\n          \"click\": null,\n          \"dblclick\": null,\n          \"mouseenter\": null,\n          \"mouseleave\": null\n        },\n        \"advancedEvents\": {\n          \"vnodeMounted\": null,\n          \"vnodeBeforeMount\": null\n        }\n      },\n      \"key\": \"LineCommon\",\n      \"chartConfig\": {\n        \"key\": \"LineCommon\",\n        \"chartKey\": \"VLineCommon\",\n        \"conKey\": \"VCLineCommon\",\n        \"title\": \"折线图\",\n        \"category\": \"Lines\",\n        \"categoryName\": \"折线图\",\n        \"package\": \"Charts\",\n        \"chartFrame\": \"echarts\",\n        \"image\": \"line.png\"\n      },\n      \"option\": {\n        \"legend\": {\n          \"show\": true,\n          \"top\": \"5%\",\n          \"textStyle\": {\n            \"color\": \"#B9B8CE\"\n          }\n        },\n        \"xAxis\": {\n          \"show\": true,\n          \"name\": \"\",\n          \"nameGap\": 15,\n          \"nameTextStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"fontSize\": 12\n          },\n          \"inverse\": false,\n          \"axisLabel\": {\n            \"show\": true,\n            \"fontSize\": 12,\n            \"color\": \"#B9B8CE\",\n            \"rotate\": 0\n          },\n          \"position\": \"bottom\",\n          \"axisLine\": {\n            \"show\": true,\n            \"lineStyle\": {\n              \"color\": \"#B9B8CE\",\n              \"width\": 1\n            },\n            \"onZero\": true\n          },\n          \"axisTick\": {\n            \"show\": true,\n            \"length\": 5\n          },\n          \"splitLine\": {\n            \"show\": false,\n            \"lineStyle\": {\n              \"color\": \"#484753\",\n              \"width\": 1,\n              \"type\": \"solid\"\n            }\n          },\n          \"type\": \"category\"\n        },\n        \"yAxis\": {\n          \"show\": true,\n          \"name\": \"\",\n          \"nameGap\": 15,\n          \"nameTextStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"fontSize\": 12\n          },\n          \"inverse\": false,\n          \"axisLabel\": {\n            \"show\": true,\n            \"fontSize\": 12,\n            \"color\": \"#B9B8CE\",\n            \"rotate\": 0\n          },\n          \"position\": \"left\",\n          \"axisLine\": {\n            \"show\": true,\n            \"lineStyle\": {\n              \"color\": \"#B9B8CE\",\n              \"width\": 1\n            },\n            \"onZero\": true\n          },\n          \"axisTick\": {\n            \"show\": true,\n            \"length\": 5\n          },\n          \"splitLine\": {\n            \"show\": true,\n            \"lineStyle\": {\n              \"color\": \"#484753\",\n              \"width\": 1,\n              \"type\": \"solid\"\n            }\n          },\n          \"type\": \"value\"\n        },\n        \"grid\": {\n          \"show\": false,\n          \"left\": \"10%\",\n          \"top\": \"60\",\n          \"right\": \"10%\",\n          \"bottom\": \"60\"\n        },\n        \"tooltip\": {\n          \"show\": true,\n          \"trigger\": \"axis\",\n          \"axisPointer\": {\n            \"type\": \"line\"\n          }\n        },\n        \"dataset\": {\n          \"dimensions\": [\n            \"日期\",\n            \"PV\",\n            \"UV\"\n          ],\n          \"source\": [\n            {\n              \"UV\": 518,\n              \"日期\": \"2021-01\",\n              \"PV\": 7954\n            },\n            {\n              \"UV\": 135,\n              \"日期\": \"2021-02\",\n              \"PV\": 9402\n            },\n            {\n              \"UV\": 905,\n              \"日期\": \"2021-03\",\n              \"PV\": 1665\n            },\n            {\n              \"UV\": 157,\n              \"日期\": \"2021-04\",\n              \"PV\": 2633\n            },\n            {\n              \"UV\": 849,\n              \"日期\": \"2021-05\",\n              \"PV\": 7650\n            },\n            {\n              \"UV\": 563,\n              \"日期\": \"2021-06\",\n              \"PV\": 2399\n            },\n            {\n              \"UV\": 427,\n              \"日期\": \"2021-07\",\n              \"PV\": 9952\n            },\n            {\n              \"UV\": 158,\n              \"日期\": \"2021-08\",\n              \"PV\": 9232\n            },\n            {\n              \"UV\": 894,\n              \"日期\": \"2021-09\",\n              \"PV\": 3013\n            },\n            {\n              \"UV\": 343,\n              \"日期\": \"2021-10\",\n              \"PV\": 6181\n            },\n            {\n              \"UV\": 294,\n              \"日期\": \"2021-11\",\n              \"PV\": 8949\n            },\n            {\n              \"UV\": 452,\n              \"日期\": \"2021-12\",\n              \"PV\": 8730\n            }\n          ]\n        },\n        \"series\": [\n          {\n            \"type\": \"line\",\n            \"label\": {\n              \"show\": true,\n              \"position\": \"top\",\n              \"color\": \"#fff\",\n              \"fontSize\": 12\n            },\n            \"symbolSize\": 5,\n            \"itemStyle\": {\n              \"color\": null,\n              \"borderRadius\": 0\n            },\n            \"lineStyle\": {\n              \"type\": \"solid\",\n              \"width\": 3,\n              \"color\": null\n            }\n          },\n          {\n            \"type\": \"line\",\n            \"label\": {\n              \"show\": true,\n              \"position\": \"top\",\n              \"color\": \"#fff\",\n              \"fontSize\": 12\n            },\n            \"symbolSize\": 5,\n            \"itemStyle\": {\n              \"color\": null,\n              \"borderRadius\": 0\n            },\n            \"lineStyle\": {\n              \"type\": \"solid\",\n              \"width\": 3,\n              \"color\": null\n            }\n          }\n        ],\n        \"backgroundColor\": \"rgba(0,0,0,0)\"\n      }\n    }\n  ],\n  \"requestGlobalConfig\": {\n    \"requestDataPond\": [],\n    \"requestOriginUrl\": \"\",\n    \"requestInterval\": 30,\n    \"requestIntervalUnit\": \"second\",\n    \"requestParams\": {\n      \"Body\": {\n        \"form-data\": {},\n        \"x-www-form-urlencoded\": {},\n        \"json\": \"\",\n        \"xml\": \"\"\n      },\n      \"Header\": {},\n      \"Params\": {}\n    }\n  }\n}', 0, NULL, '1', '2023-02-07 11:38:22', '1', '2023-02-07 17:27:43', b'0', 1), (7, 'SQL示例', 'http://test.yudao.iocoder.cn/c1f570bad6ec7e7fa4a0a7c8f563da4ea158fde6e731da4dd1abe8ba9b6baeed.png', '{\n  \"editCanvasConfig\": {\n    \"projectName\": \"SQL示例\",\n    \"width\": 1920,\n    \"height\": 1080,\n    \"filterShow\": false,\n    \"hueRotate\": 0,\n    \"saturate\": 1,\n    \"contrast\": 1,\n    \"brightness\": 1,\n    \"opacity\": 1,\n    \"rotateZ\": 0,\n    \"rotateX\": 0,\n    \"rotateY\": 0,\n    \"skewX\": 0,\n    \"skewY\": 0,\n    \"blendMode\": \"normal\",\n    \"background\": null,\n    \"backgroundImage\": null,\n    \"selectColor\": true,\n    \"chartThemeColor\": \"dark\",\n    \"chartThemeSetting\": {\n      \"title\": {\n        \"show\": true,\n        \"textStyle\": {\n          \"color\": \"#BFBFBF\",\n          \"fontSize\": 18\n        },\n        \"subtextStyle\": {\n          \"color\": \"#A2A2A2\",\n          \"fontSize\": 14\n        }\n      },\n      \"xAxis\": {\n        \"show\": true,\n        \"name\": \"\",\n        \"nameGap\": 15,\n        \"nameTextStyle\": {\n          \"color\": \"#B9B8CE\",\n          \"fontSize\": 12\n        },\n        \"inverse\": false,\n        \"axisLabel\": {\n          \"show\": true,\n          \"fontSize\": 12,\n          \"color\": \"#B9B8CE\",\n          \"rotate\": 0\n        },\n        \"position\": \"bottom\",\n        \"axisLine\": {\n          \"show\": true,\n          \"lineStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"width\": 1\n          },\n          \"onZero\": true\n        },\n        \"axisTick\": {\n          \"show\": true,\n          \"length\": 5\n        },\n        \"splitLine\": {\n          \"show\": false,\n          \"lineStyle\": {\n            \"color\": \"#484753\",\n            \"width\": 1,\n            \"type\": \"solid\"\n          }\n        }\n      },\n      \"yAxis\": {\n        \"show\": true,\n        \"name\": \"\",\n        \"nameGap\": 15,\n        \"nameTextStyle\": {\n          \"color\": \"#B9B8CE\",\n          \"fontSize\": 12\n        },\n        \"inverse\": false,\n        \"axisLabel\": {\n          \"show\": true,\n          \"fontSize\": 12,\n          \"color\": \"#B9B8CE\",\n          \"rotate\": 0\n        },\n        \"position\": \"left\",\n        \"axisLine\": {\n          \"show\": true,\n          \"lineStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"width\": 1\n          },\n          \"onZero\": true\n        },\n        \"axisTick\": {\n          \"show\": true,\n          \"length\": 5\n        },\n        \"splitLine\": {\n          \"show\": true,\n          \"lineStyle\": {\n            \"color\": \"#484753\",\n            \"width\": 1,\n            \"type\": \"solid\"\n          }\n        }\n      },\n      \"legend\": {\n        \"show\": true,\n        \"top\": \"5%\",\n        \"textStyle\": {\n          \"color\": \"#B9B8CE\"\n        }\n      },\n      \"grid\": {\n        \"show\": false,\n        \"left\": \"10%\",\n        \"top\": \"60\",\n        \"right\": \"10%\",\n        \"bottom\": \"60\"\n      },\n      \"dataset\": null\n    },\n    \"previewScaleType\": \"fit\"\n  },\n  \"componentList\": [\n    {\n      \"id\": \"5cqrghfle9g000\",\n      \"isGroup\": false,\n      \"attr\": {\n        \"x\": 27,\n        \"y\": 3,\n        \"w\": 1870,\n        \"h\": 1051,\n        \"offsetX\": 0,\n        \"offsetY\": 0,\n        \"zIndex\": -1\n      },\n      \"styles\": {\n        \"filterShow\": false,\n        \"hueRotate\": 0,\n        \"saturate\": 1,\n        \"contrast\": 1,\n        \"brightness\": 1,\n        \"opacity\": 1,\n        \"rotateZ\": 0,\n        \"rotateX\": 0,\n        \"rotateY\": 0,\n        \"skewX\": 0,\n        \"skewY\": 0,\n        \"blendMode\": \"normal\",\n        \"animations\": [\n          \"headShake\"\n        ]\n      },\n      \"status\": {\n        \"lock\": false,\n        \"hide\": false\n      },\n      \"request\": {\n        \"requestDataType\": 1,\n        \"requestHttpType\": \"post\",\n        \"requestUrl\": \"http://127.0.0.1:48080/admin-api/report/go-view/data/get-by-sql\",\n        \"requestInterval\": null,\n        \"requestIntervalUnit\": \"second\",\n        \"requestContentType\": 0,\n        \"requestParamsBodyType\": \"none\",\n        \"requestSQLContent\": {\n          \"sql\": \"SELECT DATE_FORMAT(create_time, \\\"%Y-%m\\\") AS time, COUNT(*) AS \'次数\', COUNT(DISTINCT(user_id)) AS \'人数\' FROM system_login_log GROUP BY DATE_FORMAT(create_time, \\\"%Y-%m\\\")\"\n        },\n        \"requestParams\": {\n          \"Body\": {\n            \"form-data\": {},\n            \"x-www-form-urlencoded\": {},\n            \"json\": \"\",\n            \"xml\": \"\"\n          },\n          \"Header\": {},\n          \"Params\": {}\n        }\n      },\n      \"filter\": null,\n      \"events\": {\n        \"baseEvent\": {\n          \"click\": null,\n          \"dblclick\": null,\n          \"mouseenter\": null,\n          \"mouseleave\": null\n        },\n        \"advancedEvents\": {\n          \"vnodeMounted\": null,\n          \"vnodeBeforeMount\": null\n        }\n      },\n      \"key\": \"LineCommon\",\n      \"chartConfig\": {\n        \"key\": \"LineCommon\",\n        \"chartKey\": \"VLineCommon\",\n        \"conKey\": \"VCLineCommon\",\n        \"title\": \"折线图\",\n        \"category\": \"Lines\",\n        \"categoryName\": \"折线图\",\n        \"package\": \"Charts\",\n        \"chartFrame\": \"echarts\",\n        \"image\": \"line.png\"\n      },\n      \"option\": {\n        \"legend\": {\n          \"show\": true,\n          \"top\": \"5%\",\n          \"textStyle\": {\n            \"color\": \"#B9B8CE\"\n          }\n        },\n        \"xAxis\": {\n          \"show\": true,\n          \"name\": \"\",\n          \"nameGap\": 15,\n          \"nameTextStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"fontSize\": 12\n          },\n          \"inverse\": false,\n          \"axisLabel\": {\n            \"show\": true,\n            \"fontSize\": 12,\n            \"color\": \"#B9B8CE\",\n            \"rotate\": 0\n          },\n          \"position\": \"bottom\",\n          \"axisLine\": {\n            \"show\": true,\n            \"lineStyle\": {\n              \"color\": \"#B9B8CE\",\n              \"width\": 1\n            },\n            \"onZero\": true\n          },\n          \"axisTick\": {\n            \"show\": true,\n            \"length\": 5\n          },\n          \"splitLine\": {\n            \"show\": false,\n            \"lineStyle\": {\n              \"color\": \"#484753\",\n              \"width\": 1,\n              \"type\": \"solid\"\n            }\n          },\n          \"type\": \"category\"\n        },\n        \"yAxis\": {\n          \"show\": true,\n          \"name\": \"\",\n          \"nameGap\": 15,\n          \"nameTextStyle\": {\n            \"color\": \"#B9B8CE\",\n            \"fontSize\": 12\n          },\n          \"inverse\": false,\n          \"axisLabel\": {\n            \"show\": true,\n            \"fontSize\": 12,\n            \"color\": \"#B9B8CE\",\n            \"rotate\": 0\n          },\n          \"position\": \"left\",\n          \"axisLine\": {\n            \"show\": true,\n            \"lineStyle\": {\n              \"color\": \"#B9B8CE\",\n              \"width\": 1\n            },\n            \"onZero\": true\n          },\n          \"axisTick\": {\n            \"show\": true,\n            \"length\": 5\n          },\n          \"splitLine\": {\n            \"show\": true,\n            \"lineStyle\": {\n              \"color\": \"#484753\",\n              \"width\": 1,\n              \"type\": \"solid\"\n            }\n          },\n          \"type\": \"value\"\n        },\n        \"grid\": {\n          \"show\": false,\n          \"left\": \"10%\",\n          \"top\": \"60\",\n          \"right\": \"10%\",\n          \"bottom\": \"60\"\n        },\n        \"tooltip\": {\n          \"show\": true,\n          \"trigger\": \"axis\",\n          \"axisPointer\": {\n            \"type\": \"line\"\n          }\n        },\n        \"dataset\": {\n          \"dimensions\": [\n            \"time\",\n            \"次数\",\n            \"人数\"\n          ],\n          \"source\": [\n            {\n              \"次数\": 94,\n              \"time\": \"2022-05\",\n              \"人数\": 5\n            },\n            {\n              \"次数\": 120,\n              \"time\": \"2022-06\",\n              \"人数\": 2\n            },\n            {\n              \"次数\": 118,\n              \"time\": \"2022-07\",\n              \"人数\": 5\n            },\n            {\n              \"次数\": 37,\n              \"time\": \"2022-08\",\n              \"人数\": 2\n            },\n            {\n              \"次数\": 65,\n              \"time\": \"2022-09\",\n              \"人数\": 2\n            },\n            {\n              \"次数\": 35,\n              \"time\": \"2022-10\",\n              \"人数\": 2\n            },\n            {\n              \"次数\": 86,\n              \"time\": \"2022-11\",\n              \"人数\": 1\n            },\n            {\n              \"次数\": 49,\n              \"time\": \"2022-12\",\n              \"人数\": 3\n            },\n            {\n              \"次数\": 45,\n              \"time\": \"2023-01\",\n              \"人数\": 1\n            },\n            {\n              \"次数\": 70,\n              \"time\": \"2023-02\",\n              \"人数\": 1\n            }\n          ]\n        },\n        \"series\": [\n          {\n            \"type\": \"line\",\n            \"label\": {\n              \"show\": true,\n              \"position\": \"top\",\n              \"color\": \"#fff\",\n              \"fontSize\": 12\n            },\n            \"symbolSize\": 5,\n            \"itemStyle\": {\n              \"color\": null,\n              \"borderRadius\": 0\n            },\n            \"lineStyle\": {\n              \"type\": \"solid\",\n              \"width\": 3,\n              \"color\": null\n            }\n          },\n          {\n            \"type\": \"line\",\n            \"label\": {\n              \"show\": true,\n              \"position\": \"top\",\n              \"color\": \"#fff\",\n              \"fontSize\": 12\n            },\n            \"symbolSize\": 5,\n            \"itemStyle\": {\n              \"color\": null,\n              \"borderRadius\": 0\n            },\n            \"lineStyle\": {\n              \"type\": \"solid\",\n              \"width\": 3,\n              \"color\": null\n            }\n          }\n        ],\n        \"backgroundColor\": \"rgba(0,0,0,0)\"\n      }\n    }\n  ],\n  \"requestGlobalConfig\": {\n    \"requestDataPond\": [],\n    \"requestOriginUrl\": \"\",\n    \"requestInterval\": 30,\n    \"requestIntervalUnit\": \"second\",\n    \"requestParams\": {\n      \"Body\": {\n        \"form-data\": {},\n        \"x-www-form-urlencoded\": {},\n        \"json\": \"\",\n        \"xml\": \"\"\n      },\n      \"Header\": {},\n      \"Params\": {}\n    }\n  }\n}', 0, NULL, '1', '2023-02-07 11:43:57', '1', '2023-02-07 17:27:40', b'0', 1), (8, '57q2gor533g000', NULL, NULL, 1, NULL, '1', '2023-02-07 19:27:09', '1', '2023-02-07 19:27:09', b'0', 1);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
