package com.sunkaisens.skdroid.Screens;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenTabMessage.MyHistoryEventSMSIntelligentFilter;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.ImageLoader;
import com.sunkaisens.skdroid.adapter.ScreenChatAdapter;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.ModelFileTransport;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenChat extends BaseScreen implements AnimationListener {
	private static String TAG = ScreenChat.class.getCanonicalName();
	public final INgnHistoryService mHistorytService;
	private final INgnSipService mSipService;

	private InputMethodManager mInputMethodManager;

	private static String sRemoteParty;
	public long MAX_FILE = 1024 * 1024 * 25; // 文件最大大小设为25M

	public int MAX_MSG_LENGTH = 210;// 即时消息长度限制

	public static String getRemotePartyString() {
		return sRemoteParty;
	}

	public final static int REFRESH_CHAT = 80001;

	private NgnMsrpSession mSession;
	private NgnMediaType mMediaType;
	// private ScreenChatAdapter mAdapter;
	// public ScreenChatAdapter mAdapter;
	public static ScreenChatAdapter mAdapter;
	private EditText mEtCompose;
	private static ListView mLvHistoy;
	private TextView mTvName;
	private Button mBtSend;
	private ImageView mBtadd_filetransfer_imagebutton;

	private static boolean zhankai = true; // 传输文件按钮是否展开
	private static boolean im_audiozhankai = true;

	private ImageButton mBt_takephoto_button; // 拍照按钮
	private static final int SELECT_PICTURE = 4;
	private static final int SELECT_CAMERA = 5;

	private ImageView mBt_IMaudio; // 及时语音按钮

	private ImageButton mBt_IMvideoButton; // 即时视频按钮

	private ImageButton mBt_AudioCallButton; // 语音通话按钮
	private TextView mBt_AudioCallTextView;
	private ImageButton mBt_VideoCallButton; // 视频通话按钮
	private TextView mBt_VideoCallTextView;

	private MediaRecorder mMediaRecorder;

	private File recAudioFile;

	private ImageButton mBtFiletransfer;
	private View mViewFiletransfer_view;
	private RelativeLayout mLinearLayoutFiletransfer_ll;
	private RelativeLayout mLinearLayoutEditText; // 及时语音对话布局文件
	// private Animation animation_rotateAnimation;

	private Button mBtbottom_IMaudioTalk_button_button;

	private ImageView mBtShowNum;

	private LinearLayout mBtShowNumLayout;

	private ImageView mBtBack;
	// 标识信息长度的textView
	private TextView mTvContentCount;
	private final INgnConfigurationService mConfigurationService;

	// private ModelContact userinfo = null;
	public ModelContact userinfo = null;

	public Handler mRefrashHandler;

	// add by jgc 新增获取麦克风音量线程
	private Handler mChangeAudioHandler = null;
	private Runnable mGetAudioDBRunnable = new Runnable() {

		@Override
		public void run() {
			getAudiodb();
		}
	};
	private Thread mGetAudioDBthread = null;
	private ImageView audio_loudImageView = null;
	private Dialog selectDialog = null;

	// public Handler mHandler;

	private String draftString; // 短息草稿
	public NgnHistorySMSEvent draftEvent = null; // 保存草稿的event
	private List<NgnHistoryEvent> mEvents;

	public static int refreshCount = 0;

	private GestureDetector mGestureDetector;
	private float weizhix;
	private float weizhiy;
	private boolean isrecorder = false;
	private boolean isCancel = false;
	private NgnTimer timer = new NgnTimer();

	public static final int BEGIN_RECORDER = 1115;
	public static final int CANCEL = 1116;
	public static final int LONG_PRESS = 1117;
	public static final int RECORDER = 1118;

	public static ExecutorService singleUploadThreadExecutorService = Executors
			.newSingleThreadExecutor();
	public static ExecutorService singleDownloadThreadExecutorService = Executors
			.newSingleThreadExecutor();

	public ScreenChat() {
		super(SCREEN_TYPE.CHAT_T, TAG);

		mMediaType = NgnMediaType.None;
		mHistorytService = getEngine().getHistoryService();
		mSipService = getEngine().getSipService();
		mConfigurationService = getEngine().getConfigurationService();

		MyLog.d(TAG, "ScreenChat()");

		mEvents = mHistorytService.getObservableEvents().filter(
				new MyHistoryEventSMSIntelligentFilter());
		for (int i = 0; i < mEvents.size(); i++) {
			NgnHistorySMSEvent SMSEvent = (NgnHistorySMSEvent) mEvents.get(i);
			if (SMSEvent.getIsDraft().equals("true")
					&& SMSEvent.getRemoteParty().equals(sRemoteParty)) {
				MyLog.d(TAG, "ScreenChat()  删除event" + i);
				draftString = SMSEvent.getDraftString();
				mHistorytService.deleteEvent(SMSEvent);
				refresh();
			}

		}

	}

	/**
	 * 聊天室置底，用户和该联系人处于聊天界面，并且此时该联系人发来消息，用户将聊天室置底
	 * 
	 */
	public static void ListViewToButtom() {

		if (mLvHistoy != null && mAdapter != null) {
			mLvHistoy.setAdapter(mAdapter);
			mLvHistoy.setSelection(NgnEngine.getInstance().getHistoryService()
					.getEvents().size());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_chat);

		MyLog.d(TAG, "ScreenChat onCreate");
		zhankai = true;

		SystemVarTools.ScreenChat_Is_Top = true;

		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

		mTvContentCount = (TextView) findViewById(R.id.screen_chat_tv_count);
		mTvContentCount.setText("0/" + MAX_MSG_LENGTH);

		ImageView ivMingOrMi = (ImageView) findViewById(R.id.iv_ming_or_mi_chat);
		if (GlobalVar.isSecuriteCardExist) {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_mi);
		} else {
			ivMingOrMi.setBackgroundResource(R.drawable.ic_ming);
		}
		
		
		mEtCompose = (EditText) findViewById(R.id.screen_chat_editText_compose);
		mTvName = (TextView) findViewById(R.id.screen_chat_textview_name);
		mBtSend = (Button) findViewById(R.id.screen_chat_button_send);
		mBtadd_filetransfer_imagebutton = (ImageView) findViewById(R.id.add_filetransfer_imagebutton);
		mBt_takephoto_button = (ImageButton) findViewById(R.id.screen_chat_button_takephoto_button);
		mBt_IMaudio = (ImageView) findViewById(R.id.screen_chat_button_IMaudio_button);
		mBt_IMvideoButton = (ImageButton) findViewById(R.id.screen_chat_button_IMvideo_button);

		mBt_AudioCallButton = (ImageButton) findViewById(R.id.screen_chat_button_audiocall_button);
		mBt_AudioCallTextView = (TextView) findViewById(R.id.screen_chat_button_audiocall_testView);

		mBt_VideoCallButton = (ImageButton) findViewById(R.id.screen_chat_button_videocall_button);
		mBt_VideoCallTextView = (TextView) findViewById(R.id.screen_chat_button_videocall_textview);

		mBtFiletransfer = (ImageButton) findViewById(R.id.screen_chat_button_filetransfer_button);
		mViewFiletransfer_view = (View) findViewById(R.id.screen_chat_linearLayout_bottom_filetransfer_view);
		mLinearLayoutFiletransfer_ll = (RelativeLayout) findViewById(R.id.screen_chat_linearLayout_bottom_filetransfer_ll);
		mLinearLayoutEditText = (RelativeLayout) findViewById(R.id.screen_chat_editText_compose_parentlayout);
		mBtbottom_IMaudioTalk_button_button = (Button) findViewById(R.id.screen_chat_linearLayout_bottom_IMaudioTalk_button);

		// add by jgc 2014.12.23 根据麦克的音量改变录音的图标
		LayoutInflater audio_loudinInflater = getLayoutInflater();
		final View audio_loudvView = audio_loudinInflater.inflate(
				R.layout.screen_chat_audiorecorder, null);
		audio_loudImageView = (ImageView) audio_loudvView
				.findViewById(R.id.screen_chat_audiorecorder_Imageview2);
		audio_loudImageView.setImageResource(R.drawable.audio_1);

		selectDialog = new Dialog(ScreenChat.this, R.style.dialog);
		selectDialog.setCancelable(true);
		selectDialog.setContentView(audio_loudvView);

		mChangeAudioHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				audio_loudImageView.setImageResource(R.drawable.audio_1);
				selectDialog.setContentView(audio_loudvView);

				switch (msg.what) {
				case MessageTypes.MSG_CHAT_AUDIORECORDER_0:
					audio_loudImageView.setImageResource(R.drawable.audio_1);
					selectDialog.setContentView(audio_loudvView);

					break;

				case MessageTypes.MSG_CHAT_AUDIORECORDER_1:
					audio_loudImageView.setImageResource(R.drawable.audio_1);
					selectDialog.setContentView(audio_loudvView);

					break;

				case MessageTypes.MSG_CHAT_AUDIORECORDER_2:
					audio_loudImageView.setImageResource(R.drawable.audio_2);
					selectDialog.setContentView(audio_loudvView);

					break;

				case MessageTypes.MSG_CHAT_AUDIORECORDER_3:
					audio_loudImageView.setImageResource(R.drawable.audio_3);
					selectDialog.setContentView(audio_loudvView);

					break;

				case MessageTypes.MSG_CHAT_AUDIORECORDER_4:
					audio_loudImageView.setImageResource(R.drawable.audio_4);
					selectDialog.setContentView(audio_loudvView);

					break;

				case MessageTypes.MSG_CHAT_AUDIORECORDER_5:
					audio_loudImageView.setImageResource(R.drawable.audio_5);
					selectDialog.setContentView(audio_loudvView);

					break;

				case MessageTypes.MSG_CHAT_AUDIORECORDER_6:
					audio_loudImageView.setImageResource(R.drawable.audio_6);
					selectDialog.setContentView(audio_loudvView);

					break;

				case MessageTypes.MSG_CHAT_AUDIORECORDER_7:
					audio_loudImageView.setImageResource(R.drawable.audio_7);
					selectDialog.setContentView(audio_loudvView);

					break;

				default:
					audio_loudImageView.setImageResource(R.drawable.audio_1);
					selectDialog.setContentView(audio_loudvView);

					break;
				}

			}

		};

		mLvHistoy = (ListView) findViewById(R.id.screen_chat_listView);

		mBtBack = (ImageView) findViewById(id.screen_chat_back);
		mBtShowNum = (ImageView) findViewById(id.screen_chat_shownumber);

		mBtShowNumLayout = (LinearLayout) findViewById(R.id.screen_chat_shownumber_layout);

		mAdapter = new ScreenChatAdapter(this);
		mLvHistoy.setAdapter(mAdapter);
		mAdapter.refresh();

		// 该属性不能为TRANSCRIPT_MODE_ALWAYS_SCROLL，否则每次刷新都会到列表底部
		// mLvHistoy.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		// 聊天室从上到下显示
		mLvHistoy.setStackFromBottom(false);

		mLvHistoy.setSelection(mLvHistoy.getCount());

		// 注册长按菜单调用复制功能
		registerForContextMenu(mLvHistoy);

		userinfo = SystemVarTools.createContactFromRemoteParty(sRemoteParty);

		if (userinfo.isgroup) {
			mBt_AudioCallTextView.setText(ScreenChat.this
					.getString(R.string.call_desc_group_audio));
			mBt_VideoCallTextView.setText(ScreenChat.this
					.getString(R.string.call_desc_group_video));
		}

		mTvName.setText(userinfo.name);
		mTvName.setSelected(true);

		mBtBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mScreenService.back();
				back();
			}
		});

		if (userinfo.isgroup) {
			mBtShowNumLayout.setVisibility(View.VISIBLE);
			mBtShowNum.setImageResource(R.drawable.shownumber);

		} else {
			mBtShowNumLayout.setVisibility(View.VISIBLE);

			SystemVarTools.showicon(mBtShowNum, userinfo, this);
		}

		mBtShowNum.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (userinfo.isgroup) {

					List<ModelContact> list1 = SystemVarTools
							.getOrgChildContact(userinfo, 0);
					List<ModelContact> list2 = SystemVarTools
							.getOrgChildContact(userinfo, 1);

					if ((list1 == null || list1.isEmpty())
							&& (list2 == null || list2.isEmpty())) {
						Toast.makeText(getApplicationContext(),
								getText(R.string.tip_group_no_members),
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (mScreenService.show(ScreenContactChild.class,
							"ScreenContactChild:" + userinfo.mobileNo)) {

					}
				} else {
					if (mScreenService.showPersonOrOrgInfo(
							ScreenPersonInfo.class, userinfo.mobileNo)) {

					}
				}

			}
		});

		mBtSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// ScreenTabMessage.mMessageDraftHashMap.remove(sRemoteParty);

				if (!mSipService.isRegisteSessionConnected()) {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.login_first));
					return;
				}

				if (!NgnStringUtils.isNullOrEmpty(mEtCompose.getText()
						.toString())) {
					sendMessage();
					// openOptionsMenu(); //调用菜单
				}

				// if (mInputMethodManager != null) {
				// mInputMethodManager.hideSoftInputFromWindow(
				// mEtCompose.getWindowToken(), 0);
				// }
			}
		});

		mBtadd_filetransfer_imagebutton
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (zhankai) {
							mViewFiletransfer_view.setVisibility(View.VISIBLE);
							mLinearLayoutFiletransfer_ll
									.setVisibility(View.VISIBLE);

							// float desity = getApplicationContext()
							// .getResources().getDisplayMetrics().density;
							// MyLog.e("!!!!!!!!!!!!!!", "!!!!!!!!!!"
							// + mLinearLayoutFiletransfer_ll.getHeight());
							// MyLog.e("!!!!!!!!!!!!!!", "!!!!!!!!!!" + desity);

							if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
									.isActive()
									&& ScreenChat.this.getCurrentFocus() != null)

								((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
										.hideSoftInputFromWindow(
												ScreenChat.this
														.getCurrentFocus()
														.getWindowToken(),
												InputMethodManager.HIDE_NOT_ALWAYS);

							// mLinearLayoutBottomAudioButton
							// .setVisibility(View.GONE);
							zhankai = false;
						} else {

							mViewFiletransfer_view.setVisibility(View.GONE);
							mLinearLayoutFiletransfer_ll
									.setVisibility(View.GONE);
							// mLinearLayoutBottomAudioButton
							// .setVisibility(View.GONE);
							if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
									.isActive()
									&& ScreenChat.this.getCurrentFocus() != null)

								((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
										.hideSoftInputFromWindow(
												ScreenChat.this
														.getCurrentFocus()
														.getWindowToken(),
												InputMethodManager.HIDE_NOT_ALWAYS);
							zhankai = true;
						}
					}
				});

		mBtFiletransfer.setPadding(10, 5, 10, 5);
		mBtFiletransfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				zhankai = true;
				if (!mSipService.isRegisteSessionConnected()) {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.login_first));
					return;
				}
				// 传文件
				Intent intent = new Intent();
				intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE)
						.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent,
						ScreenChat.this.getString(R.string.choose_file)),
						SELECT_CONTENT);

				mViewFiletransfer_view.setVisibility(View.GONE);
				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);
			}
		});

		// add by jgc 2014.11.17 拍照按钮单击弹出选择对话框
		mBt_takephoto_button.setPadding(10, 5, 10, 5);
		mBt_takephoto_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				zhankai = true;
				if (!mSipService.isRegisteSessionConnected()) {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.login_first));
					return;
				}
				getimage();

				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);
			}
		});

		im_audiozhankai = false;
		mBt_IMaudio.setPadding(10, 5, 10, 5);
		mBt_IMaudio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				zhankai = true;
				if (!mSipService.isRegisteSessionConnected()) {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.login_first));
					return;
				}

				if (!im_audiozhankai) {
					mBtbottom_IMaudioTalk_button_button
							.setVisibility(View.VISIBLE);
					mLinearLayoutEditText.setVisibility(View.GONE);

					mBt_IMaudio
							.setImageResource(R.drawable.screenchat_imaudeo_2);
					im_audiozhankai = true;

				} else {
					mBtbottom_IMaudioTalk_button_button
							.setVisibility(View.GONE);
					mLinearLayoutEditText.setVisibility(View.VISIBLE);
					mBt_IMaudio.setImageResource(R.drawable.screenchat_imaudeo);
					im_audiozhankai = false;
				}

				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

			}
		});

		mBt_IMvideoButton.setPadding(10, 5, 10, 5);
		mBt_IMvideoButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				zhankai = true;
				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);
				if (!mSipService.isRegisteSessionConnected()) {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.login_first));
					return;
				}
				if (!GlobalVar.mCameraIsUsed) {
					Intent intent = new Intent();
					intent.putExtra("usernum", userinfo.mobileNo);
					intent.setClass(ScreenChat.this, Screen_VideoRecorder.class);
					startActivity(intent);
				} else {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.camera_is_unavailable));
				}
			}
		});

		final ScreenChat so = this;

		mBt_AudioCallButton.setPadding(10, 5, 10, 5);
		mBt_AudioCallButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!mSipService.isRegisteSessionConnected()) {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.login_first));
					return;
				}

				if (userinfo != null) {
					if (!userinfo.isgroup) { // 语音单呼
						if (CrashHandler.isNetworkAvailable2()) {
							ServiceAV.makeCall(userinfo.mobileNo,
									NgnMediaType.Audio, SessionType.AudioCall);
						} else {
							SystemVarTools.showNotifyDialog(ScreenChat.this
									.getString(R.string.tip_net_no_conn_error),
									ScreenChat.this);
						}
					} else { // 语音组呼

						if (CrashHandler.isNetworkAvailable2()) {

							String realm = NgnEngine
									.getInstance()
									.getConfigurationService()
									.getString(
											NgnConfigurationEntry.NETWORK_REALM,
											NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
							String g_v_dial_Uri = "sip:" + userinfo.mobileNo
									+ "@" + realm;
							if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {
								ServiceAV.makeCall(g_v_dial_Uri,
										NgnMediaType.Audio,
										SessionType.GroupAudioCall);

								if (NgnApplication.isBh03()) { // 手持台 ptt
									if (Main.isFirstPTT_onKeyDown) {
										Main.isFirstPTT_onKeyDown = false;
									}
								}
								if (NgnApplication.isBh03()) { // 手持台 ptt
									if (Main.isFirstPTT_onKeyLongPress) {
										Main.isFirstPTT_onKeyLongPress = false;
									}
								}

							}
						} else {
							SystemVarTools.showNotifyDialog(""
									+ getText(R.string.tip_net_no_conn_error),
									so);

						}

					}
				}

			}
		});

		mBt_VideoCallButton.setPadding(10, 5, 10, 5);
		mBt_VideoCallButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!mSipService.isRegisteSessionConnected()) {
					SystemVarTools.showToast(ScreenChat.this
							.getString(R.string.login_first));
					return;
				}

				if (userinfo != null) {

					if (!userinfo.isgroup) { // 视频单呼

						if (CrashHandler.isNetworkAvailable2()) {

							ScreenAV.ispeoplePTT = true;

							ServiceAV.makeCall(userinfo.mobileNo,
									NgnMediaType.Video, SessionType.VideoCall);
						} else {
							SystemVarTools.showNotifyDialog(ScreenChat.this
									.getString(R.string.tip_net_no_conn_error),
									ScreenChat.this);
						}
					} else { // 视频组呼

						if (CrashHandler.isNetworkAvailable2()) {

							String realm = NgnEngine
									.getInstance()
									.getConfigurationService()
									.getString(
											NgnConfigurationEntry.NETWORK_REALM,
											NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
							String g_v_dial_Uri = "sip:" + userinfo.mobileNo
									+ "@" + realm;
							if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {

								ScreenAV.ispeoplePTT = false;

								ServiceAV.makeCall(g_v_dial_Uri,
										NgnMediaType.AudioVideo,
										SessionType.GroupVideoCall);
							}
						} else {
							SystemVarTools.showNotifyDialog(""
									+ getText(R.string.tip_net_no_conn_error),
									so);

						}

					}
				}
			}
		});

		mGestureDetector = new GestureDetector(this,
				new SimpleOnGestureListener() {

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onSingleTapUp(e);

					}

					@Override
					public void onLongPress(MotionEvent e) {
						super.onLongPress(e);
						weizhix = e.getX();
						weizhiy = e.getY();

					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {

						return super.onScroll(e1, e2, distanceX, distanceY);
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {

						return super.onFling(e1, e2, velocityX, velocityY);

					}

					@Override
					public void onShowPress(MotionEvent e) {
						super.onShowPress(e);
						weizhix = e.getX();
						weizhiy = e.getY();

					}

					@Override
					public boolean onDown(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onDown(e);
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onDoubleTap(e);
					}

					@Override
					public boolean onDoubleTapEvent(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onDoubleTapEvent(e);
					}

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						weizhix = e.getX();
						weizhiy = e.getY();

						return super.onSingleTapConfirmed(e);
					}

				});

		mBtbottom_IMaudioTalk_button_button
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						if (!mSipService.isRegisteSessionConnected()) {

							if (event.getAction() == MotionEvent.ACTION_DOWN) {

								SystemVarTools.showToast(ScreenChat.this
										.getString(R.string.login_first), false);
								return false;
							}
						}

						if (NgnAVSession.hasActiveSession()) {

							if (event.getAction() == MotionEvent.ACTION_DOWN) {

								SystemVarTools.showToast(
										ScreenChat.this
												.getString(R.string.incalling_try_later),
										false);
								return false;
							}
						}

						mGestureDetector.onTouchEvent(event);

						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if (!isrecorder) {
								MyLog.d(TAG,
										"mBtbottom_IMaudioTalk_button_button down 开启定时器");
								timer = new NgnTimer();
								timer.schedule(new TimerTask() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										isrecorder = true;
										MyLog.d(TAG, "timer 500毫秒后，开始录音");

										Message msg = Message.obtain(myHandler,
												BEGIN_RECORDER);
										myHandler.sendMessage(msg);

										startRecorder();

									}
								}, 500);
							} else {
								MyLog.d(TAG,
										"mBtbottom_IMaudioTalk_button_button down 未开启定时器");
							}

							break;

						case MotionEvent.ACTION_UP:
							if (isrecorder) {
								if (!isCancel) {
									isrecorder = false;

									// mBtbottom_IMaudioTalk_button_button.setTextColor(getApplicationContext().getResources().getColor(R.color.color_titleblack));
									selectDialog.dismiss();
									SystemClock.sleep(1000);

									stopRecorder();
									sendIMaudio();

									mBtbottom_IMaudioTalk_button_button
											.setBackgroundResource(R.drawable.shape_bg_button_normal_grey);
									mBtbottom_IMaudioTalk_button_button.setText(ScreenChat.this
											.getString(R.string.string_chat_IMaudio_down));

								} else {

									isrecorder = false;

									Message msg = Message.obtain(myHandler,
											CANCEL);
									myHandler.sendMessage(msg);

									SystemClock.sleep(1000);
									stopRecorder();

								}
							} else {
								MyLog.d(TAG, "不满500毫秒抬起，Timer cancer");
								timer.cancel();

							}
							isCancel = true;
							break;

						default:
							break;

						}

						if (((Math.abs(event.getY() - weizhiy)) > 30)
								|| ((Math.abs(event.getX() - weizhix)) > 30)) {
							if (isrecorder) {
								MyLog.d(TAG, "滑动取消！");
								isCancel = true;
							} else {
								MyLog.d(TAG, "滑动距离大于30");
								isCancel = false;
							}
						} else {
							isCancel = false;
						}
						return true;

					}

				});

		if (draftString != null && !draftString.equals("")) {
			mEtCompose.setText(draftString);

		}

		mEtCompose.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				mBtSend.setEnabled(!NgnStringUtils.isNullOrEmpty(mEtCompose
						.getText().toString()));

				int length = 0;

				if (!NgnStringUtils.isNullOrEmpty(mEtCompose.getText()
						.toString())) {
					mBtSend.setVisibility(View.VISIBLE);
					mBtadd_filetransfer_imagebutton.setVisibility(View.GONE);
					mViewFiletransfer_view.setVisibility(View.VISIBLE);
					mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

					try {
						length = mEtCompose.getText().toString()
								.getBytes(SystemVarTools.encoding_utf8).length;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

				} else {
					mBtSend.setVisibility(View.GONE);
					mBtadd_filetransfer_imagebutton.setVisibility(View.VISIBLE);
				}
				mTvContentCount.setText(length + "/" + MAX_MSG_LENGTH);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// BugFix: http://code.google.com/p/android/issues/detail?id=7189
		mEtCompose.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				// 聊天室置底
				mLvHistoy.setAdapter(mAdapter);
				mLvHistoy.setSelection(mHistorytService.getEvents().size());

				// add by jgc 以下代码至switch是为了解决软键盘弹出时界面上移问题
				mBtSend.setVisibility(View.GONE);
				mBtadd_filetransfer_imagebutton.setVisibility(View.GONE);

				mViewFiletransfer_view.setVisibility(View.VISIBLE);
				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

				int length;
				try {
					length = mEtCompose.getText().toString()
							.getBytes(SystemVarTools.encoding_utf8).length;
					mTvContentCount.setText(length + "/" + MAX_MSG_LENGTH);

					if (length == 0) {
						mBtadd_filetransfer_imagebutton
								.setVisibility(View.VISIBLE);
					} else {
						mBtSend.setVisibility(View.VISIBLE);
					}

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					// mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});
		InputFilter inputFilter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				try {
					int destLen = dest.toString().getBytes(
							SystemVarTools.encoding_utf8).length;
					int srcLen = source.toString().getBytes(
							SystemVarTools.encoding_utf8).length;
					if ((destLen + srcLen) > MAX_MSG_LENGTH) {
						return "";
					}
					// if (source.length() < 1 && (dend - dstart >= 1)) {
					// return dest.subSequence(dstart, dend - 1);
					// }
					return source;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return "";
			}
		};
		mEtCompose.setFilters(new InputFilter[] { inputFilter });

		mRefrashHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mAdapter.refresh();
			};
		};

	}

	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case BEGIN_RECORDER:
					mBtbottom_IMaudioTalk_button_button
							.setBackgroundResource(R.drawable.shape_bg_button_normal2_grey);
					mBtbottom_IMaudioTalk_button_button.setText(ScreenChat.this
							.getString(R.string.string_chat_IMaudio_up));
					selectDialog.show();
					break;
				case CANCEL:

					mBtbottom_IMaudioTalk_button_button
							.setBackgroundResource(R.drawable.shape_bg_button_normal_grey);
					mBtbottom_IMaudioTalk_button_button.setText(ScreenChat.this
							.getString(R.string.string_chat_IMaudio_down));
					selectDialog.dismiss();
					Toast.makeText(getApplicationContext(),
							ScreenChat.this.getString(R.string.has_canceled),
							Toast.LENGTH_SHORT).show();
					break;
				case LONG_PRESS:

					break;
				case RECORDER:
					mBtbottom_IMaudioTalk_button_button
							.setBackgroundResource(R.drawable.shape_bg_button_normal_grey);
					mBtbottom_IMaudioTalk_button_button.setText(ScreenChat.this
							.getString(R.string.string_chat_IMaudio_down));
					selectDialog.dismiss();
					Toast.makeText(
							getApplicationContext(),
							ScreenChat.this
									.getString(R.string.recorder_audio_failed_hint),
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			} catch (Exception e) {
			}
		}
	};

	private void stopRecorder() {
		if (mMediaRecorder != null) {

			try {
				mMediaRecorder.stop();
				mMediaRecorder.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				isrecorder = false;
				isCancel = true;
				// Message msg = Message.obtain(myHandler,
				// RECORDER);
				// myHandler.sendMessage(msg);

			} catch (RuntimeException e) {
				e.printStackTrace();
				isrecorder = false;
				isCancel = true;
				// Message msg = Message.obtain(myHandler,
				// RECORDER);
				// myHandler.sendMessage(msg);

			} catch (Exception e) {
				e.printStackTrace();
				isrecorder = false;
				isCancel = true;
				// Message msg = Message.obtain(myHandler,
				// RECORDER);
				// myHandler.sendMessage(msg);
			}

			// mGetAudioDBthread.destroy();
			mMediaRecorder = null;

		}

		if (mGetAudioDBthread != null) {
			mGetAudioDBthread = null;
		}

	}

	// add by jgc 2014.12.17 创建及时语音文件，以时间命名。
	private void startRecorder() {

		String saveDir = SystemVarTools.downloadPath;
		File dir = new File(saveDir);
		if (!dir.exists()) {
			dir.mkdir();
		} // 以拍摄时间命名照片
		String fileName = "";
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyyMMdd_HHmmss_'tempaudio'");
		fileName = dateFormat.format(date) + ".amr";
		recAudioFile = new File(saveDir, fileName);

		mMediaRecorder = new MediaRecorder();
		if (recAudioFile.exists()) {
			recAudioFile.delete();
		}

		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mMediaRecorder.setOutputFile(recAudioFile.getAbsolutePath());

		try {
			mMediaRecorder.prepare();

			PackageManager pmManager = getPackageManager();

			boolean permission = (PackageManager.PERMISSION_GRANTED == pmManager
					.checkPermission("android.permission.RECORD_AUDIO",
							"com.sunkaisens.skdroid"));
			if (permission) {
				MyLog.d(TAG, "mMediaRecorder 有权限");

				mMediaRecorder.start();

				if (mGetAudioDBthread == null) {
					mGetAudioDBthread = new Thread(mGetAudioDBRunnable);
					mGetAudioDBthread.start();
				}

			} else {
				MyLog.d(TAG, "mMediaRecorder 无权限");
				// SystemVarTools.showToast("无录音权限，请授权");
			}

		} catch (IllegalStateException e) {
			MyLog.d(TAG, "mMediaRecorder " + e.getMessage());

			isrecorder = false;
			isCancel = true;

			Message msg = Message.obtain(myHandler, RECORDER);
			myHandler.sendMessage(msg);

			stopRecorder();

		} catch (IOException e) {
			MyLog.d(TAG, "mMediaRecorder " + e.getMessage());

			isrecorder = false;
			isCancel = true;

			Message msg = Message.obtain(myHandler, RECORDER);
			myHandler.sendMessage(msg);

			stopRecorder();

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.d(TAG, "mMediaRecorder " + e.getMessage());

			isrecorder = false;
			isCancel = true;

			Message msg = Message.obtain(myHandler, RECORDER);
			myHandler.sendMessage(msg);

			stopRecorder();
		}

	}

	// add by jgc 获取录音时麦克风的音量
	public void getAudiodb() {

		if (mMediaRecorder != null) {

			int radio = mMediaRecorder.getMaxAmplitude() / 600;
			int db = 0;
			if (radio > 1) {
				db = (int) (20 * Math.log10(radio));

				Integer dbInteger = db;
				MyLog.d(TAG, "DB值是：" + dbInteger.toString());

				Message msg = null;
				switch (db / 4) {
				case 0:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_0);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 1:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_0);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 2:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_0);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 3:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_1);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 4:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_2);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 5:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_3);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 6:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_4);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 7:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_5);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 8:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_6);
					mChangeAudioHandler.sendMessage(msg);
					break;
				case 9:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_7);
					mChangeAudioHandler.sendMessage(msg);
					break;

				default:

					msg = Message.obtain(mChangeAudioHandler,
							MessageTypes.MSG_CHAT_AUDIORECORDER_7);
					mChangeAudioHandler.sendMessage(msg);
					break;
				}

			}

			mChangeAudioHandler.postDelayed(mGetAudioDBRunnable, 50);

		}
	}

	// add by jgc 2014.11.27
	public void getimage() {
		zhankai = true;
		CharSequence[] items = { ScreenChat.this.getString(R.string.images),
				ScreenChat.this.getString(R.string.camera) };

		new AlertDialog.Builder(this)
				.setTitle(ScreenChat.this.getString(R.string.choose_pic_source))
				.setItems(items, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (which == 0) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);// 自带的浏览文件Activity
							intent.addCategory(Intent.CATEGORY_OPENABLE);

							intent.setType("image/*");// 这是到达该image路径下的所有文件，默认为内存卡的
							startActivityForResult(Intent.createChooser(intent,
									ScreenChat.this
											.getString(R.string.choose_pic)),
									SELECT_PICTURE);

						} else {

							if (!GlobalVar.mCameraIsUsed) {
								Intent intent = new Intent();
								intent.putExtra("usernum", userinfo.mobileNo);
								intent.setClass(ScreenChat.this,
										Screen_takephoto_camera.class);
								startActivityForResult(intent, SELECT_CAMERA);
							} else {
								SystemVarTools.showToast(ScreenChat.this
										.getString(R.string.camera_is_unavailable));
							}

						}

					}
				}).create().show();

	}

	@Override
	public boolean hasMenu() {
		ModelContact mc = SystemVarTools
				.createContactFromRemoteParty(sRemoteParty);
		if (mc.isgroup)
			return false;
		else
			return true;
	}

	private static final int SELECT_CONTENT = 1;
	private static final int MENU_SHARE_CONTENT = 1;

	@Override
	public boolean createOptionsMenu(Menu menu) {

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case MENU_SHARE_CONTENT: {
			Intent intent = new Intent();
			intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE)
					.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select content"),
					SELECT_CONTENT);
			break;
		}

		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			switch (requestCode) {
			case SELECT_CONTENT:
				Uri selectedContentUri = data.getData();

				if (selectedContentUri != null) {
					String selectedContentPath = super
							.getPath(selectedContentUri); // /mnt/sdcard/DCIM/Camera/IMG_20140610_151900.jpg

					if (selectedContentPath == null
							|| selectedContentPath.isEmpty())
						return;

					MyLog.d(TAG, "Screen_chat_seclectcontent:"
							+ selectedContentPath.toString());

					sendChoosedFile(selectedContentPath);
				} else {

					MyLog.d(TAG, "Screen_chat_seclectcontent:null");
					Toast.makeText(
							getApplicationContext(),
							ScreenChat.this
									.getString(R.string.choose_file_failed_retry),
							2000).show();
				}

				break;

			case SELECT_PICTURE:
				// 选择图片 add by jgc 2014.11.27
				Uri selectedPictureuri = data.getData();
				if (selectedPictureuri != null) {
					MyLog.d(TAG, "Screen_chat_seclectpicture:"
							+ selectedPictureuri.toString());

					String selectedPicturePath = super
							.getPath(selectedPictureuri);

					if (selectedPicturePath == null
							|| selectedPicturePath.isEmpty())
						return;

					sendChoosedFile(selectedPicturePath);
				} else {
					MyLog.d(TAG, "Screen_chat_seclectpicture:null");
					Toast.makeText(
							getApplicationContext(),
							ScreenChat.this
									.getString(R.string.choose_pic_failed_retry),
							2000).show();
				}
				break;

			case SELECT_CAMERA:

				break;

			default:
				MyLog.d(TAG, "none");
				break;

			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		MyLog.d(TAG, "ScreenChat onNewIntent");
		updateUserinfo();
		List<NgnHistoryEvent> mEvents = mHistorytService.getObservableEvents()
				.filter(new HistoryEventChatFilter());
		mEvents = sortEvents(mEvents);

		mAdapter.setmEvents(mEvents);
		// mAdapter.refresh();
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyLog.d(TAG, "ScreenChat onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.d(TAG, "ScreenChat onResume");

		if (mMediaType != NgnMediaType.None) {
			initialize(mMediaType);
		}

		mAdapter.refresh();
		((Engine) Engine.getInstance()).cancelSMSNotif(); // 消掉短消息通知

		SystemVarTools.ScreenChat_Is_Top = true; // 屏幕黑屏唤醒后设置窗口状态

		if (mLvHistoy != null && mAdapter != null) {
			mLvHistoy.setAdapter(mAdapter);
			mLvHistoy.setSelection(NgnEngine.getInstance().getHistoryService()
					.getEvents().size());
		}

	}

	@Override
	protected void onPause() {
		if (mInputMethodManager != null) {
			mInputMethodManager.hideSoftInputFromWindow(
					mEtCompose.getWindowToken(), 0);
		}
		super.onPause();
		MyLog.d(TAG, "ScreenChat onPause");

		SystemVarTools.ScreenChat_Is_Top = false;

		changeUserMsgReadStatus();

		refreshCount = 0;

		stopRecorder();

		// 关闭聊天界面时，销毁正在播放的语音
		if (mAdapter.mplayer != null) {
			mAdapter.mplayer.stop();
			mAdapter.mplayer.release();
			mAdapter.mplayer = null;
		}

		if (mEtCompose.getText().toString() != null
				&& !mEtCompose.getText().toString().equals("")) {
			// 设置草稿
			draftEvent = new NgnHistorySMSEvent(sRemoteParty,
					StatusType.Outgoing, "");

			draftEvent.setIsDraft("true");
			draftEvent.setDraftString(mEtCompose.getText().toString());

			draftEvent.setmLocalParty(SystemVarTools.getmIdentity());

			// gzc 20140815
			long time = findMaxHistoryMsgTime();
			draftEvent.setEndTime(NgnDateTimeUtils.parseDate(
					NgnDateTimeUtils.now()).getTime());
			draftEvent.setStartTime(time);

			if (mSipService.isRegisteSessionConnected()) {
				MyLog.d("ScreenChat ", "ScreenChat onPause 添加draftEvent");
				mHistorytService.addEvent(draftEvent);
			}

		}

		// if (downloadMap.keySet() != null && downloadMap.keySet().size() > 0)
		// {
		// for (String msgId : downloadMap.keySet()) {
		// FileHttpDownLoadClient client = downloadMap.get(msgId);
		// if (client != null && client.isDowbloading) {
		// client.cancel();
		// }
		// }
		// }
		// if (uploadMap.keySet() != null && uploadMap.keySet().size() > 0) {
		// for (String msgId : uploadMap.keySet()) {
		// FileHttpUpLoadClient client = uploadMap.get(msgId);
		// if (client != null && client.isUploading) {
		// client.cancel();
		// }
		// }
		// }

		mHistorytService.updateEvent(draftEvent);

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		MyLog.d(TAG, "ScreenChat onStop");
	}

	@Override
	protected void onDestroy() {
		MyLog.d(TAG, "ScreenChat onDestory");
		// 清除聊天listview的缓存
		ImageLoader imageLoader = mAdapter.getImageLoader();
		if (imageLoader != null) {
			imageLoader.clearCache();
		}

		super.onDestroy();

		if (mSession != null) {
			mSession.decRef();
			mSession = null;
		}

		SystemVarTools.ScreenChat_Is_Top = false;
		changeUserMsgReadStatus();

		// // MyLog.e(TAG, "zhaohua20141029 uploadMap before = " + uploadMap);
		// if (FileHttpUpLoadClient.uploa dList != null) {
		// uploadMap.clear();
		// // uploadMap = null;
		// }
		// // MyLog.e(TAG, "zhaohua20141029 uploadMap after = " + uploadMap);
		//
		// if (downloadMap != null) {
		// downloadMap.clear();
		// downloadMap = null;
		// }

		if (mAdapter != null) {
			this.mHistorytService.getObservableEvents()
					.deleteObserver(mAdapter); // 解决内存泄漏问题
			mAdapter = null;
		}

	}

	@Override
	public boolean hasBack() {
		return true;
	}

	@Override
	public boolean back() {
		SystemVarTools.ScreenChat_Is_Top = false;

		return super.back();
	}

	/**
	 * 改变当前对话用户的未读消息状态，即把当前对话的用户状态改为已读
	 */
	private void changeUserMsgReadStatus() {
		// 把读过信息的用户的标志改为false，即已读
		mConfigurationService.putBoolean(sRemoteParty, false);
		mConfigurationService.commit();
	}

	private void initialize(NgnMediaType mediaType) {
		final boolean bIsNewScreen = mMediaType == NgnMediaType.None;
		mMediaType = mediaType;
		if (mMediaType == NgnMediaType.Chat) {
			final String validUri = NgnUriUtils.makeValidSipUri(sRemoteParty);
			if (!NgnStringUtils.isNullOrEmpty(validUri)) {
				mSession = NgnMsrpSession
						.getSession(new NgnPredicate<NgnMsrpSession>() {
							@Override
							public boolean apply(NgnMsrpSession session) {
								if (session != null
										&& session.getMediaType() == NgnMediaType.Chat) {
									return NgnStringUtils.equals(
											session.getRemotePartyUri(),
											validUri, false);
								}
								return false;
							}
						});
				if (mSession == null) {
					if ((mSession = NgnMsrpSession.createOutgoingSession(
							mSipService.getSipStack(), NgnMediaType.Chat,
							validUri)) == null) {
						MyLog.d(TAG, "Failed to create MSRP session");
						finish();
						return;
					}
				}
				if (bIsNewScreen && mSession != null) {
					mSession.incRef();
				}
			} else {
				MyLog.d(TAG, "makeValidSipUri(" + sRemoteParty + ") has failed");
				finish();
				return;
			}
		}
	}

	private boolean sendMessage() {

		boolean ret = false;
		final String content = mEtCompose.getText().toString();
		final NgnHistorySMSEvent e = new NgnHistorySMSEvent(sRemoteParty,
				StatusType.Outgoing, "");
		e.setmLocalParty(SystemVarTools.getmIdentity());

		// gzc 20140815
		long time = findMaxHistoryMsgTime();
		e.setEndTime(NgnDateTimeUtils.parseDate(NgnDateTimeUtils.now())
				.getTime());
		e.setStartTime(time);
		e.setContent(content);

		if (GlobalVar.bADHocMode) {
			String uri ;
			if(NgnUriUtils.checkIPAddress(sRemoteParty)){
				uri = String.format("%s@%s", sRemoteParty,sRemoteParty);
			}else{
				uri = SystemVarTools.createContactFromRemoteParty(sRemoteParty).uri;
			}
			
			if (uri == null) {
				MyLog.d(TAG,
						"Sorry, you can't call anyone who is out of your contactList!");
				return false;
			}
			MyLog.d(TAG,
					"ADHOC set cscf host:" + SystemVarTools.getIPFromUri(uri));
			// set pcscf
			((Engine) Engine.getInstance()).getSipService().ADHOC_SetPcscfHost(
					SystemVarTools.getIPFromUri(uri));
		}
		if (!mSipService.isRegisteSessionConnected()) {
			MyLog.d(TAG, "Not registered");
			return false;
		}

		if (CrashHandler.isCdmaNetwork()) {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(sRemoteParty, null, content, null, null);

			mHistorytService.addEvent(e);
			// 聊天室置底
			// 若只有ListView.setselection则无效，原因此时ListView数据还未加载完，解决方法：1、先setAdapter
			// 2、将setselection延时设置
			mLvHistoy.setAdapter(mAdapter);
			mLvHistoy.setSelection(mHistorytService.getEvents().size());

			mEtCompose.setText(NgnStringUtils.emptyValue());

			mTvContentCount.setText("0/" + MAX_MSG_LENGTH);
			return true;
		}

		if (mMediaType == NgnMediaType.Chat) {

			if (mSession != null) {

				ret = mSession.SendMessage(content);
			} else {
				MyLog.d(TAG, "MSRP session is null");
				return false;
			}
		} else {
			final String remotePartyUri = NgnUriUtils
					.makeValidSipUri(sRemoteParty); // sip:19800005001@sunkaisens.com
													// 110 ->
													// sip:110@sunkaisens.com
			final NgnMessagingSession imSession = NgnMessagingSession
					.createOutgoingSession(mSipService.getSipStack(),
							remotePartyUri);

			String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
			String mes = ctreateExpandedField(localMsgID) + "\n\n" + content;
			e.setLocalMsgID(localMsgID); // 保存消息id
											// UEdccc139c-d17c-41c5-9c83-bb3565e8706c
			if (GlobalVar.bADHocMode) {
				String submitTime = NgnDateTimeUtils.cstNow();
				Log.d(TAG, "SubmitTime : " + submitTime);
				mes = ctreateExpandedField(localMsgID, submitTime) + "\n\n"
						+ content;
			}
			if (!(ret = imSession.sendExTextMessage(mes))) {
				e.setStatus(StatusType.Failed);
			}
			NgnMessagingSession.releaseSession(imSession);
		}

		mHistorytService.addEvent(e);
		// 聊天室置底
		// 若只有ListView.setselection则无效，原因此时ListView数据还未加载完，解决方法：1、先setAdapter
		// 2、将setselection延时设置
		mLvHistoy.setAdapter(mAdapter);
		mLvHistoy.setSelection(mHistorytService.getEvents().size());

		mEtCompose.setText(NgnStringUtils.emptyValue());

		mTvContentCount.setText("0/" + MAX_MSG_LENGTH);

		return ret;
	}

	private long findMaxHistoryMsgTime() {
		// 遍历当前对话，使将要发送的消息的
		// 时间戳比最新消息的时间戳大
		long time = 0;
		List<NgnHistoryEvent> mEvents = mHistorytService.getObservableEvents()
				.filter(new HistoryEventChatFilter());
		try {
			if (mEvents != null && mEvents.size() > 0) {
				for (NgnHistoryEvent e1 : mEvents) {
					if (e1.getStartTime() > time) {
						time = e1.getStartTime();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ++time;
	}

	public static boolean sendMessageDirect(String inRemotePartyNO,
			String content) {
		boolean ret = false;

		if (GlobalVar.bADHocMode) {
			String uri ;
			if(NgnUriUtils.checkIPAddress(inRemotePartyNO)){
				uri = String.format("%s@%s", inRemotePartyNO,inRemotePartyNO);
			}else{
				uri = SystemVarTools.createContactFromRemoteParty(inRemotePartyNO).uri;
			}
			
			if (uri == null)
				return false;
			MyLog.d(TAG,
					"message set cscf host:" + SystemVarTools.getIPFromUri(uri));
			// set pcscf
			((Engine) Engine.getInstance()).getSipService().ADHOC_SetPcscfHost(
					SystemVarTools.getIPFromUri(uri));
		}

		final String tempRemotePartyUri = NgnUriUtils
				.makeValidSipUri(inRemotePartyNO);
		final NgnHistorySMSEvent e = new NgnHistorySMSEvent(tempRemotePartyUri,
				StatusType.Outgoing, "");
		e.setContent(content);
		e.setmLocalParty(SystemVarTools.getmIdentity());

		if (!Engine.getInstance().getSipService().isRegisteSessionConnected()) {
			MyLog.d(TAG, "Not registered");
			return false;
		}

		final NgnMessagingSession imSession = NgnMessagingSession
				.createOutgoingSession(Engine.getInstance().getSipService()
						.getSipStack(), tempRemotePartyUri);
		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String mes = ctreateExpandedField_socket(localMsgID, inRemotePartyNO)
				+ "\n\n" + content;
		e.setLocalMsgID(localMsgID); // 保存消息id
										// UEdccc139c-d17c-41c5-9c83-bb3565e8706c
		if (GlobalVar.bADHocMode) {
			String submitTime = NgnDateTimeUtils.cstNow();
			Log.d(TAG, "SubmitTime : " + submitTime);
			mes = ctreateExpandedField_socket(localMsgID, submitTime) + "\n\n"
					+ content;
		}
		if (!(ret = imSession.sendExTextMessage(mes))) {
			e.setStatus(StatusType.Failed);
		}
		NgnMessagingSession.releaseSession(imSession);
		// Engine.getInstance().getHistoryService().addEvent(e);
		return ret;
	}

	public static boolean sendMessageDirectNoHistory(String inRemotePartyNO,
			String content) {
		boolean ret = false;
		final String tempRemotePartyUri = NgnUriUtils
				.makeValidSipUri(inRemotePartyNO);
		final NgnHistorySMSEvent e = new NgnHistorySMSEvent(tempRemotePartyUri,
				StatusType.Outgoing, "");
		e.setContent(content);
		e.setmLocalParty(SystemVarTools.getmIdentity());

		if (!Engine.getInstance().getSipService().isRegisteSessionConnected()) {
			MyLog.d(TAG, "Not registered");
			return false;
		}

		final NgnMessagingSession imSession = NgnMessagingSession
				.createOutgoingSession(Engine.getInstance().getSipService()
						.getSipStack(), tempRemotePartyUri);
		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String mes = ctreateExpandedField_socket(localMsgID, inRemotePartyNO)
				+ "\n\n" + content;
		e.setLocalMsgID(localMsgID); // 保存消息id
										// UEdccc139c-d17c-41c5-9c83-bb3565e8706c
		if (!(ret = imSession.sendExTextMessage(mes))) {
			e.setStatus(StatusType.Failed);
		}
		NgnMessagingSession.releaseSession(imSession);
		return ret;
	}

	public static boolean sendMessageDirectNoHistory(NgnHistorySMSEvent e) {
		boolean ret = false;
		final String tempRemotePartyUri = NgnUriUtils.makeValidSipUri(e
				.getRemoteParty());

		if (!Engine.getInstance().getSipService().isRegisteSessionConnected()) {
			MyLog.d(TAG, "Not registered");
			return false;
		}

		final NgnMessagingSession imSession = NgnMessagingSession
				.createOutgoingSession(Engine.getInstance().getSipService()
						.getSipStack(), tempRemotePartyUri);
		String localMsgID = e.getLocalMsgID();
		String mes = ctreateExpandedField_socket(localMsgID, e.getRemoteParty())
				+ "\n\n" + e.getContent();
		if (!(ret = imSession.sendExTextMessage(mes))) {
			e.setStatus(StatusType.Failed);
		}
		NgnMessagingSession.releaseSession(imSession);
		return ret;
	}

	// public String ctreateExpandedField() {
	public String ctreateExpandedField(String localMsgID) {
		String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
		String msgType = "IM";
		if (userinfo.isgroup) {
			msgType = "GM" + userinfo.mobileNo;
		}
		// String msgReport = "No";
		String msgReport = "Yes";
		// String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String contentType = "text/plain";
		MessageBodyInfo msgBodyInfo = new MessageBodyInfo(nameSpace, msgType,
				msgReport, localMsgID, contentType);
		String strBody = msgBodyInfo.toString();
		return strBody;
	}

	public String ctreateExpandedField(String localMsgID, String submitTime) {
		String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
		String msgType = "IM";
		if (userinfo.isgroup) {
			msgType = "GM" + userinfo.mobileNo;
		}
		// String msgReport = "No";
		String msgReport = "Yes";
		// String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String contentType = "text/plain";
		MessageBodyInfo msgBodyInfo = new MessageBodyInfo(nameSpace, msgType,
				msgReport, localMsgID, contentType, submitTime);
		String strBody = msgBodyInfo.toString();
		return strBody;
	}

	public static String ctreateExpandedField_socket(String localMsgID,
			String remoteNO, String submitTime) {

		ModelContact mc = SystemVarTools.getContactFromPhoneNumber(remoteNO);

		String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
		String msgType = "IM";
		if (mc.isgroup) {
			msgType = "GM" + mc.mobileNo;
		}
		// String msgReport = "No";
		String msgReport = "Yes";
		// String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String contentType = "text/plain";
		MessageBodyInfo msgBodyInfo = new MessageBodyInfo(nameSpace, msgType,
				msgReport, localMsgID, contentType, submitTime);
		String strBody = msgBodyInfo.toString();
		return strBody;
	}

	public static String ctreateExpandedField_socket(String localMsgID,
			String remoteNO) {

		ModelContact mc = SystemVarTools.getContactFromPhoneNumber(remoteNO);

		String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
		String msgType = "IM";
		if (mc.isgroup) {
			msgType = "GM" + mc.mobileNo;
		}
		// String msgReport = "No";
		String msgReport = "Yes";
		// String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String contentType = "text/plain";
		MessageBodyInfo msgBodyInfo = new MessageBodyInfo(nameSpace, msgType,
				msgReport, localMsgID, contentType);
		String strBody = msgBodyInfo.toString();
		return strBody;
	}

	public static void startChat(String remoteParty, boolean bIsPagerMode) {
		final Engine engine = (Engine) NgnEngine.getInstance();
		if (!NgnStringUtils.isNullOrEmpty(remoteParty)
				&& remoteParty.startsWith("sip:")) {
			remoteParty = NgnUriUtils.getUserName(remoteParty);
		}

		if (NgnStringUtils.isNullOrEmpty((sRemoteParty = remoteParty))) {
			MyLog.d(TAG, "Null Uri");
			return;
		}

		if (engine.getScreenService().show(ScreenChat.class)) {
			final IBaseScreen screen = engine.getScreenService().getScreen(TAG);
			if (screen instanceof ScreenChat) {
				((ScreenChat) screen)
						.initialize(bIsPagerMode ? NgnMediaType.SMS
								: NgnMediaType.Chat);
			}
		}
	}

	//
	// HistoryEventSMSFilter
	//
	// static class HistoryEventChatFilter implements
	// NgnPredicate<NgnHistoryEvent> {
	public static class HistoryEventChatFilter implements
			NgnPredicate<NgnHistoryEvent> {
		@Override
		public boolean apply(NgnHistoryEvent event) {

			if (event != null
					&& event.getmLocalParty().equals(
							SystemVarTools.getmIdentity())
					&& (event.getMediaType() == NgnMediaType.SMS)) {

				NgnHistorySMSEvent SMSEvent = (NgnHistorySMSEvent) event;

				if (!SMSEvent.getIsDraft().equals("true")) {
					return NgnStringUtils.equals(sRemoteParty,
							event.getRemoteParty(), false);
				}
			}
			return false;
		}
	}

	//
	// DateComparator
	//
	// static class DateComparator implements Comparator<NgnHistoryEvent> {
	public static class DateComparator implements Comparator<NgnHistoryEvent> {
		@Override
		public int compare(NgnHistoryEvent e1, NgnHistoryEvent e2) {
			return (int) (e1.getStartTime() - e2.getStartTime());
		}
	}

	/**
	 * long click to copy
	 * 
	 * @param content
	 * 
	 *            content that will be copied
	 */
	private ClipboardManager clipboardManager;

	private void doCopy(String content) {
		// clipboardManager.setText(content);
		clipboardManager.setPrimaryClip(ClipData.newPlainText("data", content));
	}

	private void doDelete(NgnHistorySMSEvent event) {
		if (event != null) {
			mHistorytService.deleteEvent(event);
			MyLog.d(TAG, "删除短信");
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MyLog.d(TAG, "onCreateContextMenu");
		if (menuInfo instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo acm = (AdapterContextMenuInfo) menuInfo;
			if (acm.position < mAdapter.getCount()) {
				NgnHistorySMSEvent event = (NgnHistorySMSEvent) mAdapter
						.getItem(acm.position);
				if (!event.getContent().contains("file")) {
					menu.addSubMenu(0, 1, Menu.NONE,
							ScreenChat.this.getString(R.string.copy));
				}
				menu.setHeaderTitle(userinfo.name);
				menu.addSubMenu(0, 2, Menu.NONE,
						ScreenChat.this.getString(R.string.delete));// gzc
																	// 20141025
			} else {
				MyLog.d(TAG, "The selected is out if index.");
			}

		}
	}

	/**
	 * 非聊天室界面调用，发送选中的图片
	 * 
	 * @author jgc
	 * */
	protected void sendtakephoto(String uri, String usernum) {

		MyLog.d(TAG, "sendtakephoto uri:" + uri);

		if (uri == null || uri.isEmpty())
			return;

		File file = new File(uri);
		MyLog.d(TAG, "sendtakephoto 照片大小：" + file.length());

		if (SKDroid.sks_version == VERSION.NORMAL && file.length() > MAX_FILE) {
			MyLog.d(TAG, "sendtakephoto 照片大小大于25M");
			SystemVarTools
					.showToast(ScreenChat.this
							.getString(R.string.pic_more_than_25), false);
			return;
		}

		// if (file.length() == 0) {
		// MyLog.d(TAG, "sendtakephoto 图片文件长度为0");
		// SystemVarTools.showToast("  不可发送0KB的文件，请重试  ", false);
		// return;
		// }

		NgnHistorySMSEvent selectpictureevent = new NgnHistorySMSEvent(
				sRemoteParty, StatusType.Outgoing, ""); // 19800005001
		selectpictureevent.setmLocalParty(SystemVarTools.getmIdentity());

		long maxHistoryTime = this.findMaxHistoryMsgTime();
		selectpictureevent.setStartTime(maxHistoryTime);
		selectpictureevent.setEndTime(new Date().getTime());

		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		selectpictureevent.setLocalMsgID(localMsgID);

		uploadFile(selectpictureevent, uri, usernum);
	}

	/**
	 * 录完即时语音发送，在聊天室界面
	 * 
	 * @author jgc
	 * */
	public void sendIMaudio() {

		// 发送录制的音频
		String finfishedaudioPath = recAudioFile.getPath();

		if (finfishedaudioPath == null || finfishedaudioPath.isEmpty())
			return;

		File file = new File(finfishedaudioPath);
		MyLog.d(TAG, "sendIMaudio 录制音频大小：" + file.length());

		if (SKDroid.sks_version == VERSION.NORMAL && file.length() > MAX_FILE) {
			MyLog.d(TAG, "sendIMaudio 录制音频大于25M");
			SystemVarTools.showToast(
					ScreenChat.this.getString(R.string.audio_more_than_25),
					false);
			return;
		}

		if (file.length() == 0) { // 如果文件为0OK，表示录制失败
			MyLog.d(TAG, "sendIMaudio 音频文件长度为0");
			SystemVarTools.showToast(
					ScreenChat.this.getString(R.string.audio_record_failed),
					false);
			return;
		}

		NgnHistorySMSEvent finfishedaudioevent = new NgnHistorySMSEvent(
				sRemoteParty, StatusType.Outgoing, "");
		finfishedaudioevent.setmLocalParty(SystemVarTools.getmIdentity());

		long maxHistoryTime = this.findMaxHistoryMsgTime();
		finfishedaudioevent.setStartTime(maxHistoryTime);
		finfishedaudioevent.setEndTime(new Date().getTime());

		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		finfishedaudioevent.setLocalMsgID(localMsgID);

		uploadFile(finfishedaudioevent, finfishedaudioPath, null);

	}

	/**
	 * 在聊天室界面发送选中的文件
	 * 
	 * @author jgc
	 * */
	public void sendChoosedFile(String filePath) {

		String finishedFilePath = filePath;

		if (finishedFilePath == null || finishedFilePath.isEmpty())
			return;

		File file = new File(finishedFilePath);
		MyLog.d(TAG, "sendChoosedFile 选择文件大小：" + file.length());

		if (SKDroid.sks_version == VERSION.NORMAL && file.length() > MAX_FILE) {
			MyLog.d(TAG, "sendChoosedFile 文件大于25M");
			SystemVarTools.showToast(
					ScreenChat.this.getString(R.string.file_more_than_25),
					false);
			return;
		}

		// if (file.length() == 0) {
		// MyLog.d(TAG, "sendChoosedFile 文件长度为0");
		// SystemVarTools.showToast("  不可发送0KB的文件，请重试  ", false);
		// return;
		// }

		// //限制一次最多两个文件同时上传
		// if(FileHttpUpLoadClient.uploadList!=null &&
		// FileHttpUpLoadClient.uploadList.size()>=2){
		// Log.e("", "文件多于2个");
		// SystemVarTools.showToast("  请等待已有文件上传结束  ", false);
		// return;
		// }

		NgnHistorySMSEvent finishedevent = new NgnHistorySMSEvent(sRemoteParty,
				StatusType.Outgoing, "");
		finishedevent.setmLocalParty(SystemVarTools.getmIdentity());

		long maxHistoryTime = this.findMaxHistoryMsgTime();
		finishedevent.setStartTime(maxHistoryTime);
		finishedevent.setEndTime(new Date().getTime());

		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		finishedevent.setLocalMsgID(localMsgID);

		uploadFile(finishedevent, finishedFilePath, null);

	}

	/**
	 * 其他界面调用，发送选中文件
	 * 
	 * @author jgc
	 * */
	public void sendChoosedFile(String filePath, String number) {

		// 发送录制的音频
		String finfishedaudioPath = filePath;

		if (finfishedaudioPath == null || finfishedaudioPath.isEmpty())
			return;

		File file = new File(finfishedaudioPath);
		MyLog.d(TAG, "sendChoosedFile 选择文件大小：" + file.length());

		if (SKDroid.sks_version == VERSION.NORMAL && file.length() > MAX_FILE) {
			MyLog.d(TAG, "sendChoosedFile  选择文件大于25M");
			SystemVarTools.showToast(
					ScreenChat.this.getString(R.string.file_more_than_25_2),
					false);
			return;
		}

		// if (file.length() == 0) {
		// MyLog.d(TAG, "sendChoosedFile 文件长度为0");
		// SystemVarTools.showToast("  不可发送0KB的文件，请重试  ", false);
		// return;
		// }

		NgnHistorySMSEvent finfishedaudioevent = new NgnHistorySMSEvent(
				sRemoteParty, StatusType.Outgoing, "");
		finfishedaudioevent.setmLocalParty(SystemVarTools.getmIdentity());

		long maxHistoryTime = this.findMaxHistoryMsgTime();
		finfishedaudioevent.setStartTime(maxHistoryTime);
		finfishedaudioevent.setEndTime(new Date().getTime());

		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		finfishedaudioevent.setLocalMsgID(localMsgID);

		uploadFile(finfishedaudioevent, finfishedaudioPath, number);

	}

	/**
	 * 其他界面调用，发送录制好的即时视频
	 * 
	 * @author jgc
	 * */
	public void sendIMvideo(String uri, String usernum) {

		MyLog.d(TAG, "sendIMvideo uri:" + uri);

		if (uri == null || uri.isEmpty())
			return;

		File file = new File(uri);
		MyLog.d(TAG, "sendIMvideo 录制视频大小：" + file.length());

		if (SKDroid.sks_version == VERSION.NORMAL && file.length() > MAX_FILE) {
			MyLog.d(TAG, "sendIMvideo 录制视频大于25M");
			SystemVarTools.showToast(
					ScreenChat.this.getString(R.string.video_more_than_25),
					false);
			return;
		}

		if (file.length() == 0) { // 如果即时视频文件为0KB,则表示录制失败
			MyLog.d(TAG, "sendIMvideo 文件长度为0");
			SystemVarTools.showToast(
					ScreenChat.this.getString(R.string.video_record_failed),
					false);
			return;
		}

		NgnHistorySMSEvent finfishedvideoevent = new NgnHistorySMSEvent(
				sRemoteParty, StatusType.Outgoing, ""); // 19800005001
		finfishedvideoevent.setmLocalParty(SystemVarTools.getmIdentity());

		long maxHistoryTime = this.findMaxHistoryMsgTime();
		finfishedvideoevent.setStartTime(maxHistoryTime);
		finfishedvideoevent.setEndTime(new Date().getTime());

		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		finfishedvideoevent.setLocalMsgID(localMsgID);

		uploadFile(finfishedvideoevent, uri, usernum);

	}

	private void uploadFile(NgnHistorySMSEvent event, String path, String userno) {

		String[] selects = path.split("/");

		String name = URLEncoder.encode(selects[selects.length - 1]);
		MyLog.d("ywh", "URLEncoder.encode name: "+name);
		String toUserNo = event.getRemoteParty();
		StringBuilder tag = new StringBuilder();
		tag.append(name);
		tag.append(toUserNo);
		// if (FileHttpUpLoadClient.uploadList.contains(tag.toString())) {
		// SystemVarTools.showToast("文件正在上传，请稍后...");
		// return;
		// }

		ModelFileTransport fileModel = new ModelFileTransport();

		fileModel.type = "file";
		if (userno == null) {
			fileModel.url = userinfo.mobileNo + "_" + event.getLocalMsgID()
					+ "_" + URLEncoder.encode(selects[selects.length - 1]);
		} else {
			fileModel.url = userno + "_" + event.getLocalMsgID() + "_"
					+ URLEncoder.encode(selects[selects.length - 1]);
		}
		fileModel.status = "sending";

		fileModel.name = name;
		fileModel.localPath = path.toString();

		MyLog.d(TAG, "uploadFile 文件上传:" + fileModel.toString_send());
		event.setContent(fileModel.toString_send());

		// 不可直接用mHistoryService
		NgnEngine.getInstance().getHistoryService().addEvent(event);

		// 聊天室置底
		// 若只有ListView.setselection则无效，原因此时ListView数据还未加载完，解决方法：1、先setAdapter
		// 2、将setselection延时设置
		if (mLvHistoy != null && mAdapter != null) {
			mLvHistoy.setAdapter(mAdapter);
			mLvHistoy.setSelection(NgnEngine.getInstance().getHistoryService()
					.getEvents().size());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		NgnHistorySMSEvent event = (NgnHistorySMSEvent) mAdapter
				.getItem(info.position);
		switch (item.getItemId()) {
		case 1:
			doCopy(event.getContent());
			break;

		case 2:
			doDelete(event);
			this.refresh();
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {

	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	private void updateUserinfo() {
		userinfo = SystemVarTools.createContactFromRemoteParty(sRemoteParty);
		mTvName.setText(userinfo.name);
		mTvName.setSelected(true);

		// 如果多媒体聊天选项展开了，则关闭
		mViewFiletransfer_view.setVisibility(View.GONE);
		mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);
		zhankai = true;

		// 按住说话如果展开，则关闭
		mBtbottom_IMaudioTalk_button_button.setVisibility(View.GONE);
		mLinearLayoutEditText.setVisibility(View.VISIBLE);
		mBt_IMaudio.setImageResource(R.drawable.screenchat_imaudeo);
		im_audiozhankai = false;

		if (userinfo.isgroup) {
			mBt_AudioCallTextView.setText(ScreenChat.this
					.getString(R.string.string_incall_group_audio));
			mBt_VideoCallTextView.setText(ScreenChat.this
					.getString(R.string.string_incall_group_video));
		} else {
			mBt_AudioCallTextView.setText(ScreenChat.this
					.getString(R.string.call_desc_audio));
			mBt_VideoCallTextView.setText(ScreenChat.this
					.getString(R.string.call_desc_video));
		}

		if (userinfo.isgroup) {
			mBtShowNumLayout.setVisibility(View.VISIBLE);
			mBtShowNum.setImageResource(R.drawable.shownumber);

		} else {
			mBtShowNumLayout.setVisibility(View.VISIBLE);

			SystemVarTools.showicon(mBtShowNum, userinfo, this);
		}

		List<NgnHistoryEvent> mEvents = mHistorytService.getObservableEvents()
				.filter(new MyHistoryEventSMSIntelligentFilter());

		draftString = "";
		for (int i = 0; i < mEvents.size(); i++) {
			NgnHistorySMSEvent SMSEvent = (NgnHistorySMSEvent) mEvents.get(i);
			if (SMSEvent.getIsDraft().equals("true")
					&& SMSEvent.getRemoteParty().equals(sRemoteParty)) {
				MyLog.d("ScreenChat ", "updateUserinfo 删除event" + i);
				draftString = SMSEvent.getDraftString();
				mHistorytService.deleteEvent(SMSEvent);
				refresh();
			}

		}

		mEtCompose.setText(draftString);

	}

	// 对短信记录进行升序排序 gzc 20141108
	public List<NgnHistoryEvent> sortEvents(List<NgnHistoryEvent> events) {
		if (events == null || events.size() < 1) {
			return events;
		}
		List<NgnHistoryEvent> tmpList = new ArrayList<NgnHistoryEvent>();
		tmpList.addAll(events);
		synchronized (events) {
			NgnHistoryEvent tmpEvent = null;
			for (int i = 0; i < tmpList.size(); i++) {
				for (int j = 1; j < tmpList.size() - i; j++) {
					if (tmpList.get(j - 1).getStartTime() > tmpList.get(j)
							.getStartTime()) {
						tmpEvent = tmpList.get(j);
						tmpList.set(j, tmpList.get(j - 1));
						tmpList.set(j - 1, tmpEvent);
					}
				}
			}
		}

		return tmpList;
	}

}
