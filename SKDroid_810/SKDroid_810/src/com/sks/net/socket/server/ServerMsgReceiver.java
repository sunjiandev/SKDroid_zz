package com.sks.net.socket.server;

import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnCameraProducer;
import org.doubango.ngn.media.NgnCameraProducer_surface;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyVideoConsumerGL;
import org.doubango.ngn.media.NgnProxyVideoProducer;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.sip.NgnSipStack.STACK_STATE;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;
import org.doubango.utils.MyLog;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;
import com.sunkaisens.skdroid.groupcall.PTTActionTypes;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.groupcall.PTTResultTypes;
import com.sunkaisens.skdroid.groupcall.PTTTypes;
import com.sunkaisens.skdroid.model.VERSION;
import com.sks.net.socket.base.SocketMsgReceiver;
import com.sks.net.socket.base.SocketWorker;
import com.sks.net.socket.message.BCDTools;
import com.sks.net.socket.message.BaseSocketMessage;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenChat;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Services.ServiceAdhoc;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Services.ServiceSocketMode;
import com.sunkaisens.skdroid.TvOut.TvOut;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ServerMsgReceiver implements SocketMsgReceiver {
	private static final String TAG = ServerMsgReceiver.class.getSimpleName();

	public final static String MESSAGE_SOCKET_INTENT = "com.sks.socket.message";

	public static final int MSG_S_ADHOC_REFRESH = 0X5003; // 自组网刷新
	public static int mMobileNoLength = 16;
	public static int mPwdLength = 16;
	private static int REGISTER_COUNT;

	// private Thread mTvOutThread;

	// public static String mobileNo = "19800005002";
	// public static String groupNo = "110";

	private static ServerMsgReceiver mInstance = null;

	public static ServerMsgReceiver getMsgReceiver() {

		if (mInstance == null) {
			mInstance = new ServerMsgReceiver();
		}
		return mInstance;
	}

	// 记录正在注册周期次数
	private int RgistingCount = 0;
	// 正在注册最大周期次数
	private int MaxRegistingCount = 5;

	private NgnTimer mTimerAudioPTTReport; // 语音组呼心跳

	public SocketEventCallback mSocketCallback;

	private TimerTask mTimerTaskAudioPTTReport = new TimerTask() {
		@Override
		public void run() {
			if (GlobalSession.avSession != null
					&& SystemVarTools.mTimerAudioPTTReport != null) {
				sendPTTReportAliveInfoMsg();
			}
		}
	};

	// socket 事件处理接口
	// 适配软件->业务软件
	public void onSocketReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		MyLog.i("ServerMsgReceiver: action=", action);
		MyLog.d(TAG, "prient socket event :" + intent.getIntExtra("type", 0));
		if (MESSAGE_SOCKET_INTENT.equals(action)) {
			int type = intent.getIntExtra("type", 0);
			switch (type) {

			/**
			 * 3.1.1. 信令交互 初始化流程
			 * 业务软件启动后，接受适配软件发送来的初始化完成信息，收到后，应答以下三种信息：上报音量大小、上报视频清晰度、上报摄像头状态
			 */
			case BaseSocketMessage.MSG_C_INIT_OK: { // 初始化完成

				String videoSize = Engine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
								"tmedia_pref_video_size_vga");
				tmedia_pref_video_size_t value = tmedia_pref_video_size_t.tmedia_pref_video_size_720p;
				String videoQuality = "2";
				if (videoSize != null
						&& videoSize.equals("tmedia_pref_video_size_cif")) { // 0
																				// ：流畅
																				// //3
																				// CIF
																				// (352
																				// x
																				// 288)
																				// tmedia_pref_video_size_cif
					videoQuality = "0";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_cif;
				}
				if (videoSize != null
						&& videoSize.equals("tmedia_pref_video_size_vga")) { // 1
																				// ：清晰
																				// //5
																				// VGA
																				// (640
																				// x
																				// 480)
																				// tmedia_pref_video_size_vga
					videoQuality = "1";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_vga;
				}
				if (videoSize != null
						&& videoSize.equals("tmedia_pref_video_size_720p")) { // 2
																				// ：高清
																				// //9
																				// 720P
																				// (1280
																				// x
																				// 720)
																				// tmedia_pref_video_size_720p
					videoQuality = "2";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_720p;
				}

				if (videoSize == null) {
					videoSize = "tmedia_pref_video_size_720p";
				}

				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
								videoSize);

				// Compute
				if (!Engine.getInstance().getConfigurationService().commit()) {
					Log.e(TAG, "Failed to commit() configuration");
				} else {
					MediaSessionMgr.defaultsSetPrefVideoSize(value);
				}

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_SOUNDVOLUME, BCDTools
								.Str2BCD("2", 1))); // 上报音量大小
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_VIDEOQUALITY, BCDTools
								.Str2BCD(videoQuality, 1))); // 上报视频清晰度
				// 1个字节压缩BCD码表示，依次为：
				// 1－摄像头1 2－摄像头2
				boolean useFrontFacingCamera = NgnEngine
						.getInstance()
						.getConfigurationService()
						.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
								NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);
				if (useFrontFacingCamera) { // 前置摄像头 模拟
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_UPDATE_CAMERA, BCDTools
									.Str2BCD("2", 1))); // 上报摄像头状态 2
				} else { // 后置摄像头 高清
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_UPDATE_CAMERA, BCDTools
									.Str2BCD("1", 1))); // 上报摄像头状态1
				}
				Log.e("ServerMsgReceiver: ",
						"BaseSocketMessage.MSG_S_UPDATE_CAMERA = "
								+ (useFrontFacingCamera ? 2 : 1));

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + ""
						+ "; message = " + "上报音量大小 2");
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + ""
						+ "; message = " + "上报视频清晰度 " + videoQuality);
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + ""
						+ "; message = " + "上报摄像头状态 useFrontFacingCamera = "
						+ useFrontFacingCamera);

				// 是否需要注册
				if (Engine.getInstance().getSipService()
						.isRegisteSessionConnected()) { // 上报设备开机状态信息/上报联系人列表信息/上报组呼组列表信息
					String localMobileNo = Engine
							.getInstance()
							.getConfigurationService()
							.getString(
									NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
									NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_INIT_STATE, BCDTools
									.Str2BCD(localMobileNo, 10))); // 上报设备开机状态信息

					ServiceSocketMode.pushcontacts(false);

				} else { // 用户尚未注册
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_NEEDREG, null));

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = "
							+ "" + "; message = " + "用户尚未注册");
				}
			}
				break;

			/**
			 * 3.2.1.3. 用户注册（0x03f0）
			 */
			case BaseSocketMessage.MSG_C_USER_REG: { // 用户注册
				INgnSipService sipService = Engine.getInstance()
						.getSipService();
				REGISTER_COUNT += 1;
				if (sipService == null) {
					MyLog.d(TAG, "SipService is null");
					return;
				}
				ConnectionState cs = sipService.getRegistrationState();
				// 如果用户正在注册中，则不再发起新的注册
				if (cs != null && cs == ConnectionState.CONNECTING
						&& sipService.getSipStack() != null) {
					MyLog.d(TAG, "User is registing...");
					if (REGISTER_COUNT > 3) {
						sipService
								.setRegistrationState(ConnectionState.TERMINATED);
						sipService.getSipStack().stop();
						REGISTER_COUNT = 0;
					}

					return;
				}

				// 是否已经注册
				if (Engine.getInstance().getSipService()
						.isRegisteSessionConnected()) {
					// 通知其他控制台，有用户注册返回消息
					byte[] dataok = new byte[1];
					dataok[0] = 5; // 0：注册成功 1：密码错误 2：用户不存在 5：用户已登录 6：注册失败
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_REGRESULT, dataok));

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_S_USER_REGRESULT,
							"mobileNo = " + "" + "; message = " + "5：用户已登录");

					// 上报设备开机状态信息/上报联系人列表信息/上报组呼组列表信息
					final String localMobileNo = Engine
							.getInstance()
							.getConfigurationService()
							.getString(
									NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
									NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_INIT_STATE, BCDTools
									.Str2BCD(localMobileNo, 10))); // 上报设备开机状态信息
					ServiceSocketMode.pushcontacts(false);

					break;
				}

				if (!SystemVarTools.setDefaultSetting_socket()) { // Socket配置文件不存在
					SystemVarTools.setDefaultSetting();
				}

				// 开始注册
				byte[] data = intent.getByteArrayExtra("data");
				byte[] mobileNobytes = new byte[mMobileNoLength];
				try {
					System.arraycopy(data, 6, mobileNobytes, 0, mMobileNoLength);
				} catch (Exception e) {
					byte[] datanok = new byte[1];
					datanok[0] = 2; // 2 the user is not exist.
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_REGRESULT, datanok));
					MyLog.d(TAG, "type = "
							+ BaseSocketMessage.MSG_S_USER_REGRESULT
							+ "  The param is invalid.");
					return;
				}
				final String mobileNo = new String(mobileNobytes).trim();

				if (NgnStringUtils.isNullOrEmpty(mobileNo)) {
					byte[] datanok = new byte[1];
					datanok[0] = 2; // 2 the user is not exist.
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_REGRESULT, datanok));
					MyLog.d(TAG, "type = "
							+ BaseSocketMessage.MSG_S_USER_REGRESULT
							+ "  data=" + datanok[0]);
					return;
				}
				if (!NgnStringUtils.isNumberic(mobileNo)) {
					byte[] datanok = new byte[1];
					datanok[0] = 2; // 2 the user is not exist.
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_REGRESULT, datanok));
					MyLog.d(TAG, "type = "
							+ BaseSocketMessage.MSG_S_USER_REGRESULT
							+ "  data=" + datanok[0]);
					return;
				}

				byte[] pwdbytes = new byte[mPwdLength];
				try {
					System.arraycopy(data, mMobileNoLength + 6, pwdbytes, 0,
							mPwdLength);
				} catch (Exception e) {
					byte[] datanok = new byte[1];
					datanok[0] = 2; // 2 the user is not exist.
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_REGRESULT, datanok));
					MyLog.d(TAG, "type = "
							+ BaseSocketMessage.MSG_S_USER_REGRESULT
							+ "  the param is invalid.");
					e.printStackTrace();
					return;
				}
				final String pwd = new String(pwdbytes).trim();
				SystemVarTools.mIdentity = mobileNo;
				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
								mobileNo);
				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, pwd);
				Engine.getInstance()
						.getConfigurationService()
						.putString(
								NgnConfigurationEntry.IDENTITY_IMPU,
								"sip:"
										+ mobileNo
										+ "@"
										+ Engine.getInstance()
												.getConfigurationService()
												.getString(
														NgnConfigurationEntry.NETWORK_REALM,
														NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.IDENTITY_IMPI,
								mobileNo);

				Engine.getInstance()
						.getConfigurationService()
						.putString(
								NgnConfigurationEntry.NETWORK_PCSCF_HOST,
								Engine.getInstance()
										.getConfigurationService()
										.getString(
												NgnConfigurationEntry.NETWORK_PCSCF_HOST,
												NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));
				Engine.getInstance()
						.getConfigurationService()
						.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
								NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT);
				if (!Engine.getInstance().getConfigurationService().commit()) {
					Log.e("ServerMsgReceiver: ",
							"Failed to Commit() configuration");
				}
				new Thread(new Runnable() {
					public void run() {
						// boolean a = CrashHandler.isNetworkAvailable();
						// if(!a){
						// Log.d(TAG, "登录网络检测失败，网络已断开");
						// byte[] datanok = new byte[1];
						// datanok[0] = 6; // 0 Registe success,1 The passwoord
						// is wrong,2 User is not exist,6 Registe failed
						// SocketServer.sendMessage(new
						// BaseSocketMessage(BaseSocketMessage.MSG_S_USER_REGRESULT,
						// datanok));
						// return;
						// }

						Engine.getInstance().getNetworkService()
								.setNetworkEnable(true);

						Engine.getInstance().getSipService()
								.register(SKDroid.getContext());
						GlobalVar.mLocalNum = mobileNo;
						Log.d(TAG, "GlobalVar.mLocalNum 2:"
								+ GlobalVar.mLocalNum);

						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_USER_REG,
								"mobileNo = " + mobileNo + "; pwd = " + pwd);

						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_USER_REG,
								"NETWORK_REALM = "
										+ Engine.getInstance()
												.getConfigurationService()
												.getString(
														NgnConfigurationEntry.NETWORK_REALM,
														NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_USER_REG,
								"NETWORK_GROUP_REALM = "
										+ Engine.getInstance()
												.getConfigurationService()
												.getString(
														NgnConfigurationEntry.NETWORK_GROUP_REALM,
														NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM));
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_USER_REG,
								"NETWORK_GROUP_PORT = "
										+ Engine.getInstance()
												.getConfigurationService()
												.getString(
														NgnConfigurationEntry.NETWORK_GROUP_PORT,
														NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT));
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_USER_REG,
								"NETWORK_PCSCF_HOST = " + GlobalVar.pcscfIp);
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_USER_REG,
								"NETWORK_PCSCF_PORT = "
										+ Engine.getInstance()
												.getConfigurationService()
												.getInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
														NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
					}
				}).start();

			}
				break;

			/**
			 * 3.2.1.5. 用户注销
			 */
			case BaseSocketMessage.MSG_C_USER_UNREG: { // 用户注销
				// 用户注销
				Engine.getInstance().getSipService().unRegister();
				GlobalVar.mLogout = true;
				ServiceContact.sessionID = 0;
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_USER_UNREG, "mobileNo = "
						+ "" + "; message = " + "用户注销");
				if (!SKDroid.isl8848a_l1860()) {
					System.exit(0);
				}
				// //用户尚未注册
				// SocketServer.sendMessage(new
				// BaseSocketMessage(BaseSocketMessage.MSG_S_USER_NEEDREG,
				// null));
				//
				// MyLog.i("ServerMsgReceiver: type = " +
				// BaseSocketMessage.MSG_S_USER_NEEDREG, "mobileNo = " + "" +
				// "; message = " + "用户尚未注册");
			}
				break;

			// /**
			// * 3.1.1. 设置类指令（0x0000~003f）
			// */
			// case BaseSocketMessage.MSG_C_SET_SOUNDVOLUME: { //设置音量大小
			// String soundVolume =
			// BCDTools.BCD2Str(intent.getByteArrayExtra("data"));
			// SocketServer.sendMessage(new
			// BaseSocketMessage(BaseSocketMessage.MSG_S_UPDATE_SOUNDVOLUME,
			// BCDTools.Str2BCD(soundVolume, 1))); //上报音量大小
			//
			// MyLog.i("ServerMsgReceiver: type = " +
			// BaseSocketMessage.MSG_C_SET_SOUNDVOLUME, "mobileNo = " + "" +
			// "; message = " + "设置音量大小 1");
			// }
			// break;

			case BaseSocketMessage.MSG_C_SET_VIDEOQUALITY: { // 设置视频清晰度
				// 1个字节压缩BCD码表示，依次为：
				// 0 – 流畅
				// 1 – 清晰
				// 2 – 高清
				// 3 – 自动
				String videoQuality = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data"));

				String videoSize = "tmedia_pref_video_size_cif";
				tmedia_pref_video_size_t value = tmedia_pref_video_size_t.tmedia_pref_video_size_720p;
				if (videoQuality != null && videoQuality.equals("0")) { // 0 ：流畅
																		// //3
																		// CIF
																		// (352
																		// x
																		// 288)
																		// tmedia_pref_video_size_cif
					videoSize = "tmedia_pref_video_size_cif";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_cif;
				}
				if (videoQuality != null && videoQuality.equals("1")) { // 1 ：清晰
																		// //5
																		// VGA
																		// (640
																		// x
																		// 480)
																		// tmedia_pref_video_size_vga
					videoSize = "tmedia_pref_video_size_vga";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_vga;
				}
				if (videoQuality != null && videoQuality.equals("2")) { // 2 ：高清
																		// //9
																		// 720P
																		// (1280
																		// x
																		// 720)
																		// tmedia_pref_video_size_720p
					videoSize = "tmedia_pref_video_size_720p";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_720p;
				}
				if (videoQuality != null && videoQuality.equals("3")) { // 3 ：自动
																		// //3
																		// CIF
																		// (352
																		// x
																		// 288)
																		// tmedia_pref_video_size_cif
					videoSize = "tmedia_pref_video_size_cif";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_cif;
				}
				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
								videoSize);

				// Compute
				if (!Engine.getInstance().getConfigurationService().commit()) {
					Log.e(TAG, "Failed to commit() configuration");
				} else {
					MediaSessionMgr.defaultsSetPrefVideoSize(value);
				}

				// gzc 20141023 提交成功后上报状态
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_VIDEOQUALITY, BCDTools
								.Str2BCD(videoQuality, 1))); // 上报视频清晰度
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_VIDEOQUALITY,
						"mobileNo = " + "" + "; message = " + "上报视频清晰度 = "
								+ videoQuality);

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_VIDEOQUALITY,
						"mobileNo = " + "" + "; message = " + "设置视频清晰度 = "
								+ videoSize);
			}
				break;

			case BaseSocketMessage.MSG_C_SET_CAMERA: { // 设置摄像头
				// 1个字节压缩BCD码表示，依次为：
				// 1－摄像头1连接 2－摄像头2连接
				String cameraNo = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data"));
				// SocketServer.sendMessage(new
				// BaseSocketMessage(BaseSocketMessage.MSG_S_UPDATE_CAMERA,
				// BCDTools.Str2BCD(cameraNo, 1))); //上报摄像头状态

				boolean useFrontFacingCamera = false; // 1－摄像头1连接 2－摄像头2连接
				if (cameraNo != null && cameraNo.equals("1")) { // -15 1 0xF1
																// 后置摄像头
					useFrontFacingCamera = false;
				}
				if (cameraNo != null && cameraNo.equals("2")) { // -14 2 0xF2
																// 前置摄像头
					useFrontFacingCamera = true;
				}

				Engine.getInstance()
						.getConfigurationService()
						.putBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
								useFrontFacingCamera);

				// Compute
				if (!Engine.getInstance().getConfigurationService().commit()) {
					Log.e(TAG, "Failed to commit() configuration");
				}

				NgnCameraProducer.useFrontFacingCamera = NgnEngine
						.getInstance()
						.getConfigurationService()
						.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
								NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);

				NgnCameraProducer_surface.useFrontFacingCamera = NgnEngine
						.getInstance()
						.getConfigurationService()
						.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
								NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);

				// //切换摄像头
				// NgnObservableHashMap<Long, NgnAVSession> mAVSessions =
				// NgnAVSession.getSessions();
				// for (int i = 0; i < mAVSessions.size(); i++) {
				// mAVSessions.getAt(i).toggleCamera();
				// }
				// gzc
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_CAMERA, BCDTools
								.Str2BCD(cameraNo, (useFrontFacingCamera ? 2
										: 1)))); // 上报摄像头状态
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_VIDEOQUALITY,
						"mobileNo = "
								+ ""
								+ "; "
								+ "message = "
								+ "上报摄像头状态 "
								+ cameraNo
								+ " "
								+ (useFrontFacingCamera ? "front camera"
										: "back camera"));
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_CAMERA, "mobileNo = "
						+ ""
						+ "; message = "
						+ "设置摄像头 "
						+ cameraNo
						+ " "
						+ (useFrontFacingCamera ? "front camera"
								: "back camera"));
			}
				break;

			case BaseSocketMessage.MSG_C_SET_SOUNDMODE: { // 设置放音模式
				// 1个字节压缩BCD码表示，依次为：
				// 0－手柄模式 1－扬声器模式
				String soundMode = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data"));

				// gzc 20141024
				if (soundMode != null && soundMode.equals("1")) {
					SystemVarTools.isSpeakerOn = true;
				} else if (soundMode != null && soundMode.equals("0")) {
					SystemVarTools.isSpeakerOn = false;
				}

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_SOUNDMODE, "mobileNo = "
						+ "" + " message = " + "设置放音模式 :" + soundMode);

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_SOUNDMODE, BCDTools
								.Str2BCD(soundMode, 1))); // 上报放音模式

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_SOUNDMODE, "mobileNo = "
						+ "" + "; message = 上报放音模式 ：" + soundMode);
			}
				break;

			case BaseSocketMessage.MSG_C_SET_CURRENTGROUP: { // 设置当前集群组
				String groupNo = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data")); // 10字节压缩BCD码表示设置组呼组的号码

				if (groupNo.equals("")
						|| !SystemVarTools.isGroupInContact(groupNo)) {
					MyLog.d(TAG, "The groupNo is invalid.");
					return;
				}

				SystemVarTools.setCurrentGroup(groupNo);

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_CURRENTGROUP,
						"mobileNo = " + "" + "; message = " + "设置当前集群组 "
								+ groupNo);

				// gzc
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_CURRENTGROUP, BCDTools
								.Str2BCD(groupNo, 10))); // 上报当前集群组
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_CURRENTGROUP,
						"mobileNo = " + "" + "; message = " + "上报当前集群组 "
								+ groupNo);
			}
				break;

			/**
			 * 3.3.1. 语音业务
			 */
			case BaseSocketMessage.MSG_C_AUDIO_CALLOUT: { // 语音呼出
				String mobileNo = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data"));
				ServiceAV serviceAV = ServiceAV.makeCall(mobileNo,
						NgnMediaType.Audio, SessionType.AudioCall);
				MyLog.d(TAG, "receive audio call outgoing");
				if (serviceAV == null) {
					byte[] ret = new byte[1];
					ret[0] = 4; // 4 is invalid
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_AUDIO_CALLFAILED, ret));
					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_S_AUDIO_CALLFAILED,
							"mobileNo = " + mobileNo + "; message = "
									+ "user is not valid");
				} else {
					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_AUDIO_CALLOUT,
							"mobileNo = " + mobileNo + "; message = " + "语音呼出");
				}

			}
				break;
			case BaseSocketMessage.MSG_C_AUDIO_HANGUP: { // 本机挂机
				NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
						.getSessions();
				if (mAVSessions == null) {
					MyLog.e(TAG, "No active session.");
					return;
				}
				for (int i = 0; i < mAVSessions.size(); i++) {
					NgnAVSession session = mAVSessions.getAt(i);
					if (session != null)
						session.hangUpCall();
				}

				SystemVarTools.isLocalHangUp = true;
				SystemVarTools.isLocalHangUp1 = true;

				String localMobileNo = Engine
						.getInstance()
						.getConfigurationService()
						.getString(
								NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
								NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_AUDIO_HANGUP, "mobileNo = "
						+ localMobileNo + "; message = " + "本机挂机");
			}
				break;

			case BaseSocketMessage.MSG_C_AUDIO_PICK: { // 本机摘机
				NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
						.getSessions();
				if (mAVSessions == null) {
					MyLog.e(TAG, "No active session.");
					return;
				}
				for (int i = 0; i < mAVSessions.size(); i++) {
					NgnAVSession session = mAVSessions.getAt(i);
					if (session != null)
						mAVSessions.getAt(i).acceptCall();
				}

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_AUDIO_PICK, "mobileNo = "
						+ "" + "; message = " + "本机摘机");
			}
				break;

			/**
			 * 3.3.2. 视频业务
			 */
			case BaseSocketMessage.MSG_C_VIDEO_CALLOUT: { // 视频呼出
				byte[] data = intent.getByteArrayExtra("data"); // 11Bytes
				byte[] mobileNobytes = new byte[10];
				try {
					System.arraycopy(data, 0, mobileNobytes, 0, 10);
				} catch (Exception e) {
					MyLog.d(TAG, "type="
							+ BaseSocketMessage.MSG_C_VIDEO_CALLOUT
							+ "  The param is invalid.");
					e.printStackTrace();
				}
				String mobileNo = BCDTools.BCD2Str(mobileNobytes);

				if (NgnStringUtils.isNullOrEmpty(mobileNo)) {
					MyLog.d(TAG, "type="
							+ BaseSocketMessage.MSG_C_VIDEO_CALLOUT
							+ "  The mobileNo is null.");
					return;
				}
				if (!NgnStringUtils.isNumberic(mobileNo)) {
					MyLog.d(TAG, "type="
							+ BaseSocketMessage.MSG_C_VIDEO_CALLOUT
							+ "  The mobileNo is not number.");
					return;
				}

				if (data[10] == 0) { // 0：视频通话
					if (ServiceAV.makeCall(mobileNo, NgnMediaType.AudioVideo,
							SessionType.VideoCall) == null) {
						byte[] ret = new byte[1];
						ret[0] = 0; // 0 is not in your contactList
						SocketServer.sendMessage(new BaseSocketMessage(
								BaseSocketMessage.MSG_S_VIDEO_CALLFAILED, ret));
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_S_VIDEO_CALLFAILED,
								"mobileNo = "
										+ mobileNo
										+ "; message = "
										+ "audioCall out Error: is not in your contactList in adhoc");
					} else
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_VIDEO_CALLOUT,
								"mobileNo = "
										+ mobileNo
										+ "; message = "
										+ "MSG_C_VIDEO_CALLOUT 0: video callout for videocall."); // 视频呼出
																									// 视频通话
				} else if (data[10] == 1) { // 1：视频回传
					// ServiceAV.makeCall(mobileNo, NgnMediaType.AudioVideo,
					// SessionType.VideoUaMonitor);
					ServiceAV.makeCall(mobileNo, NgnMediaType.Video,
							SessionType.VideoUaMonitor);

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_VIDEO_CALLOUT,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "MSG_C_VIDEO_CALLOUT 1: video callout for videoUamonitor."); // 视频呼出
																									// 视频回传
				} else {
					MyLog.d(TAG, "The paramater byte[10] is invalid.");
				}
			}
				break;

			case BaseSocketMessage.MSG_C_VIDEO_HANGUP: { // 本机挂机
				NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
						.getSessions();
				if (mAVSessions == null) {
					MyLog.e(TAG, "No active session.");
					return;
				}
				for (int i = 0; i < mAVSessions.size(); i++) {
					NgnAVSession session = mAVSessions.getAt(i);
					if (session != null) {
						session.hangUpCall();

						SystemVarTools.isLocalHangUp = true;
						SystemVarTools.isLocalHangUp1 = true;

						String localMobileNo = Engine
								.getInstance()
								.getConfigurationService()
								.getString(
										NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
										NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_VIDEO_HANGUP,
								"mobileNo = " + localMobileNo + "; message = "
										+ "本机挂机");
					}
				}
			}
				break;

			case BaseSocketMessage.MSG_C_VIDEO_PICK: { // 本机摘机
				NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
						.getSessions();
				if (mAVSessions == null) {
					MyLog.e(TAG, "No active session.");
					return;
				}
				for (int i = 0; i < mAVSessions.size(); i++) {
					NgnAVSession session = mAVSessions.getAt(i);
					if (session != null) {
						session.acceptCall();

						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_C_VIDEO_PICK,
								"mobileNo = " + "" + "; message = " + "本机摘机");
					}
				}
			}
				break;

			/**
			 * 3.3.3. 集群业务
			 */
			case BaseSocketMessage.MSG_C_GROUP_CALLOUT: { // PTT键发起集群通话
				if (GlobalVar.bADHocMode) {
					return;
				}
				String groupNo = getDefaultGroupNoNull();
				// 建立组呼
				if (groupNo != "") {
					ServiceAV.makeCall(groupNo, NgnMediaType.Audio,
							SessionType.GroupAudioCall);
				} else {
					MyLog.d(TAG, "当前未设置集群组");
				}

				if (NgnAVSession.getSize() == 0) {
					ServiceAV.isPTTRejected = false;
					MyLog.d(TAG, "sessionSeze = 0; isPTTRejected?"
							+ ServiceAV.isPTTRejected);
				}
				MyLog.d(TAG, "isPTTRejected?" + ServiceAV.isPTTRejected);
				MyLog.d(TAG, "AVSessionSize?" + NgnAVSession.getSize());
				if (NgnAVSession.getSize() > 0 /* && !ServiceAV.isPTTRejected */) {
					NgnAVSession session = null;
					if (ServiceAV.getLastServiceAV() != null) {
						session = ServiceAV.getLastServiceAV().getAVSession();
					}
					if (session != null && session.isConnected()) {
						MyLog.d(TAG, "send ptt msg");
						sendPTTRequestInfoMsg();
					}
				} else {
					MyLog.d(TAG, "当前无通话 or rejected");
				}

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_GROUP_CALLOUT, "groupNo = "
						+ groupNo + "; message = "
						+ "MSG_C_GROUP_CALLOUT: groupcall callout."); // PTT键发起集群通话
			}
				break;

			case BaseSocketMessage.MSG_C_GROUP_TERMINATED: { // PTT键结束集群通话
				MyLog.d(TAG,
						"MSG_C_GROUP_TERMINATED time=" + new Date().getTime());
				sendPTTReleaseInfoMsg();
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_GROUP_TERMINATED,
						"mobileNo = "
								+ ""
								+ "; message = "
								+ "MSG_C_GROUP_TERMINATED: groupcall terminated."); // PTT键结束集群通话
			}
				break;

			case BaseSocketMessage.MSG_C_GROUP_EXITED: { // 退出组呼
				sendPTTReleaseInfoMsg();

				if (mTimerAudioPTTReport != null) {
					mTimerAudioPTTReport.cancel();
					mTimerAudioPTTReport = null;
					SystemVarTools.mTimerAudioPTTReport.cancel();
					SystemVarTools.mTimerAudioPTTReport = null;
					SystemVarTools.mTakeAudioPTTFlag = false;
				}

				SystemVarTools.sleep(200);
				NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
						.getSessions();
				if (mAVSessions == null) {
					MyLog.e(TAG, "No active session.");
					return;
				}
				for (int i = 0; i < mAVSessions.size(); i++) {
					NgnAVSession session = mAVSessions.getAt(i);
					if (session != null) {
						int sessionType_terminated = mAVSessions.getAt(i)
								.getSessionType();
						if (sessionType_terminated == SessionType.GroupAudioCall
								|| sessionType_terminated == SessionType.GroupVideoCall) {
							session.hangUpCall();
						}
					}
				}

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_GROUP_EXITED, "mobileNo = "
						+ "" + "; message = "
						+ "MSG_C_GROUP_EXITED: groupcall exited."); // 退出组呼
			}
				break;

			// 3.3.3.1 add by Gongle 2015-11-17
			case BaseSocketMessage.MSG_C_NORMAL_GROUP_AUDIOCALL: {
				String myIP = Engine.getInstance().getNetworkService()
						.getLocalIP(false);
				ServiceAV.makeCall(myIP, NgnMediaType.Audio,
						SessionType.GroupAudioCall, false);
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_NORMAL_GROUP_AUDIOCALL,
						"myIP:"
								+ myIP
								+ "; message = "
								+ "MSG_C_NORMAL_GROUP_AUDIOCALL: groupcall callout."); // PTT键发起集群通话

			}
				break;

			case BaseSocketMessage.MSG_C_SUPER_GROUP_AUDIOCALL: {
				String myIP = Engine.getInstance().getNetworkService()
						.getLocalIP(false);
				ServiceAV.makeCall(myIP, NgnMediaType.Audio,
						SessionType.GroupAudioCall, true);
				// ServiceAdhoc.getInstance().sendPTTRequestCMD();
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SUPER_GROUP_AUDIOCALL,
						"myIP:"
								+ myIP
								+ "; message = "
								+ "MSG_C_NORMAL_GROUP_AUDIOCALL: groupcall callout."); // PTT键发起集群通话

			}
				break;
			case BaseSocketMessage.MSG_C_GROUP_AUDIOEXITED:
				ServiceAdhoc.getInstance().sendPTTReleaseCMD();
				// ServiceAdhoc.getInstance().Hungup();

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_GROUP_AUDIOEXITED,
						"; message = "
								+ "MSG_C_NORMAL_GROUP_AUDIOTERMINATED: groupcall exit."); // PTT键结束集群通话
				break;

			/**
			 * 3.3.4. 短消息
			 */
			case BaseSocketMessage.MSG_C_S_SMS_RESULT: { // ACKS(短消息发送结果-上行)
				byte[] data = intent.getByteArrayExtra("data");
				String result = "成功";
				if (data[0] == 0) {
					result = "成功";
				}
				if (data[0] == 1) {
					result = "失败";
				} else {
					MyLog.d(TAG, "type = "
							+ BaseSocketMessage.MSG_C_S_SMS_RESULT
							+ "   The param is invalid.");
					return;
				}
				System.out.print("短消息发送结果-上行  = " + result);

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_S_SMS_RESULT, "mobileNo = "
						+ "" + "; message = " + "MSG_C_S_SMS_RESULT: "
						+ (data[0] == 0 ? "success" : "fail")); // ACKS(短消息发送结果-上行)
			}
				break;

			case BaseSocketMessage.MSG_C_S_SMS_FORMAT: { // 短消息格式(上行)

				MyLog.e(TAG, "收到短信");

				byte[] data = intent.getByteArrayExtra("data");
				// if(SKDroid.getVersionName().contains("soc_msg")){
				byte[] mobile1Nobytes = new byte[10];
				byte[] mobile2Nobytes = new byte[10];
				System.arraycopy(data, 0, mobile1Nobytes, 0, 10);
				System.arraycopy(data, 10, mobile2Nobytes, 0, 10);
				String mobileNo1 = BCDTools.BCD2Str(mobile1Nobytes);
				// 两个字节记录短信长度
				// int smsLen = data[11]*254+data[12];
				// byte[] byteMessage = new byte[smsLen];
				// System.arraycopy(data, 13, byteMessage, 0, smsLen);

				int smsLen = data[21] & 0xff; // java采用补码，byte表示范围为-128-127，该行将byte变为0-255
				byte[] byteMessage = new byte[smsLen];
				System.arraycopy(data, 22, byteMessage, 0, smsLen);

				String strMessage = null;
				try {
					strMessage = new String(byteMessage,
							SystemVarTools.encoding_gb2312);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				/**
				 * 0：发送短信（手柄->业务软件） 1：接收短信（业务软件->手柄）
				 */
				if (data[20] == 0) {
					ScreenChat.sendMessageDirect(mobileNo1, strMessage);

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_S_SMS_FORMAT,
							"mobileNo = " + mobileNo1 + "; message = "
									+ strMessage);
				} else {

				}
			}
				break;

			/**
			 * 3.3.5. 自组网
			 */
			case BaseSocketMessage.MSG_C_ADHOC_LOGIN: { // 自组网注册
				byte[] data = intent.getByteArrayExtra("data"); // 50Bytes
				if (data == null || data.length != 32) {
					MyLog.d(TAG, "Data is invalid.");
					return;
				}
				byte[] nameBytes = new byte[16];
				byte[] accountBytes = new byte[16];
				System.arraycopy(data, 0, nameBytes, 0, 16);
				System.arraycopy(data, 16, accountBytes, 0, 16);
				String name = null;
				try {
					name = new String(nameBytes, SystemVarTools.encoding_gb2312)
							.trim();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String account = null;
				try {
					account = new String(accountBytes,
							SystemVarTools.encoding_gb2312).trim();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (account == null) {
					MyLog.e(TAG, "account is null.");
					return;
				}
				if (name == null) {
					MyLog.d(TAG, "name is null.use account");
					name = account;
				}
				GlobalVar.bADHocMode = true;
				String myIP = Engine.getInstance().getNetworkService()
						.getLocalIP(false);
				MyLog.d(TAG, "localIP = " + myIP);
				if (myIP != null) {
					ServiceLoginAccount.getInstance()
							.adhoc_Login(name, account);

					ServiceAdhoc.getInstance().StartAdhoc();

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_ADHOC_LOGIN, "name = "
							+ name + "; account = " + account + "; message = "
							+ "自组网注册");

				} else {
					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_ADHOC_LOGIN, "name = "
							+ name + "; account = " + account + "; message = "
							+ "自组网注册, localip == null");
				}

			}
				break;

			case BaseSocketMessage.MSG_C_ADHOC_UNLOGIN: { // 自组网注销
				GlobalVar.bADHocMode = false;
				ServiceAdhoc.getInstance().StopAdhoc();
				ServiceLoginAccount.getInstance().adhoc_Logout();

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_ADHOC_UNLOGIN, "mobileNo = "
						+ "" + "; message = " + "自组网注销");
			}
				break;

			case MSG_S_ADHOC_REFRESH: { // 自组网刷新
				// 自组网上报联系人列表信息
				byte[] dataContact = SystemVarTools.getOrgContactAllBus(0);

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_INIT_CONTACT, dataContact)); // 自组网上报联系人列表信息

				try {
					if (dataContact != null) {
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_S_INIT_CONTACT,
								"mobileNo = "
										+ ""
										+ "; message = "
										+ "自组网上报联系人列表信息 "
										+ new String(dataContact,
												SystemVarTools.encoding_gb2312));
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
				break;

			/**
			 * 3.3.6. 模拟视频输出
			 */
			case BaseSocketMessage.MSG_C_TVOUT_OPEN_WRITE: { // 打开输出设备

				MyLog.i(TAG, "收到打开视频输出指令");
				int HAL_PIXEL_FORMAT_YCbCr_420_SP = 0x11;

				// int ret =
				// TvOut.native_tvout_open(HAL_PIXEL_FORMAT_YCbCr_420_SP);
				int ret = TvOut.native_tvout_open(PixelFormat.YCbCr_420_SP);
				if (ret < 0) { // -13 联芯
					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_TVOUT_OPEN_WRITE,
							"message = " + "打开输出设备失败！");
					break;
				}
				try {

					Handler tvHandler = new Handler() { // getMainLooper()
						public void handleMessage(Message msg) {
							Bundle b = msg.getData();
							int width = b.getInt("width");
							int height = b.getInt("height");
							byte[] buffer = b.getByteArray("data");
							TvOut.native_tvout_write(buffer, width, height);
						}
					};
					MyLog.d(TAG, "TvOut Handler create ok.");

					// NgnProxyVideoProducer.setTvHandler(tvHandler);
					NgnProxyVideoConsumerGL.setTvHandler(tvHandler);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				// }
				// },"TvOut");
				// mTvOutThread.start();

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_TVOUT_OPEN_WRITE,
						"message = " + "打开输出设备！");
			}
				break;

			case BaseSocketMessage.MSG_C_TVOUT_CLOSE: { // 关闭输出设备
				MyLog.i(TAG, "收到关闭视频输出指令");
				// if(mTvOutThread != null){
				NgnProxyVideoConsumerGL.setTvHandler(null);
				// mTvOutThread.interrupt();
				// MyLog.d(TAG, "mTvOutThread.interrupt()");
				// mTvOutThread = null;
				// }
				TvOut.native_tvout_close();

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_TVOUT_CLOSE, "message = "
						+ "关闭输出设备！");
			}
				break;
			case BaseSocketMessage.MSG_C_TVOUT_SET: {

			}
				break;

			case BaseSocketMessage.MSG_C_ALIVE_REQ: {// gzc 适配心跳请求
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_ALIVE_REQ, "message = "
						+ "接收适配心跳请求！");
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_ALIVE_RES)); // 自组网上报联系人列表信息
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_S_ALIVE_RES, "message = "
						+ "发送心跳响应！");
				// 此情况预防ServiceAdhoc自组网服务器没有开启
				if (GlobalVar.bADHocMode) {
					if (!ServiceAdhoc.getInstance().isStartOK()) {
						ServiceAdhoc.getInstance().StartAdhoc();
					}
				}
				break;
			}
			case BaseSocketMessage.MSG_S_GIS_RESPONSE: {
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_S_GIS_RESPONSE, "message = "
						+ "接收GIS信息！");
				byte[] data = intent.getByteArrayExtra("data");
				if (data.length < 32) {
					MyLog.i(TAG, "GIS数据长度错误  data Length = " + data.length);
					break;
				}
				byte[] latitude = new byte[16];
				byte[] longitude = new byte[16];
				System.arraycopy(data, 0, latitude, 0, 16);
				System.arraycopy(data, 16, longitude, 0, 16);
				String lat = "";
				String lon = "";
				try {
					lat = new String(latitude, SystemVarTools.encoding_gb2312);
					lon = new String(longitude, SystemVarTools.encoding_gb2312);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				MyLog.d(TAG, "GIS INFO lat=" + lat + ", lon=" + lon);
				Intent GisIntent = new Intent(MessageTypes.MSG_GIS_RESPONSE);
				GisIntent.putExtra("lat", lat);
				GisIntent.putExtra("lon", lon);
				SKDroid.getContext().sendBroadcast(GisIntent);
				break;
			}

			case BaseSocketMessage.MSG_C_IP_CHANGE: {
				MyLog.d(TAG, "Receive IP change message.");

				INgnSipService sipService = Engine.getInstance()
						.getSipService();

				if (sipService != null) {
					NgnSipStack sipStack = sipService.getSipStack();
					if (sipStack != null) {
						// sipStack.stop();
						// 将sipstack置空
						// MyLog.d(TAG, "SipStack stoped.");_
						MyLog.d(TAG, "Do nothing");
					} else {
						MyLog.d(TAG, "SipStack is null.");
					}
				} else {
					MyLog.d(TAG, "SipService is null.");
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_IP_CHANGE));
				MyLog.d(TAG, "The response if IP changing has sended. ");

				break;
			}
			default:
				break;

			}
		}
	}

	@Override
	public void onMsgReceived(SocketWorker worker, BaseSocketMessage message) {
		System.out.println(message.description());
		Intent intent = new Intent(MESSAGE_SOCKET_INTENT);
		intent.putExtra("pid", GlobalVar.mMyPid);
		intent.putExtra("type", message.getType());
		intent.putExtra("data", message.getData());

		if (mSocketCallback != null) {
			MyLog.d(TAG, "message.getType():" + message.getType());
			MyLog.d(TAG, "message.getType():" + message.getData());
			mSocketCallback.onSocketEvent(intent);
		} else {
			MyLog.d(TAG, "SocketCallback is NULL.");
		}

		// NgnApplication.getContext().sendBroadcast(i);
		// if(GlobalVar.orderedbroadcastSign){
		// NgnApplication.getContext().sendOrderedBroadcast(intent, null);
		// }else {
		// NgnApplication.getContext().sendBroadcast(intent);
		// }
	}

	/**
	 * 
	 * 获取当前集群组，如果为空返回默认组号“110”
	 * */
	public static String getDefaultGroupNo() {
		SharedPreferences groupInfo = NgnApplication.getContext()
				.getSharedPreferences(BaseSocketMessage.SHARED_PREF_GROUP,
						Activity.MODE_PRIVATE);
		String groupNo = groupInfo.getString(
				BaseSocketMessage.DEFAULT_CURRENT_GROUP,
				BaseSocketMessage.DEFAULT_CURRENT_GROUPNO);
		return groupNo;
	}

	/**
	 * 
	 * 获取当前集群组，如果为空返回默认组号“”
	 * */
	public static String getDefaultGroupNoNull() {
		SharedPreferences groupInfo = NgnApplication.getContext()
				.getSharedPreferences(BaseSocketMessage.SHARED_PREF_GROUP,
						Activity.MODE_PRIVATE);
		String groupNo = groupInfo.getString(
				BaseSocketMessage.DEFAULT_CURRENT_GROUP, "");
		return groupNo;
	}

	public static boolean sendPTTRequestInfoMsg() {
		Log.d(TAG, "sendPTTRequestInfoMsg()");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REQUEST);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		Log.d(TAG, "send ptt info msg: " + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	public static boolean sendPTTReleaseInfoMsg() {
		Log.d(TAG, "sendPTTReleaseInfoMsg()");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_RELEASE);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		Log.d(TAG, "send ptt info msg: " + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	private static boolean sendInfo(String content, String contentType) {
		Log.d(TAG, "sendInfo()");
		NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
				.getSessions();
		for (int i = 0; i < mAVSessions.size(); i++) {
			NgnAVSession session = mAVSessions.getAt(i);
			if (session != null) {
				return session.sendInfo(content, contentType);
			}
		}
		return false;
	}

	private static boolean sendPTTReportAliveInfoMsg() {
		Log.d(TAG, "sendPTTReportAliveInfoMsg()");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REPORT);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		msg.setPTTAction(PTTActionTypes.PTT_ACT_ALIVE);
		Log.d(TAG, "send ptt info msg: " + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

}