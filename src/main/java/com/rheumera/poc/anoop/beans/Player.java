package com.rheumera.poc.anoop.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private Float salary;
	@JsonProperty("last-name")
	private String lastName;
	@JsonProperty("first-name")
	private String firstName;
	private String position;
	@JsonProperty("birth_year")
	private Integer birthYear;
	@JsonProperty("debut.year")
	private Integer debutYear;
}