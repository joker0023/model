package com.jokerstation.model.vo;

import lombok.Data;

@Data
public class MapMessage {

	private String sid;
	
	private Double latitude;
	
	private Double longitude;
	
	private String message;
}
