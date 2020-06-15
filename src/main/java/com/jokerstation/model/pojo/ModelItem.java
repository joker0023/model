package com.jokerstation.model.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class ModelItem {

	private Long id;
	
	private String url;
	
	private String title;
	
	private String coverImg;
	
	private String localCoverImg;
	
	private String detailImg;
	
	private String localDetailImg;
	
	private Date created;
}
