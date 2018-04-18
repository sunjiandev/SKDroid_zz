/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
 *	
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 *
 * imsdroid is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *	
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.sunkaisens.skdroid.app.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.events.AdhocSessionEventArgs;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMessagingEventTypes;
import org.doubango.ngn.events.NgnMsrpEventArgs;
import org.doubango.ngn.events.NgnPublicationEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.model.NgnHistoryPushEvent;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.sip.NgnSubscriptionSession.EventPackageType;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnSipCode;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.sks.net.socket.message.BaseSocketMessage;
import com.sks.net.socket.server.ServerMsgReceiver;
import com.sks.net.socket.server.SocketEventCallback;
import com.sks.net.socket.server.SocketServer;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenAV;
import com.sunkaisens.skdroid.Screens.ScreenChat;
import com.sunkaisens.skdroid.Screens.ScreenDownloadConcacts;
import com.sunkaisens.skdroid.Screens.ScreenMap;
import com.sunkaisens.skdroid.Screens.ScreenTabContact;
import com.sunkaisens.skdroid.Services.IServiceScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Services.ServiceAdhoc;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceContact.ContactGetThread;
import com.sunkaisens.skdroid.Services.ServiceContact.ContactReqThread;
import com.sunkaisens.skdroid.Services.ServiceGPSReport;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Services.ServiceMessage;
import com.sunkaisens.skdroid.Services.ServiceRegiste;
import com.sunkaisens.skdroid.Services.ServiceSocketMode;
import com.sunkaisens.skdroid.Utils.ChkVer;
import com.sunkaisens.skdroid.Utils.ParserSubscribeState;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.Utils.XmlDoc;
import com.sunkaisens.skdroid.cpim.CPIMMessage;
import com.sunkaisens.skdroid.cpim.CPIMParser;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.ModelFileTransport;
import com.sunkaisens.skdroid.model.ModelPush;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

