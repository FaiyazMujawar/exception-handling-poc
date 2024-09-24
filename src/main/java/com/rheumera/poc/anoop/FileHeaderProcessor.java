package com.rheumera.poc.anoop;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.file.transform.FieldSet;

import com.rheumera.poc.anoop.flatfile.HeaderProcessor;

public class FileHeaderProcessor implements HeaderProcessor {

	public void init(FieldSet tokens) {
		String[] values = tokens.getValues();
		this.names = new String[this.getCount()];
		this.requiredIndices = new int[this.getCount()];
		int j = 0;
		for (int i = 0; i < values.length; i++) {
			if (isFile2BeanMappingExists(values[i])) {
				this.requiredIndices[j] = i;
				this.names[j] = getFile2BeanMapping(values[i]);
				j++;
			}
		}
	}

	public int getCount() {
		return this.file2BeanMapping.size();
	}

	public String getBean2FileMapping(String fieldName) {
		return this.bean2FileMapping.get(fieldName);
	}

	public boolean isBean2FileMapping(String fieldName) {
		return this.bean2FileMapping.containsKey(fieldName);
	}

	public String getFile2BeanMapping(String columnName) {
		return this.file2BeanMapping.get(columnName);
	}

	public boolean isFile2BeanMappingExists(String columnName) {
		return this.file2BeanMapping.containsKey(columnName);
	}

	public String[] getNames() {
		return names;
	}

	public int[] getRequiredIndices() {
		return requiredIndices;
	}

	public FileHeaderProcessor() {
		this.file2BeanMapping = new HashMap<>();
		this.bean2FileMapping = new HashMap<>();
		this.file2BeanMapping.put("ID", "id");
		this.file2BeanMapping.put("last name", "last-name");
		this.file2BeanMapping.put("first name", "first-name");
		this.file2BeanMapping.put("office position", "position");
		this.file2BeanMapping.put("DateOfBirthYear", "birth_year");
		this.file2BeanMapping.put("debutYear", "debut.year");
		this.bean2FileMapping.put("id", "ID");
		this.bean2FileMapping.put("last-name", "last name");
		this.bean2FileMapping.put("first-name", "first name");
		this.bean2FileMapping.put("position", "office position");
		this.bean2FileMapping.put("birth_year", "DateOfBirthYear");
		this.bean2FileMapping.put("debut.year", "debutYear");
	}

	private Map<String, String> bean2FileMapping;
	private Map<String, String> file2BeanMapping;
	private String[] names;
	private int[] requiredIndices;
}
