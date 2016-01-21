package com.yiniu.bi.service.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@Service("jdProcess")
public class JDServiceImpl extends BaseService
implements IService,Runnable {
	
	private static Logger LOGGER = LoggerFactory
            .getLogger(JDServiceImpl.class);

    public static final String JD_PRICE_PRE = "http://p.3.cn/prices/mgets?skuIds=J_";
    public static final String JD_PRICE_END = "&type=1";
    public static final String YHD_NAME_XPATH = "//*[@id=\"productMainName\"]";
    private  String  functionName="京东" ;
	
	public  JDServiceImpl(){
		super();
	}

    public JDServiceImpl(BlockingQueue<UrlSource> sourceQueue) {
		super(sourceQueue);
	}

    @Override
    public void init() {
    	super.init() ;
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
     //   System.out.println(htmlContent);
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

        // 获取销售价
        String priceAjaxUrl = JD_PRICE_PRE + itemCode + JD_PRICE_END;
        System.out.println(priceAjaxUrl);
        String price = null;
        String priceJsonContent =  getHtml(priceAjaxUrl, client);
        try {
            JSONObject jsonObject = (JSONObject) JSONObject.parse(formatStr(priceJsonContent));
            price = new String(jsonObject.get("p").toString());
            System.out.println(price);
        } catch (Exception e) {
            LOGGER.error("解析"+functionName+"价格信息出错，URL=" + priceAjaxUrl, e);
        }

        // 获取促销价  京东促销价也是上面的接口

        Result result = new Result();
        try {
            if (StringUtils.isNoneBlank(price)) {
                result.setPrice(Double.parseDouble(price));
            }
           
        } catch (Exception e) {
            LOGGER.error("转换"+functionName+"价格信息出错，价格字符串=" + price, e);
        }

        result.setName(name);
        result.setUseTime((System.currentTimeMillis() - start));
        source.setResult(result);

        return result;
    }

    private String formatStr(String str) {
       str =str.replace("[", "")
    		   .replace("]", "");
        return str;
    }

    private String getItemCode(String content) {
    	 Pattern pattern = Pattern.compile("skuid: (.*?),");
         Matcher matcher = pattern.matcher(content);
         while (matcher.find()) {
             return matcher.group()
            		 .replace(":", "")
            		 .replace(",","")
            		 .replace(" ", "")
            		 .replace("skuid", "");
         }
        return null;
    }

    private String parseProductName(String content) {
    	//content = decodeUnicode(content) ; 
    	Pattern pattern = Pattern.compile(" <title>(.*?)</title>");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
        	String  name = matcher.group() ;
        	name = name.replace("<title>", "")
        			.replace("-京东</title>", "");
        	//name = decodeUnicode(name) ;
            return name;
        }
    	
    	return  null;
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
	protected String getQuerySql() {
		String  sql ="SELECT url  FROM  bi_url " ;
		return sql;
	}

	@Override
	protected void initResource(List<UrlSource> queryList) {
		super.initResource(queryList) ;
	}

	@Override
	protected void initItemResource(UrlSource urlSource, String dateString) {
		String jdUrl = urlSource.getUrl();
        if (StringUtils.isNotBlank(jdUrl)) {
            UrlSource jd = new UrlSource();
            jd.setUrl(jdUrl);
            jd.setType("jd");
            jd.setDate(dateString);
            jd.setCode("99998");
            sourceQueue.offer(jd);
        }
		
	}

	@Override
	protected UrlSource cleanUrl(UrlSource urlSource) {
		// 对URL 进行 清理 
		String url = urlSource.getUrl() ;
		if(!StringUtils.isBlank(url)){
			 url = url.replaceAll("`", "");
			 url = url.replaceAll("\\^", "");
			}else
			{
			return  null ;
			}
		urlSource.setYhdUrl(url) ;
		return urlSource ;
	}

	@Override
	protected String getHtml(String url, CloseableHttpClient client)
			throws IOException, ClientProtocolException {
		String headerBrowserVal =  getBrowser() ;
		HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(hederKey, headerBrowserVal);
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String htmlContent = EntityUtils.toString(entity);
		return htmlContent;
	}

	@Override
	public void parseShop(UrlSource urlSource) {
		//获取代理链接
        ProxyInfo proxyInfo = getProxy();
        try {
            Result result = process(urlSource, proxyInfo);
            System.out.println(result);
            if (result != null) {
            	System.out.println(result);
              //  mysqlStore.save(urlSource);
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
}
