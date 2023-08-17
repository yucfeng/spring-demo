package com.yucfeng.springdemo.bean;

import lombok.Data;

@Data
public class Entity {

    private String key;
    private String value;
    private int inChange;
    private Long lastModifiedTime;
}
