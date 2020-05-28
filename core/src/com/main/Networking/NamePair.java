package com.main.Networking;

public class NamePair {
    private Integer id;
    private String name;

    public NamePair() {
    }

    public NamePair(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getKey() {
        return getId();
    }

    public String getValue() {
        return getName();
    }
}
