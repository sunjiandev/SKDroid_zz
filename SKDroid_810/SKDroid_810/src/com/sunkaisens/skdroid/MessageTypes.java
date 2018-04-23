package com.sunkaisens.skdroid;


public class MessageTypes {
	public static final int MSG_DOWNLOAD_CONTACTS = 0;
	public static final int MSG_DOWNLOAD_CONTACTS_FINISH = 1;
	public static final int MSG_DOWNLOAD_CONTACTS_FAILED = 2;
	public static final int MSG_DOWNLOAD_CONTACTSNET_FINISH = 3;
	public static final int MSG_DOWNLOAD_CONTACTSNET_FAILED = 4;
	public static final int MSG_DOWNLOAD_CONTACTSCOMMGROUP_FINISH = 23;
	public static final int MSG_DOWNLOAD_CONTACTSCOMMGROUP_FAILED = 24;

	public static final int MSG_DOWNLOAD_CONTACTSGLOBALGROUP_FINISH = 33;
	public static final int MSG_DOWNLOAD_CONTACTSGLOBALGROUP_FAILED = 34;

	public static final int MSG_DOWNLOAD_CONTACTSSUBSCRIBEGROUP_FINISH = 35;
	public static final int MSG_DOWNLOAD_CONTACTSSUBSCRIBEGROUP_FAILED = 36;

	public static final int MSG_DOWNLOAD_FINISH = 37;

	public static final int MSG_SUBSCRIBE_CONTACTSLIST = 5;

	public static final int MSG_MAP_START_MEASURE = 6;
	public static final int MSG_MAP_STOP_MEASURE = 7;

	public static final int MSG_REQ_CONTACTS = 8; // 下载通讯录前请求
	public static final int MSG_REQ_CONTACTS_SUCCESS = 9;
	public static final int MSG_REQ_CONTACTS_FAILED = 10;

	public static final int MSG_NETERROR_CLICKABLE = 13;
	public static final int MSG_NETERROR_UNCLICKABLE = 14;

	public static final int MSG_MAP_START_PLOTTING_GREEN = 11;
	public static final int MSG_MAP_START_PLOTTING_GREEN_MARK = 12;

	public static final int MSG_MAP_START_PLOTTING_RED = 21;
	public static final int MSG_MAP_START_PLOTTING_RED_MARK = 22;

	public static final int MSG_MAP_START_PLOTTING_TANK = 31;
	public static final int MSG_MAP_START_PLOTTING_TANK_MARK = 32;

	public static final int MSG_MAP_START_PLOTTING_TRUCK = 41;
	public static final int MSG_MAP_START_PLOTTING_TRUCK_MARK = 42;

	public static final int MSG_MAP_START_PLOTTING_DRAG = 51;
	public static final int MSG_MAP_START_PLOTTING_DRAG_MARK = 52;

	public static final int MSG_MAP_START_PLOTTING_DELETE = 61;
	public static final int MSG_MAP_START_PLOTTING_DELETE_MARK = 62;

	public static final int MSG_MAP_STOP_PLOTTING = 99;

	public static final int MSG_MAP_START_DRAW_CIRCLE = 71;
	public static final int MSG_MAP_STOP_DRAW_CIRCLE = 72;

	public static final int MSG_MAP_START_MRK_PROP_MODIFY = 81;
	public static final int MSG_MAP_STOP_MRK_PROP_MODIFY = 82;

	public static final int MSG_MAP_ANDROID_CITY = 91;

	public static final int MSG_MAP_GPS_CREATE = 93;
	public static final int MSG_MAP_GPS_MOVE = 94;
	public static final int MSG_MAP_GPS_REMOVE = 95;

	public static final int MSG_MAP_CALL = 97;

	public static final int MSG_MAP_SUICIDE = 98;

	public static final int MSG_CHAT_AUDIORECORDER_0 = 101;
	public static final int MSG_CHAT_AUDIORECORDER_1 = 102;
	public static final int MSG_CHAT_AUDIORECORDER_2 = 103;
	public static final int MSG_CHAT_AUDIORECORDER_3 = 104;
	public static final int MSG_CHAT_AUDIORECORDER_4 = 105;
	public static final int MSG_CHAT_AUDIORECORDER_5 = 106;
	public static final int MSG_CHAT_AUDIORECORDER_6 = 107;
	public static final int MSG_CHAT_AUDIORECORDER_7 = 108;

	public static final int MSG_CHAT_AUDIORECEVIEPLAYER = 109;
	public static final int MSG_CHAT_AUDIORECEVIEPLAYER_1 = 110;
	public static final int MSG_CHAT_AUDIORECEVIEPLAYER_2 = 111;
	public static final int MSG_CHAT_AUDIORECEVIEPLAYER_3 = 112;

	public static final int MSG_CHAT_AUDIOTRANSFERPLAYER = 113;
	public static final int MSG_CHAT_AUDIOTRANSFERPLAYER_1 = 114;
	public static final int MSG_CHAT_AUDIOTRANSFERPLAYER_2 = 115;
	public static final int MSG_CHAT_AUDIOTRANSFERPLAYER_3 = 116;

	/**
	 * GIS事件
	 */
	public static final String MSG_GIS_EVENT = "msg_gis_event";

	/**
	 * GIS消息指令
	 */
	public static final String MSG_GIS_TYPE = "msg_gis_type";
	/**
	 * GIS信息响应消息
	 */
	public static final String MSG_GIS_RESPONSE = "msg_gis_response";

	public static final String MSG_REG_EVENT = "MSG_REG_EVENT";

	public static final String MSG_NET_EVENT = "MSG_NET_EVENT";

	public static final String MSG_CONTACT_EVENT = "MSG_CONTACT_EVENT";

	public static final String MSG_STACK_EVENT = "MSG_STACK_EVENT";
	
	/**
	 * 翰迅版本内部版本号
	 */
	public static final String INNER_VERSION_CODE = "v2.03.00.85\r\n";

	// GIS事件具体消息
	/**
	 * 开始发送GIS信息请求消息
	 */
	public static final int MSG_GIS_REQUEST_START = 2001;
	/**
	 * 结束发送GIS信息请求消息
	 */
	public static final int MSG_GIS_REQUEST_STOP = 2002;

	/**
	 * 网络连接错误
	 */
	public static final int MSG_REG_NETWORK_ERROR = 40001;

	public static final int MSG_REG_NETWORK_CHECK = 40007;

	/**
	 * 正在注册
	 */
	public static final int MSG_REG_INPROGRESS = 40002;

	public static final int MSG_REG_OK = 40003;

	public static final int MSG_REG_NOK = 40004;

	public static final int MSG_UNREG_OK = 40005;

	public static final int MSG_UNREG_INPROGRESS = 40006;

	public static final int MSG_STACK_NEED_STOP = 40404;
	
	
	
	
}
