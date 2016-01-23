package com.li72.test.annotaion;
import java.lang.annotation.*;
 
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface  MyBeanFiled {
    String filed_comment();
}