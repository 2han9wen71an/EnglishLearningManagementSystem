-- 单词卡片表
CREATE TABLE IF NOT EXISTS `word_card` (
  `cardId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '卡片ID',
  `word` VARCHAR(100) NOT NULL COMMENT '单词',
  `explanation` TEXT COMMENT '解释',
  `imageUrl` VARCHAR(255) COMMENT '图片URL',
  `contextSentence` TEXT COMMENT '场景例句',
  `memoryTip` TEXT COMMENT '记忆口诀',
  `userId` INT COMMENT '用户ID',
  `createTime` DATETIME COMMENT '创建时间',
  FOREIGN KEY (`userId`) REFERENCES `user`(`userId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词卡片表';
