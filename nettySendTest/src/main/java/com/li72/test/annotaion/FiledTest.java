package com.li72.test.annotaion;


public class FiledTest {

	@MyBeanFiled(filed_comment="这是一个用户名称")
	private  String   userName;
	
	@MyBeanFiled(filed_comment="这是一个用户ID")
	private  String   userId;
	@MyBeanFiled(filed_comment="这是一个订单id")
	private  String   orderId;
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	

}
