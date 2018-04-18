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

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Services.ServiceFragment;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.encryptcall.EncryptProcess;
import com.sunkaisens.skdroid.fragments.AVGroupAudioFragment;
import com.sunkaisens.skdroid.fragments.AVGroupVideoFragment;
import com.sunkaisens.skdroid.fragments.AVSingleAudioFragment;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.groupcall.PTTActionTypes;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.listener.HeadsetListener;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenAV extends BaseScreen implements SensorEventListener {
	private static final String TAG = ScreenAV.class.getCanonicalName();

	private boolean mRotationSign = false;
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

	public boolean mIsVideoCall;
	public boolean mIsAudioCall;

	private TextView mTvInfo;
	private GroupPTTCall mPttCall;

	private KeyguardLock mKeyguardLock;
	private OrientationEventListener mListener;

	private static boolean SHOW_SIP_PHRASE = true;

	// wangds add,service av object,2014.5.24
	ServiceAV serviceAV = null;

	private static int mSessionType;

	private ModelContact mModelContact;

	final int MSG_IS_VIDEO_REFRESH = 1000;
	final int PTTENABLE = 7001;
	final int PTTNOTENABLE = 7002;

	private BroadcastReceiver mHeadsetReceiver;

	private boolean isSuicide = false;

	private static boolean isScreenChange = false;

	public static boolean ispeoplePTT = false;

	private AVGroupVideoFragment AVGroupVideo;
	private AVGroupAudioFragment AVGroupAudio;
	private AVSingleAudioFragment AVSingleAudio;

	private HeadsetListener mHeadsetListener;

	private final static String UNLOCK_SCREEN_ACTION_DT = "com.datang.unlockedScreen";

	// 调用距离传感器，控制屏幕 ywh
	private SensorManager mManager;// 传感器管理对象
	// 屏幕开关
	private PowerManager localPowerManager = null;// 电源管理对象
	private PowerManager.WakeLock localWakeLock = null;// 电源锁

	private static enum ViewType {
		ViewNone, ViewTrying, ViewInCall, ViewProxSensor, ViewTermwait
	}

	public ScreenAV() {
		super(SCREEN_TYPE.AV_T, TAG);

		MyLog.d(TAG, "ScreenAV create");

		mCurrentView = ViewType.ViewNone;

		mPttCall = new GroupPTTCall();

		isSuicide = false;
		MyLog.d(TAG, "ScreenAV create OK.");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_av);

		Log.d(TAG, "ScreenAV alive:: onCreate()");

		// ywh
		mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// 获取系统服务POWER_SERVICE，返回一个PowerManager对象
		localPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是LogCat里用的Tag
		localWakeLock = this.localPowerManager.newWakeLock(32, "MyPower");// 第一个参数为电源锁级别，第二个是日志tag

		super.mId = getIntent().getStringExtra("id"); // java中子类可以直接使用父类成员//获取屏的ID实际上也是AVSession，SipSesion的ID
		if (NgnStringUtils.isNullOrEmpty(super.mId)) {
			Log.e(TAG, "ScreenAV oncreate:: Invalid audio/video session");
			finish();
			return;
		}
		NgnAVSession avSession = NgnAVSession.getSession(NgnStringUtils
				.parseLong( // 将string型的mId转为long型，若mId为空，则返回-1
						super.mId, -1)); // 通过ID,从静态的NgnAVSession集合中取出NgnAVSession
		if (avSession == null) {
			Log.e(TAG,
					String.format(
							"ScreenAV oncreate:: Cannot find audio/video session with id=%s",
							super.mId));
			finish();
			return;
		}
		// wangds add,2014.5.24
		serviceAV = ServiceAV.create(avSession, this);
		serviceAV.registerReceiver();
		MyLog.d(TAG, "serviceAV create and registe ok.");

		mModelContact = SystemVarTools.createContactFromRemoteParty(serviceAV
				.getAVSession().getRemotePartyUri());
		mRemotePartyDisplayName = mModelContact.name; // 通话对象的显示名称

		if (mRemotePartyDisplayName.contains("%23")) {
			mRemotePartyDisplayName = mRemotePartyDisplayName.replace("%23",
					"#");
		}
		MyLog.d(TAG, "RemoteParty is " + mRemotePartyDisplayName);
		imageid = mModelContact.imageid;

		mIsVideoCall = serviceAV.getAVSession().getMediaType() == NgnMediaType.AudioVideo // 获取通话类型：Audio
																							// 带声音的为audiovideo，不带声音的为video，比如视频回传
																							// or
																							// Video
				|| serviceAV.getAVSession().getMediaType() == NgnMediaType.Video;
		mIsAudioCall = serviceAV.getAVSession().getMediaType() == NgnMediaType.Audio;

		mSendDeviceInfo = getEngine().getConfigurationService().getBoolean(
				// ???
				NgnConfigurationEntry.GENERAL_SEND_DEVICE_INFO,
				NgnConfigurationEntry.DEFAULT_GENERAL_SEND_DEVICE_INFO);
		mLastRotation = -1;
		mLastOrientation = -1;

		mInflater = LayoutInflater.from(this); // ??

		mMainLayout = (RelativeLayout) findViewById(R.id.screen_av_relativeLayout);
		frameAbove = (FrameLayout) findViewById(R.id.frameAbove);

		if (serviceAV.getAVSession().isGroupAudioCall()
				|| serviceAV.getAVSession().isGroupVideoCall()) {
			Log.d(TAG, "ScreenAV onCreate:: loadGroupView()");
			loadGroupView();
		} else {
			Log.d(TAG, "ScreenAV onCreate:: loadView()");
			loadView();
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC); // 设置设备的音量控制的硬件按钮控制的音频流为STREAM_VOICE_CALL类型的流

		isSuicide = false;

		// createHeadsetReceiver(TAG);

		Log.d(TAG, "ScreenAV alive:: onCreate  OK");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "ScreenAV alive:: onStart()");

		final KeyguardManager keyguardManager = SKDroid.getKeyguardManager();
		if (keyguardManager != null) {
			if (mKeyguardLock == null) {
				mKeyguardLock = keyguardManager.newKeyguardLock(ScreenAV.TAG);
			}
			if (keyguardManager.inKeyguardRestrictedInputMode()) {
				mKeyguardLock.disableKeyguard();
			}
		}

		if (NgnApplication.isl8848a_l1860() || NgnApplication.isBh()) {
			unlock_datang();
		}

		Log.d(TAG, "ScreenAV alive:: onStart  OK");
	}

	@Override
	protected void onPause() {
		super.onPause();

		// ywh 通话页面置于后台，取消禁止屏幕熄灭
		keepScreenOn(this, false);
		Log.d(TAG, "ScreenAV alive:: onPause()");

		if (mListener != null && mListener.canDetectOrientation()) {
			mListener.disable();
		}

		GlobalVar.bBackOrSwitch = true;
		Log.d(TAG, "ScreenAV alive:: onPause  OK");
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.d(TAG, "ScreenAV alive:: onResume()");

		// ywh 通话页面准备好后，禁止屏幕熄灭
		keepScreenOn(this, true);

		// ywh
		// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延迟类型
		if (mIsAudioCall && !serviceAV.getAVSession().isGroupAudioCall()) {
			mManager.registerListener(this,
					mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),// 距离感应器
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		if (mListener != null && mListener.canDetectOrientation()) {
			mListener.enable();
		}

		Log.d(TAG, "ScreenAV mIsVideoCall:" + mIsVideoCall);
		if (serviceAV != null && serviceAV.getAVSession() != null
				&& serviceAV.getAVSession().getState() == InviteState.INCALL) {
			if (mIsVideoCall) {
				changeFragmmentSigns(false);
				if (serviceAV.getAVSession().isVideoMonitorCall()) {
					loadInCallVideoMonitorView();

				} else if (serviceAV.getAVSession().isGroupVideoCall()) {
					loadGroupInCallVideoView();

				} else {
					loadInCallVideoView();
				}
			}
		}

		GlobalVar.bBackOrSwitch = false;
		Log.d(TAG, "ScreenAV alive:: onResume  OK");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "ScreenAV alive:: onStop()");

		if (mKeyguardLock != null) {
			mKeyguardLock.reenableKeyguard();
		}

		Main.isFirstPTT_onKeyDown = true;
		Main.isFirstPTT_onKeyLongPress = true;

	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "ScreenAV alive:: onDestroy()");
		// mTimerSuicide.cancel();
		stopTimerAndTask();
		// cancelBlankPacket();
		if (serviceAV != null) {
			serviceAV.unRegisterReceiver();
			serviceAV.release();
		}
		// if(mHeadsetReceiver != null){
		// unregisterReceiver(mHeadsetReceiver);
		// Log.i(TAG, "取消注册耳机状态接收器");
		// }
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {

		Log.d(TAG, "ScreenAV alive:: onNewIntent");
		changeFragmmentSigns(false);
		if (serviceAV.getAVSession().isGroupAudioCall()) {
			Log.d(TAG, "onNewIntent call Type: groupAudioCall");

		} else if (serviceAV.getAVSession().isGroupVideoCall()) {
			Log.d(TAG, "onNewIntent call Type: groupVideoCall");
			if (serviceAV.getAVSession().isConnected()) {
				loadGroupInCallVideoView();
			} else {
				loadGroupTryingView();
			}

		} else if (mIsVideoCall) {
			Log.d(TAG, "call Type: VideoCall");
			if (serviceAV.getAVSession().isConnected()) {
				loadInCallVideoView();
			} else {
				loadTryingView();
			}
		} else {
			Log.d(TAG, "call Type: audioCall");
		}
		super.onNewIntent(intent);
	}

	public void createHeadsetReceiver(final String tag) {
		if (mHeadsetReceiver != null) {
			return;
		}
		mHeadsetReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				int state = intent.getIntExtra("state", 2);
				Log.d(tag, "Headset state = " + state);
				if (NgnAVSession.getSize() == 0) {
					return;
				}
				NgnAVSession session = NgnAVSession.getSessions().getAt(0);
				if (session == null) {
					return;
				}

				Object obj = getFragmentManager().findFragmentById(
						R.id.screen_av_relativeLayout);
				if (obj != null && obj instanceof HeadsetListener) {
					mHeadsetListener = (HeadsetListener) obj;
				} else {
					return;
				}

				if (state == 0) { // 拔出耳机
					session.setSpeakerphoneOn(true);
					mHeadsetListener.headsetOff();
				} else if (state == 1) { // 插入耳机
					session.setSpeakerphoneOn(false);
					mHeadsetListener.headsetOn();
				}
			}
		};
		IntentFilter headsetFilter = new IntentFilter();
		headsetFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		SKDroid.getContext().registerReceiver(mHeadsetReceiver, headsetFilter);
		Log.i(tag, "注册耳机状态接收器");
	}

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
		if (serviceAV.getAVSession() != null) {
			return serviceAV.getAVSession().onVolumeChanged(bDown);
		}
		return false;
	}

	public void handleMediaEvent(Intent intent) {
		MyLog.d(TAG, "handleMediaEvent()");
		final String action = intent.getAction();

		if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(action)) {
			NgnMediaPluginEventArgs args = intent
					.getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}

			Log.d(TAG, "eventType:" + args.getEventType());

			switch (args.getEventType()) {
			case STARTED_OK: // started or restarted (e.g. reINVITE)
			{
				mIsVideoCall = (serviceAV.getAVSession().getMediaType() == NgnMediaType.AudioVideo || serviceAV
						.getAVSession().getMediaType() == NgnMediaType.Video);
				Log.d(TAG, "mMediaType: "
						+ serviceAV.getAVSession().getMediaType());

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
		MyLog.d(TAG, "handleMediaEvent()  OVER");
	}

	public void handleSipEvent(Intent intent) {

		Log.d(TAG, "handleSipEvent()");

		InviteState state;
		if (serviceAV.getAVSession() == null) {
			Log.e(TAG, "Invalid session object");
			return;
		}

		final String action = intent.getAction();
		Log.d(TAG, "Action = " + action);
		if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
			NgnInviteEventArgs args = intent
					.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args(args is null).");
				return;
			}
			if (args.getSessionId() != serviceAV.getAVSession().getId()) {
				Log.d(TAG, "Receive a call,handling.1111");
				return;
			}

			NgnInviteEventTypes eventType = args.getEventType(); // CONNECTED
																	// TERMWAIT
			Log.d(TAG, "EventType = " + eventType);
			if (eventType.equals(NgnInviteEventTypes.ENCRYPT_INFO)) {
				switch ((state = serviceAV.getAVSession().getState())) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					MyLog.d(TAG,
							"state = INCOMING or INPROGRESS or REMOTE_RINGING ");
					// Log.d(TAG,
					// "receive callstate is "+mAVSession.getState());
					loadTryingView();
					handleEncryptInfoMsg(serviceAV.getAVSession()
							.getmInfoContent());// 被叫流程
					break;
				case EARLY_MEDIA:

					handleEncryptInfoMsg(serviceAV.getAVSession()
							.getmInfoContent());// 主叫流程
				}
			} else if (eventType.equals(NgnInviteEventTypes.PTT_INFO_REQUEST)) {// GROUP_PTT_INFO))
																				// {
																				// //包含所有与PTT_INFO相关的事件,eg:组呼、云台、伴音回传
				long CseqNum = intent.getLongExtra(
						NgnInviteEventArgs.EXTRA_CSEQ, 0);
				Log.d(TAG, "当前CSeq=" + serviceAV.getmCurrentInfoCseq()
						+ "  新的CSeq=" + CseqNum);
				if (serviceAV.getmCurrentInfoCseq() > CseqNum) {
					return;
				} else {
					serviceAV.setmCurrentInfoCseq(CseqNum);
				}
				state = serviceAV.getAVSession().getState();
				switch (state) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
				case EARLY_MEDIA:
					// Log.d(TAG,
					// "receive callstate is "+mAVSession.getState());
					loadGroupTryingView();
					break;

				case INCALL:
					MyLog.d(TAG, "AVGroupVideo:" + AVGroupVideo
							+ " | AVGroupAudio:" + AVGroupAudio);
					if (AVGroupVideo != null) {
						AVGroupVideo.handleRequestPTTInfoMsg(args
								.getmInfoContent());
					}
					if (AVGroupAudio != null) {
						AVGroupAudio.handleRequestPTTInfoMsg(args
								.getmInfoContent());
					}
					if (AVSingleAudio != null) {
						AVSingleAudio.handlerPTTinfo(args.getmInfoContent(),
								args.getSessionId());
					}
					handleGetAudioInfoMsg(args.getmInfoContent());

				}
			} else if ((eventType.equals(NgnInviteEventTypes.GROUP_VIDEO_MONITORING))) {
				switch ((state = serviceAV.getAVSession().getState())) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
				case EARLY_MEDIA:
				case INCALL:
					startVideo(true, true);
					break;

				}

			} else if ((eventType.equals(NgnInviteEventTypes.VIEDO_TRANSMINT))) {
				switch ((state = serviceAV.getAVSession().getState())) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
				case EARLY_MEDIA:
					// Log.d(TAG,
					// "receive callstate is "+mAVSession.getState());
					loadTryingView();
					break;
				case INCALL:
					startVideo(true, true);
					loadInCallVideoView();
				}
			} else {
				Log.d(TAG, "会话状态 = " + serviceAV.getAVSession().getState());
				switch ((state = serviceAV.getAVSession().getState())) { // TERMINATED
				case NONE:
				default:
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					loadTryingView();

					// if (serviceAV.getAVSession() != null) { //INPROGRESS
					// 正在语音呼叫
					// if (!mIsVideoCall) {
					// mTimerTrying.schedule(mTimerTaskTrying, 0, 1000);
					// }
					// }

					break;

				case EARLY_MEDIA:
				case INCALL:

					// serviceAV.setOnStart();
					if(serviceAV.getAVSession().isGroupAudioCall() || serviceAV.getAVSession().isGroupVideoCall() ||
							serviceAV.getAVSession().isVideoMonitorCall()||serviceAV.getAVSession().isMicrophoneMute()){
						serviceAV.setOnPause(true);
						serviceAV.getAVSession().setGroupAudioTimerStart(true);
						serviceAV.setOnResetJB();
					}
					else{
						serviceAV.setOnPause(false);
						serviceAV.getAVSession().setmSendVIdeo(true);
					}
					// getEngine().getSoundService().stopRingTone();
					
					serviceAV.getAVSession().setSpeakerphoneOn(true);
					if (state == InviteState.INCALL) {
						serviceAV.setOnResetJB();
						// 180SDP两次媒体协商，重新取会话类型 gle20141222
						mIsVideoCall = (serviceAV.getAVSession().getMediaType() == NgnMediaType.AudioVideo || serviceAV
								.getAVSession().getMediaType() == NgnMediaType.Video);
						// Send blank packets to open NAT pinhole
						if (serviceAV.getAVSession() != null) {
							serviceAV.applyCamRotation(serviceAV.getAVSession()
									.compensCamRotation(true));
							for (int i = 0; i < 5; ++i) {
								if (serviceAV.getAVSession() != null) {
									serviceAV.getAVSession().pushBlankPacket();
									MyLog.d(TAG, "发送空包:" + i);
								}
							}

						}

						// release power lock if not video call
						// if (!mIsVideoCall && mWakeLock != null
						// && mWakeLock.isHeld()) {
						// mWakeLock.release();
						// }

						Log.d(TAG, "args.getEventType():" + args.getEventType());

						switch (args.getEventType()) {
						case REMOTE_DEVICE_INFO_CHANGED:
							Log.d(TAG,
									String.format(
											"Remote device info changed: orientation: %s",
											serviceAV.getAVSession()
													.getRemoteDeviceInfo()
													.getOrientation()));
							break;
						case CONNECTED:
							if (serviceAV.getAVSession().isGroupAudioCall()) {
								Log.d(TAG, "call Type: groupAudioCall");
								// mTimerGroupInCall.schedule(mTimerTaskGroupInCall,
								// 0, 1000);
								loadGroupInCallAudioView();
								// serviceAV.setOnPause(true);
								serviceAV.sendPTTInquireInfoMsg();
							} else if (serviceAV.getAVSession().isGroupVideoCall()) {
								Log.d(TAG, "call Type: groupVideoCall");
								// mTimerGroupInCall.schedule(mTimerTaskGroupInCall,
								// 0, 1000);
								serviceAV.getAVSession().setmSendVIdeo(false);
								loadGroupInCallVideoView();
								// serviceAV.setOnPause(true);
								serviceAV.sendPTTInquireInfoMsg();
							} else if (serviceAV.getAVSession().isVideoMonitorCall()) {
								loadInCallVideoMonitorView();
								serviceAV.getAVSession().setmSendVIdeo(true);
								serviceAV.setOnPause(true);

							} else if (mIsVideoCall) {
								Log.d(TAG, "call Type: VideoCall");
								// mTimerVideoDuraton.schedule(mTimerTaskVideoDuraton,
								// 0, 1000);
								loadInCallVideoView();
							} else {
								Log.d(TAG, "call Type: audioCall");
								/*
								 * mTimerInCall.schedule(mTimerTaskInCall, 0,
								 * 1000); mTimerInCallIsStart = true;
								 */
								loadInCallAudioView();
							}
							break;
						}
					}
					break;

				case TERMINATING:
				case TERMINATED: // Call Terminated
					Log.d(TAG, "InviteEvent TERMINATED%%%%%");

					ispeoplePTT = false;
					Log.e(TAG, "TERMINATED:ispeoplePTT change to false");

					AVGroupVideoFragment.mPttCall = null;
					serviceAV.setmCurrentInfoCseq(0);//
					// if (isSuicide == false) {
					// mTimerSuicide.schedule(mTimerTaskSuicide, new Date(
					// new Date().getTime() + 1500));
					// Log.d(TAG, "InviteEvent mTimerSuicide.schedule");
					// isSuicide = true;
					// }

					// mTimerTaskInCall.cancel();
					// mTimerInCall.cancel();
					// mTimerBlankPacket.cancel();
					stopTimerAndTask();

					clearFragments();

					Log.d(TAG, "InviteEvent stopTimerAndTask");
					loadTermView(SHOW_SIP_PHRASE ? args.getPhrase() : null);
					Log.d(TAG, "handleSipEvent  loadTermView("
							+ (SHOW_SIP_PHRASE ? args.getPhrase() : null) + ")");

					if (NgnApplication.isBh()) { // 正样PAD、手持台
						final AudioManager audiomanager = NgnApplication
								.getAudioManager();
						audiomanager.setMode(AudioManager.MODE_NORMAL);
						Log.d(TAG,
								"audiomanager.setMode(AudioManager.MODE_NORMAL); - bh03/bh04");
					}

					// if (mModelContact != null
					// &&
					// (mModelContact.mobileNo.startsWith("05013")||mModelContact.mobileNo.startsWith("02013"))
					// && mScreenService != null&&
					// mScreenService.getCurrentScreen() != null){
					// if (getResources().getConfiguration().orientation ==
					// Configuration.ORIENTATION_LANDSCAPE) { //横屏
					// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					// //竖屏
					// Log.d(TAG, "portrait");
					// }
					// mScreenService.show(ScreenTabHome.class);
					// }

					break;
				}
			}
		}
	}

	private void clearFragments() {
		if (AVSingleAudio != null) {
			AVSingleAudio = null;
		}
		if (AVGroupAudio != null) {
			AVGroupAudio = null;
		}
		if (AVGroupVideo != null) {
			AVGroupVideo = null;
		}
	}

	/**
	 * 处理伴音回传INFO请求
	 * 
	 * @param infoContent
	 */
	public void handleGetAudioInfoMsg(byte[] infoContent) {
		Log.d(TAG, "handleGetAudioInfoMsg()");

		if (mPttCall == null)
			mPttCall = new GroupPTTCall();
		PTTInfoMsg msg = new PTTInfoMsg(infoContent);
		mPttCall.handlePTTInfoMsg(msg);
		Engine.getInstance();

		switch (mPttCall.getState()) {
		case GET_AUDIO: // 伴音回传
			String pttAction = msg.getPTTAction();
			final AudioManager audiomanager = NgnApplication.getAudioManager();
			audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
			Log.d(TAG,
					String.format(
							"PTTAction: %s. SetMode(AudioManager.MODE_IN_COMMUNICATION)",
							pttAction));
			if (pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_DISABLE)) {
				Log.d(TAG,
						"PTTAction: PTTActionTypes.PTT_ACT_AUDIO_DISABLE && lc_audio_record=0 && onPause=true");
				audiomanager.setParameters("lc_audio_record=0");
				serviceAV.setOnPause(true);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_ANALOG)) {
				Log.d(TAG,
						"PTTAction: PTTActionTypes.PTT_ACT_AUDIO_ANALOG && lc_audio_record=1 && onPause=false");
				audiomanager.setParameters("lc_audio_record=1");
				serviceAV.setOnPause(false);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_HD)) {
				Log.d(TAG,
						"PTTAction: PTTActionTypes.PTT_ACT_AUDIO_HD && lc_audio_record=2 && onPause=false");
				audiomanager.setParameters("lc_audio_record=2");
				serviceAV.setOnPause(false);
			}

			break;

		default:
			break;
		}

	}

	public void loadView() {
		MyLog.d(TAG, "loadView()");

		switch (serviceAV.getAVSession().getState()) { // INPROGRESS
		case INCOMING:
		case INPROGRESS:
		case REMOTE_RINGING:
		case EARLY_MEDIA:
			// add by Gongle
			// 针对调度台发现的视频回传（称之为被动视频回传），终端直接接听
			if (serviceAV.getAVSession().getSessionType() == SessionType.VideoSurveilMonitor) {
				Log.d(TAG, String.format("SessionType= VideoPassiveMonitor %d",
						serviceAV.getAVSession().getSessionType()));
				serviceAV.acceptCall();
				// loadInCallVideoMonitorView();
			} else
				loadTryingView();
			break;

		case INCALL:
			if (serviceAV.getAVSession().isGroupAudioCall()) {
				MyLog.d(TAG, "call Type: groupAudioCall");
				loadGroupInCallAudioView();
				serviceAV.sendPTTInquireInfoMsg();

			} else if (serviceAV.getAVSession().isGroupVideoCall()) {
				MyLog.d(TAG, "call Type: groupVideoCall");
				serviceAV.getAVSession().setmSendVIdeo(false);
				loadGroupInCallVideoView();
				serviceAV.sendPTTInquireInfoMsg();

			} else if (serviceAV.getAVSession().isVideoMonitorCall()) {
				MyLog.d(TAG, "call Type: isVideoMonitorCall");
				loadInCallVideoMonitorView();
				serviceAV.getAVSession().setmSendVIdeo(true);

			} else if (mIsVideoCall) {
				MyLog.d(TAG, "call Type: VideoCall");
				serviceAV.getAVSession().setmSendVIdeo(true);
				loadInCallVideoView();

			} else {
				MyLog.d(TAG, "call Type: audioCall");
				loadInCallAudioView();

			}
			// loadInCallView();

			break;

		case NONE:
		case TERMINATING:
		case TERMINATED:
		default:
			Log.d(TAG, "loadView loadTermView()");
			loadTermView();
			break;
		}
	}

	private void loadGroupView() { // duhaitao 添加
		switch (serviceAV.getAVSession().getState()) {
		case INCOMING:
		case INPROGRESS:
		case REMOTE_RINGING:
		case EARLY_MEDIA:
			loadGroupTryingView();
			break;

		case INCALL:
			loadGroupInCallView();
			break;

		case NONE:
		case TERMINATING:
		case TERMINATED:
		default:
			Log.d(TAG, "loadGroupView loadTermView()");
			loadTermView();
			break;
		}

	}

	private boolean isTryingFragmentAdded1 = false;
	private boolean isTryingFragmentAdded2 = false;
	private boolean isTryingFragmentAdded3 = false;
	private boolean isInCommingFragmentAdded = false;

	/** 正在呼叫时的View */
	public void loadTryingView() {
		Log.d(TAG, "loadTryingView");
		if (mCurrentView == ViewType.ViewTrying) {
			MyLog.d(TAG, "Current View is trying view.");
			return;
		}

		mCurrentView = ViewType.ViewTrying;

		mSessionType = serviceAV.getAVSession().getSessionType();

		switch (serviceAV.getAVSession().getState()) {
		case INCOMING:
			Log.d(TAG, "INCOMING   SessionType=" + mSessionType);

			switch (mSessionType) {
			case SessionType.AudioCall:
			case SessionType.VideoCall:
			case SessionType.VideoTransmit:

			case SessionType.VideoMonitor:
			case SessionType.GroupVideoMonitor:

			case SessionType.VideoSurveilMonitor:
				MyLog.d(TAG, "isInCommingFragmentAdded ? "
						+ isInCommingFragmentAdded);
				if (!isInCommingFragmentAdded) {
					ServiceFragment.makeAvSingleIncomingFragment(this,
							serviceAV, mMainLayout);
					isInCommingFragmentAdded = true;
				}
				break;

			default:
				mTvInfo.setText(getString(R.string.string_call_incoming)); // 来电呼入

				break;

			}

			break;

		case INPROGRESS:
		case REMOTE_RINGING:
		case EARLY_MEDIA:
		default:
			Log.d(TAG, "outgoing  SessionType=" + mSessionType);
			switch (mSessionType) {
			case SessionType.AudioCall:
				if (!isTryingFragmentAdded1) {
					ServiceFragment.makeAvSingleAudioTryingFragment(this,
							serviceAV, mMainLayout);
					isTryingFragmentAdded1 = true;
				}
				break;

			case SessionType.VideoUaMonitor:
			case SessionType.VideoCall:

				if (!isTryingFragmentAdded2) {
					ServiceFragment.makeAvSingleVideoTryingFragment(this,
							serviceAV, mMainLayout); // 正在视频呼叫
					isTryingFragmentAdded2 = true;
				}
				break;
			case SessionType.GroupVideoCall:
				if (!isTryingFragmentAdded3) {
					ServiceFragment.makeAvGroupVideoTryingFragment(this,
							serviceAV, mMainLayout);
					isTryingFragmentAdded3 = true;
				}

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
		mSessionType = serviceAV.getAVSession().getSessionType();
		MyLog.d(TAG, "组呼  trying 呼叫类型:" + mSessionType + "  状态:"
				+ serviceAV.getAVSession().getState());
		switch (serviceAV.getAVSession().getState()) {
		case INCOMING:

			serviceAV.acceptCall();

		case INPROGRESS:
		case REMOTE_RINGING:
		case EARLY_MEDIA:
		default:

			switch (mSessionType) {
			case SessionType.GroupAudioCall:
				MyLog.d(TAG, "语音组呼 呼叫。。  isGroupAudioFragmentAdded："
						+ isGroupAudioFragmentAdded);
				// 正在语音组呼
				// if (!isGroupAudioFragmentAdded) {
				AVGroupAudio = ServiceFragment.makeAvGroupAudioFragment(this,
						serviceAV, mMainLayout);
				// isGroupAudioFragmentAdded = true;
				AVGroupVideo = null;
				// }

				break;

			case SessionType.GroupVideoCall:
				ServiceFragment.makeAvGroupVideoTryingFragment(this, serviceAV,
						mMainLayout); // 正在视频组呼

				break;

			default:

				break;
			}
		}
		serviceAV.setOnPause(true);
		serviceAV.getAVSession().setSpeakerphoneOn(true); // 默认打开扬声器
	}

	private void loadGroupInCallView() { // //duhaitao 添加
		Log.d(TAG, "loadGroupInCallView()");
		if (mCurrentView == ViewType.ViewInCall) {
			return;
		}
		Log.d(TAG, "loadGroupInCallView()");

		// loadGroupInCallAudioView();
		if (mIsVideoCall && serviceAV.getAVSession().isGroupVideoCall()) {
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
			isGroupAudioFragmentAdded = false;
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
			AVSingleAudio = ServiceFragment.makeAvSingleAudioFragment(this,
					serviceAV, mMainLayout);
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
		Log.d(TAG, "loadInCallVideoMonitorView()   标记:"
				+ isVideoMonitorFragmentAdded);

		if (!isVideoMonitorFragmentAdded) {
			ServiceFragment.makeAvVideoMonitorFragment(this, serviceAV,
					mMainLayout);
			isVideoMonitorFragmentAdded = true;
		}

	}

	private void loadInCallView() {
		Log.d(TAG, "loadInCallView()");
		if (mCurrentView == ViewType.ViewInCall) {
			return;
		}
		// Log.d(TAG, "loadInCallView()");

		if (mIsVideoCall) {
			Log.d(TAG, "IsVideoCall");
			// Log.d(TAG,
			// "ScreenAV mModelContact.mobileNo:"+mModelContact.mobileNo);
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
			mInflater = LayoutInflater.from(this);
			mViewProxSensor = mInflater.inflate(R.layout.view_call_proxsensor,
					null);
		}
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewProxSensor);
		mCurrentView = ViewType.ViewProxSensor;
	}

	private void loadTermView(String phrase) {
		Log.d(TAG, "loadTermView()");

		// ywh
		if (mManager != null) {
			if (localWakeLock != null && localWakeLock.isHeld()) {
				localWakeLock.release();// 释放电源锁，如果不释放finish这个activity后任然会有自动锁屏的效果
			}
			mManager.unregisterListener(this);// 注销传感器监听
		}

		GlobalVar.bBackOrSwitch = false;

		if (serviceAV != null && serviceAV.getAVSession() != null
				&& serviceAV.getAVSession().getmVideoProducer() != null) {
			
		}

		if (mViewTermwait == null) {
			mInflater = LayoutInflater.from(this);
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
				switch (serviceAV.getAVSession().getSessionType()) {
				case SessionType.VideoUaMonitor:
					mTvInfo.setText(getString(R.string.string_call_unmonitor_terminated)); // 主动视频回传已终止
					break;
				case SessionType.VideoMonitor:
					mTvInfo.setText(getString(R.string.string_call_monitor_video_terminated)); // 视频监控已终止
					break;
				case SessionType.GroupVideoCall:
					mTvInfo.setText(getString(R.string.string_call_group_video_terminated)); // 视频组呼已终止
					break;
				case SessionType.GroupVideoMonitor:
					mTvInfo.setText(getString(R.string.string_monitor_terminated)); // 监控已终止
					break;
				case SessionType.VideoTransmit:
					mTvInfo.setText(getString(R.string.string_transmit_terminated)); // 转发已终止
					break;
				default:
					mTvInfo.setText(getString(R.string.string_call_terminated)); // 通话已终止
					break;
				}
			} else if (phrase.equals("Call Cancelled")) {
				// mTvInfo.setText(getString(R.string.string_call_cancelled));
				// //通话已取消
				Log.d(TAG, "loadTermView deal with call cancelled...");
				switch (serviceAV.getAVSession().getSessionType()) {
				case SessionType.VideoUaMonitor:
					mTvInfo.setText(getString(R.string.string_call_unmonitor_cancelled)); // 主动视频回传已取消
					break;
				case SessionType.VideoMonitor:
					mTvInfo.setText(getString(R.string.string_call_monitor_video_cancelled)); // 视频监控已取消
					break;
				case SessionType.GroupVideoCall:
					mTvInfo.setText(getString(R.string.string_call_group_video_cancelled)); // 视频组呼已取消
					break;
				case SessionType.GroupVideoMonitor:
					mTvInfo.setText(getString(R.string.string_monitor_cancelled)); // 监控已取消
					break;
				case SessionType.VideoTransmit:
					mTvInfo.setText(getString(R.string.string_transmit_cancelled)); // 转发已取消
					break;
				default:
					mTvInfo.setText(getString(R.string.string_call_cancelled)); // 通话已取消
					break;
				}
			} else if (phrase.equals("Busy Here")) {
				Log.d(TAG, "loadTermView deal with busy here...");
				mTvInfo.setText(getString(R.string.string_busy_here)); // 对方通话中
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

		// if (isScreenChange
		// && mScreenService != null&& mScreenService.getCurrentScreen() !=
		// null) {
		// if (getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_LANDSCAPE) { //横屏
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// //竖屏
		// Log.d(TAG, "portrait");
		// GlobalVar.isLandscap = false;
		// isScreenChange = false;
		// }
		// }
		// Log.d(TAG, "loadTermView usertype check over");
		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		Log.d(TAG, "loadTermView prepare changing to ScreenTabHome");

		IBaseScreen baseScreen = mScreenService.getCurrentScreen();
		if (baseScreen != null && baseScreen instanceof ScreenAV) {
			// mScreenService.show(ScreenTabHome.class);
			back();
		}
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
			if (serviceAV.getAVSession() != null
					&& SystemVarTools.mTimerVideoMonitorReport != null) {
				serviceAV.sendMonitorReportAliveInfoMsg();
			}
		}
	};

	private final TimerTask mTimerTaskSuicide = new TimerTask() {
		@Override
		public void run() {
			ScreenAV.this.runOnUiThread(new Runnable() {
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

		// serviceAV.getAVSession().setSendingVideo(bStart);

		if (mViewLocalVideoPreview != null) {
			if (bStart) {
				mViewLocalVideoPreview.removeAllViews();
				// cancelBlankPacket();
				final View localPreview = serviceAV.getAVSession()
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

	@Override
	protected void onRestart() {
		Log.d(TAG, "ScreenAV onRestart!!!");
		super.onRestart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		Log.d(TAG, "ScreenAV onResoreInstanceState exec");

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		Log.d(TAG, "ScreenAV onSaveInstanceState exec");

		super.onSaveInstanceState(outState);
	}

	private void stopTimerAndTask() {

		// if (SystemVarTools.mStartGroupCalllRepoort
		// && mTimerAudioPTTReport != null) {
		// mTimerAudioPTTReport.cancel();
		// mTimerAudioPTTReport = null;
		// }
		if (SystemVarTools.mStartGroupCalllRepoort
				&& SystemVarTools.mTimerAudioPTTReport != null) {
			SystemVarTools.mTimerAudioPTTReport.cancel();
			SystemVarTools.mTimerAudioPTTReport = null;
			SystemVarTools.mTakeAudioPTTFlag = false;
		}

		/*
		 * if (SystemVarTools.mStartGroupCalllRepoort &&
		 * mTimerTaskAudioPTTReport != null) {
		 * mTimerTaskAudioPTTReport.cancel(); mTimerTaskAudioPTTReport = null; }
		 */

		// if (SystemVarTools.mStartGroupCalllRepoort
		// && mTimerVideoPTTReport != null) {
		// mTimerVideoPTTReport.cancel();
		// mTimerVideoPTTReport = null;
		// }
		if (SystemVarTools.mStartGroupCalllRepoort
				&& SystemVarTools.mTimerVideoPTTReport != null) {
			SystemVarTools.mTimerVideoPTTReport.cancel();
			SystemVarTools.mTimerVideoPTTReport = null;
			SystemVarTools.mTakeVideoPTTFlag = false;
		}

		/*
		 * if (SystemVarTools.mStartGroupCalllRepoort &&
		 * mTimerTaskVideoPTTReport != null) {
		 * mTimerTaskVideoPTTReport.cancel(); mTimerTaskVideoPTTReport = null; }
		 */

		// if (SystemVarTools.mStartGroupCalllRepoort
		// && mTimerVideoMonitorReport != null) {
		// mTimerVideoMonitorReport.cancel();
		// mTimerVideoMonitorReport = null;
		// }
		if (SystemVarTools.mStartGroupCalllRepoort
				&& SystemVarTools.mTimerVideoMonitorReport != null) {
			SystemVarTools.mTimerVideoMonitorReport.cancel();
			SystemVarTools.mTimerVideoMonitorReport = null;
			SystemVarTools.mTakeVideoMonitorFlag = false;
		}

		if (SystemVarTools.mStartGroupCalllRepoort
				&& mTimerTaskVideoMonitorReport != null) {
			mTimerTaskVideoMonitorReport.cancel();
			mTimerTaskVideoMonitorReport = null;
		}

	}

	private void changeFragmmentSigns(boolean sign) {
		isGroupVideoFragmentAdded = sign;
		isSingleVideoFragmentAdded = sign;
		isGroupAudioFragmentAdded = sign;
		isSingleAudioFragmentAdded = sign;
		isVideoMonitorFragmentAdded = sign;
		isTryingFragmentAdded1 = sign;
		isTryingFragmentAdded2 = sign;
		isTryingFragmentAdded3 = sign;
		isInCommingFragmentAdded = sign;
	}

	/**
	 * 大唐终端屏幕解锁方式
	 */
	public static void unlock_datang() {
		Intent unlockIntent = new Intent(UNLOCK_SCREEN_ACTION_DT);
		SKDroid.getContext().sendBroadcast(unlockIntent);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] its = event.values;
		if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			// Log.d(TAG, "its[0]:" + its[0]);
			// 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
			if (its[0] == 0.0) {// 贴近手机
				Log.d(TAG, "贴近手机");
				if (localWakeLock.isHeld()) {
					Log.d("sersor", "return");
					return;
				} else {
					localWakeLock.acquire();// 申请设备电源锁
				}
			} else {// 远离手机
				// Log.d(TAG, "远离手机");
				if (localWakeLock.isHeld()) {
					return;
				} else {
					localWakeLock.setReferenceCounted(false);
					localWakeLock.release(); // 释放设备电源锁
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	private WakeLock wl;

	/**
	 * 保持屏幕唤醒状态（即背景灯不熄灭）
	 * 
	 * @param context
	 * @param on
	 *            控制开关
	 */
	public void keepScreenOn(Context context, boolean on) {
		if (on) {
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, TAG);
			wl.acquire();
		} else {
			if (wl != null) {
				wl.release();
				wl = null;
			}
		}
	}

}
