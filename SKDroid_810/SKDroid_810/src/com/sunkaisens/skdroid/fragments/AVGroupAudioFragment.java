package com.sunkaisens.skdroid.fragments;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.BaseScreen;
import com.sunkaisens.skdroid.Screens.IBaseScreen;
import com.sunkaisens.skdroid.Screens.ScreenAV;
import com.sunkaisens.skdroid.Services.IServiceScreen;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.GroupUsersAdapter;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.model.BlockQueueModel;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVGroupAudioFragment extends BaseFragment {

	private final static String TAG = AVGroupAudioFragment.class
			.getCanonicalName();

	private Context mContext;
	private BaseScreen mBaseScreen;
	private ServiceAV mServiceAV;

	private boolean isInit = false;
	private View mViewInGroupCallAudio;
	private ImageView mHangup;
	private ImageView mHide;
	private ImageView mPtt;
	private ImageView mLight;

	private TextView mOrgName;
	private TextView mUsersSum;
	private TextView mDuration;
	private TextView mCurrPttUser;

	private GridView mUsers;

	private ModelContact org;

	private List<ModelContact> mOnlineUsers = new ArrayList<ModelContact>();
	private GroupPTTCall mPttCall;
	private String pttName;

	private NgnTimer CallPeriodTimer = new NgnTimer();
	private NgnTimer mPttTimerTmp;

	private MediaPlayer mPttPlayer1;
	private MediaPlayer mPttPlayer3;

	private DisplayMetrics dm;

	private BlockQueueModel mBlockQueueModel = new BlockQueueModel();
	// 标记ptt键是否按下
	private boolean mPttTouched = false;

	public AVGroupAudioFragment() {
		new Thread(mOnlineTask).start();
	}

	/**
	 * 初始化核心参数
	 * 
	 * @param service
	 * @return
	 */
	public boolean init(ServiceAV service) {
		if (service == null)
			return false;

		mServiceAV = service;
		isInit = true;

		return true;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		MyLog.d(TAG, "onAttach()");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.i(TAG, "onCreate()");
		mContext = NgnApplication.getContext();
		mBaseScreen = (BaseScreen) getActivity();
		dm = new DisplayMetrics();

		if (mPttCall == null) {
			mPttCall = new GroupPTTCall();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		MyLog.i(TAG, "onCreateView()");
		if (!isInit)
			return null;

		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		String remoteparty = mServiceAV.getRemotePartyUri();
		MyLog.d(TAG, "AVGroupAudioFragment  remote:" + remoteparty);
		org = SystemVarTools.createContactFromRemoteParty(remoteparty);
		ModelContact myself = SystemVarTools
				.createContactFromPhoneNumber(GlobalVar.mLocalNum);
		if (myself != null) {
			// OnLineHandler olh = new OnLineHandler();
			// olh.execute(GlobalVar.mLocalNum);
			mBlockQueueModel.putUser(GlobalVar.mLocalNum);
		}
		MyLog.d(TAG, "AVGroupAudioFragment  remoteParty:" + remoteparty);

		if (mViewInGroupCallAudio == null) {
			mViewInGroupCallAudio = LayoutInflater.from(mContext).inflate(
					R.layout.view_group_incall_audio, null);
		}
		
		ImageView ivMingOrMi = (ImageView) mViewInGroupCallAudio
				.findViewById(R.id.iv_ming_or_mi_group_audio);
		if (GlobalVar.isSecuriteCardExist) {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_mi);
		} else {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_ming);
		}
		
		mOrgName = (TextView) mViewInGroupCallAudio
				.findViewById(R.id.ga_org_name);

		mHangup = (ImageView) mViewInGroupCallAudio
				.findViewById(R.id.ga_hangup);

		mPtt = (ImageView) mViewInGroupCallAudio.findViewById(R.id.ga_ptt);

		mDuration = (TextView) mViewInGroupCallAudio
				.findViewById(R.id.ga_duration);

		mCurrPttUser = (TextView) mViewInGroupCallAudio
				.findViewById(R.id.ptt_user_name);

		mLight = (ImageView) mViewInGroupCallAudio.findViewById(R.id.ga_light);

		mUsers = (GridView) mViewInGroupCallAudio.findViewById(R.id.ga_users);

		mUsersSum = (TextView) mViewInGroupCallAudio
				.findViewById(R.id.ga_users_sum_num);

		mPtt.setEnabled(true);

		mHide = (ImageView) mViewInGroupCallAudio.findViewById(R.id.ga_hide);
		mHide.setOnClickListener(myOnClickListener);

		mHangup.setOnClickListener(myOnClickListener);

		int cols = (int) (dm.widthPixels / (70 * dm.density));
		mUsers.setNumColumns(cols);

		GroupUsersAdapter gua = new GroupUsersAdapter(mContext);
		mUsers.setAdapter(gua);
		gua.setUsers(mOnlineUsers);

		mServiceAV.setOnPause(true);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) { // 横屏
			if (dm.heightPixels < 640) {
				try {
					// LayoutParams lpParams = new
					// LayoutParams((int)(100*dm.density),
					// (int)(100*dm.density));
					LayoutParams lpParams = mPtt.getLayoutParams();
					lpParams.height = (int) (100 * dm.density);
					lpParams.width = (int) (100 * dm.density);
					mPtt.setLayoutParams(lpParams);
					Log.d(TAG, "");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "landscape");
		}

		mPtt.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (mPttCall.getState() == PTTState.REJECTED) {
					MyLog.d(TAG, "PTT键暂不可点.");
					return false;
				}

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Log.d(TAG, "PTT MotionEvent.ACTION_DOWN");

					if (GlobalVar.PTTHasLongClickedDown) {
						Log.d(TAG, "ptt - PTTHasLongClickedDown"
								+ GlobalVar.PTTHasLongClickedDown);
						break;
					} else {
						Log.d(TAG, "ptt - PTTHasLongClickedDown"
								+ GlobalVar.PTTHasLongClickedDown);
						GlobalVar.PTTHasLongClickedDown = true;
						mPttTouched = true;
					}

					mPttTimerTmp = new NgnTimer();
					mPttTimerTmp.schedule(new TimerTask() {

						@Override
						public void run() {
							synchronized (mServiceAV) {

								if (GlobalVar.bADHocMode == true) {
									mServiceAV.sendPTTRequestCmd();
								} else {
									mServiceAV.sendPTTRequestInfoMsg();
									mPttCall.setState(PTTState.REQUESTING);
								}

								mPttTimerTmp = null;

								if (mPttPlayer1 != null) {
									mPttPlayer1.release();
								}

							}

						}
					}, 500);

					mPtt.setBackgroundResource(R.drawable.ptt_down);
					mLight.setImageResource(R.drawable.request);

					try {
						mPttPlayer1 = MediaPlayer.create(mContext,
								R.raw.talkroom_press);
						mPttPlayer1.start();
					} catch (IllegalStateException e) {
						mPttPlayer1.release();
						mPttPlayer1 = MediaPlayer.create(mContext,
								R.raw.talkroom_press);
						e.printStackTrace();
					}

					if (mPttCall == null) {
						mPttCall = new GroupPTTCall();
					}
					break;

				case MotionEvent.ACTION_UP:

					Log.d(TAG, "PTT MotionEvent.ACTION_UP");
					if (mPttTouched) {
						Log.d(TAG, "ptt - mPttTouched = " + mPttTouched
								+ ", GlobalVar.PTTHasLongClickedDown = "
								+ GlobalVar.PTTHasLongClickedDown);
						GlobalVar.PTTHasLongClickedDown = false;
						mPttTouched = false;
					} else {
						Log.d(TAG, "ptt - mPttTouched = " + mPttTouched
								+ ", GlobalVar.PTTHasLongClickedDown = "
								+ GlobalVar.PTTHasLongClickedDown);
						return false;
					}

					if (mPttTimerTmp == null) {
						if (GlobalVar.bADHocMode == true) {
							mServiceAV.sendPTTReleaseCmd();
						} else {
							mServiceAV.sendPTTReleaseInfoMsg();
						}
					} else {
						mPttTimerTmp.cancel();
						mPttTimerTmp.purge();
						mPttTimerTmp = null;
					}

					mPtt.setBackgroundResource(R.drawable.ptt_up);
					mLight.setImageResource(R.drawable.idle);

					break;

				default:
					break;
				}

				return false;
			}
		});

		mOrgName.setText(org.name);
		mUsersSum.setText("" + mOnlineUsers.size());
		MyLog.d(TAG,
				"OrgName=" + org.name + "  OnlineNums=" + mOnlineUsers.size());
		if (mServiceAV.getAVSession() != null) {
			MyLog.d(TAG, "CallState=" + mServiceAV.getAVSession().getState());
			if (mServiceAV.getAVSession().getState().equals(InviteState.INCALL)) {

				CallPeriodTimer.schedule(mTimerTaskInCall, 0, 1000);
			} else {
				mDuration.setText(getActivity().getString(R.string.in_calling));
			}
		}

		mServiceAV.setSpeakerphoneOn(true);

		return mViewInGroupCallAudio;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume()");
		if (mServiceAV.getAVSession() != null) {
			MyLog.d(TAG, "CallState=" + mServiceAV.getAVSession().getState());
			if (mServiceAV.getAVSession().getState().equals(InviteState.INCALL)) {

				CallPeriodTimer.schedule(mTimerTaskInCall, 0, 1000);
			} else {
				mDuration
						.setText(getActivity().getString(R.string.in_calling2));
			}
		} else if (mServiceAV.getMediaSession() != null) {
			Log.d(TAG, "sessionState = "
					+ mServiceAV.getMediaSession().getSessionState());
			if (mServiceAV.getMediaSession().isConnected())
				CallPeriodTimer.schedule(mTimerTaskInCall, 0, 1000);
			else
				mDuration.setText("InCall。。。");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MyLog.i(TAG, "onPause()");
		GlobalVar.PTTHasLongClickedDown = false;
		if (GlobalVar.bADHocMode == true) {
			mServiceAV.sendPTTReleaseCmd();
		} else {
			mServiceAV.sendPTTReleaseInfoMsg();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		MyLog.i(TAG, "onStop()");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MyLog.i(TAG, "onDestroyView()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MyLog.i(TAG, "onDestroy()");

	}

	@Override
	public void onDetach() {
		super.onDetach();
		MyLog.i(TAG, "onDetach()");
	}

	private final static int REFRASH_ONLINE_USERS = 1001;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case ServiceAV.CALL_PERIOD_REFRASH: // 刷新通话时长计时器
				if (mDuration != null) {
					long period = new Date().getTime()
							- mServiceAV.getStartTime();
					mDuration.setText(SystemVarTools.mCallPeriodFormat(period));
				}
				break;
			case REFRASH_ONLINE_USERS:
				if (mUsers != null && mUsers.getAdapter() != null) {
					((GroupUsersAdapter) mUsers.getAdapter())
							.notifyDataSetChanged();
				}

				if (mUsersSum != null)
					mUsersSum.setText("" + mOnlineUsers.size());
				break;
			default:
				break;

			}
		}
	};

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == mHangup.getId()) {
				Builder builder = new Builder(mBaseScreen);
				builder.setTitle(getActivity().getString(
						R.string.quit_audio_group_calling));
				builder.setMessage(getActivity().getString(
						R.string.click_certain_quit_audio_group_calling));
				builder.setPositiveButton(R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (mServiceAV.getMediaSession() != null) {
									Log.d(TAG, "Click Hungup()...");
									mServiceAV.hangUpCall();
								} else if (mPttCall != null
										&& (mPttCall.getState() == PTTState.GRANTED || mPttCall
												.getState() == PTTState.VIDEOSUB_TURNON))
									mServiceAV.sendPTTReleaseInfoMsg();

								mServiceAV.hangUpCall();
								// gzc 视频组呼挂断时立刻结束视频输出线程
								if (mServiceAV.isConnected()) {
									NgnAVSession.mSendFrameThread = false;
									Log.d(TAG,
											"VideoSend 点击组呼挂断按钮  mSendFrameThread="
													+ NgnAVSession.mSendFrameThread);
								}
								Main.isFirstPTT_onKeyDown = true;
								IServiceScreen ss = ((Engine) Engine
										.getInstance()).getScreenService();
								if (ss != null) {
									IBaseScreen baseScreen = ss
											.getCurrentScreen();
									if (baseScreen != null
											&& baseScreen instanceof ScreenAV) {
										mBaseScreen.back();
									}
								}

							}
						});
				builder.setNegativeButton(R.string.cancel,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();

			} else if (id == mHide.getId()) {
				mBaseScreen.back();

			}
		}
	};

	// private int InquireTime = 0;

	/**
	 * 通话时长计时器
	 */
	private final TimerTask mTimerTaskInCall = new TimerTask() {
		@Override
		public void run() {
			if (mHandler != null) {
				mHandler.obtainMessage(ServiceAV.CALL_PERIOD_REFRASH)
						.sendToTarget();
				/*
				 * if(InquireTime == 10){ mServiceAV.sendPTTInquireInfoMsg();
				 * InquireTime = 0; }else { InquireTime ++; }
				 */
			}
		}
	};

	/**
	 * 处理组呼INFO请求
	 * 
	 * @param infoContent
	 */
	public void handleRequestPTTInfoMsg(byte[] infoContent) {
		MyLog.d(TAG, "语音组呼      handleRequestPTTInfoMsg()");

		if (mPttCall == null) {
			mPttCall = new GroupPTTCall();
		}
		PTTInfoMsg msg = new PTTInfoMsg(infoContent);
		mPttCall.handlePTTInfoMsg(msg);

		mServiceAV.setSpeakerphoneOn(true);

		MyLog.d(TAG, "mPttCall.State = " + mPttCall.getState());
		if (mPttCall.getState() == PTTState.REJECTED) {
			mServiceAV.setConsumerOnPause(false);
			mServiceAV.setProducerOnPause(true);

		} else if (mPttCall.getState() == PTTState.GRANTED) {
			mServiceAV.setConsumerOnPause(true);
			mServiceAV.setProducerOnPause(false);
		}else if (mPttCall.getState() == PTTState.RELEASE_SUCCESS ||
				mPttCall.getState() == PTTState.RELEASED){
			mServiceAV.setConsumerOnPause(true);
			mServiceAV.setProducerOnPause(true);
		}

		Engine.getInstance();

		switch (mPttCall.getState()) {
		case NONE:
		case REQUESTING:
			break;
		case GRANTED: // 当前用户PTT抢占成功
			MyLog.d(TAG, "GRANTED");
			ModelContact myself = SystemVarTools
					.createContactFromPhoneNumber(GlobalVar.mLocalNum);
			if (myself != null) {
				mCurrPttUser.setText(myself.name);
			}

			mPtt.setClickable(true);

			MyLog.d(TAG, "mPtt.setEnabled(true)");
			MyLog.d(TAG, "mPtt.setClickable(true)");

			ServiceAV.isPTTRejected = false;
			mPttPlayer3 = MediaPlayer.create(mContext, R.raw.talkroom_begin);
			mPttPlayer3.start();

			// PTT按钮为红色表示PTT抢占成功
			mPtt.setBackgroundResource(R.drawable.ptt_down);
			mLight.setImageResource(R.drawable.grant);

			break;
		case RELEASE_SUCCESS: // 当前用户PTT释放成功
			MyLog.d(TAG, "RELEASE_SUCCESS");

			ServiceAV.isPTTRejected = false;
			mPtt.setClickable(true);
			MyLog.d(TAG, "mPtt.setEnabled(true)");
			MyLog.d(TAG, "mPtt.setClickable(true)");

			// PTT按钮为绿色表示目前没人抢占PTT
			mPtt.setBackgroundResource(R.drawable.ptt_up);
			mLight.setImageResource(R.drawable.idle);
			mCurrPttUser.setText("");
			break;
		case RELEASED: // 非当前用户释放ptt
			MyLog.d(TAG, "RELEASED");

			ServiceAV.isPTTRejected = false;
			mPtt.setClickable(true);
			MyLog.d(TAG, "mPtt.setEnabled(true)");
			MyLog.d(TAG, "mPtt.setClickable(true)");

			// green
			// mPtt.setTextColor(getResources().getColor(R.color.color_green));
			mPtt.setBackgroundResource(R.drawable.ptt_up); // 绿色
			mLight.setImageResource(R.drawable.idle);
			mServiceAV.sendPTTReleaseAckInfoMsg();
			mCurrPttUser.setText("");
			break;
		case REJECTED: // 非当前用户ptt抢占成功
			MyLog.d(TAG, "REJECTED");

			mPtt.setClickable(false);

			MyLog.d(TAG, "mPtt.setEnabled(false)");
			MyLog.d(TAG, "mPtt.setClickable(false)");

			ServiceAV.isPTTRejected = true;

			// PTT按钮为白色表示PTT已经被抢占，目前无法抢占
			mPtt.setBackgroundResource(R.drawable.ptt_down);
			mLight.setImageResource(R.drawable.reject);
			mServiceAV.setOnResetJB();

			pttName = msg.getPTTPhoneNumber();
			ModelContact pttUser = SystemVarTools
					.createContactFromPhoneNumber(pttName);
			if (pttUser != null) {
				mCurrPttUser.setText(pttUser.name);
			}
			// mCurrPttUser.setText(pttName);
			// 只有视频组呼才存在切换视频的情况
			if (mServiceAV.getAVSession().isGroupVideoCall()) {

				// 如果当前正在订阅某人的视频，切换视频前要先取消订阅
				if (mPttCall.isSubscribe()) {
					mServiceAV.sendPTTCancelInfoMsg(mPttCall
							.getCurrentSubscribeName());

					MyLog.d(TAG,
							"语音组呼   取消当前订阅的视频: "
									+ mPttCall.getCurrentSubscribeName());
					mPttCall.setIsSubscribe(false);
				} else {
					// 订阅抢占PTT那个人的视频
					mServiceAV.sendPTTSubscribeInfoMsg(pttName);
					mPttCall.setCurrentSubscribeName(pttName);
					mPttCall.setIsSubscribe(true);
					MyLog.d(TAG, "mPttCall.getState() = REGECTED and 切换订阅视频： "
							+ pttName);
				}
			}

			break;
		case ALAVE:
			MyLog.d(TAG, "ALAVE");
			break;
		case ONLINE:
			MyLog.d(TAG, "语音组呼  ONLINE  用户:" + msg.getPTTPhoneNumber());

			String onLineUser = msg.getPTTPhoneNumber();
			mBlockQueueModel.putUser(onLineUser);
			// OnLineHandler onLineHandler = new OnLineHandler();
			// onLineHandler.execute(onLineUser);

			break;
		case OFFLINE:
			MyLog.d(TAG, "语音组呼   OFFLINE:" + msg.getPTTPhoneNumber());

			String offLineUser = msg.getPTTPhoneNumber();
			int index = -1;
			for (int loc = 0; loc < mOnlineUsers.size(); loc++) {
				if (mOnlineUsers.get(loc).mobileNo.equals(offLineUser)) {
					index = loc;
					break;
				}
			}
			if (index != -1 && index != mOnlineUsers.size()) {
				mOnlineUsers.remove(index);
				if (mUsers != null && mUsers.getAdapter() != null) {
					((GroupUsersAdapter) mUsers.getAdapter())
							.notifyDataSetChanged();
				}
				mUsersSum.setText("" + mOnlineUsers.size());
			}
			break;
		case CALSUB:
			MyLog.d(TAG, "CALSUB");
			mServiceAV.sendPTTCancelAckInfoMsg();
			break;
		case CANCEL_SUCCESS:

			MyLog.d(TAG, "CANCEL_SUCCESS");

			// 订阅抢占PTT那个人的视频
			mServiceAV.sendPTTSubscribeInfoMsg(pttName);
			mPttCall.setCurrentSubscribeName(pttName);
			mPttCall.setIsSubscribe(true);
			MyLog.d(TAG, "mPttCall.getState() = CANCEL_SUCCESS and 切换订阅视频： "
					+ pttName);
			break;

		default:
			break;
		}

	}

	/* 处理 PTT推占事件 */
	public void handleRequestPTT(PTTState mState) {
		Log.d(TAG, "PTTState = " + mState);
		switch (mState) {
		case GRANTED:
			// 当前用户PTT抢占成功
			MyLog.d(TAG, "GRANTED");
			mPttPlayer3 = MediaPlayer.create(mContext, R.raw.talkroom_begin);

			mPttPlayer3.start();

			// PTT按钮为红色表示PTT抢占成功
			mPtt.setBackgroundResource(R.drawable.ptt_down);
			mLight.setImageResource(R.drawable.grant);
			mServiceAV.setConsumerOnPause(true);
			mServiceAV.setProducerOnPause(false);
			// mServiceAV.setSpeakerphoneOn(false);
			break;
		case REJECTED:
			mServiceAV.setConsumerOnPause(false);
			mServiceAV.setProducerOnPause(true);
			// mServiceAV.setSpeakerphoneOn(true);
			MyLog.d(TAG, "REJECTED");
			mServiceAV.setOnResetJB();

			if (mPtt == null)
				return;
			// PTT按钮为白色表示PTT已经被抢占，目前无法抢占
			mPtt.setBackgroundResource(R.drawable.ptt_down);
			mLight.setImageResource(R.drawable.reject);
			mPtt.setClickable(false);

			break;

		case RELEASE_SUCCESS:
			MyLog.d(TAG, "RELEASE_SUCCESS");
			// PTT按钮为绿色表示目前没人抢占PTT
			if (mPttPlayer3 != null) {
				mPttPlayer3.stop();
				mPttPlayer3.release();
				mPttPlayer3 = null;
			}
			// mServiceAV.setConsumerOnPause(true);
			mServiceAV.setConsumerOnPause(true);
			mServiceAV.setProducerOnPause(true);
			// mServiceAV.setSpeakerphoneOn(false);

			if (mPtt == null)
				return;
			mPtt.setClickable(true);
			mPtt.setBackgroundResource(R.drawable.ptt_up);
			mLight.setImageResource(R.drawable.idle);

			break;
		default:
			break;
		}
	}

	class OnLineHandler extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			MyLog.d(TAG, "doInBackground(" + params[0] + ")");
			String onLineUser = mBlockQueueModel.getMessageSend();
			while (onLineUser != null) {
				int index = -1;
				synchronized (mOnlineUsers) {
					for (int loc = 0; loc < mOnlineUsers.size(); loc++) {
						if (mOnlineUsers.get(loc).mobileNo.equals(onLineUser)) {
							index = loc;
							break;
						}
					}
					MyLog.d(TAG, "index=" + index + "  OnlineNums"
							+ mOnlineUsers.size());
					if (index == -1) {
						ModelContact mc = SystemVarTools
								.createContactFromPhoneNumber(onLineUser);
						if (mc == null) {
							mc = new ModelContact();
							mc.name = onLineUser;
						}
						mc.mobileNo = onLineUser;
						mOnlineUsers.add(mc);
					}
				}
				mHandler.sendEmptyMessage(REFRASH_ONLINE_USERS);
				try {
					onLineUser = mBlockQueueModel.getUser();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
		}

	}

	private Runnable mOnlineTask = new Runnable() {

		@Override
		public void run() {
			MyLog.d(TAG, "mOnlineTask start...");
			String onLineUser = null;
			try {
				onLineUser = mBlockQueueModel.getUser();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			while (onLineUser != null) {
				MyLog.d(TAG, "onLineUser = " + onLineUser);
				int index = -1;
				synchronized (mOnlineUsers) {
					for (int loc = 0; loc < mOnlineUsers.size(); loc++) {
						if (mOnlineUsers.get(loc).mobileNo.equals(onLineUser)) {
							index = loc;
							break;
						}
					}
					MyLog.d(TAG, "index=" + index + "  OnlineNums"
							+ mOnlineUsers.size());
					if (index == -1) {
						ModelContact mc = SystemVarTools
								.createContactFromPhoneNumber(onLineUser);
						if (mc == null) {
							mc = new ModelContact();
							mc.name = onLineUser;
						}
						mc.mobileNo = onLineUser;
						mOnlineUsers.add(mc);
					}
				}
				mHandler.sendEmptyMessage(REFRASH_ONLINE_USERS);
				try {
					onLineUser = mBlockQueueModel.getUser();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			MyLog.d(TAG, "mOnlineTask stop.");
		}
	};

}
