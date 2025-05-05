-- 用户单词关联表
CREATE TABLE IF NOT EXISTS `user_word` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
  `userId` INT NOT NULL COMMENT '用户ID',
  `wordId` INT NOT NULL COMMENT '单词ID',
  `study` INT DEFAULT 0 COMMENT '学习状态：0未学习，1已学习',
  `remember` INT DEFAULT 0 COMMENT '记忆状态：0未记住，1已记住',
  `collection` INT DEFAULT 0 COMMENT '收藏状态：0未收藏，1已收藏',
  `learnTime` DATETIME COMMENT '学习时间',
  FOREIGN KEY (`userId`) REFERENCES `user`(`userId`) ON DELETE CASCADE,
  FOREIGN KEY (`wordId`) REFERENCES `word`(`wordId`) ON DELETE CASCADE,
  UNIQUE KEY `user_word_unique` (`userId`, `wordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户单词关联表';

-- 初始化一些测试数据
INSERT INTO `user_word` (`userId`, `wordId`, `study`, `remember`, `collection`, `learnTime`) VALUES
(1, 1, 1, 1, 0, NOW()),
(1, 2, 1, 0, 1, NOW()),
(1, 3, 1, 0, 0, NOW()),
(2, 1, 1, 1, 1, NOW()),
(2, 4, 1, 0, 0, NOW());
