package com.li72.bi.service.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.li72.bi.dao.IBaseDao;
import com.li72.bi.main.MainStart;
import com.li72.bi.model.Result;
import com.li72.bi.model.UrlSource;
import com.li72.bi.service.IBatchStore;

@Service("mySqlBatchStoreImpl")
public class MySqlBatchStoreImpl implements IBatchStore{

    private Logger LOGGER = LoggerFactory
            .getLogger(MySqlBatchStoreImpl.class);

    @Autowired
    private IBaseDao baseDao;

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

    @Override
    public void scanSaveBatch() {
        LOGGER.info("mySqlBatchStoreImpl---start" + new Date());
        if (running) {
            try {
                doSaveBatch();
            } catch (Exception e) {
                LOGGER.error("mySqlBatchStoreImpl---error" + new Date(), e);
            }
        }
        LOGGER.info("mySqlBatchStoreImpl---end" + new Date());
    }

    private void doSaveBatch() {
        JdbcTemplate template = baseDao.getTemplate();
        final String sql = "INSERT INTO bi_spider(TIME,PRICE,NAME,CODE,URL,TYPE,ADDTIME,PROMPRICE,DATE,CURRENTSTOCKNUM) VALUES(?,?,?,?,?,?,?,?,?,?)";
        final List<UrlSource> list = getMysqlSource();
        if(list != null && list.size() > 0){
            template.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    UrlSource urlSource = list.get(i);
                    Result result = urlSource.getResult();
                    ps.setLong(1, result.getUseTime());
                    ps.setDouble(2, result.getPrice());
                    ps.setString(3, result.getName());
                    ps.setString(4, urlSource.getCode());
                    ps.setString(5, urlSource.getUrl());
                    ps.setString(6, urlSource.getType());
                    ps.setLong(7, System.currentTimeMillis() / 1000);
                    ps.setDouble(8, result.getPromPrice());
                    ps.setString(9, urlSource.getDate());
                    ps.setLong(10, result.getCurrentStockNum());

                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });
        }

    }

    private List<UrlSource> getMysqlSource() {
        LOGGER.info("getMysqlSource before MYSQL_INSERT_SOURCE_QUEUE size:" + MainStart.MYSQL_INSERT_SOURCE_QUEUE.size());
        List<UrlSource> list = new ArrayList<UrlSource>();
        while (!MainStart.MYSQL_INSERT_SOURCE_QUEUE.isEmpty()) {
            UrlSource source = MainStart.MYSQL_INSERT_SOURCE_QUEUE.poll();
            if(source == null){
                continue;
            }
            list.add(source);
        }
        LOGGER.info("getMysqlSource after MYSQL_INSERT_SOURCE_LIST size:" + list.size());
        return list;
    }
}
