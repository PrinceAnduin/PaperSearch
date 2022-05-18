package com.searcher.p_searcher.entities;

import lombok.Data;

import java.io.Serializable;

@Data
public class Tag implements Serializable {

    private int id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this.id == ((Tag)o).getId()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
