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
package com.sunkaisens.skdroid;

import java.util.HashMap;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;
import org.doubango.utils.MyLog;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.sks.net.socket.server.ServerMsgReceiver;
import com.sunkaisens.skdroid.Screens.BaseScreen;
import com.sunkaisens.skdroid.Screens.BaseScreen.SCREEN_TYPE;
import com.sunkaisens.skdroid.Screens.IBaseScreen;
import com.sunkaisens.skdroid.Screens.ScreenAV;
import com.sunkaisens.skdroid.Screens.ScreenChatQueue;
import com.sunkaisens.skdroid.Screens.ScreenFileTransferQueue;
import com.sunkaisens.skdroid.Screens.ScreenLoginAccount;
import com.sunkaisens.skdroid.Screens.ScreenMap;
import com.sunkaisens.skdroid.Screens.ScreenMediaAV;
import com.sunkaisens.skdroid.Screens.ScreenSettings;
import com.sunkaisens.skdroid.Screens.ScreenSplash;
import com.sunkaisens.skdroid.Screens.ScreenTabHome;
import com.sunkaisens.skdroid.Services.IServiceScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.app.service.DaemonService;
import com.sunkaisens.skdroid.app.service.LogServiceProcess;
import com.sunkaisens.skdroid.app.service.NativeService;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

//import com.tencent.bugly.crashreport.CrashReport;

public class Main extends ActivityGroup {
	private static String TAG = Main.class.getCanonicalName();

	public static final int ACTION_NONE = 0;
	public static final int ACTION_RESTORE_LAST_STATE = 1;
	public static final int ACTION_SHOW_AVSCREEN = 2;
	public static final int ACTION_SHOW_CONTSHARE_SCREEN = 3;
	public static final int ACTION_SHOW_SMS = 4;
	public static final int ACTION_SHOW_CHAT_SCREEN = 5;
	public static final int ACTION_SHOW_PUSH = 6;
	public static final int ACTION_SHOW_CALLSCREEN = 7;
	public static final int ACTION_SHOW_HOME = 8;
	public static final int ACTION_SHOW_MAP = 9;

	public static final int ACTION_SHOW_MEDIASCREEN = 10;
	public static final int ACTION_UPDATE_VERSION = 11;

	private static final int RC_SPLASH = 0;

	private Handler mHanler; // ?
	private final Engine mEngine;
	private final IServiceScreen mScreenService; // ?

	public static final int MSG_IS_NETWORK_AVAILABLE = 1000;

	public static final int FILETRANSFERMSG = 1001;
	public static final int FILEDOWNLOADPROGRESS = 1002;
	public static final int FILEDOWNLOAD_SUCCESS = 1003;
	public static final int FILEDOWNLOAD_FAILED = 1013;

	public static final int FILEDOWNLOADERROR = 1004;
	public static final int FILEUPLOADPROGRESS = 1005;
	public static final int FILEUPLOAD_INPROGRESS = 1007;

	public static final int FILEUPLOAD_SUCCESS = 1011;
	public static final int FILEUPLOAD_FAILED = 1012;

	public static final int PROGRESS_GONE = 1012;

	public static final int SKDROIDUPDATEPROGRESS = 1006;

	public static final int FILEDOWN_EXTRA = 1008;
	public static final int FILEUPLOAD_EXTRA = 1009;
	public static final int FILE_NO_EXIST = 1010;

	public static HashMap<String, Object> mMessageReportHashMap = Tools_data
			.readData();

	private boolean MainPTTDown = false;
	public static boolean isFirstPTT_onKeyDown = true;
	public static boolean isFirstPTT_onKeyLongPress = true;
	public static final String processName = "com.sunkaisens.skdroid:back";
	public static final String processNameLog = "com.sunkaisens.skdroid:log";

