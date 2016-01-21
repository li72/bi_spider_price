package com.yiniu.bi.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.yiniu.bi.dao.IBaseDao;

@Repository(value = "baseDao")
public class BaseDaoImpl implements IBaseDao{

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public JdbcTemplate getTemplate() {
		return jdbcTemplate;
	}

}
