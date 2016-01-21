package com.yiniu.bi.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.yiniu.bi.model.ProxyInfo;
import com.yiniu.bi.model.Result;
import com.yiniu.bi.model.UrlSource;

public interface IService {

    public Result process(WebClient webClient, UrlSource source) throws Exception;
    public Result process(UrlSource source, ProxyInfo proxyInfo) throws Exception;
}