@SuppressLint("HandlerLeak")
public class NativeService extends NgnNativeService implements
		SocketEventCallback {
	private final static String TAG = NativeService.class.getCanonicalName();
	public static final String ACTION_STATE_EVENT = TAG + ".ACTION_STATE_EVENT";
	private final String DAEMONPROCESS = "com.sunkaisens.skdroid:back";

	private PowerManager.WakeLock mWakeLock;
	private BroadcastReceiver mBroadcastReceiver;
	private Engine mEngine;
	private final INgnConfigurationService mConfigurationService;

	private NgnSipStack sipStack;
	private INgnSipService sipService;

	private BroadcastReceiver mCdmaCallReceiver;

	private BroadcastReceiver mSystemMessageReceiver;

	private Handler mContactCallbackHandler;

	private ContactReqThread mContactReqThread = null;

	private ContactGetThread mContactDownloadThread = null;

	private AlertDialog mCallTipsDialog;

	private NgnTimer mCallTipsTimer;// 控制通话结束提示消失的计时器

	private Thread serverThread = new Thread("SocketServer") {
		@Override
		public void run() {
			SocketServer ss = new SocketServer(NativeService.this);
			MyLog.d(TAG, "SocketServer : " + ss);
		}
	};

	private static boolean isNotify = true;

	private NgnTimer notifyTimer = null;

	public NativeService() {
		super();
		MyLog.d(TAG, "NativeService()");

		mEngine = (Engine) Engine.getInstance();
		this.mConfigurationService = mEngine.getConfigurationService();

		sipService = mEngine.getSipService();

		/**
		 * 启动Socket命令接收处理服务器
		 */
		if (GlobalSession.bSocketService && !serverThread.isAlive()) {
			serverThread.start();
			ServiceAdhoc.getInstance().mSocketCallback = NativeService.this;
		}

		isNotify = true;
		notifyTimer = new NgnTimer();

	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		MyLog.d(TAG, "onCreate()");
		MyLog.d(TAG, "APP Version=" + SKDroid.getVersionName());
		MyLog.d(TAG, "SKS_VERSION=" + SKDroid.sks_version + "libs_version = "
				+ NgnApplication.getContext().getString(R.string.libs_version));

		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
		if (powerManager != null && mWakeLock == null) {
			// mWakeLock =
			// powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE
			// | PowerManager.SCREEN_BRIGHT_WAKE_LOCK
			// | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
			mWakeLock = powerManager.newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
		}

		if (SKDroid.sks_version != VERSION.SOCKET) {
			ServiceRegiste.getservice().networkCheck();
		} else {
			// GlobalVar.orderedbroadcastSign = true;
		}

		// 初始化sip消息接收器
		initSipMessageReceiver();

		// 初始化系统消息接收器
		// initSystemMessageReceiver();

		// 初始化系统电话状态消息接收器
		// initCdmaCallStateReceiver();

		mContactCallbackHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case MessageTypes.MSG_REQ_CONTACTS_SUCCESS:

					if (mContactReqThread != null) {
						mContactReqThread.interrupt();
						mContactReqThread = null;
					}

					ServiceContact.auidForPresence = "";
					// 通讯录下载前清空订阅列表
					ServiceContact.clearContacts();
					SystemVarTools.clear();
					// 通讯录变更相关订阅，与呈现的订阅无关
					SystemVarTools.subscribePersionInfo();
					SystemVarTools.subscribePublicGroup();
					SystemVarTools.subscribeServiceGroup();
					SystemVarTools.subscribeGlobalGroup();
					SystemVarTools.subscribeSubscribeGroup();

					mContactDownloadThread = new ContactGetThread(
							"QueryContacts", this);

					String contractReqParser = ScreenDownloadConcacts
							.getInstance().parserContactReq();

					if (contractReqParser != null
							&& !contractReqParser.equals("failed")) {
						MyLog.e(TAG, "1 contractReqParser :"
								+ ServiceContact.sessionID);
						if (ServiceContact.sessionID != 0) {
							NgnSubscriptionSession session = NgnSubscriptionSession
									.getSession(ServiceContact.sessionID);

							if (session != null) {
								session.unSubscribe();
								MyLog.e(TAG, "send unSubscribe() Session: "
										+ ServiceContact.sessionID);
							}
						}

						ServiceContact.sessionID = 0;

						if (contractReqParser.contains("public-group")
								|| contractReqParser
										.contains("org.openmobilealliance.group-usage-list-sy")) {
							mContactDownloadThread
									.setCanDownloadPublicGroup(true);
							ServiceContact.auidForPresence += "auid/public-group,";

						} else {
							mContactDownloadThread
									.setCanDownloadPublicGroup(false);
						}

						if (contractReqParser
								.contains("org.openmobilealliance.group-usage-list-sy")) {
							mContactDownloadThread
									.setCanDownloadPublicGroup(true);
						}

						if (contractReqParser.contains("service-group")) {
							mContactDownloadThread
									.setCanDownloadServiceGroup(true);

							ServiceContact.auidForPresence += "auid/service-group,";
						} else
							mContactDownloadThread
									.setCanDownloadServiceGroup(false);

						if (contractReqParser.contains("comm-group")) {
							mContactDownloadThread
									.setCanDownloadCommGroup(true);

							ServiceContact.auidForPresence += "auid/comm-group,";

						} else
							mContactDownloadThread
									.setCanDownloadCommGroup(false);

						if (contractReqParser.contains("global-group")) {
							mContactDownloadThread
									.setCanDownloadGlobalGroup(true);

							ServiceContact.auidForPresence += "auid/global-group,";
						} else
							mContactDownloadThread
									.setCanDownloadGlobalGroup(false);

						if (contractReqParser.contains("subscribe-group")) {
							mContactDownloadThread
									.setCanDownloadSubscribeGroup(true);

						} else {
							mContactDownloadThread
									.setCanDownloadSubscribeGroup(false);
						}

						if (ServiceContact.auidForPresence != ""
								&& ServiceContact.auidForPresence.endsWith(",")) {

							int length = ServiceContact.auidForPresence
									.length();
							ServiceContact.auidForPresence = ServiceContact.auidForPresence
									.substring(0, length - 1);
						}

					}

					mContactDownloadThread.start();

					break;

				case MessageTypes.MSG_DOWNLOAD_FINISH:
					// SystemVarTools.showToast("通讯录下载完成", false);

					Log.e(TAG, "MSG_DOWNLOAD_FINISH");

					// 通讯录下载完成发送刷新消息，用于界面更新显示
					ServiceContact.sendContactFrashMsg();

					ScreenDownloadConcacts.getInstance().clearXML();

					if (SKDroid.sks_version == VERSION.SOCKET) {
						ServiceSocketMode.pushcontacts(true);
					}

					// ywh 下载完成后,取消下拉刷新
					if (ScreenTabContact.refreshableView != null) {
						ScreenTabContact.refreshableView.finishRefreshing();
					}

					if (ServiceContact.auidForPresence != ""
							&& sipService.getSipStack() != null
							&& sipService.getSipStack().isValid()) {
						ServiceContact.subAll(ServiceContact.auidForPresence);
					}

					break;

				case MessageTypes.MSG_REQ_CONTACTS_FAILED:
					SystemVarTools.showToast(
							getApplicationContext().getString(
									R.string.contacts_failed_check_net), false);
					// ywh 通讯录请求失败,取消下拉刷新
					if (ScreenTabContact.refreshableView != null) {
						ScreenTabContact.refreshableView.finishRefreshing();
					}

					break;
				case MessageTypes.MSG_DOWNLOAD_CONTACTS_FAILED:

					SystemVarTools.showToast(
							getApplicationContext().getString(
									R.string.download_contacts_failed), false);

					break;

				case MessageTypes.MSG_DOWNLOAD_CONTACTSNET_FAILED:

					SystemVarTools
							.showToast(
									getApplicationContext()
											.getString(
													R.string.download_bussiness_contacts_failed),
									false);
					break;

				case MessageTypes.MSG_DOWNLOAD_CONTACTSCOMMGROUP_FAILED:

					SystemVarTools.showToast(
							getApplicationContext().getString(
									R.string.download_group_contacts_failed),
							false);

					break;

				case MessageTypes.MSG_DOWNLOAD_CONTACTSGLOBALGROUP_FAILED:

					SystemVarTools.showToast(
							getApplicationContext().getString(
									R.string.download_global_contacts_failed),
							false);

					break;

				case MessageTypes.MSG_DOWNLOAD_CONTACTSSUBSCRIBEGROUP_FAILED:

					SystemVarTools
							.showToast(
									getApplicationContext()
											.getString(
													R.string.download_subcribe_contacts_failed),
									false);

					break;

				default:
					break;
				}

			};
		};

	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "NativeService onStart()");
		try {
			// register()

			if (intent != null) {
				Bundle bundle = intent.getExtras();
				if (bundle != null && bundle.getBoolean("autostarted")) {
					if (mEngine.start()) {
						mEngine.getSipService().register(null);
					}
				}
			}

			// alert()
			final Intent i = new Intent(ACTION_STATE_EVENT);
			i.putExtra("started", true);
			sendBroadcast(i);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		MyLog.d(TAG, "onTrimMemory(" + level + ")");
	}

	/**
	 * 初始化四篇、消息接收器
	 */
	private void initSipMessageReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				MyLog.d(TAG, "FUNC:NativeService.onreceive()");

				final String action = intent.getAction();
				int pid = intent.getIntExtra("pid", 0);
				MyLog.d(TAG, "进程id标记:" + pid + "   action:" + action);

				// Registration Events
				if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT
						.equals(action)) {
					Register_OnReceive(context, intent);
				}

				// publication Events
				if (NgnPublicationEventArgs.ACTION_PUBLICATION_EVENT
						.equals(action)) {
					Publish_OnReceive(context, intent);
				}
				// subscribe event
				if (NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT
						.equals(action)) {
					SubScribe_OnReceive(context, intent);
				}

				// PagerMode Messaging Events
				if (NgnMessagingEventArgs.ACTION_MESSAGING_EVENT.equals(action)) {
					if (!Message_OnReceive(context, intent)) {
						return;
					}
				}

				// MSRP chat Events
				// For performance reasons, file transfer events will be
				// handled
				// by the owner of the context
				if (NgnMsrpEventArgs.ACTION_MSRP_EVENT.equals(action)) {
					Msrp_OnReceive(context, intent);
				}

				// Invite Events
				else if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
					Log.d(TAG, "InviteEvent Invite_OnReceive");
					boolean sign = Invite_OnReceive(context, intent);
					if (!sign) {
						return;
					}
				}

				// /**
				// * socket 事件处理接口
				// */
				// if (ServerMsgReceiver.MESSAGE_SOCKET_INTENT.equals(action)) {
				// // 方向：适配软件->业务软件
				// ServerMsgReceiver.onSocketReceive(context, intent);
				// }
				if (GlobalSession.bSocketService) { // 方向：适配软件<-业务软件
					if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT
							.equals(action)
							|| NgnMessagingEventArgs.ACTION_MESSAGING_EVENT
									.equals(action)
							|| NgnInviteEventArgs.ACTION_INVITE_EVENT
									.equals(action)
							|| NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT
									.equals(action)
							|| AdhocSessionEventArgs.ADHOC_SESSION_EVENT
									.equals(action)
							|| MessageTypes.MSG_GIS_EVENT.equals(action)) { // add
																			// the
																			// event
						// here.
						ServiceSocketMode.getInstance().onReceive(context,
								intent);
					}
				}

				if (!GlobalSession.bSocketService
						&& NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT
								.equals(action)) { // wangds
													// sample,add,2014.5.21
					ServiceLoginAccount.getInstance()
							.onReceive(context, intent);
				}

				// screen av
				if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)
						|| NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT
								.equals(action)) { // wangds,add,2014.5.24
					ServiceAV.onReceive(context, intent);
				}

				if (!GlobalSession.bSocketService
						&& GlobalVar.bADHocMode == false
						&& ConnectivityManager.CONNECTIVITY_ACTION
								.equals(action)) {
					Log.d(TAG, "network state changed...bLogin: "+SystemVarTools.bLogin);

					if (SystemVarTools.bLogin) {
						if (ServiceRegiste.isNeedRedownloadContacts
								&& sipService.isRegisteSessionConnected()) {
							Log.d(TAG, "isNeedRedownloadContacts:"
									+ ServiceRegiste.isNeedRedownloadContacts);
							ServiceRegiste
									.sendContactStatus(ServiceRegiste.NET_DOWNLOAD_CONTACTS);
							ServiceRegiste.isNeedRedownloadContacts = false;
						}
					
						String newIp = mEngine.getNetworkService().getLocalIP(false);
						Log.d(TAG, "network state changed, newIp: " + newIp);
						if(newIp != null && CrashHandler.isNetworkAvailable()) {
							sipService.refrashRegiste();
						}
						if (!NgnStringUtils.isNullOrEmpty(GlobalVar.mCurrIp)
								&& !NgnStringUtils.isNullOrEmpty(newIp)
								&& !newIp.equals(GlobalVar.mCurrIp)) {
							ServiceRegiste.isIpChange = true;
							GlobalVar.mCurrIp = newIp;
						}
					}
					
				}

				if (MessageTypes.MSG_CONTACT_EVENT.equals(action)) {
					int mRegCmdType = intent.getIntExtra(
							MessageTypes.MSG_CONTACT_EVENT, 0);
					if (mRegCmdType == ServiceRegiste.NET_DOWNLOAD_CONTACTS) {
						// ServiceContact.sendSubscribe();
						mContactReqThread = new ContactReqThread("QContactOpt",
								mContactCallbackHandler);
						mContactReqThread.start();
					}
				}
				if (MessageTypes.MSG_STACK_EVENT.equals(action)) {
					int mRegCmdType = intent.getIntExtra(
							MessageTypes.MSG_STACK_EVENT, 0);
					if (MessageTypes.MSG_STACK_NEED_STOP == mRegCmdType) {
						if (ServiceContact.mContactAll != null) {
							for (String mobileNO : ServiceContact.mContactAll
									.keySet()) {
								if (ServiceContact.mContactAll.get(mobileNO).contact != null) {
									ServiceContact.mContactAll.get(mobileNO).contact.isOnline = false;
								} else {
									MyLog.e(TAG, "mobileNO'contact is null.");
								}
							}
							ServiceContact.sendContactFrashMsg();
						} else {
							MyLog.e(TAG, "ServiceContact.mContactAll is null.");
						}
						sipService.unRegister();
					}
				}

				if (AdhocSessionEventArgs.ADHOC_SESSION_EVENT.equals(action)) {
					ServiceAV.onReceive(context, intent);
				}

				if (GlobalVar.orderedbroadcastSign
						&& !ConnectivityManager.CONNECTIVITY_ACTION
								.equals(action)) {
					abortBroadcast();
					Log.d(TAG, "NativeService 执行abortBroadcast");
				}
			}

		};
		final IntentFilter intentFilter = new IntentFilter();
		// 集群语音业务
		intentFilter.addAction(AdhocSessionEventArgs.ADHOC_SESSION_EVENT);

		intentFilter
				.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intentFilter
				.addAction(NgnPublicationEventArgs.ACTION_PUBLICATION_EVENT);
		intentFilter
				.addAction(NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intentFilter.addAction(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
		// socket intent
		intentFilter.addAction(ServerMsgReceiver.MESSAGE_SOCKET_INTENT); // 添加socket
		// screen av
		intentFilter
				.addAction(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT); // intent
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(MessageTypes.MSG_NET_EVENT);
		intentFilter.addAction(MessageTypes.MSG_CONTACT_EVENT);
		intentFilter.addAction(MessageTypes.MSG_GIS_EVENT);
		intentFilter.addAction(MessageTypes.MSG_STACK_EVENT);

		intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);

		registerReceiver(mBroadcastReceiver, intentFilter);
	}

	/**
	 * 初始化系统消息接收器
	 */
	private void initSystemMessageReceiver() {
		mSystemMessageReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				MyLog.d(TAG, "SystemMsg  action=" + action);
				if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
					MyLog.d(TAG, "加载存储空间");
					mEngine.getStorageService().initFilePath();
					MyLog.init(mEngine.getStorageService().getSdcardDir());
					SystemVarTools.initFiles(mEngine.getStorageService()
							.getSdcardDir());
				} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
					MyLog.d(TAG, "存储空间被卸载");
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addDataScheme("file");
		registerReceiver(mSystemMessageReceiver, intentFilter);
	}

	/**
	 * 初始化系统电话状态消息接收器
	 */
	private void initCdmaCallStateReceiver() {
		if (!SystemVarTools.useCdmaNetwork) {
			return;
		} else {
			TelephonyManager tpm = (TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE);
			tpm.listen(new MyPhoneStateListener(),
					PhoneStateListener.LISTEN_CALL_STATE);

			mCdmaCallReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context arg0, Intent arg1) {
					String action = arg1.getAction();
					Log.i(TAG, "cdma call action = " + action);
					if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
						try {

							String callNum = arg1
									.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
							Log.d(TAG, "cdma 电话呼出 callNum=" + callNum);

							String validUri = NgnUriUtils
									.makeValidSipUri(callNum);
							NgnHistoryEvent historyEvent = new NgnHistoryAVCallEvent(
									false, validUri);

							historyEvent.setStartTime(new Date().getTime());
							historyEvent.setEndTime(historyEvent.getEndTime());
							historyEvent.setStatus(StatusType.Outgoing);
							historyEvent.setmLocalParty(GlobalVar.mLocalNum);
							NgnEngine.getInstance().getHistoryService()
									.addEvent(historyEvent);

						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						try {

							String callNum = arg1
									.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
							Log.d(TAG, "cdma 电话呼入 callNum=" + callNum);

							String validUri = NgnUriUtils
									.makeValidSipUri(callNum);
							NgnHistoryEvent historyEvent = new NgnHistoryAVCallEvent(
									false, validUri);

							historyEvent.setStartTime(new Date().getTime());
							historyEvent.setEndTime(historyEvent.getEndTime());
							historyEvent.setStatus(StatusType.Incoming);
							historyEvent.setmLocalParty(GlobalVar.mLocalNum);
							NgnEngine.getInstance().getHistoryService()
									.addEvent(historyEvent);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			};
			IntentFilter cdmaFilter = new IntentFilter();
			cdmaFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
			cdmaFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
			registerReceiver(mCdmaCallReceiver, cdmaFilter);
		}
	}

	private boolean hasShowTermTips = false;

	private void startCallTipsTimer() {
		if (mCallTipsTimer != null) {
			mCallTipsTimer.cancel();
			mCallTipsTimer.purge();
			mCallTipsTimer = null;
		}
		final AlertDialog dialog = mCallTipsDialog;
		mCallTipsTimer = new NgnTimer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		};
		mCallTipsTimer.schedule(task, 2000);
	}

	protected boolean Invite_OnReceive(Context context, Intent intent) {
		NgnInviteEventArgs args = intent
				.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
		if (args == null) {
			Log.e(TAG, "Invalid event args");
			return false;
		}

		NgnInviteEventTypes eventType = args.getEventType();
		Log.d(TAG, "Receive a call,handling...eventtype=" + eventType);

		final String phrase = args.getPhrase();

		Log.d(TAG, "Receive a call,handling...phrase=" + phrase);

		final NgnMediaType mediaType = args.getMediaType();

		// switch (args.getEventType()) {
		switch (eventType) {
		case TERMWAIT:
			;
		case TERMINATED:
			Log.d(TAG, "InviteEvent TERMINATED");
			if (NgnMediaType.isAudioVideoType(mediaType)) {

				// 如果未接来电数有变动，进行提示
				if (SystemVarTools.avCallNotNumberLast != SystemVarTools.avCallNotNumber) {
					mEngine.showAVCallNotNotif(R.drawable.phone_call_26);
					SystemVarTools.avCallNotNumberLast = SystemVarTools.avCallNotNumber;
				} else {
					if (!hasShowTermTips) {
						SystemVarTools.showToast(getApplicationContext()
								.getString(R.string.call_finished_with_dot),
								false);
						// if(mCallTipsDialog == null){
						// Builder builder = new
						// Builder(mEngine.getMainActivity());
						// View view =
						// LayoutInflater.from(mEngine.getMainActivity())
						// .inflate(R.layout.call_tips_dialog, null);
						// builder.setView(view);
						// builder.setInverseBackgroundForced(true);
						// mCallTipsDialog = builder.create();
						// }
						// mCallTipsDialog.show();
						// startCallTipsTimer();
						hasShowTermTips = true;
					}
				}
				if (NgnAVSession.getSize() == 0) {
					if (mWakeLock != null) {
						if (mWakeLock.isHeld()) {
							mWakeLock.release();
						}
					}
				}

				mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
				Log.d(TAG, "InviteEvent refreshAVCallNotif");
				// if (SystemVarTools.isAVCalling()) { //提示不可拨打 暂不修改
				// SystemVarTools.showToast("正在通话中，请稍候再呼叫！");
				// mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
				// }
				mEngine.getSoundService().stopRingBackTone();
				Log.d(TAG, "InviteEvent stopRingBackTone");

				mEngine.getSoundService().stopRingTone();
				Log.d(TAG, "InviteEvent stopRingTone");

				Main.isFirstPTT_onKeyDown = true;
				Main.isFirstPTT_onKeyLongPress = true;

			} else if (NgnMediaType.isFileTransfer(mediaType)) {
				mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
			} else if (NgnMediaType.isChat(mediaType)) {
				mEngine.refreshChatNotif(R.drawable.chat_25);
			}

			break;

		case INCOMING:

			if (NgnMediaType.isAudioVideoType(mediaType)) {
				Log.d(TAG, "imcomCall 视频 / 语音");

				// if(mCallTipsDialog != null && mCallTipsDialog.isShowing()){
				// mCallTipsDialog.dismiss();
				// }
				SystemVarTools.avCallNotNumber++;

				if (NgnAVSession.getSize() > 1) { // 只允许一个拨打进来,优先级一样。高优先级打低优先级会先收到Byte消息
					final NgnAVSession avSession = NgnAVSession.getSession(args
							.getSessionId());
					if (avSession != null) {
						mEngine.showAVCallNotNotif(R.drawable.phone_call_26);
						avSession.hangUpCall();
					}
					return false;
				}

				final NgnAVSession avSession = NgnAVSession.getSession(args
						.getSessionId());

				if (avSession != null) {
					mEngine.showAVCallNotif(R.drawable.phone_call_25,
							getString(R.string.string_call_incoming));
					ServiceAV.receiveCall(avSession); // 来电呼入
					if (mWakeLock != null && !mWakeLock.isHeld()) {
						mWakeLock.acquire();
					}
					MyLog.d(TAG, "args.getPhrase()=" + args.getPhrase());
					if ((!"encryptcall".equals(args.getPhrase()))) // xunz++
						mEngine.getSoundService().startRingTone();

					ScreenAV.ispeoplePTT = true;

				} else {
					Log.e(TAG, String.format(
							"Failed to find session with id = ",
							args.getSessionId()));
				}

			} else if (NgnMediaType.isFileTransfer(mediaType)) {
				mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
				if (mWakeLock != null && !mWakeLock.isHeld()) {
					mWakeLock.acquire(10);
				}
			} else if (NgnMediaType.isChat(mediaType)) {
				mEngine.refreshChatNotif(R.drawable.chat_25);
				if (mWakeLock != null && !mWakeLock.isHeld()) {
					mWakeLock.acquire(10);
				}
			}

			break;

		case INPROGRESS:

			if (GlobalVar.bADHocMode) {
				mEngine.getSoundService().startRingBackTone();
			}
			if (NgnMediaType.isAudioVideoType(mediaType)) {

				mEngine.showAVCallNotif(R.drawable.phone_call_25,
						getString(R.string.string_call_outgoing)); // 正在呼叫
				if (ScreenMap.bMapCall == false) { // 非地图界面
					mEngine.showAVCallNotif(R.drawable.phone_call_25,
							getString(R.string.string_call_outgoing)); // 正在呼叫
				} else { // 地图界面
					mEngine.showMapNotif(R.drawable.phone_call_25,
							getString(R.string.string_call_outgoing)); // 正在呼叫
				}
			} else if (NgnMediaType.isFileTransfer(mediaType)) {
				mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
			} else if (NgnMediaType.isChat(mediaType)) {
				mEngine.refreshChatNotif(R.drawable.chat_25);
			}

			break;

		case RINGING:
			if (NgnMediaType.isAudioVideoType(mediaType)) {
				Log.d(TAG, "180SDP:" + NgnInviteEventArgs.m180HasSdp);
				if (!NgnInviteEventArgs.m180HasSdp) {
					mEngine.getSoundService().startRingBackTone();
				} else {
					NgnInviteEventArgs.m180HasSdp = false;
				}
			} else if (NgnMediaType.isFileTransfer(mediaType)) {
				mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
			} else if (NgnMediaType.isChat(mediaType)) {
				mEngine.refreshChatNotif(R.drawable.chat_25);
			}
			break;

		case CONNECTED:
			if (NgnMediaType.isAudioVideoType(mediaType)) { // 正在通话中
				if (ScreenMap.bMapCall == false) { // 非地图界面
					mEngine.showAVCallNotif(R.drawable.phone_call_25,
							getString(R.string.string_incall)); // 正在通话中
				} else { // 地图界面
					mEngine.showMapNotif(R.drawable.phone_call_25,
							getString(R.string.string_incall)); // 正在通话中
				}
				mEngine.getSoundService().stopRingBackTone();
				mEngine.getSoundService().stopRingTone();
				hasShowTermTips = false;

				final NgnAVSession avSession = NgnAVSession.getSession(args
						.getSessionId());
				if (avSession != null && !avSession.isOutgoing()) {
					SystemVarTools.avCallNotNumber--;
				}

			}
			break;
		case EARLY_MEDIA:
			if (NgnMediaType.isAudioVideoType(mediaType)) {
				if (ScreenMap.bMapCall == false) { // 非地图界面
					mEngine.showAVCallNotif(R.drawable.phone_call_25,
							getString(R.string.string_call_outgoing)); // 正在呼叫
				} else { // 地图界面
					mEngine.showMapNotif(R.drawable.phone_call_25,
							getString(R.string.string_call_outgoing)); // 正在呼叫
				}
				mEngine.getSoundService().stopRingBackTone();
				mEngine.getSoundService().stopRingTone();
			} else if (NgnMediaType.isFileTransfer(mediaType)) {
				mEngine.refreshContentShareNotif(R.drawable.image_gallery_25);
			} else if (NgnMediaType.isChat(mediaType)) {
				mEngine.refreshChatNotif(R.drawable.chat_25);
			}

			break;
		case ENCRYPT_INFO:

			Log.d(TAG, "receive encrypted info content");
			// return null;
			break;

		case REMOTE_REFUSE: // sks add
			break;

		case SIP_RESPONSE: // gzc add
			// short sipCode = intent.getShortExtra(
			// NgnInviteEventArgs.EXTRA_SIPCODE, (short) 0);

			// if (NgnMediaType.isAudioType(mediaType)) { //
			// 0: 487 poweroff 1:486 busy 2:404 notfound 3:480 Temporarily Unavailable 4:
			// invalid

			short sipCode = intent.getShortExtra(
					NgnInviteEventArgs.EXTRA_SIPCODE, (short) 0);
			Log.d(TAG, "NativeService sipCode:" + sipCode);

			if (sipCode == NgnSipCode.sipCode_poweroff) {
				MyLog.d(TAG, "sipCode NgnSipCode.sipCode_poweroff");
			} else if (sipCode == NgnSipCode.sipCode_busy) {
				MyLog.d(TAG, "sipCode NgnSipCode.sipCode_busy");
			} else if (sipCode == NgnSipCode.sipCode_notfound) {
				MyLog.d(TAG, "sipCode NgnSipCode.sipCode_notfound");
			} else if (sipCode == NgnSipCode.sipCode_timeout) {
				MyLog.d(TAG, "sipCode NgnSipCode.sipCode_timeout");
			}
			break;

		case REMOTE_MEDIA_NOT_EXIST:
			final NgnAVSession avSession = NgnAVSession.getSession(args
					.getSessionId());
			if (avSession != null) {
				avSession.hangUpCall();
				Log.e(TAG, "recv: HungupCall---hangUpCall");
			}

			break;
		case CURRENT_NETWORK_UNGOOD:
			SystemVarTools.showToast("当前网络状况不佳！");
			break;
		default:
			break;
		}
		return true;
	}

	protected void Msrp_OnReceive(Context context, Intent intent) {
		NgnMsrpEventArgs args = intent
				.getParcelableExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED);
		if (args == null) {
			Log.e(TAG, "Invalid event args");
			return;
		}
		switch (args.getEventType()) {
		case DATA:
			final NgnMsrpSession session = NgnMsrpSession.getSession(args
					.getSessionId());
			if (session == null) {
				Log.e(TAG,
						"Failed to find MSRP session with id="
								+ args.getSessionId());
				return;
			}
			final byte[] content = intent
					.getByteArrayExtra(NgnMsrpEventArgs.EXTRA_DATA);
			NgnHistorySMSEvent event = new NgnHistorySMSEvent(
					NgnUriUtils.getUserName(session.getRemotePartyUri()),
					StatusType.Incoming, "");
			event.setContent(content == null ? NgnStringUtils.nullValue()
					: new String(content));
			mEngine.getHistoryService().addEvent(event);
			mEngine.showSMSNotif(R.drawable.sms_25, "New message");
			break;
		default:
			break;
		}
	}

	protected boolean Message_OnReceive(Context context, Intent intent) {
		try {

			NgnMessagingEventArgs args = intent
					.getParcelableExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.d(TAG, "Invalid event args");
				return false;
			}
			NgnMessagingEventTypes eventType = args.getEventType();

			Log.d(TAG, "Message_OnReceive()收到东西");

			switch (eventType) {
			case INCOMING:
				String contentType = args.getContentType();

				// GIS开关命令处理
				if (NgnContentType.GPS_CONTENT_TYPE_HEADER_COMMAND
						.equals(contentType)) { //
					String appName = ChkVer.getAppName(getApplicationContext());
					ServiceGPSReport
							.getInstance()
							.SetRemoteRartyUri(
									intent.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY));
					ServiceGPSReport.getInstance().GPSReport(
							new String(args.getPayload()), appName);
					break;
				}

				// 收到GIS数据处理
				if (NgnContentType.GPS_CONTENT_TYPE_HEADER_DATA
						.equals(contentType)) {
					ServiceGPSReport.getInstance().receiveGPSInfo(
							new String(args.getPayload()));
					break;
				}

				// gzc 20140813 获取服务器时间与本地时间差
				String dateString = intent
						.getStringExtra(NgnMessagingEventArgs.EXTRA_DATE);
				long timeSlot = 0;// 时间差
				if (dateString == null) {
					Log.d(TAG, "sipDate is NULL");
				} else {
					try {
						String now = NgnDateTimeUtils.now();
						Log.d(TAG, "now:" + now);
						Log.d(TAG, "dateString:" + dateString);
						timeSlot = NgnDateTimeUtils.parseDate(now).getTime()
								- new Date(dateString).getTime();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}

				CPIMMessage cpimMessage = null;
				// 处理推送信息
				if (NgnContentType.TEXT_PUSHINFO_GROUP.equals(contentType)
						|| NgnContentType.TEXT_PUSHINFO_PERSON
								.equals(contentType)) { // 推送消息

					handlerPushMessage(intent);
					break;
				}

				// 获取发送方号码
				String remoteParty = intent
						.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY);
				// 空值判断
				if (NgnStringUtils.isNullOrEmpty(remoteParty)) {
					remoteParty = NgnStringUtils.nullValue();
				}
				String remoteArray[] = remoteParty.split(",");
				String gmmember = "";
				if (remoteArray.length > 1) {
					remoteParty = NgnUriUtils.makeValidSipUri(remoteArray[0]);
					gmmember = NgnUriUtils.getUserName(remoteArray[1]);
				} else {
					remoteParty = NgnUriUtils.makeValidSipUri(remoteArray[0]);
				}

				// 19800005003
				remoteParty = NgnUriUtils.getUserName(remoteParty);

				NgnHistorySMSEvent event = new NgnHistorySMSEvent(remoteParty,
						StatusType.Incoming, gmmember);
				event.setmLocalParty(SystemVarTools.getmIdentity());

				/**
				 * modified by zhaohua on 20140325
				 */
				// //取得接收到的消息体，并进行解析取得消息内容
				// String content = new String(args.getPayload());
				// if (!isTrMsg) {
				cpimMessage = CPIMParser.parse(new String(args.getPayload()));
				// }
				if (cpimMessage == null) {
					MyLog.e(TAG, "cpimMessage parse failed.");
					return false;
				}
				String content = (String) cpimMessage.getContent();

				MyLog.d(TAG, "content--->"+content);
				if (content != null && content.equals("NULL"))
					content = null;
				event.setContent(content);
				event.setLocalMsgID(cpimMessage.getLocalMsgID());

				/*
				 * gzc 20140807
				 * 如果即时消息cpim中带有时间戳（消息发送时间），则以cpim时间戳排序，否则已收到消息的时间排序
				 */
				try {
					if (cpimMessage != null) {
						if (cpimMessage.getSubmitTime() == null) {
							event.setStartTime(NgnDateTimeUtils.parseDate(
									NgnDateTimeUtils.now()).getTime());
						} else {
							// gzc 20140813 解析CST时间 Thu Aug 14 02:22:18 CST 2014
							String patten;
							DateFormat format;
							long eventStartTime, eventEndTime;
							if (GlobalVar.bADHocMode) {
								patten = "EEE MMM dd HH:mm:ss.SSS zzz yyyy";
								format = new SimpleDateFormat(patten,
										Locale.ENGLISH);

								eventStartTime = format.parse(
										cpimMessage.getSubmitTime()).getTime();
								eventEndTime = format.parse(
										cpimMessage.getSubmitTime()).getTime();
								Log.d(TAG,
										"Recv: "
												+ format.parse(cpimMessage
														.getSubmitTime()));
								event.setStartTime(eventStartTime);// 即时消息排序以服务器时间为准
								event.setEndTime(eventEndTime);// 即时消息显示以本地时间为准
							} else {
								// patten = "EEE MMM dd HH:mm:ss.SSS zzz yyyy";
								patten = "yyyy-MM-dd HH:mm:ss.SSS Z";
								format = new SimpleDateFormat(patten,
										Locale.ENGLISH);
								// eventStartTime = format.parse(
								// cpimMessage.getSubmitTime()).getTime() + 14 *
								// 3600 * 1000;
								// eventEndTime = format.parse(
								// cpimMessage.getSubmitTime()).getTime()
								// + timeSlot - 14 * 3600 * 1000;
								eventStartTime = format.parse(
										cpimMessage.getSubmitTime()).getTime();
								eventEndTime = format.parse(
										cpimMessage.getSubmitTime()).getTime()
										+ timeSlot;
								event.setStartTime(eventStartTime);// 即时消息排序以服务器时间为准
								event.setEndTime(eventEndTime);// 即时消息显示以本地时间为准
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					event.setStartTime(NgnDateTimeUtils.parseDate(
							NgnDateTimeUtils.now()).getTime());
				}
				String msgID = gmmember + ";" + cpimMessage.getLocalMsgID();// 用发送方号码+localMsgID来标识是否是重发的
				Log.d(TAG,
						String.format("Get a message with msgID = %s", msgID));

				// 文字短信的递送报告MsgType为REPORT，文件传输传输的递送报告为IM
				Log.d(TAG, cpimMessage.getMsgType());

				if (cpimMessage.getLocalMsgID() != null
						&& cpimMessage.getLocalMsgID().trim().equals("")) {
					String tmpId = "" + new Date().getTime();
					cpimMessage.setLocalMsgID(tmpId);
					event.setLocalMsgID(tmpId);
					msgID = tmpId;
					Log.d(TAG, "LocalMsgID is null . Set to current time.");
				}

				// 非递送报告
				// 普通短消息递送报告带REPORT，文件传输的递送报告带文件传输状态包含send
				if (cpimMessage.getMsgType() != null
						&& !cpimMessage.getMsgType().equals("REPORT")
						&& cpimMessage.getContent() != null
						&& !cpimMessage.getContent().toString()
								.contains("send")) {
					// add by gle 20140605 解决消息重收问题

					if (ServiceLoginAccount.mMessageIDHashMap != null
							&& msgID != null
							&& msgID.length() > 0
							&& !msgID.isEmpty()
							&& ServiceLoginAccount.mMessageIDHashMap
									.containsKey(msgID)) {
						Log.d(TAG,
								String.format(
										"You already have received this message(msgID: %s)",
										msgID));
						return false;
					} else {
						mEngine.getHistoryService().addEvent(event);

					}
				} else { // 保存递送报告

					Log.e("收到递送报告", "收到递送报告");
					if (cpimMessage != null) {
						try {

							if (cpimMessage.getReportID() != null
									&& cpimMessage.getReportType() != null) {

								ServiceLoginAccount.mMessageIDHashMap.put(
										cpimMessage.getReportID().trim(),
										System.currentTimeMillis() / 1000);
								try {
									Tools_data
											.writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
								} catch (IOException e) {
									e.printStackTrace();
								}

								// 这样写聊天界面可以自动刷新
								mEngine.getHistoryService().addEvent(event);
								mEngine.getHistoryService().deleteEvent(event);

							} else {

								if (cpimMessage.getContent().toString()
										.contains("send/")) {

									ServiceLoginAccount.mMessageIDHashMap.put(
											cpimMessage.getLocalMsgID().trim(),
											System.currentTimeMillis() / 1000);
									try {
										Tools_data
												.writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
									} catch (IOException e) {
										e.printStackTrace();
									}

									mEngine.getHistoryService().addEvent(event);
									mEngine.getHistoryService().deleteEvent(
											event);
								}

							}

						} catch (Exception e) {
							e.printStackTrace();
						}

						// 递送报告不当做新信息提示
						mConfigurationService.putBoolean(
								event.getRemoteParty(), false);
						mConfigurationService.commit();

					}
					break;
				}
				// if (remoteParty.equals("1000")) {
				if (cpimMessage.getMsgType() != null
						&& cpimMessage.getMsgType().equals("SYSTEM")
						&& !GlobalSession.bSocketService) { // 系统广播消息
					Toast welcomeToast = Toast.makeText(
							NgnApplication.getContext(), event.getContent(),
							Toast.LENGTH_LONG);
					welcomeToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					welcomeToast.show();
				} else if (cpimMessage.getMsgType() != null
						&& !cpimMessage.getMsgType().equals("REPORT")// IM/REPORT
						&& ((ServiceLoginAccount.mMessageIDHashMap == null) || (ServiceLoginAccount.mMessageIDHashMap != null && !ServiceLoginAccount.mMessageIDHashMap
								.containsKey(msgID)))) { // 重发消息提示音多遍问题
					if (content != null && !content.isEmpty() && msgID != null
							&& msgID.length() > 0 && !msgID.isEmpty()) {
						ServiceLoginAccount.mMessageIDHashMap.put(msgID,
								System.currentTimeMillis() / 1000);
						try {
							Tools_data
									.writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// mEngine.showSMSNotif(R.drawable.sms_25, "新短消息"); // IM //
					final IServiceScreen mScreenService = mEngine
							.getScreenService();

					// 大终端版本收到文件传输消息时不响铃和振动
					if (SKDroid.sks_version == VERSION.SOCKET
							&& content != null
							&& content.startsWith("type:file")) {
						return false;

					}

					if (isNotify) {
						isNotify = false;

						// 若当前处于和发件人的聊天界面中，则振动提醒，否则通知栏提醒
						if ((mScreenService.getCurrentScreen().getType()
								.toString().equals("CHAT_T"))
								&& (ScreenChat.getRemotePartyString()
										.equals(event.getRemoteParty()))

						) {
							// ScreenChat.ListViewToButtom();

							Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
							vibrator.vibrate(100);

						} else {

							ModelContact mc = SystemVarTools
									.createContactFromPhoneNumber(event
											.getDisplayName());

							if (cpimMessage.getMsgType() != null
									&& !cpimMessage.getMsgType().equals(
											"REPORT")
									&& !cpimMessage.getContent().toString()
											.contains("send")) { //

								if (!mc.isgroup) {

									if (content != null
									// &&
									// content.startsWith("type:filetransfer"))
									// {
											&& content.startsWith("type:file")) {
										ModelFileTransport fileModel = new ModelFileTransport();
										fileModel.parseFileContent(content);

										String string = null;
										if (fileModel.name
												.endsWith("tempaudio.amr")) {
											string = mc.name
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_audio);

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);
										} else if (fileModel.name
												.endsWith("VideoRecorder.mp4")) {

											string = mc.name
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_video);

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);

										} else if (fileModel.name
												.endsWith(".jpg")
												|| fileModel.name
														.endsWith(".jpeg")
												|| fileModel.name
														.endsWith(".png")
												|| fileModel.name
														.endsWith(".bmp")
												|| fileModel.name
														.endsWith(".gif")
												|| fileModel.name
														.endsWith(".JPG")
												|| fileModel.name
														.endsWith(".JPEG")
												|| fileModel.name
														.endsWith(".PNG")
												|| fileModel.name
														.endsWith(".BMP")
												|| fileModel.name
														.endsWith(".GIF")) {
											string = mc.name
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_pic);

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);

										} else {
											string = mc.name
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_file)
													+ fileModel.name;

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);

										}

									} else {

										String string = mc.name
												+ ": "
												+ cpimMessage.getContent()
														.toString();

										mEngine.showSMSNotif(R.drawable.sms_25,
												string);

									}

								} else { // 群组短信 显示每个发件人名称
									String remoteNumber = mc.mobileNo; // 初值为群组号码
									String remoteName = ""; // 用于显示群短信用户名

									if (event.getGMMember() != null
											&& !event.getGMMember().isEmpty()) {

										remoteNumber = event.getGMMember();
										Log.e("remoteNumber", remoteNumber + "");
										remoteName = remoteNumber;
										ModelContact temp = SystemVarTools
												.createContactFromPhoneNumber(remoteNumber);

										if (temp != null && temp.name != null) {
											remoteName = temp.name;
										}

									} else {
										remoteName = "";
										Log.e(TAG,
												"hisSMSEvent.getGMMember() is null");
									}

									if (content != null
									// &&
									// content.startsWith("type:filetransfer"))
									// {
											&& content.startsWith("type:file")) {
										ModelFileTransport fileModel = new ModelFileTransport();
										fileModel.parseFileContent(content);

										String string = null;
										if (fileModel.name
												.endsWith("tempaudio.amr")) {
											string = remoteName
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_audio);

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);
										} else if (fileModel.name
												.endsWith("VideoRecorder.mp4")) {

											string = remoteName
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_video);

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);

										} else if (fileModel.name
												.endsWith(".jpg")
												|| fileModel.name
														.endsWith(".jpeg")
												|| fileModel.name
														.endsWith(".png")
												|| fileModel.name
														.endsWith(".bmp")
												|| fileModel.name
														.endsWith(".gif")
												|| fileModel.name
														.endsWith(".JPG")
												|| fileModel.name
														.endsWith(".JPEG")
												|| fileModel.name
														.endsWith(".PNG")
												|| fileModel.name
														.endsWith(".BMP")
												|| fileModel.name
														.endsWith(".GIF")) {
											string = remoteName
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_pic);

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);

										} else {
											string = remoteName
													+ ": "
													+ getApplicationContext()
															.getString(
																	R.string.notif_file)
													+ fileModel.name;

											mEngine.showSMSNotif(
													R.drawable.sms_25, string);

										}

									} else {

										String string = remoteName
												+ ": "
												+ cpimMessage.getContent()
														.toString();

										mEngine.showSMSNotif(R.drawable.sms_25,
												string);

									}

								}
							}
						}

					} else {

						// isNotify = true;
					}

					notifyTimer.schedule(new TimerTask() {

						@Override
						public void run() {

							if (isNotify) {

								isNotify = false;

							} else {

								isNotify = true;
							}

						}
					}, 2000);

					mConfigurationService.putBoolean(event.getRemoteParty(),
							false);

					mConfigurationService.commit();
				}
				Log.e("NativeService-Message_OnReceive()-ACTION_MESSAGING_EVENT",
						"收到消息，显示消息通知！");
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private void handlerPushMessage(Intent intent) {

		NgnMessagingEventArgs args = intent
				.getParcelableExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED);

		String dateString = intent
				.getStringExtra(NgnMessagingEventArgs.EXTRA_DATE);

		NgnHistorySMSEvent eventSMS = new NgnHistorySMSEvent("Subscribe", // 消息主界面弹出订阅号消息
				StatusType.Incoming, "Subscribe"); // 19800005001

		eventSMS.setStartTime(NgnDateTimeUtils
				.parseDate(NgnDateTimeUtils.now()).getTime());
		String tmpId = "" + new Date().getTime();
		eventSMS.setLocalMsgID(tmpId);

		eventSMS.setmLocalParty(SystemVarTools.getmIdentity());

		mEngine.getHistoryService().addEvent(eventSMS);

		String pushInfo = new String(args.getPayload());

		if (pushInfo.startsWith("{\"type\":\"txt\"")) {

			int indexStart = pushInfo.indexOf("content");
			String content = pushInfo.substring(indexStart + 10,
					pushInfo.length() - 4);

			String remoteParty = intent
					.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY); // sip:19800005003@192.168.1.192:5061
																				// //sip:19811205001@sunkaisens.com
			if (NgnStringUtils.isNullOrEmpty(remoteParty)) {
				remoteParty = NgnStringUtils.nullValue();
			}
			remoteParty = NgnUriUtils.getUserName(remoteParty); // 19811205001

			ModelPush modelPush = new ModelPush();

			modelPush.serviceType = "pushinfo".trim();
			modelPush.msgType = "text".trim();
			modelPush.title = getString(R.string.shorttextmessage).trim();
			modelPush.content = content;

			NgnHistoryPushEvent event = new NgnHistoryPushEvent(remoteParty,
					StatusType.Incoming);

			event.setContent(modelPush.content);
			event.setStartTime(NgnDateTimeUtils.parseDate(dateString).getTime());

			event.setTitle(modelPush.title);

			event.setServiceType(modelPush.serviceType);
			event.setMsgType(modelPush.msgType);

			event.setmLocalParty(SystemVarTools.getmIdentity());

			mEngine.getHistoryService().addEvent(event);

			mEngine.showPushNotif(R.drawable.sms_25, getApplicationContext()
					.getString(R.string.notif_new_push_msm));

		} else {

			List pushList = XmlDoc.getPushList(pushInfo);
			if (pushList != null) {
				String remoteParty = intent
						.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY); // sip:19800005003@192.168.1.192:5061
																					// //sip:19811205001@sunkaisens.com
				if (NgnStringUtils.isNullOrEmpty(remoteParty)) {
					remoteParty = NgnStringUtils.nullValue();
				}
				remoteParty = NgnUriUtils.getUserName(remoteParty); // 19811205001
				savePushEvent(pushList, remoteParty, dateString);
				mEngine.showPushNotif(
						R.drawable.sms_25,
						getApplicationContext().getString(
								R.string.notif_new_push_msm));
			}
			Log.d(TAG, "" + pushInfo);
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		try {

			if (mBroadcastReceiver != null) {
				unregisterReceiver(mBroadcastReceiver);
				mBroadcastReceiver = null;
			}
			if (mWakeLock != null) {
				if (mWakeLock.isHeld()) {
					mWakeLock.release();
					mWakeLock = null;
				}
			}
			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private void Register_OnReceive(Context context, Intent intent) {
		NgnRegistrationEventArgs args = intent
				.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
		final NgnRegistrationEventTypes type;

		Notification notification = null;

		Log.e(TAG, "registration event type ==== " + args.getEventType());
		switch ((type = args.getEventType())) {
		case REGISTRATION_INPROGRESS:
			MyLog.d(TAG, "REGISTRATION_INPROGRESS");
			mEngine.showAppNotif(R.drawable.icon, getString(R.string.loading));

			ServiceRegiste.sendRegStatus(MessageTypes.MSG_REG_INPROGRESS);
			mEngine.cancelUpdateNotify();

			// R.drawable.bullet_ball_glass_green_16,
			// getString(R.string.logging));
			break;
		case REGISTRATION_OK:
			Log.e(TAG, "REGISTRATION_OK");

			SystemVarTools.bLogin = true;
			GlobalVar.mLogout = false;
			ServiceRegiste.neededreset = false;

			mEngine.getScreenService().clearScreenList();
			//add by Gongle to get IP from DNS(appserver.test.com)
			SystemVarTools.changeDNSToIPAddr();

			// 加载历史记录
			mEngine.getHistoryService().load(SystemVarTools.getmIdentity());
			if (ServiceRegiste.isNeedRedownloadContacts) {
				ServiceRegiste
						.sendContactStatus(ServiceRegiste.NET_DOWNLOAD_CONTACTS);
				ServiceRegiste.isNeedRedownloadContacts = false;
			}

			SKDroid.acquirePowerLock();

			GlobalVar.mLocalNum = SystemVarTools.getmIdentity();
			Log.d(TAG, "GlobalVar.mLocalNum 1:" + GlobalVar.mLocalNum);

			Engine.getInstance().getNetworkService().setNetworkEnable(true);

			if (!GlobalSession.bSocketService) {
				ServiceRegiste.sendRegStatus(MessageTypes.MSG_REG_OK);
				mEngine.showAppNotif(R.drawable.icon, getString(R.string.login));

				mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
			}

			SystemVarTools.isNetChecking = false;
			// 发送离线消息请求
			ServiceMessage.getOfflineMsg();

			// Intent ai = new Intent(SKDroid.getContext(),NativeService.class);
			//
			// PendingIntent pi = PendingIntent.getService(SKDroid.getContext(),
			// 1001, ai, 0);
			//
			// AlarmManager am = (AlarmManager)
			// getSystemService(Context.ALARM_SERVICE);
			// am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
			// 30000, pi);
			//
			break;
		case REGISTRATION_NOK:
			Log.d(TAG, "REGISTRATION_NOK");
			if (SKDroid.sks_version != VERSION.SOCKET || !SKDroid.isl8848a_l1860())
			{
				ServiceContact.auidForPresence = "";
				sipService.unRegister();
				//ServiceRegiste.sendStackStatus(MessageTypes.MSG_STACK_NEED_STOP);
				if (SystemVarTools.bLogin) {
					ServiceRegiste.neededreset = true;
					MyLog.d(TAG, "neededreset = true");
				}
				
				ServiceRegiste.isNeedRedownloadContacts = true;
				ServiceRegiste.sendRegStatus(MessageTypes.MSG_REG_NOK);
				Intent tIntent = new Intent(MessageTypes.MSG_NET_EVENT);
				SKDroid.getContext().sendBroadcast(tIntent);
			}
			mEngine.showAppNotif(R.drawable.icon, getString(R.string.logfail));
			break;
		case UNREGISTRATION_INPROGRESS:
			Log.d(TAG, "UNREGISTRATION_INPROGRESS");
			break;
		case UNREGISTRATION_OK:
			Log.d(TAG, "UNREGISTRATION_OK");

			SystemVarTools.bLogin = false;
			ServiceRegiste.sendStackStatus(MessageTypes.MSG_STACK_NEED_STOP);

			if (GlobalVar.mLogout) {
				mEngine.showAppNotif(R.drawable.icon,
						getString(R.string.logout));
			}

			ServiceRegiste.isNeedRedownloadContacts = true;

			SKDroid.releasePowerLock();

			// ai = new Intent(SKDroid.getContext(),NativeService.class);
			//
			// //FLAG_NO_CREATE 如果pi不存在则返回null
			// pi = PendingIntent.getService(SKDroid.getContext(), 1001, ai,
			// PendingIntent.FLAG_NO_CREATE);
			//
			// if(pi != null){
			// am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			// am.cancel(pi);
			// pi.cancel();
			// }

			break;
		case UNREGISTRATION_NOK:
			Log.d(TAG, "UNREGISTRATION_NOK");
			break;
		default:
			break;
		}
	}

	private void Publish_OnReceive(Context context, Intent intent) {
		NgnPublicationEventArgs args = intent
				.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
		if (args == null) {
			Log.e(TAG, "Invalid event args");
			return;
		}
		switch (args.getEventType()) {
		// 在程序状态通知栏显示presence图标信息：
		case PUBLICATION_OK:
			if (mEngine.getSipService().isRegisteSessionConnected()) {
				mEngine.showAppNotif(R.drawable.user_online_24,
						getApplicationContext()
								.getString(R.string.notif_online));
			}
			break;
		case PUBLICATION_NOK:
		case PUBLICATION_INPROGRESS:
		case UNPUBLICATION_OK:
			if (mEngine.getSipService().isRegisteSessionConnected()) {
				// mEngine.showAppNotif(R.drawable.user_offline_24,
				// "状态：离线");
				mEngine.showAppNotif(R.drawable.user_online_24,
						getApplicationContext()
								.getString(R.string.notif_online));
			}
			break;
		case UNPUBLICATION_NOK:
		case UNPUBLICATION_INPROGRESS:
		default:
			break;

		}
	}

	private void SubScribe_OnReceive(Context context, Intent intent) {
		MyLog.d(TAG, "SubScribe_OnReceive()");
		NgnSubscriptionEventArgs args = intent
				.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
		if (args == null) {
			return;
		}
		switch (args.getEventType()) {
		// SUBSCRIPTION_OK, SUBSCRIPTION_NOK, SUBSCRIPTION_INPROGRESS,
		// UNSUBSCRIPTION_OK, UNSUBSCRIPTION_NOK, UNSUBSCRIPTION_INPROGRESS,
		// INCOMING_NOTIFY
		case SUBSCRIPTION_INPROGRESS:
			break;
		case SUBSCRIPTION_OK:
			break;
		case INCOMING_NOTIFY:
			EventPackageType type = args.getEventPackage();
			if (args.getContent() == null)
				break;
			String notifyContent = new String(args.getContent());
			String contentType = args.getContentType();
			// Log.d("INCOMING_NOTIFY",String.format("GLE---type=%s,content=%s",notifyType,notifyContent));
			MyLog.e(TAG, "INCOMING_NOTIFY: " + notifyContent);
			ParserSubscribeState.getInstance().parserGroupMemberState2(
					contentType, notifyContent);

			MyLog.d(TAG, "screen.refresh: Notify");

			ServiceContact.sendContactFrashMsg();
			break;
		case SUBSCRIPTION_NOK:
			short sipCode = args.getSipCode();
			String toHeaderString = intent
					.getStringExtra(NgnSubscriptionEventArgs.EXTRA_TO_HEADER);
			MyLog.d(TAG, "sipCode = " + sipCode + ", to: " + toHeaderString);
			if (sipCode == 481 && ServiceContact.auidForPresence != ""
					&& sipService.getSipStack().isValid()) {
				if (toHeaderString.contains("sip:rls@test.com")) {
					ServiceContact.subAll(ServiceContact.auidForPresence);
					MyLog.d(TAG, "subAll(rls)");
				} else if (toHeaderString.contains("sip:public-group@test.com")) {
					SystemVarTools.subscribePublicGroup();
					MyLog.d(TAG, "subscribePublicGroup()");
				} else if (toHeaderString
						.contains("sip:service-group@test.com")) {
					SystemVarTools.subscribeServiceGroup();
					MyLog.d(TAG, "subscribeServiceGroup()");
				} else if (toHeaderString.contains("sip:global-group@test.com")) {
					SystemVarTools.subscribeGlobalGroup();
					MyLog.d(TAG, "subscribeGlobalGroup()");
				} else if (toHeaderString
						.contains("sip:subscribe-group@test.com")) {
					SystemVarTools.subscribeSubscribeGroup();
					MyLog.d(TAG, "subscribeSubscribeGroup()");
				} else if (toHeaderString.contains("sip:ims-pim@test.com")) {
					SystemVarTools.subscribePersionInfo();
					MyLog.d(TAG, "subscribePersionInfo()");
				}
			}
			if (sipCode == 480 || sipCode == 408 || sipCode == 500) {
				new NgnTimer().schedule(new TimerTask() {
					@Override
					public void run() {
						ServiceContact.subAll(ServiceContact.auidForPresence);
					}
				}, 30000);
			}

			break;
		case UNSUBSCRIPTION_OK:
		case UNSUBSCRIPTION_NOK:
		case UNSUBSCRIPTION_INPROGRESS:
		default:
			break;
		}
	}

	class MyPhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			Log.i(TAG, "电话 onCallStateChanged");
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				Log.d(TAG, "cdma 空闲状态");
				if (sipService.isRegisteSessionConnected()) {

					// 通话结束后返回主界面
					((Engine) Engine.getInstance()).getScreenService()
							.bringToFront(Main.ACTION_SHOW_HOME, null);
				} else {

				}
				// SystemVarTools.showToast("电话处于空闲状态!!");
				break;

			case TelephonyManager.CALL_STATE_RINGING:
				Log.d(TAG, "cdma 振铃状态 电话号码=" + incomingNumber);
				// SystemVarTools.showToast("电话处于振铃状态!!");
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.d(TAG, "cdma 通话中状态");
				// SystemVarTools.showToast("电话处于通话中状态!!");
				break;

			default:
				break;
			}
		}

	}

	/**
	 * 保存推送消息
	 * 
	 * @param pushs
	 * @param remoteParty
	 * @param dateString
	 */
	private void savePushEvent(List pushs, String remoteParty, String dateString) {
		List pushList = pushs;

		int count = pushList.size();
		if (count == 1) { // 单图文
			ModelPush modelPush = new ModelPush();
			modelPush = (ModelPush) pushList.get(0);

			NgnHistoryPushEvent event = new NgnHistoryPushEvent(remoteParty,
					StatusType.Incoming);

			event.setContent(modelPush.content);
			event.setStartTime(NgnDateTimeUtils.parseDate(dateString).getTime());
			event.setId(modelPush.id);
			event.setTitle(modelPush.title);
			event.setDigest(modelPush.digest);
			event.setImageUrl(modelPush.imageUrl);
			event.setLinkUri(modelPush.linkUri);
			event.setTurn(modelPush.turn);
			event.setServiceType(modelPush.serviceType);
			event.setMsgType(modelPush.msgType);

			event.setmLocalParty(SystemVarTools.getmIdentity());

			mEngine.getHistoryService().addEvent(event);
		} else if (count > 1) { // 多图文

			// for (int i = 0; i < count; i++) {
			ModelPush modelPush = new ModelPush();
			modelPush = (ModelPush) pushList.get(0);

			NgnHistoryPushEvent event = new NgnHistoryPushEvent(remoteParty,
					StatusType.Incoming);

			event.setContent(modelPush.content);
			event.setStartTime(NgnDateTimeUtils.parseDate(dateString).getTime());
			event.setId(modelPush.id);
			event.setTitle(modelPush.title);
			event.setDigest(modelPush.digest);
			event.setImageUrl(modelPush.imageUrl);
			event.setLinkUri(modelPush.linkUri);
			event.setTurn(modelPush.turn);
			event.setServiceType(modelPush.serviceType);
			event.setMsgType(modelPush.msgType);

			event.setmLocalParty(SystemVarTools.getmIdentity());

			List pushListLast = pushList.subList(1, count);
			event.setPushList(pushListLast);

			mEngine.getHistoryService().addEvent(event);
			// }
		}

	}

	public boolean isProcessRunning(Context context, String proessName) {

		Log.d(TAG, "check isProcessRunning()...");
		boolean isRunning = false;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
		for (RunningAppProcessInfo info : lists) {
			if (info.processName.equals(proessName)) {
				Log.d(TAG, "Running: " + info.processName);
				isRunning = true;
			}
		}

		return isRunning;
	}

	private Handler mSocketHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			MyLog.d(TAG, "Receive SocketHandlerMessage");
			if (msg.obj == null) {
				MyLog.e(TAG, "SocketMsg.obj is null.");
				return;
			}
			Intent i = (Intent) msg.obj;
			ServerMsgReceiver.getMsgReceiver().onSocketReceive(
					SKDroid.getContext(), i);
			MyLog.d(TAG, "send socket event");
		};

	};

	@Override
	public void onSocketEvent(Intent intent) {
		// TODO Auto-generated method stub
		MyLog.d(TAG, "onSocketEvent()");

		final String action = intent.getAction();
		MyLog.i("ServerMsgReceiver: action=", action);
		if (ServerMsgReceiver.MESSAGE_SOCKET_INTENT.equals(action)) {
			int type = intent.getIntExtra("type", 0);
			if (type == BaseSocketMessage.MSG_C_ALIVE_REQ) {// gzc 适配心跳请求
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_ALIVE_REQ, "message = "
						+ "接收适配心跳请求！");
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_ALIVE_RES)); // 自组网上报联系人列表信息
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_S_ALIVE_RES, "message = "
						+ "发送心跳响应！");
				return;
			}
		}
		Message msg = mSocketHandler.obtainMessage();
		msg.obj = intent;
		msg.sendToTarget();
		MyLog.d(TAG, "msg sended.");

	}
}
