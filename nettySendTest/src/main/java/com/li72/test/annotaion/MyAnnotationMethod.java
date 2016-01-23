package com.li72.test.annotaion;
import java.lang.annotation.*;
 
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface  MyAnnotationMethod {
    String method_comment();
}