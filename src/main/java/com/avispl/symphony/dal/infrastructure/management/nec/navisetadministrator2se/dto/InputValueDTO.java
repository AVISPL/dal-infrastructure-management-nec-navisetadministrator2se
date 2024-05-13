/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ValueDTO representing input values with a name and corresponding VCP (Video Control Protocol) value.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 2/1/2024
 * @since 1.0.0
 */
public class InputValueDTO {
	private String name;
	@JsonProperty("vcpvalue")
	private String value;

	/**
	 * Constructs an InputValueDTO with the specified name and value.
	 *
	 * @param name  The name associated with the input value.
	 * @param value The VCP value representing the input.
	 */
	public InputValueDTO(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Default constructor for InputValueDTO.
	 */
	public InputValueDTO() {
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name} value
	 *
	 * @param name new value of {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets {@link #value} value
	 *
	 * @param value new value of {@link #value}
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
