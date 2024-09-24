package com.rheumera.poc.anoop.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDetail {
	private String code;
	private String desc;
	private String fieldName;
	private String fieldValue;
}
