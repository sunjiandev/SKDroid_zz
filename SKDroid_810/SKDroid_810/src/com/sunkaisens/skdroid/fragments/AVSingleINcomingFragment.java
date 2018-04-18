package com.sunkaisens.skdroid.fragments;

import org.doubango.ngn.NgnApplication;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.os.Bundle;
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
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVSingleINcomingFragment extends BaseFragment {

	private final static String TAG = AVSingleAudioFragment.class
			.getCanonicalName();

	private Context mContext;

	private BaseScreen mBaseScreen;

	private ServiceAV serviceAV;

	private ModelContact remoteContact;

	private boolean isInit = false;

	private View mViewInCallAudio;
	private TextView mInfo;
	private TextView mRemoteName;
	private ImageView mHangup;
	private ImageView mPickeup;
	private ImageView mRemoteIcon;

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
		mBaseScreen = (BaseScreen) getActivity();
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

		int mSessionType = serviceAV.getAVSession().getSessionType();

		if (mViewInCallAudio == null) {
			if (mSessionType == SessionType.AudioCall) {
				mViewInCallAudio = LayoutInflater.from(mContext).inflate(
						R.layout.view_call_audio_pick, null);
				MyLog.d(TAG, "Single AudioCall  remoteParty:" + remoteParty);
			} else {
				mViewInCallAudio = LayoutInflater.from(mContext).inflate(
						R.layout.view_call_video_pick, null);
				MyLog.d(TAG, "Single VideoCall  remoteParty:" + remoteParty);
			}
		}
		
		ImageView ivMingOrMi = (ImageView) mViewInCallAudio.findViewById(R.id.iv_ming_or_mi_call_video);
		if (GlobalVar.isSecuriteCardExist) {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_mi);
		} else {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_ming);
		}
		
		mInfo = (TextView) mViewInCallAudio.findViewById(R.id.info);

		mRemoteName = (TextView) mViewInCallAudio.findViewById(R.id.name);

		mHangup = (ImageView) mViewInCallAudio.findViewById(R.id.hangup);

		mPickeup = (ImageView) mViewInCallAudio.findViewById(R.id.pick_up);

		mRemoteIcon = (ImageView) mViewInCallAudio.findViewById(R.id.icon);

		SystemVarTools.showicon(mRemoteIcon, remoteContact, mContext);

		mHangup.setOnClickListener(myOnClickListener);
		mPickeup.setOnClickListener(myOnClickListener);

		Log.d(TAG, "mRemotePartyDisplayName = " + remoteContact.name);
		mRemoteName.setText(remoteContact.name);
		if (mSessionType == SessionType.VideoTransmit) {
			mInfo.setText(mContext.getResources().getString(
					R.string.string_call_incoming_transmit));
		} else if (mSessionType == SessionType.VideoMonitor
				|| mSessionType == SessionType.GroupVideoMonitor
				|| mSessionType == SessionType.VideoSurveilMonitor) {
			mInfo.setText(mContext.getResources().getString(
					R.string.string_call_incoming_monitor));
		}
		// remote_icon.setImageResource(SystemVarTools.getThumbID(imageid));

		return mViewInCallAudio;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == mHangup.getId()) {
				serviceAV.hangUpCall();
				mBaseScreen.back();
			} else if (id == mPickeup.getId()) {
				serviceAV.acceptCall();
				//serviceAV.getAVSession().sendTransfer("test", "19800005005");
			}
		}
	};

}
