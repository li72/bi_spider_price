package com.yiniu.bi.model;

/**
 * 
 *  代理对象ip:端口
 *
 */

public class ProxyInfo {

    public ProxyInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "IP:" + ip + ", 端口:" + port;
    }
}
