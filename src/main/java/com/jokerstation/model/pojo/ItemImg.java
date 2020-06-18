package com.jokerstation.model.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

import lombok.Data;

@Data
public class ItemImg implements Serializable {

	private static final long serialVersionUID = -8353826495508205784L;

	@Id
	private Long id;
	
	private Long itemId;
	
	private String detailImg;
	
	private String localDetailImg;
	
	private Date created;
}
