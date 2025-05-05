-- 用户活动表
CREATE TABLE IF NOT EXISTS `user_activity` (
  `activity_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `activity_type` VARCHAR(50) NOT NULL COMMENT '活动类型：word_study, exam, listening, essay, reading, pronunciation',
  `activity_content` VARCHAR(255) NOT NULL COMMENT '活动内容描述',
  `related_id` INT COMMENT '相关记录ID',
  `activity_time` DATETIME NOT NULL COMMENT '活动时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活动表';

-- 初始化一些测试数据
INSERT INTO `user_activity` (`user_id`, `activity_type`, `activity_content`, `related_id`, `activity_time`) VALUES
(1, 'word_study', '完成了单词学习任务', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'exam', '参加了英语四级模拟考试', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'essay', '提交了一篇作文《My Hometown》', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'listening', '完成了听力练习', 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'word_study', '学习了20个新单词', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'reading', '完成了阅读练习', 1, DATE_SUB(NOW(), INTERVAL 2 DAY));
