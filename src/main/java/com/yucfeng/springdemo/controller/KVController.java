package com.yucfeng.springdemo.controller;


import com.yucfeng.springdemo.bean.Entity;
import com.yucfeng.springdemo.bean.RequestBean;
import com.yucfeng.springdemo.bean.ResponseBean;
import com.yucfeng.springdemo.service.KVServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class KVController {

    @Autowired
    private KVServiceImpl service;

    @GetMapping(value = "/{key}")
    public String getValue(@PathVariable String key,
                           @RequestParam(name = "force", required = false, defaultValue = "false") boolean force) throws InterruptedException {
        return service.doGetValue(key, force);
    }

    @PostMapping
    public ResponseBean SetValue(@RequestBody RequestBean requestBean) throws SQLException {
        Optional<Entity> entityOptional = service.doInsertOrUpdateValue(requestBean);
        return entityOptional.map(ResponseBean::new).orElse(null);
    }
}
