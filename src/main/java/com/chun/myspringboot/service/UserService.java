package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserService {
    //根据Id查询用户
    User queryUserById(int userId);

    //根据激活码查询用户
     User queryUserByActiveCode(String activeCode);
    //根据邮箱密码与状态码登录
    User loginByEmailAndPasswordAndActiveStatus(User user);
    //根据邮箱密码与Role=1登录
    User AdminLogin(User user);

    //增加用户
    int addUser(User user);
    //删除用户
    int deleteUser(Integer userId);
    // 修改用户
    int updateUser(User user);
    //查询用户
    List<User> queryAllUser();

    //获取用户总数
    int queryAllUserCount();

    //获取用户增长趋势
    Map<String, Integer> getUserGrowthTrend(Date startDate, Date endDate);

    //获取系统活跃度统计
    Map<String, Map<String, Integer>> getSystemActivityStats(Date startDate, Date endDate);
}
