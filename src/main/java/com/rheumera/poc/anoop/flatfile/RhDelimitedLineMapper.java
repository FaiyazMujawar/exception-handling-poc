package com.rheumera.poc.anoop.flatfile;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.rheumera.poc.anoop.beans.ErrorAware;
import com.rheumera.poc.anoop.beans.LineItem;

public class RhDelimitedLineMapper<T> implements LineMapper<LineItem<T>>, InitializingBean {
	private DelimitedLineTokenizer tokenizer;
	private FieldSetMapper<T> fieldSetMapper;
	private boolean isHeaderFound;
	private HeaderProcessor headerProcessor;

	public RhDelimitedLineMapper() {
		this.tokenizer = new DelimitedLineTokenizer();
		this.tokenizer.setStrict(false);
	}

	public void setHeaderProcessor(HeaderProcessor headerProcessor) {
		this.headerProcessor = headerProcessor;
	}

	@Override
	public LineItem<T> mapLine(String line, int lineNumber) throws Exception {
		LineItem<T> lineItem = new LineItem<T>();
		lineItem.setRowNumber(lineNumber);
		lineItem.setOriginalData(line);
		if (line != null) {
			line = line.trim();
		}
		if (line == null || line.length() == 0) {
			lineItem.setRowType("Empty Row");
			return lineItem;
		}
		FieldSet tokens = tokenizer.tokenize(line);
		if (!isHeaderFound) {
			this.headerProcessor.init(tokens);
			this.tokenizer.setNames(headerProcessor.getNames());
			this.tokenizer.setIncludedFields(headerProcessor.getRequiredIndices());
			isHeaderFound = true;
			lineItem.setRowType("Header Row");
			return lineItem;
		}
		T obj = fieldSetMapper.mapFieldSet(tokens);
		if (fieldSetMapper instanceof ErrorAware) {
			lineItem.setFieldErrors(((ErrorAware) fieldSetMapper).getFieldErrors());
		}
		lineItem.setData(obj);
		lineItem.setRowType("Data Row");
		return lineItem;
	}

	public void setFieldSetMapper(FieldSetMapper<T> fieldSetMapper) {
		this.fieldSetMapper = fieldSetMapper;
	}

	@Override
	public void afterPropertiesSet() {
		Assert.state(tokenizer != null, "The LineTokenizer must be set");
		Assert.state(fieldSetMapper != null, "The FieldSetMapper must be set");
	}
}
