package com.searcher.p_searcher.mappers;

import com.searcher.p_searcher.entities.Paper;
import com.searcher.p_searcher.entities.PaperTag;
import com.searcher.p_searcher.entities.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {

    /**
     * 插入tag
     * @param tag
     */
    void insert(Tag tag);

    /**
     * 根据文章id查询tag的List
     */
    List<PaperTag> queryTags(Integer id);
}
