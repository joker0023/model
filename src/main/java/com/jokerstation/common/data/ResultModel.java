package com.jokerstation.common.data;

public class ResultModel {

	private String code = "0";
	
	private String errorMsg;
	
	private Object data;
	
	public ResultModel() {
		
	}
	
	public ResultModel(Object data) {
		this.data = data;
	}
	
	public ResultModel(String code, String errorMsg) {
		this.code = code;
		this.errorMsg = errorMsg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
