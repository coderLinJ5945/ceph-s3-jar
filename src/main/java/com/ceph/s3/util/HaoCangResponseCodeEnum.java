package com.ceph.s3.util;

public enum HaoCangResponseCodeEnum {

	SUCCESS(200,"SUCCESS","成功"),
	ERROR(500,"ERROR","失败"),
	NEED_LOGIN(300,"NEED_LOGIN","需要登录"),
	ILLEGAL_ARGUMENT(400,"ILLEGAL_ARGUMENT","参数错误"),
	LOGICAL_EXCEPTION(0,"LOGICAL_EXCEPTION","后台逻辑错误用于测试");
	
	private final int code;
	private final String desc;
	private final String productDesc;

	
	HaoCangResponseCodeEnum(int code, String desc, String productDesc){
		this.code = code;
		this.desc = desc;
		this.productDesc = productDesc;
	}


	public int getCode() {
		return code;
	}


	public String getDesc() {
		return desc;
	}


	public String getProductDesc() {
		return productDesc;
	}
	
}
