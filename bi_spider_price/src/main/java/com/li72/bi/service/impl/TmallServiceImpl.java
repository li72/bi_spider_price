package com.li72.bi.service.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.li72.bi.model.ProxyInfo;
import com.li72.bi.model.Result;
import com.li72.bi.model.UrlSource;
import com.li72.bi.service.BaseService;
import com.li72.bi.service.IService;

@Service("tmallProcess")
public class TmallServiceImpl extends BaseService 
implements IService ,Runnable{
	
	private static Logger LOGGER = LoggerFactory
            .getLogger(YHDServiceImpl.class);

    public static final String TMALL_PRICE_PRE = "https://mdskip.taobao.com/core/initItemDetail.htm?isApparel=false&showShopProm=false&queryMemberRight=true&tryBeforeBuy=false&isForbidBuyItem=false&isRegionLevel=true&addressLevel=3&offlineShop=false&tmallBuySupport=true&service3C=false&progressiveSupport=false&household=false&isAreaSell=true&isUseInventoryCenter=true&isSecKill=false&cartEnable=true&sellerPreview=false&callback=onMdskip&ref=https%253A%252F%252Flist.tmall.com%252Fsearch_product.htm%253Fq%253D%2525BC%2525A6%2526user_id%253D1129326215%2526type%253Dp%2526cat%253D50514008%2526spm%253Da3204.7084717.a2227oh.d100%2526from%253Dchaoshi.index.pc_1_searchbutton&brandSiteId=0";
    public static final String TMALL_PRICE_TTME = "&cachedTimestamp=";
    public static final String TMALL_PRICE_ITEM_ID = "&itemId=";
    public  static  final  String  COOKIE_CONTENT="sm4=440100;tracknick=;_tb_token_=wAuKPoeCDCft;thw=cn;cna=vaU0DoVzBmYCAQ4XfJTwWLoa;x=e%3D1%26p%3D*%26s%3D0%26c%3D0%26f%3D0%26g%3D0%26t%3D0;_cc_=UIHiLt3xSw%3D%3D;tg=0;uc3=nk2=&id2=&lg2=;v=0;mt=ci=-1_0;t=d3f261db00f4de4e00b71138bd6e26d3;l=AoCAfteYkh3Xyws8c0sqE4Ns0ABSCWTT;isg=10100D905FF8E30BAA649DBBD77F2F8C;uc1=cookie14=UoWyjC5Hcmsq3w%3D%3D;cookie2=da687a35ac072e5ac79afd9d2b1b2673" ;
    public static String TMALL_PRICE_XPATH = "/*//*[@id=\"J_PromoPrice\"]/dd/div/span";
    public static String TMALL_NAME_XPATH = "//*[@id=\"J_DetailMeta\"]/div[1]/div[1]/div/div[1]/h1";
    public static int WAIT_JS_TIME = 5000;
    public static int WAIT_TIMES = 2;
    public static BlockingQueue<UrlSource> MYSQL_INSERT_SOURCE_QUEUE = new ArrayBlockingQueue<UrlSource>(10000);
    
    private  String  functionName="天猫" ;
    

    public TmallServiceImpl(BlockingQueue<UrlSource> sourceQueue) {
		super(sourceQueue);
	}
    
    public  TmallServiceImpl(){
    	super() ;
    }
    @Override
    public Result process(UrlSource source, ProxyInfo proxyInfo) throws Exception {
        long start = System.currentTimeMillis();

        String url = source.getUrl();
        CloseableHttpClient client = getClient();
        System.out.println(url);
        String htmlContent = getHtml(url, client);

        Result result = new Result();
        // 获取名称
        try {
            result.setName(parseProductNameWithCleaner(htmlContent));
        } catch (Exception e) {
            if (e instanceof ConnectTimeoutException) {
                // 访问过于频繁被封
                LOGGER.error("访问"+functionName+"URL过于频繁，URL=" + url, e);
                throw e;
            } else {
                LOGGER.error("解析"+functionName+"产品名称出错，URL=" + url, e);
                result.setName("获取名称失败");
            }
        }
        String itemId = null;
        try {
            itemId = getItemId(url);
        } catch (Exception e) {
            LOGGER.error("解析"+functionName+"itemId出错，URL=" + url, e);
            throw e;
        }

        String priceAjaxUrl = TMALL_PRICE_PRE + TMALL_PRICE_TTME + System.currentTimeMillis() + TMALL_PRICE_ITEM_ID + itemId;
        // 获取销售价格、促销价、库存
        String priceJsonContent = getHtml(priceAjaxUrl, client);
        try {
            parsePriceAndStockNum(priceJsonContent, result, url);
        } catch (Exception e) {
            LOGGER.error("解析"+functionName+"价格信息出错，URL=" + url + ", json=" + priceJsonContent, e);
        }

        result.setUseTime((System.currentTimeMillis() - start));
        source.setResult(result);
        System.out.println(result);
        return result;
    }

    @Override
    public Result process(WebClient webClient, UrlSource source) throws Exception {
        long start = System.currentTimeMillis();
        if (StringUtils.isBlank(source.getUrl())) {
            return null;
        }

        HtmlPage rootPage = webClient.getPage(source.getUrl());

        HtmlElement priceSpanNode = null;
        HtmlElement nameNode = null;
        /*for (int i = 0; i < WAIT_TIMES; i++) {
            // 等待JS执行
            Thread.sleep(WAIT_JS_TIME);
            priceSpanNode = rootPage.getFirstByXPath(TMALL_PRICE_XPATH);
            nameNode = rootPage.getFirstByXPath(TMALL_NAME_XPATH);

            if (priceSpanNode != null && nameNode != null) {
                // 获取到页面元素
                break;
            }
        }*/
        Thread.sleep(10000);
        priceSpanNode = rootPage.getFirstByXPath(TMALL_PRICE_XPATH);
        nameNode = rootPage.getFirstByXPath(TMALL_NAME_XPATH);

        // 等待JS时间过后仍然没有获取到数据
        if (priceSpanNode == null || nameNode == null) {
            return null;
        }

        Result result = new Result();
        try {
            result.setPrice(Double.parseDouble(priceSpanNode.getFirstChild().toString()));
        } catch (NumberFormatException e) {

        }
        result.setName(nameNode.getFirstChild().toString());
        result.setUseTime((System.currentTimeMillis() - start));

        source.setResult(result);
        return result;
    }


    private String getSkuId(String url) {
        Pattern pattern = Pattern.compile("[&|\\?]skuId=[0-9]+");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            return matcher.group().split("=")[1];
        }
        return null;
    }

    private void parsePriceAndStockNum(String priceContent, Result result, String url) {
        int beginIndex = priceContent.indexOf("(");
        String preFormat = priceContent.substring(beginIndex + 1);
        String json = preFormat.substring(0, preFormat.length() - 1);
        JSONObject jsonObject = (JSONObject) JSONObject.parse(json);
        JSONObject defaultModel = (JSONObject) jsonObject.get("defaultModel");
        JSONObject inventoryDO = (JSONObject) defaultModel.get("inventoryDO");
        if (inventoryDO != null) {
            result.setCurrentStockNum(((Integer) inventoryDO.get("icTotalQuantity")).longValue());
        }
        JSONObject itemPriceResultDO = (JSONObject) defaultModel.get("itemPriceResultDO");
        JSONObject priceInfo = (JSONObject) itemPriceResultDO.get("priceInfo");
        JSONObject def = (JSONObject) priceInfo.get("def");
        if (def == null) {
            //另外一种解析方式，用skuid当key
            String skuId = getSkuId(url);
            def = (JSONObject) priceInfo.get(skuId);
        }
        result.setPrice(Double.parseDouble((String) def.get("price")));
        JSONArray promotionList = def.getJSONArray("promotionList");
        if (promotionList != null) {
            JSONObject promotion = (JSONObject) promotionList.get(0);
            result.setPromPrice(Double.parseDouble((String) promotion.get("price")));
        }


    }

    private String parseProductNameWithCleaner(String content) throws Exception {
        if (content.contains("频繁") || content.contains("验证码")) {
            // 访问过于频繁被封
            throw new ConnectTimeoutException();
        }
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        TagNode rootNode = htmlCleaner.clean(content);
        Object[] objects = rootNode.evaluateXPath(TMALL_NAME_XPATH);
        TagNode nameNode = (TagNode) objects[0];
        return nameNode.getText().toString().replaceAll("\\r", "").replaceAll("\\n", "");
    }

    private String getItemId(String url) {
        Pattern pattern = Pattern.compile("[&|\\?]id=[0-9]*");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            return matcher.group().split("=")[1];
        }
        return null;
    }
    
    @Override
    public  void init() {
    	super.init();
    }
    

	@Override
	protected String getQuerySql() {
	String	querySql = "SELECT ta.code,ta.yhdUrl,ta.tmUrl FROM ( SELECT t.goodsCode code,t.goodsLinkForOne yhdUrl,t.goodsLinkForTmall tmUrl FROM bi_goods_url_manage t WHERE t.goodsLinkForOne NOT LIKE '%search%' AND t.goodsLinkForTmall IS NOT NULL AND t.goodsLinkForTmall <> '' GROUP BY t.goodsCode) ta " 
    + " LEFT JOIN ( SELECT t.code FROM bi_spider t WHERE t.date = '???' AND t.type = 'tmall') tb ON (ta.code = tb.code) WHERE tb.code IS NULL";
	Date today = new Date();
    String todayStr = format.format(today);
		return querySql.replace("???", todayStr);
	}

	@Override
	protected void initResource(List<UrlSource> queryList) {
		super.initResource(queryList) ;
	}

	@Override
	protected void initItemResource(UrlSource urlSource, String dateString) {
		String tmUrl = urlSource.getTmUrl();
        if (StringUtils.isNotBlank(tmUrl)) {
            UrlSource tmall = new UrlSource();
            tmall.setUrl(tmUrl);
            tmall.setType("tmall");
            tmall.setDate(dateString);
            tmall.setCode(urlSource.getCode());
            sourceQueue.offer(tmall);
        }
    }

	@Override
	protected UrlSource cleanUrl(UrlSource urlSource) {
		// 对URL 进行 清理 
		String url = urlSource.getTmUrl() ;
		if(!StringUtils.isBlank(url)){
			url = url.replaceAll("`", "");
			url = url.replaceAll("\\^", "");
		}else {
			return  null ;
		}
		urlSource.setTmUrl(url) ;
		return  urlSource ;
	}

	@Override
	protected String getHtml(String url, CloseableHttpClient client)
			throws IOException, ClientProtocolException {
		String  headerBrowserVal = getBrowser() ;
		HttpGet httpGet = new HttpGet(url);
        //设置浏览器信息
        httpGet.setHeader("user-agent", headerBrowserVal);
        //设置地区
        httpGet.setHeader("cookie", COOKIE_CONTENT);
        httpGet.setHeader("referer", url);// TODO 参考链接   不一定是自己
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
	
	private void putMysqlSourceQueue(UrlSource urlSource) {
       MYSQL_INSERT_SOURCE_QUEUE.offer(urlSource);
    }

	@Override
	public void run() {
		
		while(!isStop() && !isInterrupted()){
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
