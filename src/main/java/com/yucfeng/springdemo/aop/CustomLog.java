package com.yucfeng.springdemo.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;


@Slf4j
@Aspect
public class CustomLog {

    private String methodName;

    @Pointcut("execution(public String com.yucfeng.springdemo.service.KVServiceImpl.*(..))")
    public void pointCut(){};

    @Before("pointCut()")
    public void logStart(JoinPoint joinPoint){
        methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("{} start, args '{}'", methodName, args);
    }

    @After("pointCut()")
    public void logEnd(){
        log.info("{} end.", methodName);
    }

    @AfterReturning(value = "pointCut()", returning = "result")
    public void logReturn(Object result){
        log.info("{} returned '{}'.", methodName, result);
    }

    @AfterThrowing(value = "pointCut()", throwing = "exception")
    public void logException(Exception exception){
        System.out.println("Get transaction thrown: {" + exception+ "}");
        log.error("{} thrown exception.", methodName, exception);
    }
}
