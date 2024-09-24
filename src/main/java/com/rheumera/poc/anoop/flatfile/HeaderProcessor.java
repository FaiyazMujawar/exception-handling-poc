package com.rheumera.poc.anoop.flatfile;

import org.springframework.batch.item.file.transform.FieldSet;

public interface HeaderProcessor {
	public void init(FieldSet tokens);

	public int getCount();

	public String getBean2FileMapping(String fieldName);

	public boolean isBean2FileMapping(String fieldName);

	public String getFile2BeanMapping(String columnName);

	public boolean isFile2BeanMappingExists(String columnName);

	public String[] getNames();

	public int[] getRequiredIndices();
}
