package com.yucfeng.springdemo.bean;

import lombok.Data;

@Data
public class ResponseBean {

    private String key;
    private String value;

    public ResponseBean(Entity entity) {
        this.key = entity.getKey();
        this.value = entity.getValue();
    }
}
