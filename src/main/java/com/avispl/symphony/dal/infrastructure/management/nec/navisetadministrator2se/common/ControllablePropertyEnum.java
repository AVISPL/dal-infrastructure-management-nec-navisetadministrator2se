/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nec.navisetadministrator2se.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * ControllableProperty is Enumeration representing controllable properties for a system.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 1/15/2024
 * @since 1.0.0
 */
public enum ControllablePropertyEnum {
	POWER_STATE("PowerState", NaViSetAdministrator2SEConstant.POWER_GROUP, "State"),
	SAVE_LEVEL("PowerSaveLevelInStandbyMode", NaViSetAdministrator2SEConstant.POWER_GROUP, "SaveLevelInStandbyMode"),
	FAN_MODE("FanMode", NaViSetAdministrator2SEConstant.POWER_GROUP, "FanMode"),
	POWER_SAVE("PowerSave", NaViSetAdministrator2SEConstant.POWER_GROUP, "Save"),
	POWER_SAVE_TIME("PowerSaveTime", NaViSetAdministrator2SEConstant.POWER_GROUP, "SaveTime(seconds)"),
	POWER_OFF_TIMER("PowerOffTimer", NaViSetAdministrator2SEConstant.POWER_GROUP, "OffTimer(hours)"),
	FAN_CONTROL("FanControl", NaViSetAdministrator2SEConstant.POWER_GROUP, "FanControl"),
	FAN_SPEED("FanSpeed", NaViSetAdministrator2SEConstant.POWER_GROUP, "FanSpeed"),
	INDICATOR_LED("PowerIndicatorLED", NaViSetAdministrator2SEConstant.POWER_GROUP, "IndicatorLED"),
	QUICK_START("QuickStart", NaViSetAdministrator2SEConstant.POWER_GROUP, "QuickStart"),
	USB_POWER("USBPower", NaViSetAdministrator2SEConstant.POWER_GROUP, "USBPower"),
	SAVE_MESSAGE("PowerSaveMessage", NaViSetAdministrator2SEConstant.POWER_GROUP, "SaveMessage"),


	VIDEO_INPUT("VideoInput", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "Input"),
	BRIGHTNESS("Brightness", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "Brightness(%)"),
	CONTRAST("Contrast", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "Contrast(%)"),
	SHARPNESS("Sharpness", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "Sharpness(%)"),
	HUE("Hue", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "Hue(%)"),
	COLOR("Color", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "Color(%)"),
	PICTURE_PRESET("PicturePreset", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "PicturePreset"),
	PICTURE_MUTE("PictureMute", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "PictureMute"),
	ONSCREEN_MUTE("OnscreenMute", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "OnscreenMute"),
	LENS_MUTE("LensMute", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "LensMute"),
	PICTURE_FREEZE("PictureFreeze", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "PictureFreeze"),
	SPECTRA_VIEW_ENGINE("SpectraViewEngine", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "SpectraViewEngine"),
	SVE_LUMINANCE("SVELuminance", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "SVELuminance"),
	BACKLIGHT("Backlight", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "Backlight(%)"),
	BLACK_LEVEL("VideoBlackLevel", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "BlackLevel(%)"),
	SVE_BLACK("SVEBlack", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "SVEBlack(%)"),
	GAMMA_CORRECTION("GammaCorrection", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "GammaCorrection"),
	PICTURE_MODE("PictureMode", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "PictureMode"),
	SVE_PRESET("SVEPreset", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "SVEPreset"),
	COLOR_PRESET("SelectColorPreset", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "ColorPreset"),
	COLOR_TEMPERATURE("ColorTemperature", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "ColorTemperature(K)"),
	RED_GAIN("RedGain", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "RedGain(%)"),
	GREEN_GAIN("GreenGain", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "GreenGain(%)"),
	BLUE_GAIN("BlueGain", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "BlueGain(%)"),
	SCREEN_MUTE("ScreenMute", NaViSetAdministrator2SEConstant.VIDEO_GROUP, "ScreenMute"),


