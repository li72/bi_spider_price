package com.li72.bi.main;

import com.li72.bi.model.ProxyInfo;
import com.li72.bi.model.Result;
import com.li72.bi.model.UrlSource;
import com.li72.bi.service.IService;
import com.li72.bi.service.IStore;
import com.li72.bi.service.impl.TmallServiceImpl;
import com.li72.bi.service.impl.YHDServiceImpl;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

@Service("mainThread")
@Scope("prototype")
public class MainThread implements Runnable {

    private static Logger LOGGER = LoggerFactory
            .getLogger(MainThread.class);

    private IService processService;

    @Autowired
    private IStore mysqlStore;

    public void run() {
        while (!isStop() && !Thread.currentThread().isInterrupted()) {
            LOGGER.info(getThreadName() + "执行解析+++++++");
            // 获取url
            UrlSource urlSource = getUrl();
            if (urlSource == null) {
                LOGGER.info(getThreadName() + "URL队列没有数据，开始睡眠10分钟...");
                try {
                    threadSleep(10 * 60);
                } catch (InterruptedException e) {
                    LOGGER.info(getThreadName() + "线程睡眠中断...", e);
                }
                LOGGER.info(getThreadName() + "URL睡眠10分钟结束...");
                continue;
            }
            if ("tmall".equals(urlSource.getType())) {
                //天猫
                parseTmall(urlSource);
                try {
                    threadSleep(getSleepSec());
                } catch (InterruptedException e) {
                    LOGGER.info(getThreadName() + "线程睡眠中断...", e);
                }
                MainStart.TMALL_EXECUTE_NUM.incrementAndGet();
            } else if ("yhd".equals(urlSource.getType())) {
                //yhdSleepStrategy();
                //一号店
                parseYHD(urlSource);
                try {
                    threadSleep(getSleepSec());
                } catch (InterruptedException e) {
                    LOGGER.info(getThreadName() + "线程睡眠中断...", e);
                }
                MainStart.YHD_EXECUTE_NUM.incrementAndGet();
            }
        }
    }

    private void parseTmall(UrlSource urlSource) {
        //获取代理链接
        ProxyInfo proxyInfo = getProxy();
        processService = new TmallServiceImpl(MainStart.SOURCE_QUEUE);
        try {
            Result result = processService.process(urlSource, proxyInfo);
            if (result != null) {
                // 因为抓取淘宝在186上运行，链接数据库跨地域，为了稳定，采用定时器批插入处理
                putMysqlSourceQueue(urlSource);
                //mysqlStore.save(urlSource);
            }
            // 把代理链接放回队列
            putProxy(proxyInfo);
        } catch (Exception e) {
            if (e instanceof ConnectException || e instanceof SocketException || e instanceof HttpHostConnectException || e instanceof IOException) {
                // 链接超时的情况，把资源放入错误URL队列里
                if (urlSource != null) {
                    setErrorUrl(urlSource);
                    LOGGER.info("解析天猫超市信息遇到网络异常，把URL放到网络异常队列：" + urlSource, e);
                }
            } else if (e instanceof InterruptedException || e instanceof CannotGetJdbcConnectionException) {
                // 线程被中断
                if (urlSource != null) {
                    setErrorUrl(urlSource);
                    LOGGER.info("解析天猫超市信息遇到线程中断，把URL放到网络异常队列：" + urlSource, e);
                }
            } else {
                // 把代理链接放回队列
                putProxy(proxyInfo);
                LOGGER.info("解析天猫超市信息遇到异常，把代理放回队列：" + proxyInfo, e);
            }
        }
    }

    private void parseYHD(UrlSource urlSource) {
        //获取代理链接
        ProxyInfo proxyInfo = getProxy();
        processService = new YHDServiceImpl(MainStart.SOURCE_QUEUE);
        try {
            Result result = processService.process(urlSource, proxyInfo);
            if (result != null) {
                mysqlStore.save(urlSource);
            }
            // 把代理链接放回队列
            putProxy(proxyInfo);
        } catch (Exception e) {
            if (e instanceof ConnectException || e instanceof SocketException || e instanceof HttpHostConnectException || e instanceof IOException) {
                // 链接超时的情况，把资源放入错误URL队列里
                if (urlSource != null) {
                    setErrorUrl(urlSource);
                    LOGGER.info("解析一号店信息遇到网络异常，把URL放到网络异常队列：" + urlSource, e);
                }
            } else if (e instanceof InterruptedException || e instanceof CannotGetJdbcConnectionException) {
                // 线程被中断
                if (urlSource != null) {
                    setErrorUrl(urlSource);
                    LOGGER.info("解析一号店信息遇到线程中断，把URL放到网络异常队列：" + urlSource, e);
                }
            } else {
                // 把代理链接放回队列
                putProxy(proxyInfo);
                LOGGER.info("解析一号店信息遇到异常，把代理放回队列：" + proxyInfo, e);
            }
        }

    }

    public void yhdSleepStrategy() throws InterruptedException {
        int num = MainStart.YHD_EXECUTE_NUM.get();
        if (num > 200) {
            LOGGER.info(getThreadName() + "一号店抓取次数大于200，睡眠5分钟...");
            threadSleep(5 * 60);
            LOGGER.info(getThreadName() + "一号店睡眠5分钟结束...");
            MainStart.YHD_EXECUTE_NUM.set(0);
        }
    }

    private void setErrorUrl(UrlSource urlSource) {
        urlSource.setErrorTime(System.currentTimeMillis());
        urlSource.setErrorTimes(urlSource.getErrorTimes() + 1);
        MainStart.CONNECT_ERROR_SOURCE_QUEUE.offer(urlSource);
    }

    private boolean isStop() {
        return MainStart.STOP;
    }

    private UrlSource getUrl() {
        return MainStart.SOURCE_QUEUE.poll();
    }


    private ProxyInfo getProxy() {
        return MainStart.PROXY_QUEUE.poll();
    }

    private void putProxy(ProxyInfo proxyInfo) {

        //MainStart.PROXY_QUEUE.offer(proxyInfo);
    }

    private void threadSleep(int second) throws InterruptedException {
        Thread.sleep(second * 1000);
    }

    private int getRandom(int seed) {
        return (int) (Math.random() * seed) + 1;
    }

    private int getSleepSec() {
        return getRandom(7) + 3;
    }

    private String getThreadName() {
        return Thread.currentThread().getName();
    }

    private void putMysqlSourceQueue(UrlSource urlSource) {
        MainStart.MYSQL_INSERT_SOURCE_QUEUE.offer(urlSource);
    }
}
