/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se;

import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

/**
 * NaViSetAdministrator2SECommunicatorTest
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/15/2024
 * @since 1.0.0
 */
public class NaViSetAdministrator2SECommunicatorTest {
	private NaViSetAdministrator2SECommunicator naViSetAdministrator2SECommunicator;

	private ExtendedStatistics extendedStatistic;

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

	@Test
	void testGetAggregatorData() throws Exception {
		extendedStatistic = (ExtendedStatistics) naViSetAdministrator2SECommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(6, statistics.size());
	}

	@Test
	void testSystemInformation() throws Exception {
		extendedStatistic = (ExtendedStatistics) naViSetAdministrator2SECommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals("230829", statistics.get("BuildNumber"));
		Assert.assertEquals("en", statistics.get("Language"));
		Assert.assertEquals("3.0.38", statistics.get("ProjectorProfile"));
		Assert.assertEquals("2.2.01 (Build 230829)", statistics.get("Version"));
		Assert.assertEquals("230829", statistics.get("WebBuildNumber"));
	}
}
