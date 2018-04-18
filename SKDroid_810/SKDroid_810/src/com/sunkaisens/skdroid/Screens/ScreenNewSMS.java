package com.sunkaisens.skdroid.Screens;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;
import com.sunkaisens.skdroid.Screens.ScreenChat.HistoryEventChatFilter;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.FileHttpDownLoadClient;
import com.sunkaisens.skdroid.Utils.FileHttpUpLoadClient;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenNewSMS extends BaseScreen {

	private static String TAG = ScreenNewSMS.class.getCanonicalName();

	private final INgnHistoryService mHistorytService;
	private final INgnSipService mSipService;

	private InputMethodManager mInputMethodManager;

	private static String sRemoteParty;

	private NgnMsrpSession mSession;
	private NgnMediaType mMediaType;

	private EditText mEtName;
	private EditText mEtCompose;

	private Button mBtSend;
	private ImageView mBtadd_filetransfer_imagebutton;
	private ImageButton mBtFiletransfer;
	private View mViewFiletransfer_view;
	private RelativeLayout mLinearLayoutFiletransfer_ll;

	private static final int SELECT_CONTENT = 999;
	private static final int CHECH_NUMBER = 1000;

	// 标识信息长度的textView
	private TextView mTvContentCount;

	private static boolean zhankai = true; // 传输文件按钮是否展开
	private static boolean im_audiozhankai = true;
	private ImageButton mBt_takephoto_button; // 拍照按钮
	private static final int SELECT_PICTURE = 4;
	private static final int SELECT_CAMERA = 5;

	private ImageView mBt_IMaudio; // 及时语音按钮

	private ImageButton mBt_IMvideoButton; // 即时视频按钮

	private ImageButton mBt_AudioCallButton; // 语音通话按钮
	private ImageButton mBt_VideoCallButton; // 视频通话按钮

	private MediaRecorder mMediaRecorder;

	private static final String SDPATH = Environment
			.getExternalStorageDirectory() + "/SKDroidFiles/"; // 文件夹路径

	private File recAudioFile;

	private RelativeLayout mLinearLayoutEditText; // 及时语音对话布局文件
	// private Animation animation_rotateAnimation;

	private Button mBtbottom_IMaudioTalk_button_button;

	// private ImageButton mBtInfo;
	private ImageView mBtBack;
	// 标识信息长度的textView

	private final INgnConfigurationService mConfigurationService;

	// private ModelContact userinfo = null;
	// public ModelContact userinfo = null;

	// upload map,wangds added 2014.7.12.
	public static HashMap<String, FileHttpUpLoadClient> uploadMap = new HashMap<String, FileHttpUpLoadClient>();
	// download map,wangds added 2014.7.12
	public HashMap<String, FileHttpDownLoadClient> downloadMap = new HashMap<String, FileHttpDownLoadClient>();

	private long downtime = 0;
	private long uptime = 0;
	private long jiangetime;

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

	public ScreenNewSMS() {
		super(SCREEN_TYPE.NEW_SMS_T, TAG);

		mMediaType = NgnMediaType.None;
		mHistorytService = getEngine().getHistoryService();
		mSipService = getEngine().getSipService();
		mConfigurationService = getEngine().getConfigurationService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_new_sms);

		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mEtName = (EditText) findViewById(R.id.screen_newsms_editText_name);
		mEtName.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);
			}
		});
		ImageButton add_contact = (ImageButton) findViewById(R.id.add_contact);
		add_contact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("message", "test");
				intent.setClass(ScreenNewSMS.this, ScreenContactAdd.class);
				startActivityForResult(intent, CHECH_NUMBER);
			}
		});

		mTvContentCount = (TextView) findViewById(R.id.newMsg_tv_count);
		mTvContentCount.setText("0/210");

		mEtCompose = (EditText) findViewById(R.id.newMsg_editText_compose);
		mBtSend = (Button) findViewById(R.id.newMsg_button_send);
		mBtadd_filetransfer_imagebutton = (ImageView) findViewById(R.id.newMsg_add_filetransfer_imagebutton);
		mBt_takephoto_button = (ImageButton) findViewById(R.id.newMsg_button_takephoto_button);
		mBt_IMaudio = (ImageView) findViewById(R.id.newMsg_button_IMaudio_button);
		mBt_IMvideoButton = (ImageButton) findViewById(R.id.newMsg_button_IMvideo_button);

		mBt_AudioCallButton = (ImageButton) findViewById(R.id.newMsg_button_audiocall_button);
		mBt_VideoCallButton = (ImageButton) findViewById(R.id.newMsg_button_videocall_button);

		mBtFiletransfer = (ImageButton) findViewById(R.id.newMsg_button_filetransfer_button);
		mViewFiletransfer_view = (View) findViewById(R.id.newMsg_linearLayout_bottom_filetransfer_view);
		mLinearLayoutFiletransfer_ll = (RelativeLayout) findViewById(R.id.newMsg_linearLayout_bottom_filetransfer_ll);
		mLinearLayoutEditText = (RelativeLayout) findViewById(R.id.newMsg_editText_compose_parentlayout);
		mBtbottom_IMaudioTalk_button_button = (Button) findViewById(R.id.newMsg_linearLayout_bottom_IMaudioTalk_button);

		// add by jgc 2014.12.23 根据麦克的音量改变录音的图标
		LayoutInflater audio_loudinInflater = getLayoutInflater();
		final View audio_loudvView = audio_loudinInflater.inflate(
				R.layout.screen_chat_audiorecorder, null);
		audio_loudImageView = (ImageView) audio_loudvView
				.findViewById(R.id.screen_chat_audiorecorder_Imageview2);
		audio_loudImageView.setImageResource(R.drawable.audio_1);

		selectDialog = new Dialog(ScreenNewSMS.this, R.style.dialog);
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

		mBtBack = (ImageView) findViewById(id.newMsg_back);

		mBtBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mScreenService.back();
				back();
			}
		});

		mBtSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mSipService.isRegisteSessionConnected()
						&& !NgnStringUtils.isNullOrEmpty(mEtCompose.getText()
								.toString())) {
					String name = mEtName.getText().toString().trim();
					if (!NgnStringUtils.isNullOrEmpty(name)) {
						String[] idArray = name.split(";");
						String idStartChat = "";
						final String content = mEtCompose.getText().toString();
						for (int i = 0; i < idArray.length; i++) {
							if (idArray[i] != null && !idArray[i].isEmpty()) {
								ModelContact info = SystemVarTools
										.getContactFromPhoneNumber(idArray[i]);
								if (info == null) {
									info = new ModelContact();
									info.name = idArray[i];
									info.mobileNo = idArray[i];
									info.isgroup = false;
								}
								if (info.name == null
										&& isContainCharacter(info.mobileNo)) {
									SystemVarTools.showToast(String.format(
											"您输入的号码:\"%s\"不正确！", info.mobileNo));
									// continue;
								} else {
									sendMessage(info.mobileNo, content,
											info.isgroup);
									if (idStartChat.isEmpty()) {
										idStartChat = info.mobileNo;
									}
								}

							}
						}
						if (!idStartChat.isEmpty()) {
							ScreenChat.startChat(idStartChat, true);
						}
					} else {
						SystemVarTools.showToast(ScreenNewSMS.this
								.getString(R.string.input_mobile_number));
						mEtName.requestFocus();

						mTvContentCount.setText("0/210");
					}
				}
				mEtCompose.setText(NgnStringUtils.emptyValue());
				mEtName.setText(NgnStringUtils.emptyValue());
				if (mInputMethodManager != null) {
					mInputMethodManager.hideSoftInputFromWindow(
							mEtCompose.getWindowToken(), 0);
				}

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
							zhankai = false;
						} else {

							mViewFiletransfer_view.setVisibility(View.GONE);
							mLinearLayoutFiletransfer_ll
									.setVisibility(View.GONE);
							zhankai = true;
						}
					}
				});

		mBtFiletransfer.setPadding(10, 5, 10, 5); // 传文件
		mBtFiletransfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				zhankai = true;

				mViewFiletransfer_view.setVisibility(View.GONE);
				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

				if (mSipService.isRegisteSessionConnected()) {

					String name = mEtName.getText().toString().trim();
					if (!NgnStringUtils.isNullOrEmpty(name)) {
						String[] idArray = name.split(";");

						if (idArray.length == 1) {

							ModelContact info = SystemVarTools
									.createContactFromPhoneNumber(idArray[0]);
							if (info.name == null
									&& isContainCharacter(info.mobileNo)) {
								SystemVarTools.showToast(String.format(
										ScreenNewSMS.this
												.getString(R.string.your_input)
												+ "\"%s\""
												+ ScreenNewSMS.this
														.getString(R.string.error_mark),
										info.mobileNo));
							} else {

								Intent intent = new Intent();
								intent.setType("*/*")
										.addCategory(Intent.CATEGORY_OPENABLE)
										.setAction(Intent.ACTION_GET_CONTENT);
								startActivityForResult(
										Intent.createChooser(
												intent,
												ScreenNewSMS.this
														.getString(R.string.choose_file)),
										SELECT_CONTENT);

							}

						} else {
							SystemVarTools.showToast(ScreenNewSMS.this
									.getString(R.string.choose_one_person));
						}

					} else {
						SystemVarTools.showToast(ScreenNewSMS.this
								.getString(R.string.input_mobile_number));
						mEtName.requestFocus();
					}

				}
			}
		});

		// add by jgc 2014.11.17 拍照按钮单击弹出选择对话框
		mBt_takephoto_button.setPadding(10, 5, 10, 5);
		mBt_takephoto_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				zhankai = true;

				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

				String name = mEtName.getText().toString().trim();
				if (!NgnStringUtils.isNullOrEmpty(name)) {
					String[] idArray = name.split(";");

					if (idArray.length == 1) {

						ModelContact info = SystemVarTools
								.createContactFromPhoneNumber(idArray[0]);
						if (info.name == null
								&& isContainCharacter(info.mobileNo)) {
							SystemVarTools.showToast(String.format(
									ScreenNewSMS.this
											.getString(R.string.your_input)
											+ "\"%s\""
											+ ScreenNewSMS.this
													.getString(R.string.error_mark),
									info.mobileNo));
						} else {

							getimage();

						}

					} else {
						SystemVarTools.showToast(ScreenNewSMS.this
								.getString(R.string.choose_one_person));
					}

				} else {
					SystemVarTools.showToast(ScreenNewSMS.this
							.getString(R.string.input_mobile_number));
					mEtName.requestFocus();
				}

			}
		});

		im_audiozhankai = false;
		mBt_IMaudio.setPadding(10, 5, 10, 5);
		mBt_IMaudio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				zhankai = true;

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

				if (!GlobalVar.mCameraIsUsed) {

					ModelContact info = new ModelContact();

					boolean iserror = false;
					String name = mEtName.getText().toString().trim();
					if (!NgnStringUtils.isNullOrEmpty(name)) {

						String[] idArray = name.split(";");
						if (idArray.length > 1) {
							SystemVarTools.showToast(ScreenNewSMS.this
									.getString(R.string.choose_one_person));
							iserror = true;
						} else {
							info = SystemVarTools
									.createContactFromPhoneNumber(idArray[0]);
							if (info.name == null && info.mobileNo != null
									&& isContainCharacter(info.mobileNo)) {
								SystemVarTools.showToast(String.format(
										ScreenNewSMS.this
												.getString(R.string.your_input)
												+ "\"%s\""
												+ ScreenNewSMS.this
														.getString(R.string.error_mark),
										info.mobileNo));
								iserror = true;
							}
						}

						if (!iserror) {

							ScreenChat.startChat(info.mobileNo, true);

							Intent intent = new Intent();
							intent.putExtra("usernum", info.mobileNo);

							intent.setClass(ScreenNewSMS.this,
									Screen_VideoRecorder.class);

							startActivity(intent);

						}

					} else {
						SystemVarTools.showToast(ScreenNewSMS.this
								.getString(R.string.input_mobile_number));
						mEtName.requestFocus();
					}
				} else {
					SystemVarTools.showToast(ScreenNewSMS.this
							.getString(R.string.camera_is_unavailable));
				}

			}
		});

		mBt_AudioCallButton.setPadding(10, 5, 10, 5);
		mBt_AudioCallButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String name = mEtName.getText().toString().trim();
				if (!NgnStringUtils.isNullOrEmpty(name)) {
					String[] idArray = name.split(";");

					if (idArray.length == 1) {

						ModelContact info = SystemVarTools
								.createContactFromPhoneNumber(idArray[0]);
						if (info.name == null
								&& isContainCharacter(info.mobileNo)) {
							SystemVarTools.showToast(String.format(
									ScreenNewSMS.this
											.getString(R.string.your_input)
											+ "\"%s\""
											+ ScreenNewSMS.this
													.getString(R.string.error_mark),
									info.mobileNo));
						} else {

							if (CrashHandler.isNetworkAvailable2()) {
								ServiceAV.makeCall(info.mobileNo,
										NgnMediaType.Audio,
										SessionType.AudioCall);
							} else {
								SystemVarTools.showNotifyDialog(
										ScreenNewSMS.this
												.getString(R.string.tip_net_no_conn_error),
										ScreenNewSMS.this);
							}

						}

					} else {
						SystemVarTools.showToast(ScreenNewSMS.this
								.getString(R.string.choose_one_person));
					}

				} else {
					SystemVarTools.showToast(ScreenNewSMS.this
							.getString(R.string.input_mobile_number));
					mEtName.requestFocus();
				}

			}
		});

		mBt_VideoCallButton.setPadding(10, 5, 10, 5);
		mBt_VideoCallButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String name = mEtName.getText().toString().trim();
				if (!NgnStringUtils.isNullOrEmpty(name)) {
					String[] idArray = name.split(";");

					if (idArray.length == 1) {

						ModelContact info = SystemVarTools
								.createContactFromPhoneNumber(idArray[0]);
						if (info.name == null
								&& isContainCharacter(info.mobileNo)) {
							SystemVarTools.showToast(String.format(
									ScreenNewSMS.this
											.getString(R.string.your_input)
											+ "\"%s\""
											+ ScreenNewSMS.this
													.getString(R.string.error_mark),
									info.mobileNo));
						} else {

							if (CrashHandler.isNetworkAvailable2()) {
								ServiceAV.makeCall(info.mobileNo,
										NgnMediaType.Video,
										SessionType.VideoCall);
							} else {
								SystemVarTools.showNotifyDialog(
										ScreenNewSMS.this
												.getString(R.string.tip_net_no_conn_error),
										ScreenNewSMS.this);
							}

						}

					} else {
						SystemVarTools.showToast(ScreenNewSMS.this
								.getString(R.string.choose_one_person));
					}

				} else {
					SystemVarTools.showToast(ScreenNewSMS.this
							.getString(R.string.input_mobile_number));
					mEtName.requestFocus();
				}

			}
		});

		mBtbottom_IMaudioTalk_button_button
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						MyLog.d(TAG, TAG + "我响应了");

						if (!mSipService.isRegisteSessionConnected()) {

							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								MyLog.d(TAG, TAG + "toast show");
								SystemVarTools.showToast(ScreenNewSMS.this
										.getString(R.string.login_first));
								return false;
							}
						} else {

							if (NgnAVSession.hasActiveSession()) {
								if (event.getAction() == MotionEvent.ACTION_DOWN) {
									MyLog.d(TAG, TAG + "toast show");
									SystemVarTools.showToast(ScreenNewSMS.this
											.getString(R.string.incalling_try_later));
									return false;
								}
							} else {

								String name = mEtName.getText().toString()
										.trim();
								if (!NgnStringUtils.isNullOrEmpty(name)) {
									String[] idArray = name.split(";");

									if (idArray.length == 1) {

										ModelContact info = SystemVarTools
												.createContactFromPhoneNumber(idArray[0]);
										if (info.name == null
												&& isContainCharacter(info.mobileNo)) {

											if (event.getAction() == MotionEvent.ACTION_DOWN) { // 放置toast重复显示

												if (event.getAction() == MotionEvent.ACTION_DOWN) {
													MyLog.d(TAG, TAG
															+ "toast show");

													SystemVarTools.showToast(String
															.format(ScreenNewSMS.this
																	.getString(R.string.your_input)
																	+ "\"%s\""
																	+ ScreenNewSMS.this
																			.getString(R.string.error_mark),
																	info.mobileNo));

												}
											}
										} else {

											if (event.getAction() == MotionEvent.ACTION_DOWN) {
												downtime = event.getEventTime();

												mBtbottom_IMaudioTalk_button_button
														.setBackgroundResource(R.drawable.shape_bg_button_normal2_grey);
												mBtbottom_IMaudioTalk_button_button
														.setText(ScreenNewSMS.this
																.getString(R.string.string_chat_IMaudio_up));
												// mBtbottom_IMaudioTalk_button_button.setTextColor(getApplicationContext().getResources().getColor(R.color.color_white));
												selectDialog.show();

												try {
													startRecorder();
												} catch (Exception e) {
													e.printStackTrace();

												}
											}

											if (event.getAction() == MotionEvent.ACTION_UP) {

												uptime = event.getEventTime();

												jiangetime = uptime - downtime;
												mBtbottom_IMaudioTalk_button_button
														.setBackgroundResource(R.drawable.shape_bg_button_normal_grey);
												mBtbottom_IMaudioTalk_button_button
														.setText(ScreenNewSMS.this
																.getString(R.string.string_chat_IMaudio_down));
												// mBtbottom_IMaudioTalk_button_button.setTextColor(getApplicationContext().getResources().getColor(R.color.color_titleblack));
												selectDialog.dismiss();

												try {
													stopRecorder();

													if (jiangetime < 1000)// 如果按下按钮间隔小于1秒钟，则不发送录音
														Toast.makeText(
																getApplicationContext(),
																ScreenNewSMS.this
																		.getString(R.string.long_pressed_talk),
																Toast.LENGTH_SHORT)
																.show();
													else {

														ScreenChat.startChat(
																info.mobileNo,
																true);

														new ScreenChat()
																.sendChoosedFile(
																		recAudioFile
																				.getPath(),
																		info.mobileNo);

													}

												} catch (Exception e) {
													e.printStackTrace();

												}
												// mLinearLayoutBottomAudioButton.setVisibility(View.GONE);
											}

										}

									} else {

										if (event.getAction() == MotionEvent.ACTION_DOWN) {
											SystemVarTools
													.showToast(ScreenNewSMS.this
															.getString(R.string.choose_one_person));
										}
									}

								} else {

									if (event.getAction() == MotionEvent.ACTION_DOWN) {
										MyLog.d(TAG, TAG + "toast show");
										SystemVarTools.showToast(ScreenNewSMS.this
												.getString(R.string.input_mobile_number));
									}
									mEtName.requestFocus();
								}

							}

						}
						return false;
					}

				});

		mEtCompose.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mBtSend.setEnabled(!NgnStringUtils.isNullOrEmpty(mEtCompose
						.getText().toString()));

				if (!NgnStringUtils.isNullOrEmpty(mEtCompose.getText()
						.toString())) {
					mBtSend.setVisibility(View.VISIBLE);
					mBtadd_filetransfer_imagebutton.setVisibility(View.GONE);
					mViewFiletransfer_view.setVisibility(View.VISIBLE);
					mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

					int length;
					try {
						length = mEtCompose.getText().toString()
								.getBytes(SystemVarTools.encoding_utf8).length;
						mTvContentCount.setText(length + "/" + 210);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

				} else {
					mBtSend.setVisibility(View.GONE);
					mBtadd_filetransfer_imagebutton.setVisibility(View.VISIBLE);
				}
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

				// add by jgc 以下代码至switch是为了解决软键盘弹出时界面上移问题
				mBtSend.setVisibility(View.GONE);
				mBtadd_filetransfer_imagebutton.setVisibility(View.GONE);

				mViewFiletransfer_view.setVisibility(View.VISIBLE);
				mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

				int length;
				try {
					length = mEtCompose.getText().toString()
							.getBytes(SystemVarTools.encoding_utf8).length;
					mTvContentCount.setText(length + "/" + 210);

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
					if ((destLen + srcLen) > 210) {
						return "";
					}
					// if (source.length() < 1 && (dend - dstart >= 1)) {
					// return dest.subSequence(dstart, dend - 1);
					// }
					return source;
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "";
			}
		};
		mEtCompose.setFilters(new InputFilter[] { inputFilter });

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CHECH_NUMBER && resultCode == RESULT_OK) { // 检测号码情况
			String result_value = data.getStringExtra("result");
			String name = mEtName.getText().toString().trim();
			if (!NgnStringUtils.isNullOrEmpty(name)) {
				if (name.indexOf(result_value) != -1) {
					Toast.makeText(
							this,
							ScreenNewSMS.this
									.getString(R.string.this_mobile_num_with_left)
									+ result_value
									+ ScreenNewSMS.this
											.getString(R.string.has_been_added_with_right),
							Toast.LENGTH_SHORT).show();
					return;
				}
				mEtName.setText(name + ";" + result_value);
			} else {
				mEtName.setText(result_value);
			}
		}

		if (requestCode == SELECT_CONTENT && resultCode == RESULT_OK) { // 打开第一个文件传输界面
			String name = mEtName.getText().toString().trim();
			ModelContact info = SystemVarTools
					.createContactFromPhoneNumber(name);
			Uri selectedContentUri = data.getData();
			String selectedContentPath = super.getPath(selectedContentUri);

			if (selectedContentPath == null || selectedContentPath.isEmpty()) {
				return;
			}

			ScreenChat.startChat(info.mobileNo, true);
			new ScreenChat()
					.sendChoosedFile(selectedContentPath, info.mobileNo);

		}

		if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {

			String name = mEtName.getText().toString().trim();

			ModelContact info = SystemVarTools
					.createContactFromPhoneNumber(name);

			Uri selectedContentUri = data.getData();
			String selectedContentPath = super.getPath(selectedContentUri);

			if (selectedContentPath == null || selectedContentPath.isEmpty()) {
				return;
			}

			ScreenChat.startChat(info.mobileNo, true);
			new ScreenChat().sendtakephoto(selectedContentPath, info.mobileNo);

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mInputMethodManager != null) {
			mInputMethodManager.hideSoftInputFromWindow(
					mEtCompose.getWindowToken(), 0);
		}
		super.onPause();
	}

	private boolean sendMessage(String name, String content, boolean isGroup) {
		boolean ret = false;
		// final String name = mEtName.getText().toString();
		// final String content = mEtCompose.getText().toString();
		final NgnHistorySMSEvent e = new NgnHistorySMSEvent(name.trim(),
				StatusType.Outgoing, "");

		e.setmLocalParty(SystemVarTools.getmIdentity());

		long time = findMaxHistoryMsgTime();
		e.setEndTime(NgnDateTimeUtils.parseDate(NgnDateTimeUtils.now())
				.getTime());
		e.setStartTime(time);
		e.setContent(content);

		if (!mSipService.isRegisteSessionConnected()) {
			MyLog.d(TAG, "Not registered");
			return false;
		}
		if (mMediaType == NgnMediaType.Chat) {
			if (mSession != null) {
				ret = mSession.SendMessage(content);
			} else {
				MyLog.d(TAG, "MSRP session is null");
				return false;
			}
		} else {
			final String remotePartyUri = NgnUriUtils.makeValidSipUri(name
					.trim());
			final NgnMessagingSession imSession = NgnMessagingSession
					.createOutgoingSession(mSipService.getSipStack(),
							remotePartyUri);
			String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
			String mes = ctreateExpandedField(localMsgID, isGroup, name)
					+ "\n\n" + content;
			e.setLocalMsgID(localMsgID);
			if (!(ret = imSession.sendExTextMessage(mes))) {
				e.setStatus(StatusType.Failed);
			}
			NgnMessagingSession.releaseSession(imSession);
		}

		// mEtCompose.setText(NgnStringUtils.emptyValue());
		mHistorytService.addEvent(e);

		mTvContentCount.setText("0/210");

		return ret;
	}

	public String ctreateExpandedField(String localMsgID, boolean isGroup,
			String mobileNum) {
		String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
		String msgType = "IM";
		if (isGroup) {
			msgType = "GM" + mobileNum;
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

	public String ctreateExpandedField(String localMsgID) {
		String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
		String msgType = "IM";
		// String msgReport = "No";
		String msgReport = "Yes";
		// String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String contentType = "text/plain";
		MessageBodyInfo msgBodyInfo = new MessageBodyInfo(nameSpace, msgType,
				msgReport, localMsgID, contentType);
		String strBody = msgBodyInfo.toString();
		return strBody;
	}

	public static boolean isContainCharacter(String phoneNumber) {
		int len = phoneNumber.length();
		for (int i = 0; i < len; ++i) {
			if (Character.isLetter(phoneNumber.charAt(i)))
				return true;
		}
		return false;
	}

	private long findMaxHistoryMsgTime() {
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
			MyLog.d(TAG, "findMaxHistoryMsgTime :" + e.getMessage());
		}
		return ++time;
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
			String uri = SystemVarTools
					.createContactFromRemoteParty(sRemoteParty).uri;
			MyLog.e(TAG,
					"audio set cscf host:" + SystemVarTools.getIPFromUri(uri));
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
			mEtCompose.setText(NgnStringUtils.emptyValue());

			mTvContentCount.setText("0/210");
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
			if (!(ret = imSession.sendExTextMessage(mes))) {
				e.setStatus(StatusType.Failed);
			}
			NgnMessagingSession.releaseSession(imSession);
		}

		mHistorytService.addEvent(e);
		mEtCompose.setText(NgnStringUtils.emptyValue());

		mTvContentCount.setText("0/210");

		return ret;
	}

	private void stopRecorder() {
		if (recAudioFile != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
			// mGetAudioDBthread.destroy();
			mMediaRecorder = null;

		}

		if (mGetAudioDBthread != null) {
			mGetAudioDBthread = null;
		}

	}

	// add by jgc 2014.12.17 创建及时语音文件，以时间命名。
	private void startRecorder() {
		// TODO Auto-generated method stub

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
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mMediaRecorder.start();

		if (mGetAudioDBthread == null) {
			mGetAudioDBthread = new Thread(mGetAudioDBRunnable);
			mGetAudioDBthread.start();
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
		CharSequence[] items = { ScreenNewSMS.this.getString(R.string.images),
				ScreenNewSMS.this.getString(R.string.camera) };

		new AlertDialog.Builder(this)
				.setTitle(
						ScreenNewSMS.this.getString(R.string.choose_pic_source))
				.setItems(items, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (which == 0) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);// 自带的浏览文件Activity
							intent.addCategory(Intent.CATEGORY_OPENABLE);

							intent.setType("image/*"); // 这是到达该image路径下的所有文件，默认为内存卡的
							startActivityForResult(Intent.createChooser(intent,
									ScreenNewSMS.this
											.getString(R.string.choose_pic)),
									SELECT_PICTURE);

						} else {

							if (!GlobalVar.mCameraIsUsed) {

								String name = mEtName.getText().toString()
										.trim();
								ModelContact info = SystemVarTools
										.createContactFromPhoneNumber(name);

								ScreenChat.startChat(info.mobileNo, true);

								Intent intent = new Intent();

								intent.putExtra("usernum", info.mobileNo);

								intent.setClass(ScreenNewSMS.this,
										Screen_takephoto_camera.class);

								startActivity(intent);

							}

							else {
								SystemVarTools.showToast(ScreenNewSMS.this
										.getString(R.string.camera_is_unavailable));
							}

						}

					}
				}).create().show();

	}

}
