package com.iotmanager;

/**
 * Created by connorstein on 15-06-03.
 * All commands for communication with the ESP devices
 */
public final class Constants {
    public static final String HELLO_DEVICES="Hello ESP Devices?";
    public static final String LOCATION_MODE="Devices Low Power";
    public static final String DEFAULT_DEVICE_BROADCAST_IP="255.255.255.255";
    public static final int DEFAULT_DEVICE_TCP_PORT=80;
    public static final int DEFAULT_DEVICE_UDP_PORT=1025;
    public static final String DEFAULT_DEVICE_IP="192.168.4.1";
    public static final String COMMAND_CONNECT="Connect:";
    public static final String COMMAND_MAC_GET="Mac Get";
    public static final String COMMAND_TEMPERATURE_SET="Temperature Set:";
    public static final String COMMAND_TEMPERATURE_GET="Temperature Get";
    public static final String COMMAND_NAME="Name:";
    public static final String RESPONSE_NAME_SUCCESS="Name Set";
    public static final String COMMAND_TYPE="Type:";
    public static final String RESPONSE_TYPE_SUCCESS="Type Set";
    public static final String COMMAND_ROOM="Room:";
    public static final String RESPONSE_ROOM_SUCCESS="Room Set";
    public static final String RESPONSE_FAIL="Failed";
    public static final String COMMAND_LIGHTING_SET="Lighting Set";
    public static final String COMMAND_LIGHTING_GET="Lighting Get";
    public static final String COMMAND_RUN_AP="Run AP";
    public static final String COMMAND_CAMERA_TAKE_PICTURE="Camera Take Picture";
    public static final String COMMAND_CAMERA_CHANGE_COMPRESSION="Camera Compression Ratio:";
    public static final String RESPONSE_CAMERA_CHANGE_COMPRESSION_SUCCESS="Camera Compression Ratio Set";
    public static final String RESPONSE_CAMERA_CHANGE_COMPRESSION_FAIL="Camera Compression Ratio Fail";

    public static final String RESPONSE_TAKE_PICTURE_SUCCESS="Picture Taken";
    public static final String RESPONSE_TAKE_PICTURE_FAIL="Picture Take Fail";
    public static final String RESPONSE_GET_PICTURE_SUCCESS="Picture Got";
    public static final String RESPONSE_GET_PICTURE_FAIL="Picture Got Fail";
    public static final String COMMAND_CAMERA_GET_PICTURE="Camera Get Picture";
    public static final String COMMAND_CAMERA_STOP_PICTURE="Camera Stop Picture";
    public static final String COMMAND_CAMERA_GET_SIZE="Camera Get Size";

}
