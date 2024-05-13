/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration representing various changed monitor properties with their default and alternative names.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/19/2024
 * @since 1.0.0
 */
public enum ControllablePropertyEnum {
	POWER("PowerState", NaViSetAdministrator2SEConstant.CONTROL_GROUP, "65545"),
	INPUT("VideoInput", NaViSetAdministrator2SEConstant.CONTROL_GROUP, "96"),
	VOLUME("AudioVolume", NaViSetAdministrator2SEConstant.CONTROL_GROUP, "98"),
	;
	private final String propertyName;
	private final String group;
	private final String code;

	/**
	 * Constructor for ControllablePropertyEnum.
	 *
	 * @param defaultName The default name of the property.
	 * @param propertyName The name of the property.
	 * @param code The code of the control.
	 */
	ControllablePropertyEnum(String defaultName, String propertyName, String code) {
		this.propertyName = defaultName;
		this.group = propertyName;
		this.code = code;
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
	 * Retrieves {@link #group}
	 *
	 * @return value of {@link #group}
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Retrieves {@link #code}
	 *
	 * @return value of {@link #code}
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Retrieve a ChangedMonitorPropertyEnum by its name.
	 *
	 * @param name The default name to search for.
	 * @return The ChangedMonitorPropertyEnum with the specified default name, or null if not found.
	 */
	public static ControllablePropertyEnum getByDefaultName(String name) {
		Optional<ControllablePropertyEnum> property = Arrays.stream(ControllablePropertyEnum.values()).filter(item -> item.getPropertyName().equalsIgnoreCase(name)).findFirst();
		return property.orElse(null);
	}
}
