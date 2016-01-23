package com.li72.test.annotaion;

public class MethodTest {

	 @MyAnnotationMethod(method_comment="方法注释-----这是第一个方法")
	    public void testAnnotation1(){
	       System.out.println("this is test  annotation---on");
	    }
	     
	    @MyAnnotationMethod(method_comment="方法注释-------这是第二个方法")
	    public String testAnnotation2(){
	        return "Hello, World!";
	    }
	     
	    public String testAnnotation3(){
	        return "Hello, World!";
	    }
	

}
