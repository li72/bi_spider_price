package com.li72.bi.model;

/**
 * 
 * @author root
 *
 */
public class UrlSource {

    private String code;
    private String url;
    private String type;
    private Result result;
    private int errorTimes = 0; // 错误达到一定的次数则  放弃 

    private String yhdUrl;
    private String tmUrl;
    private String date;
    /**
     * 错误发生时间
     */
    private Long errorTime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getErrorTimes() {
        return errorTimes;
    }

    public void setErrorTimes(int errorTimes) {
        this.errorTimes = errorTimes;
    }

    public Long getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(Long errorTime) {
        this.errorTime = errorTime;
    }

    public String getYhdUrl() {
        return yhdUrl;
    }

    public void setYhdUrl(String yhdUrl) {
        this.yhdUrl = yhdUrl;
    }

    public String getTmUrl() {
        return tmUrl;
    }

    public void setTmUrl(String tmUrl) {
        this.tmUrl = tmUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "编号:" + code + ", 类型:" + type + ", URL" + url;
    }
}
