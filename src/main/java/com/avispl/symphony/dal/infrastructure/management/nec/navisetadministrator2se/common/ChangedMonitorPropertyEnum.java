/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * ChangedMonitorPropertyEnum
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/19/2024
 * @since 1.0.0
 */
public enum ChangedMonitorPropertyEnum {
	POWER_ON_TIME("PowerOnTime", "PowerOnTime"),
	LAMP_HOURS_USED("LampHoursUsed", "TimeLampUsage(hours)"),
	PANEL_HOURS_USED("PanelHoursUsed", "TimePanelUsage(hours)"),
	TOTAL_POWER_TIME("TotalPowerTime(includingPowerSave)", "PowerTotalTime"),
	HORIZONTAL_FREQUENCY("HorizontalFrequency(kHz)", "FrequencyHorizontal(kHz)"),
	VERTICAL_FREQUENCY("VerticalFrequency(Hz)", "FrequencyVertical(Hz)"),
	CARBON_USAGE("TotalCarbonUsage(kgCO2)", "CarbonUsageTotal(kgCO2)"),
	CARBON_SAVING("TotalCarbonSavings(kgCO2)", "CarbonSavingsTotal(kgCO2)"),
	INTERNAL_FAN_STATUS("InternalFanStatus", "FanStatusInternal"),
	SLOT_FAN_STATUS("SlotFanStatus", "FanStatusSlot"),
	;
	private final String defaultName;
	private final String propertyName;

	/**
	 * Constructor for ControllablePropertyEnum.
	 *
	 * @param defaultName The default name of the property.
	 * @param propertyName The name of the property.
	 */
	ChangedMonitorPropertyEnum(String defaultName, String propertyName) {
		this.defaultName = defaultName;
		this.propertyName = propertyName;
	}

	/**
	 * Retrieves {@link #defaultName}
	 *
	 * @return value of {@link #defaultName}
	 */
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * Retrieves {@link #propertyName}
	 *
	 * @return value of {@link #propertyName}
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Gets the {@code ChangedMonitorPropertyEnum} corresponding to the given default name.
	 *
	 * @param name The default name to match.
	 * @return The matching {@code ChangedMonitorPropertyEnum}, or {@code null} if no match is found.
	 */
	public static ChangedMonitorPropertyEnum getByDefaultName(String name) {
		Optional<ChangedMonitorPropertyEnum> property = Arrays.stream(ChangedMonitorPropertyEnum.values()).filter(item -> item.getDefaultName().equalsIgnoreCase(name)).findFirst();
		return property.orElse(null);
	}
}
