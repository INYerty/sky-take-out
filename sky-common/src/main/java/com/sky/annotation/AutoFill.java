package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//自定义注解：用于标识某个方法需要进行功能字段的自动填充处理。
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型
    OperationType value();
    //OperationType这里面就有INSERT和UPDATE两种操作类型,为什么呢？
    //因为只有在数据库进行插入和更新操作时，才会对操作时间、操作人等字段进行自动填充处理。
}
