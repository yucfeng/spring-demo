package com.yucfeng.springdemo.controller;

import com.yucfeng.springdemo.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/msg/")
public class SendMessageController {
    @PostMapping("/sendMsg")
    public ResponseEntity<String> sendMsg(@RequestBody String content){
        List<String> list = WebSocketServer.getList();
        list.forEach(sid ->{
            try {
                WebSocketServer.sendInfo("服务端说：" + content, sid);
            } catch (IOException e) {
                log.error("sendMsg failed.", e);
            }
        });
        return new ResponseEntity<>(content, HttpStatus.OK);
    }
}
