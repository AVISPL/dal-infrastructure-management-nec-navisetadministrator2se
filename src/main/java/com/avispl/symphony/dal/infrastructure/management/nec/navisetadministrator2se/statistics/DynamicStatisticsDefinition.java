/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.statistics;


import java.util.Arrays;
import java.util.Optional;

/**
 * DynamicStatisticsDefinition is Enum representing dynamic statistics definitions.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/15/2024
 * @since 1.0.0
 */
public enum DynamicStatisticsDefinition {
	TEMPERATURE1("IntakeTemperature", "TemperatureIntake(C)"),
	TEMPERATURE2("ExhaustTemperature", "TemperatureExhaust(C)"),
	TEMPERATURE3("Sensor1Temperature", "TemperatureSensor1(C)"),
	TEMPERATURE4("Sensor2Temperature", "TemperatureSensor2(C)"),
	TEMPERATURE5("InternalTemperature", "TemperatureInternal(C)"),
	TEMPERATURE6("SlotTemperature", "TemperatureSlot(C)"),
	;

	private final String defaultName;
	private final String propertyName;

	/**
	 * Constructs a DynamicStatisticsDefinition enum with the specified name.
	 *
	 * @param defaultName The name of the dynamic statistic definition.
	 */
	DynamicStatisticsDefinition(final String defaultName, final String propertyName) {
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
	 * Retrieve a DynamicStatisticsDefinition by its name.
	 *
	 * @param name The name to search for.
	 * @return The DynamicStatisticsDefinition with the specified name, or null if not found.
	 */
	public static DynamicStatisticsDefinition getByDefaultName(String name) {
		Optional<DynamicStatisticsDefinition> property = Arrays.stream(DynamicStatisticsDefinition.values()).filter(item -> item.getDefaultName().equalsIgnoreCase(name)).findFirst();
		return property.orElse(null);
	}
}
