-- 情景对话模拟功能相关表结构
-- 执行此SQL文件以创建必要的表和初始数据

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

-- 初始化对话内容数据（餐厅点餐场景示例）
INSERT INTO `dialogue_content` (`scenario_id`, `role`, `content`, `prompt`, `order_num`) VALUES
(3, 'AI', 'Good evening and welcome to our restaurant. My name is Alex, and I\'ll be your server tonight. Would you like to start with something to drink?', '服务员向您问好并询问是否需要饮料', 1),
(3, 'USER', '', '告诉服务员您想要点的饮料', 2),
(3, 'AI', 'Great choice. I\'ll get that for you right away. Have you had a chance to look at our menu? Today\'s special is grilled salmon with roasted vegetables.', '服务员确认饮料并介绍今日特餐', 3),
(3, 'USER', '', '询问菜单或推荐菜品', 4),
(3, 'AI', 'I would recommend our signature pasta dish or the steak. The pasta comes with a creamy sauce and fresh seafood, while the steak is served with mashed potatoes and seasonal vegetables. Both are very popular with our guests.', '服务员推荐菜品并描述', 5),
(3, 'USER', '', '选择您想要点的主菜', 6),
(3, 'AI', 'Excellent choice. How would you like your [meat/dish] cooked? And would you like to add a side salad or soup?', '服务员询问烹饪偏好和配菜', 7),
(3, 'USER', '', '告诉服务员您的烹饪偏好和是否需要配菜', 8),
(3, 'AI', 'Thank you for your order. I\'ll bring your drinks shortly, and your meal should be ready in about 20 minutes. Please let me know if you need anything else.', '服务员确认订单并告知等待时间', 9);

-- 初始化对话内容数据（购物对话场景示例）
INSERT INTO `dialogue_content` (`scenario_id`, `role`, `content`, `prompt`, `order_num`) VALUES
(4, 'AI', 'Hello there! Welcome to our store. Is there something specific you\'re looking for today?', '店员向您问好并询问是否需要帮助', 1),
(4, 'USER', '', '告诉店员您想要购买的商品', 2),
(4, 'AI', 'We do have a great selection of [product]. They\'re right over here. Do you have a specific style or color in mind?', '店员带您到商品区并询问偏好', 3),
(4, 'USER', '', '描述您想要的款式或颜色', 4),
(4, 'AI', 'Here are some options that match what you\'re looking for. This one is particularly popular, and we also have this new arrival. Would you like to try any of these?', '店员展示商品并询问是否需要试用', 5),
(4, 'USER', '', '告诉店员您想试用哪件商品', 6),
(4, 'AI', 'How does it feel? We also have it in different sizes if this one doesn\'t fit perfectly. The price is $[price], and it\'s currently on a 20% discount if you\'re interested.', '店员询问试用感受并提供价格信息', 7),
(4, 'USER', '', '告诉店员您的决定（购买或继续看看）', 8),
(4, 'AI', 'Great decision! I\'ll take this to the counter for you. Will you be paying by cash or card today?', '店员确认购买并询问支付方式', 9);

-- 初始化对话内容数据（医院就诊场景示例）
INSERT INTO `dialogue_content` (`scenario_id`, `role`, `content`, `prompt`, `order_num`) VALUES
(5, 'AI', 'Good morning. I\'m Dr. Johnson. What brings you to the clinic today?', '医生向您问好并询问就诊原因', 1),
(5, 'USER', '', '描述您的症状和不适', 2),
(5, 'AI', 'I see. How long have you been experiencing these symptoms? And have you noticed anything that makes them better or worse?', '医生询问症状持续时间和影响因素', 3),
(5, 'USER', '', '告诉医生症状持续时间和可能的影响因素', 4),
(5, 'AI', 'Thank you for that information. I\'d like to check your vital signs and do a brief examination. Based on what you\'ve told me, it sounds like you might have [condition]. Let me confirm with a few tests.', '医生进行初步诊断并提出检查建议', 5),
(5, 'USER', '', '询问医生关于检查或诊断的更多信息', 6),
(5, 'AI', 'The tests will help us rule out other possibilities and confirm the diagnosis. It\'s a simple procedure and shouldn\'t take long. After that, we can discuss treatment options, which might include medication and some lifestyle changes.', '医生解释检查过程和可能的治疗方案', 7),
(5, 'USER', '', '询问治疗方案的详细信息或可能的副作用', 8),
(5, 'AI', 'The medication I\'m considering has few side effects, mainly mild drowsiness in some patients. As for lifestyle changes, I recommend [specific advice]. Do you have any other questions or concerns about the treatment plan?', '医生详细解释治疗方案和注意事项', 9);
