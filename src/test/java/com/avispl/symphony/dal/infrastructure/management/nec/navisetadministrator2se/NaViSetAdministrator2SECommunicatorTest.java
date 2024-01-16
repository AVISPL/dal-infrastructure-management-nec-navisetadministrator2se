/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * NaViSetAdministrator2SECommunicatorTest
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/8/2023
 * @since 1.0.0
 */
public class NaViSetAdministrator2SECommunicatorTest {
	private NaViSetAdministrator2SECommunicator naViSetAdministrator2SECommunicator;

	@BeforeEach
	void setUp() throws Exception {
		naViSetAdministrator2SECommunicator = new NaViSetAdministrator2SECommunicator();
		naViSetAdministrator2SECommunicator.setHost("");
		naViSetAdministrator2SECommunicator.setLogin("");
		naViSetAdministrator2SECommunicator.setPassword("");
		naViSetAdministrator2SECommunicator.setPort(80);
		naViSetAdministrator2SECommunicator.init();
		naViSetAdministrator2SECommunicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		naViSetAdministrator2SECommunicator.disconnect();
		naViSetAdministrator2SECommunicator.destroy();
	}
}
