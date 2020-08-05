package com.jokerstation.model.job;

import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jokerstation.common.util.JsonUtils;
import com.jokerstation.model.queue.MessageQueue;
import com.jokerstation.model.service.WsServerEndpoint;
import com.jokerstation.model.vo.MapMessage;
import com.jokerstation.model.vo.WsSessionVo;

@Component
public class HandleMessageJob {
	
	private static Logger logger = LoggerFactory.getLogger(HandleMessageJob.class);
	
	@Autowired
	private MessageQueue messageQueue;
	
	private static final double MAX_RANGE = 0.02;
	
	public void run() throws Exception {
		try {
			while (true) {
				Thread.sleep(1000);
				sendMsgList();
			}
		} catch (Exception e) {
			logger.error("ws回传消息出错", e);
		}
	}

	private void sendMsgList() {
		try {
//			logger.info("sendMsgList...");
			while (true) {
				MapMessage msg = messageQueue.pop();
				if (null == msg) {
					return;
				}
				
				sendOneMsg(msg);
			}
		} catch (Exception e) {
			logger.error("sendMsgList error", e);
		}
	}
	
	private void sendOneMsg(MapMessage msg) {
//		logger.info("###send msg: " + msg.getMessage());
		try {
			if (StringUtils.isBlank(msg.getSid()) || StringUtils.isBlank(msg.getMessage())
					|| null == msg.getLatitude() || null == msg.getLongitude()) {
				return;
			}
			Enumeration<String> keys = WsServerEndpoint.sessionPools.keys();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
//				logger.info("###key: " + key);
				if (msg.getSid().equals(key)) {
//					logger.warn("sid is equals");
					continue;
				}
				WsSessionVo wsSessionVo = WsServerEndpoint.sessionPools.get(key);
				if (null == wsSessionVo.getLatitude() || null == wsSessionVo.getLongitude()) {
//					logger.warn("Latitude or Longitude is null");
					continue;
				}
				
				double latv = wsSessionVo.getLatitude() - msg.getLatitude();
				double longv = wsSessionVo.getLongitude() - msg.getLongitude();
				if (Math.abs(latv) > MAX_RANGE || Math.abs(longv) > MAX_RANGE) {
//					logger.warn("max to max_range");
					continue;
				}
				
				String text = JsonUtils.toJson(msg);
				wsSessionVo.getSession().getBasicRemote().sendText(text);
//				logger.info("send msg: " + text);
			}
		} catch (Exception e) {
			logger.error("sendOneMsg error", e);
		}
	}
}
