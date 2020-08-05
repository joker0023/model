package com.jokerstation.model.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jokerstation.common.util.JsonUtils;
import com.jokerstation.common.util.SpringUtil;
import com.jokerstation.model.queue.MessageQueue;
import com.jokerstation.model.vo.MapMessage;
import com.jokerstation.model.vo.WsSessionVo;

@ServerEndpoint("/ws/{sid}")
@Component
public class WsServerEndpoint {

	private static Logger logger = LoggerFactory.getLogger(WsServerEndpoint.class);

	public static ConcurrentHashMap<String, WsSessionVo> sessionPools = new ConcurrentHashMap<>();

	public void sendMessage(String sid, String message) throws IOException {
		WsSessionVo wsSessionVo = sessionPools.get(sid);
		if (wsSessionVo != null) {
			wsSessionVo.getSession().getBasicRemote().sendText(message);
		}
	}

	@OnOpen
	public void onOpen(Session session, @PathParam(value = "sid") String sid) throws Exception {
		logger.info("webSocket open, sid: " + sid);
		WsSessionVo wsSessionVo = new WsSessionVo();
		wsSessionVo.setSession(session);
		wsSessionVo.setSid(sid);
		sessionPools.put(sid, wsSessionVo);
	}

	@OnClose
	public void onClose(@PathParam(value = "sid") String sid) {
		logger.info("remove ws: " + sid);
		sessionPools.remove(sid);
	}

	@OnMessage
	public void onMessage(String message) throws IOException {
		try {
			logger.info("onMessage: " + message);
			MapMessage mapMsg = JsonUtils.toBean(message, MapMessage.class);
			if (StringUtils.isBlank(mapMsg.getSid()) || null == mapMsg.getLatitude() || null == mapMsg.getLongitude()) {
				return;
			}
			WsSessionVo wsSessionVo = sessionPools.get(mapMsg.getSid());
			if (null != wsSessionVo) {
				wsSessionVo.setLatitude(mapMsg.getLatitude());
				wsSessionVo.setLongitude(mapMsg.getLongitude());
			}
			if (StringUtils.isNotBlank(mapMsg.getMessage())) {
				SpringUtil.getBean(MessageQueue.class).push(message);
			}
		} catch (Exception e) {
			logger.error("onMessage error", e);
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		logger.error("webSocket error", throwable);
	}
}
