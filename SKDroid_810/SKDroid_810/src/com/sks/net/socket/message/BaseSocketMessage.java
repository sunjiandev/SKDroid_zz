package com.sks.net.socket.message;

public class BaseSocketMessage {

	public static final int MSG_SOCKET_PORT = 31000;
	public static final int MSG_UNDEFINED = 0XFFFF;
	// 3.1.1. 设置类指令（0x0000~003f）
	public static final int MSG_C_SET_SOUNDVOLUME = 0X0006; // 设置音量大小
	public static final int MSG_C_SET_VIDEOQUALITY = 0X0005; // 设置视频清晰度
	public static final int MSG_C_SET_CAMERA = 0X0007; // 设置摄像头
	public static final int MSG_C_SET_SOUNDMODE = 0X0008; // 设置放音模式
	public static final int MSG_C_SET_CURRENTGROUP = 0X0015; // 设置当前集群组
	// 3.1.2. 参数返回类指令（0x0080~0x00ff）
	public static final int MSG_S_UPDATE_SOUNDVOLUME = 0X0087; // 上报音量大小
	public static final int MSG_S_UPDATE_VIDEOQUALITY = 0X008B; // 上报视频清晰度
	public static final int MSG_S_UPDATE_CAMERA = 0X008D; // 上报摄像头状态
	public static final int MSG_S_UPDATE_SOUNDMODE = 0X0088; // 上报放音模式
	public static final int MSG_S_UPDATE_CURRENTGROUP = 0X0090; // 上报当前集群组
	// 3.2. 开机交互消息（0x0300~0x03ff）
	public static final int MSG_C_INIT_OK = 0X0305; // 初始化完成
	public static final int MSG_S_USER_NEEDREG = 0X03EF; // 用户尚未注册
	public static final int MSG_C_USER_REG = 0X03F0; // 用户注册
	public static final int MSG_S_USER_REGRESULT = 0X03F2; // 用户注册返回消息
	public static final int MSG_C_USER_UNREG = 0X03F3; // 用户注销
	public static final int MSG_S_USER_UNREGRESULT = 0X03F4; // 用户注销返回消息
	public static final int MSG_S_INIT_STATE = 0X03F1; // 上报设备开机状态信息
	public static final int MSG_S_INIT_CONTACT = 0X0391; // 上报联系人列表信息
	public static final int MSG_S_INIT_GROUP = 0X0392; // 上报组呼组列表信息
	// 3.3.1. 语音业务
	public static final int MSG_S_AUDIO_INCOMING = 0X1000; // 语音来电
	public static final int MSG_S_AUDIO_CANCEL = 0X1001; // 语音振铃结束
	public static final int MSG_C_AUDIO_CALLOUT = 0X1002; // 语音呼出
	public static final int MSG_S_AUDIO_CALLING = 0X1003; // 正在呼叫
	public static final int MSG_S_AUDIO_CALLFAILED = 0X1006; // 呼出失败
	public static final int MSG_S_AUDIO_INCALL = 0X1007; // 开始通话
	public static final int MSG_C_AUDIO_PICK = 0X1005; // 本机摘机
	public static final int MSG_C_AUDIO_HANGUP = 0X1004; // 本机挂机
	public static final int MSG_S_AUDIO_TERMINATED = 0X1008; // 通话断开
	// 3.3.2. 视频业务
	public static final int MSG_S_VIDEO_INCOMING = 0X2000; // 视频来电
	public static final int MSG_S_VIDEO_CANCEL = 0X2001; // 视频振铃结束
	public static final int MSG_C_VIDEO_CALLOUT = 0X2002; // 视频呼出
	public static final int MSG_S_VIDEO_CALLING = 0X2003; // 正在呼叫
	public static final int MSG_S_VIDEO_CALLFAILED = 0X2006; // 呼出失败
	public static final int MSG_S_VIDEO_INCALL = 0X2007; // 开始视频
	public static final int MSG_C_VIDEO_PICK = 0X2005; // 本机摘机
	public static final int MSG_C_VIDEO_HANGUP = 0X2004; // 本机挂机
	public static final int MSG_S_VIDEO_TERMINATED = 0X2008; // 视频断开
	public static final int MSG_S_VIDEO_STATE = 0X0385; // 上报业务状态信息
	// 3.3.3. 集群业务
	public static final int MSG_C_GROUP_CALLOUT = 0X3000; // PTT键发起集群通话
	public static final int MSG_C_GROUP_TERMINATED = 0X3001; // PTT键结束集群通话
	public static final int MSG_C_GROUP_EXITED = 0X3003; // 退出组呼
	public static final int MSG_S_GROUP_STATE = 0X3002; // 组呼状态
	public static final int MSG_S_GROUP_INCOMING = 0X3004; // 组呼来电
	// 自组网组呼功能
	public static final int MSG_C_NORMAL_GROUP_AUDIOCALL = 0x3600;// PTT键发起集群自组网组呼
	public static final int MSG_C_SUPER_GROUP_AUDIOCALL = 0x3601;// PTT键发起集群自组网组呼
	public static final int MSG_C_GROUP_AUDIOEXITED = 0X3602; // 退出组呼
	// 3.3.4. 短消息
	public static final int MSG_C_S_SMS_RESULT = 0X4000; // ACKS(短消息发送结果-上/下行)
	public static final int MSG_C_S_SMS_FORMAT = 0X4001; // 短消息格式(上/下行)
	// 3.3.5. 自组网
	public static final int MSG_C_ADHOC_LOGIN = 0X5000; // 自组网注册
	public static final int MSG_C_ADHOC_UNLOGIN = 0X5001; // 自组网注销
	// public static final int MSG_S_ADHOC_UPDATE_CONTACT = 0X5002;
	// //自组网上报联系人列表信息
	// 3.3.6. 模拟视频输出
	public static final int MSG_C_TVOUT_OPEN_WRITE = 0X6000; // 打开输出设备
	public static final int MSG_C_TVOUT_CLOSE = 0X6001; // 关闭输出设备

