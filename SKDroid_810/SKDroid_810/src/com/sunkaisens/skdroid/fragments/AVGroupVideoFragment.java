package com.sunkaisens.skdroid.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
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
import com.sunkaisens.skdroid.listener.HeadsetListener;
import com.sunkaisens.skdroid.model.BlockQueueModel;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.util.GlobalVar;

public class AVGroupVideoFragment extends BaseFragment implements
		HeadsetListener {

	private String TAG = AVGroupVideoFragment.class.getCanonicalName();

	private BaseScreen mScreen;

	private ServiceAV serviceAV;

	private Context mContext;

	private boolean isInit = false;

	private View mViewInGroupCallVideo;

	private FrameLayout gv_LocalVideoPreview;

	private FrameLayout gv_LocalVideoPreview_parent;

	private FrameLayout gv_RemoteVideoPreview;

	private LinearLayout gv_users_block;
	private FrameLayout gv_screen_bottom;
	private RelativeLayout gv_top_cmd;

	private ImageView gv_SwitchCamera;
	private TextView gv_duration;
	private TextView gv_org_name;
	private TextView gv_users_sum_num;
	private TextView gv_currPttUser;
	private TextView gv_sub_name;

	private ImageButton gv_ptt;
	private ImageView gv_users_hide_bt;
	private ImageView gv_hangup;

	private ImageView gv_hide;
	private ImageView gv_light;
	private ImageView gv_speaker;

	private GridView gv_users;

	private List<ModelContact> mOnlineUsers = new ArrayList<ModelContact>();
	private ModelContact org;

	protected boolean mPttClickable = true;
	protected boolean isSpeaker = true;

	private NgnTimer mTimerVideoPTTReport = new NgnTimer();
	private NgnTimer CallPeriodTimer = new NgnTimer();
	private NgnTimer mPttTimerTmp = null;

	public static GroupPTTCall mPttCall;
	private PTTState mPttTmp;

	// 将要订阅的用户号码
	private String pttName;

	private MediaPlayer mPttPlayer1;
	private MediaPlayer mPttPlayer3;

	private DisplayMetrics dm;

	// 标记ptt键是否按下
	private boolean mPttTouched = false;

	private NgnTimer mSwitchCameraTimer = new NgnTimer();

	private Thread mThread = null;

	private BlockQueueModel mBlockQueueModel = new BlockQueueModel();

	public AVGroupVideoFragment() {
		mThread = new Thread(mOnlineTask);
		mThread.start();
	}

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
		MyLog.d(TAG, "onCreate()");
		mScreen = (BaseScreen) getActivity();
		mContext = NgnApplication.getContext();
		mPttPlayer1 = MediaPlayer.create(mContext, R.raw.talkroom_press);
		mPttPlayer3 = MediaPlayer.create(mContext, R.raw.talkroom_begin);
		dm = new DisplayMetrics();

		if (mPttCall == null) {
			mPttCall = new GroupPTTCall();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		GlobalVar.PTTHasLongClickedDown = false;
		if (GlobalVar.bADHocMode == true) {
			serviceAV.sendPTTReleaseCmd();
		} else {
			serviceAV.sendPTTReleaseInfoMsg();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (!isInit)
			return null;

		Log.d(TAG, "onCreateView()");

		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		String remoteparty = serviceAV.getAVSession().getRemotePartyUri();
		MyLog.d(TAG, "AVGroupVideoFragment  remote:" + remoteparty);
		org = SystemVarTools.createContactFromRemoteParty(remoteparty);
		// if (!mc.isgroup && mc.parent != null) {
		// MyLog.d(TAG, "语音组呼 组织  parent:" + mc.parent);
		// org = SystemVarTools.getContactFromIndex(mc.parent);
		// }
		// if (org == null) {
		// org = mc;
		// }
		MyLog.d(TAG, "VideoGroupCall  name:" + org.name);

		ModelContact myself = SystemVarTools
				.createContactFromPhoneNumber(GlobalVar.mLocalNum);
		if (myself != null) {
			// int loc = 0;
			// for (; loc < mOnlineUsers.size(); loc++) {
			// if (mOnlineUsers.get(loc).mobileNo.equals(myself)) {
			// break;
			// }
			// }
			// if(loc == mOnlineUsers.size()){
			// mOnlineUsers.add(myself);
			// }else {
			// MyLog.d(TAG, "OnLineUsers has contained myself.");
			// }
			// OnLineHandler olh = new OnLineHandler();
			// olh.execute(GlobalVar.mLocalNum);
			mBlockQueueModel.putUser(GlobalVar.mLocalNum);
		}

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
			
			gv_LocalVideoPreview = (FrameLayout) mViewInGroupCallVideo
					.findViewById(R.id.gv_local_video);
			gv_LocalVideoPreview_parent = (FrameLayout) mViewInGroupCallVideo
					.findViewById(R.id.view_group_call_incall_video_FrameLayout_local_video_layout);

			gv_RemoteVideoPreview = (FrameLayout) mViewInGroupCallVideo
					.findViewById(R.id.gv_remote_video);
			setGv_screen_bottom((FrameLayout) mViewInGroupCallVideo
					.findViewById(R.id.gv_bottom_hide_block));
			gv_top_cmd = (RelativeLayout) mViewInGroupCallVideo
					.findViewById(R.id.gv_top_cmd);

			gv_hangup = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_hangup);

			gv_duration = (TextView) mViewInGroupCallVideo
					.findViewById(R.id.gv_duration);

			gv_ptt = (ImageButton) mViewInGroupCallVideo
					.findViewById(R.id.gv_ptt);
			gv_SwitchCamera = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_switch_camera);
			gv_light = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_light);
			gv_currPttUser = (TextView) mViewInGroupCallVideo
					.findViewById(R.id.ptt_user_name);
			gv_speaker = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_speaker);
			gv_org_name = (TextView) mViewInGroupCallVideo
					.findViewById(R.id.gv_org_name);
			gv_users_sum_num = (TextView) mViewInGroupCallVideo
					.findViewById(R.id.ga_users_sum_num);
			gv_sub_name = (TextView) mViewInGroupCallVideo
					.findViewById(R.id.gv_sub_name);

			gv_users_sum_num.setText("" + mOnlineUsers.size());
			gv_org_name.setText(org.name);

			gv_ptt.bringToFront();

			// 查询显示处于组呼中的用户
			final int windowHeigh = dm.heightPixels;
			final int windowWidth = dm.widthPixels;
			final int dpi = dm.densityDpi;
			final float density = dm.density;

			if (SKDroid.isBh03() || SKDroid.isHaiXin()) {
				android.view.ViewGroup.LayoutParams lp = gv_ptt.getLayoutParams();
				lp.width = (int) (density * 150);
				lp.height = (int) (density * 150);
				gv_ptt.setLayoutParams(lp);
			}

			gv_users_block = (LinearLayout) mViewInGroupCallVideo
					.findViewById(R.id.gv_users_block);
			gv_users_hide_bt = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_users_but);
			gv_users = (GridView) mViewInGroupCallVideo
					.findViewById(R.id.gv_users);
			gv_users_block.bringToFront();

			// android.view.ViewGroup.LayoutParams lp = gv_users_block
			// .getLayoutParams();
			// Log.d(TAG, "窗口高度=" + windowHeigh + "  宽度=" + windowWidth +
			// "  dpi="
			// + dpi + "  density=" + density);
			// lp.height = (int) (windowHeigh * 0.4);
			// gv_users_block.setLayoutParams(lp);

			int cols = (int) (windowWidth / (70 * density));
			gv_users.setNumColumns(cols);

			GroupUsersAdapter gua = new GroupUsersAdapter(mContext);
			gv_users.setAdapter(gua);
			gua.setUsers(mOnlineUsers);

			// if(SKDroid.isBh04() || SKDroid.isl8848a_l1860()){
			// MyLog.d(TAG, "PAD终端禁用扬声器切换按钮");
			// gv_speaker
			// .setBackgroundResource(R.drawable.speaker_down_2);
			// serviceAV.getAVSession().setSpeakerphoneOn(true);
			// isSpeaker = true;
			// gv_speaker.setClickable(false);
			// }

			gv_speaker.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (isSpeaker) {
						serviceAV.getAVSession().setSpeakerphoneOn(false);
						gv_speaker
								.setBackgroundResource(R.drawable.speaker_up_2);
						isSpeaker = false;
					} else {
						serviceAV.getAVSession().setSpeakerphoneOn(true);
						gv_speaker
								.setBackgroundResource(R.drawable.speaker_down_2);
						isSpeaker = true;
					}

				}
			});
			gv_users_hide_bt.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:

						gv_users_hide_bt
								.setBackgroundResource(R.drawable.gv_users_down);
						if (gv_users_block.getVisibility() == View.VISIBLE) {
							TranslateAnimation mHiddAnimation = new TranslateAnimation(
									Animation.RELATIVE_TO_SELF, 0.0f,
									Animation.RELATIVE_TO_SELF, 1.0f,
									Animation.RELATIVE_TO_SELF, 0.0f,
									Animation.RELATIVE_TO_SELF, 0.0f);
							mHiddAnimation.setDuration(500);
							gv_users_block.startAnimation(mHiddAnimation);
							gv_users_block.setVisibility(View.GONE);
						} else {
							gv_users_sum_num.setText("" + mOnlineUsers.size());
							TranslateAnimation mHiddAnimation = new TranslateAnimation(
									Animation.RELATIVE_TO_SELF, 1.0f,
									Animation.RELATIVE_TO_SELF, 0.0f,
									Animation.RELATIVE_TO_SELF, 0.0f,
									Animation.RELATIVE_TO_SELF, 0.0f);
							mHiddAnimation.setDuration(500);
							gv_users_block.startAnimation(mHiddAnimation);
							gv_users_block.setVisibility(View.VISIBLE);
							gv_users_block.bringToFront();
						}
						break;

					case MotionEvent.ACTION_UP:

						gv_users_hide_bt
								.setBackgroundResource(R.drawable.gv_users_up);
						break;

					default:
						break;
					}

					return true;
				}
			});

			// back
			gv_hide = (ImageView) mViewInGroupCallVideo
					.findViewById(R.id.gv_hide);
			gv_hide.setOnClickListener(myOnClickListener);
			//

			gv_hangup.setOnClickListener(myOnClickListener);

			gv_SwitchCamera.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					serviceAV.switchCameraFrontOrBack();

					if (gv_SwitchCamera != null) {
						gv_SwitchCamera.setClickable(false);
						MyLog.d(TAG, "mSwitchCamera.setClickable(false)");
						mSwitchCameraTimer.schedule(new TimerTask() {

							@Override
							public void run() {
								if (gv_SwitchCamera != null) {
									gv_SwitchCamera.setClickable(true);
									MyLog.d(TAG,
											"mSwitchCamera.setClickable(true)");
								}
							}
						}, AVSingleVideoFragment.SWITCH_CAMERA_PERIOD);
					}
				}
			});
			gv_ptt.setEnabled(true);

			gv_ptt.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					if (mPttTmp == PTTState.REJECTED) {
						MyLog.d(TAG, "PTT键暂不可点.");
						return false;
					}

					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						MyLog.d(TAG, "AVGroupVideo PTT down");
						if (GlobalVar.PTTHasLongClickedDown) {
							Log.d(TAG, "ptt - PTTHasLongClickedDown" + GlobalVar.PTTHasLongClickedDown);
							break;
						}else{
							Log.d(TAG, "ptt - PTTHasLongClickedDown" + GlobalVar.PTTHasLongClickedDown);
							GlobalVar.PTTHasLongClickedDown = true;
							mPttTouched = true;
						}

						gv_ptt.setBackgroundResource(R.drawable.ptt_down);
						gv_light.setImageResource(R.drawable.request);
						mPttTimerTmp = new NgnTimer();
						mPttTimerTmp.schedule(new TimerTask() {

							@Override
							public void run() {
								synchronized (serviceAV) {

									serviceAV.sendPTTRequestInfoMsg();
									mPttTimerTmp = null;

									if (mPttPlayer1 != null) {
										mPttPlayer1.release();
									}

								}
							}
						}, 500);

						ScreenAV.ispeoplePTT = true;
						MyLog.d(TAG, "ispeoplePTT changed to true");
						try {
							mPttPlayer1 = MediaPlayer.create(mContext,
									R.raw.talkroom_press);
							mPttPlayer1.start();
						} catch (Exception e) {
							mPttPlayer1.release();
							mPttPlayer1 = MediaPlayer.create(mContext,
									R.raw.talkroom_press);
							e.printStackTrace();

						}

						break;

					case MotionEvent.ACTION_UP:

						MyLog.d(TAG, "AVGroupVideo PTT up");
						if (mPttTouched) {
							Log.d(TAG, "ptt - mPttTouched = " + mPttTouched + ", GlobalVar.PTTHasLongClickedDown = " + GlobalVar.PTTHasLongClickedDown);
							GlobalVar.PTTHasLongClickedDown = false;
							mPttTouched = false;
						}
						else{
							Log.d(TAG, "ptt - mPttTouched = " + mPttTouched +", GlobalVar.PTTHasLongClickedDown = " + GlobalVar.PTTHasLongClickedDown);
							return false;
						}

						serviceAV.setConsumerOnPause(false);
						if (mPttTimerTmp == null) {
							serviceAV.sendPTTReleaseInfoMsg();
						} else {
							mPttTimerTmp.cancel();
							mPttTimerTmp.purge();
							mPttTimerTmp = null;
						}
						gv_light.setImageResource(R.drawable.idle);
						gv_ptt.setBackgroundResource(R.drawable.ptt_up);
						break;

					default:
						break;
					}

					return true;
				}
			});

		}

		if (mPttCall == null) {
			mPttCall = new GroupPTTCall();
			Log.d(TAG, "mPttCall = " + mPttCall.toString());
		}

		Log.d(TAG, "mPttCall = " + mPttCall.toString() + "; "
				+ "isSubscribe = " + mPttCall.isSubscribe());

		// 终端作为被呼时执行
		if ((!serviceAV.getAVSession().isOutgoing())
				&& (!mPttCall.isSubscribe())) {
			Log.d(TAG,
					"serviceAV.getAVSession() is not Outgoing and subscribe remotePartyDisplayName(主呼的视频)");
			if (remoteparty.contains("@")) {
				pttName = NgnUriUtils.getValidPhoneNumber(remoteparty);
			} else {
				pttName = remoteparty;
			}
			serviceAV.sendPTTSubscribeInfoMsg(pttName);
			mPttCall.setCurrentSubscribeName(pttName);
			mPttCall.setIsSubscribe(true);

			Log.d(TAG, "视频组呼    FIRST  当前订阅视频： " + pttName);
		}

		if (SystemVarTools.mStartGroupCalllRepoort
				&& SystemVarTools.mTakeVideoPTTFlag == false) {
			mTimerVideoPTTReport.schedule(mTimerTaskVideoPTTReport, 0, 10000);
			SystemVarTools.mTakeVideoPTTFlag = true;
		}

		if (SKDroid.sks_version == VERSION.NORMAL) {
			android.view.ViewGroup.LayoutParams lp = gv_LocalVideoPreview_parent
					.getLayoutParams();
			lp.height = SystemVarTools.dip2px(mContext, 120);
			lp.width = SystemVarTools.dip2px(mContext, 90);
			gv_LocalVideoPreview_parent.setLayoutParams(lp);
		}

		View mRemoteView = ServiceAV.createRemoteVideoPreview(serviceAV);
		if (mRemoteView != null) {
			gv_RemoteVideoPreview.removeAllViews();
			gv_RemoteVideoPreview.addView(ServiceAV
					.createRemoteVideoPreview(serviceAV));
		}

		// Video Producer
		if (!ScreenAV.ispeoplePTT) {
			MyLog.e(TAG, "First time to groupVideoCall,full screen");

			LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);

			gv_LocalVideoPreview.setVisibility(View.VISIBLE);

			if (gv_LocalVideoPreview_parent != null) {
				gv_LocalVideoPreview_parent.setLayoutParams(param);
				gv_LocalVideoPreview_parent.setPadding(10, 10, 10, 10);
			}

		} else {
			MyLog.e(TAG, "Not First time to groupVideoCall,screen normal");
			LayoutParams param = new LayoutParams(SystemVarTools.dip2px(
					mContext, 90), SystemVarTools.dip2px(mContext, 120));
			param.topMargin = SystemVarTools.dip2px(mContext, 15);
			param.rightMargin = SystemVarTools.dip2px(mContext, 15);

			param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			gv_LocalVideoPreview.setVisibility(View.VISIBLE);
			if (gv_LocalVideoPreview_parent != null) {
				gv_LocalVideoPreview_parent.setLayoutParams(param);
				gv_LocalVideoPreview_parent.setPadding(0, 0, 0, 0);
			}

		}

		View mLocalView = ServiceAV.createLocalPreview(serviceAV, true);
		if (mLocalView != null) {
			gv_LocalVideoPreview.removeAllViews();
			gv_LocalVideoPreview.addView(ServiceAV.createLocalPreview(
					serviceAV, true));
		}
		// gv_duration.bringToFront();
		gv_top_cmd.bringToFront();

		gv_RemoteVideoPreview.setOnClickListener(myOnClickListener);

		CallPeriodTimer.schedule(mTimerTaskInCall, 0, 1000);

		serviceAV.sendPTTInquireInfoMsg();

		serviceAV.getAVSession().setSpeakerphoneOn(false);

		return mViewInGroupCallVideo;
	}

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			if (id == gv_hangup.getId()) {

				Builder builder = new Builder(mScreen);
				builder.setTitle(getActivity().getString(
						R.string.quit_video_group_calling));
				builder.setMessage(getActivity().getString(
						R.string.click_certain_quit_video_group_calling));
				builder.setPositiveButton(R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ScreenAV.ispeoplePTT = false;
								MyLog.e(TAG, "ispeoplePTT change to false");

								if (mPttCall != null
										&& (mPttCall.getState() == PTTState.GRANTED || mPttCall
												.getState() == PTTState.VIDEOSUB_TURNON))
									serviceAV.sendPTTReleaseInfoMsg();

								serviceAV.hangUpCall();
								// gzc 视频组呼挂断时立刻结束视频输出线程
								if (serviceAV.getAVSession().isConnected()) {
									NgnAVSession.mSendFrameThread = false;
									Log.d(TAG,
											"VideoSend 点击组呼挂断按钮  mSendFrameThread="
													+ NgnAVSession.mSendFrameThread);
								}
								Main.isFirstPTT_onKeyDown = true;
								Main.isFirstPTT_onKeyLongPress = true;
								gv_LocalVideoPreview.removeAllViews();
								IServiceScreen ss = ((Engine)Engine.getInstance()).getScreenService();
								if(ss != null){
									IBaseScreen baseScreen = ss.getCurrentScreen();
									if(baseScreen != null && baseScreen instanceof ScreenAV){
										baseScreen.back();
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

			} else if (id == gv_hide.getId()) {
				mScreen.back();

			} else if (gv_RemoteVideoPreview != null
					&& id == gv_RemoteVideoPreview.getId()) {
				changeVisible();
			} else if (gv_LocalVideoPreview != null
					&& id == gv_LocalVideoPreview.getId()) {
				changeVisible();
			}
		}
	};

	/**
	 * 组呼心跳计时器
	 */
	private TimerTask mTimerTaskVideoPTTReport = new TimerTask() {
		@Override
		public void run() {
			if (serviceAV.getAVSession() != null
					&& SystemVarTools.mTimerVideoPTTReport != null) {
				serviceAV.sendPTTReportAliveInfoMsg();
			}
		}
	};

	private final static int REFRASH_ONLINE_USERS = 1001;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {

			case ServiceAV.CALL_PERIOD_REFRASH: // 刷新通话时长计时器
				long period = new Date().getTime()
						- serviceAV.getAVSession().getStartTime();
				gv_duration.setText(SystemVarTools.mCallPeriodFormat(period));
				break;
			case REFRASH_ONLINE_USERS:
				if (gv_users != null && gv_users.getAdapter() != null) {
					((GroupUsersAdapter) gv_users.getAdapter())
							.notifyDataSetChanged();
				}

				if (gv_users_sum_num != null && mOnlineUsers != null)
					gv_users_sum_num.setText("" + mOnlineUsers.size());
				break;
			default:
				break;

			}
		}
	};

	/**
	 * 处理组呼INFO请求
	 * 
	 * @param infoContent
	 */
	public void handleRequestPTTInfoMsg(byte[] infoContent) {

		MyLog.d(TAG, "视频组呼      handleRequestPTTInfoMsg()");

		if (mPttCall == null) {
			mPttCall = new GroupPTTCall();
		}
		PTTInfoMsg msg = new PTTInfoMsg(infoContent);
		mPttCall.handlePTTInfoMsg(msg);

		String onLineUser = "";

		Log.d(TAG,
				"视频组呼  state : " + mPttCall.getState() + "   NUM:"
						+ msg.getPTTPhoneNumber());

		if (mPttCall.getState() == PTTState.REJECTED) {
			serviceAV.setConsumerOnPause(false);
			serviceAV.setProducerOnPause(true);

		} else if (mPttCall.getState() == PTTState.GRANTED) {
			serviceAV.setConsumerOnPause(true);
			serviceAV.setProducerOnPause(false);
		}else if (mPttCall.getState() == PTTState.RELEASE_SUCCESS ||
				mPttCall.getState() == PTTState.RELEASED){
			serviceAV.setConsumerOnPause(true);
			serviceAV.setProducerOnPause(true);
		}

		ModelContact pttUser = null;

		switch (mPttCall.getState()) {
		case NONE:
		case REQUESTING:
			break;
		case GRANTED: // PTT抢占成功
			MyLog.d(TAG, "GRANTED");

			mPttTmp = PTTState.GRANTED;

			gv_ptt.setClickable(true);

			ServiceAV.isPTTRejected = false;

			ModelContact myself = SystemVarTools
					.createContactFromPhoneNumber(GlobalVar.mLocalNum);
			if (myself != null) {
				gv_currPttUser.setText(myself.name);
			}

			// engine.playNotificationTone();
			if (mPttPlayer3.isPlaying()) {
				try {
					mPttPlayer3.stop();
					mPttPlayer3.prepare();
					mPttPlayer3.start();
				} catch (IllegalStateException e) {
					mPttPlayer3.release();
					mPttPlayer3 = MediaPlayer.create(mContext,
							R.raw.talkroom_begin);
					e.printStackTrace();
				} catch (IOException e) {
					mPttPlayer3.release();
					mPttPlayer3 = MediaPlayer.create(mContext,
							R.raw.talkroom_begin);
					e.printStackTrace();
				}
			}

			// PTT按钮为红色表示PTT抢占成功
			gv_ptt.setBackgroundResource(R.drawable.ptt_down);
			gv_light.setImageResource(R.drawable.grant);

			// 语音组呼时，终端抢PTT后，扬声器停止放音
			serviceAV.setOnPause(false);

			break;
		case RELEASE_SUCCESS: // PTT释放成功
			Log.d(TAG, "mPttCall.getState() = RELEASE_SUCCESS");

			mPttTmp = PTTState.RELEASE_SUCCESS;

			ServiceAV.isPTTRejected = false;

			gv_ptt.setClickable(true);

			// PTT按钮为绿色表示目前没人抢占PTT
			gv_ptt.setBackgroundResource(R.drawable.ptt_up);
			gv_light.setImageResource(R.drawable.idle);
			serviceAV.setOnPause(true);

			gv_currPttUser.setText("");

			break;
		case RELEASED:
			Log.d(TAG, "mPttCall.getState() = RELEASED");
			ServiceAV.isPTTRejected = false;
			mPttTmp = PTTState.RELEASED;

			gv_ptt.setClickable(true);

			// green
			// gv_ptt.setTextColor(getResources().getColor(R.color.color_green));
			gv_ptt.setBackgroundResource(R.drawable.ptt_up); // 绿色
			gv_light.setImageResource(R.drawable.idle);
			serviceAV.sendPTTReleaseAckInfoMsg();

			gv_currPttUser.setText("");

			break;
		case REJECTED:
			Log.d(TAG, "视频组呼  REGECTED");

			mPttTmp = PTTState.REJECTED;

			ServiceAV.isPTTRejected = true;
			ScreenAV.ispeoplePTT = true;

			gv_ptt.setClickable(false);

			// PTT按钮为白色表示PTT已经被抢占，目前无法抢占
			gv_ptt.setBackgroundResource(R.drawable.ptt_down);
			gv_light.setImageResource(R.drawable.reject);

			serviceAV.setOnResetJB();
			pttName = msg.getPTTPhoneNumber();
			String mCurrPttNum = mPttCall.getCurrentSubscribeName();
			// 只有视频组呼才存在切换视频的情况
			// if (mCurrPttNum != null && mCurrPttNum.equals(pttName)) {
			// MyLog.d(TAG, "视频组呼   Reject 两次抢PTT用户相同,不再重新订阅");
			// return;
			// }

			pttUser = SystemVarTools.createContactFromPhoneNumber(pttName);
			if (pttUser != null) {
				gv_currPttUser.setText(pttUser.name);
			}

			// 如果当前正在订阅某人的视频，切换视频前要先取消订阅
			if (mPttCall.isSubscribe()) {
				serviceAV.sendPTTCancelInfoMsg(mPttCall
						.getCurrentSubscribeName());

				Log.d(TAG,
						"视频组呼 CANCEL 取消当前订阅的视频: "
								+ mPttCall.getCurrentSubscribeName());
				mPttCall.setIsSubscribe(false);
			} else {
				// 订阅抢占PTT那个人的视频
				serviceAV.sendPTTSubscribeInfoMsg(pttName);
				mPttCall.setCurrentSubscribeName(pttName);
				mPttCall.setIsSubscribe(true);
				Log.d(TAG, "视频组呼  SUBSCRIBE 切换订阅视频： " + pttName);
			}

			break;
		case ALAVE:
			Log.d(TAG, "mPttCall.getState() = ALAVE");
			// mTvInfo.setText(getString(R.string.string_groupcall_ptttaken));
			// setOnPause(true);
			// toggleSpeakerphone();
			break;
		case SUBSCRIBE_SUCCESS:
			Log.d(TAG, "视频组呼   SUBSCRIBE_SUCCESS");
			// Video Consumer
			gv_RemoteVideoPreview.removeAllViews();
			gv_RemoteVideoPreview.addView(ServiceAV
					.createRemoteVideoPreview(serviceAV));

			pttUser = SystemVarTools.createContactFromPhoneNumber(pttName);
			if (pttUser != null) {
				gv_sub_name.setText(pttUser.name);
			} else {
				gv_sub_name.setText(pttName);
			}

			if (!ScreenAV.ispeoplePTT) {
				MyLog.e(TAG, "SUBSCRIBE_SUCCESS:First time to groupVideoCall,full screen");

				LayoutParams param = new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT);

				gv_LocalVideoPreview.setVisibility(View.VISIBLE);
				if (gv_LocalVideoPreview_parent != null) {
					gv_LocalVideoPreview_parent.setLayoutParams(param);
					gv_LocalVideoPreview_parent.setPadding(10, 10, 10, 10);
				}

			} else {
				MyLog.e(TAG, "SUBSCRIBE_SUCCESS:Not First time to groupVideoCall,screen normal");

				// gv_RemoteVideoPreview.removeAllViews();
				// gv_RemoteVideoPreview.addView(ServiceAV.createRemoteVideoPreview(serviceAV));

				LayoutParams param = new LayoutParams(SystemVarTools.dip2px(
						mContext, 90), SystemVarTools.dip2px(mContext, 120));

				param.topMargin = SystemVarTools.dip2px(mContext, 15);
				param.rightMargin = SystemVarTools.dip2px(mContext, 15);

				param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

				gv_LocalVideoPreview.setVisibility(View.VISIBLE);
				if (gv_LocalVideoPreview_parent != null) {
					gv_LocalVideoPreview_parent.setLayoutParams(param);
					gv_LocalVideoPreview_parent.setPadding(0, 0, 0, 0);
				}
			}
			// gv_duration.bringToFront();

			// gv_top_cmd.bringToFront();

			break;
		case SUBSCRIBE_FAILED:
			Log.d(TAG, "视频组呼  State = SUBSCRIBE_FAILED");
			// mTvInfo.setText(getString(R.string.string_groupcall_subscribe_failed));
			// //视频订阅失败
			break;
		case ONLINE:
			Log.d(TAG, "视频组呼  ONLINE, isSubscribe = " + mPttCall.isSubscribe());

			onLineUser = msg.getPTTPhoneNumber();
			mBlockQueueModel.putUser(onLineUser);
			break;

		case OFFLINE:
			MyLog.d(TAG, "视频组呼   OFFLINE:" + msg.getPTTPhoneNumber());
			String offLineUser = msg.getPTTPhoneNumber();
			int index = -1;
			for (int loc = 0; loc < mOnlineUsers.size(); loc++) {
				if (mOnlineUsers.get(loc).mobileNo.equals(offLineUser)) {
					index = loc;
					break;
				}
			}
			MyLog.d(TAG, "index="+index+"  OnLineNums="+mOnlineUsers.size());
			if (index != -1) {
				mOnlineUsers.remove(index);
				if (gv_users != null && gv_users.getAdapter() != null) {
					((GroupUsersAdapter) gv_users.getAdapter())
							.notifyDataSetChanged();
				}
				Log.d(TAG, "视频组呼  Online User(" + msg.getPTTPhoneNumber()
						+ ") OFFLINE");
			}
			if (gv_users_sum_num != null)
				gv_users_sum_num.setText("" + mOnlineUsers.size());
			break;

		case VIDEOSUB_TURNON:
			Log.d(TAG, "视频组呼   VIDEOSUB_TURNON");

			serviceAV.sendPTTSubscribeAckInfoMsg();
			// startVideo(true,true);
			if (mPttCall.isStateChanged()) {
				// Video Producer
				// startVideo(true,true);
				serviceAV.getAVSession().setmSendVIdeo(true);
			}
			break;

		case VIDEOSUB_TURNOFF:

			Log.d(TAG, "视频组呼   VIDEOSUB_TURNOFF");
			serviceAV.sendPTTSubscribeAckInfoMsg();
			if (mPttCall.isStateChanged()) {
				// Video Producer
				// startVideo(false,true);
				serviceAV.getAVSession().setmSendVIdeo(false);
			}
			break;
		case CALSUB:
			Log.d(TAG, "视频组呼   CALSUB");
			serviceAV.sendPTTCancelAckInfoMsg();
			break;
		case CANCEL_SUCCESS:

			Log.d(TAG, "视频组呼   CANCEL_SUCCESS");

			// 订阅抢占PTT那个人的视频
			serviceAV.sendPTTSubscribeInfoMsg(pttName);
			mPttCall.setCurrentSubscribeName(pttName);
			mPttCall.setIsSubscribe(true);
			Log.d(TAG, "视频组呼   CANCEL_SUCCESS 切换订阅视频： " + pttName);
			break;

		default:
			break;
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

	public GroupPTTCall getmPttCall() {
		return mPttCall;
	}

	public void setmPttCall(GroupPTTCall mPttCall) {
		this.mPttCall = mPttCall;
	}

	@Override
	public void onStop() {
		super.onStop();
		MyLog.d(TAG, "视频组呼  onStop()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MyLog.d(TAG, "视频组呼  onDestroy()");
	}

	private void changeVisible() {
		MyLog.d(TAG, "changeVisible()");
		if (gv_top_cmd.getVisibility() == View.VISIBLE) {
			MyLog.d(TAG, "gv_top_cmd GONE");
			gv_top_cmd.setVisibility(View.GONE);
		} else {
			MyLog.d(TAG, "gv_top_cmd VISIBLE");
			gv_top_cmd.setVisibility(View.VISIBLE);
			gv_top_cmd.bringToFront();
		}
		if (gv_users_block.getVisibility() == View.VISIBLE) {
			TranslateAnimation mHiddAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 1.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);
			mHiddAnimation.setDuration(500);
			gv_users_block.startAnimation(mHiddAnimation);
			gv_users_block.setVisibility(View.GONE);
		}
	}

	public FrameLayout getGv_screen_bottom() {
		return gv_screen_bottom;
	}

	public void setGv_screen_bottom(FrameLayout gv_screen_bottom) {
		this.gv_screen_bottom = gv_screen_bottom;
	}

	class OnLineHandler extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			MyLog.d(TAG, "doInBackground(" + params[0] + ")");
			String onLineUser = params[0];
			int index = -1;
			synchronized (mOnlineUsers) {
				for (int loc = 0; loc < mOnlineUsers.size(); loc++) {
					if (mOnlineUsers.get(loc).mobileNo.equals(onLineUser)) {
						index = loc;
						break;
					}
				}
			MyLog.d(TAG, "index="+index +"  OnlineNums"+mOnlineUsers.size());
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
			return mOnlineUsers.size();
		}

		@Override
		protected void onPostExecute(Integer result) {
			MyLog.d(TAG, "onPostExecute(" + result + ")");
			super.onPostExecute(result);
			if (gv_users != null && gv_users.getAdapter() != null) {
				((GroupUsersAdapter) gv_users.getAdapter())
						.notifyDataSetChanged();
			}

			if (gv_users_sum_num != null)
				gv_users_sum_num.setText("" + result);
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

	@Override
	public void headsetOn() {

	}

	@Override
	public void headsetOff() {
		if (gv_speaker != null) {
			gv_speaker.setBackgroundResource(R.drawable.speaker_down);
		}
	}

}
