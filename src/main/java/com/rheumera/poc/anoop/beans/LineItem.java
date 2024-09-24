package com.rheumera.poc.anoop.beans;

import java.util.Map;

import lombok.Data;

@Data
public class LineItem<T> {
	private int rowNumber;
	private String rowType;
	private String originalData;
	private T data;
	private Map<String, ErrorDetail> fieldErrors;
}
