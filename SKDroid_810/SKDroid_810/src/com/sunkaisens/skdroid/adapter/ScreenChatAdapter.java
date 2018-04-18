package com.sunkaisens.skdroid.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.ScreenChat;
import com.sunkaisens.skdroid.Screens.ScreenChat.HistoryEventChatFilter;
import com.sunkaisens.skdroid.Screens.ScreenPersonInfo;
import com.sunkaisens.skdroid.Services.IServiceScreen;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.DateTimeUtils;
import com.sunkaisens.skdroid.Utils.FileHttpDownLoadClient;
import com.sunkaisens.skdroid.Utils.FileHttpUpLoadClient;
import com.sunkaisens.skdroid.Utils.RoundProgressBar;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.ModelFileTransport;
import com.sunkaisens.skdroid.util.GlobalVar;

/**
 * ScreenChatAdapter
 */
public class ScreenChatAdapter extends BaseAdapter implements Observer {
	private static String TAG = ScreenChatAdapter.class.getCanonicalName();

	private List<NgnHistoryEvent> mEvents;
	private final LayoutInflater mInflater;
	private final Handler mHandler;
	private final ScreenChat mBaseScreen;

	public String localpath;
	public MediaPlayer mplayer = null;

	private static int playingposition = 0;
	private static int istransfer = 0;
	private AnimationDrawable animation = null;
	private AnimationDrawable animation2 = null;
	private ImageLoader mImageLoader;

	public int choosedPosition = -100;

	public ScreenChatAdapter(ScreenChat baseScreen) {
		mBaseScreen = baseScreen;
		mHandler = new Handler();
		mInflater = LayoutInflater.from(mBaseScreen);
		mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(
				new HistoryEventChatFilter());
		// Collections.sort(mEvents,new DateComparator()
		mEvents = mBaseScreen.sortEvents(mEvents);
		mBaseScreen.mHistorytService.getObservableEvents().addObserver(this);

		mImageLoader = new ImageLoader(baseScreen.getBaseContext());

	}

	public void setmEvents(List<NgnHistoryEvent> mEvents) {
		this.mEvents = mEvents;
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		mBaseScreen.mHistorytService.getObservableEvents().deleteObserver(this);
	}

	public void refresh() {
		mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(
				new HistoryEventChatFilter());
		mEvents = mBaseScreen.sortEvents(mEvents);
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			notifyDataSetChanged();
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	}

	@Override
	public int getCount() {
		return mEvents.size();
	}

