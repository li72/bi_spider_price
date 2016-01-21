package com.yiniu.bi.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yiniu.bi.dao.IBaseDao;
import com.yiniu.bi.model.ProxyInfo;
import com.yiniu.bi.model.UrlSource;
import com.yiniu.bi.store.IStore;

/**
 *  
 * 
 *
 */
public abstract class BaseService {
	
	private static Logger LOGGER = LoggerFactory
            .getLogger(BaseService.class);
	
	@Autowired
	protected IBaseDao baseDao;
	
	@Autowired
	protected IStore mysqlStore;

	protected final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	protected  String hederKey = "user-agent";
	
	protected  AtomicInteger executeNum = new AtomicInteger();
	
	protected  volatile boolean stop = false;
	
	/**
	 *  初始化url 队列   默认1w  可以用 set 方法修改 
	 */
	protected   BlockingQueue<UrlSource> sourceQueue = new ArrayBlockingQueue<UrlSource>(10000);;
	//  考虑代理ip 限制在最多1w条    这里设置到父类 1w  
	// 出错url 也写在父类   不会出错
	/**
     * 代理队列
     */
	protected  BlockingQueue<ProxyInfo>  proxyQueue  = new ArrayBlockingQueue<ProxyInfo>(10000);
    /**
     * 网络错误导致的资源队列
     */
	protected  BlockingQueue<UrlSource> connectErrorSourceQueue  = new ArrayBlockingQueue<UrlSource>(10000);
   
	
	public  BaseService(BlockingQueue<UrlSource> sourceQueue){
		this.sourceQueue= sourceQueue ;
	}
	
	public  BaseService(){
	}
	
	
	
	 public  String[] BROWSERS = {
         "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36",
         "Mozilla/5.0 (Windows; U; Windows NT 5.1; it; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11",
         "Opera/9.25 (Windows NT 5.1; U; en)",
         "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",
         "Mozilla/5.0 (compatible; Konqueror/3.5; Linux) KHTML/3.5.5 (like Gecko) (Kubuntu)",
         "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.12) Gecko/20070731 Ubuntu/dapper-security Firefox/1.5.0.12",
         "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.7 (KHTML, like Gecko) Ubuntu/11.04 Chromium/16.0.912.77 Chrome/16.0.912.77 Safari/535.7",
         "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:10.0) Gecko/20100101 Firefox/10.0"
 };
	 
	  public  String getBrowser() {
	        int length = BROWSERS.length;
	        return BROWSERS[(int) (Math.random() * length)];
	    }
	 
	  
	  protected  void  init (){
		  String querySql = getQuerySql();
		  System.out.println(querySql);
	      JdbcTemplate template = baseDao.getTemplate();
	        List<UrlSource> queryList = template.query(querySql,
	                BeanPropertyRowMapper.newInstance(UrlSource.class));
	        initResource(queryList) ;
	  };
	  
	  protected abstract String getQuerySql() ;
	  
	  protected  void initResource(List<UrlSource> queryList) {
		 Date date = new Date();
	     String dateString = format.format(date);
	        for (UrlSource urlSource : queryList) {
	        	if(cleanUrl(urlSource)!=null){
	        	   initItemResource(urlSource, dateString);
	        	}
	     }
	  }


	
	  protected abstract UrlSource cleanUrl(UrlSource urlSource) ;
	  protected abstract void initItemResource(UrlSource urlSource, String dateString) ;
	  
	  
	  protected CloseableHttpClient getClient() {
			HttpClientBuilder build = HttpClients.custom();
	        CloseableHttpClient client = build.build();
			return client;
		}
	  
	  /**
		 * @param url
		 * @param headerBrowserVal
		 * @param client
		 * @return
		 * @throws IOException
		 * @throws ClientProtocolException
		 */
		protected abstract String getHtml(String url, CloseableHttpClient client) throws IOException,
				ClientProtocolException ;

	public BlockingQueue<UrlSource> getSourceQueue() {
		return sourceQueue;
	}

	public void setSourceQueue(BlockingQueue<UrlSource> sourceQueue) {
		this.sourceQueue = sourceQueue;
	}

	
	 public  boolean isInterrupted(){
		return  Thread.currentThread().isInterrupted();
	 }
	 
	 public int getRandom(int seed) {
	        return (int) (Math.random() * seed) + 1;
	   }
	 public int getSleepSec() {
	        return getRandom(7) + 3;
	    }
	 public String getThreadName() {
	        return Thread.currentThread().getName();
	    }
	 public UrlSource getUrl() {
	        return sourceQueue.poll();
	    }
	  
	 public void threadSleep(int second) throws InterruptedException {
	        Thread.sleep(second * 1000);
	    }

	 public abstract void parseShop(UrlSource urlSource);
	 
	 public  ProxyInfo getProxy() {
	        return proxyQueue.poll();
	    }
	 
	 protected void setErrorUrl(UrlSource urlSource) {
	        urlSource.setErrorTime(System.currentTimeMillis());
	        urlSource.setErrorTimes(urlSource.getErrorTimes() + 1);
	        getConnectErrorSourceQueue().offer(urlSource);
	    }
	 
	 protected void putProxy(ProxyInfo proxyInfo) {
	        //MainStart.PROXY_QUEUE.offer(proxyInfo);
	    }
	 
	 /**
		 * 
		 */
		protected boolean judgeUrl(UrlSource urlSource) {
			LOGGER.info(getThreadName() + "执行解析+++++++");
			if (urlSource == null) {
			    LOGGER.info(getThreadName() + "URL队列没有数据，开始睡眠10分钟...");
			    try {
			        threadSleep(10 * 60);
			    } catch (InterruptedException e) {
			        LOGGER.info(getThreadName() + "线程睡眠中断...", e);
			    }
			    LOGGER.info(getThreadName() + "URL睡眠10分钟结束...");
			    return  false;
			}else {
				return  true ;
			}
		}
	 
	 
	 

	public BlockingQueue<ProxyInfo> getProxyQueue() {
		return proxyQueue;
	}

	public void setProxyQueue(BlockingQueue<ProxyInfo> proxyQueue) {
		this.proxyQueue = proxyQueue;
	}

	public BlockingQueue<UrlSource> getConnectErrorSourceQueue() {
		return connectErrorSourceQueue;
	}

	public void setConnectErrorSourceQueue(
			BlockingQueue<UrlSource> connectErrorSourceQueue) {
		this.connectErrorSourceQueue = connectErrorSourceQueue;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
	 
}
