package com.jokerstation.common.data;

public enum ErrorCode {

	SYSTEM_ERROR("1", "系统错误"),
	PARAM_ILLEGAL("1001", "参数非法"),
	SERVICE_ILLEGAL("1002", "业务非法"),
	MISSING_TIMESTAMP("1003", "缺少时间戳"),
	EXPIRED("1004", "请求过期"),
	SIGN_ILLEGAL("1005", "签名不合法");
	
	private String code;
	private String msg;
	private ErrorCode(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
