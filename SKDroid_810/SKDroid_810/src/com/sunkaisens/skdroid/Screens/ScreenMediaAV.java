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
 * 
 * @contributors: See $(DOUBANGO_HOME)\contributors.txt
 */
package com.sunkaisens.skdroid.Screens;

import java.nio.ByteBuffer;
import java.util.TimerTask;

import org.doubango.ngn.events.AdhocSessionEventArgs;
import org.doubango.ngn.events.AdhocSessionEventTypes;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnMediaSession.NgnMediaSessionState;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Services.ServiceFragment;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.encryptcall.EncryptProcess;
import com.sunkaisens.skdroid.fragments.AVGroupAudioFragment;
import com.sunkaisens.skdroid.fragments.AVGroupVideoFragment;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenMediaAV extends BaseScreen {
	private static final String TAG = ScreenMediaAV.class.getCanonicalName();

	private static int mLastRotation; // values: degrees
	private boolean mSendDeviceInfo;
	private int mLastOrientation; // values: portrait, landscape...

	private String mRemotePartyDisplayName;
	private int imageid = 0;

	private ViewType mCurrentView;
	private LayoutInflater mInflater;
	private RelativeLayout mMainLayout;
	private FrameLayout frameAbove;

	private FrameLayout mViewLocalVideoPreview;
	private View mViewTermwait;
	private View mViewProxSensor;

	private final NgnTimer mTimerSuicide;
	private final NgnTimer mTimerBlankPacket;

	public boolean mIsVideoCall;

	private TextView mTvInfo;
	private GroupPTTCall mPttCall;

	private KeyguardLock mKeyguardLock;
	private OrientationEventListener mListener;

	private PowerManager.WakeLock mWakeLock;

	private static boolean SHOW_SIP_PHRASE = true;

	ServiceAV serviceAV = null;

	private static int mSessionType;

	final int MSG_IS_VIDEO_REFRESH = 1000;
	final int PTTENABLE = 7001;
	final int PTTNOTENABLE = 7002;

	private boolean isSuicide = false;

	private static boolean isScreenChange = false;

	public static boolean ispeoplePTT = false;

	private AVGroupVideoFragment AVGroupVideo;
	private AVGroupAudioFragment AVGroupAudio;

	private MyProxSensor mProxSensor;

	private static enum ViewType {
		ViewNone, ViewTrying, ViewInCall, ViewProxSensor, ViewTermwait
	}

	public ScreenMediaAV() {
		super(SCREEN_TYPE.MEDIA_AV_T, TAG);

		MyLog.d(TAG, "ScreanMediaAV create");

		mCurrentView = ViewType.ViewNone;

		mTimerSuicide = new NgnTimer();
		mTimerBlankPacket = new NgnTimer();
		// mTimerPTTReport = new NgnTimer();

		mPttCall = new GroupPTTCall();

		isSuicide = false;
		MyLog.d(TAG, "ScreanMediaAV create OK.");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_av);

		Log.d(TAG, "ScreanMediaAV alive:: onCreate()");

		super.mId = getIntent().getStringExtra("id"); // java中子类可以直接使用父类成员//获取屏的ID实际上也是AVSession，SipSesion的ID
		if (NgnStringUtils.isNullOrEmpty(super.mId)) {
			Log.e(TAG, "ScreanMediaAV oncreate:: Invalid audio/video session");
			finish();
			// back();
			// mScreenService.show(ScreenMyHome.class);
			return;
		}
		if (SKDroid.sks_version == VERSION.ADHOC) {
			NgnMediaSession mediaSession = NgnMediaSession
					.getSession(NgnStringUtils.parseLong(super.mId, -1));
			if (mediaSession == null) {
				Log.e(TAG,
						String.format(
								"ScreanMediaAV oncreate:: Cannot find audio/video session with id=%s",
								super.mId));
				finish();
				// back(); //error?? //发生错误：java.lang.StackOverflowError
				// mScreenService.show(ScreenMyHome.class);
				return;
			}
			serviceAV = ServiceAV.create(mediaSession, this);

		}

		serviceAV.registerReceiver();
		MyLog.d(TAG, "serviceAV create and registe ok. SessionState = "
				+ serviceAV.getMediaSession().getSessionState());

		mRemotePartyDisplayName = serviceAV.getMediaSession()
				.getRemotePartyUri();
		// mRemotePartyDisplayName = mModelContact.name; // 通话对象的显示名称

		if (mRemotePartyDisplayName.contains("255.255.255")) {
			mRemotePartyDisplayName = ScreenMediaAV.this
					.getString(R.string.mdeiaav_group_calling);
		}
		MyLog.d(TAG, "RemoteParty is " + mRemotePartyDisplayName);
		imageid = 1;

		mIsVideoCall = serviceAV.getMediaSession().getMediaType() == NgnMediaType.AudioVideo // 获取通话类型：Audio
																								// or
																								// Video
				|| serviceAV.getMediaSession().getMediaType() == NgnMediaType.Video;

		mSendDeviceInfo = getEngine().getConfigurationService().getBoolean(
				// ???
				NgnConfigurationEntry.GENERAL_SEND_DEVICE_INFO,
				NgnConfigurationEntry.DEFAULT_GENERAL_SEND_DEVICE_INFO);
		mLastRotation = -1;
		mLastOrientation = -1;

		mInflater = LayoutInflater.from(this); // ??

		mMainLayout = (RelativeLayout) findViewById(R.id.screen_av_relativeLayout);
		frameAbove = (FrameLayout) findViewById(R.id.frameAbove);

		if (serviceAV.getMediaSession().isGroupAudioCall()
				|| serviceAV.getMediaSession().isGroupVideoCall()) {
			Log.d(TAG, "ScreanMediaAV onCreate:: loadGroupView()");
			loadGroupView();
		} else {
			Log.d(TAG, "ScreanMediaAV onCreate:: loadView()");
			loadView();
		}

		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL); // 设置设备的音量控制的硬件按钮控制的音频流为STREAM_VOICE_CALL类型的流

		isSuicide = false;
		Log.d(TAG, "ScreanMediaAV alive:: onCreate  OK");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "ScreanMediaAV alive:: onStart()");

		final KeyguardManager keyguardManager = SKDroid.getKeyguardManager();
		if (keyguardManager != null) {
			if (mKeyguardLock == null) {
				mKeyguardLock = keyguardManager
						.newKeyguardLock(ScreenMediaAV.TAG);
			}
			if (keyguardManager.inKeyguardRestrictedInputMode()) {
				mKeyguardLock.disableKeyguard();
			}
		}

		final PowerManager powerManager = SKDroid.getPowerManager();
		if (powerManager != null && mWakeLock == null) {
			mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE
					| PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
			if (mWakeLock != null) {
				mWakeLock.acquire();
			}
		}

		if (mProxSensor == null && !SKDroid.isBuggyProximitySensor()) {
			mProxSensor = new MyProxSensor(this);
		}
		Log.d(TAG, "ScreanMediaAV alive:: onStart  OK");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "ScreanMediaAV alive:: onPause()");

		if (mProxSensor != null) {
			mProxSensor.stop();
		}

		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}

		if (mListener != null && mListener.canDetectOrientation()) {
			mListener.disable();
		}

		GlobalVar.bBackOrSwitch = true;
		Log.d(TAG, "ScreanMediaAV alive:: onPause  OK");
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.d(TAG, "ScreanMediaAV alive:: onResume()");

		if (mProxSensor != null) {
			mProxSensor.start();
		}

		if (mListener != null && mListener.canDetectOrientation()) {
			mListener.enable();
		}

		Log.d(TAG, "ScreanMediaAV mIsVideoCall:" + mIsVideoCall
				+ ", mediaSession = " + serviceAV.getMediaSession());
		if (serviceAV != null
				&& serviceAV.getMediaSession() != null
				&& serviceAV.getMediaSession().getSessionState() == NgnMediaSessionState.CONNECTED) {
			if (mIsVideoCall) {
				changeFragmmentSigns(false);
				if (serviceAV.getMediaSession().isVideoMonitorCall()) {
					loadInCallVideoMonitorView();

				} else if (serviceAV.getMediaSession().isGroupVideoCall()) {
					loadGroupInCallVideoView();

				} else {
					loadInCallVideoView();
				}
			} else if (serviceAV.getMediaSession().isGroupAudioCall()) {
				loadGroupInCallAudioView();

			} else {
				loadInCallAudioView();
			}
		}

		GlobalVar.bBackOrSwitch = false;
		Log.d(TAG, "ScreanMediaAV alive:: onResume  OK");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "ScreanMediaAV alive:: onStop()");

		if (mKeyguardLock != null) {
			mKeyguardLock.reenableKeyguard();
		}

		Main.isFirstPTT_onKeyDown = true;
		Main.isFirstPTT_onKeyLongPress = true;

	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "ScreanMediaAV alive:: onDestroy()");

		mTimerSuicide.cancel();

		// cancelBlankPacket();

		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
		mWakeLock = null;

		if (serviceAV != null) {
			serviceAV.unRegisterReceiver();
			serviceAV.release();
		}
		super.onDestroy();
	}

	// @Override
	// protected void onNewIntent(Intent intent) {
	//
	// Log.d(TAG, "ScreanMediaAV alive:: onNewIntent");
	// changeFragmmentSigns(false);
	// if (serviceAV.getMediaSession().isGroupAudioCall()) {
	// Log.d(TAG, "onNewIntent call Type: groupAudioCall"+ ", mediaSession = " +
	// serviceAV.getMediaSession());
	//
	// } else if (serviceAV.getMediaSession().isGroupVideoCall()) {
	// Log.d(TAG, "onNewIntent call Type: groupVideoCall");
	// if(serviceAV.getMediaSession().isConnected()){
	// loadGroupInCallVideoView();
	// }else {
	// loadGroupTryingView();
	// }
	//
	// } else if (mIsVideoCall) {
	// Log.d(TAG, "call Type: VideoCall");
	// if(serviceAV.getMediaSession().isConnected()){
	// loadInCallVideoView();
	// }else {
	// loadTryingView();
	// }
	// } else {
	// Log.d(TAG, "call Type: audioCall");
	// }
	// super.onNewIntent(intent);
	// }

	@Override
	public boolean hasBack() {
		return true;
	}

	@Override
	public boolean back() {
		boolean ret = super.back();
		// if (ret) {
		// mScreenService.destroy(getId());
		// }

		GlobalVar.bBackOrSwitch = true;
		return ret;
	}

	public boolean onVolumeChanged(boolean bDown) {
		if (serviceAV.getMediaSession() != null) {
			return serviceAV.getMediaSession().onVolumeChanged(bDown);
		}
		return false;
	}

	public void handleMediaEvent(Intent intent) {
		final String action = intent.getAction();

		if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(action)) {
			NgnMediaPluginEventArgs args = intent
					.getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}

			Log.d(TAG,
					"Session type handleMediaEvent eventType:"
							+ args.getEventType());

			switch (args.getEventType()) {
			case STARTED_OK: // started or restarted (e.g. reINVITE)
			{
				mIsVideoCall = (serviceAV.getMediaSession().getMediaType() == NgnMediaType.AudioVideo || serviceAV
						.getMediaSession().getMediaType() == NgnMediaType.Video);
				Log.d(TAG, "Session type handleMediaEvent mMediaType: "
						+ serviceAV.getMediaSession().getMediaType());
				MyLog.d(TAG, "loadInCallVideoView  00315");
				loadView();

				break;
			}
			case PREPARED_OK:
			case PREPARED_NOK:
			case STARTED_NOK:
			case STOPPED_OK:
			case STOPPED_NOK:
			case PAUSED_OK:
			case PAUSED_NOK: {
				break;
			}
			}
		}
	}

	public void handleAdhocSessionEvent(Intent intent) {
		Log.d(TAG, "handleAdhocSessionEvent()  NgnMediaSession");

		NgnMediaSessionState state;
		if (serviceAV.getMediaSession() == null) {
			Log.e(TAG, "Invalid session object");
			return;
		}

		final String action = intent.getAction();
		Log.d(TAG, "action=" + action);
		if (AdhocSessionEventArgs.ADHOC_SESSION_EVENT.equals(action)) {
			AdhocSessionEventArgs args = intent
					.getParcelableExtra(AdhocSessionEventArgs.EXTRA_EMBEDDED);

			AdhocSessionEventTypes eventType = args.getEventType(); // INCALL
																	// INCOMMING
			Log.d(TAG,
					String.format("mediaSession = %s",
							serviceAV.getMediaSession()));
			Log.d(TAG, "eventtype = " + eventType + ", sessionState = "
					+ serviceAV.getMediaSession().getSessionState());
			if (args == null) {
				Log.e(TAG, "Invalid event args(args is null).");
				return;
			}

			if (serviceAV.getMediaSession().getId() != args.getSessionId()) {
				Log.d(TAG, "Receive a call,handling.1111");
				return;
			}
			if (eventType.equals(AdhocSessionEventTypes.INCALL)// 主叫流程
					|| eventType.equals(AdhocSessionEventTypes.INCOMING)) {// 被叫流程
				Log.d(TAG, "state ="
						+ serviceAV.getMediaSession().getSessionState());
				switch ((state = serviceAV.getMediaSession().getSessionState())) { // TERMINATED
				default:
					break;

				case INPROGRESS:
					// loadTryingView();
					break;

				case CONNECTED:
					serviceAV.setOnPause(true);

					getEngine().getSoundService().stopRingTone();
					serviceAV.getMediaSession().setGroupAudioTimerStart(true);
					// serviceAV.setSpeakerphoneOn(true);

					if (serviceAV.getMediaSession().isGroupAudioCall()) {
						Log.d(TAG, "call Type: groupAudioCall");
						// mTimerGroupInCall.schedule(mTimerTaskGroupInCall,
						// 0, 1000);
						loadGroupInCallAudioView();
					} else if (serviceAV.getMediaSession().isGroupVideoCall()) {
						Log.d(TAG, "call Type: groupVideoCall");

						serviceAV.getMediaSession().setmSendVIdeo(false);
						loadGroupInCallVideoView();
					} else if (mIsVideoCall) {
						Log.d(TAG, "call Type: VideoCall");
						// mTimerVideoDuraton.schedule(mTimerTaskVideoDuraton,
						// 0, 1000);
						loadInCallVideoView();
					} else {
						Log.d(TAG, "call Type: audioCall");
						/*
						 * mTimerInCall.schedule(mTimerTaskInCall, 0, 1000);
						 * mTimerInCallIsStart = true;
						 */
						loadInCallAudioView();
					}
					/*
					 * if(eventType.equals(AdhocSessionEventTypes.INCALL)) {
					 * serviceAV.getMediaSession().setConsumerOnPause(true);
					 * serviceAV.getMediaSession().setProducerOnPause(false);
					 * }else{
					 * serviceAV.getMediaSession().setConsumerOnPause(false);
					 * serviceAV.getMediaSession().setProducerOnPause(true); }
					 */
					break;
				case TERMINATED: // Call Terminated
					// release power lock

					if (mWakeLock != null && mWakeLock.isHeld()) {
						mWakeLock.release();
						Log.d(TAG, "InviteEvent release mWakeLock");
					}
					Log.d(TAG, "InviteEvent TERMINATED%%%%%");
					//
					loadTermView("Call Terminated");
					serviceAV.getMediaSession().hungUp();
					break;
				}
			} else if (eventType.equals(AdhocSessionEventTypes.PTT_REQUEST)) {// PTT
				if (AVGroupAudio != null) {
					AVGroupAudio.handleRequestPTT(args.getmPTTState());
				}
			}
		}

	}

	public void loadView() {

		switch (serviceAV.getMediaSession().getSessionState()) { // INPROGRESS
		case INCOMMING:
		case INPROGRESS:

			loadTryingView();
			break;

		case CONNECTED:
			MyLog.d(TAG, "loadInCallVideoView  0015");
			loadInCallView();
			break;
		case TERMINATED:
		default:
			Log.d(TAG, "loadView loadTermView()");
			loadTermView();
			break;
		}
	}

	private void loadGroupView() { // duhaitao 添加
		switch (serviceAV.getMediaSession().getSessionState()) {
		case INPROGRESS:
			loadGroupTryingView();
			break;

		case CONNECTED:
			loadGroupInCallView();
			break;

		case TERMINATED:
		default:
			Log.d(TAG, "loadGroupView loadTermView()");
			loadTermView();
			break;
		}

	}

	/** 正在呼叫时的View */
	public void loadTryingView() {
		Log.d(TAG, "loadTryingView");
		if (mCurrentView == ViewType.ViewTrying) {
			MyLog.d(TAG, "Current View is trying view.");
			return;
		}

		mCurrentView = ViewType.ViewTrying;

		mSessionType = serviceAV.getMediaSession().getSessionType();

		switch (serviceAV.getMediaSession().getSessionState()) {
		case INCOMMING:
			Log.d(TAG, "INCOMING   SessionType=" + mSessionType);

			switch (mSessionType) {
			case SessionType.AudioCall:
			case SessionType.VideoCall:
			case SessionType.VideoTransmit:

			case SessionType.VideoMonitor:
			case SessionType.GroupVideoMonitor:
				ServiceFragment.makeAvSingleIncomingFragment(this, serviceAV,
						mMainLayout);
				break;

			default:
				mTvInfo.setText(getString(R.string.string_call_incoming)); // 来电呼入

				break;

			}

			break;

		case INPROGRESS:
		default:
			Log.d(TAG, "outgoing  SessionType=" + mSessionType);
			switch (mSessionType) {
			case SessionType.AudioCall:
				ServiceFragment.makeAvSingleAudioTryingFragment(this,
						serviceAV, mMainLayout);
				break;

			case SessionType.VideoUaMonitor:
			case SessionType.VideoCall:

				ServiceFragment.makeAvSingleVideoTryingFragment(this,
						serviceAV, mMainLayout); // 正在视频呼叫
				break;
			case SessionType.GroupVideoCall:
				ServiceFragment.makeAvGroupVideoTryingFragment(this, serviceAV,
						mMainLayout);

			default:
				// mTvInfo.setText(getString(R.string.string_call_outgoing)); //
				// 正在呼叫

				break;

			}
			break;
		}
		Log.d(TAG, "loadTryingView ok");
	}

	public void handleEncryptInfoMsg(byte[] infoContent) {
		Log.d("zhangjie:handleEncryptInfoMsg()", "handleEncryptInfoMsg");
		Log.d(TAG, "receive a encrypt_info!!!!");
		EncryptProcess proc = new EncryptProcess();
		ByteBuffer retBytes = proc.process(infoContent);
		if (EncryptProcess.ENCRYPT_BEGIN.equals(proc.getEncryptState())) {
			Log.d(TAG, "receive a encrypt_info:ENCRYPT_BEGIN!!!!");
			mTvInfo.setText(getString(R.string.string_call_keybegining));
			serviceAV.sendInfo(retBytes, NgnContentType.USSD_INFO);
			mTvInfo.setText(getString(R.string.string_call_keyrequesting));
		} else if (EncryptProcess.ENCRYPT_KEY_DIS
				.equals(proc.getEncryptState())) {
			Log.d(TAG, "receive a encrypt_info:ENCRYPT_KEY_DIS!!!!");
			mTvInfo.setText(getString(R.string.string_call_keydis));
			serviceAV.sendInfo(retBytes, NgnContentType.USSD_INFO);
			mTvInfo.setText(getString(R.string.string_call_keyresq));
			if (!proc.isCaller())
				getEngine().getSoundService().startRingTone();
		}

	}

	/** 组呼时 */
	public void loadGroupTryingView() { //
		Log.d(TAG, "loadGroupTryingView()");
		if (mCurrentView == ViewType.ViewTrying) {
			return;
		}
		mSessionType = serviceAV.getMediaSession().getSessionType();
		MyLog.d(TAG, "GroupCall  trying; mSessionType:" + mSessionType
				+ "  mSessionState:"
				+ serviceAV.getMediaSession().getSessionState());
		switch (serviceAV.getMediaSession().getSessionState()) {
		case INCOMMING:
			// serviceAV.acceptCall();

		case INPROGRESS:
		default:
			switch (mSessionType) {
			case SessionType.GroupAudioCall:
				MyLog.d(TAG, "GroupAudioCall。。  isGroupAudioFragmentAdded: "
						+ isGroupAudioFragmentAdded);
				// 正在语音组呼
				if (!isGroupAudioFragmentAdded) {
					AVGroupAudio = ServiceFragment.makeAvGroupAudioFragment(
							this, serviceAV, mMainLayout);
					isGroupAudioFragmentAdded = true;
					AVGroupVideo = null;
				}

				break;

			case SessionType.GroupVideoCall:
				ServiceFragment.makeAvGroupVideoTryingFragment(this, serviceAV,
						mMainLayout); // 正在视频组呼

				break;

			default:

				break;
			}
		}
		serviceAV.setOnPause(false);
		serviceAV.setSpeakerphoneOn(true); // 默认打开扬声器
	}

	private void loadGroupInCallView() { // //duhaitao 添加
		Log.d(TAG, "loadGroupInCallView()");
		if (mCurrentView == ViewType.ViewInCall) {
			return;
		}
		Log.d(TAG, "loadGroupInCallView()");
		if (mIsVideoCall && serviceAV.getMediaSession().isGroupVideoCall()) {
			Log.d(TAG, "is video or groupvideo call");
			// startVideo(true, true);
			loadGroupInCallVideoView();
		} else {
			Log.d(TAG, "is audio or groupaudio call");
			loadGroupInCallAudioView();
		}
	}

	private boolean isGroupAudioFragmentAdded = false;

	private void loadGroupInCallAudioView() { // duhaitao 添加
		Log.d(TAG, "loadGroupInCallAudioView()");

		if (!isGroupAudioFragmentAdded) {
			AVGroupAudio = ServiceFragment.makeAvGroupAudioFragment(this,
					serviceAV, mMainLayout);
			isGroupAudioFragmentAdded = true;
			AVGroupVideo = null;
		}
	}

	private boolean isGroupVideoFragmentAdded = false;

	private void loadGroupInCallVideoView() { // xunzy 添加
		Log.d(TAG, "loadGroupInCallVideoView()");

		if (!isGroupVideoFragmentAdded) {
			AVGroupVideo = ServiceFragment.makeAvGroupVideoFragment(this,
					serviceAV, mMainLayout);
			isGroupVideoFragmentAdded = true;
			AVGroupAudio = null;
		}
		mCurrentView = ViewType.ViewInCall;

	}

	private boolean isSingleAudioFragmentAdded = false;

	private void loadInCallAudioView() {
		Log.d(TAG, "loadInCallAudioView()  标记:" + isSingleAudioFragmentAdded);

		if (!isSingleAudioFragmentAdded) {
			ServiceFragment.makeAvSingleAudioFragment(this, serviceAV,
					mMainLayout);
			isSingleAudioFragmentAdded = true;
		}
	}

	private boolean isSingleVideoFragmentAdded = false;

	private void loadInCallVideoView() {
		Log.d(TAG, "loadInCallVideoView()   标记:" + isSingleVideoFragmentAdded);

		if (!isSingleVideoFragmentAdded) {
			ServiceFragment.makeAvSingleVideoFragment(this, serviceAV,
					mMainLayout);
			isSingleVideoFragmentAdded = true;
		}

	}

	private boolean isVideoMonitorFragmentAdded = false;

	private void loadInCallVideoMonitorView() {
		Log.d(TAG, "loadInCallVideoView()   标记:" + isSingleVideoFragmentAdded);

		if (!isVideoMonitorFragmentAdded) {
			ServiceFragment.makeAvVideoMonitorFragment(this, serviceAV,
					mMainLayout);
			isVideoMonitorFragmentAdded = true;
		}

	}

	private void loadInCallView() {
		// Log.d(TAG, "loadInCallView()");
		if (mCurrentView == ViewType.ViewInCall) {
			return;
		}
		// Log.d(TAG, "loadInCallView()");

		if (mIsVideoCall) {
			Log.d(TAG, "IsVideoCall");
			// Log.d(TAG,
			// "ScreanMediaAV mModelContact.mobileNo:"+mModelContact.mobileNo);
			// if (mModelContact != null
			// && mModelContact.userType != null
			// && mModelContact.userType.equals("1")) {
			// if (this.getResources().getConfiguration().orientation ==
			// Configuration.ORIENTATION_PORTRAIT) { //竖屏
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			// //横屏
			// mTimerInCallIsStart = false;
			// isScreenChange = true;
			// GlobalVar.isLandscap=true;
			// Log.d(TAG, "landscape");
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// }
			// }

			// if (mModelContact != null
			// &&(mModelContact.mobileNo.startsWith("05013")
			// || mModelContact.mobileNo.startsWith("02013"))) {
			// if (this.getResources().getConfiguration().orientation ==
			// Configuration.ORIENTATION_PORTRAIT) { //竖屏
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			// //横屏
			// Log.d(TAG, "landscape");
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// }
			// }

			// startVideo(true,true); //remarked by zhaohua on 20140831 for test
			MyLog.d(TAG, "loadInCallVideoView  005");
			loadInCallVideoView();
		} else {
			Log.d(TAG, "IsAudioCall");
			loadInCallAudioView();
		}
		mCurrentView = ViewType.ViewInCall;
	}

	private void loadProxSensorView() {
		Log.d(TAG, "loadProxSensorView()");
		if (mCurrentView == ViewType.ViewProxSensor) {
			return;
		}
		Log.d(TAG, "loadProxSensorView()");
		GlobalVar.bBackOrSwitch = true;
		if (mViewProxSensor == null) {
			mViewProxSensor = mInflater.inflate(R.layout.view_call_proxsensor,
					null);
		}
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewProxSensor);
		mCurrentView = ViewType.ViewProxSensor;
	}

	private void loadTermView(String phrase) {
		Log.d(TAG, "loadTermView()");

		SystemVarTools.showToast(
				ScreenMediaAV.this.getString(R.string.call_finished_with_dot),
				false);

		GlobalVar.bBackOrSwitch = false;

		if (serviceAV != null && serviceAV.getMediaSession() != null
				&& serviceAV.getMediaSession().getmVideoProducer() != null) {
			// serviceAV.getMediaSession().setmSendVIdeo(false);
			Log.d(TAG, "TermView mSendVideo:false");
		}

		if (mViewTermwait == null) {
			mViewTermwait = mInflater.inflate(R.layout.view_call_trying, null);
			// loadKeyboard(mViewTermwait);
		}

		Log.d(TAG, "phrase = " + phrase); // Call Terminated / Call Cancelled /
											// Busy Here
		mTvInfo = (TextView) mViewTermwait
				.findViewById(R.id.view_call_trying_textView_info);
		// mTvInfo.setText(NgnStringUtils.isNullOrEmpty(phrase) ?
		// getString(R.string.string_call_terminated)
		// : phrase);
		mTvInfo.setText(getString(R.string.string_call_terminated)); // 通话已终止
		Log.d(TAG, "loadTermView prepare to deal phrase...");
		if (!NgnStringUtils.isNullOrEmpty(phrase)) {
			if (phrase.equals("Call Terminated")
					|| phrase.equals("Terminating dialog")) {
				// mTvInfo.setText(getString(R.string.string_call_terminated));
				// //通话已终止
				Log.d(TAG, "loadTermView deal with call terminated...");
				switch (serviceAV.getMediaSession().getMediaType()) {

				default:
					mTvInfo.setText(getString(R.string.string_call_terminated)); // 通话已终止
					break;
				}
			}

		}

		// loadTermView() could be called twice (onTermwait() and OnTerminated)
		// and this is why we need to
		// update the info text for both
		if (mCurrentView == ViewType.ViewTermwait) {
			return;
		}

		Log.d(TAG, "loadTermView prepare views...");

		final TextView tvRemote = (TextView) mViewTermwait
				.findViewById(R.id.view_call_trying_textView_remote);
		final ImageView ivAvatar = (ImageView) mViewTermwait
				.findViewById(R.id.view_call_trying_imageView_avatar);
		mViewTermwait.findViewById(R.id.view_call_trying_imageButton_pick)
				.setVisibility(View.GONE);
		mViewTermwait.findViewById(R.id.view_call_trying_imageButton_hang)
				.setVisibility(View.GONE);
		mViewTermwait.setBackgroundResource(R.drawable.grad_bkg_termwait);

		tvRemote.setText(mRemotePartyDisplayName);
		ivAvatar.setImageResource(SystemVarTools.getThumbID(imageid));

		Log.d(TAG, "loadTermView mMainLayout prepare removing all views...");
		mMainLayout.removeAllViews();

		changeFragmmentSigns(false);

		Log.d(TAG, "loadTermView mMainLayout remove all views over");
		mMainLayout.addView(mViewTermwait);
		Log.d(TAG, "loadTermView mMainLayout add mViewTermwait view over ");
		mCurrentView = ViewType.ViewTermwait;

		/**
		 * 视频监控，设置终端为横屏方式，但是关闭视频时，终端界面隐藏了，下面解决这个问题了。
		 */
		Log.d(TAG, "loadTermView prepare to check user type...");
		// if (mModelContact != null && mModelContact.userType != null
		// && mModelContact.userType.equals("1")
		// && mScreenService != null
		// && mScreenService.getCurrentScreen() != null) {
		// if (getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_LANDSCAPE) { //横屏
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// //竖屏
		// Log.d(TAG, "portrait");
		// }
		// }

		// 获取当前屏幕宽高
		// DisplayMetrics dm = new DisplayMetrics();
		// dm = getResources().getDisplayMetrics();
		Log.d(TAG, "isScreenChange:" + isScreenChange);

		Log.d(TAG, "loadTermView prepare changing to ScreenTabHome");
		mScreenService.show(ScreenTabHome.class); // zhaohua add 20140713
													// 解决通话结束后界面最小化的问题
		NgnProxyPluginMgr.setDefaultMaxVideoSize();
	}

	private void loadTermView() {
		Log.d(TAG, "loadTermView() loadTermView(null)");
		loadTermView(null);
	}

	private TimerTask mTimerTaskVideoMonitorReport = new TimerTask() { // zhaohua
																		// add
																		// on
																		// 20140708
		@Override
		public void run() {
			if (serviceAV.getMediaSession() != null
					&& SystemVarTools.mTimerVideoMonitorReport != null) {
				serviceAV.sendMonitorReportAliveInfoMsg();
			}
		}
	};

	private final TimerTask mTimerTaskSuicide = new TimerTask() {
		@Override
		public void run() {
			ScreenMediaAV.this.runOnUiThread(new Runnable() {
				public void run() {
					IBaseScreen currentScreen = mScreenService
							.getCurrentScreen();
					// currentScreen.getId() == getId() ??
					// com.sunkaisens.skdroid.Screens.ScreenTabHome == 27
					boolean gotoHome = (currentScreen != null && currentScreen
							.getId() == getId());
					if (gotoHome) {
						back();
						// mScreenService.show(ScreenMyHome.class);
					}
					mScreenService.destroy(getId());
				}
			});
		}
	};

	private void startVideo(boolean bStart, boolean bZOrderTop) {
		Log.d("zhangjie:startVideo()", "startVideo()");
		Log.d(TAG, "startStopVideo(" + bStart + ")");
		if (!mIsVideoCall) {
			Log.d(TAG, "startStopVideo(0000000000000000)");
			return;
		}

		// serviceAV.getMediaSession().setSendingVideo(bStart);

		if (mViewLocalVideoPreview != null) {
			if (bStart) {
				mViewLocalVideoPreview.removeAllViews();
				// cancelBlankPacket();
				final View localPreview = serviceAV.getMediaSession()
						.startVideoProducerPreview();
				if (localPreview != null) {
					final ViewParent viewParent = localPreview.getParent();
					if (viewParent != null && viewParent instanceof ViewGroup) {
						((ViewGroup) (viewParent)).removeView(localPreview);
					}
					if (bZOrderTop == true) {
						if (localPreview instanceof SurfaceView) {
							((SurfaceView) localPreview).setZOrderOnTop(true);
						}
					}
					mViewLocalVideoPreview.addView(localPreview);
					mViewLocalVideoPreview.bringChildToFront(localPreview);
				}
			}
			// mViewLocalVideoPreview.setVisibility(bStart ? View.VISIBLE
			// : View.GONE);
			mViewLocalVideoPreview.setVisibility(View.VISIBLE);
			mViewLocalVideoPreview.bringToFront();
		}
	}

	/**
	 * MyProxSensor
	 */
	static class MyProxSensor implements SensorEventListener {
		private final SensorManager mSensorManager;
		private Sensor mSensor;
		private final ScreenMediaAV mAVScreen;
		private float mMaxRange;

		MyProxSensor(ScreenMediaAV avScreen) {
			mAVScreen = avScreen;
			mSensorManager = SKDroid.getSensorManager();
		}

		void start() {
			if (mSensorManager != null && mSensor == null) {
				if ((mSensor = mSensorManager
						.getDefaultSensor(Sensor.TYPE_PROXIMITY)) != null) {
					mMaxRange = mSensor.getMaximumRange();
					mSensorManager.registerListener(this, mSensor,
							SensorManager.SENSOR_DELAY_UI);
				}
			}
		}

		void stop() {
			if (mSensorManager != null && mSensor != null) {
				mSensorManager.unregisterListener(this);
				mSensor = null;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			try { // Keep it until we get a phone supporting this feature
				if (mAVScreen == null) {
					Log.e(ScreenMediaAV.TAG, "invalid state");
					return;
				}

				if (event.values != null && event.values.length > 0) {
					MyLog.d(TAG, "SensorValue distance:" + event.values[0]);
					if (event.values[0] == 0.0) {
						// Log.d(TAG, "reenableKeyguard()");
						// mAVScreen.loadProxSensorView();
						if (mAVScreen.frameAbove != null) {
							mAVScreen.frameAbove.setVisibility(View.VISIBLE);
						}
					} else {
						// Log.d(TAG, "disableKeyguard()");
						if (mAVScreen.frameAbove != null) {
							mAVScreen.frameAbove.setVisibility(View.GONE);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "ScreanMediaAV onRestart!!!");
		super.onRestart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		Log.d(TAG, "ScreanMediaAV onResoreInstanceState exec");

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		Log.d(TAG, "ScreanMediaAV onSaveInstanceState exec");

		super.onSaveInstanceState(outState);
	}

	private void changeFragmmentSigns(boolean sign) {
		isGroupVideoFragmentAdded = sign;
		isSingleVideoFragmentAdded = sign;
		isGroupAudioFragmentAdded = sign;
		isSingleAudioFragmentAdded = sign;
		isVideoMonitorFragmentAdded = sign;
	}

}
