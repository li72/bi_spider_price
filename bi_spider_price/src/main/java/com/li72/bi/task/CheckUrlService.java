package com.li72.bi.task;

import com.li72.bi.main.MainStart;
import com.li72.bi.model.UrlSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * 启动一个定时任务    每隔一定的时间检查   检查是否有需要 爬取的url  
 *
 */

@Service("checkUrlService")
public class CheckUrlService {

    private Logger LOGGER = LoggerFactory
            .getLogger(CheckUrlService.class);

    /**
     * false：表示不运行，true：表示运行
     */
    public boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void execute() {
        LOGGER.info("CheckUrlService---start:" + new Date());
        if (running) {
            try {
                doCheck();
            } catch (Exception e) {
                LOGGER.error("CheckUrlService---error" + new Date(), e);
            }
        }
        LOGGER.info("CheckUrlService---end:" + new Date());
    }

    private void doCheck() {
        List<UrlSource> list = new ArrayList<UrlSource>();
        LOGGER.info("doCheck before CONNECT_ERROR_SOURCE_QUEUE size:" + MainStart.CONNECT_ERROR_SOURCE_QUEUE.size());
        while (!MainStart.CONNECT_ERROR_SOURCE_QUEUE.isEmpty()) {
            UrlSource urlSource = MainStart.CONNECT_ERROR_SOURCE_QUEUE.poll();
            if (urlSource == null) {
                continue;
            }
            // 错误的次数
            int errorTimes = urlSource.getErrorTimes();
            if (errorTimes < 3) {
                if (checkErrorTime(urlSource)) {
                    // 重置errorTime,放入URL队列
                    urlSource.setErrorTime(null);
                    MainStart.SOURCE_QUEUE.offer(urlSource);
                } else {
                    // 错误时间与当前时间间隔不到10分钟，重新放回ERROR URL队列
                    list.add(urlSource);
                }
            }
        }
        for (UrlSource urlSource : list) {
            MainStart.CONNECT_ERROR_SOURCE_QUEUE.offer(urlSource);
        }
        LOGGER.info("doCheck end CONNECT_ERROR_SOURCE_QUEUE size:" + MainStart.CONNECT_ERROR_SOURCE_QUEUE.size());
    }

    private boolean checkErrorTime(UrlSource urlSource) {
        Long errorTime = urlSource.getErrorTime();
        if ((System.currentTimeMillis() - errorTime) > 10 * 60 * 1000) {
            return true;
        } else {
            return false;
        }
    }

}