	@Override
	public Object getItem(int position) {
		return mEvents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View view = null;

		// if (view == null) {

		final NgnHistoryEvent event = (NgnHistoryEvent) getItem(position);
		if (event == null) {
			return null;
		}

		final NgnHistorySMSEvent hisSMSEvent = (NgnHistorySMSEvent) event;

		String content = hisSMSEvent.getContent();
		String remoteNumber = ((ScreenChat) mBaseScreen).userinfo.mobileNo;
		String remoteName = ""; // 用于显示群短信用户名

		ModelContact remoteContact = ((ScreenChat) mBaseScreen).userinfo;

		if (hisSMSEvent.getGMMember() != null
				&& !hisSMSEvent.getGMMember().isEmpty()) {

			remoteNumber = hisSMSEvent.getGMMember();
			MyLog.d(TAG, "remoteNumber " + remoteNumber);
			remoteName = remoteNumber;
			remoteContact = SystemVarTools
					.createContactFromPhoneNumber(remoteNumber);

			if (remoteContact != null && remoteContact.name != null) {
				remoteName = remoteContact.name;
			}

		} else {
			remoteName = "";
			MyLog.d(TAG, "hisSMSEvent.getGMMember() is null");
		}
		if (remoteContact == null) {
			remoteContact = new ModelContact();
			remoteContact.imageid = 0;
			remoteContact.isgroup = false;
			remoteContact.mobileNo = remoteNumber;
			remoteContact.name = remoteNumber;
			remoteContact.parent = "";
			remoteName = remoteContact.name;
		}

		ModelContact meContact = SystemVarTools
				.createContactFromNumberorName(SystemVarTools.getmIdentity());

		// 判断是发送短信还是接受短信
		final boolean bIncoming = hisSMSEvent.getStatus() == StatusType.Incoming;

		hisSMSEvent.setIsSeen("true");
		// mBaseScreen.mHistorytService.updateEvent(hisSMSEvent);

		final int playingpos = position;

		ImageView remoteIcon = null;
		ImageView myIcon = null;

		if (bIncoming) { // 接收

			TextView textSender; // 用于群短信显示发送方名称

			// 判断是否为文件传输，ui做相应调整
			if (content != null && content.startsWith("type:file")) { // 文件短信

				final ModelFileTransport fileModel = new ModelFileTransport();
				fileModel.parseFileContent(content);

				if (content.contains("status:receive")) // 等待接受
				{
					MyLog.d(TAG, "getView &&&&& 文件传输 状态 wait receive");

					view = mInflater.inflate(
							R.layout.screen_chat_item_progress_receive, null);

					// 接收方头像
					remoteIcon = (ImageView) view
							.findViewById(R.id.screen_chat_item_progress_receive_iconleft);

					SystemVarTools.showicon(remoteIcon, remoteContact,
							mBaseScreen);

					// 短信时间
					TextView date = (TextView) view
							.findViewById(R.id.screen_chat_item_progress_receive_textView_date);
					date.setText(DateTimeUtils.getFriendlyDateString(new Date(
							event.getEndTime())));

					// 显示进度条
					ProgressBar progressBar = (ProgressBar) view
							.findViewById(R.id.screen_chat_item_progress_receive_file_progress);
					progressBar.setVisibility(View.GONE);

					TextView filename = (TextView) view
							.findViewById(R.id.screen_chat_item_progress_receive_filename);
					filename.setText(fileModel.name);
					// filename.setText(content);

					// 群短信发送方名称
					textSender = (TextView) view
							.findViewById(R.id.screen_chat_item_progress_receive_textView_sender);
					if (!remoteName.equals("")) {
						textSender.setVisibility(View.VISIBLE);
						textSender.setText(remoteName);
						textSender.setSelected(true);
					} else {
						textSender.setVisibility(View.GONE);
					}

					Button downloadButton = (Button) view
							.findViewById(R.id.screen_chat_item_progress_receive_file_btn);
					downloadButton.setText("下载");

					downloadButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							String contentT = hisSMSEvent.getContent();
							contentT = contentT.replace("receive", "receiving");

							hisSMSEvent.setContent(contentT);

							reSetEvent(fileModel, contentT, hisSMSEvent);

						}
					});

					if (fileModel.name.endsWith(".jpg")
							|| fileModel.name.endsWith(".jpeg")
							|| fileModel.name.endsWith(".png")
							|| fileModel.name.endsWith(".bmp")
							|| fileModel.name.endsWith(".gif")
							|| fileModel.name.endsWith(".JPG")
							|| fileModel.name.endsWith(".JPEG")
							|| fileModel.name.endsWith(".PNG")
							|| fileModel.name.endsWith(".BMP")
							|| fileModel.name.endsWith(".GIF")
							|| fileModel.name.endsWith(".amr")
							|| fileModel.name.endsWith(".mp4")) { // 若是图片和即时录音则直接传输

						filename.setVisibility(View.GONE);
						downloadButton.setVisibility(View.GONE);

						String contentT = hisSMSEvent.getContent();
						contentT = contentT.replace("receive", "receiving");
						hisSMSEvent.setContent(contentT);

						reSetEvent(fileModel, contentT, hisSMSEvent);

					} else {
						filename.setVisibility(View.VISIBLE);
						downloadButton.setVisibility(View.VISIBLE);
					}

				} else if (content.contains("status:receiving")) { // 接受中
					MyLog.d(TAG, "getView &&&&& 文件传输 状态 receiving");

					if (fileModel.name.endsWith(".jpg")
							|| fileModel.name.endsWith(".jpeg")
							|| fileModel.name.endsWith(".png")
							|| fileModel.name.endsWith(".bmp")
							|| fileModel.name.endsWith(".gif")
							|| fileModel.name.endsWith(".JPG")
							|| fileModel.name.endsWith(".JPEG")
							|| fileModel.name.endsWith(".PNG")
							|| fileModel.name.endsWith(".BMP")
							|| fileModel.name.endsWith(".GIF")) {

						view = mInflater.inflate(
								R.layout.screen_chat_item_image_receive, null);

						// 接收方头像
						remoteIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_image_receive_iconleft);

						SystemVarTools.showicon(remoteIcon, remoteContact,
								mBaseScreen);

						// 短信时间
						TextView date = (TextView) view
								.findViewById(R.id.screen_chat_item_image_receive_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 显示进度条
						RoundProgressBar progressBar = (RoundProgressBar) view
								.findViewById(R.id.screen_chat_item_image_receive_progress);

						progressBar.setVisibility(View.VISIBLE);

						// 群短信发送方名称
						textSender = (TextView) view
								.findViewById(R.id.screen_chat_item_image_receive_textView_sender);
						if (!remoteName.equals("")) {
							textSender.setVisibility(View.VISIBLE);
							textSender.setText(remoteName);
							textSender.setSelected(true);
						} else {
							textSender.setVisibility(View.GONE);
						}

						fileDownload(hisSMSEvent, progressBar, position);

					} else if (fileModel.name.contains(".amr")) {

						view = mInflater.inflate(
								R.layout.screen_chat_item_audio_receive, null);

						// 接收方头像
						remoteIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_audio_receive_iconleft);

						SystemVarTools.showicon(remoteIcon, remoteContact,
								mBaseScreen);

						// 短信时间
						TextView date = (TextView) view
								.findViewById(R.id.screen_chat_item_audio_receive_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 显示进度条
						ProgressBar progressBar = (ProgressBar) view
								.findViewById(R.id.screen_chat_item_audio_receive_progress);

						progressBar.setVisibility(View.VISIBLE);

						// 群短信发送方名称
						textSender = (TextView) view
								.findViewById(R.id.screen_chat_item_audio_receive_textView_sender);
						if (!remoteName.equals("")) {
							textSender.setVisibility(View.VISIBLE);
							textSender.setText(remoteName);
							textSender.setSelected(true);
						} else {
							textSender.setVisibility(View.GONE);
						}

						fileDownload(hisSMSEvent, progressBar, position);

					} else if (fileModel.name.contains(".mp4")) {

						view = mInflater.inflate(
								R.layout.screen_chat_item_video_receive, null);

						// 接收方头像
						remoteIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_video_receive_iconleft);
						SystemVarTools.showicon(remoteIcon, remoteContact,
								mBaseScreen);

						// 短信时间
						TextView date = (TextView) view
								.findViewById(R.id.screen_chat_item_video_receive_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 显示进度条
						RoundProgressBar progressBar = (RoundProgressBar) view
								.findViewById(R.id.screen_chat_item_video_receive_progress);
						progressBar.setVisibility(View.VISIBLE);

						// 群短信发送方名称
						textSender = (TextView) view
								.findViewById(R.id.screen_chat_item_video_receive_textView_sender);
						if (!remoteName.equals("")) {
							textSender.setVisibility(View.VISIBLE);
							textSender.setText(remoteName);
							textSender.setSelected(true);
						} else {
							textSender.setVisibility(View.GONE);
						}

						fileDownload(hisSMSEvent, progressBar, position);

					} else {

						view = mInflater.inflate(
								R.layout.screen_chat_item_progress_receive,
								null);

						// 接收方头像
						remoteIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_progress_receive_iconleft);

						SystemVarTools.showicon(remoteIcon, remoteContact,
								mBaseScreen);

						// 短信时间
						TextView date = (TextView) view
								.findViewById(R.id.screen_chat_item_progress_receive_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 显示进度条
						ProgressBar progressBar = (ProgressBar) view
								.findViewById(R.id.screen_chat_item_progress_receive_file_progress);

						TextView filename = (TextView) view
								.findViewById(R.id.screen_chat_item_progress_receive_filename);
						filename.setText(fileModel.name);

						// 群短信发送方名称
						textSender = (TextView) view
								.findViewById(R.id.screen_chat_item_progress_receive_textView_sender);
						if (!remoteName.equals("")) {
							textSender.setVisibility(View.VISIBLE);
							textSender.setText(remoteName);
							textSender.setSelected(true);
						} else {
							textSender.setVisibility(View.GONE);
						}

						Button cancelButton = (Button) view
								.findViewById(R.id.screen_chat_item_progress_receive_file_btn);
						cancelButton.setText(mBaseScreen
								.getString(R.string.cancel));

						cancelButton.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								NgnHistorySMSEvent tagevent = hisSMSEvent;
								if (FileHttpDownLoadClient.downloadMap
										.containsKey(tagevent.getLocalMsgID())) {//
									MyLog.d(TAG,
											"cancelButton onclick 文件传输  取消  LocalMsgID:"
													+ tagevent.getLocalMsgID()
													+ "|"
													+ "download:"
													+ FileHttpDownLoadClient.downloadMap.get(tagevent
															.getLocalMsgID()));
									FileHttpDownLoadClient.downloadMap.get(
											tagevent.getLocalMsgID()).cancel();
								}
							}
						});

						if (fileModel.name.endsWith(".jpg")
								|| fileModel.name.endsWith(".jpeg")
								|| fileModel.name.endsWith(".png")
								|| fileModel.name.endsWith(".bmp")
								|| fileModel.name.endsWith(".gif")
								|| fileModel.name.endsWith(".JPG")
								|| fileModel.name.endsWith(".JPEG")
								|| fileModel.name.endsWith(".PNG")
								|| fileModel.name.endsWith(".BMP")
								|| fileModel.name.endsWith(".GIF")
								|| fileModel.name.contains(".amr")
								|| fileModel.name.contains(".mp4")) {
							filename.setVisibility(View.GONE);
							cancelButton.setVisibility(View.GONE);
							progressBar.setVisibility(View.VISIBLE);

						} else {
							filename.setVisibility(View.VISIBLE);
							cancelButton.setVisibility(View.VISIBLE);
							progressBar.setVisibility(View.VISIBLE);
						}

						fileDownload(hisSMSEvent, progressBar, position);

					}
				} else if (content.contains("status:cancel")
						|| content.contains("status:failed")) { // 接受失败或者取消接受
					MyLog.d(TAG, "getView &&&&& 文件传输 状态 receive cancel/failed");
					view = mInflater.inflate(
							R.layout.screen_chat_item_progress_receive, null);

					// 接收方头像
					remoteIcon = (ImageView) view
							.findViewById(R.id.screen_chat_item_progress_receive_iconleft);
					SystemVarTools.showicon(remoteIcon, remoteContact,
							mBaseScreen);

					// 短信时间
					TextView date = (TextView) view
							.findViewById(R.id.screen_chat_item_progress_receive_textView_date);
					date.setText(DateTimeUtils.getFriendlyDateString(new Date(
							event.getEndTime())));

					// 显示进度条
					ProgressBar progressBar = (ProgressBar) view
							.findViewById(R.id.screen_chat_item_progress_receive_file_progress);
					progressBar.setVisibility(View.GONE);

					TextView filename = (TextView) view
							.findViewById(R.id.screen_chat_item_progress_receive_filename);
					filename.setText(fileModel.name);

					// 群短信发送方名称
					textSender = (TextView) view
							.findViewById(R.id.screen_chat_item_progress_receive_textView_sender);
					if (!remoteName.equals("")) {
						textSender.setVisibility(View.VISIBLE);
						textSender.setText(remoteName);
						textSender.setSelected(true);
					} else {
						textSender.setVisibility(View.GONE);
					}

					Button cancelButton = (Button) view
							.findViewById(R.id.screen_chat_item_progress_receive_file_btn);
					cancelButton.setText(mBaseScreen
							.getString(R.string.rereceive));

					cancelButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							NgnHistorySMSEvent tagevent = hisSMSEvent;
							if (fileModel.name == null)
								return;
							if (fileModel.url == null) {
								SystemVarTools.showToast(mBaseScreen
										.getString(R.string.file_url_error));
								return;
							}
							if (fileModel.savePath == null) {
								SystemVarTools.showToast(mBaseScreen
										.getString(R.string.file_path_not_exit));
								return;
							}

							MyLog.d(TAG,
									"cancelButton onclick   重新接收 tagevent.getLocalMsgID:"
											+ tagevent.getLocalMsgID()
											+ "|"
											+ "download:"
											+ FileHttpDownLoadClient.downloadMap
													.get(tagevent
															.getLocalMsgID()));

							String contentT = hisSMSEvent.getContent();
							if (contentT.contains("receive")) {
								hisSMSEvent.setContent(contentT.replace(
										"status:receive", "status:receiving"));
							} else if (contentT.contains("failed")) {
								hisSMSEvent.setContent(contentT.replace(
										"status:failed", "status:receiving"));
							} else if (contentT.contains("cancel")) {
								hisSMSEvent.setContent(contentT.replace(
										"status:cancel", "status:receiving"));
							}
							// Engine.getInstance().getHistoryService().updateEvent(hisSMSEvent);
							Engine.getInstance().getHistoryService()
									.updateEvent(hisSMSEvent);

							Message msg = Message.obtain(
									mBaseScreen.mRefrashHandler,
									ScreenChat.REFRESH_CHAT);
							mBaseScreen.mRefrashHandler.sendMessage(msg);
						}
					});

				} else if (content.contains("status:ok")) { // 接受成功
					MyLog.d(TAG, "getView &&&&& 文件传输 状态 receive OK");
					if (fileModel.name != null) {
						if (fileModel.name.endsWith(".jpg")
								|| fileModel.name.endsWith(".jpeg")
								|| fileModel.name.endsWith(".png")
								|| fileModel.name.endsWith(".bmp")
								|| fileModel.name.endsWith(".gif")
								|| fileModel.name.endsWith(".JPG")
								|| fileModel.name.endsWith(".JPEG")
								|| fileModel.name.endsWith(".PNG")
								|| fileModel.name.endsWith(".BMP")
								|| fileModel.name.endsWith(".GIF")) {

							view = mInflater.inflate(
									R.layout.screen_chat_item_image_receive,
									null);

							// 接收方头像
							remoteIcon = (ImageView) view
									.findViewById(R.id.screen_chat_item_image_receive_iconleft);
							SystemVarTools.showicon(remoteIcon, remoteContact,
									mBaseScreen);

							// 短信时间
							TextView date = (TextView) view
									.findViewById(R.id.screen_chat_item_image_receive_textView_date);
							date.setText(DateTimeUtils
									.getFriendlyDateString(new Date(event
											.getEndTime())));

							// 显示图片
							ImageView picture = (ImageView) view
									.findViewById(R.id.screen_chat_item_image_receive_picture_preview);

							ImageView imageViewEdge = (ImageView) view
									.findViewById(R.id.screen_chat_item_image_edge);

							// 群短信发送方名称
							textSender = (TextView) view
									.findViewById(R.id.screen_chat_item_image_receive_textView_sender);
							if (!remoteName.equals("")) {
								textSender.setVisibility(View.VISIBLE);
								textSender.setText(remoteName);
								textSender.setSelected(true);

							} else {
								textSender.setVisibility(View.GONE);
							}

							if (fileExists(fileModel.savePath)) {
								mImageLoader.DisplayImage(fileModel.savePath,
										picture, false, imageViewEdge);

								picture.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										Intent intent = new Intent();
										intent.setAction(Intent.ACTION_VIEW);
										File file = new File(fileModel.savePath);
										String type = "image/*";
										intent.setDataAndType(
												Uri.fromFile(file), type);
										mBaseScreen.startActivity(intent);
									}
								});

							} else {

								picture.setImageResource(R.drawable.default_image);

								MyLog.d(TAG, "文件不存在！");
								picture.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										Intent intent = new Intent();
										intent.setAction(Intent.ACTION_VIEW);
										File file = new File(fileModel.savePath);
										String type = "image/*";
										intent.setDataAndType(
												Uri.fromFile(file), type);
										mBaseScreen.startActivity(intent);
									}
								});

							}

						} else if (fileModel.name.contains(".amr")) {

							view = mInflater.inflate(
									R.layout.screen_chat_item_audio_receive,
									null);

							// 接收方头像
							remoteIcon = (ImageView) view
									.findViewById(R.id.screen_chat_item_audio_receive_iconleft);
							SystemVarTools.showicon(remoteIcon, remoteContact,
									mBaseScreen);

							// 短信时间
							TextView date = (TextView) view
									.findViewById(R.id.screen_chat_item_audio_receive_textView_date);
							date.setText(DateTimeUtils
									.getFriendlyDateString(new Date(event
											.getEndTime())));

							// 显示即时音频图标
							final ImageView screen_chat_picture_preView = (ImageView) view
									.findViewById(R.id.screen_chat_item_audio_receive_picture_preview);

							// 群短信发送方名称
							textSender = (TextView) view
									.findViewById(R.id.screen_chat_item_audio_receive_textView_sender);
							if (!remoteName.equals("")) {
								textSender.setVisibility(View.VISIBLE);
								textSender.setText(remoteName);
								textSender.setSelected(true);
							} else {
								textSender.setVisibility(View.GONE);
							}

							final File file = new File(fileModel.savePath);

							if (fileExists(fileModel.savePath)) {

								screen_chat_picture_preView
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												// TODO Auto-generated method
												// stub

												playingposition = playingpos;
												istransfer = 0;

												if (animation2 != null) {
													animation2.stop();
													animation2
															.selectDrawable(2);
													animation2 = null;

												}

												try {

													if (mplayer != null)
														mplayer.release();
													mplayer = null;
													mplayer = new MediaPlayer();

													mplayer.setDataSource(file
															.getPath());
													mplayer.prepare();
													mplayer.start();

												} catch (IOException e) {
													e.printStackTrace();
												}

												if (mplayer == null) {
													return;
												}

												if (animation != null) {
													animation.stop();

													animation.selectDrawable(2);
													animation = null;

												}
												animation = new AnimationDrawable();
												screen_chat_picture_preView
														.setImageResource(R.drawable.receiver_audio_changeicon);

												animation = (AnimationDrawable) screen_chat_picture_preView
														.getDrawable();

												mplayer.setOnCompletionListener(new OnCompletionListener() {

													@Override
													public void onCompletion(
															MediaPlayer mp) {
														if (animation != null) {
															animation.stop();
															animation
																	.selectDrawable(2);
														}
														animation = null;
														MyLog.d(TAG,
																"player complete,animation is null");

														if (animation2 != null) {
															animation2.stop();
															animation2
																	.selectDrawable(2);
															animation2 = null;

														}

														mplayer.stop();
														mplayer.release();
														mplayer = null;
														MyLog.d(TAG,
																"playingcomplete");
														playingposition = -1;

													}
												});

												if (mplayer.isPlaying()) {

													animation.start();

												} else {

													mplayer.stop();
													mplayer.release();
													mplayer = null;
													animation.stop();
													animation.selectDrawable(2);
													animation = null;

												}
											}
										});

								if ((mplayer != null) && mplayer.isPlaying()
										&& (position == playingposition)) {

									screen_chat_picture_preView
											.setImageResource(R.drawable.receiver_audio_changeicon);

									animation2 = (AnimationDrawable) screen_chat_picture_preView
											.getDrawable();

									animation2.start();
									MyLog.d(TAG, "animation不为空");

								} else {

									screen_chat_picture_preView
											.setImageResource(R.drawable.receiver_audio);
								}

							} else {
								screen_chat_picture_preView
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												SystemVarTools.showToast(mBaseScreen
														.getString(R.string.audio_file_has_been_deleted));
											}
										});
							}

						} else if (fileModel.name.contains(".mp4")) {

							view = mInflater.inflate(
									R.layout.screen_chat_item_video_receive,
									null);

							// 接收方头像
							remoteIcon = (ImageView) view
									.findViewById(R.id.screen_chat_item_video_receive_iconleft);
							SystemVarTools.showicon(remoteIcon, remoteContact,
									mBaseScreen);
							// 短信时间
							TextView date = (TextView) view
									.findViewById(R.id.screen_chat_item_video_receive_textView_date);
							date.setText(DateTimeUtils
									.getFriendlyDateString(new Date(event
											.getEndTime())));

							ImageView screen_chat_picture_preView = (ImageView) view
									.findViewById(R.id.screen_chat_item_video_receive_picture_preview);
							ImageView screen_chat_picture_preView_edge = (ImageView) view
									.findViewById(R.id.screen_chat_item_video_edge_receive_picture_preview);

							ImageView playVideo = (ImageView) view
									.findViewById(R.id.screen_chat_item_video_receive_playvideo);
							playVideo.setVisibility(View.VISIBLE);

							// 群短信发送方名称
							textSender = (TextView) view
									.findViewById(R.id.screen_chat_item_video_receive_textView_sender);
							if (!remoteName.equals("")) {
								textSender.setVisibility(View.VISIBLE);
								textSender.setText(remoteName);
								textSender.setSelected(true);
							} else {
								textSender.setVisibility(View.GONE);
							}

							String picturepath = fileModel.savePath.substring(
									0, fileModel.savePath.length() - 4)
									+ ".jpg";

							if (fileExists(picturepath)) { // 截图已存在

								File pictureFile = new File(picturepath);

								if (pictureFile.length() == 0) {
									screen_chat_picture_preView
											.setImageResource(R.drawable.default_image);

								} else {
									mImageLoader.DisplayImage(picturepath,
											screen_chat_picture_preView, false,
											screen_chat_picture_preView_edge);

								}

							} else { // 截图不存在
								if (fileExists(fileModel.savePath)) {

									File pictureFile = new File(picturepath);

									FileOutputStream fout = null;
									try {
										fout = new FileOutputStream(pictureFile);
									} catch (FileNotFoundException e) {
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									}

									MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
									try {
										mediaMetadataRetriever
												.setDataSource(fileModel.savePath);

										Bitmap bitmap = mediaMetadataRetriever
												.getFrameAtTime(200);

										bitmap.compress(
												Bitmap.CompressFormat.JPEG,
												100, fout);

										if (fout != null) {
											fout.flush();

										} else {
											MyLog.d(TAG, "fout is null.");
										}

									} catch (IllegalArgumentException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} catch (IllegalStateException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} catch (RuntimeException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} catch (IOException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} finally {
										if (fout != null) {
											try {
												fout.close();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}

									mImageLoader.DisplayImage(
											pictureFile.getPath(),
											screen_chat_picture_preView, false,
											screen_chat_picture_preView_edge);

								} else {
									screen_chat_picture_preView
											.setImageResource(R.drawable.default_image);
								}

							}

							screen_chat_picture_preView
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {

											if (fileExists(fileModel.savePath)) {
												Intent intent = new Intent();
												intent.setAction(Intent.ACTION_VIEW);
												File file = new File(
														fileModel.savePath);
												String type = "video/*";
												intent.setDataAndType(
														Uri.fromFile(file),
														type);
												mBaseScreen
														.startActivity(intent);
											} else {

												SystemVarTools.showToast(mBaseScreen
														.getString(R.string.video_file_has_been_deleted));

											}
										}
									});

						} else { // 普通文件

							view = mInflater.inflate(
									R.layout.screen_chat_item_file_receive,
									null);

							// 接收方头像
							remoteIcon = (ImageView) view
									.findViewById(R.id.screen_chat_item_file_receive_iconleft);
							SystemVarTools.showicon(remoteIcon, remoteContact,
									mBaseScreen);

							// 短信时间
							TextView date = (TextView) view
									.findViewById(R.id.screen_chat_item_file_receive_textView_date);
							date.setText(DateTimeUtils
									.getFriendlyDateString(new Date(event
											.getEndTime())));

							// 群短信发送方名称
							textSender = (TextView) view
									.findViewById(R.id.screen_chat_item_file_receive_textView_sender);
							if (!remoteName.equals("")) {
								textSender.setVisibility(View.VISIBLE);
								textSender.setText(remoteName);
								textSender.setSelected(true);
							} else {
								textSender.setVisibility(View.GONE);
							}

							if (fileExists(fileModel.savePath)) {

								// 显示文件名称
								TextView filename = (TextView) view
										.findViewById(R.id.screen_chat_item_file_receive_filename);
								filename.setText(mBaseScreen
										.getString(R.string.file_change_line)
										+ fileModel.name
										+ mBaseScreen
												.getString(R.string.receive_success_change_line));

								// 显示打开按钮
								Button openButton = (Button) view
										.findViewById(R.id.screen_chat_item_file_receive_file_btn);
								openButton.setText(mBaseScreen
										.getString(R.string.open_file));

								openButton
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												if (fileModel.name == null)
													return;

												Intent intent = new Intent();
												intent.setAction(Intent.ACTION_VIEW);
												File file = new File(
														fileModel.savePath);
												String type = "*/*";
												// text/plain image/jpeg
												// image/gif
												// image/bmp
												// image/png
												if (fileModel.name
														.endsWith(".jpg")
														|| fileModel.name
																.endsWith(".jpeg")
														|| fileModel.name
																.endsWith(".png")
														|| fileModel.name
																.endsWith(".bmp")
														|| fileModel.name
																.endsWith(".gif")) {
													type = "image/*";
												} else if (fileModel.name
														.endsWith(".txt")) {
													type = "text/*";
												} else if (fileModel.name
														.endsWith(".mp3")) {
													type = "audio/*";
												}

												intent.setDataAndType(
														Uri.fromFile(file),
														type);
												mBaseScreen
														.startActivity(intent);
											}
										});

							} else {
								// 显示文件名称
								TextView filename = (TextView) view
										.findViewById(R.id.screen_chat_item_file_receive_filename);
								filename.setText(mBaseScreen
										.getString(R.string.file_change_line)
										+ fileModel.name
										+ mBaseScreen
												.getString(R.string.file_not_exit_change_line));

								// 显示打开按钮
								Button openButton = (Button) view
										.findViewById(R.id.screen_chat_item_file_receive_file_btn);
								openButton.setText(mBaseScreen
										.getString(R.string.open_file));
								openButton.setVisibility(View.GONE);
							}

						}
					}

				} else { // 其他非法接受状态
					MyLog.d(TAG, "getView &&&&& 文件传输 状态 receive illegal");

					view = mInflater.inflate(
							R.layout.screen_chat_item_message_receive, null);

					// 接收方头像
					remoteIcon = (ImageView) view
							.findViewById(R.id.screen_chat_item_message_receive_iconleft);
					SystemVarTools.showicon(remoteIcon, remoteContact,
							mBaseScreen);
					// 短信时间
					TextView date = (TextView) view
							.findViewById(R.id.screen_chat_item_message_receive_textView_date);
					date.setText(DateTimeUtils.getFriendlyDateString(new Date(
							event.getEndTime())));

					// 短信内容
					TextView text = (TextView) view
							.findViewById(R.id.screen_chat_item_message_receive_textView);
					text.setText(mBaseScreen
							.getString(R.string.file_format_error) + content);

					textSender = (TextView) view
							.findViewById(R.id.screen_chat_item_message_receive_textView_sender);

					if (!remoteName.equals("")) {
						textSender.setVisibility(View.VISIBLE);
						textSender.setText(remoteName);
						textSender.setSelected(true);
					} else {
						textSender.setVisibility(View.GONE);
					}

				}

			} else { // 文字短信

				MyLog.d(TAG, "getView &&&&& 文字短信接收");

				view = mInflater.inflate(
						R.layout.screen_chat_item_message_receive, null);

				// 接收方头像
				remoteIcon = (ImageView) view
						.findViewById(R.id.screen_chat_item_message_receive_iconleft);
				SystemVarTools.showicon(remoteIcon, remoteContact, mBaseScreen);
				// 短信时间
				TextView date = (TextView) view
						.findViewById(R.id.screen_chat_item_message_receive_textView_date);
				date.setText(DateTimeUtils.getFriendlyDateString(new Date(event
						.getEndTime())));

				// 短信内容
				TextView text = (TextView) view
						.findViewById(R.id.screen_chat_item_message_receive_textView);
				text.setText(content);

				textSender = (TextView) view
						.findViewById(R.id.screen_chat_item_message_receive_textView_sender);

				if (!remoteName.equals("")) {
					textSender.setVisibility(View.VISIBLE);
					textSender.setText(remoteName);
					textSender.setSelected(true);
				} else {
					textSender.setVisibility(View.GONE);
				}

			}

		} else { // 发送

			TextView textView_status = null; // 发送短信状态
			// ImageView myicon = null; // 发送方（自己）头像
			TextView date = null; // 信息时间

			final ModelFileTransport fileModel = new ModelFileTransport();
			fileModel.parseFileContent(content);

			if (content != null && content.contains("type:file")) { // 文件短信
				if (fileModel.status.trim().equals("sending")) { // 发送中
					MyLog.d(TAG, "getView &&&&& 文件传输 状态 send sending");

					final String path = fileModel.localPath;

					if (path.endsWith(".jpg") || path.endsWith(".jpeg")
							|| path.endsWith(".png") || path.endsWith(".bmp")
							|| path.endsWith(".gif") || path.endsWith(".JPG")
							|| path.endsWith(".JPEG") || path.endsWith(".PNG")
							|| path.endsWith(".BMP") || path.endsWith(".GIF")) {

						view = mInflater.inflate(
								R.layout.screen_chat_item_image_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_image_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_image_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_image_send_textView_status);
						textView_status.setText(mBaseScreen
								.getString(R.string.sending));

						// 显示进度条
						RoundProgressBar progressBar = (RoundProgressBar) view
								.findViewById(R.id.screen_chat_item_image_send_progress);

						progressBar.setVisibility(View.VISIBLE);

						if (path != null) {

							NgnHistorySMSEvent tagevent = hisSMSEvent;
							FileHttpUpLoadClient uploadclient = FileHttpUpLoadClient.uploadMap
									.get(tagevent.getLocalMsgID());

							progressBar.setTag(tagevent.getLocalMsgID());

							if (uploadclient == null) {
								uploadclient = fileUpload(tagevent,
										progressBar, fileModel);
								uploadclient.setRoundProgressBar(progressBar);
								MyLog.d(TAG, "创建新的【文件上传】对象.");
							} else {
								uploadclient.setRoundProgressBar(progressBar);
								if (!uploadclient.isUploading) {
									uploadclient.httpSendFileInThread(path,
											fileModel.name, tagevent,
											ScreenChatAdapter.this);
									MyLog.d(TAG, "文件重新上传..");
								} else {
									MyLog.d(TAG, "文件正在上传..");
								}
							}
						}

					} else if (path.contains(".amr")) {

						view = mInflater.inflate(
								R.layout.screen_chat_item_audio_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_audio_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_audio_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_audio_send_textView_status);
						textView_status.setVisibility(View.GONE);

						// 显示进度条
						ProgressBar progressBar = (ProgressBar) view
								.findViewById(R.id.screen_chat_item_audio_send_progress);

						progressBar.setVisibility(View.VISIBLE);

						if (path != null) {

							NgnHistorySMSEvent tagevent = hisSMSEvent;
							FileHttpUpLoadClient uploadclient = FileHttpUpLoadClient.uploadMap
									.get(tagevent.getLocalMsgID());

							progressBar.setTag(tagevent.getLocalMsgID());

							if (uploadclient == null) {
								uploadclient = fileUpload(tagevent,
										progressBar, fileModel);
								uploadclient.setProgressBar(progressBar);
								MyLog.d(TAG, "创建新的【文件上传】对象.");
							} else {
								uploadclient.setProgressBar(progressBar);
								if (!uploadclient.isUploading) {
									uploadclient.httpSendFileInThread(path,
											fileModel.name, tagevent,
											ScreenChatAdapter.this);
									MyLog.d(TAG, "文件重新上传..");
								} else {
									MyLog.d(TAG, "文件正在上传..");
								}
							}
						}

					} else if (path.contains(".mp4")) {
						view = mInflater.inflate(
								R.layout.screen_chat_item_video_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_video_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_video_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_video_send_textView_status);
						textView_status.setText(mBaseScreen
								.getString(R.string.sending));

						// 显示进度条
						RoundProgressBar progressBar = (RoundProgressBar) view
								.findViewById(R.id.screen_chat_item_video_send_progress);

						progressBar.setVisibility(View.VISIBLE);

						if (path != null) {

							NgnHistorySMSEvent tagevent = hisSMSEvent;
							FileHttpUpLoadClient uploadclient = FileHttpUpLoadClient.uploadMap
									.get(tagevent.getLocalMsgID());

							progressBar.setTag(tagevent.getLocalMsgID());

							if (uploadclient == null) {
								uploadclient = fileUpload(tagevent,
										progressBar, fileModel);
								uploadclient.setRoundProgressBar(progressBar);
								MyLog.d(TAG, "创建新的【文件上传】对象.");
							} else {
								uploadclient.setRoundProgressBar(progressBar);
								if (!uploadclient.isUploading) {
									uploadclient.httpSendFileInThread(path,
											fileModel.name, tagevent,
											ScreenChatAdapter.this);
									MyLog.d(TAG, "文件重新上传..");
								} else {
									MyLog.d(TAG, "文件正在上传..");
								}
							}
						}

					} else {

						view = mInflater.inflate(
								R.layout.screen_chat_item_progress_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_progress_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_progress_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_progress_send_textView_status);

						// 显示进度条
						ProgressBar progressBar = (ProgressBar) view
								.findViewById(R.id.screen_chat_item_progress_send_file_progress);

						TextView filename = (TextView) view
								.findViewById(R.id.screen_chat_item_progress_send_filename);
						filename.setText(fileModel.name);

						Button cancelButton = (Button) view
								.findViewById(R.id.screen_chat_item_progress_send_file_btn);

						cancelButton.setText(mBaseScreen
								.getString(R.string.cancel));

						cancelButton.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								NgnHistorySMSEvent tagevent = hisSMSEvent;

								if (FileHttpUpLoadClient.uploadMap
										.containsKey(tagevent.getLocalMsgID())) {
									FileHttpUpLoadClient.uploadMap.get(
											tagevent.getLocalMsgID()).cancel();

								}
							}
						});

						if (path != null) {

							NgnHistorySMSEvent tagevent = hisSMSEvent;
							FileHttpUpLoadClient uploadclient = FileHttpUpLoadClient.uploadMap
									.get(tagevent.getLocalMsgID());

							progressBar.setTag(tagevent.getLocalMsgID());

							if (uploadclient == null) {
								uploadclient = fileUpload(tagevent,
										progressBar, fileModel);
								uploadclient.setProgressBar(progressBar);
								MyLog.d(TAG, "创建新的【文件上传】对象.");
							} else {
								uploadclient.setProgressBar(progressBar);
								if (!uploadclient.isUploading) {
									uploadclient.httpSendFileInThread(path,
											fileModel.name, tagevent,
											ScreenChatAdapter.this);
									MyLog.d(TAG, "文件重新上传..");
								} else {
									MyLog.d(TAG, "文件正在上传..");
								}
							}

						}

					}

				} else if (fileModel.status.trim().equals("send/failed") // 发送失败或者取消发送
						|| fileModel.status.trim().equals("send/cancel")) {
					MyLog.e(TAG, "getView &&&&& 文件传输 状态 send failed/cancel");

					// 和普通文件发送成功使用同一个布局
					view = mInflater.inflate(
							R.layout.screen_chat_item_file_send, null);

					// 发送方头像
					myIcon = (ImageView) view
							.findViewById(R.id.screen_chat_item_file_send_iconright);

					SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

					// 短信时间
					date = (TextView) view
							.findViewById(R.id.screen_chat_item_file_send_textView_date);
					date.setText(DateTimeUtils.getFriendlyDateString(new Date(
							event.getEndTime())));

					// 显示文件名称
					TextView filename = (TextView) view
							.findViewById(R.id.screen_chat_item_file_send_filename);
					filename.setText(mBaseScreen
							.getString(R.string.file_change_line)
							+ fileModel.name);

					// 显示重新发送按钮
					Button reSendButton = (Button) view
							.findViewById(R.id.screen_chat_item_file_send_file_btn);
					reSendButton
							.setText(mBaseScreen.getString(R.string.resend));

					reSendButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							NgnHistorySMSEvent tagevent = hisSMSEvent;

							if (fileModel.name == null)
								return;
							if (fileModel.url == null) {
								SystemVarTools.showToast(mBaseScreen
										.getString(R.string.file_send_url_error));
								return;
							}
							if (fileModel.savePath == null) {
								SystemVarTools.showToast(mBaseScreen
										.getString(R.string.file_send_path_not_exit));
								return;
							}

							MyLog.d(TAG,
									"reSendButton onCilck 文件发送  重新发送 tagevent.getLocalMsgID:"
											+ tagevent.getLocalMsgID()
											+ "|"
											+ "upload:"
											+ FileHttpUpLoadClient.uploadMap
													.get(tagevent
															.getLocalMsgID()));

							String contentT = hisSMSEvent.getContent();
							if (contentT.contains("send")) {

								if (contentT.contains("failed")) {
									hisSMSEvent.setContent(contentT.replace(
											"status:send/failed",
											"status:sending"));
								} else if (contentT.contains("cancel")) {
									hisSMSEvent.setContent(contentT.replace(
											"status:send/cancel",
											"status:sending"));
								}
							}
							Engine.getInstance().getHistoryService()
									.updateEvent(hisSMSEvent);

							Message msg = Message.obtain(
									mBaseScreen.mRefrashHandler,
									ScreenChat.REFRESH_CHAT);
							mBaseScreen.mRefrashHandler.sendMessage(msg);

						}
					});

					// 发送状态
					textView_status = (TextView) view
							.findViewById(R.id.screen_chat_item_file_send_textView_status);

					textView_status.setText(mBaseScreen
							.getString(R.string.not_send));

				} else if (fileModel.status.trim().equals("send/success")) { // 发送成功
					MyLog.e(TAG, "getView &&&&& 文件传输 状态 send success");

					final String path = fileModel.localPath;

					if (path.endsWith(".jpg") || path.endsWith(".jpeg")
							|| path.endsWith(".png") || path.endsWith(".bmp")
							|| path.endsWith(".gif") || path.endsWith(".JPG")
							|| path.endsWith(".JPEG") || path.endsWith(".PNG")
							|| path.endsWith(".BMP") || path.endsWith(".GIF")) { // 图片发送成功显示

						view = mInflater.inflate(
								R.layout.screen_chat_item_image_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_image_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_image_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 显示图片
						ImageView screen_chat_picture_preView = (ImageView) view
								.findViewById(R.id.screen_chat_item_image_send_picture_preview);

						ImageView sendImageViewEdge = (ImageView) view
								.findViewById(R.id.screen_chat_item_image_edge_send);

						if (fileExists(path)) {

							MyLog.d(TAG, "picture path :" + path);

							mImageLoader.DisplayImage(path,
									screen_chat_picture_preView, false,
									sendImageViewEdge);

							screen_chat_picture_preView
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											Intent intent = new Intent();
											intent.setAction(Intent.ACTION_VIEW);
											File file = new File(path);
											String type = "image/*";
											intent.setDataAndType(
													Uri.fromFile(file), type);
											mBaseScreen.startActivity(intent);
										}
									});

						} else {

							screen_chat_picture_preView
									.setImageResource(R.drawable.default_image);

							screen_chat_picture_preView
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {

											SystemVarTools.showToast(mBaseScreen
													.getString(R.string.pic_has_been_deleted));

										}
									});

						}

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_image_send_textView_status);

					} else if (path.contains(".amr")) { // 即时语音发送成功显示

						view = mInflater.inflate(
								R.layout.screen_chat_item_audio_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_audio_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_audio_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 显示图片
						final ImageView screen_chat_picture_preView = (ImageView) view
								.findViewById(R.id.screen_chat_item_audio_send_picture_preview);

						final File file = new File(path);

						if (fileExists(path)) {

							MyLog.d(TAG, "即时语音大小" + file.length());

							screen_chat_picture_preView
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											// TODO Auto-generated method stub
											playingposition = playingpos;
											istransfer = 1;

											if (animation2 != null) {
												animation2.stop();
												animation2.selectDrawable(2);
												animation2 = null;

											}

											try {

												if (mplayer != null)
													mplayer.release();
												mplayer = null;
												mplayer = new MediaPlayer();

												mplayer.setDataSource(file
														.getPath());
												mplayer.prepare();
												mplayer.start();

											} catch (IOException e) {
												e.printStackTrace();
											}

											if (mplayer == null) {
												return;
											}

											if (animation != null) {
												animation.stop();

												animation.selectDrawable(2);
												animation = null;

											}
											animation = new AnimationDrawable();
											screen_chat_picture_preView
													.setImageResource(R.drawable.transfer_audio_changeicon);

											animation = (AnimationDrawable) screen_chat_picture_preView
													.getDrawable();

											mplayer.setOnCompletionListener(new OnCompletionListener() {

												@Override
												public void onCompletion(
														MediaPlayer mp) {
													if (animation != null) {
														animation.stop();
														animation
																.selectDrawable(2);
													}
													animation = null;
													MyLog.d(TAG,
															"playingcomplete");

													if (animation2 != null) {
														animation2.stop();
														animation2
																.selectDrawable(2);
														animation2 = null;

													}

													mplayer.stop();
													mplayer.release();
													mplayer = null;
													playingposition = -1;

												}
											});

											if (mplayer.isPlaying()) {

												animation.start();

											} else {

												mplayer.stop();
												mplayer.release();
												mplayer = null;
												animation.stop();
												animation.selectDrawable(2);
												animation = null;

											}
										}
									});

							if ((mplayer != null) && mplayer.isPlaying()
									&& (position == playingposition)) {

								screen_chat_picture_preView
										.setImageResource(R.drawable.transfer_audio_changeicon);

								animation2 = (AnimationDrawable) screen_chat_picture_preView
										.getDrawable();

								animation2.start();
								MyLog.d(TAG, "animation不为空");

							} else {
								screen_chat_picture_preView
										.setImageResource(R.drawable.transfer_audio);

							}

						} else {
							screen_chat_picture_preView
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											SystemVarTools.showToast(mBaseScreen
													.getString(R.string.audio_file_has_been_deleted));
										}
									});
						}

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_audio_send_textView_status);

					} else if (path.contains(".mp4")) { // 即时视频发送成功显示
						view = mInflater.inflate(
								R.layout.screen_chat_item_video_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_video_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_video_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						// 显示视频开头图片
						ImageView screen_chat_picture_preView = (ImageView) view
								.findViewById(R.id.screen_chat_item_video_send_picture_preview);
						ImageView screen_chat_picture_preView_edge = (ImageView) view
								.findViewById(R.id.screen_chat_item_video_edge_send_picture_preview);

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_video_send_textView_status);

						ImageView playVideo = (ImageView) view
								.findViewById(R.id.screen_chat_item_video_send_playvideo);
						playVideo.setVisibility(View.VISIBLE);

						String picturepath = path.substring(0,
								path.length() - 4) + ".jpg";

						if (fileExists(picturepath)) { // 截图已存在

							File pictureFile = new File(picturepath);

							if (pictureFile.length() == 0) {
								screen_chat_picture_preView
										.setImageResource(R.drawable.default_image);

							} else {
								mImageLoader.DisplayImage(picturepath,
										screen_chat_picture_preView, false,
										screen_chat_picture_preView_edge);

							}

						} else { // 截图不存在

							if (fileExists(path)) {

								File pictureFile = new File(picturepath);

								if (!fileExists(picturepath)) {

									FileOutputStream fout = null;
									try {
										fout = new FileOutputStream(pictureFile);
									} catch (FileNotFoundException e) {
									}

									MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
									try {
										mediaMetadataRetriever
												.setDataSource(path);

										Bitmap bitmap = mediaMetadataRetriever
												.getFrameAtTime(200);

										bitmap.compress(
												Bitmap.CompressFormat.JPEG,
												100, fout);

										if (fout != null) {
											fout.flush();

										}

									} catch (IllegalArgumentException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} catch (IllegalStateException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} catch (RuntimeException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} catch (IOException e) {
										e.printStackTrace();
										screen_chat_picture_preView
												.setImageResource(R.drawable.default_image);
									} finally {
										if (fout != null) {
											try {
												fout.close();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}
								}

								mImageLoader.DisplayImage(
										pictureFile.getPath(),
										screen_chat_picture_preView, false,
										screen_chat_picture_preView_edge);

							} else {

								screen_chat_picture_preView
										.setImageResource(R.drawable.default_image);

							}

						}

						screen_chat_picture_preView
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										if (fileExists(path)) {
											Intent intent = new Intent();
											intent.setAction(Intent.ACTION_VIEW);
											File file = new File(path);
											String type = "video/*";
											intent.setDataAndType(
													Uri.fromFile(file), type);
											mBaseScreen.startActivity(intent);
										} else {
											SystemVarTools.showToast(mBaseScreen
													.getString(R.string.video_file_has_been_deleted));

										}
									}
								});

					} else { // 普通文件发送成功显示
						view = mInflater.inflate(
								R.layout.screen_chat_item_file_send, null);

						// 发送方头像
						myIcon = (ImageView) view
								.findViewById(R.id.screen_chat_item_file_send_iconright);

						SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

						// 短信时间
						date = (TextView) view
								.findViewById(R.id.screen_chat_item_file_send_textView_date);
						date.setText(DateTimeUtils
								.getFriendlyDateString(new Date(event
										.getEndTime())));

						if (fileExists(path)) {

							// 显示文件名称
							TextView filename = (TextView) view
									.findViewById(R.id.screen_chat_item_file_send_filename);
							filename.setText(mBaseScreen
									.getString(R.string.file_change_line)
									+ fileModel.name
									+ mBaseScreen
											.getString(R.string.send_success_change_line));

							// 显示打开按钮
							Button openButton = (Button) view
									.findViewById(R.id.screen_chat_item_file_send_file_btn);
							openButton.setText(mBaseScreen
									.getString(R.string.open_file));

							openButton
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											if (fileModel.name == null)
												return;

											Intent intent = new Intent();
											intent.setAction(Intent.ACTION_VIEW);
											File file = new File(path);
											String type = "*/*";
											// text/plain image/jpeg image/gif
											// image/bmp
											// image/png
											if (fileModel.name.endsWith(".jpg")
													|| fileModel.name
															.endsWith(".jpeg")
													|| fileModel.name
															.endsWith(".png")
													|| fileModel.name
															.endsWith(".bmp")
													|| fileModel.name
															.endsWith(".gif")) {
												type = "image/*";
											} else if (fileModel.name
													.endsWith(".txt")) {
												type = "text/*";
											} else if (fileModel.name
													.endsWith(".mp3")) {
												type = "audio/*";
											}

											intent.setDataAndType(
													Uri.fromFile(file), type);
											mBaseScreen.startActivity(intent);
										}
									});

						} else {
							// 显示文件名称
							MyLog.d(TAG, "文件不存在");
							TextView filename = (TextView) view
									.findViewById(R.id.screen_chat_item_file_send_filename);
							filename.setText(mBaseScreen
									.getString(R.string.file_change_line)
									+ fileModel.name
									+ mBaseScreen
											.getString(R.string.file_not_exit_change_line));

							// 显示打开按钮
							Button openButton = (Button) view
									.findViewById(R.id.screen_chat_item_file_send_file_btn);
							openButton.setText(mBaseScreen
									.getString(R.string.open_file));
							openButton.setVisibility(View.GONE);
						}

						// 发送状态
						textView_status = (TextView) view
								.findViewById(R.id.screen_chat_item_file_send_textView_status);

					}

				} else { // 其他非法状态
					MyLog.d(TAG, "getView &&&&& 文件传输 状态 send illegal");

				}
			} else { // 文字短信

				MyLog.d(TAG, "getView &&&&& 文字短信发送");

				view = mInflater.inflate(
						R.layout.screen_chat_item_message_send, null);

				// 发送方头像
				myIcon = (ImageView) view
						.findViewById(R.id.screen_chat_item_message_send_iconright);
				SystemVarTools.showicon(myIcon, meContact, mBaseScreen);

				// 短信时间
				date = (TextView) view
						.findViewById(R.id.screen_chat_item_message_send_textView_date);
				date.setText(DateTimeUtils.getFriendlyDateString(new Date(event
						.getEndTime())));

				// 短信内容
				TextView text = (TextView) view
						.findViewById(R.id.screen_chat_item_message_send_textView);
				text.setText(content);

				textView_status = (TextView) view
						.findViewById(R.id.screen_chat_item_message_send_textView_status);

			}

			// 更改发送的递送报告
			if (hisSMSEvent.getLocalMsgID() != null) {
				if (ServiceLoginAccount.mMessageIDHashMap != null
						&& ServiceLoginAccount.mMessageIDHashMap
								.containsKey(hisSMSEvent.getLocalMsgID().trim())
						&& GlobalVar.bADHocMode == false) {

					if (fileModel != null && fileModel.status != null) {
						if (fileModel.status.equals("send/success")
								&& textView_status != null) {
							textView_status.setText(mBaseScreen
									.getString(R.string.send_arrived));
						}
					} else {
						if (textView_status != null) {
							textView_status.setText(mBaseScreen
									.getString(R.string.send_arrived));
						}
					}

				}
			}

		}

		// 点击聊天室头像图标跳转到用户信息界面
		final String remotenumber = remoteNumber;
		if (remoteIcon != null) {
			remoteIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					IServiceScreen mScreenService;
					mScreenService = ((Engine) Engine.getInstance())
							.getScreenService();
					if (remotenumber != null) {
						mScreenService.showPersonOrOrgInfo(
								ScreenPersonInfo.class, remotenumber);
					} else {
						MyLog.d(TAG, "remoteIcon onClick remotenumber is null");
					}
				}
			});
		} else {
			MyLog.e(TAG, "remoteIcon is null");
		}

		INgnConfigurationService mConfigurationService = ((Engine) Engine
				.getInstance()).getConfigurationService();
		final String accountString = mConfigurationService.getString(
				NgnConfigurationEntry.IDENTITY_IMPI,
				NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

		if (myIcon != null) {
			myIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					IServiceScreen mScreenService;
					mScreenService = ((Engine) Engine.getInstance())
							.getScreenService();
					if (accountString != null) {
						mScreenService.showPersonOrOrgInfo(
								ScreenPersonInfo.class, accountString);
					} else {
						MyLog.d(TAG, "myIcon onClick accountString is null");
					}
				}
			});
		} else {
			MyLog.d(TAG, "myIcon is null");
		}

		mBaseScreen.mHistorytService.updateEvent(hisSMSEvent);

		// add by gle 20140605
		if (ServiceLoginAccount.mMessageIDHashMap != null) {
			boolean isRemove = false;
			Iterator keys = ServiceLoginAccount.mMessageIDHashMap.keySet()
					.iterator();
			while (keys.hasNext()) {
				long currentTime = System.currentTimeMillis();
				String key = (String) keys.next();
				String lastTime = ServiceLoginAccount.mMessageIDHashMap
						.get(key).toString();
				if (currentTime / 1000 - Long.valueOf(lastTime) > 24 * 3600) {
					keys.remove();
					isRemove = true;
					MyLog.d(TAG, "currentTime Remove OK");
				}
			}
			if (isRemove) {
				try {
					Tools_data
							.writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return view;
	}

	public boolean fileExists(String path) {
		try {
			File f = new File(path);

			if (!f.exists()) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}

	}

	// 自定义圆型的进度条上传文件
	private FileHttpUpLoadClient fileUpload(NgnHistorySMSEvent event,
			RoundProgressBar bar, final ModelFileTransport fileModel) {

		final FileHttpUpLoadClient uploadclient = new FileHttpUpLoadClient();

		FileHttpUpLoadClient.uploadMap.put(event.getLocalMsgID(), uploadclient);
		uploadclient.setRoundProgressBar(bar);
		// uploadclient.setmRefreshHandler(mBaseScreen.mHandler);

		Handler progressHandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {
					case Main.FILEUPLOADPROGRESS:

						RoundProgressBar progressBar = uploadclient
								.getRoundProgressBar();

						int progress = msg.getData().getInt(
								"fileTransferProgress");

						if ((progress % 20) == 0) {
							MyLog.d(TAG, "upload progress=" + progress);
						}
						String msgid = msg.getData().getString("msgid");
						if (!progressBar.getTag().equals(msgid))
							break;

						// if (position == progressPos) {

						if (progress < 100) {
							progressBar.setProgress(progress);
							// progressBar
							// .setVisibility(View.VISIBLE);
						} else {
							progressBar.setVisibility(View.GONE);
						}
						// } else {
						// break;
						// }
						break;
					case Main.FILEUPLOAD_INPROGRESS:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_isuploading));

						break;

					case Main.FILEUPLOAD_EXTRA:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_upload_queue_full));

						break;
					case Main.FILE_NO_EXIST:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_not_exit));

						break;
					default:
						break;
					}
				} catch (Exception e) {
					MyLog.d(TAG, "" + e.getMessage());
				}
			}
		};
		uploadclient.setmFileUploadProgressHandler(progressHandler);
		uploadclient.setmRefreshHandler(mBaseScreen.mRefrashHandler);
		uploadclient.httpSendFileInThread(fileModel.localPath, fileModel.name,
				event, ScreenChatAdapter.this);
		return uploadclient;
	}

	// 系统自带的进度条
	private FileHttpUpLoadClient fileUpload(NgnHistorySMSEvent event,
			ProgressBar bar, final ModelFileTransport fileModel) {

		final FileHttpUpLoadClient uploadclient = new FileHttpUpLoadClient();

		FileHttpUpLoadClient.uploadMap.put(event.getLocalMsgID(), uploadclient);
		uploadclient.setProgressBar(bar);
		// uploadclient.setmRefreshHandler(mBaseScreen.mHandler);

		Handler progressHandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {
					case Main.FILEUPLOADPROGRESS:

						ProgressBar progressBar = uploadclient.getProgressBar();

						int progress = msg.getData().getInt(
								"fileTransferProgress");

						if ((progress % 20) == 0) {
							MyLog.d(TAG, "upload progress=" + progress);
						}
						String msgid = msg.getData().getString("msgid");
						if (!progressBar.getTag().equals(msgid))
							break;

						// if (position == progressPos) {

						if (progress < 100) {
							progressBar.setProgress(progress);
							// progressBar
							// .setVisibility(View.VISIBLE);
						} else {
							progressBar.setVisibility(View.GONE);
						}
						// } else {
						// break;
						// }
						break;
					case Main.FILEUPLOAD_INPROGRESS:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_isuploading));

						break;

					case Main.FILEUPLOAD_EXTRA:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_upload_queue_full));

						break;
					case Main.FILE_NO_EXIST:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_not_exit));

						break;
					default:
						break;
					}
				} catch (Exception e) {
					MyLog.d(TAG, "" + e.getMessage());
				}
			}
		};
		uploadclient.setmFileUploadProgressHandler(progressHandler);
		uploadclient.setmRefreshHandler(mBaseScreen.mRefrashHandler);
		MyLog.d("ywh", "文件的路径---"+fileModel.localPath+"------"+fileModel.name);
		uploadclient.httpSendFileInThread(fileModel.localPath, fileModel.name,
				event, ScreenChatAdapter.this);
		return uploadclient;
	}

	// 自定义圆形进度条
	private FileHttpDownLoadClient fileDownload(NgnHistorySMSEvent event,
			RoundProgressBar bar, final int position) {

		MyLog.d(TAG, "下载的文件名： "+event.mContent);
		final RoundProgressBar progressBar = bar;
		ModelFileTransport fileModel = new ModelFileTransport();
		fileModel.parseFileContent(event.getContent());

		FileHttpDownLoadClient download = FileHttpDownLoadClient.downloadMap
				.get(event.getLocalMsgID());
		if (download == null) {
			MyLog.d(TAG, "fileDownload %%%%% 文件下载 Download:新建");
			download = new FileHttpDownLoadClient();
		}

		// bar.setTag(event.getLocalMsgID());

		download.position = position;

		final Handler progressHandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {
					case Main.FILEDOWNLOADPROGRESS:
						String msgType = msg.getData().getString("msgType");
						int progressPos = msg.getData().getInt("position");

						String msgid = msg.getData().getString("msgid");

						// MyLog.e(TAG, "&&&& 文件下载  Handler消息 Type:" + msgType);
						if ("progress".equals(msgType)) {
							int progress = Integer.parseInt(msg.getData()
									.getString("msgData"));
							if ((progress % 20) == 0) {
								MyLog.d(TAG, "download progress : " + progress);
							}

							if (progressPos == position) {

								if (progress < 100) {
									progressBar.setProgress(progress);
									// progressBar.setVisibility(View.VISIBLE);
								} else {
									progressBar.setVisibility(View.GONE);

								}
							} else {
								break;
							}
						}
						if ("exception".equals(msgType)) {
							String reason = msg.getData().getString("msgData");
							SystemVarTools
									.showToast(mBaseScreen
											.getString(R.string.file_download_with_colon)
											+ reason);
						}
						break;

					case Main.FILEDOWN_EXTRA:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_download_queue_full));

						break;
					case Main.FILE_NO_EXIST:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_not_exit));

						break;

					default:
						break;
					}
				} catch (Exception e) {
					MyLog.d(TAG, "" + e.getMessage());
				}
			}
		};

		download.setmProgressHandler(progressHandler);
		download.setmRefreshHandler(mBaseScreen.mRefrashHandler);
		MyLog.d(TAG, "fileDownload %%%%% 文件下载 isDownload:"
				+ download.isDowbloading);
		if (!download.isDowbloading) {
			download.httpDownloadFileInThread(fileModel, event,
					ScreenChat.mAdapter);
		}

		FileHttpDownLoadClient.downloadMap.put(event.getLocalMsgID(), download);
		return download;
	}

	// 系统自带进度条
	private FileHttpDownLoadClient fileDownload(NgnHistorySMSEvent event,
			ProgressBar bar, final int position) {

		final ProgressBar progressBar = bar;
		ModelFileTransport fileModel = new ModelFileTransport();
		fileModel.parseFileContent(event.getContent());

		FileHttpDownLoadClient download = FileHttpDownLoadClient.downloadMap
				.get(event.getLocalMsgID());
		if (download == null) {
			MyLog.d(TAG, "fileDownload %%%%% 文件下载 Download:新建");
			download = new FileHttpDownLoadClient();
		}

		// bar.setTag(event.getLocalMsgID());

		download.position = position;

		final Handler progressHandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {
					case Main.FILEDOWNLOADPROGRESS:
						String msgType = msg.getData().getString("msgType");
						int progressPos = msg.getData().getInt("position");

						String msgid = msg.getData().getString("msgid");

						// MyLog.e(TAG, "&&&& 文件下载  Handler消息 Type:" + msgType);
						if ("progress".equals(msgType)) {
							int progress = Integer.parseInt(msg.getData()
									.getString("msgData"));
							if ((progress % 20) == 0) {
								MyLog.d(TAG, "download progress : " + progress);
							}

							if (progressPos == position) {

								if (progress < 100) {
									progressBar.setProgress(progress);
									// progressBar.setVisibility(View.VISIBLE);
								} else {
									progressBar.setVisibility(View.GONE);

								}
							} else {
								break;
							}
						}
						if ("exception".equals(msgType)) {
							String reason = msg.getData().getString("msgData");
							SystemVarTools
									.showToast(mBaseScreen
											.getString(R.string.file_download_with_colon)
											+ reason);
						}
						break;

					case Main.FILEDOWN_EXTRA:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_download_queue_full));

						break;
					case Main.FILE_NO_EXIST:
						SystemVarTools.showToast(mBaseScreen
								.getString(R.string.file_not_exit));

						break;

					default:
						break;
					}
				} catch (Exception e) {
					MyLog.d(TAG, "" + e.getMessage());
				}
			}
		};

		download.setmProgressHandler(progressHandler);
		download.setmRefreshHandler(mBaseScreen.mRefrashHandler);
		MyLog.d(TAG, "fileDownload %%%%% 文件下载 isDownload:"
				+ download.isDowbloading);
		if (!download.isDowbloading) {
			download.httpDownloadFileInThread(fileModel, event,
					ScreenChat.mAdapter);
		}

		FileHttpDownLoadClient.downloadMap.put(event.getLocalMsgID(), download);
		return download;
	}

	@Override
	public void update(Observable observable, Object data) {
		refresh();
	}

	/**
	 * 清除upload对象
	 */
	public void clearUpload(String key) {
		if (FileHttpUpLoadClient.uploadMap != null && key != null) {
			if (FileHttpUpLoadClient.uploadMap.containsKey(key)) {
				FileHttpUpLoadClient.uploadMap.remove(key);
			}
		}
	}

	/**
	 * 清除download对象
	 */
	public void clearDownload(String key) {
		if (FileHttpDownLoadClient.downloadMap != null && key != null) {
			if (FileHttpDownLoadClient.downloadMap.containsKey(key)) {
				FileHttpDownLoadClient.downloadMap.remove(key);
			}
		}
	}

	/**
	 * 文件下载前的查询文件保存路径中是否有同名文件，如果有则文件名加上(i),保存到历史记录中并且刷新
	 * 
	 * */
	public void reSetEvent(ModelFileTransport fileModel, String content,
			NgnHistorySMSEvent hissEvent) {

		if (fileModel != null && fileModel.savePath != null
				&& !fileModel.savePath.equals("")) {

			File temp = new File(fileModel.savePath);

			// 不仅要看SD卡中有没有，还要看下载列表中有没有
			if ((!temp.exists())
					&& (!FileHttpDownLoadClient.downloadList
							.contains(fileModel.savePath))) {

				Engine.getInstance().getHistoryService().updateEvent(hissEvent);

				Message msg = Message.obtain(mBaseScreen.mRefrashHandler,
						ScreenChat.REFRESH_CHAT);
				mBaseScreen.mRefrashHandler.sendMessage(msg);

				return;
			} else {
				MyLog.d(TAG, TAG + "文件存在：" + fileModel.savePath);
				for (int i = 1; i < 100; i++) {
					String tempPath = fileModel.savePath;
					int index = tempPath.lastIndexOf(".");
					String newPath = "";

					if (index != -1) {
						String lastPath = tempPath.substring(index);
						String forePath = tempPath.substring(0, index);
						newPath = forePath + "(" + i + ")" + lastPath;
					} else {
						newPath = tempPath + "(" + i + ")";
					}

					if (newPath != null && !newPath.equals("")) {
						MyLog.d(TAG, TAG + "新文件路径：" + newPath);
						temp = new File(newPath);
						if ((!temp.exists())
								&& (!FileHttpDownLoadClient.downloadList
										.contains(newPath))) {

							if (fileModel.name != null
									&& !fileModel.name.equals("")) {
								String tempName = fileModel.name;
								int index2 = tempName.lastIndexOf(".");
								String newName = "";

								if (index != -1) {
									String lastName = tempName
											.substring(index2);
									String foreName = tempName.substring(0,
											index2);
									newName = foreName + "(" + i + ")"
											+ lastName;
								} else {
									newName = tempName + "(" + i + ")";
								}

								int indexContent = content.indexOf("name:");
								String tempContent = content.substring(0,
										indexContent + 5);
								String newContent = tempContent + newName;
								MyLog.d(TAG, TAG + "新event Content："
										+ newContent);
								hissEvent.setContent(newContent);
								Engine.getInstance().getHistoryService()
										.updateEvent(hissEvent);
								Message msg = Message.obtain(
										mBaseScreen.mRefrashHandler,
										ScreenChat.REFRESH_CHAT);
								mBaseScreen.mRefrashHandler.sendMessage(msg);

								return;
							}
						}
					}

				}
			}
		} else {
			MyLog.d(TAG, TAG + "fileModel something  is null");
		}

	}
}
