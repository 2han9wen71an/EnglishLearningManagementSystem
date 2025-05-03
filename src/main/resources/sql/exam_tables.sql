-- 考试相关表结构
-- 执行此SQL文件以创建必要的表

-- 考试表
CREATE TABLE IF NOT EXISTS `exam` (
  `exam_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '考试ID',
  `title` VARCHAR(255) NOT NULL COMMENT '考试标题',
  `description` TEXT COMMENT '考试描述',
  `duration` INT NOT NULL COMMENT '考试时长(分钟)',
  `total_score` INT NOT NULL COMMENT '总分',
  `pass_score` INT NOT NULL COMMENT '及格分数',
  `grade_id` INT COMMENT '关联的等级ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `status` INT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  FOREIGN KEY (`grade_id`) REFERENCES `grade`(`gradeId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 考试题目表
CREATE TABLE IF NOT EXISTS `exam_question` (
  `question_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
  `exam_id` INT NOT NULL COMMENT '所属考试ID',
  `question_type` INT NOT NULL COMMENT '题目类型：1单选题，2多选题，3判断题，4填空题，5简答题',
  `question_content` TEXT NOT NULL COMMENT '题目内容',
  `options` TEXT COMMENT '选项(JSON格式)',
  `correct_answer` TEXT NOT NULL COMMENT '正确答案',
  `score` INT NOT NULL COMMENT '分值',
  `analysis` TEXT COMMENT '解析',
  `order_num` INT COMMENT '题目顺序',
  FOREIGN KEY (`exam_id`) REFERENCES `exam`(`exam_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试题目表';

-- 考试记录表
CREATE TABLE IF NOT EXISTS `exam_record` (
  `record_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `exam_id` INT NOT NULL COMMENT '考试ID',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `score` INT COMMENT '得分',
  `status` INT DEFAULT 0 COMMENT '状态：0进行中，1已完成，2已批改',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`userId`) ON DELETE CASCADE,
  FOREIGN KEY (`exam_id`) REFERENCES `exam`(`exam_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 答题记录表
CREATE TABLE IF NOT EXISTS `exam_answer` (
  `answer_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '答题ID',
  `record_id` INT NOT NULL COMMENT '考试记录ID',
  `question_id` INT NOT NULL COMMENT '题目ID',
  `user_answer` TEXT COMMENT '用户答案',
  `score` INT COMMENT '得分',
  `is_correct` INT DEFAULT 0 COMMENT '是否正确：0错误，1正确',
  `scoring_status` INT DEFAULT 0 COMMENT '评分状态：0自动评分，1待人工评分，2已人工评分，3AI评分待确认',
  `ai_score_explanation` TEXT COMMENT 'AI评分解释',
  FOREIGN KEY (`record_id`) REFERENCES `exam_record`(`record_id`) ON DELETE CASCADE,
  FOREIGN KEY (`question_id`) REFERENCES `exam_question`(`question_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- 初始化示例考试数据
INSERT INTO `exam` (`title`, `description`, `duration`, `total_score`, `pass_score`, `grade_id`, `create_time`, `status`) VALUES
('英语四级词汇测试', '测试你的英语四级词汇掌握程度', 30, 100, 60, 1, NOW(), 1),
('英语六级词汇测试', '测试你的英语六级词汇掌握程度', 30, 100, 60, 2, NOW(), 1);

-- 初始化示例题目数据 (四级考试)
INSERT INTO `exam_question` (`exam_id`, `question_type`, `question_content`, `options`, `correct_answer`, `score`, `analysis`, `order_num`) VALUES
(1, 1, 'What is the meaning of "abandon"?', '{"A":"放弃，抛弃","B":"能力，才能","C":"绝对的，完全的","D":"学术的，教学的"}', 'A', 5, '"abandon" 的意思是"放弃，抛弃"', 1),
(1, 1, 'What is the meaning of "ability"?', '{"A":"放弃，抛弃","B":"能力，才能","C":"绝对的，完全的","D":"学术的，教学的"}', 'B', 5, '"ability" 的意思是"能力，才能"', 2),
(1, 1, 'What is the meaning of "abroad"?', '{"A":"放弃，抛弃","B":"能力，才能","C":"在国外","D":"学术的，教学的"}', 'C', 5, '"abroad" 的意思是"在国外"', 3),
(1, 1, 'What is the meaning of "absolute"?', '{"A":"放弃，抛弃","B":"能力，才能","C":"在国外","D":"绝对的，完全的"}', 'D', 5, '"absolute" 的意思是"绝对的，完全的"', 4),
(1, 1, 'What is the meaning of "academic"?', '{"A":"放弃，抛弃","B":"能力，才能","C":"在国外","D":"学术的，教学的"}', 'D', 5, '"academic" 的意思是"学术的，教学的"', 5),
(1, 5, 'Write a short paragraph about your favorite English word and explain why you like it.', NULL, 'Answers will vary. Students should write a coherent paragraph explaining their favorite English word and the reasons for their preference.', 25, 'This is a subjective question. Evaluation should focus on language use, coherence, and the quality of explanation.', 6);

-- 初始化示例题目数据 (六级考试)
INSERT INTO `exam_question` (`exam_id`, `question_type`, `question_content`, `options`, `correct_answer`, `score`, `analysis`, `order_num`) VALUES
(2, 1, 'What is the meaning of "abolish"?', '{"A":"废除，废止","B":"附件，配件","C":"容纳，提供住宿","D":"责任，义务"}', 'A', 5, '"abolish" 的意思是"废除，废止"', 1),
(2, 1, 'What is the meaning of "accessory"?', '{"A":"废除，废止","B":"附件，配件","C":"容纳，提供住宿","D":"责任，义务"}', 'B', 5, '"accessory" 的意思是"附件，配件"', 2),
(2, 1, 'What is the meaning of "accommodate"?', '{"A":"废除，废止","B":"附件，配件","C":"容纳，提供住宿","D":"责任，义务"}', 'C', 5, '"accommodate" 的意思是"容纳，提供住宿"', 3),
(2, 1, 'What is the meaning of "accountability"?', '{"A":"废除，废止","B":"附件，配件","C":"容纳，提供住宿","D":"责任，义务"}', 'D', 5, '"accountability" 的意思是"责任，义务"', 4),
(2, 1, 'What is the meaning of "accumulate"?', '{"A":"废除，废止","B":"附件，配件","C":"容纳，提供住宿","D":"积累，积聚"}', 'D', 5, '"accumulate" 的意思是"积累，积聚"', 5),
(2, 5, 'Write a short essay discussing the importance of English in today\'s global society.', NULL, 'Answers will vary. Students should write a coherent essay discussing the role and importance of English in global communication, business, education, etc.', 25, 'This is a subjective question. Evaluation should focus on language use, coherence, argument quality, and examples provided.', 6);

-- 更新现有记录的评分状态（如果有数据）
-- 将所有简答题设置为待人工评分
UPDATE `exam_answer` a
JOIN `exam_question` q ON a.question_id = q.question_id
SET a.scoring_status = 1
WHERE q.question_type = 5;

-- 将其他题型设置为自动评分
UPDATE `exam_answer` a
JOIN `exam_question` q ON a.question_id = q.question_id
SET a.scoring_status = 0
WHERE q.question_type != 5;
