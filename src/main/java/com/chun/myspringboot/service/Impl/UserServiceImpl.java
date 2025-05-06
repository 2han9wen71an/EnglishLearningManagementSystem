package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.UserActivityMapper;
import com.chun.myspringboot.mapper.UserMapper;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UserActivityMapper userActivityMapper;


    @Override
    public User queryUserById(int userId) {
        return  userMapper.queryUserById(userId);
    }

    @Override
    public User queryUserByActiveCode(String activeCode) {
        return userMapper.queryUserByActiveCode(activeCode);
    }

    @Override
    public User loginByEmailAndPasswordAndActiveStatus(User user) {
        return userMapper.loginByEmailAndPasswordAndActiveStatus(user);
    }

    @Override
    public User AdminLogin(User user) {
        return userMapper.AdminLogin(user);
    }

    @Override
    public int addUser(User user) {
        return userMapper.addUser(user);
    }

    @Override
    public int deleteUser(Integer userId) {
        return userMapper.deleteUser(userId);
    }

    @Override
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }

    @Override
    public List<User> queryAllUser() {
        return userMapper.queryAllUser();
    }

    @Override
    public int queryAllUserCount() {
        // 获取所有用户数量
        List<User> users = userMapper.queryAllUser();
        return users != null ? users.size() : 0;
    }

    @Override
    public Map<String, Integer> getUserGrowthTrend(Date startDate, Date endDate) {
        // 这里应该从数据库中获取用户增长趋势数据
        // 由于没有现成的方法，这里使用模拟数据
        Map<String, Integer> growthData = new LinkedHashMap<>();

        // 日期格式化
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 计算日期范围
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // 生成日期范围内的每一天
        while (!calendar.getTime().after(endDate)) {
            String dateStr = dateFormat.format(calendar.getTime());
            // 这里应该查询数据库获取实际数据，暂时使用随机数模拟
            int userCount = 100 + new Random().nextInt(20); // 模拟数据
            growthData.put(dateStr, userCount);

            // 增加一天
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return growthData;
    }

    @Override
    public Map<String, Map<String, Integer>> getSystemActivityStats(Date startDate, Date endDate) {
        // 这里应该从数据库中获取系统活跃度数据
        // 由于没有现成的方法，这里使用模拟数据
        Map<String, Map<String, Integer>> activityData = new LinkedHashMap<>();

        // 日期格式化
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 计算日期范围
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // 生成日期范围内的每一天
        while (!calendar.getTime().after(endDate)) {
            String dateStr = dateFormat.format(calendar.getTime());

            // 创建活动类型和数量的映射
            Map<String, Integer> activities = new HashMap<>();
            activities.put("word_study", 10 + new Random().nextInt(20)); // 单词学习活动
            activities.put("listening", 5 + new Random().nextInt(10));   // 听力练习活动
            activities.put("reading", 3 + new Random().nextInt(8));      // 阅读活动
            activities.put("exam", 1 + new Random().nextInt(5));         // 考试活动

            activityData.put(dateStr, activities);

            // 增加一天
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return activityData;
    }
}
