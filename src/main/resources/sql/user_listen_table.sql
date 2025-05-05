-- 用户听力练习关联表
CREATE TABLE IF NOT EXISTS `user_listen` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
  `userId` INT NOT NULL COMMENT '用户ID',
  `listenId` INT NOT NULL COMMENT '听力ID',
  `completed` INT DEFAULT 0 COMMENT '完成状态：0未完成，1已完成',
  `completionTime` DATETIME COMMENT '完成时间',
  FOREIGN KEY (`userId`) REFERENCES `user`(`userId`) ON DELETE CASCADE,
  FOREIGN KEY (`listenId`) REFERENCES `listen`(`listenId`) ON DELETE CASCADE,
  UNIQUE KEY `user_listen_unique` (`userId`, `listenId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户听力练习关联表';

-- 初始化一些测试数据
INSERT INTO `user_listen` (`userId`, `listenId`, `completed`, `completionTime`) VALUES
(1, 1, 1, NOW()),
(1, 2, 1, NOW()),
(2, 1, 1, NOW()),
(2, 3, 0, NULL);
