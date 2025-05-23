package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.NoticeMapper;
import com.chun.myspringboot.pojo.Notice;
import com.chun.myspringboot.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public Notice queryNoticeById(Integer noticeId) {
        return noticeMapper.queryNoticeById(noticeId);
    }

    @Override
    public int addNotice(Notice notice) {
        return noticeMapper.addNotice(notice);
    }

    @Override
    public int deleteNotice(Integer noticeId) {
        return noticeMapper.deleteNotice(noticeId);
    }

    @Override
    public int updateNotice(Notice notice) {
        return noticeMapper.updateNotice(notice);
    }

    @Override
    public List<Notice> queryAllNotice() {
        return noticeMapper.queryAllNotice();
    }

    @Override
    public Notice queryNewNoticeById() {
        return noticeMapper.queryNewNoticeById();
    }

    @Override
    public int queryAllNoticeCount() {
        // 获取所有公告数量
        List<Notice> notices = noticeMapper.queryAllNotice();
        return notices != null ? notices.size() : 0;
    }
}
