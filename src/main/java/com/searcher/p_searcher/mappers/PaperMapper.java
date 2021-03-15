package com.searcher.p_searcher.mappers;

import com.searcher.p_searcher.entities.Paper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaperMapper {
    /**
     * 插入论文
     * @param name 文章名
     * @param author 作者名
     * @param content 论文内容
     * @param path 文件存储路径
     */
    void insert(Paper paper);
}
