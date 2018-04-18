package com.sunkaisens.skdroid.Screens;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryPushEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Utils.AsyncImageLoader;
import com.sunkaisens.skdroid.Utils.DateTimeUtils;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.ModelPush;

public class ScreenPushInfo extends BaseScreen {
	private static String TAG = ScreenPushInfo.class.getCanonicalName();

	// private final INgnHistoryService mHistorytService;
	public final INgnHistoryService mHistorytService;
	private final INgnSipService mSipService;

	private InputMethodManager mInputMethodManager;

	private static String sRemoteParty;

	private NgnMsrpSession mSession;
	private NgnMediaType mMediaType;
	private ScreenPushinfoAdapter mAdapter;
	// public ScreenPushinfoAdapter mAdapter;
	// public static ScreenPushinfoAdapter mAdapter;
	private EditText mEtCompose;
	private ListView mLvHistoy;
	private TextView mTvName;
	private Button mBtSend;
	private ImageView mBtadd_filetransfer_imagebutton;
	private Button mBtFiletransfer;
	private View mViewFiletransfer_view;
	private LinearLayout mLinearLayoutFiletransfer_ll;
	private ImageButton mBtInfo;
	private ImageView mBtBack;
	// 标识信息长度的textView
	private TextView mTvContentCount;
	// private final INgnConfigurationService mConfigurationService;
	private static INgnConfigurationService mConfigurationService;

	private RelativeLayout.LayoutParams lpParams;
	// 创建浮动窗口设置布局参数的对象
	private WindowManager mWindowManager;

	// private ModelContact userinfo = null;
	public ModelContact userinfo = null;

	static AsyncImageLoader imageLoader = new AsyncImageLoader();

	public ScreenPushInfo() {
		super(SCREEN_TYPE.CHAT_T, TAG);

		mMediaType = NgnMediaType.None;
		mHistorytService = getEngine().getHistoryService();
		mSipService = getEngine().getSipService();
		mConfigurationService = getEngine().getConfigurationService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_pushinfo);

		mTvName = (TextView) findViewById(R.id.screen_pushinfo_textview_name);

		mLvHistoy = (ListView) findViewById(R.id.screen_pushinfo_listView);

		mBtBack = (ImageView) findViewById(id.screen_pushinfo_linearLayout_top_back);

		mAdapter = new ScreenPushinfoAdapter(this);
		mLvHistoy.setAdapter(mAdapter);
		mLvHistoy.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mLvHistoy.setStackFromBottom(false);

		mLvHistoy.setSelection(mLvHistoy.getCount()-1);
		
		userinfo = SystemVarTools.createContactFromRemoteParty(sRemoteParty);
		mTvName.setText(userinfo.name);

		mBtBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMediaType != NgnMediaType.None) {
			initialize(mMediaType);
		}
		mAdapter.refresh();

		((Engine) Engine.getInstance()).cancelSMSNotif(); // 消掉短消息通知

		SystemVarTools.ScreenChat_Is_Top = true; // 屏幕黑屏唤醒后设置窗口状态
	}

	@Override
	protected void onPause() {
		if (mInputMethodManager != null) {
			mInputMethodManager.hideSoftInputFromWindow(
					mEtCompose.getWindowToken(), 0);
		}
		super.onPause();

		changeUserMsgReadStatus();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mSession != null) {
			mSession.decRef();
			mSession = null;
		}

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
		// if (mMediaType == NgnMediaType.Push) {
		// final String validUri = NgnUriUtils.makeValidSipUri(sRemoteParty);
		// if (!NgnStringUtils.isNullOrEmpty(validUri)) {
		// mSession = NgnMsrpSession.getSession(new
		// NgnPredicate<NgnMsrpSession>() {
		// @Override
		// public boolean apply(NgnMsrpSession session) {
		// if (session != null && session.getMediaType() == NgnMediaType.Push) {
		// return NgnStringUtils.equals(session.getRemotePartyUri(), validUri,
		// false);
		// }
		// return false;
		// }
		// });
		// if (mSession == null) {
		// if ((mSession =
		// NgnMsrpSession.createOutgoingSession(mSipService.getSipStack(),
		// NgnMediaType.Push, validUri)) == null) {
		// Log.e(TAG, "Failed to create MSRP session");
		// finish();
		// return;
		// }
		// }
		// if (bIsNewScreen && mSession != null) {
		// mSession.incRef();
		// }
		// } else {
		// Log.e(TAG, "makeValidSipUri(" + sRemoteParty + ") has failed");
		// finish();
		// return;
		// }
		// }
	}

	public static void startPushInfo(String remoteParty, boolean bIsPagerMode) {
		final Engine engine = (Engine) NgnEngine.getInstance();
		if (!NgnStringUtils.isNullOrEmpty(remoteParty)
				&& remoteParty.startsWith("sip:")) {
			remoteParty = NgnUriUtils.getUserName(remoteParty);
		}

		if (NgnStringUtils.isNullOrEmpty((sRemoteParty = remoteParty))) {
			Log.e(TAG, "Null Uri");
			return;
		}

		if (engine.getScreenService().show(ScreenPushInfo.class)) {
			final IBaseScreen screen = engine.getScreenService().getScreen(TAG);
			if (screen instanceof ScreenPushInfo) {
				((ScreenPushInfo) screen)
						.initialize(bIsPagerMode ? NgnMediaType.Push
								: NgnMediaType.Push);
			}
		}
	}

	static class HistoryEventPushFilter implements
			NgnPredicate<NgnHistoryEvent> {
		@Override
		public boolean apply(NgnHistoryEvent event) {
			if (event != null && (event.getMediaType() == NgnMediaType.Push)) {
				return NgnStringUtils.equals(sRemoteParty,
						event.getRemoteParty(), false);
			}
			return false;
		}
	}

	static class DateComparator implements Comparator<NgnHistoryEvent> {
		@Override
		public int compare(NgnHistoryEvent e1, NgnHistoryEvent e2) {
			return (int) (e1.getStartTime() - e2.getStartTime());
		}
	}

	/**
	 * ScreenPushinfoAdapter
	 */
	class ScreenPushinfoAdapter extends BaseAdapter implements Observer {
		private List<NgnHistoryEvent> mEvents;
		private final LayoutInflater mInflater;
		private final Handler mHandler;
		private final ScreenPushInfo mBaseScreen;

		ScreenPushinfoAdapter(ScreenPushInfo baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			mEvents = mBaseScreen.mHistorytService.getObservableEvents()
					.filter(new HistoryEventPushFilter());
			Log.e(TAG, "ScreenPushinfo---mEvents====" + mEvents);
			Collections.sort(mEvents, new DateComparator());
			mBaseScreen.mHistorytService.getObservableEvents()
					.addObserver(this);
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			mBaseScreen.mHistorytService.getObservableEvents().deleteObserver(
					this);
		}

		public void refresh() {
			mEvents = mBaseScreen.mHistorytService.getObservableEvents()
					.filter(new HistoryEventPushFilter());
			Collections.sort(mEvents, new DateComparator());
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
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final NgnHistoryEvent event = (NgnHistoryEvent) getItem(position);
			if (event == null) {
				return null;
			}
			if (view == null) {
				switch (event.getMediaType()) {
				case Audio:

				case AudioVideo:

				case FileTransfer:

				case SMS:

				case Push:
					view = mInflater.inflate(R.layout.screen_pushinfo_item,
							null);
					break;

				default:
					Log.e(TAG, "Invalid media type");
					return null;

				}
			}

			view.setTag(position);
			//
			String remoteParty = event.getRemoteParty();

			// 取出保存的用户消息信息，根据flag 判断是否有未读消息，并改变item的背景颜色，完成未读信息标识功能
			boolean flag = mConfigurationService.getBoolean(remoteParty, false);

			if (flag) {
				view.setBackgroundColor(Color.GRAY);
			} else {
				view.setBackgroundColor(mBaseScreen.getResources().getColor(
						R.color.color_mainbg));
			}

			ModelContact mc = SystemVarTools
					.createContactFromRemoteParty(remoteParty);

			remoteParty = mc.name;

			final NgnHistoryPushEvent pushEvent = (NgnHistoryPushEvent) event;

			// 标题
			final TextView tvTitle = (TextView) view
					.findViewById(R.id.pushinfo_item_title);
			// 接收消息的时间
			final TextView tvTime = (TextView) view
					.findViewById(R.id.pushinfo_item_time);
			// //消息的内容/摘要
			// final TextView tvContent = (TextView)
			// view.findViewById(R.id.pushinfo_item_content);

			tvTitle.setText(pushEvent.getTitle());
			tvTitle.setSelected(true);
			tvTime.setText(DateTimeUtils.getFriendlyDateString(new Date(event
					.getStartTime())));

			// 图片处理
			final ImageView imageicon = (ImageView) view
					.findViewById(R.id.pushinfo_item_image);
			// 消息的内容/摘要
			final TextView tvContent = (TextView) view
					.findViewById(R.id.pushinfo_item_content);
			// android获得手机屏幕的宽度和高度
			mWindowManager = mBaseScreen.getWindowManager();
			int width = mWindowManager.getDefaultDisplay().getWidth();
			// int height = mWindowManager.getDefaultDisplay().getHeight();
			// 设置图片长宽数据
			lpParams = new RelativeLayout.LayoutParams(width, width / 2);
			imageicon.setLayoutParams(lpParams);
			if (pushEvent.getMsgType() != null
					&& pushEvent.getMsgType().equals("text")) { // text
				imageicon.setVisibility(View.GONE);
				tvContent.setVisibility(View.VISIBLE);
				tvTitle.setVisibility(View.GONE);
			} else { // text-picture
				// imageicon.setImageResource(R.drawable.default_push_image_large);
				imageicon.setVisibility(View.VISIBLE);
				tvContent.setVisibility(View.GONE);
				tvTitle.setVisibility(View.VISIBLE);
				if (pushEvent.getImageUrl() != null
						&& !pushEvent.getImageUrl().equals("")) {
					AsyncImageLoader.ImageCallback callback = new AsyncImageLoader.ImageCallback() {
						@Override
						public void imageLoaded(Integer t, Bitmap bitmap,
								ImageView v) {
							if (bitmap != null) {
								v.setImageBitmap(bitmap);
							}
						}
					};
					// pushEvent.getImageUrl() =
					// http://192.168.1.222:8080/ipp/loadData.action?filename=image/10658108111415343319599.jpg
					// imageLoader.loadDrawable(R.drawable.default_push_image_large,
					// pushEvent.getImageUrl(), imageicon, callback);
					Bitmap bm = imageLoader.loadDrawable(
							R.drawable.default_push_image_large,
							pushEvent.getImageUrl(), imageicon, callback);
					if (bm != null) {
						imageicon.setScaleType(ScaleType.FIT_XY);
					} else { // default_push_image_large.png
						imageicon.setScaleType(ScaleType.FIT_XY);
					}
				} else { // default_push_image_large.png
					imageicon.setScaleType(ScaleType.FIT_XY);
				}
			}
			imageicon.setTag(position);
			imageicon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					final NgnHistoryEvent event = (NgnHistoryEvent) getItem(pos);
					if (event != null) {
						NgnHistoryPushEvent pushEvent = (NgnHistoryPushEvent) event;
						if (pushEvent.getMsgType() != null
								&& pushEvent.getMsgType().equals("image")) { // text-picture
							
							if (pushEvent.getTitle() != null
									&& !pushEvent.getTitle().equals("")) {
								ScreenPushInfoLink.mTitle = pushEvent.getTitle();
							} else {
								ScreenPushInfoLink.mTitle = getApplicationContext().getString(R.string.notitle);
							}
							
							
							ScreenPushInfoLink.startPushInfoLink(pushEvent
									.getLinkUri());
						}
					}
				}
			});

			String pushContent = pushEvent.getContent();
			if (pushContent == null) {
				pushContent = pushEvent.getDigest();
			}
			tvContent
					.setText(NgnStringUtils.isNullOrEmpty(pushContent) ? NgnStringUtils
							.emptyValue() : pushContent);

			LinearLayout pushInfoLastLayout = (LinearLayout) view
					.findViewById(R.id.pushinfo_last_layout);
			pushInfoLastLayout.removeAllViews();

			List lastList = pushEvent.getPushList();
			// Log.e("", "lastList size"+lastList.size());
			if (lastList != null && lastList.size() != 0) {

				for (int i = 0; i < lastList.size(); i++) {

					final ModelPush modelPushLast = (ModelPush) lastList.get(i);

					LayoutInflater inflater = LayoutInflater
							.from(getApplicationContext());
					View pushInfoLastView = inflater.inflate(
							R.layout.screen_pushinfos_last_item, null);

					final ImageView imageiconLast = (ImageView) pushInfoLastView
							.findViewById(R.id.screen_pushinfos_last_item_image);

					if (modelPushLast.imageUrl != null
							&& !modelPushLast.imageUrl.equals("")) {
						AsyncImageLoader.ImageCallback callback = new AsyncImageLoader.ImageCallback() {
							@Override
							public void imageLoaded(Integer t, Bitmap bitmap,
									ImageView v) {
								if (bitmap != null) {
									v.setImageBitmap(bitmap);
								}
							}
						};
						Bitmap bm = imageLoader
								.loadDrawable(
										R.drawable.default_push_image_large,
										modelPushLast.imageUrl, imageiconLast,
										callback);
						if (bm != null) {
							imageiconLast.setScaleType(ScaleType.FIT_XY);
						} else { // default_push_image_large.png
							imageiconLast.setScaleType(ScaleType.CENTER);
						}
					} else { // default_push_image_large.png
						imageiconLast.setScaleType(ScaleType.CENTER);
					}

					// 标题
					final TextView tvTitleLast = (TextView) pushInfoLastView
							.findViewById(R.id.screen_pushinfos_last_item_name);
					// 消息的内容/摘要
					final TextView tvContentLast = (TextView) pushInfoLastView
							.findViewById(R.id.screen_pushinfos_lastitem_content);

					if (modelPushLast.title != null
							&& !modelPushLast.title.equals("")) {
						tvTitleLast.setText(modelPushLast.title);
						tvTitleLast.setSelected(true);
					} else {
						tvTitleLast.setText(getApplicationContext().getString(R.string.notitle));
					}

					String pushLastContent = modelPushLast.content;
					if (pushLastContent == null) {
						pushLastContent = modelPushLast.digest;
					}
					tvContentLast.setText(NgnStringUtils
							.isNullOrEmpty(pushLastContent) ? NgnStringUtils
							.emptyValue() : pushLastContent);

					pushInfoLastView
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if (modelPushLast.title != null
											&& !modelPushLast.title.equals("")) {
										ScreenPushInfoLink.mTitle = modelPushLast.title;
									} else {
										ScreenPushInfoLink.mTitle = getApplicationContext().getString(R.string.notitle);
									}
									ScreenPushInfoLink
											.startPushInfoLink(modelPushLast.linkUri);

								}

							});

					pushInfoLastLayout.addView(pushInfoLastView, i);
				}

			}

			return view;
		}

		@Override
		public void update(Observable observable, Object data) {
			refresh();
		}

	}

	private static void enterLink(String sUrl) {
		Intent intent = new Intent();
		Uri uri = Uri.parse(sUrl); // sUrl
		intent.setData(uri); // http://www.fzwgov.com/Article/Html/2013/05/15_499127.html
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName("com.android.browser",
				"com.android.browser.BrowserActivity");
		SKDroid.getContext().startActivity(intent); // 启动浏览器
	}
}
