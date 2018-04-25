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

	public static final int MSG_S_ADHOC_REFRESH = 0X5003; // ������ˢ��
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

	// ��¼����ע�����ڴ���
	private int RgistingCount = 0;
	// ����ע��������ڴ���
	private int MaxRegistingCount = 5;

	private NgnTimer mTimerAudioPTTReport; // �����������

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

	// socket �¼�����ӿ�
	// �������->ҵ�����
	public void onSocketReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		MyLog.i("ServerMsgReceiver: action=", action);
		MyLog.d(TAG, "prient socket event :" + intent.getIntExtra("type", 0));
		if (MESSAGE_SOCKET_INTENT.equals(action)) {
			int type = intent.getIntExtra("type", 0);
			switch (type) {

			/**
			 * 3.1.1. ����� ��ʼ������
			 * ҵ����������󣬽�����������������ĳ�ʼ�������Ϣ���յ���Ӧ������������Ϣ���ϱ�������С���ϱ���Ƶ�����ȡ��ϱ�����ͷ״̬
			 */
			case BaseSocketMessage.MSG_C_INIT_OK: { // ��ʼ�����

				String videoSize = Engine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
								"tmedia_pref_video_size_vga");
				tmedia_pref_video_size_t value = tmedia_pref_video_size_t.tmedia_pref_video_size_720p;
				String videoQuality = "2";
				if (videoSize != null
						&& videoSize.equals("tmedia_pref_video_size_cif")) { // 0
																				// ������
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
																				// ������
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
																				// ������
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
								.Str2BCD("2", 1))); // �ϱ�������С
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_VIDEOQUALITY, BCDTools
								.Str2BCD(videoQuality, 1))); // �ϱ���Ƶ������
				// 1���ֽ�ѹ��BCD���ʾ������Ϊ��
				// 1������ͷ1 2������ͷ2
				boolean useFrontFacingCamera = NgnEngine
						.getInstance()
						.getConfigurationService()
						.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
								NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);
				if (useFrontFacingCamera) { // ǰ������ͷ ģ��
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_UPDATE_CAMERA, BCDTools
									.Str2BCD("2", 1))); // �ϱ�����ͷ״̬ 2
				} else { // ��������ͷ ����
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_UPDATE_CAMERA, BCDTools
									.Str2BCD("1", 1))); // �ϱ�����ͷ״̬1
				}
				Log.e("ServerMsgReceiver: ",
						"BaseSocketMessage.MSG_S_UPDATE_CAMERA = "
								+ (useFrontFacingCamera ? 2 : 1));

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + ""
						+ "; message = " + "�ϱ�������С 2");
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + ""
						+ "; message = " + "�ϱ���Ƶ������ " + videoQuality);
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + ""
						+ "; message = " + "�ϱ�����ͷ״̬ useFrontFacingCamera = "
						+ useFrontFacingCamera);

				// �Ƿ���Ҫע��
				if (Engine.getInstance().getSipService()
						.isRegisteSessionConnected()) { // �ϱ��豸����״̬��Ϣ/�ϱ���ϵ���б���Ϣ/�ϱ�������б���Ϣ
					String localMobileNo = Engine
							.getInstance()
							.getConfigurationService()
							.getString(
									NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
									NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_INIT_STATE, BCDTools
									.Str2BCD(localMobileNo, 10))); // �ϱ��豸����״̬��Ϣ

					ServiceSocketMode.pushcontacts(false);

				} else { // �û���δע��
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_NEEDREG, null));

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = "
							+ "" + "; message = " + "�û���δע��");
				}
			}
				break;

			/**
			 * 3.2.1.3. �û�ע�ᣨ0x03f0��
			 */
			case BaseSocketMessage.MSG_C_USER_REG: { // �û�ע��
				INgnSipService sipService = Engine.getInstance()
						.getSipService();
				REGISTER_COUNT += 1;
				if (sipService == null) {
					MyLog.d(TAG, "SipService is null");
					return;
				}
				ConnectionState cs = sipService.getRegistrationState();
				// ����û�����ע���У����ٷ����µ�ע��
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

				// �Ƿ��Ѿ�ע��
				if (Engine.getInstance().getSipService()
						.isRegisteSessionConnected()) {
					// ֪ͨ��������̨�����û�ע�᷵����Ϣ
					byte[] dataok = new byte[1];
					dataok[0] = 5; // 0��ע��ɹ� 1��������� 2���û������� 5���û��ѵ�¼ 6��ע��ʧ��
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_USER_REGRESULT, dataok));

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_S_USER_REGRESULT,
							"mobileNo = " + "" + "; message = " + "5���û��ѵ�¼");

					// �ϱ��豸����״̬��Ϣ/�ϱ���ϵ���б���Ϣ/�ϱ�������б���Ϣ
					final String localMobileNo = Engine
							.getInstance()
							.getConfigurationService()
							.getString(
									NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
									NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_INIT_STATE, BCDTools
									.Str2BCD(localMobileNo, 10))); // �ϱ��豸����״̬��Ϣ
					ServiceSocketMode.pushcontacts(false);

					break;
				}

				if (!SystemVarTools.setDefaultSetting_socket()) { // Socket�����ļ�������
					SystemVarTools.setDefaultSetting();
				}

				// ��ʼע��
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
						// Log.d(TAG, "��¼������ʧ�ܣ������ѶϿ�");
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
			 * 3.2.1.5. �û�ע��
			 */
			case BaseSocketMessage.MSG_C_USER_UNREG: { // �û�ע��
				// �û�ע��
				Engine.getInstance().getSipService().unRegister();
				GlobalVar.mLogout = true;
				ServiceContact.sessionID = 0;
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_USER_UNREG, "mobileNo = "
						+ "" + "; message = " + "�û�ע��");
				if (!SKDroid.isl8848a_l1860()) {
					System.exit(0);
				}
				// //�û���δע��
				// SocketServer.sendMessage(new
				// BaseSocketMessage(BaseSocketMessage.MSG_S_USER_NEEDREG,
				// null));
				//
				// MyLog.i("ServerMsgReceiver: type = " +
				// BaseSocketMessage.MSG_S_USER_NEEDREG, "mobileNo = " + "" +
				// "; message = " + "�û���δע��");
			}
				break;

			// /**
			// * 3.1.1. ������ָ�0x0000~003f��
			// */
			// case BaseSocketMessage.MSG_C_SET_SOUNDVOLUME: { //����������С
			// String soundVolume =
			// BCDTools.BCD2Str(intent.getByteArrayExtra("data"));
			// SocketServer.sendMessage(new
			// BaseSocketMessage(BaseSocketMessage.MSG_S_UPDATE_SOUNDVOLUME,
			// BCDTools.Str2BCD(soundVolume, 1))); //�ϱ�������С
			//
			// MyLog.i("ServerMsgReceiver: type = " +
			// BaseSocketMessage.MSG_C_SET_SOUNDVOLUME, "mobileNo = " + "" +
			// "; message = " + "����������С 1");
			// }
			// break;

			case BaseSocketMessage.MSG_C_SET_VIDEOQUALITY: { // ������Ƶ������
				// 1���ֽ�ѹ��BCD���ʾ������Ϊ��
				// 0 �C ����
				// 1 �C ����
				// 2 �C ����
				// 3 �C �Զ�
				String videoQuality = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data"));

				String videoSize = "tmedia_pref_video_size_cif";
				tmedia_pref_video_size_t value = tmedia_pref_video_size_t.tmedia_pref_video_size_720p;
				if (videoQuality != null && videoQuality.equals("0")) { // 0 ������
																		// //3
																		// CIF
																		// (352
																		// x
																		// 288)
																		// tmedia_pref_video_size_cif
					videoSize = "tmedia_pref_video_size_cif";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_cif;
				}
				if (videoQuality != null && videoQuality.equals("1")) { // 1 ������
																		// //5
																		// VGA
																		// (640
																		// x
																		// 480)
																		// tmedia_pref_video_size_vga
					videoSize = "tmedia_pref_video_size_vga";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_vga;
				}
				if (videoQuality != null && videoQuality.equals("2")) { // 2 ������
																		// //9
																		// 720P
																		// (1280
																		// x
																		// 720)
																		// tmedia_pref_video_size_720p
					videoSize = "tmedia_pref_video_size_720p";
					value = tmedia_pref_video_size_t.tmedia_pref_video_size_720p;
				}
				if (videoQuality != null && videoQuality.equals("3")) { // 3 ���Զ�
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

				// gzc 20141023 �ύ�ɹ����ϱ�״̬
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_VIDEOQUALITY, BCDTools
								.Str2BCD(videoQuality, 1))); // �ϱ���Ƶ������
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_VIDEOQUALITY,
						"mobileNo = " + "" + "; message = " + "�ϱ���Ƶ������ = "
								+ videoQuality);

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_VIDEOQUALITY,
						"mobileNo = " + "" + "; message = " + "������Ƶ������ = "
								+ videoSize);
			}
				break;

			case BaseSocketMessage.MSG_C_SET_CAMERA: { // ��������ͷ
				// 1���ֽ�ѹ��BCD���ʾ������Ϊ��
				// 1������ͷ1���� 2������ͷ2����
				String cameraNo = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data"));
				// SocketServer.sendMessage(new
				// BaseSocketMessage(BaseSocketMessage.MSG_S_UPDATE_CAMERA,
				// BCDTools.Str2BCD(cameraNo, 1))); //�ϱ�����ͷ״̬

				boolean useFrontFacingCamera = false; // 1������ͷ1���� 2������ͷ2����
				if (cameraNo != null && cameraNo.equals("1")) { // -15 1 0xF1
																// ��������ͷ
					useFrontFacingCamera = false;
				}
				if (cameraNo != null && cameraNo.equals("2")) { // -14 2 0xF2
																// ǰ������ͷ
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

				// //�л�����ͷ
				// NgnObservableHashMap<Long, NgnAVSession> mAVSessions =
				// NgnAVSession.getSessions();
				// for (int i = 0; i < mAVSessions.size(); i++) {
				// mAVSessions.getAt(i).toggleCamera();
				// }
				// gzc
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_CAMERA, BCDTools
								.Str2BCD(cameraNo, (useFrontFacingCamera ? 2
										: 1)))); // �ϱ�����ͷ״̬
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_VIDEOQUALITY,
						"mobileNo = "
								+ ""
								+ "; "
								+ "message = "
								+ "�ϱ�����ͷ״̬ "
								+ cameraNo
								+ " "
								+ (useFrontFacingCamera ? "front camera"
										: "back camera"));
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_CAMERA, "mobileNo = "
						+ ""
						+ "; message = "
						+ "��������ͷ "
						+ cameraNo
						+ " "
						+ (useFrontFacingCamera ? "front camera"
								: "back camera"));
			}
				break;

			case BaseSocketMessage.MSG_C_SET_SOUNDMODE: { // ���÷���ģʽ
				// 1���ֽ�ѹ��BCD���ʾ������Ϊ��
				// 0���ֱ�ģʽ 1��������ģʽ
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
						+ "" + " message = " + "���÷���ģʽ :" + soundMode);

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_SOUNDMODE, BCDTools
								.Str2BCD(soundMode, 1))); // �ϱ�����ģʽ

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_SOUNDMODE, "mobileNo = "
						+ "" + "; message = �ϱ�����ģʽ ��" + soundMode);
			}
				break;

			case BaseSocketMessage.MSG_C_SET_CURRENTGROUP: { // ���õ�ǰ��Ⱥ��
				String groupNo = BCDTools.BCD2Str(intent
						.getByteArrayExtra("data")); // 10�ֽ�ѹ��BCD���ʾ���������ĺ���

				if (groupNo.equals("")
						|| !SystemVarTools.isGroupInContact(groupNo)) {
					MyLog.d(TAG, "The groupNo is invalid.");
					return;
				}

				SystemVarTools.setCurrentGroup(groupNo);

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_CURRENTGROUP,
						"mobileNo = " + "" + "; message = " + "���õ�ǰ��Ⱥ�� "
								+ groupNo);

				// gzc
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_CURRENTGROUP, BCDTools
								.Str2BCD(groupNo, 10))); // �ϱ���ǰ��Ⱥ��
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_SET_CURRENTGROUP,
						"mobileNo = " + "" + "; message = " + "�ϱ���ǰ��Ⱥ�� "
								+ groupNo);
			}
				break;

			/**
			 * 3.3.1. ����ҵ��
			 */
			case BaseSocketMessage.MSG_C_AUDIO_CALLOUT: { // ��������
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
							"mobileNo = " + mobileNo + "; message = " + "��������");
				}

			}
				break;
			case BaseSocketMessage.MSG_C_AUDIO_HANGUP: { // �����һ�
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
						+ localMobileNo + "; message = " + "�����һ�");
			}
				break;

			case BaseSocketMessage.MSG_C_AUDIO_PICK: { // ����ժ��
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
						+ "" + "; message = " + "����ժ��");
			}
				break;

			/**
			 * 3.3.2. ��Ƶҵ��
			 */
			case BaseSocketMessage.MSG_C_VIDEO_CALLOUT: { // ��Ƶ����
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

				if (data[10] == 0) { // 0����Ƶͨ��
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
										+ "MSG_C_VIDEO_CALLOUT 0: video callout for videocall."); // ��Ƶ����
																									// ��Ƶͨ��
				} else if (data[10] == 1) { // 1����Ƶ�ش�
					// ServiceAV.makeCall(mobileNo, NgnMediaType.AudioVideo,
					// SessionType.VideoUaMonitor);
					ServiceAV.makeCall(mobileNo, NgnMediaType.Video,
							SessionType.VideoUaMonitor);

					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_VIDEO_CALLOUT,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "MSG_C_VIDEO_CALLOUT 1: video callout for videoUamonitor."); // ��Ƶ����
																									// ��Ƶ�ش�
				} else {
					MyLog.d(TAG, "The paramater byte[10] is invalid.");
				}
			}
				break;

			case BaseSocketMessage.MSG_C_VIDEO_HANGUP: { // �����һ�
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
										+ "�����һ�");
					}
				}
			}
				break;

			case BaseSocketMessage.MSG_C_VIDEO_PICK: { // ����ժ��
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
								"mobileNo = " + "" + "; message = " + "����ժ��");
					}
				}
			}
				break;

			/**
			 * 3.3.3. ��Ⱥҵ��
			 */
			case BaseSocketMessage.MSG_C_GROUP_CALLOUT: { // PTT������Ⱥͨ��
				if (GlobalVar.bADHocMode) {
					return;
				}
				String groupNo = getDefaultGroupNoNull();
				// �������
				if (groupNo != "") {
					ServiceAV.makeCall(groupNo, NgnMediaType.Audio,
							SessionType.GroupAudioCall);
				} else {
					MyLog.d(TAG, "��ǰδ���ü�Ⱥ��");
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
					MyLog.d(TAG, "��ǰ��ͨ�� or rejected");
				}

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_GROUP_CALLOUT, "groupNo = "
						+ groupNo + "; message = "
						+ "MSG_C_GROUP_CALLOUT: groupcall callout."); // PTT������Ⱥͨ��
			}
				break;

			case BaseSocketMessage.MSG_C_GROUP_TERMINATED: { // PTT��������Ⱥͨ��
				MyLog.d(TAG,
						"MSG_C_GROUP_TERMINATED time=" + new Date().getTime());
				sendPTTReleaseInfoMsg();
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_GROUP_TERMINATED,
						"mobileNo = "
								+ ""
								+ "; message = "
								+ "MSG_C_GROUP_TERMINATED: groupcall terminated."); // PTT��������Ⱥͨ��
			}
				break;

			case BaseSocketMessage.MSG_C_GROUP_EXITED: { // �˳����
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
						+ "MSG_C_GROUP_EXITED: groupcall exited."); // �˳����
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
								+ "MSG_C_NORMAL_GROUP_AUDIOCALL: groupcall callout."); // PTT������Ⱥͨ��

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
								+ "MSG_C_NORMAL_GROUP_AUDIOCALL: groupcall callout."); // PTT������Ⱥͨ��

			}
				break;
			case BaseSocketMessage.MSG_C_GROUP_AUDIOEXITED:
				ServiceAdhoc.getInstance().sendPTTReleaseCMD();
				// ServiceAdhoc.getInstance().Hungup();

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_GROUP_AUDIOEXITED,
						"; message = "
								+ "MSG_C_NORMAL_GROUP_AUDIOTERMINATED: groupcall exit."); // PTT��������Ⱥͨ��
				break;

			/**
			 * 3.3.4. ����Ϣ
			 */
			case BaseSocketMessage.MSG_C_S_SMS_RESULT: { // ACKS(����Ϣ���ͽ��-����)
				byte[] data = intent.getByteArrayExtra("data");
				String result = "�ɹ�";
				if (data[0] == 0) {
					result = "�ɹ�";
				}
				if (data[0] == 1) {
					result = "ʧ��";
				} else {
					MyLog.d(TAG, "type = "
							+ BaseSocketMessage.MSG_C_S_SMS_RESULT
							+ "   The param is invalid.");
					return;
				}
				System.out.print("����Ϣ���ͽ��-����  = " + result);

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_S_SMS_RESULT, "mobileNo = "
						+ "" + "; message = " + "MSG_C_S_SMS_RESULT: "
						+ (data[0] == 0 ? "success" : "fail")); // ACKS(����Ϣ���ͽ��-����)
			}
				break;

			case BaseSocketMessage.MSG_C_S_SMS_FORMAT: { // ����Ϣ��ʽ(����)

				MyLog.e(TAG, "�յ�����");

				byte[] data = intent.getByteArrayExtra("data");
				// if(SKDroid.getVersionName().contains("soc_msg")){
				byte[] mobile1Nobytes = new byte[10];
				byte[] mobile2Nobytes = new byte[10];
				System.arraycopy(data, 0, mobile1Nobytes, 0, 10);
				System.arraycopy(data, 10, mobile2Nobytes, 0, 10);
				String mobileNo1 = BCDTools.BCD2Str(mobile1Nobytes);
				// �����ֽڼ�¼���ų���
				// int smsLen = data[11]*254+data[12];
				// byte[] byteMessage = new byte[smsLen];
				// System.arraycopy(data, 13, byteMessage, 0, smsLen);

				int smsLen = data[21] & 0xff; // java���ò��룬byte��ʾ��ΧΪ-128-127�����н�byte��Ϊ0-255
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
				 * 0�����Ͷ��ţ��ֱ�->ҵ������� 1�����ն��ţ�ҵ�����->�ֱ���
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
			 * 3.3.5. ������
			 */
			case BaseSocketMessage.MSG_C_ADHOC_LOGIN: { // ������ע��
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
							+ "������ע��");

				} else {
					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_ADHOC_LOGIN, "name = "
							+ name + "; account = " + account + "; message = "
							+ "������ע��, localip == null");
				}

			}
				break;

			case BaseSocketMessage.MSG_C_ADHOC_UNLOGIN: { // ������ע��
				GlobalVar.bADHocMode = false;
				ServiceAdhoc.getInstance().StopAdhoc();
				ServiceLoginAccount.getInstance().adhoc_Logout();

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_ADHOC_UNLOGIN, "mobileNo = "
						+ "" + "; message = " + "������ע��");
			}
				break;

			case MSG_S_ADHOC_REFRESH: { // ������ˢ��
				// �������ϱ���ϵ���б���Ϣ
				byte[] dataContact = SystemVarTools.getOrgContactAllBus(0);

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_INIT_CONTACT, dataContact)); // �������ϱ���ϵ���б���Ϣ

				try {
					if (dataContact != null) {
						MyLog.i("ServerMsgReceiver: type = "
								+ BaseSocketMessage.MSG_S_INIT_CONTACT,
								"mobileNo = "
										+ ""
										+ "; message = "
										+ "�������ϱ���ϵ���б���Ϣ "
										+ new String(dataContact,
												SystemVarTools.encoding_gb2312));
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
				break;

			/**
			 * 3.3.6. ģ����Ƶ���
			 */
			case BaseSocketMessage.MSG_C_TVOUT_OPEN_WRITE: { // ������豸

				MyLog.i(TAG, "�յ�����Ƶ���ָ��");
				int HAL_PIXEL_FORMAT_YCbCr_420_SP = 0x11;

				// int ret =
				// TvOut.native_tvout_open(HAL_PIXEL_FORMAT_YCbCr_420_SP);
				int ret = TvOut.native_tvout_open(PixelFormat.YCbCr_420_SP);
				if (ret < 0) { // -13 ��о
					MyLog.i("ServerMsgReceiver: type = "
							+ BaseSocketMessage.MSG_C_TVOUT_OPEN_WRITE,
							"message = " + "������豸ʧ�ܣ�");
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
						"message = " + "������豸��");
			}
				break;

			case BaseSocketMessage.MSG_C_TVOUT_CLOSE: { // �ر�����豸
				MyLog.i(TAG, "�յ��ر���Ƶ���ָ��");
				// if(mTvOutThread != null){
				NgnProxyVideoConsumerGL.setTvHandler(null);
				// mTvOutThread.interrupt();
				// MyLog.d(TAG, "mTvOutThread.interrupt()");
				// mTvOutThread = null;
				// }
				TvOut.native_tvout_close();

				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_TVOUT_CLOSE, "message = "
						+ "�ر�����豸��");
			}
				break;
			case BaseSocketMessage.MSG_C_TVOUT_SET: {

			}
				break;

			case BaseSocketMessage.MSG_C_ALIVE_REQ: {// gzc ������������
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_ALIVE_REQ, "message = "
						+ "����������������");
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_ALIVE_RES)); // �������ϱ���ϵ���б���Ϣ
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_S_ALIVE_RES, "message = "
						+ "����������Ӧ��");
				// �����Ԥ��ServiceAdhoc������������û�п���
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
						+ "����GIS��Ϣ��");
				byte[] data = intent.getByteArrayExtra("data");
				if (data.length < 32) {
					MyLog.i(TAG, "GIS���ݳ��ȴ���  data Length = " + data.length);
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
						// ��sipstack�ÿ�
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
	 * ��ȡ��ǰ��Ⱥ�飬���Ϊ�շ���Ĭ����š�110��
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
	 * ��ȡ��ǰ��Ⱥ�飬���Ϊ�շ���Ĭ����š���
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