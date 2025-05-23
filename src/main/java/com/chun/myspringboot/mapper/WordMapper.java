package com.chun.myspringboot.mapper;

import com.chun.myspringboot.pojo.Word;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface WordMapper {


    /**
     * ############################
     *          增加
     *#############################
     */
    //增加一个单词
    int addWord(Word word);


    /**
     *#############################
     *          删除
     *#############################
     */
    //删除一个单词
    int deleteWord(Integer wordId);

    /**
     *#############################
     *          修改
     *#############################
     */
    //修改一个单词
    int updateWord(Word word);

    //修改一个单词study变为1
    int updateWordStudy1(Integer wordId);
    //修改一个单词study变为0
    int updateWordStudy0(Integer wordId);

    //修改一个单词remember变为1
    int updateWordRemember1(Integer wordId);
    //修改一个单词remember变为0
    int updateWordRemember0(Integer wordId);

    //重新把学习进度单词归为0
    int updateWordStudyByGrade(Integer grade);


    //修改一个单词collection变为1
    int updateWordCollection1(Integer wordId);
    //修改一个单词collection变为0
    int updateWordCollection0(Integer wordId);

    /**
     *#############################
     *          查询
     *#############################
     */

    //查询所有单词信息
    List<Word> queryAllWord();
    //根据ID查询一个单词的信息
    Word queryWordById(Integer wordId);

    //根据单词名称查询单词
    Word queryWordByName(String wordName);

    /*
       ----------------------------
       学习功能（从数据库随机查询一个单词）
       ----------------------------
     */

    //根据grade查询未学习的单词
    Word queryWordStudy0ByGrade(Integer grade);


    /*
       ---------------------------
            收藏功能
       ---------------------------
     */
    //查询所有收藏的单词
    List<Word> queryAllWordCollection();
    //根据Grade查询收藏的单词
    List<Word> queryWordCollectionByGrade(Integer grade);


    /*
       ---------------------------
             记住与未记住功能
       ---------------------------
     */
    //查询所有记住的单词
    List<Word> queryAllRemember();
    //根据grade查询记住的单词
    List<Word> queryAllWordRememberByGrade(Integer grade);

    //查询所有没有记住的单词
    List<Word> queryAllUnremembered();
    //根据grade查询没有记住的单词
    List<Word> queryAllWordUnrememberedByGrade(Integer grade);


    /*
      ---------------------------
            查询数量功能
      ---------------------------
     */
    //根据grade查询单词数量
    int queryAllWordNumberByGrade(Integer grade);
    //根据grade查询已会单词数量
    int queryRememberNumberByGrade(Integer grade);
    //根据grade查询已经学习单词数量
    int queryStudyNumberByGrade(Integer grade);

    /**
     * 搜索单词
     * @param query 搜索关键词
     * @param gradeId 等级ID（可选）
     * @param status 学习状态（可选）
     * @param userId 用户ID（可选）
     * @return 单词列表
     */
    List<Word> searchWords(String query, Integer gradeId, Integer status, Integer userId);

    /**
     * 获取搜索结果总数
     * @param query 搜索关键词
     * @param gradeId 等级ID（可选）
     * @param status 学习状态（可选）
     * @param userId 用户ID（可选）
     * @return 总数
     */
    int countSearchWords(String query, Integer gradeId, Integer status, Integer userId);

    /**
     * 获取单词总数
     * @return 单词总数
     */
    int getTotalWordsCount();

}
