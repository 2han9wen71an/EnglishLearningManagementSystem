-- 情景对话场景表
CREATE TABLE IF NOT EXISTS `dialogue_scenario` (
  `scenario_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '场景ID',
  `scenario_name` VARCHAR(100) NOT NULL COMMENT '场景名称',
  `scenario_description` TEXT COMMENT '场景描述',
  `scenario_image` VARCHAR(255) COMMENT '场景图片路径',
  `difficulty_level` INT DEFAULT 1 COMMENT '难度级别：1初级，2中级，3高级'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情景对话场景表';

-- 对话内容表
CREATE TABLE IF NOT EXISTS `dialogue_content` (
  `content_id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '内容ID',
  `scenario_id` INT NOT NULL COMMENT '场景ID',
  `role` VARCHAR(50) NOT NULL COMMENT '角色（AI或用户）',
  `content` TEXT NOT NULL COMMENT '对话内容',
  `prompt` TEXT COMMENT 'AI提示内容',
  `order_num` INT NOT NULL COMMENT '对话顺序',
  FOREIGN KEY (`scenario_id`) REFERENCES `dialogue_scenario`(`scenario_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话内容表';

-- 初始化场景数据
INSERT INTO `dialogue_scenario` (`scenario_name`, `scenario_description`, `difficulty_level`) VALUES
('机场值机', '模拟在机场值机柜台办理登机手续的对话场景，包括出示护照、选择座位、托运行李等环节。', 1),
('酒店预订', '模拟与酒店前台预订房间的对话，包括询问房型、价格、设施以及办理入住手续等环节。', 1),
('餐厅点餐', '模拟在西式餐厅点餐的对话，包括询问菜单、推荐菜品、点餐以及结账等环节。', 2),
('购物对话', '模拟在商店购物的对话，包括询问商品、价格、尺寸、颜色以及付款等环节。', 2),
('医院就诊', '模拟在医院就诊的对话，包括描述症状、回答医生问题、了解治疗方案等环节。', 3);

-- 初始化对话内容数据（机场值机场景示例）
INSERT INTO `dialogue_content` (`scenario_id`, `role`, `content`, `prompt`, `order_num`) VALUES
(1, 'AI', 'Good morning! Welcome to the check-in counter. May I see your passport and flight details, please?', '机场工作人员向您问好并要求查看护照和航班信息', 1),
(1, 'USER', '', '请出示您的护照和航班信息', 2),
(1, 'AI', 'Thank you. I can see you\'re flying to London today. Would you like a window or an aisle seat?', '工作人员确认您的航班并询问座位偏好', 3),
(1, 'USER', '', '回答您的座位偏好（靠窗或靠过道）', 4),
(1, 'AI', 'I\'ve assigned you a [seat_type] seat. Do you have any luggage to check in today?', '工作人员为您安排了座位并询问是否有托运行李', 5),
(1, 'USER', '', '告诉工作人员您的行李情况', 6),
(1, 'AI', 'Your luggage has been checked in. Here\'s your boarding pass. Your flight departs from Gate 12 at 10:30 AM. Boarding will begin at 9:45 AM. Have a pleasant flight!', '工作人员完成托运并提供登机牌和登机信息', 7);

-- 初始化对话内容数据（酒店预订场景示例）
INSERT INTO `dialogue_content` (`scenario_id`, `role`, `content`, `prompt`, `order_num`) VALUES
(2, 'AI', 'Good afternoon and welcome to Grand Hotel. How may I help you today?', '酒店前台工作人员向您问好并询问需要什么帮助', 1),
(2, 'USER', '', '告诉前台您想预订房间', 2),
(2, 'AI', 'I\'d be happy to help you with a reservation. What dates are you looking to stay with us, and how many guests will there be?', '前台询问您的入住日期和人数', 3),
(2, 'USER', '', '提供您的入住日期和人数信息', 4),
(2, 'AI', 'We have several room types available for those dates. Would you prefer a standard room, deluxe room, or a suite?', '前台介绍可用房型并询问您的偏好', 5),
(2, 'USER', '', '告诉前台您想要的房型', 6),
(2, 'AI', 'Excellent choice. The [room_type] is $[price] per night and includes breakfast and access to our fitness center. Would you like to proceed with this reservation?', '前台确认房型和价格，并询问是否确认预订', 7),
(2, 'USER', '', '确认预订并询问其他细节', 8),
(2, 'AI', 'Perfect! I\'ve booked a [room_type] for you from [check_in_date] to [check_out_date]. We\'ll need a credit card to secure the reservation. Check-in time is 3:00 PM, and check-out is at noon. Is there anything else you\'d like to know?', '前台完成预订并提供入住和退房信息', 9);
