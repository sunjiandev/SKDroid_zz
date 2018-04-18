package com.sunkaisens.skdroid.model;

/**
 * @author ZhichengGu
 * 
 *         版本列表
 * 
 */
public enum VERSION {

	/**
	 * 公网版本 1、默认视频分辨率 skd 2、调度台账号无视频回传功能
	 */
	ONLINE,

	/**
	 * 810版本 1、默认视频分辨率 cif 2、调度台账号有视频回传功能
	 */
	NORMAL,

	/**
	 * PAD大屏版本
	 */
	PAD,

	/**
	 * 自组网模式
	 */
	ADHOC,

	/**
	 * 大终端版本
	 */
	SOCKET,

	/**
	 * 大终端模式在小终端上的测试版本
	 */
	SOCKET_TEST
}
