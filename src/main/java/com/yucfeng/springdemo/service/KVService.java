package com.yucfeng.springdemo.service;

import com.yucfeng.springdemo.bean.Entity;
import com.yucfeng.springdemo.bean.RequestBean;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Service
public interface KVService {

    String doGetValue(String key, boolean force) throws InterruptedException;
    Optional<Entity> doInsertOrUpdateValue(RequestBean requestBean) throws InterruptedException, SQLException;
}