	// gzc 业务软件与适配软件心跳
	public static final int MSG_C_ALIVE_REQ = 0X7000; // 业务与适配心跳请求
	public static final int MSG_S_ALIVE_RES = 0X7001; // 业务与适配心跳响应
	public static final int MSG_C_TVOUT_SET = 0X7002; // 设置视频输出源
	public static final int MSG_S_TVOUT_REPORT = 0X7003; // 上报视频输出源

	// 回铃音通知消息
	public static final int MSG_S_RING_NOTIF = 0X8001;

	public static final int MSG_C_GIS_REQUEST = 0X0100; // GIS信息请求消息
	public static final int MSG_S_GIS_RESPONSE = 0X0101; // GIS信息响应消息

	public static final int MSG_C_IP_CHANGE = 0X0200; // WAN口ip变更消息

	public static final int MSG_S_IP_CHANGE = 0X0201; // 可登录后返回消息

	// 默认当前集群组存储名称
	public final static String SHARED_PREF_GROUP = "CURRENT_GROUP";
	public final static String DEFAULT_CURRENT_GROUP = "DEFAULT_CURRENT_GROUP";

	// 默认当前集群组
	public static final String DEFAULT_CURRENT_GROUPNO = "110";

	// 消息类型
	private int msgType = MSG_UNDEFINED;
	// 数据体
	private byte[] data = null;

	//

	public BaseSocketMessage() {

	}

	public BaseSocketMessage(int type) {
		msgType = type;
	}

	public BaseSocketMessage(int type, byte[] indata) {
		msgType = type;
		data = indata;
	}

	public int getType() {
		return msgType;
	}

	public byte[] getData() {
		return data;
	}

	//
	public void setType(int type) {
		msgType = type;
	}

	public void setData(byte[] indata) {
		data = indata;
	}

	public String description() {
		String description = "type=0x"
				+ MessageTools.bytes2HexString(MessageTools.intToBytes(msgType,
						2));
		description += ",";
		if (data != null)
			description += "length=" + data.length;
		else
			description += "length=" + 0;
		description += ",";
		if (data != null) {
			description += "data=0x" + MessageTools.bytes2HexString(data);
		}
		return description;
	}

}
