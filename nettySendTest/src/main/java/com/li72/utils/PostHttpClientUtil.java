package com.li72.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class PostHttpClientUtil {
	public static void tryLogin() {
		// 创建默认的httpClient实例.
		HttpClient httpclient = new DefaultHttpClient();
		// 创建httppost
		HttpPost httppost = new HttpPost(
				"http://localhost:8080/yizhenWeb/userLogin");
		// 创建参数队列
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("model.userName", "admin"));
		formparams.add(new BasicNameValuePair("model.password", "admin"));
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);
			System.out.println("executing request " + httppost.getURI());
			HttpResponse response;
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				System.out.println("--------------------------------------");
				System.out.println("Response content: "
						+ EntityUtils.toString(entity, "UTF-8"));
				System.out.println("--------------------------------------");

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			httpclient.getConnectionManager().shutdown();
		}
	}

	
	/**
	 * @param post_url   要提交的url
	 * @param model      要提交的参数model
	 */
	public static boolean  post(String  post_url,List<NameValuePair> formparams) {
		boolean  isSuccess =true;  //返回是否发送成功
		// 创建默认的httpClient实例.
		HttpClient httpclient = new DefaultHttpClient();
		// 创建httppost
		HttpPost httppost = new HttpPost(post_url);
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);
			System.out.println("executing request " + httppost.getURI());
			HttpResponse response;
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				System.out.println("----------------success----------------------");
				System.out.println("Response content: "
						+ EntityUtils.toString(entity, "UTF-8"));
				System.out.println("--------------------------------------");
			}else {
				isSuccess =false;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			httpclient.getConnectionManager().shutdown();
		}
		return  isSuccess;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		//postHttp();
		//applyEvent();
		//webEvent();
		
		for(int i=0;i<100;i++){
			netty_sendKafka();
			Thread.sleep(1000);
		}
		
		
		String  url ="http://192.168.224.149:41414";
		String data="[{\"headers\" : {\"timestamp\" : \"434324343\"},\"body\" : \"'test':'is','hello':'world'\"}]" ;
		//System.out.println(sendInfo(url,data));
	/*	
		for(int i=0;i<100000;i++){
			//System.out.println(sendInfo(url,data));
			startup_test();
			Thread.sleep(500);
		}
		*/
		
	}
	  myRunnel  a = new myRunnel("li72") ;
	
	  public static String sendInfo(String sendurl, String data) throws Exception {
		  HttpClient client = new DefaultHttpClient();
	         HttpPost post = new HttpPost(sendurl);
	         System.out.println(data);
	         StringEntity myEntity = new StringEntity(data,"application/json","UTF-8");// 构造请求数据
	         post.setEntity(myEntity);// 设置请求体
	         String responseContent = null; // 响应内容
	         HttpResponse response = null;
	         try {
	             response = client.execute(post);
	             if (response.getStatusLine().getStatusCode() == 200) {
	             }
	             HttpEntity entity = response.getEntity();
	             responseContent = EntityUtils.toString(entity, "UTF-8");
	         } catch (ClientProtocolException e) {
	             e.printStackTrace();
	         } catch (IOException e) {
	             e.printStackTrace();
	         } 
	         return responseContent;
	     }
	


	/**
	 * 
	 */
	public static void postHttp() {// 
		//				String  url="http://localhost:8080/yizhenWeb/fetchUserStartup" ;
	//	String  url="http://10.11.11.71:6088/yizhenWeb/fetchUserStartup" ;
		startup_test();
	}
    
	public  static  void applyEvent(){
		//http://gathyz.111.com.cn/yizhenWeb/webEvent
				//	String  url="http://192.168.152.4:8080/yizhenWeb/applyEvent" ;
					String  url="http://localhost:8080/yizhenWeb/applyEvent" ;
					//	String  url="http://192.168.224.128:8080/yizhenWeb/applyEvent" ;
					//    String  url="http://10.11.11.71:6088/yizhenWeb/applyEvent" ;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("sessionId[1]","3|4|2|2|2|3|5|6|7|8|9|10"));
		formparams.add(new BasicNameValuePair("sessionId[2]","4"));
		formparams.add(new BasicNameValuePair("zrequest","4"));
		formparams.add(new BasicNameValuePair("lastPath","2"));
		formparams.add(new BasicNameValuePair("currentPath","2"));
		formparams.add(new BasicNameValuePair("deviceId","2"));
		formparams.add(new BasicNameValuePair("appSourceType","2"));
		formparams.add(new BasicNameValuePair("imei","2"));
		formparams.add(new BasicNameValuePair("time","2"));
		formparams.add(new BasicNameValuePair("productId","2"));
		formparams.add(new BasicNameValuePair("userId","2")); 
		formparams.add(new BasicNameValuePair("isLogin","2")); 
		formparams.add(new BasicNameValuePair("loginType","2")); 
		formparams.add(new BasicNameValuePair("thirdLogin","2")); 
		formparams.add(new BasicNameValuePair("channelId","2")); 
		formparams.add(new BasicNameValuePair("platformId","2")); 
		formparams.add(new BasicNameValuePair("version","2")); 
		formparams.add(new BasicNameValuePair("osVersion","2")); 
		formparams.add(new BasicNameValuePair("location","2")); 
		formparams.add(new BasicNameValuePair("stayTime","2")); 
		formparams.add(new BasicNameValuePair("indexNum","2")); 
		formparams.add(new BasicNameValuePair("keyWord","2")); 
		formparams.add(new BasicNameValuePair("appType","2")); 
		formparams.add(new BasicNameValuePair("itemName","2"));
		formparams.add(new BasicNameValuePair("itemID","2"));
		formparams.add(new BasicNameValuePair("mapParam","2"));
		
		System.out.println(post(url,formparams));
		
	}
	
	
	
	public  static  void webEvent(){
		//http://gathyz.111.com.cn/yizhenWeb/webEvent
				//	String  url="http://192.168.152.4:8080/yizhenWeb/applyEvent" ;
					String  url="http://192.168.224.151:41414" ;
					//	String  url="http://192.168.224.128:8080/yizhenWeb/applyEvent" ;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("sessionId","1234567890"));    
		formparams.add(new BasicNameValuePair("zrequest","12"));     
		formparams.add(new BasicNameValuePair("lastUrl","http://www.baidu.com"));      
		formparams.add(new BasicNameValuePair("currentUrl","http://www.111.com"));   
		formparams.add(new BasicNameValuePair("deviceId","1"));     
		formparams.add(new BasicNameValuePair("productId","1001"));    
		formparams.add(new BasicNameValuePair("time","1"));         
		formparams.add(new BasicNameValuePair("source","1"));       
		formparams.add(new BasicNameValuePair("userId","1123123"));       
		formparams.add(new BasicNameValuePair("version","1"));      
		formparams.add(new BasicNameValuePair("os","win7"));           
		formparams.add(new BasicNameValuePair("resolution","160*192"));   
		formparams.add(new BasicNameValuePair("browser","1"));      
		formparams.add(new BasicNameValuePair("stayTime","100"));     
		formparams.add(new BasicNameValuePair("keyWord","1sous"));      
		formparams.add(new BasicNameValuePair("mccmnc","1"));       

		
		System.out.println(post(url,formparams));
		
	}
	

	/**
	 * 
	 */
	public static void startup_test() {
		String url ="http://192.168.119.131:8989//xqkd/startup" ;
		//String  url="http://192.168.224.131:8080/yizhenWeb/fetchUserStartup" ;
		//	String  url="http://10.11.11.71:6088/yizhenWeb/fetchUserStartup" ;
		//String  url="http://101.226.186.11:6088/yizhenWeb/fetchUserStartup" ;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	    formparams.add(new BasicNameValuePair("productId","中文测试"));      
			formparams.add(new BasicNameValuePair("deviceId","2"));       
			formparams.add(new BasicNameValuePair("imei","3"));           
			formparams.add(new BasicNameValuePair("time","4"));           
			formparams.add(new BasicNameValuePair("channelId","5"));      
			formparams.add(new BasicNameValuePair("platformId","6"));     
			formparams.add(new BasicNameValuePair("version","7"));        
			formparams.add(new BasicNameValuePair("osversion","8"));   
			formparams.add(new BasicNameValuePair("os","androId"));   
			formparams.add(new BasicNameValuePair("isMobiledevice","9")); 
			formparams.add(new BasicNameValuePair("userId","11"));   
			formparams.add(new BasicNameValuePair("resolution","resolution"));    
			formparams.add(new BasicNameValuePair("network","12"));        
			formparams.add(new BasicNameValuePair("clientIp","13"));    
			formparams.add(new BasicNameValuePair("location","location")); 
			formparams.add(new BasicNameValuePair("havegps","要学点")); 
			formparams.add(new BasicNameValuePair("deviceName","16"));     
			formparams.add(new BasicNameValuePair("isjailBroken","18"));   
			//formparams.add(new BasicNameValuePair("serverTime","19"));    
			formparams.add(new BasicNameValuePair("mccmnc","20"));             

		System.out.println(post(url,formparams));
	}
	
	
	public static void netty_test() {
		String url ="http://localhost:8080/foo2" ;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	    formparams.add(new BasicNameValuePair("productId","中文测试"));      
			formparams.add(new BasicNameValuePair("deviceId","2"));       
			formparams.add(new BasicNameValuePair("imei","3"));           
			formparams.add(new BasicNameValuePair("time","4"));           
			formparams.add(new BasicNameValuePair("channelId","5"));      
			formparams.add(new BasicNameValuePair("platformId","6"));     
			   

		System.out.println(post(url,formparams));
	}
	
	
	/**
	 * 测试发送数据到kafka  
	 * 
	 */
	static   Random  r = new Random();
	public static void netty_sendKafka() {
		String url ="http://localhost:8989/xqkd/order" ;
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		    int  id= r.nextInt(1000000000);
		    int memberId=r.nextInt(1000000);
		    int  totalPrice= r.nextInt(1000)+100;
		    int  youhui= r.nextInt(100);
		    int   sendpay =r.nextInt(3);
	        formparams.add(new BasicNameValuePair("orderId",String.valueOf(id)));      
			formparams.add(new BasicNameValuePair("memberId",String.valueOf(memberId)));       
			formparams.add(new BasicNameValuePair("totalPrice",String.valueOf(totalPrice)));           
			formparams.add(new BasicNameValuePair("youhui",String.valueOf(youhui)));           
			formparams.add(new BasicNameValuePair("sendPay",String.valueOf(sendpay)));      
			formparams.add(new BasicNameValuePair("createDate","20151201"));     
		   

		System.out.println(post(url,formparams));
	}
 class   myRunnel   implements  Runnable{
		 
		 private String  threadName;
		 public myRunnel(String  threadName){
			 this.threadName = threadName ;
		 }

		@Override
		public void run() {
			for(int i=0;i<20;i++){
				applyEvent();
				System.out.println("-------" +threadName+" ------------");
			}
			
			
		}
		
	}
}

