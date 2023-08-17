package com.yucfeng.springdemo.bean;

import lombok.Data;

import java.util.Map;

@Data
public class ResponseBean {

    private String key;
    private String value;

    public ResponseBean(Map<String, Object> map) {
        this.key = (String) map.getOrDefault("key", null);
        this.value = (String) map.getOrDefault("value", null);
    }
}
