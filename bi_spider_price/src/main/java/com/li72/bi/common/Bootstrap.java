package com.li72.bi.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.li72.bi.model.UrlSource;
import com.li72.bi.service.impl.JDServiceImpl;
import com.li72.bi.service.impl.TmallServiceImpl;

public class Bootstrap {
	
	 public static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);
	public static void main(String[] args) {
		BlockingQueue<UrlSource> queue = new ArrayBlockingQueue<UrlSource>(10000);
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/spring.xml");
		/*YHDServiceImpl  yhdService2 =(YHDServiceImpl)applicationContext.getBean("yhdProcess") ;
		yhdService2.setSourceQueue(queue) ;
		yhdService2.init();
		THREAD_POOL.execute(yhdService2) ;*/
		/*TmallServiceImpl  tmallService2 =(TmallServiceImpl)applicationContext.getBean("tmallProcess") ;
		tmallService2.init() ;
		THREAD_POOL.execute(tmallService2) ;*/   
		
		JDServiceImpl  jdService2 =(JDServiceImpl)applicationContext.getBean("jdProcess") ;
		jdService2.init() ;
		THREAD_POOL.execute(jdService2) ;    // kkk
		
		System.out.println(" init  finsh ");
		
	}

}
