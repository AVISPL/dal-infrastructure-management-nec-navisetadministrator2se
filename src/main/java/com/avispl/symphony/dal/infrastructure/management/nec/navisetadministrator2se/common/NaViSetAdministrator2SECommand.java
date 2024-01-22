/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common;

/**
 * Utility class containing constant commands for interacting with NaViSetAdministrator2SE API.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/15/2024
 * @since 1.0.0
 */
public class NaViSetAdministrator2SECommand {
	public static final String LOGIN_COMMAND = "api/authenticate";
	public static final String DEVICE_ID_COMMAND = "api/tree";
	public static final String DEVICE_TYPE_NAME_COMMAND = "api/devicetypenames";
	public static final String SYSTEM_INFO_COMMAND = "api/about";
	public static final String DEVICE_TAB_COMMAND = "api/devicetabs?id=%s";
	public static final String CONTROL_COMMAND = "api/devicecontrol";
}
