-- 作文和语音评测相关表结构
-- 执行此SQL文件以创建必要的表

-- 作文表
CREATE TABLE IF NOT EXISTS `essay` (
  `essay_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '作文ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(255) NOT NULL COMMENT '作文标题',
  `content` TEXT NOT NULL COMMENT '作文内容',
  `submit_time` DATETIME NOT NULL COMMENT '提交时间',
  `status` INT DEFAULT 0 COMMENT '状态：0未批改，1已批改',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作文表';

-- 作文批改结果表
CREATE TABLE IF NOT EXISTS `essay_correction` (
  `correction_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '批改ID',
  `essay_id` INT NOT NULL COMMENT '作文ID',
  `corrected_content` TEXT NOT NULL COMMENT '批改后的内容',
  `grammar_errors` TEXT COMMENT '语法错误JSON',
  `vocabulary_suggestions` TEXT COMMENT '词汇建议JSON',
  `structure_comments` TEXT COMMENT '结构评价',
  `overall_score` INT COMMENT '总体评分(0-100)',
  `correction_time` DATETIME NOT NULL COMMENT '批改时间',
  FOREIGN KEY (`essay_id`) REFERENCES `essay`(`essay_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作文批改结果表';

-- 语音评测记录表
CREATE TABLE IF NOT EXISTS `pronunciation_assessment` (
  `assessment_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '评测ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '评测内容',
  `audio_data` TEXT COMMENT '音频数据(Base64)',
  `phoneme_errors` TEXT COMMENT '音素错误JSON',
  `overall_score` INT COMMENT '总体评分(0-100)',
  `assessment_time` DATETIME NOT NULL COMMENT '评测时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语音评测记录表';
