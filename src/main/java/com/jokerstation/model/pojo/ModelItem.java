package com.jokerstation.model.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

import lombok.Data;

@Data
public class ModelItem implements Serializable {
	
	private static final long serialVersionUID = 2445430389801083496L;

	@Id
	private Long id;
	
	private String url;
	
	private String title;
	
	private String coverImg;
	
	private String localCoverImg;
	
	private String detailImg;
	
	private String localDetailImg;
	
	private String type;
	
	/**
	 * 0 初始
	 * 1 成功
	 * 2 失败
	 */
	private Integer status;
	
	private Boolean open;
	
	private Date created;
}
