package com.jokerstation.common.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.jokerstation.common.data.ErrorCode;

public class ParamUtil {

	private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	
	public static String validObj(Object obj) {
		if (null == obj) {
			return ErrorCode.PARAM_ILLEGAL.getMsg();
		}
		Set<ConstraintViolation<Object>> validResult = factory.getValidator().validate(obj);
		if (null == validResult || validResult.size() == 0) {
			return null;
		}
		Iterator<ConstraintViolation<Object>> iterator = validResult.iterator();
		if (iterator.hasNext()) {
			ConstraintViolation<Object> constraintViolation = (ConstraintViolation<Object>) iterator.next();
			String error = constraintViolation.getMessage();
			if (StringUtils.isBlank(error) || error.startsWith("Failed to convert")) {
				error = ErrorCode.PARAM_ILLEGAL.getMsg();
			}
			error = constraintViolation.getPropertyPath() + " " + error;
			return error;
		}
		return null;
	}
	
	public static String validList(List<?> list) {
		if (null == list || list.isEmpty()) {
			return "list is empty";
		}
		for (Object obj : list) {
			String msg = validObj(obj);
			if (null != msg) {
				return msg;
			}
		}
		return null;
	}
	
	public static String check(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			FieldError error = (FieldError )bindingResult.getAllErrors().get(0);
			String msg = error.getDefaultMessage();
			if (StringUtils.isBlank(msg) || msg.startsWith("Failed to convert")) {
				msg = "参数非法";
			}
			msg = error.getField() + msg;
			return msg;
		}
		return null;
	}
}
