package com.sunkaisens.skdroid.fragments;

import java.util.Date;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

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
import com.sunkaisens.skdroid.Screens.BaseScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.listener.HeadsetListener;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVSingleAudioTryingFragment extends BaseFragment implements
		HeadsetListener {

	private final static String TAG = AVSingleAudioFragment.class
			.getCanonicalName();

	private Context mContext;

	private BaseScreen mActivity;

	private ServiceAV serviceAV;

	private ModelContact remoteContact;

	private boolean isInit = false;
	private boolean isSpeaker = true;
	private boolean isMute = false;

	private Handler mHandler;
	// private NgnTimer CallPeriodTimer = new NgnTimer();

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
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = NgnApplication.getContext();
		mActivity = (BaseScreen) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (!isInit)
			return null;

		String remoteParty = serviceAV.getAVSession().getRemotePartyUri();
		remoteContact = SystemVarTools
				.createContactFromRemoteParty(remoteParty);
		if (remoteContact.name == null) {
			remoteContact.name = remoteParty;
		}
		MyLog.d(TAG, "语音单呼  remoteParty:" + remoteParty);

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

		back.setOnClickListener(myOnClickListener);
		btHang.setOnClickListener(myOnClickListener);
		mAudioSpeaker.setOnClickListener(myOnClickListener);
		mAudioSpeaker.setBackgroundResource(R.drawable.speaker_down);

		mMute.setBackgroundResource(R.drawable.mute_disable);
		mMute.setClickable(false);

		Log.d(TAG, "mRemotePartyDisplayName = " + remoteContact.name);
		tvRemote.setText(remoteContact.name);
		// remote_icon.setImageResource(SystemVarTools.getThumbID(imageid));

		mTvInfo.setText(getString(R.string.string_audio_trying));

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case ServiceAV.CALL_PERIOD_REFRASH: // 刷新通话时长计时器
					long period = new Date().getTime()
							- serviceAV.getAVSession().getStartTime();
					mTvDuration.setText(SystemVarTools
							.mCallPeriodFormat(period));
					break;

				default:
					break;

				}
			}
		};

		// CallPeriodTimer.schedule(mTimerTaskInCall, 0, 1000);

		serviceAV.getAVSession().setSpeakerphoneOn(false);

		if (GlobalVar.bADHocMode) {
			NgnTimer timer = new NgnTimer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					if (serviceAV.getAVSession() != null
							&& serviceAV.getAVSession().isConnected() == false) {
						serviceAV.getAVSession().hangUpCall();
					}
				}
			}, 20 * 1000);
		}
		return mViewInCallAudio;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// mTimerTaskInCall.cancel();
		// CallPeriodTimer.cancel();
		// CallPeriodTimer = null;
	}

	@Override
	public void headsetOn() {
		if (mAudioSpeaker != null) {
			mAudioSpeaker.setBackgroundResource(R.drawable.speaker_up);
		}
	}

	@Override
	public void headsetOff() {
		if (mAudioSpeaker != null) {
			mAudioSpeaker.setBackgroundResource(R.drawable.speaker_down);
		}
	}

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.audio_hangup) {
				SystemVarTools.sleep(200);
				serviceAV.hangUpCall();
				mActivity.back();
			} else if (id == R.id.back) {
				mActivity.back();
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
	// private final TimerTask mTimerTaskInCall = new TimerTask() {
	// @Override
	// public void run() {
	// if (mHandler != null) {
	// mHandler.obtainMessage(ServiceAV.CALL_PERIOD_REFRASH).sendToTarget();
	// }
	// }
	// };

}
