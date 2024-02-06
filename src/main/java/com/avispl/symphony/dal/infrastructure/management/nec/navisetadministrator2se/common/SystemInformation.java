/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common;

/**
 * Enumeration representing different system information types.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/15/2024
 * @since 1.0.0
 */
public enum SystemInformation {
	VERSION("Version", "version"),
	BUILD("BuildNumber", "buildnum"),
	LANGUAGE("Language", "language"),
	WEB_BUILD_NUMBER("WebBuildNumber", "webbuildnum"),
	LICENSE_STATUS("LicenseStatus", "licensestatusmsg"),
	PROJECTOR_PROFILE("ProjectorProfile", "pjprofileversion"),
	;
	private final String name;
	private final String value;

	/**
	 * Constructor for SystemInfo.
	 *
	 * @param name The name representing the system information category.
	 * @param value The corresponding value associated with the category.
	 */
	SystemInformation(String name, String value) {
		this.name = name;
		this.value = value;
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
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}
}
