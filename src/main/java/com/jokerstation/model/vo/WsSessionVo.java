package com.jokerstation.model.vo;

import javax.websocket.Session;

import lombok.Data;

@Data
public class WsSessionVo {

	private String sid;
	
	private Double latitude;
	
	private Double longitude;
	
	private Session session;
}
