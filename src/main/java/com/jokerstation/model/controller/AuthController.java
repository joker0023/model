package com.jokerstation.model.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jokerstation.common.data.ResultModel;
import com.jokerstation.model.interceptor.ConsoleInterceptor;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Value("${console.u}")
	private String def_username;
	@Value("${console.p}")
	private String def_password;
	
	@RequestMapping("/consoleLogin")
	public ResultModel login(@RequestBody Map<String, String> params, HttpServletRequest request, HttpServletResponse response) {
		String user = params.get("user");
		String password = params.get("password");
		if (def_username.equals(user) && def_password.equals(DigestUtils.md5Hex(password))) {
			request.getSession().setAttribute(ConsoleInterceptor.CONSOLE_USER, user);
		} else {
			return new ResultModel("1", "用户信息错误");
		}
		return new ResultModel();
	}
}
