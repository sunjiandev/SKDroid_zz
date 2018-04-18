package com.sunkaisens.skdroid.fragments;

import org.doubango.ngn.NgnApplication;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.BaseScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.listener.HeadsetListener;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVGroupVideoTryingFragment extends BaseFragment implements
		HeadsetListener {

	private String TAG = AVGroupVideoTryingFragment.class.getCanonicalName();

	private String tip = AVGroupVideoTryingFragment.class.getSimpleName();

	private BaseScreen mScreen;

	private ServiceAV serviceAV;

	private Context mContext;

	private boolean isInit = false;

	private View mViewInGroupCallVideo;

	private FrameLayout gv_LocalVideoPreview_parent;

	private ImageView gv_SwitchCamera;

	private ImageButton gv_ptt;
	private ImageView gv_users_hide_bt;
	private ImageView gv_hangup;

	private ImageView gv_hide;

	private TextView gv_duration;
	private TextView gv_sub_name;

	protected boolean mPttClickable = true;

	/**
	 * 初始化核心参数
	 * 
	 * @param service
	 * @return
	 */
	public boolean init(ServiceAV service) {
		if (service == null) {
			return false;
		}
		serviceAV = service;
		isInit = true;
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mScreen = (BaseScreen) getActivity();
		mContext = NgnApplication.getContext();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (!isInit)
			return null;

		Log.d(TAG, "loadGroupTryingVideoView()");

		if (mViewInGroupCallVideo == null) {

			mViewInGroupCallVideo = LayoutInflater.from(mContext).inflate(
					R.layout.view_group_incall_video, null);
			
			ImageView ivMingOrMi = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.iv_ming_or_mi_group_video);
			if (GlobalVar.isSecuriteCardExist) {
				ivMingOrMi.setBackgroundResource(R.drawable.ic_mi);
			} else {
				ivMingOrMi.setBackgroundResource(R.drawable.ic_ming);
			}
			gv_LocalVideoPreview_parent = (FrameLayout) mViewInGroupCallVideo
					.findViewById(R.id.view_group_call_incall_video_FrameLayout_local_video_layout);

			gv_hangup = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_hangup);

			gv_ptt = (ImageButton) mViewInGroupCallVideo
					.findViewById(R.id.gv_ptt);
			gv_SwitchCamera = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_switch_camera);
			gv_duration = (TextView) mViewInGroupCallVideo
					.findViewById(R.id.gv_duration);
			gv_sub_name = (TextView) mViewInGroupCallVideo
					.findViewById(R.id.gv_sub_name);
		}

		gv_ptt.bringToFront();

		gv_users_hide_bt = (ImageView) mViewInGroupCallVideo
				.findViewById(R.id.gv_users_but);
		gv_users_hide_bt.setClickable(false);

		// back
		gv_hide = (ImageView) mViewInGroupCallVideo.findViewById(R.id.gv_hide);
		gv_hide.setOnClickListener(myOnClickListener);
		//

		gv_hangup.setOnClickListener(myOnClickListener);

		gv_sub_name.setText(getActivity().getString(R.string.in_calling3));

		gv_SwitchCamera.setClickable(false);
		gv_ptt.setClickable(false);
		gv_LocalVideoPreview_parent.setVisibility(View.GONE);

		if (SKDroid.sks_version == VERSION.NORMAL) {
			android.view.ViewGroup.LayoutParams lp = gv_LocalVideoPreview_parent
					.getLayoutParams();
			lp.height = SystemVarTools.dip2px(mContext, 120);
			lp.width = SystemVarTools.dip2px(mContext, 90);
			gv_LocalVideoPreview_parent.setLayoutParams(lp);
		}

		return mViewInGroupCallVideo;
	}

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == gv_hangup.getId()) {
				serviceAV.hangUpCall();
				mScreen.back();

			} else if (id == gv_hide.getId()) {
				mScreen.back();

			}
		}
	};

	@Override
	public void headsetOn() {

	}

	@Override
	public void headsetOff() {

	}

}
