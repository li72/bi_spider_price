package com.li72.bi.main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.li72.bi.model.ProxyInfo;
import com.li72.bi.model.UrlSource;
import com.li72.bi.task.InitUrlService;



public class MainStart {

    private static Logger LOGGER = LoggerFactory
            .getLogger(MainStart.class);

    private ApplicationContext applicationContext;

    /**
     * 资源队列
     */
    public static BlockingQueue<UrlSource> SOURCE_QUEUE = new ArrayBlockingQueue<UrlSource>(10000);
    /**
     * 代理队列
     */
    public static BlockingQueue<ProxyInfo> PROXY_QUEUE = new ArrayBlockingQueue<ProxyInfo>(10000);
    /**
     * 网络错误导致的资源队列
     */
    public static BlockingQueue<UrlSource> CONNECT_ERROR_SOURCE_QUEUE = new ArrayBlockingQueue<UrlSource>(10000);
    /**
     * 线程池
     */
    public static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);
    /**
     * 一号店连续解析数量
     */
    public static AtomicInteger YHD_EXECUTE_NUM = new AtomicInteger();
    /**
     * 天猫超市连续解析数量
     */
    public static AtomicInteger TMALL_EXECUTE_NUM = new AtomicInteger();
    public static volatile boolean STOP = false;
    /**
     * 抓取网站类型
     */
    public static String WEB_TYPE = "yhd";
    /**
     * mysql存放待插入资源队列
     */
    public static BlockingQueue<UrlSource> MYSQL_INSERT_SOURCE_QUEUE = new ArrayBlockingQueue<UrlSource>(10000);


    /*private static final String LOG4J_PATH = "E:\\java\\resource\\log4j.properties";
    private static final String SPRING_PATH = "file:E:\\java\\resource\\spring\\spring.xml";*/
    private static final String SPRING_PATH = "classpath:spring/spring.xml";

   

  

    public MainStart() {
        applicationContext = new ClassPathXmlApplicationContext(SPRING_PATH);
    }

    public static void main(String[] args) {
        LOGGER.info("爬虫项目启动...");

        if (args != null && args.length > 0 && StringUtils.isNotBlank(args[0])) {
            WEB_TYPE = args[0];
        }
        LOGGER.info("爬虫抓取网站为:" + WEB_TYPE);
        MainStart mainStart = new MainStart();
        mainStart.initUrl(WEB_TYPE);
        mainStart.start();
        //mainStart.pkGoodsStart();
    }

    public void start() {
        for (int i = 0; i < 2; i++) {
            THREAD_POOL.execute((MainThread) applicationContext.getBean("mainThread"));
        }
    }


    public void initUrl(String webType) {
        LOGGER.info("初始化URL...");
        InitUrlService initUrlService = (InitUrlService) applicationContext.getBean("initUrlService");
        initUrlService.initUrl(WEB_TYPE);
    }

  

    @Override
    protected void finalize() throws Throwable {
        THREAD_POOL.shutdownNow();
    }
}
