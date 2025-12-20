package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面：实现公共字段自动填充
 */

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //切入点
    //指定切入点表达式，表示拦截com.sky.mapper包下所有类的所有方法，并且这些方法上标注了@AutoFill注解
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    //拦截到了，做什么？
    //写到通知里面
    //前置通知:在通知中进行公共字段的赋值
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");
        //需要做什么？
        //1.获取当前被拦截方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法的注解对象
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //2.获取到被拦截的方法的参数即 实体对象 ： (为当前的实体属性赋值)
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];//不能用Employee 因为不确定 可能是菜品

        //3.准备对当前的实体公共的属性赋值的数据 (时间 ID)
        LocalDateTime now = LocalDateTime.now();
        long currentUserId = BaseContext.getCurrentId();

        //4.根据不同的数据库操作类型，为对应的属性赋值 用反射
        if (operationType == OperationType.INSERT) {
            //如果是插入操作，要为这4个公共字段赋值
            try {
                //反射机制 TODO 注意这里的反射 ：没学过补课
                //获取实体对象的set方法
                Method setCreateTimes = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTimes = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUsers = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUsers = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射为实体对象的公共字段赋值
                setCreateTimes.invoke(entity, now);
                setUpdateTimes.invoke(entity, now);
                setCreateUsers.invoke(entity, currentUserId);
                setUpdateUsers.invoke(entity, currentUserId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            try {
                //获取实体对象的set方法
                Method setUpdateTimes = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUsers = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射为实体对象的公共字段赋值
                setUpdateTimes.invoke(entity, now);
                setUpdateUsers.invoke(entity, currentUserId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }
}