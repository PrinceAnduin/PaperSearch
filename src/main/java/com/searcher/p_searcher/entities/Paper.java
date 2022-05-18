package com.searcher.p_searcher.entities;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Paper implements Serializable {

    private int id;
    private String name;
    private String author;
    private String content;
    private String path;

    private List<PaperTag> tags = new ArrayList<>();
}
