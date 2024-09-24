package com.rheumera.poc.anoop.flatfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.rheumera.poc.anoop.beans.ErrorAware;
import com.rheumera.poc.anoop.beans.ErrorDetail;

public class JsonFieldSetMapper<T> implements FieldSetMapper<T>, ErrorAware {

	private final Class<T> type;
	private final ObjectMapper objectMapper;
	private final Map<String, ErrorDetail> fieldErrors;

	public JsonFieldSetMapper(Class<T> type, ObjectMapper objectMapper) {
		this.fieldErrors = new HashMap<>();
		this.type = type;
		this.objectMapper = objectMapper;
		this.objectMapper.addHandler(new CustomDeserializationProblemHandler());
	}

	@Override
	public T mapFieldSet(FieldSet fieldSet) {
		try {
			fieldErrors.clear();
			return objectMapper.convertValue(fieldSet.getProperties(), type);
		} catch (Exception e) {
			throw new RuntimeException("Error mapping FieldSet to object", e);
		}
	}

	public Map<String, ErrorDetail> getFieldErrors() {
		return new HashMap<>(fieldErrors);
	}

	private class CustomDeserializationProblemHandler extends DeserializationProblemHandler {

		@Override
		public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert,
				String failureMsg) throws IOException {
			String fieldName = ctxt.getParser().currentName();
			ErrorDetail errorDetail = ErrorDetail.builder().fieldName(fieldName).fieldValue(valueToConvert)
					.code("conversion")
					.desc("Failed to convert value: " + valueToConvert + " to type: " + targetType.getName()).build();
			fieldErrors.put(fieldName, errorDetail);
			return null;
		}

		@Override
		public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
				com.fasterxml.jackson.databind.JsonDeserializer<?> deserializer, Object beanOrClass,
				String propertyName) throws IOException {
			String fieldName = ctxt.getParser().currentName();
			System.err.println("handleUnknownProperty " + fieldName);
			return true;
		}

		@Override
		public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert,
				String failureMsg) throws IOException {
			String fieldName = ctxt.getParser().currentName();
			System.err.println("handleWeirdNumberValue " + fieldName);
			return null;
		}

		@Override
		public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass, JsonParser p,
				String msg) throws IOException {
			String fieldName = ctxt.getParser().currentName();
			System.err.println("handleMissingInstantiator " + fieldName);
			return null;
		}

	}
}
