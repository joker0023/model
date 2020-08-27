package com.jokerstation.model.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jokerstation.model.queue.MessageQueue;
import com.jokerstation.model.service.WsServerEndpoint;


@RestController
public class CommonCtrl {
	
	private static Logger logger = LoggerFactory.getLogger(CommonCtrl.class);
	
	@Autowired
	private MessageQueue messageQueue;
	
	@RequestMapping("/test")
	public String test() {
		logger.info("test in ...");
//		logger.debug("debug log test");
//		loggenir.info("info log test");
//		logger.warn("warn log test");
//		logger.error("error log test");
		return "success";
	}
	
	@RequestMapping("/info")
	public Object info() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("web-socket-size", WsServerEndpoint.sessionPools.size());
		map.put("msg-size", messageQueue.size());
		return map;
	}
}
