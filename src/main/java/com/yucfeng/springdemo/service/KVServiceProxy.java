package com.yucfeng.springdemo.service;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class KVServiceProxy {

//    private KVService service = null;
//
//    public KVServiceProxy(KVService service) {
//        this.service = service;
//    }
//
//    public KVService getKVServiceProxy() {
//        KVService proxy = (KVService) Proxy.newProxyInstance(service.getClass().getClassLoader(),
//                service.getClass().getInterfaces(),
//                new InvocationHandler() {
//                    @Override
//                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                        log.info("Transaction start");
//                        try {
//                            method.invoke(service, args);
//                            log.info("Transaction commit");
//                        } catch (Exception e) {
//                            log.warn("Transaction rollback", e);
//                        }
//                        return null;
//                    }
//                });
//
//        return proxy;
//    }
}
