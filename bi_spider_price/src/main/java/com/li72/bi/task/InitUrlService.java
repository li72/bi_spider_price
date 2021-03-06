package com.li72.bi.task;


import com.li72.bi.dao.IBaseDao;
import com.li72.bi.main.MainStart;
import com.li72.bi.model.UrlSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Service("initUrlService")
public class InitUrlService {

    private Logger LOGGER = LoggerFactory
            .getLogger(InitUrlService.class);

    @Autowired
    private IBaseDao baseDao;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

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
        LOGGER.info("initUrlService---start" + new Date());
        if (running) {
            try {
                initUrl(MainStart.WEB_TYPE);
            } catch (Exception e) {
                LOGGER.error("initUrlService---error" + new Date(), e);
            }
        }
        LOGGER.info("initUrlService---end" + new Date());
    }

    public void initUrl(String webType) {
        String querySql = getQuerySql(webType);
        LOGGER.info("initUrlService---sql:" + querySql);
        JdbcTemplate template = baseDao.getTemplate();
        List<UrlSource> queryList = template.query(querySql,
                BeanPropertyRowMapper.newInstance(UrlSource.class));
        LOGGER.info("initUrlService---URLSize:" + queryList.size());
        initResource(queryList, webType);
    }

    private String getQuerySql(String webType) {
        String sql1 = null;
        String sql2 = null;
        if ("yhd".equals(webType)) {
            sql1 = "SELECT ta.code,ta.yhdUrl,ta.tmUrl FROM ( SELECT t.goodsCode code,t.goodsLinkForOne yhdUrl,t.goodsLinkForTmall tmUrl FROM bi_goods_url_manage t WHERE t.goodsLinkForOne NOT LIKE '%search%' AND t.goodsLinkForOne IS NOT NULL AND t.goodsLinkForOne <> '' GROUP BY t.goodsCode) ta ";
            sql2 = " LEFT JOIN ( SELECT t.code FROM bi_spider t WHERE t.date = '???' AND t.type = 'yhd') tb ON (ta.code = tb.code) WHERE tb.code IS NULL";
        }else if("tmall".equals(webType)){
            sql1 = "SELECT ta.code,ta.yhdUrl,ta.tmUrl FROM ( SELECT t.goodsCode code,t.goodsLinkForOne yhdUrl,t.goodsLinkForTmall tmUrl FROM bi_goods_url_manage t WHERE t.goodsLinkForOne NOT LIKE '%search%' AND t.goodsLinkForTmall IS NOT NULL AND t.goodsLinkForTmall <> '' GROUP BY t.goodsCode) ta ";
            sql2 = " LEFT JOIN ( SELECT t.code FROM bi_spider t WHERE t.date = '???' AND t.type = 'tmall') tb ON (ta.code = tb.code) WHERE tb.code IS NULL";
        }
        Date today = new Date();
        String todayStr = format.format(today);

        String querySql = sql1 + sql2;
        return querySql.replace("???", todayStr);
    }


    private void initResource(List<UrlSource> queryList, String webType) {
        Date date = new Date();
        String dateString = format.format(date);
        if ("yhd".equals(webType)) {
            for (UrlSource urlSource : queryList) {
                initYhdResource(urlSource, dateString);
            }
        } else if ("tmall".equals(webType)) {
            for (UrlSource urlSource : queryList) {
                initTmallResource(urlSource, dateString);
            }
        }
        LOGGER.info(webType + " URLResource size:" + MainStart.SOURCE_QUEUE.size());
    }

    private void initYhdResource(UrlSource urlSource, String dateString) {
        String yhdUrl = urlSource.getYhdUrl();
        if (StringUtils.isNotBlank(yhdUrl)) {
            UrlSource yhd = new UrlSource();
            yhd.setUrl(yhdUrl);
            yhd.setType("yhd");
            yhd.setDate(dateString);
            yhd.setCode(urlSource.getCode());
            MainStart.SOURCE_QUEUE.offer(yhd);
        }

    }

    private void initTmallResource(UrlSource urlSource, String dateString) {
        String tmUrl = urlSource.getTmUrl();
        if (StringUtils.isNotBlank(tmUrl)) {
            UrlSource yhd = new UrlSource();
            yhd.setUrl(tmUrl);
            yhd.setType("tmall");
            yhd.setDate(dateString);
            yhd.setCode(urlSource.getCode());
            MainStart.SOURCE_QUEUE.offer(yhd);
        }
    }
}