	LENS_SHIFT("LensShiftHomePosition", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "LensShiftHomePosition"),
	PROJECTOR_ORIENTATION("ProjectorOrientation", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "ProjectorOrientation"),
	ASPECT_RATIO("AspectRatio", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "AspectRatio"),
	KEYSTONE_H("KeystoneH", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "KeystoneH(%)"),
	CORRECTION_MODE("GeometricCorrectionMode", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "GeometricCorrectionMode"),
	KEYSTONE_V("KeystoneV", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "KeystoneV(%)"),
	HARDWARE_EDGE_BLENDING("HardwareEdgeBlending", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "HardwareEdgeBlending"),
	ZOOM("Zoom", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "Zoom(%)"),
	ZOOM_H_EXPANSION("ZoomHExpansion", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "ZoomHExpansion(%)"),
	ZOOM_V_EXPANSION("ZoomVExpansion", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "ZoomVExpansion(%)"),
	ZOOM_H_POSITION("ZoomHPosition", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "ZoomHPosition(%)"),
	ZOOM_V_POSITION("ZoomVPosition", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "ZoomVPosition(%)"),
	ASPECT("Aspect", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "Aspect"),
	IMAGE_FLIP("ImageFlip", NaViSetAdministrator2SEConstant.GEOMETRY_GROUP, "ImageFlip"),


