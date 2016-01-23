package com.li72.test.annotaion;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

 
 
public class AnnotaionTest {
 
   
 
    public static void main(String[] args) throws UnsupportedEncodingException, Exception {
        Method[] test_methods = MethodTest.class.getMethods();
         
        for(Method m : test_methods){
            if(m.isAnnotationPresent(MyAnnotationMethod.class)){
                MyAnnotationMethod test_annotation = m.getAnnotation(MyAnnotationMethod.class);
                System.out.println(test_annotation.method_comment());
            }
        }
 
        FiledTest ft= new FiledTest();
        ft.setOrderId("1234") ;
        ft.setUserId("9876") ;
        ft.setUserName("hello") ;
        Field[] fileds = ft.getClass().getDeclaredFields() ;
        Field.setAccessible(fileds, true);
        for(Field filed :fileds){
        	if(filed.isAnnotationPresent(MyBeanFiled.class)){
        		MyBeanFiled  mbf = filed.getAnnotation(MyBeanFiled.class);
        		System.out.println(mbf.filed_comment());
        		System.out.println(filed.get(ft));
        	}
        }
        
        
        
    }
 
}