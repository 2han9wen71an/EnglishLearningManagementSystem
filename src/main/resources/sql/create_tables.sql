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
('admin', '123456', 'admin@example.com', 1, 1),
('user1', '123456', 'user1@example.com', 1, 0),
('user2', '123456', 'user2@example.com', 1, 0);

-- 初始化公告数据
INSERT INTO `notice` (`title`, `content`, `creatTime`) VALUES
('欢迎使用英语学习管理系统', '<p>欢迎使用英语学习管理系统！本系统提供单词学习、听力练习和阅读材料等功能，帮助您提高英语水平。</p><p>如有任何问题，请联系管理员。</p>', '2025-05-01'),
('系统更新通知', '<p>尊敬的用户：</p><p>我们将于2025年5月10日凌晨2:00-4:00进行系统维护，期间系统可能无法正常访问。给您带来的不便，敬请谅解。</p>', '2025-05-02'),
('新功能上线通知', '<p>我们新增了以下功能：</p><ol><li>单词发音功能</li><li>学习进度统计</li><li>个性化学习计划</li></ol><p>欢迎体验！</p>', '2025-05-03');

-- 初始化单词数据 (四级)
INSERT INTO `word` (`wordName`, `audio`, `explanation`, `example`, `grade`, `study`, `remember`, `collection`) VALUES
('abandon', '/əˈbændən/', 'v. 放弃，抛弃', 'He abandoned his family and moved to another city.', 1, 0, 0, 0),
('ability', '/əˈbɪləti/', 'n. 能力，才能', 'She has the ability to learn languages quickly.', 1, 0, 0, 0),
('abroad', '/əˈbrɔːd/', 'adv. 在国外', 'He went abroad to study English.', 1, 0, 0, 0),
('absolute', '/ˈæbsəluːt/', 'adj. 绝对的，完全的', 'I have absolute confidence in his ability.', 1, 0, 0, 0),
('academic', '/ˌækəˈdemɪk/', 'adj. 学术的，教学的', 'His academic achievements are impressive.', 1, 0, 0, 0);

-- 初始化单词数据 (六级)
INSERT INTO `word` (`wordName`, `audio`, `explanation`, `example`, `grade`, `study`, `remember`, `collection`) VALUES
('abolish', '/əˈbɒlɪʃ/', 'v. 废除，废止', 'The government abolished the law.', 2, 0, 0, 0),
('accessory', '/əkˈsesəri/', 'n. 附件，配件', 'She bought some accessories for her new phone.', 2, 0, 0, 0),
('accommodate', '/əˈkɒmədeɪt/', 'v. 容纳，提供住宿', 'The hotel can accommodate up to 500 guests.', 2, 0, 0, 0),
('accountability', '/əˌkaʊntəˈbɪləti/', 'n. 责任，义务', 'There must be accountability for these mistakes.', 2, 0, 0, 0),
('accumulate', '/əˈkjuːmjəleɪt/', 'v. 积累，积聚', 'He accumulated a lot of wealth over the years.', 2, 0, 0, 0);

-- 初始化单词数据 (雅思)
INSERT INTO `word` (`wordName`, `audio`, `explanation`, `example`, `grade`, `study`, `remember`, `collection`) VALUES
('aesthetic', '/iːsˈθetɪk/', 'adj. 审美的，美学的', 'The building has great aesthetic appeal.', 3, 0, 0, 0),
('affinity', '/əˈfɪnəti/', 'n. 亲和力，吸引力', 'She has a natural affinity for languages.', 3, 0, 0, 0),
('aggregate', '/ˈæɡrɪɡət/', 'v. 合计，总计', 'The company\'s aggregate sales exceeded expectations.', 3, 0, 0, 0),
('alleviate', '/əˈliːvieɪt/', 'v. 减轻，缓解', 'The medicine should alleviate the pain.', 3, 0, 0, 0),
('ambiguous', '/æmˈbɪɡjuəs/', 'adj. 模糊的，不明确的', 'His answer was ambiguous and confusing.', 3, 0, 0, 0);

