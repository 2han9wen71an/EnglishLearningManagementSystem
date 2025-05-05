package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.ListenMapper;
import com.chun.myspringboot.mapper.UserListenMapper;
import com.chun.myspringboot.pojo.Listen;
import com.chun.myspringboot.pojo.UserListen;
import com.chun.myspringboot.service.ActivityRecordService;
import com.chun.myspringboot.service.UserListenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户听力练习关联服务实现类
 */
@Service
public class UserListenServiceImpl implements UserListenService {

    @Autowired
    private UserListenMapper userListenMapper;

    @Autowired
    private ListenMapper listenMapper;

    @Autowired
    private ActivityRecordService activityRecordService;

    @Override
    public int addUserListen(UserListen userListen) {
        int result = userListenMapper.addUserListen(userListen);

        // 如果添加成功且是完成状态，记录活动
        if (result > 0 && userListen.getCompleted() != null && userListen.getCompleted() == 1) {
            try {
                // 获取听力信息
                Listen listen = listenMapper.queryListenById(userListen.getListenId());
                if (listen != null) {
                    // 记录听力练习活动
                    activityRecordService.recordListeningActivity(
                            userListen.getUserId(),
                            userListen.getListenId(),
                            listen.getListenName()
                    );
                }
            } catch (Exception e) {
                // 记录活动失败不影响主要业务
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public int deleteUserListen(Integer id) {
        return userListenMapper.deleteUserListen(id);
    }

    @Override
    public int updateUserListen(UserListen userListen) {
        UserListen oldUserListen = userListenMapper.queryUserListenById(userListen.getId());
        int result = userListenMapper.updateUserListen(userListen);

        // 如果更新成功且完成状态从0变为1，记录活动
        if (result > 0 && oldUserListen != null &&
            (oldUserListen.getCompleted() == null || oldUserListen.getCompleted() == 0) &&
            userListen.getCompleted() != null && userListen.getCompleted() == 1) {
            try {
                // 获取听力信息
                Listen listen = listenMapper.queryListenById(userListen.getListenId());
                if (listen != null) {
                    // 记录听力练习活动
                    activityRecordService.recordListeningActivity(
                            userListen.getUserId(),
                            userListen.getListenId(),
                            listen.getListenName()
                    );
                }
            } catch (Exception e) {
                // 记录活动失败不影响主要业务
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public UserListen queryUserListenById(Integer id) {
        return userListenMapper.queryUserListenById(id);
    }

    @Override
    public UserListen queryUserListenByUserIdAndListenId(Integer userId, Integer listenId) {
        return userListenMapper.queryUserListenByUserIdAndListenId(userId, listenId);
    }

    @Override
    public List<UserListen> queryUserListensByUserId(Integer userId) {
        return userListenMapper.queryUserListensByUserId(userId);
    }

    @Override
    public List<UserListen> queryUserListensByListenId(Integer listenId) {
        return userListenMapper.queryUserListensByListenId(listenId);
    }

    @Override
    public int queryUserCompletedListeningCount(Integer userId) {
        return userListenMapper.queryUserCompletedListeningCount(userId);
    }
}
