package com.li72.bi.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;

import com.li72.bi.dao.IBaseDao;
import com.li72.bi.model.Result;
import com.li72.bi.model.UrlSource;
import com.li72.bi.service.IStore;

@Service("mysqlStore")
public class MySqlStoreImpl implements IStore {


    @Autowired
    private IBaseDao baseDao;

    @Override
    public void save(final UrlSource urlSource) {
        JdbcTemplate template = baseDao.getTemplate();
        final String sql = "INSERT INTO bi_spider(TIME,PRICE,NAME,CODE,URL,TYPE,ADDTIME,PROMPRICE,DATE,CURRENTSTOCKNUM) VALUES(?,?,?,?,?,?,?,?,?,?)";
        final Result result = urlSource.getResult();
        template.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql);
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
                return ps;
            }
        });
    }
}