	AUDIO_VOLUME("AudioVolume", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Volume"),
	AUDIO_MUTE("AudioMute", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Mute"),
	AUDIO_PRESET_MODE("AudioPresetMode", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "PresetMode"),
	AUDIO_CHANNEL("AudioChannel", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Channel"),
	AUDIO_BALANCE("AudioBalance", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Balance"),
	AUDIO_BASS("AudioBass", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Bass"),
	AUDIO_TREBLE("AudioTreble", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Treble"),
	AUDIO_INPUT("AudioInput", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Input"),
	MULTI_PICTURE_AUDIO("MultiPictureAudio", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "MultiPictureAudio"),
	SURROUND_SOUND("SurroundSound", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "SurroundSound"),
	AUDIO_LINE_OUT("AudioLineOut", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "LineOut"),
	AUDIO_DELAY("AudioDelay", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "Delay"),
	AUDIO_DELAY_TIME("AudioDelayTime", NaViSetAdministrator2SEConstant.AUDIO_GROUP, "DelayTime(ms)"),

	LANGUAGE("OSDLanguage", NaViSetAdministrator2SEConstant.OSD_GROUP, "Language"),
	MENU_DISPLAY_TIME("OSDMenuDisplayTime", NaViSetAdministrator2SEConstant.OSD_GROUP, "MenuDisplayTime(seconds)"),
	OSD_SIZE("OSDSize", NaViSetAdministrator2SEConstant.OSD_GROUP, "Size"),
	INFORMATION_OSD("InformationOSD", NaViSetAdministrator2SEConstant.OSD_GROUP, "InformationOSD(seconds)"),
	COMMUNICATION_INFO("CommunicationInfoOSD", NaViSetAdministrator2SEConstant.OSD_GROUP, "CommunicationInfo"),
	SIGNAL_INFORMATION("SignalInformation", NaViSetAdministrator2SEConstant.OSD_GROUP, "SignalInformation"),
	TRANSPARENCY("OSDTransparency", NaViSetAdministrator2SEConstant.OSD_GROUP, "Transparency"),
	ROTATION("OSDRotation", NaViSetAdministrator2SEConstant.OSD_GROUP, "Rotation"),
	POSITION_H("OSDHPosition", NaViSetAdministrator2SEConstant.OSD_GROUP, "PositionH"),
	POSITION_V("OSDVPosition", NaViSetAdministrator2SEConstant.OSD_GROUP, "PositionV"),
	FLIP("OSDFlip", NaViSetAdministrator2SEConstant.OSD_GROUP, "Flip"),
	KEY_GUIDE("OSDKeyGuide", NaViSetAdministrator2SEConstant.OSD_GROUP, "KeyGuide"),

	LIGHT_ECO_MODE("LightECOMode", NaViSetAdministrator2SEConstant.ECO_GROUP, "LightECOMode"),
	CONSTANT_BRIGHTNESS("ConstantBrightness", NaViSetAdministrator2SEConstant.ECO_GROUP, "ConstantBrightness"),
	LIGHT_MODE_ADJUST("LightModeAdjust", NaViSetAdministrator2SEConstant.ECO_GROUP, "LightModeAdjust"),
	AUTO_BRIGHTNESS("AutoBrightness", NaViSetAdministrator2SEConstant.ECO_GROUP, "AutoBrightness"),
	AMBIENT_LIGHT_SENSING("AmbientLightSensing", NaViSetAdministrator2SEConstant.ECO_GROUP, "AmbientLightSensing"),
	ILLUMINANCE_HIGH("AmbientLightIlluminanceHigh", NaViSetAdministrator2SEConstant.ECO_GROUP, "AmbientLightIlluminanceHigh(%)"),
	BACKLIGHT_HIGH("AmbientLightBacklightHigh", NaViSetAdministrator2SEConstant.ECO_GROUP, "AmbientLightBacklightHigh"),
	ILLUMINANCE_LOW("AmbientLightIlluminanceLow", NaViSetAdministrator2SEConstant.ECO_GROUP, "AmbientLightIlluminanceLow"),
	BACKLIGHT_LOW("AmbientLightBacklightLow", NaViSetAdministrator2SEConstant.ECO_GROUP, "AmbientLightBacklightLow"),
	ILLUMINANCE_READING("IlluminanceReading", NaViSetAdministrator2SEConstant.ECO_GROUP, "IlluminanceReading"),
	HUMAN_SENSING_MODE("HumanSensingMode", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingMode"),
	HUMAN_SENSING_WAITING_TIME("HumanSensingWaitingTime", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingWaitingTime"),
	POWER_SAVER_TIMER("PowerSaverTimer", NaViSetAdministrator2SEConstant.ECO_GROUP, "PowerSaverTimer"),
	BACKLIGHT_STATUS("HumanSensingBacklightOn/Off", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingBacklightStatus"),
	BACKLIGHT_VALUE("HumanSensingBacklight", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingBacklight"),
	VOLUME_STATUS("HumanSensingVolumeOn/Off", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingVolumeStatus"),
	VOLUME_VALUE("HumanSensingVolume", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingVolume"),
	INPUT_STATUS("HumanSensingInputOn/Off", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingInputStatus"),
	INPUT_VALUE("HumanSensingInput", NaViSetAdministrator2SEConstant.ECO_GROUP, "HumanSensingInput"),

	;
	private final String defaultName;
	private final String group;
	private final String propertyName;

	/**
	 * Constructor for ControllablePropertyEnum.
	 *
	 * @param defaultName The default name of the property.
	 * @param group The group to which the property belongs.
	 * @param propertyName The name of the property.
	 */
	ControllablePropertyEnum(String defaultName, String group, String propertyName) {
		this.defaultName = defaultName;
		this.group = group;
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
	 * Retrieves {@link #group}
	 *
	 * @return value of {@link #group}
	 */
	public String getGroup() {
		return group;
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
	 * Retrieve a ControllablePropertyEnum by its default name.
	 *
	 * @param name The default name to search for.
	 * @return The ControllablePropertyEnum with the specified default name, or null if not found.
	 */
	public static ControllablePropertyEnum getByDefaultName(String name) {
		Optional<ControllablePropertyEnum> property = Arrays.stream(ControllablePropertyEnum.values()).filter(item -> item.getDefaultName().equalsIgnoreCase(name)).findFirst();
		return property.orElse(null);
	}
}
