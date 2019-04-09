package com.imooc.myo2o.dto;

import java.util.HashSet;

public class EchartXAxis {
	private String type="category";
	//去重
	private HashSet<String> data;
	public String getType() {
		return type;
	}
	
	public HashSet<String> getData() {
		return data;
	}
	public void setData(HashSet<String> data) {
		this.data = data;
	}
	
}
