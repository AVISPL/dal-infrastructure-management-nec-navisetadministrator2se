/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import javax.security.auth.login.FailedLoginException;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.NaViSetAdministrator2SECommand;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.NaViSetAdministrator2SEConstant;
import com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common.SystemInformation;
import com.avispl.symphony.dal.util.StringUtils;


public class NaViSetAdministrator2SECommunicator extends RestCommunicator implements Aggregator, Monitorable, Controller {

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
	 * API Token
	 */
	private String token;

	/**
	 * List of device ID
	 */
	private List<String> deviceIdList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * A JSON node containing the response from an aggregator.
	 */
	private JsonNode aggregatorResponse;

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
		return null;
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

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal init is called.");
		}
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
		if (localExtendedStatistics != null && localExtendedStatistics.getStatistics() != null && localExtendedStatistics.getControllableProperties() != null) {
			localExtendedStatistics.getStatistics().clear();
			localExtendedStatistics.getControllableProperties().clear();
		}
		deviceIdList.clear();
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
			if (!NaViSetAdministrator2SEConstant.ZERO.equals(item.get(NaViSetAdministrator2SEConstant.DEVICE_ID).asText())) {
				deviceIdList.add(item.get(NaViSetAdministrator2SEConstant.DEVICE_ID).asText());
			}
		}
	}

	/**
	 * Retrieves a token using the provided username and password
	 *
	 * @return the token string
	 */
	private String getCookieSession() throws Exception {
		String token = NaViSetAdministrator2SEConstant.EMPTY;
		try {
			Map<String, String> credentials = new HashMap<>();
			credentials.put(NaViSetAdministrator2SEConstant.NAME, this.getLogin());
			credentials.put(NaViSetAdministrator2SEConstant.PASSWORD, this.getPassword());
			JsonNode response = this.doPost(NaViSetAdministrator2SECommand.LOGIN_COMMAND, credentials, JsonNode.class);
			if (response != null && response.has(NaViSetAdministrator2SEConstant.DATA) && response.get(NaViSetAdministrator2SEConstant.DATA).has(NaViSetAdministrator2SEConstant.TOKEN)) {
				token = response.get(NaViSetAdministrator2SEConstant.DATA).get(NaViSetAdministrator2SEConstant.TOKEN).asText();
			}
		} catch (Exception e) {
			throw new FailedLoginException("Failed to retrieve the cookie for account with from username and password");
		}
		return token;
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
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) ? value : NaViSetAdministrator2SEConstant.NONE;
	}
}
