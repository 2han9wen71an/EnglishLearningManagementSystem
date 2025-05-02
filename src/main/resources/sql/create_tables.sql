-- 英语学习管理系统数据库建表语句
-- 使用MySQL数据库

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS english_learning_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE english_learning_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `userId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
  `userName` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `activeCode` VARCHAR(100) COMMENT '激活码',
  `activeStatus` INT DEFAULT 0 COMMENT '激活状态：0未激活，1已激活',
  `role` INT DEFAULT 0 COMMENT '角色：0普通用户，1管理员'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 等级表
CREATE TABLE IF NOT EXISTS `grade` (
  `gradeId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '等级ID',
  `gradeName` VARCHAR(50) NOT NULL COMMENT '等级名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等级表';

-- 单词表
CREATE TABLE IF NOT EXISTS `word` (
  `wordId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '单词ID',
  `wordName` VARCHAR(100) NOT NULL COMMENT '单词名称',
  `audio` VARCHAR(255) COMMENT '音频路径',
  `explanation` TEXT COMMENT '单词解释',
  `example` TEXT COMMENT '例句',
  `grade` INT COMMENT '等级ID',
  `study` INT DEFAULT 0 COMMENT '学习状态：0未学习，1已学习',
  `remember` INT DEFAULT 0 COMMENT '记忆状态：0未记住，1已记住',
  `collection` INT DEFAULT 0 COMMENT '收藏状态：0未收藏，1已收藏',
  FOREIGN KEY (`grade`) REFERENCES `grade`(`gradeId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词表';

-- 句子表
CREATE TABLE IF NOT EXISTS `sentence` (
  `sentenceId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '句子ID',
  `sentenceName` TEXT NOT NULL COMMENT '句子内容',
  `explain` TEXT COMMENT '句子解释'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='句子表';

-- 书籍表
CREATE TABLE IF NOT EXISTS `book` (
  `bookId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '书籍ID',
  `bookName` VARCHAR(100) NOT NULL COMMENT '书籍名称',
  `bookUser` VARCHAR(100) COMMENT '作者',
  `description` TEXT COMMENT '描述',
  `content` TEXT COMMENT '内容'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍表';

-- 听力表
CREATE TABLE IF NOT EXISTS `listen` (
  `listenId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '听力ID',
  `listenName` VARCHAR(100) NOT NULL COMMENT '听力名称',
  `grade` INT COMMENT '等级ID',
  `path` VARCHAR(255) COMMENT '音频路径',
  `content` TEXT COMMENT '听力内容',
  FOREIGN KEY (`grade`) REFERENCES `grade`(`gradeId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='听力表';

-- 公告表
CREATE TABLE IF NOT EXISTS `notice` (
  `noticeId` INT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
  `title` VARCHAR(100) NOT NULL COMMENT '公告标题',
  `content` TEXT COMMENT '公告内容',
  `creatTime` DATE COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 初始化等级数据
INSERT INTO `grade` (`gradeId`, `gradeName`) VALUES 
(1, '四级'),
(2, '六级'),
(3, '雅思'),
(4, '托福');

-- 初始化管理员账户
INSERT INTO `user` (`userName`, `password`, `email`, `activeStatus`, `role`) VALUES 
('admin', '123456', 'admin@example.com', 1, 1);
