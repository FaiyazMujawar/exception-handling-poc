package com.rheumera.poc.anoop.beans;

import java.util.Map;

public interface ErrorAware {
	public Map<String, ErrorDetail> getFieldErrors();
}
