/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.security.auth.login.FailedLoginException;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.ControllablePropertyEnum;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.MonitorPropertyEnum;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.NaViSetAdministrator2SECommand;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.NaViSetAdministrator2SEConstant;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.SystemInformation;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.dto.InputValueDTO;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.dto.VolumeValueDTO;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.statistics.DynamicStatisticsDefinition;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * NaViSetAdministrator2SECommunicator
 * Supported features are:
 * Monitoring Aggregator Device:
 *  <ul>
 *  <li> - BuildNumber</li>
 *  <li> - Language</li>
 *  <li> - LicenseStatus</li>
 *  <li> - ProjectorProfile</li>
 *  <li> - Version</li>
 *  <li> - WebBuildNumber</li>
 *  <ul>
 *
 * General Info Aggregated Device:
 * <ul>
 * <li> - AssetTag</li>
 * <li> - CarbonSavings(kgCO2)</li>
 * <li> - CarbonSavingsTotal(kgCO2)</li>
 * <li> - CommunicationLink</li>
 * <li> - deviceId</li>
 * <li> - deviceModel</li>
 * <li> - deviceName</li>
 * <li> - deviceOnline</li>
 * <li> - Diagnostics</li>
 * <li> - FirmwareVersion</li>
 * <li> - FrequencyHorizontal(kHz)</li>
 * <li> - FrequencyVertical(Hz)</li>
 * <li> - IPAddress</li>
 * <li> - LastRefresh</li>
 * <li> - MACAddress</li>
 * <li> - ManufactureDate</li>
 * <li> - Manufacturer</li>
 * <li> - PowerOnTime</li>
 * <li> - SerialNumber</li>
 * <li> - TemperatureExhaust(C)</li>
 * <li> - TemperatureIntake(C)</li>
 * <li> - FrequencyVertical(Hz)</li>
 * <li> - TimeLampUsage(hours)</li>
 * <li> - TimePanelUsage(hours)</li>
 * </ul>
 *
 * Controls Group:
 * <ul>
 * <li> - AudioMute</li>
 * <li> - VideoInput</li>
 * <li> - PowerState</li>
 * </ul>
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/15/2024
 * @since 1.0.0
 */

public class NaViSetAdministrator2SECommunicator extends RestCommunicator implements Aggregator, Monitorable, Controller {

	/**
	 * Process that is running constantly and triggers collecting data from NaViSet Administrator SE API endpoints, based on the given timeouts and thresholds.
	 *
	 * @author Harry
	 * @since 1.0.0
	 */
	class NavisetDataLoader implements Runnable {
		private volatile boolean inProgress;
		private volatile boolean flag = false;

		public NavisetDataLoader() {
			inProgress = true;
		}

		@Override
		public void run() {
			loop:
			while (inProgress) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					// Ignore for now
				}

				if (!inProgress) {
					break loop;
				}

				// next line will determine whether Naviset monitoring was paused
				updateAggregatorStatus();
				if (devicePaused) {
					continue loop;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Fetching other than aggregated device list");
				}
				long currentTimestamp = System.currentTimeMillis();
				if (!flag && nextDevicesCollectionIterationTimestamp <= currentTimestamp) {
					populateDeviceDetails();
					flag = true;
				}

				while (nextDevicesCollectionIterationTimestamp > System.currentTimeMillis()) {
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						//
					}
				}

