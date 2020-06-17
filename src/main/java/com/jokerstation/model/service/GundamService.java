package com.jokerstation.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.jokerstation.model.mapper.ModelItemMapper;
import com.jokerstation.model.pojo.ModelItem;

@Service
public class GundamService {

	@Autowired
	private ModelItemMapper modelItemMapper;
	
	public List<ModelItem> listOpenItems(String type) {
		PageHelper.orderBy("id");
		ModelItem record = new ModelItem();
		record.setOpen(true);
		record.setType(type);
		return modelItemMapper.select(record);
	}
}
