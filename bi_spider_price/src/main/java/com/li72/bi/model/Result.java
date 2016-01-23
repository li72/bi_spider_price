package com.li72.bi.model;

/**
 * 
 *  返回结果的封装 
 *
 */
public class Result {

    private String name;
    private double price;
    private double promPrice;
    private Long currentStockNum = -1l;
    private Long useTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getUseTime() {
        return useTime;
    }

    public double getPromPrice() {
        return promPrice;
    }

    public void setPromPrice(double promPrice) {
        this.promPrice = promPrice;
    }

    public void setUseTime(Long useTime) {
        this.useTime = useTime;
    }

    public Long getCurrentStockNum() {
        return currentStockNum;
    }

    public void setCurrentStockNum(Long currentStockNum) {
        this.currentStockNum = currentStockNum;
    }
    
    @Override
    public String toString() {
    	return "name= "+name+" ,price= " +price+",promPrice=" +promPrice;
    }
    
}
