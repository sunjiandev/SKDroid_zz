package com.sunkaisens.skdroid.fragments;

import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.BaseScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVSingleVideoTryingFragment extends BaseFragment {

	private static String TAG = AVSingleVideoFragment.class.getCanonicalName();
	private static String tip = AVSingleVideoFragment.class.getSimpleName();

	private View mViewInCallVideo;

	private ImageView goback;
	private ImageView mVideoHangUpBt;

	private RelativeLayout block_bottom;

	private TextView remote_contact;

	private ModelContact remoteContact;

	ServiceAV serviceAV = null;

	private Context mContext;

	private BaseScreen mBaseScreen;

	final int MSG_IS_VIDEO_REFRESH = 1000;

	private Handler mHandler;

	private boolean isInit = false;

	// private NgnTimer CallPeriodTimer = new NgnTimer();

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

		MyLog.d(TAG, tip + ":onCreateView()");

		if (!isInit)
			return null;

		String remoteParty = serviceAV.getAVSession().getRemotePartyUri();
		remoteContact = SystemVarTools
				.createContactFromRemoteParty(remoteParty);
		if (remoteContact.name == null) {
			remoteContact.name = remoteParty;
		}

		if (mViewInCallVideo == null) {

			LayoutInflater mInflater = LayoutInflater.from(mContext);

			mViewInCallVideo = mInflater.inflate(
					R.layout.view_call_video_trying, null);

			ImageView ivMingOrMi = (ImageView) mViewInCallVideo
					.findViewById(R.id.iv_ming_or_mi_call_video_trying);
			if (GlobalVar.isSecuriteCardExist) {
				ivMingOrMi.setBackgroundResource(R.drawable.ic_mi);
			} else {
				ivMingOrMi.setBackgroundResource(R.drawable.ic_ming);
			}

			block_bottom = (RelativeLayout) mViewInCallVideo
					.findViewById(R.id.block_bottom);

			goback = (ImageView) mViewInCallVideo.findViewById(R.id.goback);
			mVideoHangUpBt = (ImageView) mViewInCallVideo
					.findViewById(R.id.video_hangup);
			remote_contact = (TextView) mViewInCallVideo
					.findViewById(R.id.remote_contact);
			remote_contact.setText(remoteContact.name);

			mVideoHangUpBt.setOnClickListener(myOnClickListener);
			goback.setOnClickListener(myOnClickListener);

			block_bottom.bringToFront();

		}

		serviceAV.getAVSession().setSpeakerphoneOn(true);

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
		return mViewInCallVideo;
	}

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.video_switch_camera) {
				serviceAV.switchCameraFrontOrBack();
			} else if (id == R.id.video_hangup) {
				if (serviceAV.getAVSession().isConnected()) {
					NgnAVSession.mSendFrameThread = false;
					Log.d(TAG, "VideoSend 点击单呼挂断按钮  mSendFrameThread="
							+ NgnAVSession.mSendFrameThread);
				}
				// stopTimerAndTask();
				SystemVarTools.sleep(200);
				serviceAV.hangUpCall();
				mBaseScreen.back();
			} else if (id == R.id.goback) {
				mBaseScreen.back();
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public ServiceAV getServiceAV() {
		return serviceAV;
	}

}
