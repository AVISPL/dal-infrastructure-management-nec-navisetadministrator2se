/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;

/**
 * NaViSetAdministrator2SECommunicatorTest includes the unit test for NaViSetAdministrator2SECommunicator
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

	/**
	 * Test case to verify the correctness of getting aggregator data.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetAggregatorData() throws Exception {
		extendedStatistic = (ExtendedStatistics) naViSetAdministrator2SECommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(6, statistics.size());
	}

	/**
	 * Test case for retrieving system information.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
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

	/**
	 * Test case to verify the correctness of retrieving multiple statistics.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatistics() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(2, aggregatedDeviceList.size());
	}

	/**
	 * Test case for retrieving multiple statistics with historical properties set to "TemperatureIntake(C), TemperatureExhaust(C)".
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithHistorical() throws Exception {
		naViSetAdministrator2SECommunicator.setHistoricalProperties("TemperatureIntake(C), TemperatureExhaust(C)");
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(2, aggregatedDeviceList.size());
	}

	/**
	 * Test case for retrieving multiple statistics with other historical properties set to "TemperatureSensor1(C), TemperatureSensor2(C)".
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithOtherHistorical() throws Exception {
		naViSetAdministrator2SECommunicator.setHistoricalProperties("TemperatureSensor1(C), TemperatureSensor2(C)");
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(3, aggregatedDeviceList.size());
	}

	/**
	 * Test case for retrieving multiple statistics with general group information.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithGeneralGroup() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		String deviceId = "4";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("PJ-2800089LN", stats.get("AssetTag"));
			Assert.assertEquals("RJ45 Connection to LAN", stats.get("CommunicationLink"));
			Assert.assertEquals("Normal", stats.get("Diagnostics"));
			Assert.assertEquals("1.04.039", stats.get("FirmwareVersion"));
			Assert.assertEquals("172.31.254.173", stats.get("IPAddress"));
			Assert.assertEquals("d4-92-34-56-aa-3f", stats.get("MACAddress"));
			Assert.assertEquals("8/1/2022", stats.get("ManufactureDate"));
			Assert.assertEquals("NEC", stats.get("Manufacturer"));
			Assert.assertEquals("2800089LN", stats.get("SerialNumber"));
		}
	}

	/**
	 * Test case for retrieving multiple statistics with audio group information.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithAudioGroup() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		String deviceId = "4";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("Unmute", stats.get("Mute"));
			Assert.assertEquals("13 (0 — 31)", stats.get("Volume"));
		}
	}

	/**
	 * Test case for retrieving multiple statistics with ECO group information.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithECOGroup() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		String deviceId = "4";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("Off", stats.get("ConstantBrightness"));
			Assert.assertEquals("Normal", stats.get("LightECOMode"));
			Assert.assertEquals("100 (50 — 100)", stats.get("LightModeAdjust"));
		}
	}

	/**
	 * Test case for retrieving multiple statistics with geometry group information.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithGeometryGroup() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		String deviceId = "4";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("Full", stats.get("AspectRatio"));
			Assert.assertEquals("Off", stats.get("GeometricCorrectionMode"));
			Assert.assertEquals("Off", stats.get("HardwareEdgeBlending"));
			Assert.assertEquals("Auto", stats.get("ProjectorOrientation"));
		}
	}

	/**
	 * Test case for retrieving multiple statistics with power group information.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithPowerGroup() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		String deviceId = "4";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("Auto", stats.get("FanMode"));
			Assert.assertEquals("Standard (Limited LAN access)", stats.get("SaveLevelInStandbyMode"));
		}
	}

	/**
	 * Test case for retrieving multiple statistics with video group information.
	 *
	 * @throws Exception If an error occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithVideoGroup() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		String deviceId = "4";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("VGA", stats.get("Input"));
			Assert.assertEquals("Unmute", stats.get("OnscreenMute"));
			Assert.assertEquals("Off", stats.get("PictureFreeze"));
			Assert.assertEquals("Unmute", stats.get("PictureMute"));
			Assert.assertEquals("Presentation", stats.get("PicturePreset"));
		}
	}

	@Test
	void testVolumeControl() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(60000);
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Controls#AudioVolume";
		String value = "16.0";
		String deviceId = "4";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		naViSetAdministrator2SECommunicator.controlProperty(controllableProperty);

		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	@Test
	void testPowerControl() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(60000);
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Controls#PowerState";
		String value = "1";
		String deviceId = "4";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		naViSetAdministrator2SECommunicator.controlProperty(controllableProperty);

		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	@Test
	void testVideoInput() throws Exception {
		naViSetAdministrator2SECommunicator.getMultipleStatistics();
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Thread.sleep(60000);
		naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Controls#VideoInput";
		String value = "1";
		String deviceId = "4";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		naViSetAdministrator2SECommunicator.controlProperty(controllableProperty);

		List<AggregatedDevice> aggregatedDeviceList = naViSetAdministrator2SECommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}
}
