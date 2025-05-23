package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.Listen;

import java.util.List;

public interface ListenService {
    //根据ID查询一条听力的信息
    Listen queryListenById(Integer listenId);
    //增加一条听力
    int addListen(Listen listen);
    //删除一条听力
    int deleteListen(Integer listenId);
    //修改一条听力
    int updateListen(Listen listen);
    //查看所有听力所有信息
    List<Listen> queryAllListen();

    /**
     * 获取用户已完成的听力练习数量
     * @param userId 用户ID
     * @return 已完成的听力练习数量
     */
    int queryUserCompletedListeningCount(Integer userId);

    /**
     * 获取总听力练习数量
     * @return 总听力练习数量
     */
    int queryTotalListeningCount();
}