				if (!inProgress) {
					break loop;
				}
				if (flag) {
					nextDevicesCollectionIterationTimestamp = System.currentTimeMillis() + 30000;
					flag = false;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Finished collecting devices statistics cycle at " + new Date());
				}
			}
			// Finished collecting
		}

		/**
		 * Triggers main loop to stop
		 */
		public void stop() {
			inProgress = false;
		}
	}

	/**
	 * Private variable representing the local extended statistics.
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * A private final ReentrantLock instance used to provide exclusive access to a shared resource
	 * that can be accessed by multiple threads concurrently. This lock allows multiple reentrant
	 * locks on the same shared resource by the same thread.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * List of device ID
	 */
	private List<String> deviceIdList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Map of monitoring aggregated device
	 */
	private Map<String, Map<String, String>> cachedMonitoringDevice = Collections.synchronizedMap(new HashMap<>());

	/**
	 * A mapper for reading and writing JSON using Jackson library.
	 * ObjectMapper provides functionality for converting between Java objects and JSON.
	 * It can be used to serialize objects to JSON format, and deserialize JSON data to objects.
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Synchronized map containing video input values.
	 */
	private Map<String, List<InputValueDTO>> videoInputValues = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Synchronized map containing audio volume values.
	 */
	private Map<String, VolumeValueDTO> audioVolumeValues = Collections.synchronizedMap(new HashMap<>());

	/**
	 * List of aggregated device
	 */
	private List<AggregatedDevice> aggregatedDeviceList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * check control
	 */
	private boolean checkControl;

	/**
	 * Indicates whether a device is considered as paused.
	 * True by default so if the system is rebooted and the actual value is lost -> the device won't start stats
	 * collection unless the {@link NaViSetAdministrator2SECommunicator#retrieveMultipleStatistics()} method is called which will change it
	 * to a correct value
	 */
	private volatile boolean devicePaused = true;

	/**
	 * We don't want the statistics to be collected constantly, because if there's not a big list of devices -
	 * new devices' statistics loop will be launched before the next monitoring iteration. To avoid that -
	 * this variable stores a timestamp which validates it, so when the devices' statistics is done collecting, variable
	 * is set to currentTime + 30s, at the same time, calling {@link #retrieveMultipleStatistics()} and updating the
	 */
	private long nextDevicesCollectionIterationTimestamp;

	/**
	 * This parameter holds timestamp of when we need to stop performing API calls
	 * It used when device stop retrieving statistic. Updated each time of called #retrieveMultipleStatistics
	 */
	private volatile long validRetrieveStatisticsTimestamp;

	/**
	 * Aggregator inactivity timeout. If the {@link NaViSetAdministrator2SECommunicator#retrieveMultipleStatistics()}  method is not
	 * called during this period of time - device is considered to be paused, thus the Cloud API
	 * is not supposed to be called
	 */
	private static final long retrieveStatisticsTimeOut = 3 * 60 * 1000;

	/**
	 * Executor that runs all the async operations, that is posting and
	 */
	private ExecutorService executorService;

	/**
	 * A private field that represents an instance of the NavisetDataLoader class, which is responsible for loading device data for Naviset
	 */
	private NavisetDataLoader deviceDataLoader;

	/**
	 * A JSON node containing the response from an aggregator.
	 */
	private JsonNode aggregatorResponse;

	/**
	 * API Token
	 */
	private String token;

	/**
	 * Configurable property for historical properties, comma separated values kept as set locally
	 */
	private Set<String> historicalProperties = new HashSet<>();

	/**
	 * Retrieves {@link #historicalProperties}
	 *
	 * @return value of {@link #historicalProperties}
	 */
	public String getHistoricalProperties() {
		return String.join(",", this.historicalProperties);
	}

	/**
	 * Sets {@link #historicalProperties} value
	 *
	 * @param historicalProperties new value of {@link #historicalProperties}
	 */
	public void setHistoricalProperties(String historicalProperties) {
		this.historicalProperties.clear();
		Arrays.asList(historicalProperties.split(",")).forEach(propertyName -> {
			this.historicalProperties.add(propertyName.trim());
		});
	}

	/**
	 * Update the status of the device.
	 * The device is considered as paused if did not receive any retrieveMultipleStatistics()
	 * calls during {@link NaViSetAdministrator2SECommunicator}
	 */
	private synchronized void updateAggregatorStatus() {
		devicePaused = validRetrieveStatisticsTimestamp < System.currentTimeMillis();
	}

	/**
	 * Uptime time stamp to valid one
	 */
	private synchronized void updateValidRetrieveStatisticsTimestamp() {
		validRetrieveStatisticsTimestamp = System.currentTimeMillis() + retrieveStatisticsTimeOut;
		updateAggregatorStatus();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * Check for available devices before retrieving the value
	 * ping latency information to Symphony
	 */
	@Override
	public int ping() throws Exception {
		if (isInitialized()) {
			long pingResultTotal = 0L;

			for (int i = 0; i < this.getPingAttempts(); i++) {
				long startTime = System.currentTimeMillis();

				try (Socket puSocketConnection = new Socket(this.host, this.getPort())) {
					puSocketConnection.setSoTimeout(this.getPingTimeout());
					if (puSocketConnection.isConnected()) {
						long pingResult = System.currentTimeMillis() - startTime;
						pingResultTotal += pingResult;
						if (this.logger.isTraceEnabled()) {
							this.logger.trace(String.format("PING OK: Attempt #%s to connect to %s on port %s succeeded in %s ms", i + 1, host, this.getPort(), pingResult));
						}
					} else {
						if (this.logger.isDebugEnabled()) {
							logger.debug(String.format("PING DISCONNECTED: Connection to %s did not succeed within the timeout period of %sms", host, this.getPingTimeout()));
						}
						return this.getPingTimeout();
					}
				} catch (SocketTimeoutException | ConnectException tex) {
					throw new SocketTimeoutException("Socket connection timed out");
				} catch (UnknownHostException tex) {
					throw new SocketTimeoutException("Socket connection timed out" + tex.getMessage());
				} catch (Exception e) {
					if (this.logger.isWarnEnabled()) {
						this.logger.warn(String.format("PING TIMEOUT: Connection to %s did not succeed, UNKNOWN ERROR %s: ", host, e.getMessage()));
					}
					return this.getPingTimeout();
				}
			}
			return Math.max(1, Math.toIntExact(pingResultTotal / this.getPingAttempts()));
		} else {
			throw new IllegalStateException("Cannot use device class without calling init() first");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		reentrantLock.lock();
		try {
			if (!checkValidCookieSession()) {
				throw new FailedLoginException("Please enter valid password and username field.");
			}
			Map<String, String> statistics = new HashMap<>();
			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			retrieveSystemInfo();
			populateSystemInfo(statistics);
			extendedStatistics.setStatistics(statistics);
			localExtendedStatistics = extendedStatistics;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		String property = controllableProperty.getProperty();
		String deviceId = controllableProperty.getDeviceId();
		String value = String.valueOf(controllableProperty.getValue());

		String[] propertyList = property.split(NaViSetAdministrator2SEConstant.HASH);
		String propertyName = property;
		if (property.contains(NaViSetAdministrator2SEConstant.HASH)) {
			propertyName = propertyList[1];
		}
		reentrantLock.lock();
		try {
			Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
			if (aggregatedDevice.isPresent()) {
				Map<String, String> stats = aggregatedDevice.get().getProperties();
				List<AdvancedControllableProperty> advancedControllableProperties = aggregatedDevice.get().getControllableProperties();
				ControllablePropertyEnum item = ControllablePropertyEnum.getByDefaultName(propertyName);
				boolean controlPropagated = true;
				switch (item) {
					case VOLUME:
						value = String.valueOf((int) Float.parseFloat(value));
						sendControlCommand(deviceId, item.getCode(), propertyName, value, value);
						stats.put(property + NaViSetAdministrator2SEConstant.CURRENT_VALUE, value);
						updateCachedValue(deviceId, property, value);
						checkControl = true;
						break;
					case INPUT:
						List<InputValueDTO> inputValues = videoInputValues.get(deviceId);
						String requestValue = getValueByName(inputValues, value);
						if (!NaViSetAdministrator2SEConstant.NONE.equalsIgnoreCase(requestValue)) {
							sendControlCommand(deviceId, item.getCode(), propertyName, requestValue, value);
							updateCachedValue(deviceId, property, requestValue);
							checkControl = true;
						} else {
							controlPropagated = false;
						}
						break;
					case POWER:
						sendControlCommand(deviceId, item.getCode(), propertyName, value, value);
						updateCachedValue(deviceId, property, value);
						String audioPropertyName = ControllablePropertyEnum.VOLUME.getGroup() + ControllablePropertyEnum.VOLUME.getPropertyName();
						if (NaViSetAdministrator2SEConstant.ZERO.equals(value)) {
							removeValueForTheControllableProperty(stats, advancedControllableProperties, audioPropertyName);
							stats.remove(audioPropertyName + NaViSetAdministrator2SEConstant.CURRENT_VALUE);
						} else {
							retrieveAudioVolume(deviceId);
							String audioValue = cachedMonitoringDevice.get(deviceId).get(audioPropertyName);
							VolumeValueDTO volumeValue = audioVolumeValues.get(deviceId);
							String minValue = volumeValue.getMinValue();
							String maxValue = volumeValue.getMaxValue();
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createSlider(stats, propertyName, minValue, maxValue, Float.parseFloat(minValue), Float.parseFloat(maxValue), Float.parseFloat(audioValue)), audioValue);
							stats.put(propertyName + "CurrentValue", audioValue);
						}
						break;
					default:
						if (logger.isWarnEnabled()) {
							logger.warn(String.format("Unable to execute %s command on device %s: Not Supported", property, deviceId));
						}
						controlPropagated = false;
						break;
				}
				if (controlPropagated) {
					updateLocalControlValue(stats, advancedControllableProperties, property, value);
					updateListAggregatedDevice(deviceId, stats, advancedControllableProperties);
				}
			} else {
				throw new IllegalArgumentException(String.format("Unable to control property: %s as the device does not exist.", property));
			}
		} finally {
			reentrantLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
		if (CollectionUtils.isEmpty(controllableProperties)) {
			throw new IllegalArgumentException("ControllableProperties can not be null or empty");
		}
		for (ControllableProperty p : controllableProperties) {
			try {
				controlProperty(p);
			} catch (Exception e) {
				logger.error(String.format("Error when control property %s", p.getProperty()), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {
		if (!checkValidCookieSession()) {
			throw new FailedLoginException("Please enter valid password and username field.");
		}
		if (executorService == null) {
			executorService = Executors.newFixedThreadPool(1);
			executorService.submit(deviceDataLoader = new NavisetDataLoader());
		}
		nextDevicesCollectionIterationTimestamp = System.currentTimeMillis();
		updateValidRetrieveStatisticsTimestamp();
		if (cachedMonitoringDevice.isEmpty()) {
			return Collections.emptyList();
		}
		return cloneAndPopulateAggregatedDeviceList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
		return retrieveMultipleStatistics().stream().filter(aggregatedDevice -> list.contains(aggregatedDevice.getDeviceId())).collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void authenticate() throws Exception {
		// Naviset Administrator 2SE aggregator only require API token for each request.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal init is called.");
		}
		executorService = Executors.newFixedThreadPool(1);
		executorService.submit(deviceDataLoader = new NavisetDataLoader());
		super.internalInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal destroy is called.");
		}
		if (deviceDataLoader != null) {
			deviceDataLoader.stop();
			deviceDataLoader = null;
		}
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
		}
		if (localExtendedStatistics != null && localExtendedStatistics.getStatistics() != null && localExtendedStatistics.getControllableProperties() != null) {
			localExtendedStatistics.getStatistics().clear();
			localExtendedStatistics.getControllableProperties().clear();
		}
		nextDevicesCollectionIterationTimestamp = 0;
		deviceIdList.clear();
		aggregatedDeviceList.clear();
		cachedMonitoringDevice.clear();
		super.internalDestroy();
	}

	/**
	 * {@inheritDoc}
	 * set cookie into Header of Request
	 */
	@Override
	protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) {
		headers.setBearerAuth(token);
		return headers;
	}

	/**
	 * Check API token validation
	 * If the token expires, we send a request to get a new token
	 *
	 * @return boolean
	 */
	private boolean checkValidCookieSession() throws Exception {
		JsonNode response = null;
		try {
			//Send request to check valid cookie
			response = this.doGet(NaViSetAdministrator2SECommand.DEVICE_ID_COMMAND, JsonNode.class);
		} catch (FailedLoginException e) {
			token = getCookieSession();
			if (NaViSetAdministrator2SEConstant.EMPTY.equals(token)) {
				return false;
			}
		}
		if (response == null || !response.has(NaViSetAdministrator2SEConstant.DATA)) {
			response = this.doGet(NaViSetAdministrator2SECommand.DEVICE_ID_COMMAND, JsonNode.class);
		}
		getDeviceIdList(response);
		return true;
	}

	/**
	 * Retrieves a token using the provided username and password
	 *
	 * @return the token string
	 */
	private String getCookieSession() throws Exception {
		String authenToken = NaViSetAdministrator2SEConstant.EMPTY;
		try {
			Map<String, String> credentials = new HashMap<>();
			credentials.put(NaViSetAdministrator2SEConstant.NAME, this.getLogin());
			credentials.put(NaViSetAdministrator2SEConstant.PASSWORD, this.getPassword());
			JsonNode response = this.doPost(NaViSetAdministrator2SECommand.LOGIN_COMMAND, credentials, JsonNode.class);
			if (response != null && response.has(NaViSetAdministrator2SEConstant.DATA) && response.get(NaViSetAdministrator2SEConstant.DATA).has(NaViSetAdministrator2SEConstant.TOKEN)) {
				authenToken = response.get(NaViSetAdministrator2SEConstant.DATA).get(NaViSetAdministrator2SEConstant.TOKEN).asText();
			}
		} catch (Exception e) {
			throw new FailedLoginException("Failed to retrieve the cookie for account with from username and password");
		}
		return authenToken;
	}

	/**
	 * Extracts device IDs from the JSON response and populates the deviceIdList.
	 *
	 * @param response The JSON response containing device information.
	 */
	private void getDeviceIdList(JsonNode response) {
		deviceIdList.clear();
		if (!response.has(NaViSetAdministrator2SEConstant.DATA)) {
			return;
		}
		for (JsonNode item : response.get(NaViSetAdministrator2SEConstant.DATA)) {
			if (item.has(NaViSetAdministrator2SEConstant.DEVICE_ID) && !NaViSetAdministrator2SEConstant.ZERO.equals(item.get(NaViSetAdministrator2SEConstant.DEVICE_ID).asText())) {
				deviceIdList.add(item.get(NaViSetAdministrator2SEConstant.DEVICE_ID).asText());
			}
		}
	}

	/**
	 * Sends a control command to the specified device.
	 *
	 * @param deviceId The unique identifier of the target device.
	 * @param code The VCP code associated with the control property.
	 * @param name The name of the control property.
	 * @param value The numerical value to set for the control property.
	 * @param textValue The textual representation of the value (for error reporting).
	 * @throws IllegalArgumentException If an error occurs during the control command execution.
	 */
	private void sendControlCommand(String deviceId, String code, String name, String value, String textValue) {
		try {
			ObjectNode body = objectMapper.createObjectNode();
			body.put("id", deviceId);
			body.put("vcpcode", code);
			body.put("vcpvalue", value);
			JsonNode response = this.doPut(NaViSetAdministrator2SECommand.CONTROL_COMMAND, (JsonNode) body, JsonNode.class);
			if (response == null || !response.has(NaViSetAdministrator2SEConstant.DATA)) {
				throw new IllegalArgumentException("Error setting a control on the device.");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Can't control property %s with value %s", name, textValue), e);
		}
	}

	/**
	 * Retrieves system information using a GET request to the specified command.
	 * The result is stored in the aggregatorResponse field.
	 *
	 * @throws IllegalArgumentException If the response does not contain the expected data field
	 * or if there is an error during the retrieval process.
	 */
	private void retrieveSystemInfo() {
		try {
			aggregatorResponse = this.doGet(NaViSetAdministrator2SECommand.SYSTEM_INFO_COMMAND, JsonNode.class);
			if (!aggregatorResponse.has(NaViSetAdministrator2SEConstant.DATA)) {
				throw new IllegalArgumentException("The response is not correct.");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error while retrieve system information" + e);
		}
	}

	/**
	 * Populates the provided stats map with system information extracted from the aggregator response.
	 * The mapping is based on the SystemInformation enum.
	 *
	 * @param stats The map to populate with system information.
	 */
	private void populateSystemInfo(Map<String, String> stats) {
		for (SystemInformation item : SystemInformation.values()) {
			String propertyName = item.getName();
			String value = getDefaultValueForNullData(aggregatorResponse.get(NaViSetAdministrator2SEConstant.DATA).get(item.getValue()).asText());
			stats.put(propertyName, value);
		}
	}

	/**
	 * Populates device details using a multithreaded approach by retrieving aggregated data for each device ID.
	 * The number of threads is set to 8, and a thread pool is utilized to parallelize the retrieval process.
	 * The results are printed to the console, and the thread pool is shutdown upon completion.
	 */
	private void populateDeviceDetails() {
		int numberOfThreads = 8;
		ExecutorService executorServiceForRetrieveAggregatedData = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<?>> futures = new ArrayList<>();

		synchronized (deviceIdList) {
			for (String deviceId : deviceIdList) {
				Future<?> future = executorServiceForRetrieveAggregatedData.submit(() -> processDeviceId(deviceId));
				futures.add(future);
			}
		}
		waitForFutures(futures, executorServiceForRetrieveAggregatedData);
		executorServiceForRetrieveAggregatedData.shutdown();
	}

	/**
	 * Waits for the completion of all futures in the provided list and then shuts down the executor service.
	 *
	 * @param futures The list of Future objects representing asynchronous tasks.
	 * @param executorService The ExecutorService to be shut down.
	 */
	private void waitForFutures(List<Future<?>> futures, ExecutorService executorService) {
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		executorService.shutdown();
	}

	/**
	 * Processes the specified device ID by retrieving and handling tab data for the device.
	 * This method prints the device ID to the console, removes cached data for the device,
	 * and retrieves information based on the device tabs using corresponding commands.
	 *
	 * @param deviceId The ID of the device to be processed.
	 */
	private void processDeviceId(String deviceId) {
		retrieveDeviceInfo(deviceId);
		retrieveControlData(deviceId);
	}

	/**
	 * Retrieves device information for the specified device ID using the given command.
	 * The retrieved information is processed and stored in the cachedMonitoringDevice map.
	 *
	 * @param deviceId The ID of the device for which information is to be retrieved.
	 */
	private void retrieveDeviceInfo(String deviceId) {
		try {
			JsonNode response = this.doGet(String.format(NaViSetAdministrator2SECommand.DEVICE_INFO_COMMAND, deviceId), JsonNode.class);
			if (response != null && response.has(NaViSetAdministrator2SEConstant.DATA) && response.get(NaViSetAdministrator2SEConstant.DATA).has(NaViSetAdministrator2SEConstant.TABLES)) {
				Map<String, String> mappingValue = new HashMap<>();
				mappingValue.put(NaViSetAdministrator2SEConstant.LAST_REFRESH, response.get(NaViSetAdministrator2SEConstant.DATA).get(NaViSetAdministrator2SEConstant.TIME).asText());
				JsonNode deviceInfo = response.get(NaViSetAdministrator2SEConstant.DATA).get(NaViSetAdministrator2SEConstant.TABLES);
				for (JsonNode item : deviceInfo) {
					JsonNode propertiesNode = item.get(NaViSetAdministrator2SEConstant.PROPERTIES);
					if (NaViSetAdministrator2SEConstant.CONTROL_SETTINGS.equalsIgnoreCase(item.get(NaViSetAdministrator2SEConstant.NAME).asText())) {
						for (JsonNode property : propertiesNode) {
							String name = cleanPropertyName(property.get(NaViSetAdministrator2SEConstant.PROP_NAME).asText());
							String value = property.get(NaViSetAdministrator2SEConstant.PROP_VALUE).asText();
							MonitorPropertyEnum monitorPropertyEnum = MonitorPropertyEnum.getByDefaultName(getPropertyName(name));
							String group = NaViSetAdministrator2SEConstant.OTHER_GROUP;
							if (monitorPropertyEnum != null) {
								if (monitorPropertyEnum == MonitorPropertyEnum.POWER_STATE || monitorPropertyEnum == MonitorPropertyEnum.VIDEO_INPUT || monitorPropertyEnum == MonitorPropertyEnum.AUDIO_VOLUME) {
									continue;
								}
								name = monitorPropertyEnum.getPropertyName();
								group = monitorPropertyEnum.getGroup();
							}
							mappingValue.put(group + name, value);
						}
					} else {
						for (JsonNode property : propertiesNode) {
							String name = cleanPropertyName(property.get(NaViSetAdministrator2SEConstant.PROP_NAME).asText());
							String value = property.get(NaViSetAdministrator2SEConstant.PROP_VALUE).asText();
							mappingValue.put(name, value);
						}
					}
				}
				putMapIntoCachedData(deviceId, mappingValue);
			}
		} catch (Exception e) {
			logger.debug(String.format("Error when retrieve device info by id %s", deviceId), e);
		}
	}

	/**
	 * Retrieves control data for the specified device and updates the cached values accordingly.
	 *
	 * @param deviceId The identifier of the device.
	 */
	private void retrieveControlData(String deviceId) {
		for (ControllablePropertyEnum item : ControllablePropertyEnum.values()) {
			try {
				JsonNode response = this.doGet(String.format(NaViSetAdministrator2SECommand.CONTROL_DATA_COMMAND, deviceId, item.getCode()), JsonNode.class);
				if (response != null && response.has(NaViSetAdministrator2SEConstant.DATA) && response.get(NaViSetAdministrator2SEConstant.DATA).has(NaViSetAdministrator2SEConstant.CONTROL)) {
					JsonNode nodeInfo = response.get(NaViSetAdministrator2SEConstant.DATA).get(NaViSetAdministrator2SEConstant.CONTROL);
					Map<String, String> mapValue = new HashMap<>();
					mapValue.put(item.getGroup() + item.getPropertyName(), nodeInfo.get(NaViSetAdministrator2SEConstant.VCP_VALUE).asText());
					putMapIntoCachedData(deviceId, mapValue);
					switch (item) {
						case INPUT:
							List<InputValueDTO> values = objectMapper.readValue(nodeInfo.get(NaViSetAdministrator2SEConstant.VALUES).toString(), new TypeReference<List<InputValueDTO>>() {
							});
							videoInputValues.put(deviceId, values);
							break;
						case VOLUME:
							String minValue = nodeInfo.get(NaViSetAdministrator2SEConstant.MIN_VALUE).asText();
							String maxValue = nodeInfo.get(NaViSetAdministrator2SEConstant.MAX_VALUE).asText();
							audioVolumeValues.put(deviceId, new VolumeValueDTO(minValue, maxValue));
							break;
						default:
							break;
					}
				}
				//Sleep after sending request
				Thread.sleep(1000);
			} catch (Exception e) {
				logger.debug(String.format("Error when retrieve %s with id %s", item.getPropertyName(), deviceId), e);
			}
		}
	}

	/**
	 * Retrieves audio volume information for the specified device and updates the cached data.
	 *
	 * @param deviceId The identifier of the device.
	 */
	private void retrieveAudioVolume(String deviceId) {
		try {
			JsonNode response = this.doGet(String.format(NaViSetAdministrator2SECommand.CONTROL_DATA_COMMAND, deviceId, ControllablePropertyEnum.VOLUME.getCode()), JsonNode.class);
			if (response != null && response.has(NaViSetAdministrator2SEConstant.DATA) && response.get(NaViSetAdministrator2SEConstant.DATA).has(NaViSetAdministrator2SEConstant.CONTROL)) {
				JsonNode nodeInfo = response.get(NaViSetAdministrator2SEConstant.DATA).get(NaViSetAdministrator2SEConstant.CONTROL);
				Map<String, String> mapValue = new HashMap<>();
				mapValue.put(ControllablePropertyEnum.VOLUME.getGroup() + ControllablePropertyEnum.VOLUME.getPropertyName(), nodeInfo.get(NaViSetAdministrator2SEConstant.VCP_VALUE).asText());
				putMapIntoCachedData(deviceId, mapValue);
				String minValue = nodeInfo.get(NaViSetAdministrator2SEConstant.MIN_VALUE).asText();
				String maxValue = nodeInfo.get(NaViSetAdministrator2SEConstant.MAX_VALUE).asText();
				audioVolumeValues.put(deviceId, new VolumeValueDTO(minValue, maxValue));
			}
		} catch (
				Exception e) {
			logger.debug(String.format("Error when retrieve %s with id %s", ControllablePropertyEnum.VOLUME.getPropertyName(), deviceId), e);
		}
	}

	/**
	 * Removes spaces from the provided string by replacing them with an empty string.
	 *
	 * @param value The string from which spaces are to be removed.
	 * @return The resulting string with spaces removed.
	 */
	private String cleanPropertyName(String value) {
		return value.replaceAll(NaViSetAdministrator2SEConstant.SPACE, NaViSetAdministrator2SEConstant.EMPTY)
				.replaceAll(NaViSetAdministrator2SEConstant.HYPHEN, NaViSetAdministrator2SEConstant.EMPTY)
				.replace(".", NaViSetAdministrator2SEConstant.EMPTY);
	}

	/**
	 * Retrieves the controllable name from the given input string.
	 * If the input contains a hyphen, it extracts the part before the hyphen.
	 * The resulting name is trimmed and spaces are removed.
	 *
	 * @param input The input string from which to extract the controllable name.
	 * @return The controllable name extracted from the input string.
	 */
	private String getPropertyName(String input) {
		if (input.contains(NaViSetAdministrator2SEConstant.HYPHEN)) {
			input = input.split(NaViSetAdministrator2SEConstant.HYPHEN)[0];
		}
		input = cleanPropertyName(input.trim());
		if (input.matches(".*\\([^)]+\\)$")) {
			return input.replaceFirst("\\([^)]+\\)$", NaViSetAdministrator2SEConstant.EMPTY);
		}
		return input;
	}

	/**
	 * Puts the provided mapping values into the cached monitoring data for the specified device ID.
	 *
	 * @param deviceId The ID of the device.
	 * @param mappingValue The mapping values to be added.
	 */
	private void putMapIntoCachedData(String deviceId, Map<String, String> mappingValue) {
		synchronized (cachedMonitoringDevice) {
			Map<String, String> map = new HashMap<>();
			if (cachedMonitoringDevice.get(deviceId) != null) {
				map = cachedMonitoringDevice.get(deviceId);
			}
			map.putAll(mappingValue);
			cachedMonitoringDevice.put(deviceId, map);
		}
	}

	/**
	 * Clones and populates the aggregatedDeviceList based on the cachedMonitoringDevice data.
	 * This method clears the existing aggregatedDeviceList, retrieves device information from cachedMonitoringDevice,
	 * and populates the list with AggregatedDevice instances containing monitor and controllable properties.
	 *
	 * @return The updated aggregatedDeviceList with the latest device information.
	 */
	private List<AggregatedDevice> cloneAndPopulateAggregatedDeviceList() {
		if (!checkControl) {
			synchronized (aggregatedDeviceList) {
				aggregatedDeviceList.clear();
				cachedMonitoringDevice.forEach((key, value) -> {
					AggregatedDevice aggregatedDevice = new AggregatedDevice();
					Map<String, String> cachedData = cachedMonitoringDevice.get(key);
					String deviceName = findValueByPartialKey(cachedData, NaViSetAdministrator2SEConstant.DEVICE_NAME);
					String modelName = findValueByPartialKey(cachedData, NaViSetAdministrator2SEConstant.DEVICE_MODEL);
					String deviceStatus = cachedData.get(NaViSetAdministrator2SEConstant.DEVICE_STATUS);
					aggregatedDevice.setDeviceId(key);
					aggregatedDevice.setDeviceOnline(false);
					if (deviceStatus != null) {
						aggregatedDevice.setDeviceOnline(NaViSetAdministrator2SEConstant.NUMBER_ONE.equals(deviceStatus));
					}
					if (deviceName != null) {
						aggregatedDevice.setDeviceName(deviceName);
					}
					if (modelName != null) {
						aggregatedDevice.setDeviceModel(modelName);
					}
					Map<String, String> stats = new HashMap<>();
					Map<String, String> dynamicStats = new HashMap<>();
					List<AdvancedControllableProperty> advancedControllableProperties = new ArrayList<>();
					populateMonitorProperties(cachedData, stats, dynamicStats);
					populateControlProperties(key, cachedData, stats, advancedControllableProperties);
					aggregatedDevice.setProperties(stats);
					aggregatedDevice.setControllableProperties(advancedControllableProperties);
					aggregatedDevice.setDynamicStatistics(dynamicStats);
					aggregatedDeviceList.add(aggregatedDevice);
				});
			}
		}
		checkControl = false;
		return aggregatedDeviceList;
	}

	/**
	 * Populates control properties for the specified device based on cached data, statistics, and advanced controllable properties.
	 *
	 * @param deviceId The identifier of the device.
	 * @param cachedData The cached data for the device.
	 * @param stats The statistics for the device.
	 * @param advancedControllableProperties The list of advanced controllable properties to be populated.
	 */
	private void populateControlProperties(String deviceId, Map<String, String> cachedData, Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		for (ControllablePropertyEnum item : ControllablePropertyEnum.values()) {
			String propertyName = item.getGroup() + item.getPropertyName();
			String value = getDefaultValueForNullData(cachedData.get(propertyName));
			if (!NaViSetAdministrator2SEConstant.NONE.equalsIgnoreCase(value)) {
				switch (item) {
					case POWER:
						int status = NaViSetAdministrator2SEConstant.NUMBER_ONE.equals(value) ? 1 : 0;
						addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(propertyName, status, NaViSetAdministrator2SEConstant.OFF, NaViSetAdministrator2SEConstant.ON),
								String.valueOf(status));
						break;
					case INPUT:
						List<InputValueDTO> values = videoInputValues.get(deviceId);
						String newValue = getNameByValue(values, value);
						addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(propertyName, getAllNames(values), newValue), newValue);
						break;
					case VOLUME:
						if (NaViSetAdministrator2SEConstant.NUMBER_ONE.equalsIgnoreCase(getDefaultValueForNullData(cachedData.get(NaViSetAdministrator2SEConstant.DEVICE_STATUS)))) {
							VolumeValueDTO volumeValue = audioVolumeValues.get(deviceId);
							String minValue = volumeValue.getMinValue();
							String maxValue = volumeValue.getMaxValue();
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createSlider(stats, propertyName, minValue, maxValue, Float.parseFloat(minValue), Float.parseFloat(maxValue), Float.parseFloat(value)), value);
							stats.put(propertyName + NaViSetAdministrator2SEConstant.CURRENT_VALUE, value);
						} else {
							stats.remove(propertyName);
						}
						break;
				}
			}
		}
	}

	/**
	 * Populates monitor properties in the stats map based on the provided cached data.
	 * This method iterates through the entries in the cached data and maps specific properties to the stats map.
	 * Some properties may undergo additional processing before being added to the stats map.
	 *
	 * @param cached The cached data containing monitor properties.
	 * @param stats The map to which monitor properties will be added.
	 * @param dynamicStats The map to which dynamic properties will be added.
	 */
	private void populateMonitorProperties(Map<String, String> cached, Map<String, String> stats, Map<String, String> dynamicStats) {
		cached.forEach((key, value) -> {
			DynamicStatisticsDefinition statisticsDefinition = DynamicStatisticsDefinition.getByDefaultName(key);
			if (statisticsDefinition != null) {
				boolean propertyListed = false;
				String propertyName = statisticsDefinition.getPropertyName();
				String propertyValue = getTemperatureC(value);
				if (!historicalProperties.isEmpty()) {
					propertyListed = historicalProperties.contains(propertyName);
				}
				if (propertyListed && StringUtils.isNotNullOrEmpty(propertyValue) && !NaViSetAdministrator2SEConstant.NONE.equalsIgnoreCase(propertyValue)) {
					dynamicStats.put(propertyName, propertyValue);
				} else {
					stats.put(propertyName, getDefaultValueForNullData(propertyValue));
				}
			} else {
				MonitorPropertyEnum propertyEnum = MonitorPropertyEnum.getByDefaultName(key);
				if (propertyEnum != null) {
					key = propertyEnum.getPropertyName();
				}
				if (!NaViSetAdministrator2SEConstant.VIDEO_INPUT.equalsIgnoreCase(key) && !NaViSetAdministrator2SEConstant.POWER_STATE.equalsIgnoreCase(key)) {
					stats.put(key, value);
				}
			}
		});
	}

	/**
	 * Retrieves the name associated with the specified value from the list of {@code InputValueDTO} objects.
	 *
	 * @param objectList The list of {@code InputValueDTO} objects.
	 * @param targetValue The target value for which the corresponding name is to be retrieved.
	 * @return The name associated with the specified value, or {@link NaViSetAdministrator2SEConstant#NONE} if not found.
	 */
	private String getNameByValue(List<InputValueDTO> objectList, String targetValue) {
		return objectList.stream().filter(item -> item.getValue().equals(targetValue))
				.findFirst().map(InputValueDTO::getName).orElse(NaViSetAdministrator2SEConstant.NONE);
	}

	/**
	 * Retrieves the value associated with the specified name from the list of {@code InputValueDTO} objects.
	 *
	 * @param objectList The list of {@code InputValueDTO} objects.
	 * @param targetName The target name for which the corresponding value is to be retrieved.
	 * @return The value associated with the specified name, or {@link NaViSetAdministrator2SEConstant#NONE} if not found.
	 */
	private String getValueByName(List<InputValueDTO> objectList, String targetName) {
		return objectList.stream().filter(item -> item.getName().equals(targetName))
				.findFirst().map(InputValueDTO::getValue).orElse(NaViSetAdministrator2SEConstant.NONE);
	}

	/**
	 * Retrieves an array containing all names from the list of {@code InputValueDTO} objects.
	 *
	 * @param objectList The list of {@code InputValueDTO} objects.
	 * @return An array containing all names from the list.
	 */
	private String[] getAllNames(List<InputValueDTO> objectList) {
		return objectList.stream().map(InputValueDTO::getName).toArray(String[]::new);
	}

	/**
	 * Extracts the temperature value in Celsius from the input string.
	 * This method uses a regular expression pattern to find a decimal number followed by "°C" in the input string.
	 * If a match is found, the extracted temperature value is returned; otherwise, "NONE" is returned.
	 *
	 * @param input The input string containing temperature information.
	 * @return The extracted temperature value in Celsius or "NONE" if not found.
	 */
	private String getTemperatureC(String input) {
		Pattern pattern = Pattern.compile("(\\d+\\.\\d+)°C");
		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return NaViSetAdministrator2SEConstant.NONE;
		}
	}

	/**
	 * Finds and retrieves the value from a map based on a partial key match.
	 * This method iterates through the keys of the map and returns the value
	 * associated with the first key that contains the specified partial key.
	 *
	 * @param map The map to search for the partial key.
	 * @param partialKey The partial key to match against the map's keys.
	 * @return The value associated with the first matching partial key, or {@code null} if no match is found.
	 */
	private String findValueByPartialKey(Map<String, String> map, String partialKey) {
		for (String key : map.keySet()) {
			if (key.contains(partialKey)) {
				String value = map.get(key);
				map.remove(key);
				return value;
			}
		}
		return null;
	}

	/**
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) ? value : NaViSetAdministrator2SEConstant.NONE;
	}

	/**
	 * Updates the cached value for a specific device with the given name and value.
	 *
	 * @param deviceId The identifier of the device.
	 * @param name The name associated with the value.
	 * @param value The new value to be updated.
	 */
	private void updateCachedValue(String deviceId, String name, String value) {
		cachedMonitoringDevice.computeIfPresent(deviceId, (key, map) -> {
			map.put(name, value);
			return map;
		});
	}

	/**
	 * Updates cached devices' control value, after the control command was executed with the specified value.
	 * It is done in order for aggregator to populate the latest control values, after the control command has been executed,
	 * but before the next devices details polling cycle was addressed.
	 *
	 * @param stats The updated device properties.
	 * @param advancedControllableProperties The updated list of advanced controllable properties.
	 * @param name of the control property
	 * @param value to set to the control property
	 */
	private void updateLocalControlValue(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties, String name, String value) {
		stats.put(name, value);
		advancedControllableProperties.stream().filter(advancedControllableProperty ->
				name.equals(advancedControllableProperty.getName())).findFirst().ifPresent(advancedControllableProperty ->
				advancedControllableProperty.setValue(value));
	}

	/**
	 * Updates the properties and controllable properties of an aggregated device in the list.
	 *
	 * @param deviceId The unique identifier of the device to update.
	 * @param stats The updated device properties.
	 * @param advancedControllableProperties The updated list of advanced controllable properties.
	 */
	private void updateListAggregatedDevice(String deviceId, Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		Optional<AggregatedDevice> device = aggregatedDeviceList.stream().filter(aggregatedDevice ->
				deviceId.equals(aggregatedDevice.getDeviceId())).findFirst();
		if (device.isPresent()) {
			device.get().setControllableProperties(advancedControllableProperties);
			device.get().setProperties(stats);
		}
	}

	/**
	 * Removes a controllable property and its associated value from the provided statistics and advanced controllable properties lists.
	 *
	 * @param stats The statistics map containing property values.
	 * @param advancedControllableProperties The list of advanced controllable properties.
	 * @param name The name of the property to remove.
	 */
	private void removeValueForTheControllableProperty(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties, String name) {
		stats.remove(name);
		advancedControllableProperties.removeIf(item -> item.getName().equalsIgnoreCase(name));
	}

	/**
	 * Add addAdvancedControlProperties if advancedControllableProperties different empty
	 *
	 * @param advancedControllableProperties advancedControllableProperties is the list that store all controllable properties
	 * @param stats store all statistics
	 * @param property the property is item advancedControllableProperties
	 * @throws IllegalStateException when exception occur
	 */
	private void addAdvancedControlProperties(List<AdvancedControllableProperty> advancedControllableProperties, Map<String, String> stats, AdvancedControllableProperty property, String value) {
		if (property != null) {
			for (AdvancedControllableProperty controllableProperty : advancedControllableProperties) {
				if (controllableProperty.getName().equals(property.getName())) {
					advancedControllableProperties.remove(controllableProperty);
					break;
				}
			}
			if (StringUtils.isNotNullOrEmpty(value)) {
				stats.put(property.getName(), value);
			} else {
				stats.put(property.getName(), NaViSetAdministrator2SEConstant.EMPTY);
			}
			advancedControllableProperties.add(property);
		}
	}

	/***
	 * Create AdvancedControllableProperty slider instance
	 *
	 * @param stats extended statistics
	 * @param name name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty slider instance
	 */
	private AdvancedControllableProperty createSlider(Map<String, String> stats, String name, String labelStart, String labelEnd, Float rangeStart, Float rangeEnd, Float initialValue) {
		stats.put(name, initialValue.toString());
		AdvancedControllableProperty.Slider slider = new AdvancedControllableProperty.Slider();
		slider.setLabelStart(labelStart);
		slider.setLabelEnd(labelEnd);
		slider.setRangeStart(rangeStart);
		slider.setRangeEnd(rangeEnd);

		return new AdvancedControllableProperty(name, new Date(), slider, initialValue);
	}

	/***
	 * Create dropdown advanced controllable property
	 *
	 * @param name the name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty dropdown instance
	 */
	private AdvancedControllableProperty createDropdown(String name, String[] values, String initialValue) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(values);
		dropDown.setLabels(values);

		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}

	/**
	 * Create switch is control property for metric
	 *
	 * @param name the name of property
	 * @param status initial status (0|1)
	 * @return AdvancedControllableProperty switch instance
	 */
	private AdvancedControllableProperty createSwitch(String name, int status, String labelOff, String labelOn) {
		AdvancedControllableProperty.Switch toggle = new AdvancedControllableProperty.Switch();
		toggle.setLabelOff(labelOff);
		toggle.setLabelOn(labelOn);

		AdvancedControllableProperty advancedControllableProperty = new AdvancedControllableProperty();
		advancedControllableProperty.setName(name);
		advancedControllableProperty.setValue(status);
		advancedControllableProperty.setType(toggle);
		advancedControllableProperty.setTimestamp(new Date());

		return advancedControllableProperty;
	}
}
