/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.dto;

/**
 * VolumeValueDTO
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 2/1/2024
 * @since 1.0.0
 */
public class VolumeValueDTO {
	private String minValue;
	private String maxValue;

	public VolumeValueDTO(String minValue, String maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public VolumeValueDTO() {
	}

	/**
	 * Retrieves {@link #minValue}
	 *
	 * @return value of {@link #minValue}
	 */
	public String getMinValue() {
		return minValue;
	}

	/**
	 * Retrieves {@link #maxValue}
	 *
	 * @return value of {@link #maxValue}
	 */
	public String getMaxValue() {
		return maxValue;
	}
}
