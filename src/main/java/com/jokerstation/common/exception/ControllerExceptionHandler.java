package com.jokerstation.common.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jokerstation.common.data.ResultModel;

@RestControllerAdvice
public class ControllerExceptionHandler {
	
	public static Logger logger = LoggerFactory
			.getLogger(ControllerExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	public Object handlerException(Exception exception, HttpServletRequest request, HttpServletResponse response) {
		
		response.setCharacterEncoding("UTF-8");
		
		String code = "1";
		String message = "系统异常: " + exception.getMessage();
		if (exception instanceof BizException) {
			//业务错误返回正常，通过errorMsg显示错误
			code = ((BizException) exception).getCode();
			message = exception.getMessage();
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			logger.error(exception.getMessage(), exception);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
		return new ResultModel(code, message);
	}
}