-- 初始化单词数据 (托福)
INSERT INTO `word` (`wordName`, `audio`, `explanation`, `example`, `grade`, `study`, `remember`, `collection`) VALUES
('anomaly', '/əˈnɒməli/', 'n. 异常，反常', 'The data showed an anomaly that needed investigation.', 4, 0, 0, 0),
('anticipate', '/ænˈtɪsɪpeɪt/', 'v. 预期，预料', 'We anticipate strong growth in the next quarter.', 4, 0, 0, 0),
('arbitrary', '/ˈɑːbɪtrəri/', 'adj. 任意的，武断的', 'The decision seemed arbitrary and unfair.', 4, 0, 0, 0),
('articulate', '/ɑːˈtɪkjuleɪt/', 'v. 清晰地表达', 'She can articulate her ideas very well.', 4, 0, 0, 0),
('assimilate', '/əˈsɪmɪleɪt/', 'v. 吸收，同化', 'It takes time to assimilate new information.', 4, 0, 0, 0);

-- 初始化句子数据
INSERT INTO `sentence` (`sentenceName`, `explain`) VALUES
('The best preparation for tomorrow is doing your best today.', '对明天最好的准备就是今天做到最好。'),
('Life is like riding a bicycle. To keep your balance, you must keep moving.', '生活就像骑自行车，要保持平衡就得不断前进。'),
('Success is not final, failure is not fatal: It is the courage to continue that counts.', '成功不是终点，失败也不是致命的：重要的是继续前进的勇气。'),
('The only limit to our realization of tomorrow will be our doubts of today.', '实现明天理想的唯一障碍是今天的疑虑。'),
('Education is the most powerful weapon which you can use to change the world.', '教育是你可以用来改变世界的最强大的武器。'),
('The future belongs to those who believe in the beauty of their dreams.', '未来属于那些相信梦想之美的人。'),
('It does not matter how slowly you go as long as you do not stop.', '慢慢走没关系，只要你不停下来。'),
('Believe you can and you\'re halfway there.', '相信你能做到，你就已经成功了一半。'),
('Your time is limited, don\'t waste it living someone else\'s life.', '你的时间有限，不要浪费在过别人的生活上。'),
('The only way to do great work is to love what you do.', '做出伟大工作的唯一方法是热爱你所做的事。');

-- 初始化书籍数据
INSERT INTO `book` (`bookName`, `bookUser`, `description`, `content`) VALUES
('英语阅读精选', '张教授', '适合英语学习者的精选阅读材料', '<h2>Chapter 1: The Importance of Reading</h2><p>Reading is one of the most effective ways to improve your English skills. It exposes you to new vocabulary, grammar structures, and ideas.</p><p>Regular reading can help you become more fluent and confident in English.</p>'),
('商务英语指南', '李老师', '针对商务场景的英语学习材料', '<h2>Introduction to Business English</h2><p>Business English focuses on the language used in business contexts, including meetings, negotiations, presentations, and written communication.</p><p>This guide will help you develop the specific language skills needed for success in international business.</p>'),
('英语写作技巧', '王教授', '提高英语写作能力的实用指南', '<h2>Basic Writing Principles</h2><p>Good writing starts with clear thinking. Before you begin writing, organize your thoughts and create an outline.</p><p>Remember that effective writing is concise, coherent, and purposeful.</p>'),
('英语口语练习', '刘老师', '日常英语口语练习材料', '<h2>Daily Conversations</h2><p>This book contains common conversations that occur in everyday situations, such as shopping, dining out, traveling, and socializing.</p><p>Practice these dialogues to improve your speaking fluency and confidence.</p>');

-- 初始化听力数据
INSERT INTO `listen` (`listenName`, `grade`, `path`, `content`) VALUES
('四级听力: 2018年12月第1题', 1, '/cet4-201812-1.mp3', '<p>这是2018年12月四级考试的第一道听力题目。</p><p>听力内容包括一段对话，考察考生对日常对话的理解能力。</p><p>请仔细聆听，注意把握对话中的关键信息。</p>'),
('四级听力: 2018年12月第2题', 1, '/cet4-201812-2.mp3', '<p>这是2018年12月四级考试的第二道听力题目。</p><p>听力内容包括一段短文，考察考生对短文主旨和细节的理解能力。</p><p>请集中注意力，把握文章的主要内容和关键细节。</p>'),
('四级听力: 2018年12月第3题', 1, '/cet4-201812-3.mp3', '<p>这是2018年12月四级考试的第三道听力题目。</p><p>听力内容包括一段讲座，考察考生对学术性内容的理解能力。</p><p>请注意讲座的主题、论点和支持论据。</p>'),
('日常英语听力练习', 2, '/cet4-201812-1.mp3', '<p>这是一段日常英语对话，内容涉及日常生活场景。</p><p>通过练习这类听力材料，可以提高您在实际生活中的英语交流能力。</p><p>建议多次听取，直到能完全理解对话内容。</p>');
