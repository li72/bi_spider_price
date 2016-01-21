package com.yiniu.bi.service.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.WebClient;
import com.yiniu.bi.model.ProxyInfo;
import com.yiniu.bi.model.Result;
import com.yiniu.bi.model.UrlSource;
import com.yiniu.bi.service.BaseService;
import com.yiniu.bi.service.IService;

@Service("yhdProcess")
public class YHDServiceImpl extends BaseService 
implements IService,Runnable {

	private static Logger LOGGER = LoggerFactory
            .getLogger(YHDServiceImpl.class);

    public static final String YHD_PRICE_PRE = "http://gps.yhd.com/restful/detail?mcsite=1&provinceId=20&pmId=";
    public static final String YHD_PRICE_END = "&callback=jQuery111306277138183359057_1450844554317&_=1450844554318";
    public static final String YHD_PROM_PRICE_PRE = "http://gps.yhd.com/restful/promotion?callback=jQuery1113014867033367045224_1451029683228&pmId=";
    public static final String YHD_PROM_PRICE_END = "&promId=1245183&provinceId=20&channelId=1&_=1451029683252";
    public static final String YHD_NAME_XPATH = "//*[@id=\"productMainName\"]";
    private  String  functionName="1号店" ;
    
    public YHDServiceImpl(BlockingQueue<UrlSource> sourceQueue) {
		super(sourceQueue);
		
	}
    
    public YHDServiceImpl() {
		super();
	}
    
    @Override
    public void init() {
    	super.init() ;
    }
	@Override
	public void initResource(List<UrlSource> queryList) {
		super.initResource(queryList) ;
	}
	@Override
	public void initItemResource(UrlSource urlSource, String dateString) {
		String yhdUrl = urlSource.getYhdUrl();
        if (StringUtils.isNotBlank(yhdUrl)) {
            UrlSource yhd = new UrlSource();
            yhd.setUrl(yhdUrl);
            yhd.setType("yhd");
            yhd.setDate(dateString);
            yhd.setCode(urlSource.getCode());
            sourceQueue.offer(yhd);
        }
	}
    @Override
    public Result process(WebClient webClient, UrlSource source) throws Exception {
        return null;
    }
    @Override
    public Result process(UrlSource source, ProxyInfo proxyInfo) throws Exception {
        long start = System.currentTimeMillis();
        String url = source.getUrl();
        CloseableHttpClient client = getClient();
		String htmlContent = getHtml(url, client);
        System.out.println(url);
        // 获取名称
        String name = null;
        try {
            name = parseProductName(htmlContent);
        } catch (Exception e) {
            if (e instanceof ConnectTimeoutException) {
                // 访问过于频繁被封
                LOGGER.error("访问"+functionName+"URL过于频繁，URL=" + url, e);
                throw e;
            } else {
                LOGGER.error("解析"+functionName+"产品名称出错，URL=" + url, e);
                name = "获取名称失败";
            }
        }
        String itemCode = null;
        try {
            itemCode = getItemCode(htmlContent);
        } catch (Exception e) {
            LOGGER.error("解析"+functionName+"itemId出错，URL=" + url, e);
            throw e;
        }
        // 获取销售价与库存
        String priceAjaxUrl = YHD_PRICE_PRE + itemCode + YHD_PRICE_END;
        System.out.println(priceAjaxUrl);
        String price = null;
        String currentStockNum = null;
        String priceJsonContent =  getHtml(priceAjaxUrl, client);

        try {
            JSONObject jsonObject = (JSONObject) JSONObject.parse(formatStr(priceJsonContent));
            price = new String(jsonObject.get("currentPrice").toString());
            currentStockNum = new String(jsonObject.get("currentStockNum").toString());
        } catch (Exception e) {
            LOGGER.error("解析"+functionName+"价格信息出错，URL=" + priceAjaxUrl, e);
        }

        // 获取促销价
        String promPriceAjaxUrl = YHD_PROM_PRICE_PRE + itemCode + YHD_PROM_PRICE_END;
        System.out.println(promPriceAjaxUrl);
        String promPriceJsonContent = getHtml(promPriceAjaxUrl, client);
        String promPrice = null;
        try {
            Object jsonObject = JSONObject.parse(formatStr(promPriceJsonContent));
            if (jsonObject != null) {
                Object promPriceStr = ((JSONObject) jsonObject).get("promPrice");
                if (promPriceStr != null) {
                    promPrice = new String(promPriceStr.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("解析"+functionName+"促销价格信息出错，URL=" + promPriceAjaxUrl, e);
        }
        Result result = new Result();
        try {
            if (StringUtils.isNoneBlank(price)) {
                result.setPrice(Double.parseDouble(price));
            }
            if (StringUtils.isNoneBlank(promPrice)) {
                result.setPromPrice(Double.parseDouble(promPrice));
            }
            if (StringUtils.isNoneBlank(currentStockNum)) {
                result.setCurrentStockNum(Long.parseLong(currentStockNum));
            }
        } catch (Exception e) {
            LOGGER.error("转换"+functionName+"属性信息出错，价格字符串=" + price, e);
        }
        result.setName(name);
        result.setUseTime((System.currentTimeMillis() - start));
        source.setResult(result);

        return result;
    }

	/**
	 * @param url
	 * @param client
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
    @Override
	protected String getHtml(String url, 
			CloseableHttpClient client) throws IOException,
			ClientProtocolException {
		String headerBrowserVal =  getBrowser() ;
		HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(hederKey, headerBrowserVal);
        httpGet.setHeader("cookie", "provinceId=20");
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String htmlContent = EntityUtils.toString(entity);
		return htmlContent;
	}

	
    private String formatStr(String str) {
        int beginIndex = str.indexOf("(");
        String preFormat = str.substring(beginIndex + 1);
        return preFormat.substring(0, preFormat.length() - 1);
    }

    private String getItemCode(String content) {
        int preIndex = content.indexOf("content=\"YHD_DETAIL.");
        String newTest = content.substring(preIndex + 20);
        int endIndex = newTest.indexOf("\"");
        return newTest.substring(0, endIndex);
    }
    
    
    private String parseProductName(String content) {
        try {
            int preIndex = content.indexOf("<meta name=\"Keywords\" content=\"");
            if (preIndex == -1) {
                return "获取名称失败";
            }
            String newText = content.substring(preIndex + 31);
            int endIndex = newText.indexOf("\">");
            return newText.substring(0, endIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取名称失败";
        }
    }

    public String parseProductNameWithCleaner(String content) throws Exception {
        if (content.contains("过于频繁")) {
            // 访问过于频繁被封
            throw new ConnectTimeoutException();
        }
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        TagNode rootNode = htmlCleaner.clean(content);
        Object[] objects = rootNode.evaluateXPath(YHD_NAME_XPATH);
        TagNode nameNode = (TagNode) objects[0];
        return nameNode.getText().toString();
    }


    @Override
	public String getQuerySql() {
    String 	 querySql = "SELECT ta.code,ta.yhdUrl,ta.tmUrl FROM ( SELECT t.goodsCode code,t.goodsLinkForOne yhdUrl,t.goodsLinkForTmall tmUrl FROM bi_goods_url_manage t WHERE t.goodsLinkForOne NOT LIKE '%search%' AND t.goodsLinkForOne IS NOT NULL AND t.goodsLinkForOne <> '' GROUP BY t.goodsCode) ta " 
         + " LEFT JOIN ( SELECT t.code FROM bi_spider t WHERE t.date = '???' AND t.type = 'yhd') tb ON (ta.code = tb.code) WHERE tb.code IS NULL";
      Date today = new Date();
      String todayStr = format.format(today);
		return querySql.replace("???", todayStr);
	}

	@Override
	public UrlSource cleanUrl(UrlSource urlSource) {
		// 对URL 进行 清理 
		String url = urlSource.getYhdUrl() ;
		if(!StringUtils.isBlank(url)){
			 url = url.replaceAll("`", "");
		     url = url.replaceAll("\\^", "");
		}else {
			return  null ;
		}
		urlSource.setYhdUrl(url) ;
		return  urlSource ;
	}

	@Override
	public void run() {
		
		while(!isStop() && ! isInterrupted()){
			// 获取url
			 UrlSource urlSource = getUrl();
			 if(judgeUrl(urlSource)){
			    parseShop(urlSource);
			  }
             try {
                 threadSleep(getSleepSec());
             } catch (InterruptedException e) {
                 LOGGER.info(getThreadName() + "线程睡眠中断...", e);
             }
             executeNum.incrementAndGet();
		}
		
	}

	@Override
	public void parseShop(UrlSource urlSource) {
		 //获取代理链接
        ProxyInfo proxyInfo = getProxy();
        try {
            Result result = process(urlSource, proxyInfo);
            System.out.println(result);
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
                    LOGGER.info("解析"+functionName+"信息遇到网络异常，把URL放到网络异常队列：" + urlSource, e);
                }
            } else if (e instanceof InterruptedException || e instanceof CannotGetJdbcConnectionException) {
                // 线程被中断
                if (urlSource != null) {
                    setErrorUrl(urlSource);
                    LOGGER.info("解析"+functionName+"信息遇到线程中断，把URL放到网络异常队列：" + urlSource, e);
                }
            } else {
                // 把代理链接放回队列
                putProxy(proxyInfo);
                LOGGER.info("解析"+functionName+"信息遇到异常，把代理放回队列：" + proxyInfo, e);
            }
        }
		
	}



}
