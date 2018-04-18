/* 
 * service for ScreenAV
 */
package com.sunkaisens.skdroid.Services;

import java.util.TimerTask;
import java.util.Vector;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.AdhocSessionEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.media.NgnCameraProducer;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.sks.adhoc.service.CommandType;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenAV;
import com.sunkaisens.skdroid.Screens.ScreenMap;
import com.sunkaisens.skdroid.Screens.ScreenMediaAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.app.service.NativeService;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.groupcall.PTTActionTypes;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.groupcall.PTTResultTypes;
import com.sunkaisens.skdroid.groupcall.PTTTypes;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ServiceAV {
	// class
	private static final String TAG = ServiceAV.class.getCanonicalName();
	private static ServiceAV lastServiceAV = null;
	private static Vector<ServiceAV> receiverArray = new Vector<ServiceAV>();
	private static boolean antiRotation = false;

	// object
	private NgnAVSession mAVSession = null;
	private ScreenAV screenav = null;
	private long mCurrentInfoCseq = 0;

	public static final int CALL_PERIOD_REFRASH = 10001;

	// adhocSession
	private NgnMediaSession mMediaSession = null;
	private ScreenMediaAV screenMediaAV = null;

	public static boolean isPTTRejected = false;// 标记是否在PTT被抢状态

	private ServiceAV(NgnAVSession session, Context mContext) {
		mAVSession = session;
		mAVSession.incRef(); // Increments the reference counting 增加引用计数
		mAVSession.setContext(mContext); // 本AVSession关联的上下文

		GlobalSession.avSession = mAVSession; // 记住这个会话对象，备用
	}

	private ServiceAV(NgnAVSession session, ScreenAV av) {
		mAVSession = session;
		screenav = av;
		mAVSession.incRef(); // Increments the reference counting 增加引用计数
		mAVSession.setContext(screenav); // 本AVSession关联的上下文

		GlobalSession.avSession = mAVSession; // 记住这个会话对象，备用
	}

	private ServiceAV(NgnAVSession session, ScreenMap map) {
		mAVSession = session;
		mAVSession.incRef(); // Increments the reference counting 增加引用计数
		// mAVSession.setContext(screenMap); // 本AVSession关联的上下文
		mAVSession.setContext(SKDroid.getContext()); // 本AVSession关联的上下文

		// GlobalSession.avSession = mAVSession; //记住这个会话对象，备用
	}

	private ServiceAV(NgnAVSession session) {
		mAVSession = session;
		mAVSession.incRef(); // Increments the reference counting 增加引用计数
	}

	// 创建服务型的av服务对象
	public static ServiceAV create(NgnAVSession session, Context mContext) {
		return (lastServiceAV = new ServiceAV(session, mContext));
	}

	// 创建界面类型的av服务对象
	public static ServiceAV create(NgnAVSession session, ScreenAV av) {
		return (lastServiceAV = new ServiceAV(session, av));
	}

	// 创建界面类型的av服务对象
	public static ServiceAV create(NgnAVSession session, ScreenMap map) {
		return (lastServiceAV = new ServiceAV(session, map));
	}

	// 创建界面类型的av服务对象
	public static ServiceAV create(NgnAVSession session) {
		return (lastServiceAV = new ServiceAV(session));
	}

	// 使用完毕后，需要去释放
	public void release() {
		if (mAVSession != null) {
			mAVSession.setContext(null);
			mAVSession.decRef();
		}
		if (mMediaSession != null) {
			mMediaSession.setContext(null);
			mMediaSession.decRef();
		}
	}

	public static ServiceAV getLastServiceAV() {
		return lastServiceAV;
	}

	/*
	 * get the AVSession
	 */
	public NgnAVSession getAVSession() {
		return mAVSession;
	}

	/*
	 * receive call
	 */
	public static ServiceAV receiveCall(NgnAVSession avSession) {

		Log.d(TAG, "MediaSession: ServiceAV.receiveCall");

		if (GlobalSession.bSocketService == false) {
			((Engine) Engine.getInstance()).getScreenService().bringToFront(
					Main.ACTION_SHOW_AVSCREEN,
					new String[] { "session-id",
							Long.toString(avSession.getId()) });
			return null;
		} else {
			return (lastServiceAV = create(avSession, SKDroid.getContext()));
		}
	}

	/*
	 * make call if socket service mode,return the service av if ui mode,return
	 * null;
	 */
	public static ServiceAV makeCall(String remoteUri, NgnMediaType mediaType,
			int sessionType) {
		// SystemVarTools.mCSdomain = true;
		if (CrashHandler.isCdmaNetwork() && mediaType == NgnMediaType.Audio) {
			Intent csCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ remoteUri));
			SKDroid.getContext().startActivity(csCall);
			return null;
		}

		Log.d(TAG, "呼叫类型:" + sessionType + " |媒体类型:" + mediaType + " |被叫："
				+ remoteUri);

		if (remoteUri == null) {
			MyLog.d(TAG, "remoteUri is null");
			return null;
		}

		// 解决视频通话协商SDP问题 视频分辨率由主叫来定，主被叫都是该分辨率
		NgnProxyPluginMgr.setCurrentVideoSize();
		if (NgnAVSession.hasActiveSession()) { // 提示不可拨打
			if (GlobalSession.bSocketService == false) {
				// SystemVarTools.releaseSessionAll();
				SystemVarTools.showToast(NgnApplication.getContext().getString(
						R.string.is_in_calling_wait));

				Main.isFirstPTT_onKeyDown = true;
				Main.isFirstPTT_onKeyLongPress = true;
			}
			MyLog.d(TAG, "hasActiveSession");
			return null;
		}

		if (GlobalVar.bADHocMode) {
			String uri ;
			if(NgnUriUtils.checkIPAddress(remoteUri)){
				uri = String.format("%s@%s", remoteUri,remoteUri);
			}else{
				uri = SystemVarTools.createContactFromRemoteParty(remoteUri).uri;
			}
			if (uri == null || uri.length() < 1)
				return null;
			Log.e(TAG,"ADHOC set cscf host:" + SystemVarTools.getIPFromUri(uri));
			// set pcscf
			((Engine) Engine.getInstance()).getSipService().ADHOC_SetPcscfHost(
					SystemVarTools.getIPFromUri(uri));
			// ((Engine)Engine.getInstance()).getSipService().ADHOC_SetPcscfHost("255.255.255.255");
		}

		final Engine engine = (Engine) Engine.getInstance();
		final INgnSipService sipService = engine.getSipService();
		final INgnConfigurationService configurationService = engine
				.getConfigurationService();
		final IServiceScreen screenService = engine.getScreenService();
		String validUri = NgnUriUtils.makeValidSipUri(remoteUri); // sip:#@test.com

		if (validUri == null) {
			Log.e(TAG, "failed to normalize sip uri '" + remoteUri + "'");
			return null;
		} else {
			remoteUri = validUri;
			if (remoteUri.startsWith("tel:")) {
				// E.164 number => use ENUM protocol
				final NgnSipStack sipStack = sipService.getSipStack();
				if (sipStack != null) {
					String phoneNumber = NgnUriUtils
							.getValidPhoneNumber(remoteUri);
					if (phoneNumber != null) {
						Log.d(TAG, "!!!!!!!!!@@@@-----" + phoneNumber);
						String enumDomain = configurationService
								.getString(
										NgnConfigurationEntry.GENERAL_ENUM_DOMAIN,
										NgnConfigurationEntry.DEFAULT_GENERAL_ENUM_DOMAIN);
						String sipUri = sipStack.dnsENUM("E2U+SIP",
								phoneNumber, enumDomain);
						if (sipUri != null) {
							remoteUri = sipUri;
						}
					}
				}
			}
		}

		final NgnAVSession avSession = NgnAVSession.createOutgoingSession(
				sipService.getSipStack(), mediaType); // 建立一个拨打电话用的outgoing
														// NgnAVSession
		avSession.setRemotePartyUri(remoteUri); // HACK
		avSession.setSessionType(sessionType);

		// GlobalSession.avSession = avSession; //记住这个会话对象，备用

		// flag_group = flag;

		if (GlobalSession.bSocketService == false) {
			screenService
					.show(ScreenAV.class, Long.toString(avSession.getId()));
		} else {

		}

		// Hold the active call
		final NgnAVSession activeCall = NgnAVSession
				.getFirstActiveCallAndNot(avSession.getId());
		if (activeCall != null) {
			activeCall.holdCall();
		}

		avSession.makeCall(remoteUri, sessionType);

		if (GlobalSession.bSocketService) {
			return (lastServiceAV = create(avSession, SKDroid.getContext()));
		} else {
			MyLog.d(TAG, "bSocketService false.");
			return null;
		}
	}

	public void applyCamRotation(int rotation) {
		if (mAVSession != null) {
			switch (rotation) {
			case 0:
				if (antiRotation == false) {
					mAVSession.setRotation(0);
				} else {
					mAVSession.setRotation(180);
				}
				mAVSession.setProducerFlipped(false);
				break;
			case 90:
				if (antiRotation == false) {
					mAVSession.setRotation(90);
				} else {
					mAVSession.setRotation(270);
				}
				mAVSession.setProducerFlipped(false);
				break;
			case 180:
				if (antiRotation == false) {
					mAVSession.setRotation(180);
				} else {
					mAVSession.setRotation(0);
				}
				mAVSession.setProducerFlipped(true);
				break;
			case 270:
				if (antiRotation == false) {
					mAVSession.setRotation(270);
				} else {
					mAVSession.setRotation(90);
				}
				mAVSession.setProducerFlipped(true);
				break;
			}

		}
	}

	public boolean hangUpCall() {
		if (mAVSession != null) {
			if (NgnApplication.isBh()) { // 正样PAD、手持台
				final AudioManager audiomanager = NgnApplication
						.getAudioManager();
				audiomanager.setMode(AudioManager.MODE_NORMAL);
				Log.d(TAG,
						"audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION); - bh03/bh04");
			}

			// 用户挂断100s后，如果session没有被清空则将其清空 gzc 20140921
			NgnTimer timer = new NgnTimer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					long sessionId = mAVSession.getId();
					NgnAVSession session = NgnAVSession.getSession(sessionId);
					if (session != null) {
						NgnAVSession.releaseSession(session);
					}
					;
				}
			}, 100 * 1000);

			return mAVSession.hangUpCall();
		} else if (mMediaSession != null) {
			ServiceAdhoc.getInstance().Hungup();

		}
		return false;
	}

	public boolean acceptCall() {
		if (mAVSession != null) {
			Log.i(TAG, "语音/视频 摘机");
			return mAVSession.acceptCall();
		}
		return false;
	}

	// public void setOnMute(){
	// if (mAVSession != null) {
	// if(!mAVSession.isOnMute())
	// mAVSession.setOnMute(true);
	// }
	// }
	public void setOnResetJB() {
		if (mAVSession != null) {
			mAVSession.setOnResetJB();
		}
	}

	public void setOnPause(boolean pause) {
		MyLog.d(TAG, "setOnPause(" + pause + ")");
		if (mAVSession != null) {
			mAVSession.setOnPause(pause);
		}
		if (mMediaSession != null) {
			mMediaSession.setOnPause(pause);
		}
	}

	// public void setOnStart()
	// {
	// if (mAVSession != null) {
	// mAVSession.setOnStart(true);
	// }
	// }

	// }

	public void setConsumerOnPause(boolean pause) {
		if (mAVSession != null) {
			mAVSession.setConsumerOnPause(pause);
		}
		if (mMediaSession != null) {
			mMediaSession.setConsumerOnPause(pause);
		}
	}

	public void setProducerOnPause(boolean pause) {
		if (mAVSession != null) {
			mAVSession.setProducerOnPause(pause);
		}
		if (mMediaSession != null) {
			mMediaSession.setProducerOnPause(pause);
		}
	}

	public void setOnLocalHold() {
		if (mAVSession != null) {
			if (!mAVSession.isLocalHeld())
				mAVSession.setLocalHold(true);
		}
	}

	public boolean resumeCall() {
		if (mAVSession != null) {
			if (mAVSession.isLocalHeld())
				return mAVSession.resumeCall();
		}
		return false;
	}

	// public void setMicrophoneMute(){
	// if (mAVSession != null) {
	// if(!mAVSession.isMicrophoneMute())
	// mAVSession.setMicrophoneMute(true);
	// }
	// }

	// public void cancelMute(){
	// if(mAVSession.isOnMute()){
	// mAVSession.setOnMute(false);
	// }
	// }
	public void toggleSpeakerphone() {
		if (mAVSession != null) {
			mAVSession.toggleSpeakerphone();
		}
	}

	public boolean sendInfo(java.nio.ByteBuffer payload, String contentType) {
		if (mAVSession != null) {
			return mAVSession.sendInfo(payload, contentType);
		}
		return false;
	}

	// public boolean sendInfo(String content,String contentType) {
	// if (mAVSession != null) {
	// return mAVSession.sendInfo(content, contentType);
	// }
	// return false;
	// }

	/**
	 * 发送info消息
	 * 
	 * @param content
	 * @param contentType
	 * @return
	 */
	private static boolean sendInfo(String content, String contentType) {
		Log.d(TAG, "sendInfo()");
		NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
				.getSessions();
		for (int i = 0; i < mAVSessions.size(); i++) {
			NgnAVSession session = mAVSessions.getAt(i);
			if (session != null && isGroupCall(session.getSessionType())) {
				return session.sendInfo(content, contentType);
			}
		}
		return false;
	}

	/**
	 * 判断是否是组呼会话
	 * 
	 * @param st
	 * @return
	 */
	public static boolean isGroupCall(int st) {
		if (st == SessionType.GroupAudioCall
				|| st == SessionType.GroupVideoCall) {
			return true;
		}
		return false;
	}

	/*
	 * register,and receive sip and media event.
	 */
	public void registerReceiver() {
		receiverArray.add(this);
	}

	/*
	 * unregister.
	 */
	public void unRegisterReceiver() {
		receiverArray.remove(this);
	}

	/*
	 * 
	 */
	public static void onReceive(Context context, Intent intent) {
		try {
			for (int i = 0; i < receiverArray.size(); i++) {
				if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(intent
						.getAction())) {
					receiverArray.get(i).handleSipEvent(intent);
				} else if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT
						.equals(intent.getAction())) {
					receiverArray.get(i).handleMediaEvent(intent);
				} else if (AdhocSessionEventArgs.ADHOC_SESSION_EVENT
						.equals(intent.getAction())) {
					receiverArray.get(i).handleAdhocSessionEvent(intent);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleMediaEvent(Intent intent) {
		Log.d(TAG,
				"service AVhandleMediaEvent loadView() loadTermView() loadTermView(phrase)");
		try {

			if (GlobalSession.bSocketService == false && screenav != null) {// 非服务模式，消息交给界面去处理
				screenav.handleMediaEvent(intent);
				return;
			}

			final String action = intent.getAction();

			if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT
					.equals(action)) {
				NgnMediaPluginEventArgs args = intent
						.getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
				if (args == null) {
					Log.e(TAG, "Invalid event args");
					return;
				}

				switch (args.getEventType()) {
				case STARTED_OK: // started or restarted (e.g. reINVITE)
				{
					// screenav.mIsVideoCall = (mAVSession.getMediaType() ==
					// NgnMediaType.AudioVideo || mAVSession.getMediaType() ==
					// NgnMediaType.Video);
					// screenav.loadView();
					if (mAVSession != null
							&& (mAVSession.getMediaType() == NgnMediaType.AudioVideo || mAVSession
									.getMediaType() == NgnMediaType.Video)) {
						mAVSession.startVideoProducerPreview();
					}

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

		} catch (Exception e) {
			Log.d(TAG, "Exception:" + e.getMessage());
		}
	}

	private void handleSipEvent(Intent intent) {

		Log.d(TAG, "InviteEvent ServiceAV handleSipEvent");

		if (GlobalSession.bSocketService == false && screenav != null) {// 非服务模式，消息交给界面去处理
			Log.d(TAG, "InviteEvent bSocketService false");
			screenav.handleSipEvent(intent);
			return;
		}
		if (mAVSession == null) {
			Log.e(TAG, "Invalid session object");
			return;
		}

		try {

			final String action = intent.getAction();
			Log.d(TAG, "Receive a call,handling...eventtype=" + action);
			if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
				NgnInviteEventArgs args = intent
						.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
				if (args == null) {
					return;
				}
				if (args.getSessionId() != mAVSession.getId()) {
					return;
				}

				NgnInviteEventTypes eventType = args.getEventType(); // CONNECTED
																		// TERMWAIT
				if (eventType.equals(NgnInviteEventTypes.ENCRYPT_INFO)) {
					Log.d(TAG, "Receive a call,encryptCall and eventtype="
							+ eventType);
					switch (mAVSession.getState()) {
					case NONE:
					default:
						break;
					case INCOMING:
					case INPROGRESS:
					case REMOTE_RINGING:// 被叫流程
						break;
					case EARLY_MEDIA:// 主叫流程
						break;
					}
				} else if (eventType
						.equals(NgnInviteEventTypes.PTT_INFO_REQUEST)) { // 组呼
					Log.d(TAG,
							"Receive a call,group Call info message and eventtype="
									+ eventType);
					long CseqNum = intent.getLongExtra(
							NgnInviteEventArgs.EXTRA_CSEQ, 0);
					Log.d(TAG, "当前CSeq=" + mCurrentInfoCseq + "  新的CSeq="
							+ CseqNum);
					if (mCurrentInfoCseq > CseqNum) {
						return;
					} else {
						mCurrentInfoCseq = CseqNum;
					}
					switch (mAVSession.getState()) {
					case NONE:
					default:
						break;

					case INCOMING:
					case INPROGRESS:
					case REMOTE_RINGING:
					case EARLY_MEDIA:
						break;
					case INCALL:
						break;

					}
				} else if ((eventType
						.equals(NgnInviteEventTypes.GROUP_VIDEO_MONITORING))) {
					Log.d(TAG,
							"Receive a call, group video monitoring and eventtype = "
									+ eventType);
					switch (mAVSession.getState()) {
					case NONE:
					default:
						// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
						break;

					case INCOMING:
					case INPROGRESS:
					case REMOTE_RINGING:
					case EARLY_MEDIA:
					case INCALL:
						if (mAVSession.getmVideoProducer() != null) {
							// mAVSession.setmSendVIdeo(true);
							Log.d(TAG, "GROUP_VIDEO_MONITORING mSendVideo:true");
						}
						mAVSession.startVideoProducerPreview();// startVideo(true,true);
						break;

					}

				} else if ((eventType.equals(NgnInviteEventTypes.VIEDO_TRANSMINT))) {
					Log.d(TAG,
							"Receive a call, video transmint and eventtype = "
									+ eventType);
					switch (mAVSession.getState()) {
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
						// loadTryingView();
						break;
					case INCALL:
						if (mAVSession.getmVideoProducer() != null) {
							// mAVSession.setmSendVIdeo(true);
							Log.d(TAG, "VIEDO_TRANSMINT mSendVideo:true");
						}
						mAVSession.startVideoProducerPreview();// startVideo(true,true);//loadInCallVideoView();
					}
				} else {
					switch (mAVSession.getState()) { // TERMINATED
					case NONE:
					default:
						break;

					case INCOMING:
					case INPROGRESS:
					case REMOTE_RINGING:
						break;

					case EARLY_MEDIA:
					case INCALL:
						if (!mAVSession.isGroupVideoCall()) {
							if (mAVSession.getmVideoProducer() != null) {
								Log.d(TAG, "VIEDO mSendVideo:true");
							}
						} else {
							mAVSession.setmSendVIdeo(false);
						}
						mAVSession.startVideoProducerPreview();
						break;

					case TERMINATING:
					case TERMINATED:
						// mAVSession.setmSendVIdeo(false);
						break;
					}
				}
			}

		} catch (Exception e) {
			Log.d(TAG, "Exception:" + e.getMessage());
		}

	}

	/**
	 * 处理组呼INFO请求
	 * 
	 * @param infoContent
	 */
	/*
	 * public void handleRequestPTTInfoMsg(byte[] infoContent,ServiceAV
	 * serviceAV,ImageView mBtPTT) { Log.d(TAG, "handleRequestPTTInfoMsg()");
	 * Log.d(TAG, "receive handle request ptt info msg!!!!");
	 * 
	 * if (mPttCall == null) mPttCall = new GroupPTTCall(); PTTInfoMsg msg = new
	 * PTTInfoMsg(infoContent); mPttCall.handlePTTInfoMsg(msg);
	 * 
	 * String pttAction,pttResult;
	 * 
	 * Log.d(TAG, "mPttCall.getState() = " + mPttCall.getState());
	 * 
	 * if(mPttCall.getState() != PTTState.REGECTED){
	 * serviceAV.setConsumerOnPause(false); }else {
	 * serviceAV.setConsumerOnPause(true); }
	 * 
	 * Engine engine = (Engine) Engine.getInstance(); Context mContext =
	 * NgnApplication.getContext();
	 * 
	 * switch (mPttCall.getState()) { case NONE: case REQUESTING: break; case
	 * GRANTED: //PTT抢占成功 engine.playNotificationTone();
	 * 
	 * Log.d("handleRequestPTTInfoMsg()", "mPttCall.getState() = GRANTED");
	 * 
	 * //PTT按钮为红色表示PTT抢占成功 mBtPTT.setImageResource(R.drawable.ptt_down);
	 * mBtPTT.setSelected(true);
	 * 
	 * //语音组呼时，终端抢PTT后，扬声器停止放音 serviceAV.setOnPause(false);
	 * 
	 * break; case RELEASE_SUCCESS: //PTT释放成功 Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = RELEASE_SUCCESS");
	 * 
	 * //PTT按钮为绿色表示目前没人抢占PTT mBtPTT.setImageResource(R.drawable.ptt_up);
	 * mBtPTT.setEnabled(true); serviceAV.setOnPause(true);
	 * 
	 * break; case RELEASED: Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = RELEASED"); //green
	 * //mBtPTT.setTextColor(getResources().getColor(R.color.color_green));
	 * mBtPTT.setImageResource(R.drawable.ptt_up); //绿色 mBtPTT.setEnabled(true);
	 * serviceAV.sendPTTReleaseAckInfoMsg();
	 * 
	 * break; case REGECTED: Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = REGECTED"); //PTT按钮为白色表示PTT已经被抢占，目前无法抢占
	 * mBtPTT.setImageResource(R.drawable.ptt_down); mBtPTT.setEnabled(false);
	 * mBtPTT.setSelected(false); mTakePTTFlag = false; //保存抢占PTT这个人的号码 pttName
	 * = msg.getPTTPhoneNumber(); serviceAV.setOnResetJB(); //只有视频组呼才存在切换视频的情况
	 * if (serviceAV.getAVSession().isGroupVideoCall()) {
	 * //如果当前正在订阅某人的视频，切换视频前要先取消订阅 if (mPttCall.isSubscribe()) {
	 * serviceAV.sendPTTCancelInfoMsg(mPttCall.getCurrentSubscribeName());
	 * 
	 * Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = REGECTED and 取消当前订阅的视频: " +
	 * mPttCall.getCurrentSubscribeName()); mPttCall.setIsSubscribe(false); }
	 * else { //订阅抢占PTT那个人的视频
	 * serviceAV.sendPTTSubscribeInfoMsg(msg.getPTTPhoneNumber());
	 * mPttCall.setCurrentSubscribeName(msg.getPTTPhoneNumber());
	 * mPttCall.setIsSubscribe(true); Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = REGECTED and 切换订阅视频： " + msg.getPTTPhoneNumber());
	 * } }
	 * 
	 * break; case ALAVE: Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = ALAVE");
	 * //mTvInfo.setText(getString(R.string.string_groupcall_ptttaken));
	 * //setOnPause(true); //toggleSpeakerphone(); break; case
	 * SUBSCRIBE_SUCCESS: Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = SUBSCRIBE_SUCCESS"); // Video Consumer
	 * loadVideoPreview(); //
	 * mTvInfo.setText(getString(R.string.string_groupcall_subscribed));
	 * //视频订阅成功 break; case SUBSCRIBE_FAILED: Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = SUBSCRIBE_FAILED"); //
	 * mTvInfo.setText(getString(R.string.string_groupcall_subscribe_failed));
	 * //视频订阅失败 break; case ONLINE: Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = ONLINE, " + "mPttCall = " + mPttCall.toString());
	 * Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = ONLINE, isSubscribe = " + mPttCall.isSubscribe());
	 * if (mPttCall.isFirstOnline() && !mPttCall.isSubscribe()) {
	 * Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = ONLINE is first Online and sendPTTSubscribeInfoMsg"
	 * );
	 * 
	 * //主叫延迟1秒订阅被叫，目的是使被叫有足够的时间LoadGroupInCallVideoView try {
	 * Thread.sleep(500); } catch (InterruptedException e) {
	 * e.printStackTrace(); } if (serviceAV.getAVSession().isGroupVideoCall()) {
	 * //subscribe,only video group call;
	 * serviceAV.sendPTTSubscribeInfoMsg(msg.getPTTPhoneNumber()); }
	 * mPttCall.setCurrentSubscribeName(msg.getPTTPhoneNumber());
	 * mPttCall.setIsSubscribe(true); Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = ONLINE and 当前订阅视频： " + msg.getPTTPhoneNumber()); }
	 * 
	 * ModelContact onLineUser =
	 * SystemVarTools.getContactFromPhoneNumber(msg.getPTTPhoneNumber());
	 * if(onLineUser == null){ onLineUser = new ModelContact(); onLineUser.name
	 * = msg.getPTTPhoneNumber(); onLineUser.mobileNo = msg.getPTTPhoneNumber();
	 * } if(!onLineUsers.contains(onLineUser)){ onLineUsers.add(onLineUser);
	 * if(usersView != null && usersView.getAdapter()!=null){
	 * ((GroupUsersAdapter)usersView.getAdapter()).notifyDataSetChanged(); }
	 * Log.d(TAG, "Online User("+msg.getPTTPhoneNumber()+") is NOT ONLINE");
	 * }else { Log.d(TAG, "Online User("+msg.getPTTPhoneNumber()+") is ONLINE");
	 * }
	 * 
	 * 
	 * break; case VIDEOSUB_TURNON: Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = VIDEOSUB_TURNON");
	 * 
	 * serviceAV.sendPTTSubscribeAckInfoMsg(); //startVideo(true,true); if
	 * (mPttCall.isStateChanged()) { // Video Producer // startVideo(true,true);
	 * serviceAV.getAVSession().setmSendVIdeo(true); } break; case
	 * VIDEOSUB_TURNOFF:
	 * 
	 * Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = VIDEOSUB_TURNOFF");
	 * serviceAV.sendPTTSubscribeAckInfoMsg(); if (mPttCall.isStateChanged()) {
	 * // Video Producer // startVideo(false,true);
	 * serviceAV.getAVSession().setmSendVIdeo(false); } break; case CALSUB:
	 * Log.d("handleRequestPTTInfoMsg()", "mPttCall.getState() = CALSUB");
	 * serviceAV.sendPTTCancelAckInfoMsg(); break; case CANCEL_SUCCESS:
	 * 
	 * Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = CANCEL_SUCCESS");
	 * 
	 * //订阅抢占PTT那个人的视频 serviceAV.sendPTTSubscribeInfoMsg(pttName);
	 * mPttCall.setCurrentSubscribeName(pttName); mPttCall.setIsSubscribe(true);
	 * Log.d("handleRequestPTTInfoMsg()",
	 * "mPttCall.getState() = CANCEL_SUCCESS and 切换订阅视频： " + pttName); break;
	 * 
	 * case GET_AUDIO: //伴音回传 pttAction = msg.getPTTAction(); final AudioManager
	 * audiomanager = NgnApplication.getAudioManager();
	 * audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION); Log.d(TAG,
	 * String
	 * .format("PTTAction: %s. SetMode(AudioManager.MODE_IN_COMMUNICATION)"
	 * ,pttAction)); if(pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_DISABLE))
	 * { Log.d(TAG,
	 * "PTTAction: PTTActionTypes.PTT_ACT_AUDIO_DISABLE && lc_audio_record=0 && onPause=true"
	 * ); audiomanager.setParameters("lc_audio_record=0");
	 * serviceAV.setOnPause(true); }else
	 * if(pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_ANALOG)) { Log.d(TAG,
	 * "PTTAction: PTTActionTypes.PTT_ACT_AUDIO_ANALOG && lc_audio_record=1 && onPause=false"
	 * ); audiomanager.setParameters("lc_audio_record=1");
	 * serviceAV.setOnPause(false); }else
	 * if(pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_HD)) { Log.d(TAG,
	 * "PTTAction: PTTActionTypes.PTT_ACT_AUDIO_HD && lc_audio_record=2 && onPause=false"
	 * ); audiomanager.setParameters("lc_audio_record=2");
	 * serviceAV.setOnPause(false); }
	 * 
	 * break;
	 * 
	 * default: break; }
	 * 
	 * }
	 */

	/**
	 * 发送话权请求消息
	 * 
	 * @return
	 */
	public static boolean sendPTTRequestInfoMsg() {
		try {
			Log.d(TAG, "sendPTTRequestInfoMsg()");
			PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REQUEST);
			msg.setPTTPhoneNumber(NgnEngine.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
			Log.d(TAG, "send ptt info msg:" + msg.toString());
			return sendInfo(msg.toString(), "sunkaisens/PTT");

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 发送话权释放消息
	 * 
	 * @return
	 */
	public static boolean sendPTTReleaseInfoMsg() {
		Log.d(TAG, "sendPTTReleaseInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_RELEASE);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送组呼成员在线状态请求
	 * 
	 * @return
	 */
	public boolean sendPTTInquireInfoMsg() {
		Log.d(TAG, "sendPTTInquireInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_INQUIRE);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTAction("Active Member");
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送视频订阅消息
	 * 
	 * @param to
	 * @return
	 */
	public static boolean sendPTTSubscribeInfoMsg(String to) {
		Log.d("zhangjie:sendPTTSubscribeInfoMsg()", "sendPTTSubscribeInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_SUBSCRIBE);
		msg.setPTTPhoneNumber(to);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送取消订阅消息
	 * 
	 * @param to
	 * @return
	 */
	public boolean sendPTTCancelInfoMsg(String to) {
		Log.d(TAG, "sendPTTCancelInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_CANCEL);
		msg.setPTTPhoneNumber(to);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 
	 * 发送订阅成功消息
	 * 
	 * @return
	 */
	public static boolean sendPTTSubscribeAckInfoMsg() {
		Log.d(TAG, "sendPTTSubscribeAckInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_SUBSCRIBE_ACK);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送取消订阅成功消息
	 * 
	 * @return
	 */
	public static boolean sendPTTCancelAckInfoMsg() {
		Log.d(TAG, "sendPTTCancelAckInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_CANCEL_ACK);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送话权释放成功消息
	 * 
	 * @return
	 */
	public static boolean sendPTTReleaseAckInfoMsg() {
		Log.d(TAG, "sendPTTReleaseAckInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_RELEASE_ACK);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送组呼心跳消息
	 * 
	 * @return
	 */
	public static boolean sendPTTReportAliveInfoMsg() {
		Log.d(TAG, "sendPTTReportAliveInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REPORT);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		msg.setPTTAction(PTTActionTypes.PTT_ACT_ALIVE);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 
	 * @return
	 */
	public static boolean sendMonitorReportAliveInfoMsg() { // zhaohua add on
															// 20140708
		Log.d(TAG, "sendMonitorReportAliveInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REPORT);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		msg.setPTTAction(PTTActionTypes.PTT_ACT_ALIVE);
		Log.d(TAG, "send monitor info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT"); // sunkaisens/Monitor
	}

	/**
	 * 切换摄像头
	 */
	public void switchCameraFrontOrBack() {
		Log.d(TAG, "当前的摄像头数 = " + NgnCameraProducer.getNumberOfCameras());
		if (getAVSession() != null) {
			getAVSession().toggleCamera();
			applyCamRotation(getAVSession().compensCamRotation(true)); // false
																		// ->
																		// true
																		// 解决手持台切换摄像头时，对端设备显示倒像问题
		}

	}

	/**
	 * 创建本地视频界面组件
	 * 
	 * @param service
	 * @param bZOrderTop
	 * @return
	 */
	public static View createLocalPreview(ServiceAV service, boolean bZOrderTop) {

		if (service == null) {
			return null;
		}

		// cancelBlankPacket();
		final View localPreview = service.getAVSession()
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
		} else {
			MyLog.d(TAG, "localPreview is null.");
		}
		return localPreview;
	}

	/**
	 * 创建对端视频界面组件
	 * 
	 * @param service
	 * @return
	 */
	public static View createRemoteVideoPreview(ServiceAV service) {
		MyLog.d(TAG, "loadVideoPreview()");
		MyLog.d(TAG, "service=" + service + "  " + "service.getAVSession()="
				+ service.getAVSession());
		final View remotePreview = service.getAVSession()
				.startVideoConsumerPreview();
		MyLog.d(TAG, "remotePreview=" + remotePreview);
		if (remotePreview != null) {
			final ViewParent viewParent = remotePreview.getParent();
			if (viewParent != null && viewParent instanceof ViewGroup) {
				((ViewGroup) (viewParent)).removeView(remotePreview);
			}

			GlobalVar.isVideoDisp = false;
		}
		return remotePreview;
	}

	public void applyCameraUpOrDown(int rotation) {
		if (getAVSession() != null) {
			switch (rotation) {
			case 0:
				if (antiRotation == false) {
					getAVSession().setRotation(0);
				} else {
					getAVSession().setRotation(180);
				}
				getAVSession().setProducerFlipped(false);
				break;
			case 90:
				if (antiRotation == false) {
					getAVSession().setRotation(270);
				} else {
					getAVSession().setRotation(90);
				}
				getAVSession().setProducerFlipped(false);
				break;
			case 180:
				if (antiRotation == false) {
					getAVSession().setRotation(180);
				} else {
					getAVSession().setRotation(0);
				}
				getAVSession().setProducerFlipped(true);
				break;
			case 270:
				if (antiRotation == false) {
					getAVSession().setRotation(270);
				} else {
					getAVSession().setRotation(90);
				}
				getAVSession().setProducerFlipped(true);
				break;
			}

		}
	}

	public long getmCurrentInfoCseq() {
		return mCurrentInfoCseq;
	}

	public void setmCurrentInfoCseq(long mCurrentInfoCseq) {
		this.mCurrentInfoCseq = mCurrentInfoCseq;
	}

	/************************* Gongle Create MediaSession Object ********************************************/
	//
	private void handleAdhocSessionEvent(Intent intent) {
		Log.d(TAG, "InviteEvent ServiceAV handleAdhocSessionEvent");
		final String action = intent.getAction();
		if (AdhocSessionEventArgs.ADHOC_SESSION_EVENT.equals(action)) {
			AdhocSessionEventArgs args = intent
					.getParcelableExtra(AdhocSessionEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args(args is null).");
				return;
			}
			// setMediaSession(args.getSessionId());
			if (screenMediaAV != null) {// 非服务模式，消息交给界面去处理
				Log.d(TAG, "InviteEvent ADHOC");
				screenMediaAV.handleAdhocSessionEvent(intent);
				return;
			}
			// else{
			//
			// AdhocSessionEventTypes eventType = args.getEventType(); // INCALL
			// Log.d(TAG, "action=" + action + "eventtype=" + eventType);
			//
			// if (eventType.equals(AdhocSessionEventTypes.INCALL)) {//主叫流程
			//
			// }
			// else if(eventType.equals(AdhocSessionEventTypes.INCOMING)){//被叫流程
			//
			// }
			// Log.d(TAG,
			// "InviteEvent ServiceAV handleAdhocSessionEvent  screenMediaAV == null");
			// }
			//
		}
	}

	private ServiceAV(NgnMediaSession session, ScreenMediaAV av) {

		mMediaSession = session;
		screenMediaAV = av;
		mMediaSession.incRef(); // Increments the reference counting 增加引用计数
		mMediaSession.setContext(screenMediaAV); // 本AVSession关联的上下文

		GlobalSession.mediaSession = mMediaSession; // 记住这个会话对象，备用
	}

	private ServiceAV(NgnMediaSession session, Context mContext) {
		mMediaSession = session;
		mMediaSession.incRef(); // Increments the reference counting 增加引用计数
		mMediaSession.setContext(mContext); // 本AVSession关联的上下文

		GlobalSession.mediaSession = mMediaSession; // 记住这个会话对象，备用
	}

	private ServiceAV(NgnMediaSession session) {
		mMediaSession = session;
		mMediaSession.incRef(); // Increments the reference counting 增加引用计数
	}

	// 创建界面类型的av服务对象
	public static ServiceAV create(NgnMediaSession session, ScreenMediaAV av) {
		return (lastServiceAV = new ServiceAV(session, av));
	}

	// 创建服务型的av服务对象
	public static ServiceAV create(NgnMediaSession session, Context mContext) {
		return (lastServiceAV = new ServiceAV(session, mContext));
	}

	public void setMediaSession(long sessionID) {
		mMediaSession = NgnMediaSession.getSession(sessionID);
	}

	/*
	 * get the AVSession
	 */
	public NgnMediaSession getMediaSession() {
		return mMediaSession;
	}

	public static ServiceAV receiveCall(NgnMediaSession mediaSession) {
		// if(mediaSession.isOutgoing() == false){
		if (GlobalSession.bSocketService == false) {
			Log.d(TAG, "ServiceAV.receiveCall: NgnMediaSessionid = "
					+ mediaSession.getId());
			((Engine) Engine.getInstance()).getScreenService().bringToFront(
					Main.ACTION_SHOW_MEDIASCREEN,
					new String[] { "session-id",
							Long.toString(mediaSession.getId()) });
		} else {
			return (lastServiceAV = create(mediaSession, SKDroid.getContext()));
		}

		return null;
	}

	public static ServiceAV makeCall(String remoteIp, NgnMediaType mediaType,
			int sessionType, boolean isSuperCall) {

		String myIP = Engine.getInstance().getNetworkService()
				.getLocalIP(false);
		if (sessionType == SessionType.GroupAudioCall){
			remoteIp = CommandType.BROADCAST_IP;
		}
			
		final NgnMediaSession mediaSession = NgnMediaSession
				.creatOutGoingSession(myIP, remoteIp, mediaType, sessionType);

		Log.d(TAG, "mediaSession ID = " + mediaSession.getId() + ", remoteIp = " + remoteIp);
		// final Engine engine = (Engine) Engine.getInstance();
		// final IScreenService screenService = engine.getScreenService();
		// screenService.show(ScreenMediaAV.class,
		// Long.toString(mediaSession.getId()));
		if (sessionType == SessionType.GroupAudioCall) {
			if (isSuperCall == false) {
				ServiceAdhoc.getInstance().MakeCall(
						CommandType.CMD_NORMAL_GROUP_AUDIOCALL,
						CommandType.BROADCAST_IP, mediaSession.getId());
			} else if (isSuperCall)
				ServiceAdhoc.getInstance().MakeCall(
						CommandType.CMD_SUPER_GROUP_AUDIOCALL,
						CommandType.BROADCAST_IP, mediaSession.getId());
		} else if (sessionType == SessionType.AudioCall)
			ServiceAdhoc.getInstance().MakeCall(
					CommandType.CMD_NORMAL_GROUP_AUDIOCALL, remoteIp,
					mediaSession.getId());
		if (sessionType == SessionType.GroupVideoCall) {
			if (isSuperCall == false) {
				ServiceAdhoc.getInstance().MakeCall(
						CommandType.CMD_NORMAL_GROUP_VIDEOCALL,
						CommandType.BROADCAST_IP, mediaSession.getId());
			} else if (isSuperCall)
				ServiceAdhoc.getInstance().MakeCall(
						CommandType.CMD_SUPER_GROUP_VIDEOCALL,
						CommandType.BROADCAST_IP, mediaSession.getId());
		}

		return null;
	}

	public void setSpeakerphoneOn(boolean speakerOn) {
		if (mAVSession != null)
			mAVSession.setSpeakerphoneOn(speakerOn);
		else
			mMediaSession.setSpeakerphoneOn(speakerOn);
	}

	public String getRemotePartyUri() {
		if (mAVSession != null) {
			return mAVSession.getRemotePartyUri();
		} else {
			return mMediaSession.getRemotePartyUri();
		}
	}

	public long getStartTime() {
		if (mAVSession != null) {
			return mAVSession.getStartTime();
		} else {
			return mMediaSession.getStartTime();
		}
	}

	public boolean isSpeakerOn() {
		if (mAVSession != null) {
			return mAVSession.isSpeakerOn();
		} else if (mMediaSession != null) {
			return mMediaSession.isSpeakerOn();
		}
		return false;
	}

	public boolean isConnected() {
		if (mAVSession != null) {
			return mAVSession.isConnected();
		} else if (mMediaSession != null) {
			return mMediaSession.isConnected();
		}
		return false;
	}

	public void sendPTTRequestCmd() {
		ServiceAdhoc.getInstance().sendPTTRequestCMD();
	}

	public void sendPTTReleaseCmd() {
		ServiceAdhoc.getInstance().sendPTTReleaseCMD();
	}

}
