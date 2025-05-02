package com.chun.myspringboot.mapper;

import com.chun.myspringboot.pojo.Essay;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface EssayMapper {
    // 添加作文
    int addEssay(Essay essay);
    
    // 删除作文
    int deleteEssay(Integer essayId);
    
    // 更新作文
    int updateEssay(Essay essay);
    
    // 更新作文状态
    int updateEssayStatus(Integer essayId, Integer status);
    
    // 根据ID查询作文
    Essay queryEssayById(Integer essayId);
    
    // 查询用户的所有作文
    List<Essay> queryEssaysByUserId(Integer userId);
    
    // 查询所有作文
    List<Essay> queryAllEssays();
}
