package com.li72.bi.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.li72.bi.model.ProxyInfo;
import com.li72.bi.model.Result;
import com.li72.bi.model.UrlSource;

public interface IService {

    public Result process(WebClient webClient, UrlSource source) throws Exception;
    public Result process(UrlSource source, ProxyInfo proxyInfo) throws Exception;
}