	public Main() {
		super();

		// Sets main activity (should be done before starting services)
		mEngine = (Engine) Engine.getInstance();
		mEngine.setMainActivity(this); // !!����Activity----Main.java
										// ����Ϊ���������MainActivity���Ϳ�ͨ����MainActivity��ԴActivity��ȥ��������Activity��������BaseScreen����������ЩActivity��
		mScreenService = ((Engine) Engine.getInstance()).getScreenService();

		mHanler = new Handler();

		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {

				MyLog.d(TAG, "init thread  start");

				if (!mEngine.isStarted()) {

					try {
						mEngine.start();
					} catch (Exception e) {
						e.printStackTrace();
					}

					MyLog.d(TAG, "Starts the engine from the main screen");

				}

				init();
			}
		});
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		Log.d(TAG, "main onCreate");

		if (!GlobalSession.bSocketService && mScreenService != null) { // �ǿն���
			if (SystemVarTools.bLogin
					|| mEngine.getSipService().isRegisteSessionConnected()) {
				mScreenService.show(ScreenTabHome.class);

				Log.e("�Ѿ���¼��������", "�Ѿ���¼������");
			} else {
				mScreenService.show(ScreenSplash.class);
				Log.e("δ��¼��������", "δ��¼������");
			}
		}

	}

	public void init() {
		// ��ʼ���ļ�����·��
		SystemVarTools.initFiles(mEngine.getStorageService().getSdcardDir());
		SystemVarTools.sdcardRootPath = mEngine.getStorageService()
				.getSdcardRootDir();

		if (Main.mMessageReportHashMap == null) {
			Main.mMessageReportHashMap = new HashMap<String, Object>();
		}

		if (NgnApplication.isBhSocket()) {
			Tools_data.writeVersion();
		}

		// ��¼�û���ʼ���õ�cscf host
		INgnConfigurationService mConfigurationService = Engine.getInstance()
				.getConfigurationService();
		GlobalVar.pcscfIp = mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST);
		MyLog.d(TAG, "cscf host=" + GlobalVar.pcscfIp);

		if (!NgnApplication.isl8848a_l1860()) { // ��о���ն˲�����ת������
			// �Զ���Ӧ��Ļ����
			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) { // ����
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				Log.d(TAG, "portrait");
			} else { // ����
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				Log.d(TAG, "landscape");
			}
		}

		//if (NgnApplication.isBh04()) { // ����PAD //����
		//	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//	Log.d(TAG, "landscape");
		//}

		if (NgnApplication.isBh()) { // ����PAD �ֳ�̨

			boolean bAec = ((Engine) Engine.getInstance())
					.getConfigurationService().getBoolean(
							NgnConfigurationEntry.GENERAL_AEC,
							NgnConfigurationEntry.DEFAULT_GENERAL_AEC);

			if (bAec) {
				((Engine) Engine.getInstance()).getConfigurationService()
						.putBoolean(NgnConfigurationEntry.GENERAL_AEC, !bAec); // Ĭ�����÷ǻ�������(AEC)

				// Compute
				if (!((Engine) Engine.getInstance()).getConfigurationService()
						.commit()) {
					Log.e(TAG, "Failed to commit() configuration");
				}
			}
		}
		
		

		if (GlobalSession.bSocketService == true) { // ���ն�
			((Engine) Engine.getInstance()).getConfigurationService()
					.putString(
							NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
							tmedia_pref_video_size_t.tmedia_pref_video_size_vga
									.toString()); // Ĭ������ͨ��������QOS�� //5 VGA (640
													// x 480)
													// tmedia_pref_video_size_vga

			// Compute
			if (!((Engine) Engine.getInstance()).getConfigurationService()
					.commit()) {
				Log.e(TAG, "Failed to commit() configuration");
			}
		}
		else { // С�ն˼��
			//GlobalVar.isSecuriteCardExist = Tools_data.getSecurityCard();//����
			GlobalVar.isSecuriteCardExist = false;//�Ǳ���
		}

		// !
		setVolumeControlStream(AudioManager.STREAM_MUSIC); // ?

		// �쳣��������Ҫ����ʱע�͵������伴�ɣ�
		CrashHandler crashHandler = CrashHandler.getInstance();
		// ע��crashHandler
		crashHandler.init(getApplicationContext());

	}

	private LogServiceProcess lsp = null;

	private ServiceConnection serConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			MyLog.d(TAG, "onServiceDisconnected(" + name + ")");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyLog.d(TAG, "onServiceConnected(" + name + ")");
			lsp = LogServiceProcess.Stub.asInterface(service);
			try {
				lsp.startLogging();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onNewIntent(Intent intent) { // ��launch mode����Ϊ single
												// Top��Activity���ᱣ��ջ��Activity���ᱻ�µġ��Լ������棩��onNewIntent��ʹ��ԭintent��������
		super.onNewIntent(intent);

		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			handleAction(bundle);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { // ���û������˵���ʱ�����÷�����This is
													// only called once, the
													// first time the options
													// menu is displayed.
		if (mScreenService != null && mScreenService.getCurrentScreen() != null
				&& mScreenService.getCurrentScreen().hasMenu()) {
			return mScreenService.getCurrentScreen().createOptionsMenu(menu);
		}

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) { // To update the menu every
														// time it is displayed
		if (mScreenService.getCurrentScreen().hasMenu()) {
			menu.clear(); // Remove all existing items from the menu, leaving it
							// empty as if it had just been created.
			return mScreenService.getCurrentScreen().createOptionsMenu(menu);
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // �˵��������Ļص�����
		IBaseScreen baseScreen = mScreenService.getCurrentScreen();
		if (baseScreen instanceof Activity) {
			return ((Activity) baseScreen).onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		MyLog.d(TAG, "onSaveInstanceState()");

		if (mScreenService == null) {
			Log.d(TAG,
					"main onSaveInstanceState exec and mScreenService == null \n"
							+ NgnDateTimeUtils.now());
			super.onSaveInstanceState(outState);
			return;
		}

		IBaseScreen screen = mScreenService.getCurrentScreen();
		if (screen != null) {
			Log.d(TAG, NgnDateTimeUtils.now() + "|\n CurrentScreen is "
					+ screen.getId());
			outState.putInt("action", Main.ACTION_RESTORE_LAST_STATE);
			outState.putString("screen-id", screen.getId());
			outState.putString("screen-type", screen.getType().toString());
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		MyLog.d(TAG, "onRestoreInstanceState()");

		Log.d(TAG,
				"main onRestoreInstanceState  handleAction exec  \n"
						+ "CurrentScreen is "
						+ savedInstanceState.getString("screen-id")
						+ " | action is " + savedInstanceState.getInt("action"));

		this.handleAction(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ")");
		if (resultCode == RESULT_OK) {
			if (requestCode == Main.RC_SPLASH) {
				Log.d(TAG, "Result from splash screen");
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MyLog.d(TAG, "onKeyDown(" + keyCode + "," + event.getAction() + ")");

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& SystemVarTools.bLogin
				&& mScreenService.getCurrentScreen() != null
				&& mScreenService.getCurrentScreen().getType() == SCREEN_TYPE.AV_T) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& !SystemVarTools.bLogin
				&& mScreenService.getCurrentScreen() != null
				&& mScreenService.getCurrentScreen().getType() == SCREEN_TYPE.SPLASH_T) {
			exit();
			return true;
		}

		//if (NgnApplication.isHaiXin() && keyCode != KeyEvent.KEYCODE_BACK) {
		if ((NgnApplication.isBh() || NgnApplication.isHaiXin() ) && keyCode != KeyEvent.KEYCODE_BACK) {
			MyLog.d(TAG, "Hanxun | HaiXin client.");
			if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == 119) {
				event.startTracking();
				MyLog.d(TAG, "event.startTracking() exec.");
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}

		if (!BaseScreen.processKeyDown(keyCode, event)) {
			MyLog.d(TAG, "processKeyDown() is false");
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		MyLog.d(TAG, "onKeyLongPress(" + keyCode + "," + event.getAction()
				+ ")");

		if (GlobalVar.PTTHasLongClickedDown) {
			Log.d(TAG, "ptt - PTTHasLongClickedDown = "
					+ GlobalVar.PTTHasLongClickedDown);
			return false;
		} else {
			Log.d(TAG, "ptt - PTTHasLongClickedDown = "
					+ GlobalVar.PTTHasLongClickedDown);
			GlobalVar.PTTHasLongClickedDown = true;
			this.MainPTTDown = true;
		}
		// �������
		if (!NgnAVSession.hasActiveSession()) {
			if (Engine.getInstance().getSipService()
					.isRegisteSessionConnected()) {
				if (keyCode == 119) { // �ֳ�̨ ptt
					Log.e(TAG, "ptt - keyCode = " + keyCode);
					if (isFirstPTT_onKeyDown) {
						// String groupNo =
						// ServerMsgReceiver.getDefaultGroupNo();
						// ServiceAV.makeCall(groupNo, NgnMediaType.Audio,
						// SessionType.GroupAudioCall);

						String groupNo = ServerMsgReceiver
								.getDefaultGroupNoNull(); // ��ȡ��ǰ��Ⱥ��
						if (groupNo.equals("")) { // ���ΰ�װ�����ֻ��һ���飬������Ϊ��Ⱥ��
							MyLog.e(TAG, "��һ�ΰ�װ�������ļ���û��Ĭ�ϼ�Ⱥ��һ��");

							if (SystemVarTools.getAllOrg().size() == 1) {
								SystemVarTools.setCurrentGroup(SystemVarTools
										.getAllOrg().get(0).mobileNo);

								ServiceAV.makeCall(SystemVarTools.getAllOrg()
										.get(0).mobileNo, NgnMediaType.Audio,
										SessionType.GroupAudioCall);
							} else {
								SystemVarTools.showToast("��ǰ�޼�Ⱥ��");
								MyLog.d(TAG, "��Ⱥ�鲻ֹһ����������Ĭ�ϼ�Ⱥ��");
							}

						} else {

							if (SystemVarTools.isGroupInContact(groupNo)) { // ��ȡ�ļ�Ⱥ����ͨѶ¼��
								MyLog.e(TAG, "groupNo is in contact");

								ServiceAV.makeCall(groupNo, NgnMediaType.Audio,
										SessionType.GroupAudioCall);
							} else { // ��������ϱ�ͨѶ¼�еĵ�һ����

								MyLog.e(TAG, "groupNo is not in contact");

								String newGroupNo = SystemVarTools
										.getFirstOrg();
								if (newGroupNo != null) {

									MyLog.e(TAG, "newgroupNo is :" + newGroupNo);
									SystemVarTools.setCurrentGroup(newGroupNo);

									ServiceAV.makeCall(newGroupNo,
											NgnMediaType.Audio,
											SessionType.GroupAudioCall);
								} else {
									SystemVarTools
											.showToast(Main.this
													.getString(R.string.current_group_not_exit_tip));
								}
							}

						}

						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						isFirstPTT_onKeyDown = false;
					}
				}
			}
			return false;
		}

		// ��ptt
		if (NgnAVSession.hasActiveSession()) {
			if (GlobalSession.avSession != null) {
				int sessionType = GlobalSession.avSession.getSessionType();
				if (sessionType != SessionType.GroupAudioCall
						&& sessionType != SessionType.GroupVideoCall) {
					return false;
				}
			}
		}
		if (Engine.getInstance().getSipService().isRegisteSessionConnected()) {
			if (keyCode == 119) { // �ֳ�̨ ptt
				Log.d(TAG, "ptt - keyCode = " + keyCode);
				if (isFirstPTT_onKeyLongPress) {
					isFirstPTT_onKeyLongPress = false;
				}
				if (ServiceAV.isPTTRejected) {
					MyLog.d(TAG, "PTT has been rejected.");
					return false;
				}
				ServerMsgReceiver.sendPTTRequestInfoMsg();
			}

		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		MyLog.d(TAG, "onKeyUp(" + keyCode + "," + event.getAction() + ")");
		if (this.MainPTTDown) {
			Log.d(TAG, "ptt - MainPTTDown = " + MainPTTDown
					+ ", GlobalVar.PTTHasLongClickedDown = "
					+ GlobalVar.PTTHasLongClickedDown);
			GlobalVar.PTTHasLongClickedDown = false;
			this.MainPTTDown = false;
		} else {
			Log.d(TAG, "ptt - MainPTTDown = " + MainPTTDown
					+ ", GlobalVar.PTTHasLongClickedDown = "
					+ GlobalVar.PTTHasLongClickedDown);
			return false;
		}
		if (NgnAVSession.hasActiveSession()) {
			if (GlobalSession.avSession != null) {
				int sessionType = GlobalSession.avSession.getSessionType();
				if (sessionType != SessionType.GroupAudioCall
						&& sessionType != SessionType.GroupVideoCall) {
					return false;
				}
			}
		}
		if (Engine.getInstance().getSipService().isRegisteSessionConnected()) {
			if (keyCode == 119) { // �ֳ�̨ ptt
				Log.d(TAG, "ptt - keyCode = " + keyCode);
				ServerMsgReceiver.sendPTTReleaseInfoMsg();
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	public void exit() {
		Intent stopNativeService = new Intent(SKDroid.getContext(),
				NativeService.class);
		stopService(stopNativeService);

		Intent stopDaemonService = new Intent(getApplicationContext(),
				DaemonService.class);
		stopService(stopDaemonService);
		// if(lsp != null){
		// try {
		// lsp.stopProcess();
		// } catch (RemoteException e) {
		// e.printStackTrace();
		// }
		// }
		// unbindService(serConn);

		mHanler.post(new Runnable() {
			public void run() {
				try {
					INgnSipService sipService = mEngine.getSipService();
					if (sipService != null) {
						NgnSipStack sipStack = sipService.getSipStack();
						if (sipStack != null) {
							Log.d(TAG, "Stop SipStack!");
							sipService.stopStack();
						}
					}
					if (!Engine.getInstance().stop()) {
						Log.e(TAG, "Failed to stop engine");
					}
					// NgnApplication.getContext().stopService(
					// new Intent(NgnApplication.getContext(),
					// GPSDataService.class));
					// //ֹͣ���봫��������
					// Intent intentSensorService = new Intent(Main.this,
					// SensorService.class);
					// stopService(intentSensorService);
					finish();

					System.exit(0); // sks add over all //�����˳�
				} catch (Exception e) {
					Log.d(TAG, "Exception:" + e.getMessage());
				}
			}
		});
	}

	// remarked by zhaohua on 20140317
	// ��̨������̵Ŀ�����ϵͳ�˳��������ṩ����
	public void background() {

		// PackageManager pm = getPackageManager();
		//
		// ResolveInfo homeInfo = pm.resolveActivity(new
		// Intent(Intent.ACTION_MAIN).
		// addCategory(Intent.CATEGORY_HOME), 0);
		// ActivityInfo ai = homeInfo.activityInfo;
		// Intent startIntent = new Intent(Intent.ACTION_MAIN);
		// startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		// startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
		// startActivity(startIntent);

		mHanler.post(new Runnable() {
			public void run() {
				finish();
			}
		});
	}

	public void showTableHome() {
		PackageManager pm = getPackageManager();

		ResolveInfo homeInfo = pm.resolveActivity(
				new Intent(Intent.ACTION_MAIN)
						.addCategory(Intent.CATEGORY_HOME), 0);
		ActivityInfo ai = homeInfo.activityInfo;
		Intent startIntent = new Intent(Intent.ACTION_MAIN);
		startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
		startActivity(startIntent);
	}

	private void handleAction(Bundle bundle) {
		final String id;
		switch (bundle.getInt("action", Main.ACTION_NONE)) {
		// Default or ACTION_RESTORE_LAST_STATE
		default:
		case ACTION_RESTORE_LAST_STATE:
			id = bundle.getString("screen-id");
			final String screenTypeStr = bundle.getString("screen-type");
			SCREEN_TYPE screenType = SCREEN_TYPE.TAB_HOME;
			if (SystemVarTools.bLogin == false) {
				screenType = SCREEN_TYPE.SCREENLOGIN_T;
			} else if (screenTypeStr != null
					&& screenTypeStr.isEmpty() == false) {
				screenType = SCREEN_TYPE.valueOf(screenTypeStr);
			}
			switch (screenType) {
			case SCREENLOGIN_T:
				mScreenService.show(ScreenLoginAccount.class);
				break;
			case AV_T:
				mScreenService.show(ScreenAV.class, id);
				break;
			case MEDIA_AV_T:
				mScreenService.show(ScreenMediaAV.class, id);
				break;
			default:
				if (!mScreenService.show(id)) {
					if (mEngine.getSipService().isRegisteSessionConnected()) {
						mScreenService.show(ScreenTabHome.class);
					} else {
						mScreenService.show(ScreenSplash.class);
					}
				}
				break;
			}
			break;

		// Notify for new SMSs
		case ACTION_SHOW_SMS:
			Bundle params = new Bundle();
			params.putInt("index", 2);
			IBaseScreen baseScreen = mScreenService.getCurrentScreen();
			if (baseScreen != null && baseScreen instanceof ScreenTabHome) {
				((ScreenTabHome) baseScreen).updateData(2);
			} else {
				mScreenService.show(ScreenTabHome.class, params);
				baseScreen = mScreenService.getScreen(ScreenTabHome.TAG);
				((ScreenTabHome) baseScreen).updateData(2);
			}
			mEngine.cancelSMSNotif();
			break;

		// Show Audio/Video Calls ///!!!
		case ACTION_SHOW_AVSCREEN:
			Log.d(TAG, "Main.ACTION_SHOW_AVSCREEN");

			final int activeSessionsCount = NgnAVSession // !!!��ϵͳ��AVSession��̬�洢����ȡ����ǰ���е�AVsession����
					.getSize(new NgnPredicate<NgnAVSession>() {
						@Override
						public boolean apply(NgnAVSession session) { // �Ӿ�̬�洢���д洢��NgnAVSessionɸѡ�����session
							return session != null && session.isActive();
						}
					});
			if (activeSessionsCount > 1) { // AVsession��������1����
				// mScreenService.show(ScreenAVQueue.class); //
				// ����������AVsession�Ķ���
				MyLog.d(TAG, "activeSessionsCount = " + activeSessionsCount); // Activity
				NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
						.getSessions();
				NgnAVSession session = mAVSessions.getAt(0);
				if (mAVSessions.getAt(1) != null) {
					mAVSessions.getAt(1).hangUpCall();
					MyLog.d(TAG, "hangUpCall(1)");
				}
				if (session != null) {
					MyLog.d(TAG, "showScreenAV(0)");
					if (!mScreenService.show(ScreenAV.class, // /!!!
							// �����ҵ���AVSession��ID��ScreenAV�࣬������֮��
							Long.toString(session.getId())))// !!!��ID��һ��SipSession��ID���Դ�ID�ֱ�ʾ��һ��NgnSipSession��������ô�ID����ScreenAV��ʾ��һ����ScreenAV��������
					{
						if (mEngine.getSipService().isRegisteSessionConnected()) {
							mScreenService.show(ScreenTabHome.class);
						} else {
							mScreenService.show(ScreenSplash.class);
						}
					}
				}
			} else { // AVsession����������1
				NgnAVSession avSession = NgnAVSession
						.getSession(new NgnPredicate<NgnAVSession>() {// ȡ����һ����Ĭ�ϵ�sesion
							@Override
							public boolean apply(NgnAVSession session) {// �Ӿ�̬�洢���д洢��NgnAVSessionɸѡ:���session��û�б�������Է�����
								return session != null && session.isActive()
										&& !session.isLocalHeld()
										&& !session.isRemoteHeld();
							}
						});
				if (avSession == null) {// �޷�������Ҫ���session������ȥȡ�������session
					avSession = NgnAVSession
							.getSession(new NgnPredicate<NgnAVSession>() {
								@Override
								public boolean apply(NgnAVSession session) {
									return session != null
											&& session.isActive();
								}
							});
				}
				if (avSession != null) {// ����ҵ��˱������session����ȥ����ScreenAV�����������ʧ��������ScreenHome��
					if (!mScreenService.show(ScreenAV.class, // /!!!
																// �����ҵ���AVSession��ID��ScreenAV�࣬������֮��
							Long.toString(avSession.getId())))// !!!��ID��һ��SipSession��ID���Դ�ID�ֱ�ʾ��һ��NgnSipSession��������ô�ID����ScreenAV��ʾ��һ����ScreenAV��������
					{
						if (mEngine.getSipService().isRegisteSessionConnected()) {
							mScreenService.show(ScreenTabHome.class);
						} else {
							mScreenService.show(ScreenSplash.class);
						}
					}
				} else {// ��̬�洢���д洢��NgnAVSession����Ϊ0
					Log.e(TAG, "Failed to find associated audio/video session");
					if (mEngine.getSipService().isRegisteSessionConnected()) {
						mScreenService.show(ScreenTabHome.class);
					} else {
						mScreenService.show(ScreenSplash.class);
					}
					mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
				}
			}
			break;

		case ACTION_SHOW_MEDIASCREEN:
			Log.d(TAG, "Main.ACTION_SHOW_MEDIASCREEN");

			final int activeMeidaSessionsCount = NgnMediaSession // !!!��ϵͳ��NgnMediaSession��̬�洢����ȡ����ǰ���е�AVsession����
					.getSize(new NgnPredicate<NgnMediaSession>() {
						@Override
						public boolean apply(NgnMediaSession session) { // �Ӿ�̬�洢���д洢��NgnMediaSessionɸѡ�����session
							return session != null && session.isActive();
						}
					});
			if (activeMeidaSessionsCount > 1) { // AVsession��������1����
				// mScreenService.show(ScreenAVQueue.class); //
				// ����������AVsession�Ķ���
				// --ScreenAVQueue
				// Activity
			} else { // AVsession����������1
				Log.d(TAG, "Main.ACTION_SHOW_MEDIASCREEN count = "
						+ activeMeidaSessionsCount);
				NgnMediaSession mediaSession = NgnMediaSession
						.getSession(new NgnPredicate<NgnMediaSession>() {// ȡ����һ����Ĭ�ϵ�sesion
							@Override
							public boolean apply(NgnMediaSession session) {// �Ӿ�̬�洢���д洢��NgnMediaSessionɸѡ:���session��û�б�������Է�����
								return session != null && session.isActive();
								// && !session.isLocalHeld()
								// && !session.isRemoteHeld();
							}
						});

				if (mediaSession != null) {// ����ҵ��˱������session����ȥ����ScreenMediaAV�����������ʧ��������ScreenTabHome��
					if (!mScreenService.show(ScreenMediaAV.class, // /!!!
																	// �����ҵ���MediaSession��ID��ScreenMediaAV�࣬������֮��
							Long.toString(mediaSession.getId())))// !!!��ID��һ��MediaSession��ID���Դ�ID�ֱ�ʾ��һ��NgnMediaSession��������ô�ID����ScreenMeidaAV��ʾ��һ����ScreenMediaAV��������
					{
						if (mEngine.getSipService().isRegisteSessionConnected()) {
							mScreenService.show(ScreenTabHome.class);
						} else {
							mScreenService.show(ScreenSplash.class);
						}
					}
				} else {// ��̬�洢���д洢��NgnAVSession����Ϊ0
					Log.e(TAG, "Failed to find associated audio/video session");
					if (mEngine.getSipService().isRegisteSessionConnected()) {
						mScreenService.show(ScreenTabHome.class);
					} else {
						mScreenService.show(ScreenSplash.class);
					}
					mEngine.refreshAVCallNotif(R.drawable.phone_call_25);
				}
			}
			break;

		// Show Content Share Queue
		case ACTION_SHOW_CONTSHARE_SCREEN:
			mScreenService.show(ScreenFileTransferQueue.class);
			break;

		// Show Chat Queue
		case ACTION_SHOW_CHAT_SCREEN:
			mScreenService.show(ScreenChatQueue.class);
			break;

		// Notify for new PUSHs
		case ACTION_SHOW_HOME:
			mScreenService.show(ScreenTabHome.class);
			break;

		// Show Call List
		case ACTION_SHOW_CALLSCREEN:
			// mScreenService.show(ScreenTabCall.class);
			// mScreenService.show(ScreenTabHome.class)
			// gzc 20141015
			// ���δ������֪ͨ��ֱ����ת��������н��棬��ȡ��δ������֪ͨ
			Bundle params2 = new Bundle();
			params2.putInt("index", 0);
			IBaseScreen baseScreen2 = mScreenService.getCurrentScreen();
			if (baseScreen2 != null && baseScreen2 instanceof ScreenTabHome) {
				((ScreenTabHome) baseScreen2).updateData(0);
			} else {
				mScreenService.show(ScreenTabHome.class, params2);
				baseScreen = mScreenService.getScreen(ScreenTabHome.TAG);
				((ScreenTabHome) baseScreen).updateData(0);
			}
			mEngine.cancelAVCallNotNotif();
			break;
		case ACTION_SHOW_PUSH:
			mEngine.cancelPushNotif();
			break;
		case ACTION_SHOW_MAP:
			mScreenService.show(ScreenMap.class);
			break;
		case ACTION_UPDATE_VERSION:
			Bundle dataBundle = new Bundle();
			dataBundle.putString("action", "UPDATE_APP");
			mScreenService.show(ScreenSettings.class, dataBundle);
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "Main class destroy!!!");
		super.onDestroy();
		Log.d(TAG, "Main class destroy  finished!!!");
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "Main class pause!!!");
		super.onPause();
		Log.d(TAG, "Main class pause  finished!!!");
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "Main class resume!!!");
		super.onResume();
		Log.d(TAG, "Main class resume  finished");

		if (GlobalSession.bSocketService) {
			finish();
		}

	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Main class stop!!!");
		super.onStop();
		Log.d(TAG, "Main class stop  finished!!!");
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "Main class restart!!!!");
		super.onRestart();
	}
}