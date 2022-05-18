package com.searcher.p_searcher.entities;

import lombok.Data;

@Data
public class PaperTag {

    private Tag tag;
    private int percent;

    @Override
    public boolean equals(Object o) {
        if (this.tag.getId() == ((PaperTag)o).getTag().getId()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tag.getId();
    }
}
