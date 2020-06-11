package com.jokerstation.common.exception;

import com.jokerstation.common.data.ErrorCode;

public class BizException extends RuntimeException {

	private static final long serialVersionUID = -75365637106647848L;
	
	private String code;
	
	public BizException() {
		this(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMsg());
	}
	
	public BizException(final ErrorCode errorCode) {
		this(errorCode.getCode(), errorCode.getMsg());
	}
	
	public BizException(final String message) {
		this(ErrorCode.SYSTEM_ERROR.getCode(), message);
	}
	
	public BizException(final String code, final String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
