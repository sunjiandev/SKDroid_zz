package com.sunkaisens.skdroid.fragments;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.BaseScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.listener.HeadsetListener;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVSingleAudioFragment extends BaseFragment implements
		HeadsetListener {

	private final static String TAG = AVSingleAudioFragment.class
			.getCanonicalName();

	private Context mContext;

	private BaseScreen mBaseScreen;

	private ServiceAV serviceAV;

	private ModelContact remoteContact;

	private boolean isInit = false;
	private boolean isSpeaker = false;
	private boolean isMute = false;

	private Handler mHandler;
	private NgnTimer CallPeriodTimer = new NgnTimer();

	private View mViewInCallAudio;
	private TextView mTvInfo;
	private TextView mTvDuration;
	private TextView tvRemote;
	private ImageView mAudioSpeaker;
	private ImageView btHang;
	private ImageView mMute;
	private ImageView remote_icon;
	private ImageView back;

	/**
	 * 初始化核心参数
	 * 
	 * @param service
	 * @return
	 */
	public boolean init(ServiceAV service) {

		if (service == null)
			return false;

		serviceAV = service;

		isInit = true;

		MyLog.i(TAG, "Init Finish.");
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = NgnApplication.getContext();
		mBaseScreen = (BaseScreen) getActivity();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (!isInit)
			return null;

		activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		
		GCThread gcThread = new GCThread();
		timer = new Timer();
		timer.schedule(gcThread, 0,10*1000);
		
		MyLog.d(TAG, "ServiceAV=" + serviceAV.toString());

		String remoteParty = serviceAV.getAVSession().getRemotePartyUri();
		remoteContact = SystemVarTools
				.createContactFromRemoteParty(remoteParty);
		if (remoteContact.name == null) {
			remoteContact.name = remoteParty;
		}
		MyLog.d(TAG, "AVSingleAudio  remoteParty:" + remoteParty);

		if (mViewInCallAudio == null) {
			mViewInCallAudio = LayoutInflater.from(mContext).inflate(
					R.layout.view_call_incall_audio, null);
		}
		
		ImageView ivMingOrMi = (ImageView) mViewInCallAudio.findViewById(R.id.iv_ming_or_mi_call_video_incall);
		if (GlobalVar.isSecuriteCardExist) {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_mi);
		} else {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_ming);
		}
		
		mTvInfo = (TextView) mViewInCallAudio.findViewById(R.id.audio_incall);

		tvRemote = (TextView) mViewInCallAudio.findViewById(R.id.audio_remote);

		btHang = (ImageView) mViewInCallAudio.findViewById(R.id.audio_hangup);

		mMute = (ImageView) mViewInCallAudio.findViewById(R.id.audio_mute);

		mAudioSpeaker = (ImageView) mViewInCallAudio
				.findViewById(R.id.audio_speaker);

		remote_icon = (ImageView) mViewInCallAudio
				.findViewById(R.id.audio_icon);

		SystemVarTools.showicon(remote_icon, remoteContact, mContext);

		mTvDuration = (TextView) mViewInCallAudio
				.findViewById(R.id.screen_audio_duration);

		// back
		back = (ImageView) mViewInCallAudio.findViewById(R.id.back);

		mMute.setOnClickListener(myOnClickListener);
		back.setOnClickListener(myOnClickListener);
		btHang.setOnClickListener(myOnClickListener);
		mAudioSpeaker.setOnClickListener(myOnClickListener);

		
		Log.d(TAG, "mRemotePartyDisplayName = " + remoteContact.name);
		tvRemote.setText(remoteContact.name);
		// remote_icon.setImageResource(SystemVarTools.getThumbID(imageid));

		mTvInfo.setText(getString(R.string.string_incall));

		// 如果是安全通信，则显示安全通信标记
		// mViewInCallAudio.findViewById(
		// R.id.view_call_incall_audio_imageView_secure).setVisibility(
		// serviceAV.getAVSession().isSecure() ? View.VISIBLE : View.INVISIBLE);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case ServiceAV.CALL_PERIOD_REFRASH: // 刷新通话时长计时器
					long period = new Date().getTime()
							- serviceAV.getAVSession().getStartTime();
					mTvDuration.setText(SystemVarTools
							.mCallPeriodFormat(period));
					Runtime.getRuntime().gc();
					break;

				default:
					break;

				}
			}
		};

		CallPeriodTimer.schedule(mTimerTaskInCall, 0, 1000);

		serviceAV.getAVSession().setSpeakerphoneOn(false);
		serviceAV.setOnPause(false);
		
		if (SKDroid.isBh04() || SKDroid.isl8848a_l1860()) {
			MyLog.d(TAG, "PAD终端禁用扬声器切换按钮");
			mAudioSpeaker.setBackgroundResource(R.drawable.speaker_down);
			serviceAV.getAVSession().setSpeakerphoneOn(true);
			isSpeaker = true;
			mAudioSpeaker.setClickable(false);
		}

		if( SKDroid.isBh03())
		{
			mAudioSpeaker.setBackgroundResource(R.drawable.speaker_down);
			serviceAV.getAVSession().setSpeakerphoneOn(true);
			isSpeaker = true;
		}

		return mViewInCallAudio;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTimerTaskInCall.cancel();
		CallPeriodTimer.cancel();
		CallPeriodTimer = null;
		timer.cancel();
	}

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.audio_hangup) {
				SystemVarTools.sleep(200);
				serviceAV.hangUpCall();
				mBaseScreen.back();
				isInit = false;
			} else if (id == R.id.back) {
				mBaseScreen.back();
			} else if (id == mAudioSpeaker.getId()) {
				if (!isSpeaker) {
					mAudioSpeaker
							.setBackgroundResource(R.drawable.speaker_down);
					serviceAV.getAVSession().setSpeakerphoneOn(true);
					isSpeaker = true;
				} else {
					mAudioSpeaker.setBackgroundResource(R.drawable.speaker_up);
					serviceAV.getAVSession().setSpeakerphoneOn(false);
					isSpeaker = false;
				}
			} else if (id == mMute.getId()) {
				if (!isMute) {
					serviceAV.setProducerOnPause(true);
					mMute.setBackgroundResource(R.drawable.mute_down);
					isMute = true;
				} else {
					serviceAV.setProducerOnPause(false);
					mMute.setBackgroundResource(R.drawable.mute_up);
					isMute = false;
				}
			}
		}
	};

	/**
	 * 通话时长计时器
	 */
	private final TimerTask mTimerTaskInCall = new TimerTask() {
		@Override
		public void run() {
			if (mHandler != null) {
				mHandler.obtainMessage(ServiceAV.CALL_PERIOD_REFRASH)
						.sendToTarget();
			}
		}
	};

	private Timer timer;

	private static ActivityManager activityManager;

	

	public void handlerPTTinfo(byte[] infoContent, long sessionId) {

		NgnAVSession mySession = NgnAVSession.getSession(sessionId);
		saveLastHistoryEvent(mySession);
		createNewHistoryEvent(mySession);

		PTTInfoMsg msg = new PTTInfoMsg(infoContent);
		if ("Flash Number".equals(msg.getPTTType())) {
			String newRemoteNO = msg.getPTTPhoneNumber();
			remoteContact = SystemVarTools
					.createContactFromRemoteParty(newRemoteNO);
			if (remoteContact.name == null) {
				remoteContact.name = newRemoteNO;
			}
			tvRemote.setText(remoteContact.name);
			NgnAVSession avSession = serviceAV.getAVSession();
			avSession.setRemotePartyUri(NgnUriUtils
					.makeValidSipUri(newRemoteNO));
		}

	}

	/**
	 * 创建新的通话记录，用来保存强插之后调度台的通话记录
	 */
	private void createNewHistoryEvent(NgnAVSession mySession) {
		
		mySession.mHistoryEvent = new NgnHistoryAVCallEvent(false, null);
		((NgnInviteSession) mySession).setState(InviteState.INCOMING);
		((NgnInviteSession) mySession).setState(InviteState.INCALL);

	}

	/**
	 * 调度台强插终端语音单呼后，接收info消息，同时保存被拆掉的通话记录
	 */
	private void saveLastHistoryEvent(NgnAVSession mySession) {

		((NgnInviteSession) mySession).setState(InviteState.TERMINATED);

	}

	@Override
	public void headsetOn() {

	}

	@Override
	public void headsetOff() {
		if (mAudioSpeaker != null) {
			mAudioSpeaker.setBackgroundResource(R.drawable.speaker_down);
		}
	}
	private static class GCThread extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Runtime.getRuntime().gc();
			MyLog.d(TAG, "start gc");

			
			MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
			activityManager.getMemoryInfo(memoryInfo);
			MyLog.d(TAG, "系统剩余可用内存"+(memoryInfo.availMem>>10)+"k");
			MyLog.d(TAG, "系统是否处于低内存运行"+memoryInfo.lowMemory);
			MyLog.d(TAG, "当系统内存低于"+memoryInfo.threshold+"时看成是低内存运行");
		}
	}
	
}
