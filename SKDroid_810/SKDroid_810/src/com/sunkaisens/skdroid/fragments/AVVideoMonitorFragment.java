package com.sunkaisens.skdroid.fragments;

import java.util.Date;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.media.NgnCameraProducer;
import org.doubango.ngn.sip.NgnAVSession;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.BaseScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVVideoMonitorFragment extends BaseFragment {

	private static String TAG = AVVideoMonitorFragment.class.getCanonicalName();
	private static String tip = AVVideoMonitorFragment.class.getSimpleName();

	private View mViewInCallVideo;

	private FrameLayout mViewLocalVideoPreview;
	private FrameLayout mViewRemoteVideoPreview;

	private ImageView mGoback;
	private ImageView mSwitchCamera;
	private ImageView mVideoHangUpBt;
	private TextView mTvDuration;

	private RelativeLayout block_bottom;

	ServiceAV serviceAV = null;

	private Context mContext;

	private BaseScreen mbaseScreen;

	final int MSG_IS_VIDEO_REFRESH = 1000;

	private Handler mHandler;

	private boolean isInit = false;

	private boolean isSpeaker = false;

	private NgnTimer CallPeriodTimer = new NgnTimer();

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

		mbaseScreen = (BaseScreen) getActivity();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MyLog.d(TAG, tip + ":onCreateView()");

		if (!isInit)
			return null;

		if (mViewInCallVideo == null) {

			LayoutInflater mInflater = LayoutInflater.from(mContext);

			mViewInCallVideo = mInflater.inflate(
					R.layout.view_call_incall_videomonitor, null);
			
			ImageView ivMingOrMi = (ImageView) mViewInCallVideo.findViewById(R.id.iv_ming_or_mi_call_remote_video);
			if (GlobalVar.isSecuriteCardExist) {
				ivMingOrMi.setBackgroundResource(R.drawable.ic_mi);
			} else {
				ivMingOrMi.setBackgroundResource(R.drawable.ic_ming);
			}

			block_bottom = (RelativeLayout) mViewInCallVideo
					.findViewById(R.id.block_bottom);

			mViewLocalVideoPreview = (FrameLayout) mViewInCallVideo
					.findViewById(R.id.sv_local_view);
			mViewRemoteVideoPreview = (FrameLayout) mViewInCallVideo
					.findViewById(R.id.sv_remote_video);

			mGoback = (ImageView) mViewInCallVideo.findViewById(R.id.goback);
			mVideoHangUpBt = (ImageView) mViewInCallVideo
					.findViewById(R.id.video_hangup);
			mSwitchCamera = (ImageView) mViewInCallVideo
					.findViewById(R.id.video_switch_camera);
			mTvDuration = (TextView) mViewInCallVideo
					.findViewById(R.id.screen_video_duration);

			mVideoHangUpBt.setOnClickListener(myOnClickListener);
			mSwitchCamera.setOnClickListener(myOnClickListener);
			mGoback.setOnClickListener(myOnClickListener);
			mViewRemoteVideoPreview.setOnClickListener(myOnClickListener);
			mViewLocalVideoPreview.setOnClickListener(myOnClickListener);

			block_bottom.bringToFront();
		}

		View localView = null;
		View remoteView = null;

		switch (serviceAV.getAVSession().getSessionType()) {
		case SessionType.VideoMonitor:
		case SessionType.GroupVideoMonitor:
		case SessionType.VideoUaMonitor:
		case SessionType.VideoSurveilMonitor:
			MyLog.d(TAG,
					"SignalVideo  VideoType : GroupVideoMonitor/VideoMonitor");

			mViewLocalVideoPreview = (FrameLayout) mViewInCallVideo
					.findViewById(R.id.sv_remote_video);
			mViewRemoteVideoPreview = (FrameLayout) mViewInCallVideo
					.findViewById(R.id.sv_local_view);
			mViewRemoteVideoPreview.setVisibility(View.GONE);

			NgnCameraProducer.useRearCamera(); // 设置使用后置摄像头

			localView = ServiceAV.createLocalPreview(serviceAV, true);
			mViewLocalVideoPreview.removeAllViews();
			mViewLocalVideoPreview.addView(localView);
			mViewLocalVideoPreview.bringChildToFront(localView);

			block_bottom.bringToFront();

			break;
		case SessionType.VideoTransmit:
			MyLog.d(TAG, "SignalVideo  VideoType : VideoTransmit");
			mViewLocalVideoPreview.setVisibility(View.GONE);

			remoteView = ServiceAV.createRemoteVideoPreview(serviceAV);
			mViewRemoteVideoPreview.removeAllViews();
			mViewRemoteVideoPreview.addView(remoteView);
			break;
		default: // 视频通话
			MyLog.d(TAG, "SignalVideo  VideoType : Video");

			break;
		}

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

		CallPeriodTimer.schedule(mTimerTaskInCall, 0, 1000);

		serviceAV.getAVSession().setSpeakerphoneOn(false);

		return mViewInCallVideo;
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
				mViewLocalVideoPreview.removeAllViews();
				mbaseScreen.back();
			} else if (id == R.id.goback) {
				mbaseScreen.back();
			} else if (mViewLocalVideoPreview != null
					&& id == mViewLocalVideoPreview.getId()) {
				changeVisible();

			} else if (mViewRemoteVideoPreview != null
					&& id == mViewRemoteVideoPreview.getId()) {
				changeVisible();

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
		mTimerTaskInCall.cancel();
		CallPeriodTimer.cancel();
		CallPeriodTimer = null;

	}

	/*
	 * private void cancelBlankPacket() { if (mTimerBlankPacket != null) {
	 * mTimerBlankPacket.cancel(); mTimerBlankPacketIsCanceled = true;
	 * mCountBlankPacket = 0; } }
	 */

	private void changeVisible() {
		if (block_bottom.getVisibility() == View.VISIBLE) {
			block_bottom.setVisibility(View.GONE);
		} else {
			block_bottom.setVisibility(View.VISIBLE);
			block_bottom.bringToFront();
		}
		if (mGoback.getVisibility() == View.VISIBLE) {
			mGoback.setVisibility(View.GONE);
		} else {
			mGoback.setVisibility(View.VISIBLE);
			mGoback.bringToFront();
		}
	}

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

	public ServiceAV getServiceAV() {
		return serviceAV;
	}

}
