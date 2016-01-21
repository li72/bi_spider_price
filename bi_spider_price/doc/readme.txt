
 1  基于spring容器  管理类
 2  springJDBC 关联连接池   druid
 3  httpClient 请求
 4  





暂存

     /*HttpClientBuilder build = HttpClients.custom();
        HttpHost proxy = new HttpHost("202.106.16.36", 3128);
        HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler();
        CloseableHttpClient client = build.setRetryHandler(retryHandler).setProxy(proxy).build();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();*/



 /*HttpHost proxy = new HttpHost("202.106.16.36", 3128);
        HttpClientBuilder build = HttpClients.custom();
        HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler();
        CloseableHttpClient client = build.setRetryHandler(retryHandler).setProxy(proxy).build();
        HttpGet httpGet = new HttpGet(url);*/

