/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
 *	
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 *
 * imsdroid is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *	
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.sunkaisens.skdroid.Screens;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceGPSReport;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.ScreenChatMapAdapter;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.update.ChkVer;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenMap extends BaseScreen {
	private final static String TAG = ScreenMap.class.getCanonicalName();

	private final static String mimeType = "text/html";
	private final static String encoding = "utf-8";

	// private final static String url = "http://192.168.1.222:8080/GIS_lhr/";

	private final static String url = "http://"
			+ Engine.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.MAP_SERVER_URL,
							NgnConfigurationEntry.DEFAULT_MAP_SERVER_URL);

	private String errorHtml = "";

	private WebView mWebView;
	private WebSettings mWebSettings;

	public LinearLayout mFloatLayout;
	public WindowManager.LayoutParams wmParams;
	// ���������������ò��ֲ����Ķ���
	public WindowManager mWindowManager;

	public LinearLayout mFloatLayout_user_list;

	public LinearLayout mFloatLayout_user_gps;

	public RelativeLayout mFloatLayout_seek_site;

	public ImageButton mIbOpenKind;

	public LinearLayout mLlPlotting;

	public ImageButton mMoving;
	public ImageButton mFold;
	public ImageButton mDistance;
	public ImageButton mIBPlotting_green;
	public ImageButton mIBPlotting_red;
	public ImageButton mIBPlotting_tank;
	public ImageButton mIBPlotting_truck;
	public ImageButton mIBPlotting_drag;
	public ImageButton mIBPlotting_delete;
	public ImageButton mIBDrawCircle;
	public ImageButton mIBMarkPropModify;
	public LinearLayout mContent;
	public boolean isDraw = true;
	public boolean isPlotting_green = true;
	public boolean isPlotting_red = true;
	public boolean isPlotting_tank = true;
	public boolean isPlotting_truck = true;
	public boolean isPlotting_drag = true;
	public boolean isPlotting_delete = true;
	public boolean isDrawCircle = true;
	public boolean isMarkPropModify = true;
	public static Handler mMapHandler;

	public TextView mTitle;

	// ����PopupWindow���������
	private PopupWindow mPopupWindow;

	// ����PopupWindow���������
	private PopupWindow mPopupWindow_report;

	// ����PopupWindow���������
	private PopupWindow mPopupWindow_site;

	private RadioGroup mRGPlotting;
	private RadioButton mRBPlotting_green;
	private RadioButton mRBPlotting_red;
	private RadioButton mRBPlotting_tank;
	private RadioButton mRBPlotting_truck;

	private EditText mEtName;

	private Button mBtnOK;
	private Button mBtnCancel;

	public static LinearLayout mLlCallLayout;

	private static Button mBtnAudioCall;
	private static Button mBtnVideoCall;
	private static Button mBtnSendMessage;
	private static Button mBtnEndReport;

	private Button mBtnOpenReport;
	private Button mBtnCloseReport;

	public static HashMap<String, String> contactMapOnLine = new HashMap<String, String>();

	private ImageButton mUserOnLine;
	private ListView mUserOnLineList;
	private UserOnLineAdapter mUserOnLineAdapter;

	public static HashMap<String, String> contactMapGps = new HashMap<String, String>();
	public static List<HashMap<String, String>> contactListGps = new ArrayList<HashMap<String, String>>();

	private ImageButton mUserGps;
	private ListView mUserGpsList;
	private UserGpsAdapter mUserGpsAdapter;

	private ImageButton mIbSeekSite;

	private EditText mEtSeekSite;

	private Button mBtnSeek;
	private Button mBtnBrowse;
	private Button mBtnExit;

	private List<HashMap<String, String>> mSeekSiteList = new ArrayList<HashMap<String, String>>();

	private ListView mLvSeekSiteList;
	private SeekSiteAdapter mSeekSiteAdapter;

	private String mName = "";
	private String mFlag = "";

	private String mSeekSite = "";

	private static String mRemoteParty = "";

	private int mMessageType = 99;
	private int mIndex = 0;

	private double mLon = 116.35362;
	private double mLat = 39.82371;

	private static LayoutInflater mInflater;
	private static RelativeLayout mCallLayout;

	private static View mViewTrying;
	private static View mViewTermwait;
	private static View mViewInCallAudio;
	private static View mViewInCallVideo;
	private static FrameLayout mViewLocalVideoPreview;
	private static FrameLayout mViewRemoteVideoPreview;
	private static Button mVideoHangUpBt;
	private static View mViewChat;

	private static TextView mTvInfo;

	private static boolean SHOW_SIP_PHRASE = true;

	static ServiceAV serviceAV_map = null;

	static Context context_map = null;

	private static int mSessionType;

	public static boolean bMapCall = false; // true/false ��ͼ�����ն˺��п���

	private static boolean mIsVideoCall;

	public static ScreenChatMapAdapter mAdapter;
	private static EditText mEtCompose;
	private static ListView mLvHistoy;
	private static TextView mTvName;
	private static Button mBtSend;
	// private static ImageButton mBtadd_filetransfer_imagebutton;
	// private static Button mBtFiletransfer;
	// private static View mViewFiletransfer_view;
	// private static LinearLayout mLinearLayoutFiletransfer_ll;
	private static ImageView mBtBack;
	// ��ʶ��Ϣ���ȵ�textView
	private static TextView mTvContentCount;

	private static ModelContact userinfo = null;

	private final static INgnHistoryService mHistorytService = Engine
			.getInstance().getHistoryService();
	private final static INgnSipService mSipService = Engine.getInstance()
			.getSipService();

	public ScreenMap() {
		super(SCREEN_TYPE.AV_T, TAG);
		context_map = SKDroid.getContext();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_map);

		errorHtml = "<html><body><h1>Page not found��</h1></body></html>";

		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
				mWebView.removeView(mFloatLayout);
			}
		});
		//
		BroadcastReceiver bcReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event ��������ʵΪ���ش���CSCF-Proxy���Կͻ���ע�ᶯ������Ӧ������
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenMap.this.refresh();
				}
			}
		};

		registerReceiver(bcReceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));

		mTitle = (TextView) findViewById(R.id.title);

		mWebView = (WebView) findViewById(R.id.webView);

		// ����web��ͼ�Ŀͻ���
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.setWebChromeClient(new MyWebChromeClient());

		mWebView.setFocusable(true);
		mWebView.requestFocus(View.FOCUS_DOWN); // ��������������

		mWebSettings = mWebView.getSettings();
		mWebSettings.setDefaultTextEncodingName(encoding); // ����Ĭ�ϵ���ʾ����
		mWebSettings.setJavaScriptEnabled(true); // ����jsִ��
		// mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

		mWebView.loadDataWithBaseURL(null, "", mimeType, encoding, null);
		mWebView.clearCache(true);
		mWebView.destroyDrawingCache();

		mWebSettings.setUserAgentString("SKDroid");

		/**
		 * ��javascript�е���java���� 1.�Ƚ�һ����ǰ��java����󶨵�һ��javascript���棬ʹ�����·���
		 * webv.addJavascriptInterface(this, "someThing");
		 * //thisΪ��ǰ���󣬰󶨵�js��someThing���棬��ҪsomeThing����������ȫ�ֵġ�һ����ʼ������洦����
		 * 2.���屻���õ�java����
		 * 
		 * For example:
		 * 
		 * class JsObject {
		 * 
		 * @JavascriptInterface public String toString() { return
		 *                      "injectedObject"; } }
		 *                      webView.addJavascriptInterface(new JsObject(),
		 *                      "injectedObject"); webView.loadData("",
		 *                      "text/html", null);
		 *                      webView.loadUrl("javascript:alert(injectedObject.toString())"
		 *                      );
		 */
		mWebView.addJavascriptInterface(this, "jsScreenMap");

		/**
		 * ����webview����ʱ��������ر�����html�ļ����£� file:///android_asset/teste.html
		 * ������Ŀassets�µ��ļ�teste.html file:///sdcard/index.html
		 * ����sdcard�µ�index.html�ļ�
		 * webv.loadUrl("file:///android_asset/index.html");
		 */
		mWebView.loadUrl(url);

		mLlCallLayout = (LinearLayout) findViewById(R.id.llCall);

		mBtnAudioCall = (Button) findViewById(R.id.btnAudioCall);
		mBtnVideoCall = (Button) findViewById(R.id.btnVideoCall);
		mBtnSendMessage = (Button) findViewById(R.id.btnSendMessage);
		mBtnEndReport = (Button) findViewById(R.id.btnEndReport);

		BtnListener btn_listener = new BtnListener();

		mBtnAudioCall.setOnClickListener(btn_listener);
		mBtnVideoCall.setOnClickListener(btn_listener);
		mBtnSendMessage.setOnClickListener(btn_listener);
		mBtnEndReport.setOnClickListener(btn_listener);

		// Handler��Ϣ����
		mMapHandler = new Handler(getMainLooper()) {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageTypes.MSG_MAP_START_MEASURE:
					mWebView.loadUrl("javascript:startMeasure()");
					break;

				case MessageTypes.MSG_MAP_STOP_MEASURE:
					mWebView.loadUrl("javascript:stopMeasure()");
					break;

				case MessageTypes.MSG_MAP_START_PLOTTING_GREEN:
					Log.d(TAG, "MessageTypes.MSG_MAP_START_PLOTTING_GREEN");
					mWebView.loadUrl("javascript:PlottingByImageName(\"flag_green_32\")");
					break;

				case MessageTypes.MSG_MAP_START_PLOTTING_RED:
					mWebView.loadUrl("javascript:PlottingByImageName(\"flag_red_32\")");
					break;

				case MessageTypes.MSG_MAP_START_PLOTTING_TANK:
					mWebView.loadUrl("javascript:PlottingByImageName(\"flag_tank_32\")");
					break;

				case MessageTypes.MSG_MAP_START_PLOTTING_TRUCK:
					mWebView.loadUrl("javascript:PlottingByImageName(\"flag_truck_32\")");
					break;

				case MessageTypes.MSG_MAP_START_PLOTTING_DRAG:
					mWebView.loadUrl("javascript:PlottingByImageName(\"flag_drag_32\")");
					break;

				case MessageTypes.MSG_MAP_START_PLOTTING_DELETE:
					mWebView.loadUrl("javascript:PlottingByImageName(\"flag_delete_32\")");
					break;

				case MessageTypes.MSG_MAP_START_PLOTTING_GREEN_MARK:
					Log.d(TAG, "MessageTypes.MSG_MAP_START_PLOTTING_GREEN_MARK");
				case MessageTypes.MSG_MAP_START_PLOTTING_RED_MARK:
				case MessageTypes.MSG_MAP_START_PLOTTING_TANK_MARK:
				case MessageTypes.MSG_MAP_START_PLOTTING_TRUCK_MARK:
				case MessageTypes.MSG_MAP_START_PLOTTING_DRAG_MARK:
				case MessageTypes.MSG_MAP_START_PLOTTING_DELETE_MARK:
					createSituationMarker(msg);
					break;

				case MessageTypes.MSG_MAP_STOP_PLOTTING:
					mWebView.loadUrl("javascript:PlottingByImageName(null)");
					break;

				case MessageTypes.MSG_MAP_START_DRAW_CIRCLE:
					mWebView.loadUrl("javascript:drawCircle(true)");
					break;

				case MessageTypes.MSG_MAP_STOP_DRAW_CIRCLE:
					mWebView.loadUrl("javascript:drawCircle(false)");
					break;

				case MessageTypes.MSG_MAP_START_MRK_PROP_MODIFY:
					mWebView.loadUrl("javascript:markPropModifyActive()");
					break;

				case MessageTypes.MSG_MAP_STOP_MRK_PROP_MODIFY:
					mWebView.loadUrl("javascript:markPropModifyDeactive()");
					break;

				case MessageTypes.MSG_MAP_ANDROID_CITY:
					andriodCity(msg);
					break;

				case MessageTypes.MSG_MAP_GPS_CREATE:
					createTrackMark(msg);
					mUserGpsAdapter.notifyDataSetChanged();
					break;

				case MessageTypes.MSG_MAP_GPS_MOVE:
					moveTrackMark(msg);
					break;

				case MessageTypes.MSG_MAP_GPS_REMOVE:
					removeTrackMark(msg);
					mUserGpsAdapter.notifyDataSetChanged();
					break;

				case MessageTypes.MSG_MAP_CALL:
					setLlVisibility();
					break;

				case MessageTypes.MSG_MAP_SUICIDE:
					mCallLayout.removeAllViews();
					break;

				default:
					break;

				}
			}

			/**
			 * ����js���� �ڵ�ͼ������ӱ���ʶ���������졢���졢̹�ˡ��������֣� createSituationMarker(id,
			 * strname, lon, lat, isShowName, isShowLonlat)
			 * 
			 * @param msg
			 */
			private void createSituationMarker(Message msg) {
				Bundle b = msg.getData();
				String lonlat_lon = b.getString("lonlat_lon");
				String lonlat_lat = b.getString("lonlat_lat");
				mWebView.loadUrl("javascript:createSituationMarker(\"" + mIndex
						+ "\", \"site" + mIndex++ + "\", " + lonlat_lon + ", "
						+ lonlat_lat + ", true, false)");
			}

			/**
			 * ��ͼҳ���ʼ�� andriodCity(lon, lat)
			 * 
			 * @param msg
			 */
			private void andriodCity(Message msg) {
				Bundle b = msg.getData();
				String lon = b.getString("lon");
				String lat = b.getString("lat");
				mWebView.loadUrl("javascript:andriodCity(" + lon + ", " + lat
						+ ")");
			}

			/**
			 * createTrackMark(terminalName, terminalId, lon, lat, icon) ����ֵ����
			 * markerObjs.ID19800005001 lineObjs.ID19800005001
			 * 
			 * @param msg
			 */
			private void createTrackMark(Message msg) {
				// String localMobileNo =
				// Engine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
				// NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
				// String localName = NgnUriUtils.getDisplayName(localMobileNo);
				Bundle b = msg.getData();
				String id = b.getString("id");
				String name = b.getString("name");
				String lon = b.getString("lon");
				String lat = b.getString("lat");
				mWebView.loadUrl("javascript:markerObjs.ID" + id
						+ "=createTrackMark(\"" + name + "\", \"" + id + "\", "
						+ lon + ", " + lat + ", \"marker.png\")");
			}

			/**
			 * moveTrackMark(obj, lon, lat, strId)
			 * 
			 * @param msg
			 */
			private void moveTrackMark(Message msg) {
				Bundle b = msg.getData();
				String id = b.getString("id");
				String lon = b.getString("lon");
				String lat = b.getString("lat");
				mWebView.loadUrl("javascript:moveTrackMark(markerObjs.ID" + id
						+ ", " + lon + ", " + lat + ", " + null + ")");
			}

			/**
			 * removeTrackMark(obj)
			 */
			private void removeTrackMark(Message msg) {
				Bundle b = msg.getData();
				String id = b.getString("id");
				mWebView.loadUrl("javascript:removeTrackMark(markerObjs.ID"
						+ id + ")");
			}
		};

		ServiceGPSReport.setMapHandler(mMapHandler);

		// //���ø�������
		// setFloatingWindow();

		// ���ø�������
		setFloatingWindow_user_list();

		// //���ø�������
		// setFloatingWindow_user_gps();

		// //���ø�������
		// setFloatingWindow_seek_site();

		mInflater = LayoutInflater.from(this);

		mCallLayout = (RelativeLayout) findViewById(R.id.rl_avm);

		// isSuicide = false;
		mTimerSuicide = new NgnTimer();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mWebView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			mWebView.goBack(); // goBack()��ʾ����WebView����һҳ��
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		// ���ø�������
		setFloatingWindow();

		// //���ø�������
		// setFloatingWindow_user_list();

		// ���ø�������
		setFloatingWindow_user_gps();

		// ���ø�������
		setFloatingWindow_seek_site();

		super.onResume();
	}

	protected void onPause() {
		try {
			mWindowManager.removeView(mFloatLayout);
			mWindowManager.removeView(mFloatLayout_user_gps);
			mWindowManager.removeView(mFloatLayout_seek_site);
			mCallLayout.removeView(mViewTrying);
			mCallLayout.removeView(mViewInCallAudio);
			mCallLayout.removeView(mViewChat);
		} catch (Exception e) {
			e.printStackTrace();
		}

		bMapCall = false;

		if (serviceAV_map != null) {
			serviceAV_map.unRegisterReceiver();
			serviceAV_map.release();
			serviceAV_map = null;
		}

		// if (mWebView != null) {
		// mWebView.removeAllViews();
		// mWebView.clearView();
		// mWebView.destroy();
		// mWebView = null;
		// }

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWebView != null) {
			mWebView.removeAllViews();
			mWebView.clearHistory();
			mWebView.destroy();
		}
		mTimerSuicide.cancel();
	}

	@Override
	public boolean hasBack() {
		return true;
	}

	@Override
	public boolean back() {
		return super.back();
	}

	@Override
	public boolean refresh() {
		mUserOnLineList = (ListView) mFloatLayout_user_list
				.findViewById(R.id.lv_user_online);
		if (mUserOnLineList == null)
			return false;
		((UserOnLineAdapter) mUserOnLineList.getAdapter())
				.notifyDataSetChanged();
		return true;
	}

	public static String getRemoteParty() {
		return mRemoteParty;
	}

	private class MyWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i(TAG, "-MyWebViewClient->shouldOverrideUrlLoading()--");
			// view.loadUrl(url); //��������ӵ�ʱ��������ԭ�������ϼ���URL
			// return super.shouldOverrideUrlLoading(view, url);
			view.loadUrl(url); // ����������ʱ��ʹ�õ�ǰ�� WebView������ʹ��ϵͳ���������
			return true;
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed(); // ����https����
			super.onReceivedSslError(view, handler, error);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.i(TAG, "-MyWebViewClient->onPageStarted()--");
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.i(TAG, "-MyWebViewClient->onPageFinished()--");
			// mWebView.loadUrl("javascript:startMeasure()");
			// setFloatingWindow();
			ServiceGPSReport.bFirst = true;
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			Log.i(TAG, "-MyWebViewClient->onReceivedError()--\n errorCode="
					+ errorCode + " \ndescription=" + description
					+ " \nfailingUrl=" + failingUrl);
			// �������������������������Ը���errorCode��ֵ�����жϣ�������ϸ�Ĵ���
			view.loadData(errorHtml, mimeType, encoding);
		}
	}

	private class MyWebChromeClient extends WebChromeClient {
		// ������ҳ���صĽ�����
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			setTitle("" + getText(R.string.map_loading) + newProgress + "%");
			mTitle.setText("" + getText(R.string.map_loading) + newProgress
					+ "%");
			setProgress(newProgress * 100);

			if (newProgress == 100) {
				setTitle(R.string.app_name);
				mTitle.setText(R.string.string_map);
			}
			super.onProgressChanged(view, newProgress);
		}

		// ��ȡ��ҳ�ı���
		public void onReceivedTitle(WebView view, String title) {
		}

		// JavaScript������
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			return super.onJsAlert(view, url, message, result);
		}

		// JavaScript�����
		@Override
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, JsPromptResult result) {
			return super.onJsPrompt(view, url, message, defaultValue, result);
		}

		// JavaScriptȷ�Ͽ�
		@Override
		public boolean onJsConfirm(WebView view, String url, String message,
				JsResult result) {
			return super.onJsConfirm(view, url, message, result);
		}
	}

	// ******************************��������************************************************
	private void setFloatingWindow() {
		wmParams = new WindowManager.LayoutParams();
		// ��ȡWindowManagerImpl.CompatModeWrapper
		// mWindowManager =
		// (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		mWindowManager = this.getWindowManager();
		// ����window type
		wmParams.type = LayoutParams.TYPE_PHONE;// ; TYPE_APPLICATION_PANEL
		// ����ͼƬ��ʽ��Ч��Ϊ����͸��
		wmParams.format = PixelFormat.RGBA_8888;
		// ���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
		wmParams.flags =
		// LayoutParams.FLAG_NOT_TOUCH_MODAL |
		LayoutParams.FLAG_NOT_FOCUSABLE
		// LayoutParams.FLAG_NOT_TOUCHABLE
		;

		// ������������ʾ��ͣ��λ��Ϊ����ö�
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;

		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		wmParams.x = 0;
		wmParams.y = 250;

		/**
		 * �����������ڳ������� wmParams.width = 200; wmParams.height = 80;
		 */

		// �����������ڳ�������
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		// wmParams.verticalMargin = 1;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// ��ȡ����������ͼ���ڲ���
		mFloatLayout = (LinearLayout) inflater.inflate(
				R.layout.screen_floating_window, null);
		// ���mFloatLayout
		mWindowManager.addView(mFloatLayout, wmParams);

		// mWebView.addView(mFloatLayout, wmParams);

		Log.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
		Log.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
		Log.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
		Log.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());

		mIbOpenKind = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_open_kind);

		mLlPlotting = (LinearLayout) mFloatLayout.findViewById(R.id.llPlotting);

		// �������ڰ�ť
		mMoving = (ImageButton) mFloatLayout.findViewById(R.id.btn_moving);
		mFold = (ImageButton) mFloatLayout.findViewById(R.id.btn_fold);
		mDistance = (ImageButton) mFloatLayout.findViewById(R.id.btn_distance);
		mIBPlotting_green = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_plotting_green);
		mIBPlotting_red = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_plotting_red);
		mIBPlotting_tank = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_plotting_tank);
		mIBPlotting_truck = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_plotting_truck);
		mIBPlotting_drag = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_plotting_drag);
		mIBPlotting_delete = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_plotting_delete);
		mIBDrawCircle = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_draw_circle);
		mIBMarkPropModify = (ImageButton) mFloatLayout
				.findViewById(R.id.btn_mark_prop_modify);
		mContent = (LinearLayout) mFloatLayout.findViewById(R.id.content);

		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		Log.i(TAG, "Width/2--->" + mMoving.getMeasuredWidth() / 2);
		Log.i(TAG, "Height/2--->" + mMoving.getMeasuredHeight() / 2);
		// ���ü����������ڵĴ����ƶ�
		mMoving.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				// getRawX�Ǵ���λ���������Ļ�����꣬getX������ڰ�ť������
				wmParams.x = (int) event.getRawX() - mMoving.getWidth() / 2;
				// Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
				Log.i(TAG, "RawX" + event.getRawX());
				Log.i(TAG, "X" + event.getX());
				// 25Ϊ״̬���ĸ߶�
				wmParams.y = (int) event.getRawY() - mMoving.getHeight() / 2
						- 60;
				int height = mMoving.getHeight();
				int weigth = mMoving.getWidth();
				// Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredHeight()/2);
				Log.i(TAG, "RawY" + event.getRawY());
				Log.i(TAG, "Y" + event.getY());
				// ˢ��
				// mWindowManager.updateViewLayout(mFloatLayout, wmParams);
				// mWebView.updateViewLayout(mFloatLayout, wmParams);
				return false;
			}
		});

		BtnListener btn_listener = new BtnListener();

		mIbOpenKind.setOnClickListener(btn_listener);

		mMoving.setOnClickListener(btn_listener);
		mFold.setOnClickListener(btn_listener);
		mDistance.setOnClickListener(btn_listener);
		mIBPlotting_green.setOnClickListener(btn_listener);
		mIBPlotting_red.setOnClickListener(btn_listener);
		mIBPlotting_tank.setOnClickListener(btn_listener);
		mIBPlotting_truck.setOnClickListener(btn_listener);
		mIBPlotting_drag.setOnClickListener(btn_listener);
		mIBPlotting_delete.setOnClickListener(btn_listener);
		mIBDrawCircle.setOnClickListener(btn_listener);
		mIBMarkPropModify.setOnClickListener(btn_listener);
	}

	/**
	 * ������ť�����¼�
	 * 
	 * @author boostor
	 * 
	 */
	private class BtnListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			Log.d(TAG, "id = " + id);
			if (id == R.id.btn_fold) {
				setBtnVisibility();
			} else if (id == R.id.btn_distance) {
				isDrawDistance();
			} else if (id == R.id.btn_plotting_green) {
				situate_green(MessageTypes.MSG_MAP_START_PLOTTING_GREEN_MARK);
			} else if (id == R.id.btn_plotting_red) {
				situate_red(MessageTypes.MSG_MAP_START_PLOTTING_RED_MARK);
			} else if (id == R.id.btn_plotting_tank) {
				situate_tank(MessageTypes.MSG_MAP_START_PLOTTING_TANK_MARK);
			} else if (id == R.id.btn_plotting_truck) {
				situate_truck(MessageTypes.MSG_MAP_START_PLOTTING_TRUCK_MARK);
			} else if (id == R.id.btn_plotting_drag) {
				situate_drag(MessageTypes.MSG_MAP_START_PLOTTING_DRAG_MARK);
			} else if (id == R.id.btn_plotting_delete) {
				situate_delete(MessageTypes.MSG_MAP_START_PLOTTING_DELETE_MARK);
			} else if (id == R.id.btn_draw_circle) {
				drawCircle();
			} else if (id == R.id.btn_mark_prop_modify) {
				markPropModify();
			} else if (id == R.id.radioButton_green
					|| id == R.id.radioButton_red
					|| id == R.id.radioButton_tank
					|| id == R.id.radioButton_truck || id == R.id.btnOK
					|| id == R.id.btnCancel) {
				setFlag(id);
			} else if (id == R.id.btnAudioCall) { // ����ͨ��
				// closePopupWindow(mPopupWindow);
				bMapCall = true;
				ServiceAV.makeCall(mRemoteParty, NgnMediaType.Audio,
						SessionType.AudioCall); // mRemoteParty 19811205002
			} else if (id == R.id.btnVideoCall) { // ��Ƶͨ��
				// closePopupWindow(mPopupWindow);
				bMapCall = true;
				ServiceAV.makeCall(mRemoteParty, NgnMediaType.Video,
						SessionType.VideoCall);
			} else if (id == R.id.btnSendMessage) { // ������Ϣ
				// closePopupWindow(mPopupWindow);
				if (GlobalVar.bADHocMode) {
					final String remotePartyUri = NgnUriUtils
							.makeValidSipUri(mRemoteParty);
					Log.e(TAG,
							"sms set cscf host:"
									+ SystemVarTools
											.getIPFromUri(remotePartyUri));
					Engine.getInstance()
							.getSipService()
							.ADHOC_SetPcscfHost(
									SystemVarTools.getIPFromUri(remotePartyUri));
				}
				bMapCall = true;
				// ScreenChat.startChat(mRemoteParty, true);
				startChat_map(mRemoteParty);
			} else if (id == R.id.btnEndReport) {
				ServiceGPSReport.closeGpsReport(mRemoteParty, contactMapGps,
						contactListGps);
				// closePopupWindow(mPopupWindow);
				setLlVisibility();
			} else if (id == R.id.btnOpenReport) {
				ServiceGPSReport.openGpsReport(mRemoteParty);
				closePopupWindow(mPopupWindow_report);
			} else if (id == R.id.btnCloseReport) {
				ServiceGPSReport.closeGpsReport(mRemoteParty, contactMapGps,
						contactListGps);
				closePopupWindow(mPopupWindow_report);
				if (mLlCallLayout.getVisibility() == View.VISIBLE) {
					mLlCallLayout.setVisibility(View.GONE);
				}
			} else if (id == R.id.btn_user_online) {
				setLvVisibility();
			} else if (id == R.id.btn_user_gps) {
				setLvVisibility_gps();
			} else if (id == R.id.btn_seek_site) {
				// openSeekSiteWindow(mIbSeekSite);
				openSeekSiteWindow();
			} else if (id == R.id.etSeekSite) {
				mPopupWindow_site.setFocusable(true);
				mPopupWindow_site.update();
			} else if (id == R.id.btnSeek) {
				mSeekSite = mEtSeekSite.getText().toString().trim();
				if (mEtSeekSite != null && mEtSeekSite.getText() != null
						&& !mEtSeekSite.getText().toString().trim().equals("")) {
					mWebView.loadUrl("javascript:seekSite('" + mSeekSite + "')");
					// if (mPopupWindow != null && mPopupWindow.isShowing()) {
					// mPopupWindow.dismiss();
					// mPopupWindow = null;
					// }
				} else {
					Toast.makeText(
							NgnApplication.getContext(),
							ScreenMap.this.getString(R.string.input_place_hint),
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, "��������Ϊ��.");
				}
			} else if (id == R.id.btnBrowse) {
				setLvVisibility_seek_site();
			} else if (id == R.id.btnExit) {
				if (mPopupWindow_site != null && mPopupWindow_site.isShowing()) {
					mPopupWindow_site.dismiss();
					mPopupWindow_site = null;
				}
				mIbSeekSite.setImageResource(R.drawable.map_open_seek_site);
				mLvSeekSiteList.setVisibility(View.GONE);
			} else if (id == R.id.btn_open_kind) {
				setLlVisibility_kind();

			}
		} // onClick()

		/**
		 * ��ʾ/���ر�漰������ȹ���ͼƬ��ť
		 */
		private void setBtnVisibility() {
			if (mContent.getVisibility() == View.VISIBLE) {
				mContent.setVisibility(View.GONE);
				mFold.setBackgroundResource(R.drawable.map_close_group);
			} else {
				mContent.setVisibility(View.VISIBLE);
				mFold.setBackgroundResource(R.drawable.map_open_group);
			}
		}

		/**
		 * ����/�䰵���ͼƬ��ť
		 */
		private void isDrawDistance() {
			if (isDraw) { // ִ��֮�󣬵����ͼ���Բ��
				mDistance.setBackgroundResource(R.drawable.map_close_draw_line);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_MEASURE);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mDistance.setBackgroundResource(R.drawable.map_open_draw_line);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_MEASURE);
				mMapHandler.sendMessage(msg);
			}
			isDraw = !isDraw;
		}

		/**
		 * ����/�䰵������ͼƬ��ť
		 * 
		 * @param messageType
		 */
		private void situate_green(int messageType) {
			Log.d(TAG, "situate_green()");
			mMessageType = messageType;
			if (isPlotting_green) { // ִ��֮�󣬵����ͼ���Բ�����
				mIBPlotting_green
						.setBackgroundResource(R.drawable.map_close_flag);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_PLOTTING_GREEN);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBPlotting_green
						.setBackgroundResource(R.drawable.map_open_flag_green);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_PLOTTING);
				mMapHandler.sendMessage(msg);
			}
			isPlotting_green = !isPlotting_green;
		}

		/**
		 * ����/�䰵�����ͼƬ��ť
		 * 
		 * @param messageType
		 */
		private void situate_red(int messageType) {
			Log.d(TAG, "situate_red()");
			mMessageType = messageType;
			if (isPlotting_red) { // ִ��֮�󣬵����ͼ���Բ����
				mIBPlotting_red
						.setBackgroundResource(R.drawable.map_close_flag);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_PLOTTING_RED);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBPlotting_red
						.setBackgroundResource(R.drawable.map_open_flag_red);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_PLOTTING);
				mMapHandler.sendMessage(msg);
			}
			isPlotting_red = !isPlotting_red;
		}

		/**
		 * ����/�䰵��̹��ͼƬ��ť
		 * 
		 * @param messageType
		 */
		private void situate_tank(int messageType) {
			Log.d(TAG, "situate_tank()");
			mMessageType = messageType;
			if (isPlotting_tank) { // ִ��֮�󣬵����ͼ���Բ�̹��
				mIBPlotting_tank
						.setBackgroundResource(R.drawable.map_close_flag_tank);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_PLOTTING_TANK);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBPlotting_tank
						.setBackgroundResource(R.drawable.map_open_flag_tank);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_PLOTTING);
				mMapHandler.sendMessage(msg);
			}
			isPlotting_tank = !isPlotting_tank;
		}

		/**
		 * ����/�䰵�忨��ͼƬ��ť
		 * 
		 * @param messageType
		 */
		private void situate_truck(int messageType) {
			Log.d(TAG, "situate_truck()");
			mMessageType = messageType;
			if (isPlotting_truck) { // ִ��֮�󣬵����ͼ���Բ忨��
				mIBPlotting_truck
						.setBackgroundResource(R.drawable.map_close_flag_truck);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_PLOTTING_TRUCK);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBPlotting_truck
						.setBackgroundResource(R.drawable.map_open_flag_truck);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_PLOTTING);
				mMapHandler.sendMessage(msg);
			}
			isPlotting_truck = !isPlotting_truck;
		}

		/**
		 * ����/�䰵�϶�����ʶͼƬ��ť
		 * 
		 * @param messageType
		 */
		private void situate_drag(int messageType) {
			Log.d(TAG, "situate_drag()");
			mMessageType = messageType;
			if (isPlotting_drag) { // ִ��֮�󣬵����ͼ�����϶�����ʶ
				mIBPlotting_drag
						.setBackgroundResource(R.drawable.map_close_flag_drag);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_PLOTTING_DRAG);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBPlotting_drag
						.setBackgroundResource(R.drawable.map_open_flag_drag);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_PLOTTING);
				mMapHandler.sendMessage(msg);
			}
			isPlotting_drag = !isPlotting_drag;
		}

		/**
		 * ����/�䰵ɾ������ʶͼƬ��ť
		 * 
		 * @param messageType
		 */
		private void situate_delete(int messageType) {
			Log.d(TAG, "situate_delete()");
			mMessageType = messageType;
			if (isPlotting_delete) { // ִ��֮�󣬵����ͼ����ɾ������ʶ
				mIBPlotting_delete
						.setBackgroundResource(R.drawable.map_close_flag_delete);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_PLOTTING_DELETE);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBPlotting_delete
						.setBackgroundResource(R.drawable.map_open_flag_delete);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_PLOTTING);
				mMapHandler.sendMessage(msg);
			}
			isPlotting_delete = !isPlotting_delete;
		}

		/**
		 * ����/�䰵��Ȧѡ�����ʶͼƬ��ť
		 */
		private void drawCircle() {
			Log.d(TAG, "drawCircle()");
			if (isDrawCircle) { // ִ��֮�󣬵����ͼ���Ի�Ȧѡ�����ʶ
				mIBDrawCircle
						.setBackgroundResource(R.drawable.map_close_draw_circle);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_DRAW_CIRCLE);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBDrawCircle
						.setBackgroundResource(R.drawable.map_open_draw_circle);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_DRAW_CIRCLE);
				mMapHandler.sendMessage(msg);
			}
			isDrawCircle = !isDrawCircle;
		}

		/**
		 * ����/�䰵�����޸�ͼƬ��ť
		 */
		private void markPropModify() {
			Log.d(TAG, "markPropModify()");
			if (isMarkPropModify) { // ִ��֮�󣬵����ͼ�����޸�����
				mIBMarkPropModify
						.setBackgroundResource(R.drawable.map_close_prop_modify);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_START_MRK_PROP_MODIFY);
				mMapHandler.sendMessage(msg);
			} else { // ִ��֮��ȡ����������
				mIBMarkPropModify
						.setBackgroundResource(R.drawable.map_open_prop_modify);
				Message msg = Message.obtain(mMapHandler,
						MessageTypes.MSG_MAP_STOP_MRK_PROP_MODIFY);
				mMapHandler.sendMessage(msg);
			}
			isMarkPropModify = !isMarkPropModify;
		}

		/**
		 * �����������͵�
		 * 
		 * @param id
		 */
		private void setFlag(int id) {
			Log.d(TAG, "setFlag()");
			Log.d(TAG, "id = " + id);
			mRGPlotting.check(id);
			if (id == R.id.radioButton_green) {
				mFlag = "flag_green_32";
			} else if (id == R.id.radioButton_red) {
				mFlag = "flag_red_32";
			} else if (id == R.id.radioButton_tank) {
				mFlag = "flag_tank_32";
			} else if (id == R.id.radioButton_truck) {
				mFlag = "flag_truck_32";
			} else if (id == R.id.btnOK) {
				mName = mEtName.getText().toString().trim();
				if (mEtName != null && mEtName.getText() != null
						&& !mEtName.getText().toString().trim().equals("")) {
					mWebView.loadUrl("javascript:modifySituationProp('" + mName
							+ "', '" + mFlag + "')"); // \"flag_green_32\"
					closePopupWindow(mPopupWindow);
				} else {
					Toast.makeText(NgnApplication.getContext(),
							ScreenMap.this.getString(R.string.input_name_hint),
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, "���Ʋ���Ϊ��.");
				}
			} else if (id == R.id.btnCancel) {
				closePopupWindow(mPopupWindow);

			}
		} // setFlag()

		/**
		 * ��ʾ/���������û��б�
		 */
		private void setLvVisibility() {
			if (mUserOnLineList.getVisibility() == View.VISIBLE) {
				mUserOnLineList.setVisibility(View.GONE);
				// mUserOnLine.setBackgroundResource(R.drawable.map_open_user_online);
				mUserOnLine.setImageResource(R.drawable.map_open_user_online);
			} else {
				mUserOnLineList.setVisibility(View.VISIBLE);
				// mUserOnLine.setBackgroundResource(R.drawable.map_close_user_online);
				mUserOnLine.setImageResource(R.drawable.map_close_user_online);
			}
		}

		/**
		 * ��ʾ/�����ϱ��û��б�
		 */
		private void setLvVisibility_gps() {
			if (mUserGpsList.getVisibility() == View.VISIBLE) {
				mUserGpsList.setVisibility(View.GONE);
				mUserGps.setImageResource(R.drawable.map_open_user_gps);
			} else {
				mUserGpsList.setVisibility(View.VISIBLE);
				mUserGps.setImageResource(R.drawable.map_close_user_gps);
			}
		}

		/**
		 * ����ʾ��ѯ�ص㴰�� ���ز�ѯ��ַ�б�
		 */
		private void openSeekSiteWindow() {
			if (mPopupWindow_site != null && mPopupWindow_site.isShowing()) {
				mPopupWindow_site.dismiss();
				mPopupWindow_site = null;
				mIbSeekSite.setImageResource(R.drawable.map_open_seek_site);
				mLvSeekSiteList.setVisibility(View.GONE);
			} else {
				ScreenMap.this.openSeekSiteWindow(mIbSeekSite);
				mIbSeekSite.setImageResource(R.drawable.map_close_seek_site);
			}
		}

		/**
		 * ��ʾ/���ر��ͼ�갴ť
		 */
		private void setLlVisibility_kind() {
			if (mLlPlotting.getVisibility() == View.VISIBLE) {
				mLlPlotting.setVisibility(View.GONE);
				mIbOpenKind.setBackgroundResource(R.drawable.map_open_kind);
			} else {
				mLlPlotting.setVisibility(View.VISIBLE);
				mIbOpenKind.setBackgroundResource(R.drawable.map_close_kind);
			}
		}
	} // class BtnListener

	private void closePopupWindow(PopupWindow popupWindow) {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	// ******************************��������************************************************
	private void setFloatingWindow_user_list() {

		wmParams = new WindowManager.LayoutParams();
		// ��ȡWindowManagerImpl.CompatModeWrapper
		// mWindowManager =
		// (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		mWindowManager = this.getWindowManager();
		// ����window type
		wmParams.type = LayoutParams.TYPE_PHONE;// ; TYPE_APPLICATION_PANEL
		// ����ͼƬ��ʽ��Ч��Ϊ����͸��
		wmParams.format = PixelFormat.RGBA_8888;
		// ���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
		wmParams.flags =
		// LayoutParams.FLAG_NOT_TOUCH_MODAL |
		LayoutParams.FLAG_NOT_FOCUSABLE
		// LayoutParams.FLAG_NOT_TOUCHABLE
		;

		// ������������ʾ��ͣ��λ��Ϊ����ö�
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;

		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		wmParams.x = 0;
		wmParams.y = 0;

		/**
		 * �����������ڳ������� wmParams.width = 200; wmParams.height = 80;
		 */

		int width = mWindowManager.getDefaultDisplay().getWidth();

		// �����������ڳ�������
		// wmParams.width = 600; //WindowManager.LayoutParams.MATCH_PARENT
		wmParams.width = width * 2 / 3; // WindowManager.LayoutParams.MATCH_PARENT
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// ��ȡ����������ͼ���ڲ���
		mFloatLayout_user_list = (LinearLayout) inflater.inflate(
				R.layout.screen_floating_window_user_list, null);
		// ���mFloatLayout
		// mWindowManager.addView(mFloatLayout, wmParams);

		mWebView.addView(mFloatLayout_user_list, wmParams);

		Log.i(TAG,
				"mFloatLayout_user_list-->left"
						+ mFloatLayout_user_list.getLeft());
		Log.i(TAG,
				"mFloatLayout_user_list-->right"
						+ mFloatLayout_user_list.getRight());
		Log.i(TAG,
				"mFloatLayout_user_list-->top"
						+ mFloatLayout_user_list.getTop());
		Log.i(TAG,
				"mFloatLayout_user_list-->bottom"
						+ mFloatLayout_user_list.getBottom());

		// �������ڰ�ť
		mUserOnLine = (ImageButton) mFloatLayout_user_list
				.findViewById(R.id.btn_user_online);

		mUserOnLineList = (ListView) mFloatLayout_user_list
				.findViewById(R.id.lv_user_online);

		mUserOnLineAdapter = new UserOnLineAdapter(this);
		mUserOnLineList.setAdapter(mUserOnLineAdapter);
		mUserOnLineList
				.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mUserOnLineList.setStackFromBottom(true);

		mFloatLayout_user_list.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		BtnListener btn_listener = new BtnListener();

		mUserOnLine.setOnClickListener(btn_listener);
	}

	// ******************************��������************************************************
	private void setFloatingWindow_user_gps() {

		wmParams = new WindowManager.LayoutParams();
		// ��ȡWindowManagerImpl.CompatModeWrapper
		// mWindowManager =
		// (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		mWindowManager = this.getWindowManager();
		// ����window type
		wmParams.type = LayoutParams.TYPE_PHONE;// ; TYPE_APPLICATION_PANEL
		// ����ͼƬ��ʽ��Ч��Ϊ����͸��
		wmParams.format = PixelFormat.RGBA_8888;
		// ���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
		wmParams.flags =
		// LayoutParams.FLAG_NOT_TOUCH_MODAL |
		LayoutParams.FLAG_NOT_FOCUSABLE
		// LayoutParams.FLAG_NOT_TOUCHABLE
		;

		// ������������ʾ��ͣ��λ��Ϊ����ö�
		wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;

		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		wmParams.x = 0;
		wmParams.y = 0;

		/**
		 * �����������ڳ������� wmParams.width = 200; wmParams.height = 80;
		 */

		int width = mWindowManager.getDefaultDisplay().getWidth();

		// �����������ڳ�������
		// wmParams.width = 250; //WindowManager.LayoutParams.MATCH_PARENT
		wmParams.width = width * 2 / 3; // WindowManager.LayoutParams.MATCH_PARENT
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// ��ȡ����������ͼ���ڲ���
		mFloatLayout_user_gps = (LinearLayout) inflater.inflate(
				R.layout.screen_floating_window_user_gps, null);
		// ���mFloatLayout
		mWindowManager.addView(mFloatLayout_user_gps, wmParams);

		// mWebView.addView(mFloatLayout_user_gps, wmParams);

		Log.i(TAG,
				"mFloatLayout_user_gps-->left"
						+ mFloatLayout_user_gps.getLeft());
		Log.i(TAG,
				"mFloatLayout_user_gps-->right"
						+ mFloatLayout_user_gps.getRight());
		Log.i(TAG,
				"mFloatLayout_user_gps-->top" + mFloatLayout_user_gps.getTop());
		Log.i(TAG,
				"mFloatLayout_user_gps-->bottom"
						+ mFloatLayout_user_gps.getBottom());

		// �������ڰ�ť
		mUserGps = (ImageButton) mFloatLayout_user_gps
				.findViewById(R.id.btn_user_gps);

		mUserGpsList = (ListView) mFloatLayout_user_gps
				.findViewById(R.id.lv_user_gps);

		mUserGpsAdapter = new UserGpsAdapter(this);
		mUserGpsList.setAdapter(mUserGpsAdapter);
		mUserGpsList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mUserGpsList.setStackFromBottom(true);

		mFloatLayout_user_gps.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		BtnListener btn_listener = new BtnListener();

		mUserGps.setOnClickListener(btn_listener);
	}

	// ******************************��������************************************************
	private void setFloatingWindow_seek_site() {

		wmParams = new WindowManager.LayoutParams();
		// ��ȡWindowManagerImpl.CompatModeWrapper
		// mWindowManager =
		// (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		mWindowManager = this.getWindowManager();
		// ����window type
		wmParams.type = LayoutParams.TYPE_PHONE;// ; TYPE_APPLICATION_PANEL
		// ����ͼƬ��ʽ��Ч��Ϊ����͸��
		wmParams.format = PixelFormat.RGBA_8888;
		// ���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
		wmParams.flags =
		// LayoutParams.FLAG_NOT_TOUCH_MODAL |
		LayoutParams.FLAG_NOT_FOCUSABLE
		// LayoutParams.FLAG_NOT_TOUCHABLE
		;

		// ������������ʾ��ͣ��λ��Ϊ����ö�
		wmParams.gravity = Gravity.RIGHT | Gravity.TOP;

		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		wmParams.x = 0;
		wmParams.y = 50;

		/**
		 * �����������ڳ������� wmParams.width = 200; wmParams.height = 80;
		 */

		// �����������ڳ�������
		wmParams.width = 250; // WindowManager.LayoutParams.WRAP_CONTENT
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// ��ȡ����������ͼ���ڲ���
		mFloatLayout_seek_site = (RelativeLayout) inflater.inflate(
				R.layout.screen_floating_window_seek_site, null);
		// ���mFloatLayout
		mWindowManager.addView(mFloatLayout_seek_site, wmParams);

		// �������ڰ�ť
		mIbSeekSite = (ImageButton) mFloatLayout_seek_site
				.findViewById(R.id.btn_seek_site);

		mLvSeekSiteList = (ListView) mFloatLayout_seek_site
				.findViewById(R.id.lv_seek_site);

		mSeekSiteAdapter = new SeekSiteAdapter(this);
		mLvSeekSiteList.setAdapter(mSeekSiteAdapter);
		mLvSeekSiteList
				.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mLvSeekSiteList.setStackFromBottom(true);

		BtnListener btn_listener = new BtnListener();

		mIbSeekSite.setOnClickListener(btn_listener);
	}

	/**
	 * ���屻���õ�java���� �ڵ�ͼ������ӱ���ʶ���������졢���졢̹�ˡ��������֣� �˷���Ϊ�ص�����
	 * javascript:window.jsScreenMap.addSituationMarker('lonlat_lon',
	 * 'lonlat_lat', 'situationImage')
	 * 
	 * @param lonlat_lon
	 * @param lonlat_lat
	 * @param situationImage
	 */
	public void addSituationMarker(String lonlat_lon, String lonlat_lat,
			String situationImage) {
		Log.d(TAG, "addSituationMarker()");
		Message msg = Message.obtain(mMapHandler, mMessageType);
		Bundle b = new Bundle();
		b.putString("lonlat_lon", lonlat_lon);
		b.putString("lonlat_lat", lonlat_lat);
		msg.setData(b);
		mMapHandler.sendMessage(msg);
	}

	/**
	 * ���屻���õ�java���� ��ͼҳ���ʼ�� �˷���Ϊ�ص����� javascript:window.jsScreenMap.initEnd()
	 */
	public void initEnd() {
		Log.d(TAG, "initEnd()");
		putGpsInfo();
		Message msg = Message.obtain(mMapHandler,
				MessageTypes.MSG_MAP_ANDROID_CITY);
		Bundle b = new Bundle();
		b.putString("lon", mLon + ""); // lon
		b.putString("lat", mLat + ""); // lat
		msg.setData(b);
		mMapHandler.sendMessage(msg);
	}

	/**
	 * ���屻���õ�java���� ���ز�ѯ��ַ�����б� �˷���Ϊ�ص����� ��js����"javascript:seekSite(site)"�����ûص�����
	 * javascript:window.jsScreenMap.PlaceSearch(query.analyseXML(req));
	 * 1420940431;;bus_stop;;�廪��ѧ����;116.30899249999663, 39.99682250000023:
	 * 1501538379;;bus_stop;;�廪��ѧ����;116.30901510000119, 39.99653509999984:
	 * 245130485;;bus_stop;;�廪��ѧ����;116.3092120000036, 39.996137000000154
	 * 
	 * @param queryXml
	 */
	public void PlaceSearch(String queryXml) {
		Log.d(TAG, "PlaceSearch()");
		mSeekSiteList.clear();
		parseQueryXml(queryXml);
		// setLvVisibility_seek_site();
	}

	/**
	 * ���ա�����GPS�ϱ����� �ڵ�ͼ�������ն�ͼ��
	 * 
	 * @param contentBody
	 * @param appName
	 */
	private void parseQueryXml(String queryXml) {
		String[] seekSiteItems = queryXml.split(":"); // 1420940431;;bus_stop;;�廪��ѧ����;116.30899249999663,
														// 39.99682250000023
		int count = seekSiteItems.length;
		for (int i = 0; i < count; i++) {
			String[] items = seekSiteItems[i].split(";");
			HashMap<String, String> siteinfo = new HashMap<String, String>();
			siteinfo.put("id", items[0]);
			siteinfo.put("site", items[4]);
			String[] gpss = items[5].split(", ");
			siteinfo.put("lon", gpss[0]);
			siteinfo.put("lat", gpss[1]);
			mSeekSiteList.add(siteinfo);
		}
	}

	/**
	 * ��ʾ/���ز�ѯ��ַ�б�
	 */
	private void setLvVisibility_seek_site() {
		if (mLvSeekSiteList.getVisibility() == View.VISIBLE) {
			mLvSeekSiteList.setVisibility(View.GONE);
			mPopupWindow_site.setFocusable(true);
			mPopupWindow_site.update();
		} else {
			mLvSeekSiteList.setVisibility(View.VISIBLE);
			mPopupWindow_site.setFocusable(false);
		}
	}

	/**
	 * ���屻���õ�java���� �������Ĳ˵����� �򿪵����˵����� ���޸����Դ���
	 * javascript:window.jsScreenMap.openPropWindow()
	 */
	public void openPropWindow(View anchor, int x, int y) {
		getPopupWindow_prop();
		// ������λ����ʾ��ʽ,����Ļ�����
		mPopupWindow.showAtLocation(mFold, Gravity.LEFT, x, y - 340);
		// mPopupWindow.showAtLocation(mFold, Gravity.NO_GRAVITY, x, y + 80);
		// mPopupWindow.showAsDropDown(mFold, x, y - 50);
		// mPopupWindow.showAsDropDown(mFold);
	}

	/**
	 * ���屻���õ�java���� �������Ĳ˵����� �򿪵����˵����� ��ͨ�������ŵȴ���
	 * javascript:window.jsScreenMap.openCallWindow()
	 */
	public void openCallWindow(String remoteParty, int x, int y) {
		mRemoteParty = remoteParty;
		// getPopupWindow_call();
		// // ������λ����ʾ��ʽ,����Ļ�����
		// mPopupWindow.showAtLocation(mUserOnLine, Gravity.LEFT, x, y - 335);

		if (mMapHandler != null) { // �����ڵ�ͼ���������ն˺��н������Ϣ
			Log.d(TAG, "openCallWindow() - mMapHandler != null");
			Message msg = Message
					.obtain(mMapHandler, MessageTypes.MSG_MAP_CALL);
			mMapHandler.sendMessage(msg);
		}
	}

	/**
	 * ��ʾ/���غ��й��ܽ��� ��������ͨ������Ƶͨ����������Ϣ���ر��ϱ�����
	 */
	private void setLlVisibility() {
		if (mLlCallLayout.getVisibility() == View.VISIBLE) {
			mLlCallLayout.setVisibility(View.GONE);
		} else {
			mLlCallLayout.setVisibility(View.VISIBLE);
			mLlCallLayout.bringToFront();
		}
	}

	/**
	 * �򿪡��ر�GPS�ϱ������˵�����
	 */
	private void openReportWindow(View anchor, String remoteParty) {
		mRemoteParty = remoteParty;
		getPopupWindow_report();
		mPopupWindow_report.showAsDropDown(anchor);
	}

	/**
	 * �򿪲�ѯ�ص㴰�ڵ����˵�����
	 */
	private void openSeekSiteWindow(View anchor) {
		getPopupWindow_seek_site();
		mPopupWindow_site.showAsDropDown(anchor);
	}

	/**
	 * ����PopupWindow
	 */
	protected void initPopuptWindow_prop() {
		// ��ȡ�Զ��岼���ļ�popup_window_map_prop.xml����ͼ
		View popupWindow_view = getLayoutInflater().inflate(
				R.layout.popup_window_map_prop, null, false);
		// ����PopupWindowʵ��,200,LayoutParams.MATCH_PARENT�ֱ��ǿ�Ⱥ͸߶�
		// mPopupWindow = new PopupWindow(popupWindow_view, 200,
		// LayoutParams.MATCH_PARENT, true);
		mPopupWindow = new PopupWindow(popupWindow_view,
				LayoutParams.WRAP_CONTENT, 150, true);
		// // ���ö���Ч��
		// mPopupWindow.setAnimationStyle(R.style.AnimationFade);
		// ��������ط���ʧ
		popupWindow_view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mPopupWindow != null && mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
					mPopupWindow = null;
				}
				return false;
			}
		});

		mRGPlotting = (RadioGroup) popupWindow_view
				.findViewById(R.id.radioGroupProp);
		mRBPlotting_green = (RadioButton) popupWindow_view
				.findViewById(R.id.radioButton_green);
		mRBPlotting_red = (RadioButton) popupWindow_view
				.findViewById(R.id.radioButton_red);
		mRBPlotting_tank = (RadioButton) popupWindow_view
				.findViewById(R.id.radioButton_tank);
		mRBPlotting_truck = (RadioButton) popupWindow_view
				.findViewById(R.id.radioButton_truck);

		mEtName = (EditText) popupWindow_view.findViewById(R.id.etName);

		mBtnOK = (Button) popupWindow_view.findViewById(R.id.btnOK);
		mBtnCancel = (Button) popupWindow_view.findViewById(R.id.btnCancel);

		BtnListener btn_listener = new BtnListener();

		mRBPlotting_green.setOnClickListener(btn_listener);
		mRBPlotting_red.setOnClickListener(btn_listener);
		mRBPlotting_tank.setOnClickListener(btn_listener);
		mRBPlotting_truck.setOnClickListener(btn_listener);

		mBtnOK.setOnClickListener(btn_listener);
		mBtnCancel.setOnClickListener(btn_listener);
	}

	/***
	 * ��ȡPopupWindowʵ��
	 */
	private void getPopupWindow_prop() {
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
			return;
		} else {
			initPopuptWindow_prop();
		}
	}

	/**
	 * ����PopupWindow
	 */
	protected void initPopuptWindow_call() {
		// ��ȡ�Զ��岼���ļ�popup_window_map_call.xml����ͼ
		View popupWindow_view = getLayoutInflater().inflate(
				R.layout.popup_window_map_call, null, false);
		// ����PopupWindowʵ��,200,LayoutParams.MATCH_PARENT�ֱ��ǿ�Ⱥ͸߶�
		// mPopupWindow = new PopupWindow(popupWindow_view, 200,
		// LayoutParams.MATCH_PARENT, true);
		mPopupWindow = new PopupWindow(popupWindow_view,
				LayoutParams.WRAP_CONTENT, 150, true);
		// // ���ö���Ч��
		// mPopupWindow.setAnimationStyle(R.style.AnimationFade);
		// ��������ط���ʧ
		popupWindow_view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mPopupWindow != null && mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
					mPopupWindow = null;
				}
				return false;
			}
		});

		mBtnAudioCall = (Button) popupWindow_view
				.findViewById(R.id.btnAudioCall);
		mBtnVideoCall = (Button) popupWindow_view
				.findViewById(R.id.btnVideoCall);
		mBtnSendMessage = (Button) popupWindow_view
				.findViewById(R.id.btnSendMessage);
		mBtnEndReport = (Button) popupWindow_view
				.findViewById(R.id.btnEndReport);

		BtnListener btn_listener = new BtnListener();

		mBtnAudioCall.setOnClickListener(btn_listener);
		mBtnVideoCall.setOnClickListener(btn_listener);
		mBtnSendMessage.setOnClickListener(btn_listener);
		mBtnEndReport.setOnClickListener(btn_listener);
	}

	/***
	 * ��ȡPopupWindowʵ��
	 */
	private void getPopupWindow_call() {
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
			return;
		} else {
			initPopuptWindow_call();
		}
	}

	/**
	 * ����PopupWindow
	 */
	protected void initPopuptWindow_report() {
		// ��ȡ�Զ��岼���ļ�popup_window_map_report.xml����ͼ
		View popupWindow_view = getLayoutInflater().inflate(
				R.layout.popup_window_map_report, null, false);
		// ����PopupWindowʵ��,200,LayoutParams.MATCH_PARENT�ֱ��ǿ�Ⱥ͸߶�
		// mPopupWindow_report = new PopupWindow(popupWindow_view, 200,
		// LayoutParams.MATCH_PARENT, true);
		// mPopupWindow_report = new PopupWindow(popupWindow_view,
		// LayoutParams.WRAP_CONTENT, 80, true);
		mPopupWindow_report = new PopupWindow(popupWindow_view,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		// // ���ö���Ч��
		// mPopupWindow_report.setAnimationStyle(R.style.AnimationFade);
		// ��������ط���ʧ
		popupWindow_view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mPopupWindow_report != null
						&& mPopupWindow_report.isShowing()) {
					mPopupWindow_report.dismiss();
					mPopupWindow_report = null;
					// mUserOnLineAdapter.notifyDataSetChanged();
				}
				return false;
			}
		});

		mBtnOpenReport = (Button) popupWindow_view
				.findViewById(R.id.btnOpenReport);
		mBtnCloseReport = (Button) popupWindow_view
				.findViewById(R.id.btnCloseReport);

		BtnListener btn_listener = new BtnListener();

		mBtnOpenReport.setOnClickListener(btn_listener);
		mBtnCloseReport.setOnClickListener(btn_listener);
	}

	/***
	 * ��ȡPopupWindowʵ��
	 */
	private void getPopupWindow_report() {
		if (null != mPopupWindow_report) {
			mPopupWindow_report.dismiss();
			return;
		} else {
			initPopuptWindow_report();
		}
	}

	/**
	 * ����PopupWindow
	 */
	protected void initPopuptWindow_seek_site() {
		// ��ȡ�Զ��岼���ļ�popup_window_map_report.xml����ͼ
		View popupWindow_view = getLayoutInflater().inflate(
				R.layout.popup_window_map_seek_site, null, false);
		// ����PopupWindowʵ��,200,LayoutParams.MATCH_PARENT�ֱ��ǿ�Ⱥ͸߶�
		// mPopupWindow_site = new PopupWindow(popupWindow_view, 200,
		// LayoutParams.MATCH_PARENT, true);
		mPopupWindow_site = new PopupWindow(popupWindow_view,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		// // ���ö���Ч��
		// mPopupWindow_site.setAnimationStyle(R.style.AnimationFade);
		// ��������ط���ʧ
		// popupWindow_view.setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// if (mPopupWindow_site != null && mPopupWindow_site.isShowing()) {
		// mPopupWindow_site.dismiss();
		// mPopupWindow_site = null;
		// }
		// return false;
		// }
		// });

		mEtSeekSite = (EditText) popupWindow_view.findViewById(R.id.etSeekSite);

		mBtnSeek = (Button) popupWindow_view.findViewById(R.id.btnSeek);
		mBtnBrowse = (Button) popupWindow_view.findViewById(R.id.btnBrowse);
		mBtnExit = (Button) popupWindow_view.findViewById(R.id.btnExit);

		BtnListener btn_listener = new BtnListener();

		mEtSeekSite.setOnClickListener(btn_listener);
		mBtnSeek.setOnClickListener(btn_listener);
		mBtnBrowse.setOnClickListener(btn_listener);
		mBtnExit.setOnClickListener(btn_listener);
	}

	/***
	 * ��ȡPopupWindowʵ��
	 */
	private void getPopupWindow_seek_site() {
		if (null != mPopupWindow_site) {
			mPopupWindow_site.dismiss();
			return;
		} else {
			initPopuptWindow_seek_site();
		}
	}

	/**
	 * ��GPS��ȡ�����λ����Ϣ��
	 */
	private void putGpsInfo() {
		LocationManager mLocationManager = (LocationManager) NgnApplication
				.getContext().getSystemService(Context.LOCATION_SERVICE);

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // �ж�gps�Ƿ����
			Location location = mLocationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER); // ��GPS��ȡ�����λ����Ϣ��

			if (location != null) {
				mLon = location.getLongitude();
				mLat = location.getLatitude();
			} else {
				Log.d(TAG, "��λ��Ϣ��ȡʧ��.");
			}
		} else {
			String appName = ChkVer.getAppName(getApplicationContext());
			SystemVarTools.showToast(appName
					+ ScreenMap.this.getString(R.string.gps_unreable_hint));
			Log.d(TAG, appName + "��ʾ��\n gps������");
		}
	}

	/**
	 * UserOnLineAdapter
	 */
	private class UserOnLineAdapter extends BaseAdapter {
		private List<ModelContact> mUsers;
		private final LayoutInflater mInflater;
		private final ScreenMap mBaseScreen;

		public UserOnLineAdapter(ScreenMap baseScreen) {
			mBaseScreen = baseScreen;
			mInflater = LayoutInflater.from(mBaseScreen);
			// mUsers = SystemVarTools.getContactAll();
			// mUsers = SystemVarTools.getContactAllOnLine();
			String localMobileNo = Engine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
							NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
			mUsers = SystemVarTools.getContactAllOnLineNoSelf(localMobileNo);
		}

		@Override
		public int getCount() {
			return mUsers.size();
		}

		@Override
		public Object getItem(int position) {
			return mUsers.get(position);
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
			final ModelContact userinfo = (ModelContact) getItem(position);
			if (userinfo == null) {
				return null;
			}
			if (view == null) {
				view = mInflater.inflate(
						R.layout.screen_floating_window_user_list_item, null);
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// v.setBackgroundColor(Color.RED);
						int pos = (Integer) v.getTag();
						final ModelContact userinfo = (ModelContact) getItem(pos);
						if (userinfo != null) {
							openReportWindow(v, userinfo.mobileNo);
						}
					}
				});
			}

			view.setTag(position);

			TextView user_name = (TextView) view.findViewById(R.id.user_name);
			TextView user_id = (TextView) view.findViewById(R.id.user_id);
			user_name.setText(userinfo.name);
			user_id.setText(userinfo.mobileNo);

			// view.setBackgroundColor(Color.TRANSPARENT);
			// // view.setBackgroundResource(R.color.color_mainbg);

			return view;
		}

	}

	/**
	 * UserGpsAdapter
	 */
	private class UserGpsAdapter extends BaseAdapter {
		private List<HashMap<String, String>> mUsers;
		private final LayoutInflater mInflater;
		private final ScreenMap mBaseScreen;

		public UserGpsAdapter(ScreenMap baseScreen) {
			mBaseScreen = baseScreen;
			mInflater = LayoutInflater.from(mBaseScreen);
			mUsers = contactListGps;
		}

		@Override
		public int getCount() {
			return mUsers.size();
		}

		@Override
		public Object getItem(int position) {
			return mUsers.get(position);
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
			@SuppressWarnings("unchecked")
			final HashMap<String, String> userinfo = (HashMap<String, String>) getItem(position);
			if (userinfo == null) {
				return null;
			}
			if (view == null) {
				view = mInflater.inflate(
						R.layout.screen_floating_window_user_gps_item, null);
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = (Integer) v.getTag();
						@SuppressWarnings("unchecked")
						final HashMap<String, String> userinfo = (HashMap<String, String>) getItem(pos);
						if (userinfo != null) {
							String lon = userinfo.get("lon");
							String lat = userinfo.get("lat");
							selectSite(lon, lat);
						}
					}
				});
			}

			view.setTag(position);

			ImageButton icon_gps = (ImageButton) view
					.findViewById(R.id.icon_gps);
			TextView user_id_gps = (TextView) view
					.findViewById(R.id.user_id_gps);
			ImageButton show_track_gps = (ImageButton) view
					.findViewById(R.id.show_track_gps);
			ImageButton hide_track_gps = (ImageButton) view
					.findViewById(R.id.hide_track_gps);
			icon_gps.setBackgroundResource(SystemVarTools.getThumbID(userinfo
					.get("imageid") == null ? 0 : Integer.parseInt(userinfo
					.get("imageid"))));
			user_id_gps.setText(userinfo.get("id"));

			icon_gps.setTag(position);
			user_id_gps.setTag(position);
			show_track_gps.setTag(position);
			hide_track_gps.setTag(position);

			icon_gps.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					@SuppressWarnings("unchecked")
					final HashMap<String, String> userinfo = (HashMap<String, String>) getItem(pos);
					if (userinfo != null) {
						String lon = userinfo.get("lon");
						String lat = userinfo.get("lat");
						selectSite(lon, lat);
					}
				}
			});
			user_id_gps.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					@SuppressWarnings("unchecked")
					final HashMap<String, String> userinfo = (HashMap<String, String>) getItem(pos);
					if (userinfo != null) {
						String lon = userinfo.get("lon");
						String lat = userinfo.get("lat");
						selectSite(lon, lat);
					}
				}
			});
			show_track_gps.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					@SuppressWarnings("unchecked")
					final HashMap<String, String> userinfo = (HashMap<String, String>) getItem(pos);
					if (userinfo != null) {
						String id = userinfo.get("id");
						String lon = userinfo.get("lon");
						String lat = userinfo.get("lat");
						showTrackLine(id, lon, lat);
					}
				}
			});
			hide_track_gps.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					@SuppressWarnings("unchecked")
					final HashMap<String, String> userinfo = (HashMap<String, String>) getItem(pos);
					if (userinfo != null) {
						String id = userinfo.get("id");
						hideTrackLine(id);
					}
				}
			});

			return view;
		}

		/**
		 * selectSite(lon, lat)
		 * 
		 * @param lon
		 * @param lat
		 */
		private void selectSite(String lon, String lat) {
			mWebView.loadUrl("javascript:selectSite(" + lon + ", " + lat + ")");
		}

		/**
		 * showTrackLine(terId, lon, lat)
		 * 
		 * @param terId
		 * @param lon
		 * @param lat
		 */
		private void showTrackLine(String terId, String lon, String lat) {
			mWebView.loadUrl("javascript:showTrackLine(" + terId + ", " + lon
					+ ", " + lat + ")");
		}

		/**
		 * hideTrackLine(terId)
		 * 
		 * @param terId
		 */
		private void hideTrackLine(String terId) {
			mWebView.loadUrl("javascript:hideTrackLine(" + terId + ")");
		}

	}

	/**
	 * SeekSiteAdapter
	 */
	private class SeekSiteAdapter extends BaseAdapter {
		private List<HashMap<String, String>> mSeekSites;
		private final LayoutInflater mInflater;
		private final ScreenMap mBaseScreen;

		public SeekSiteAdapter(ScreenMap baseScreen) {
			Log.i(TAG, "SeekSiteAdapter - SeekSiteAdapter()");
			mBaseScreen = baseScreen;
			mInflater = LayoutInflater.from(mBaseScreen);
			mSeekSites = mSeekSiteList;
		}

		@Override
		public int getCount() {
			return mSeekSites.size();
		}

		@Override
		public Object getItem(int position) {
			return mSeekSites.get(position);
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
			Log.i(TAG, "SeekSiteAdapter - getView()");
			View view = convertView;
			@SuppressWarnings("unchecked")
			final HashMap<String, String> siteinfo = (HashMap<String, String>) getItem(position);
			if (siteinfo == null) {
				return null;
			}
			if (view == null) {
				view = mInflater.inflate(
						R.layout.screen_floating_window_seek_site_item, null);
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = (Integer) v.getTag();
						@SuppressWarnings("unchecked")
						final HashMap<String, String> siteinfo = (HashMap<String, String>) getItem(pos);
						if (siteinfo != null) {
							String lon = siteinfo.get("lon");
							String lat = siteinfo.get("lat");
							selectSite(lon, lat);
						}
					}
				});
			}

			view.setTag(position);

			TextView seek_site_item = (TextView) view
					.findViewById(R.id.seek_site_item);
			String id = siteinfo.get("id");
			String site = siteinfo.get("site");
			seek_site_item.setText(position + "." + id + ":" + site);

			return view;
		}

		/**
		 * selectSite(lon, lat)
		 * 
		 * @param lon
		 * @param lat
		 */
		private void selectSite(String lon, String lat) {
			mWebView.loadUrl("javascript:selectSite(" + lon + ", " + lat + ")");
		}

	}

	/**
	 * �����ն�ͨ��(����)��Ϣ�¼�
	 * 
	 * @param intent
	 */
	public static void handleSipEvent(Intent intent) {
		InviteState state;
		if (serviceAV_map.getAVSession() == null) {
			Log.e(TAG, "Invalid session object");
			return;
		}

		final String action = intent.getAction();
		Log.d(TAG, "Receive a call,handling...eventtype=" + action);
		if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
			NgnInviteEventArgs args = intent
					.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}
			if (args.getSessionId() != serviceAV_map.getAVSession().getId()) {
				Log.d(TAG, "Receive a call, handling...");
				return;
			}

			NgnInviteEventTypes eventType = args.getEventType(); // CONNECTED
																	// TERMWAIT
																	// INPROGRESS
																	// SIP_RESPONSE
																	// EARLY_MEDIA
																	// RINGING
																	// TERMINATED
																	// REMOTE_REFUSE
																	// MEDIA_UPDATED
			Log.d(TAG, "Receive a call,handling...eventtype=" + eventType);
			if (eventType.equals(NgnInviteEventTypes.ENCRYPT_INFO)) {
			} else if (eventType.equals(NgnInviteEventTypes.GROUP_PTT_INFO)) {
			} else if ((eventType
					.equals(NgnInviteEventTypes.GROUP_VIDEO_MONITORING))) {
			} else if ((eventType.equals(NgnInviteEventTypes.VIEDO_TRANSMINT))) {
			} else {
				switch ((state = serviceAV_map.getAVSession().getState())) { // TERMINATED
				case NONE:
				default:
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					loadTryingView(serviceAV_map.getAVSession());
					break;

				case EARLY_MEDIA:
				case INCALL:
					// stop using the speaker (also done in ServiceManager())
					Engine.getInstance().getSoundService().stopRingTone();
					serviceAV_map.getAVSession().setSpeakerphoneOn(true); // Ĭ�ϴ�������
					serviceAV_map.setOnResetJB();
					// 180SDP����ý��Э�̣�����ȡ�Ự���� gle20141222
					mIsVideoCall = (serviceAV_map.getAVSession().getMediaType() == NgnMediaType.AudioVideo || serviceAV_map
							.getAVSession().getMediaType() == NgnMediaType.Video);
					// Send blank packets to open NAT pinhole
					if (serviceAV_map.getAVSession() != null) {
						serviceAV_map.applyCamRotation(serviceAV_map
								.getAVSession().compensCamRotation(true));
					}

					switch (eventType) {
					case REMOTE_DEVICE_INFO_CHANGED:
						Log.d(TAG,
								String.format(
										"Remote device info changed: orientation: %s",
										serviceAV_map.getAVSession()
												.getRemoteDeviceInfo()
												.getOrientation()));
						break;
					case CONNECTED:
						if (serviceAV_map.getAVSession().isGroupAudioCall()) {
						} else if (serviceAV_map.getAVSession()
								.isGroupVideoCall()) {
						} else if (mIsVideoCall) {
							loadInCallVideoView();
						} else {
							loadInCallAudioView();
						}
						break;
					}
					break;

				case TERMINATING:
				case TERMINATED: // Call Terminated
					// if (isSuicide == false) {
					// mTimerSuicide.schedule(mTimerTaskSuicide, new Date(new
					// Date().getTime() + 1500));
					mTimerSuicide.schedule(new TimerTask() {
						@Override
						public void run() {
							runTaskSuicide();
						}
					}, new Date(new Date().getTime() + 1500));
					// isSuicide = true;
					// }

					loadTermView(SHOW_SIP_PHRASE ? args.getPhrase() : null);

					if (NgnApplication.isBh()) { // ����PAD���ֳ�̨
						final AudioManager audiomanager = NgnApplication
								.getAudioManager();
						audiomanager.setMode(AudioManager.MODE_NORMAL);
						Log.d(TAG,
								"audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION); - bh03/bh04");
					}

					break;
				}
			}
		}
	} // handleSipEvent()

	public static void handleMediaEvent(Intent intent) { // û������
		final String action = intent.getAction();

		if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(action)) {
			NgnMediaPluginEventArgs args = intent
					.getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}

			switch (args.getEventType()) {
			case STARTED_OK: // started or restarted (e.g. reINVITE)
			{
				mIsVideoCall = (serviceAV_map.getAVSession().getMediaType() == NgnMediaType.AudioVideo || serviceAV_map
						.getAVSession().getMediaType() == NgnMediaType.Video);
				// loadView();

				break;
			}
			case PREPARED_OK:
			case PREPARED_NOK:
			case STARTED_NOK:
			case STOPPED_OK:
			case STOPPED_NOK:
			case PAUSED_OK:
			case PAUSED_NOK: {
				break;
			}
			}
		}
	} // handleMediaEvent()

	/** ���ں���ʱ��View */
	public static void loadTryingView(NgnAVSession avSession) {
		Log.d(TAG, "loadTryingView start");
		// if (mCurrentView == ViewType.ViewTrying) {
		// return;
		// }

		if (serviceAV_map == null) {
			// serviceAV_map = ServiceAV.create(avSession, this);
			serviceAV_map = ServiceAV.create(avSession, new ScreenMap());
			// serviceAV_map = ServiceAV.create(avSession);
			serviceAV_map.registerReceiver();
			// NgnSipStack sipStack = serviceAV_map.getAVSession().getStack();
		}

		if (mViewTrying == null) {
			mViewTrying = mInflater
					.inflate(R.layout.view_call_trying_map, null);
		}

		mTvInfo = (TextView) mViewTrying
				.findViewById(R.id.view_call_trying_textView_info);

		final TextView tvRemote = (TextView) mViewTrying
				.findViewById(R.id.view_call_trying_textView_remote);
		final Button btHang = (Button) mViewTrying
				.findViewById(R.id.view_call_trying_imageButton_hang);

		btHang.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallLayout.removeAllViews();
				serviceAV_map.hangUpCall();
				Engine.getInstance().getSoundService().stopRingBackTone();
				Engine.getInstance().getSoundService().stopRingTone();

				if (serviceAV_map != null) {
					serviceAV_map.unRegisterReceiver();
					serviceAV_map.release();
					serviceAV_map = null;
				}
			}
		});

		InviteState state = serviceAV_map.getAVSession().getState(); // TERMINATED
		switch (state) {
		case INCOMING:
			Log.d(TAG, "serviceAV_map.getAVSession().getState() = INCOMING");
			mSessionType = serviceAV_map.getAVSession().getSessionType();
			switch (mSessionType) {
			case SessionType.AudioCall:
				mTvInfo.setText(context_map
						.getString(R.string.string_call_incoming_audio)); // �����������
				break;

			case SessionType.VideoCall:
				mTvInfo.setText(context_map
						.getString(R.string.string_call_incoming_video)); // ��Ƶ�������
				break;

			default:
				mTvInfo.setText(context_map
						.getString(R.string.string_call_incoming)); // �������
				break;
			}
			break;

		case INPROGRESS:
		case REMOTE_RINGING:
		case EARLY_MEDIA:
		default:
			Log.d(TAG,
					"serviceAV_map.getAVSession().getState() = default:outgoing");
			// mTvInfo.setText(getString(R.string.string_call_outgoing)); //���ں���
			mSessionType = serviceAV_map.getAVSession().getSessionType();
			switch (mSessionType) {
			case SessionType.AudioCall:
				mTvInfo.setText(context_map
						.getString(R.string.string_call_outgoing_audio)); // ������������
				break;

			case SessionType.VideoCall:
				mTvInfo.setText(context_map
						.getString(R.string.string_call_outgoing_video)); // ������Ƶ����
				break;

			default:
				mTvInfo.setText(context_map
						.getString(R.string.string_call_outgoing)); // ���ں���
				break;
			}
			break;
		}

		tvRemote.setText(mRemoteParty);

		setCallLayoutParams(50, 60, 3, 3);

		mCallLayout.removeAllViews();
		mCallLayout.addView(mViewTrying);
		mCallLayout.bringChildToFront(mViewTrying);
		// mCurrentView = ViewType.ViewTrying;
		Log.d(TAG, "loadTryingView ok");
		// bMapCall = false;

		// setCallButtonEnabled(false);
	}

	private static void loadInCallAudioView() {
		Log.d(TAG, "loadInCallAudioView()");

		// isSpeaker = false;
		// isSpeaker = true;

		if (mViewInCallAudio == null) {
			mViewInCallAudio = mInflater.inflate(R.layout.view_call_trying_map,
					null);
		}
		mTvInfo = (TextView) mViewInCallAudio
				.findViewById(R.id.view_call_trying_textView_info);

		final TextView tvRemote = (TextView) mViewInCallAudio
				.findViewById(R.id.view_call_trying_textView_remote);
		final Button btHang = (Button) mViewInCallAudio
				.findViewById(R.id.view_call_trying_imageButton_hang);

		btHang.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallLayout.removeAllViews();
				serviceAV_map.hangUpCall();
			}
		});

		Log.d("loadInCallAudioView()", "mRemoteParty = " + mRemoteParty);
		tvRemote.setText(mRemoteParty);

		Log.d("loadInCallAudioView()",
				"mTvInfo = " + context_map.getString(R.string.string_incall));
		mTvInfo.setText(context_map.getString(R.string.string_incall));

		setCallLayoutParams(50, 60, 3, 3);

		mCallLayout.removeAllViews();
		mCallLayout.addView(mViewInCallAudio);
		// mCurrentView = ViewType.ViewInCall;

		// setCallButtonEnabled(false);
	}

	private static void loadInCallVideoView() {
		Log.d(TAG, "loadInCallVideoView()");

		if (mViewInCallVideo == null) {
			mViewInCallVideo = mInflater.inflate(
					R.layout.view_call_incall_video_map, null);
			final TextView userRemoteparty = (TextView) mViewInCallVideo
					.findViewById(R.id.tv_user_remote_party);
			userRemoteparty.setText(mRemoteParty);
			mViewLocalVideoPreview = (FrameLayout) mViewInCallVideo
					.findViewById(R.id.view_call_incall_video_FrameLayout_local_video);
			mViewRemoteVideoPreview = (FrameLayout) mViewInCallVideo
					.findViewById(R.id.view_call_incall_video_FrameLayout_remote_video);
			mVideoHangUpBt = (Button) mViewInCallVideo
					.findViewById(R.id.video_hangup);
			mVideoHangUpBt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCallLayout.removeAllViews();
					serviceAV_map.hangUpCall();
				}
			});
		}

		setCallLayoutParams(50, 60, 3, 3);

		// mTvInfo = null;
		mCallLayout.removeAllViews();
		mCallLayout.addView(mViewInCallVideo);

		switch (serviceAV_map.getAVSession().getSessionType()) {
		case SessionType.VideoMonitor:
		case SessionType.GroupVideoMonitor:
		case SessionType.VideoTransmit:
		case SessionType.VideoUaMonitor:
			break;
		default: // ��Ƶͨ��
			// Video Consumer
			loadVideoPreview();
			// // Video Producer
			startVideo(true, true);
			break;
		}

		// mCurrentView = ViewType.ViewInCall;

		// setCallButtonEnabled(false);
	}

	private static void loadVideoPreview() {
		Log.d(TAG, "loadVideoPreview()");
		mViewRemoteVideoPreview.removeAllViews();
		final View remotePreview = serviceAV_map.getAVSession()
				.startVideoConsumerPreview();
		if (remotePreview != null) {
			final ViewParent viewParent = remotePreview.getParent();
			if (viewParent != null && viewParent instanceof ViewGroup) {
				((ViewGroup) (viewParent)).removeView(remotePreview);
			}
			mViewRemoteVideoPreview.addView(remotePreview);
		}
	}

	private static void startVideo(boolean bStart, boolean bZOrderTop) {
		Log.d(TAG, "startVideo()");
		if (!mIsVideoCall) {
			return;
		}

		serviceAV_map.getAVSession().setSendingVideo(bStart);

		if (mViewLocalVideoPreview != null) {
			if (bStart) {
				mViewLocalVideoPreview.removeAllViews();
				final View localPreview = serviceAV_map.getAVSession()
						.startVideoProducerPreview();
				if (localPreview != null) {
					final ViewParent viewParent = localPreview.getParent();
					if (viewParent != null && viewParent instanceof ViewGroup) {
						((ViewGroup) viewParent).removeView(localPreview);
					}
					if (bZOrderTop == true) {
						if (localPreview instanceof SurfaceView) {
							((SurfaceView) localPreview).setZOrderOnTop(true);
						}
					}
					mViewLocalVideoPreview.addView(localPreview);
					mViewLocalVideoPreview.bringChildToFront(localPreview);
				}
			}
			mViewLocalVideoPreview.setVisibility(View.VISIBLE);
			mViewLocalVideoPreview.bringToFront();
		}
	}

	private static void loadTermView(String phrase) {
		Log.d(TAG, "loadTermView()");

		if (mViewTermwait == null) {
			mViewTermwait = mInflater.inflate(R.layout.view_call_trying_map,
					null);
		}

		Log.d(TAG, "phrase = " + phrase); // Call Terminated / Call Cancelled /
											// Busy Here / Dialog connecting
		mTvInfo = (TextView) mViewTermwait
				.findViewById(R.id.view_call_trying_textView_info);
		mTvInfo.setText(context_map.getString(R.string.string_call_terminated)); // ͨ������ֹ
		if (!NgnStringUtils.isNullOrEmpty(phrase)) {
			if (phrase.equals("Call Terminated")
					|| phrase.equals("Terminating dialog")) {
				// mTvInfo.setText(getString(R.string.string_call_terminated));
				// //ͨ������ֹ

				switch (serviceAV_map.getAVSession().getSessionType()) {
				case SessionType.VideoUaMonitor:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_unmonitor_terminated)); // ������Ƶ�ش�����ֹ
					break;
				case SessionType.VideoMonitor:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_monitor_video_terminated)); // ��Ƶ�������ֹ
					break;
				case SessionType.GroupVideoCall:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_group_video_terminated)); // ��Ƶ�������ֹ
					break;
				case SessionType.GroupVideoMonitor:
					mTvInfo.setText(context_map
							.getString(R.string.string_monitor_terminated)); // �������ֹ
					break;
				case SessionType.VideoTransmit:
					mTvInfo.setText(context_map
							.getString(R.string.string_transmit_terminated)); // ת������ֹ
					break;
				default:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_terminated)); // ͨ������ֹ
					break;
				}
			} else if (phrase.equals("Call Cancelled")) {
				// mTvInfo.setText(getString(R.string.string_call_cancelled));
				// //ͨ����ȡ��

				switch (serviceAV_map.getAVSession().getSessionType()) {
				case SessionType.VideoUaMonitor:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_unmonitor_cancelled)); // ������Ƶ�ش���ȡ��
					break;
				case SessionType.VideoMonitor:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_monitor_video_cancelled)); // ��Ƶ�����ȡ��
					break;
				case SessionType.GroupVideoCall:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_group_video_cancelled)); // ��Ƶ�����ȡ��
					break;
				case SessionType.GroupVideoMonitor:
					mTvInfo.setText(context_map
							.getString(R.string.string_monitor_cancelled)); // �����ȡ��
					break;
				case SessionType.VideoTransmit:
					mTvInfo.setText(context_map
							.getString(R.string.string_transmit_cancelled)); // ת����ȡ��
					break;
				default:
					mTvInfo.setText(context_map
							.getString(R.string.string_call_cancelled)); // ͨ����ȡ��
					break;
				}
			} else if (phrase.equals("Busy Here")) {
				mTvInfo.setText(context_map
						.getString(R.string.string_busy_here)); // �Է�ͨ����
			}
		}

		// loadTermView() could be called twice (onTermwait() and OnTerminated)
		// and this is why we need to
		// update the info text for both
		// if (mCurrentView == ViewType.ViewTermwait) {
		// return;
		// }

		final TextView tvRemote = (TextView) mViewTermwait
				.findViewById(R.id.view_call_trying_textView_remote);
		mViewTermwait.findViewById(R.id.view_call_trying_imageButton_hang)
				.setVisibility(View.GONE);
		mViewTermwait.setBackgroundResource(R.drawable.grad_bkg_termwait);

		final RelativeLayout screen_top = (RelativeLayout) mViewTermwait
				.findViewById(R.id.screen_top);
		screen_top.setBackgroundResource(R.color.color_main1);

		tvRemote.setText(mRemoteParty);

		setCallLayoutParams(50, 60, 3, 3);

		mCallLayout.removeAllViews();
		mCallLayout.addView(mViewTermwait);
		// mCurrentView = ViewType.ViewTermwait;

		// SystemVarTools.sleep(1000);

		// mCallLayout.removeAllViews();

		bMapCall = false;

		if (serviceAV_map != null) {
			serviceAV_map.unRegisterReceiver();
			serviceAV_map.release();
			serviceAV_map = null;
		}

		NgnProxyPluginMgr.setDefaultMaxVideoSize();

		// setCallButtonEnabled(true);
	}

	// private static boolean isSuicide = false;

	private static NgnTimer mTimerSuicide;

	// private final static TimerTask mTimerTaskSuicide = new TimerTask() {
	// @Override
	// public void run() {
	// runTaskSuicide();
	// }
	// };

	/**
	 * ��ʱ�˳���ֹ����
	 */
	private static void runTaskSuicide() {
		if (mMapHandler != null) { // �����ڵ�ͼ������ʱ�˳���ֹ�������Ϣ
			Log.d(TAG, "runTaskSuicide() - mMapHandler != null");
			Message msg = Message.obtain(mMapHandler,
					MessageTypes.MSG_MAP_SUICIDE);
			mMapHandler.sendMessage(msg);
		}
	}

	private static void startChat_map(String remoteParty) {
		final Engine engine = (Engine) NgnEngine.getInstance();
		if (!NgnStringUtils.isNullOrEmpty(remoteParty)
				&& remoteParty.startsWith("sip:")) {
			remoteParty = NgnUriUtils.getUserName(remoteParty);
		}

		if (NgnStringUtils.isNullOrEmpty((mRemoteParty = remoteParty))) {
			Log.e(TAG, "Null Uri");
			return;
		}

		if (mViewChat == null) {
			mViewChat = mInflater.inflate(R.layout.screen_chat_map, null);

			mTvContentCount = (TextView) mViewChat
					.findViewById(R.id.screen_chat_tv_count);
			mTvContentCount.setText("0/256");

			mEtCompose = (EditText) mViewChat
					.findViewById(R.id.screen_chat_editText_compose);
			mTvName = (TextView) mViewChat
					.findViewById(R.id.screen_chat_textview_name);
			mBtSend = (Button) mViewChat
					.findViewById(R.id.screen_chat_button_send);
			// mBtadd_filetransfer_imagebutton = (ImageButton)
			// mViewChat.findViewById(R.id.add_filetransfer_imagebutton);
			// mBtFiletransfer = (Button)
			// mViewChat.findViewById(R.id.screen_chat_button_filetransfer_button);
			// mViewFiletransfer_view = (View)
			// mViewChat.findViewById(R.id.screen_chat_linearLayout_bottom_filetransfer_view);
			// mLinearLayoutFiletransfer_ll = (LinearLayout)
			// mViewChat.findViewById(R.id.screen_chat_linearLayout_bottom_filetransfer_ll);
			mLvHistoy = (ListView) mViewChat
					.findViewById(R.id.screen_chat_listView);

			mBtBack = (ImageView) mViewChat.findViewById(id.back);

			mAdapter = new ScreenChatMapAdapter(context_map);
			mLvHistoy.setAdapter(mAdapter);
			mLvHistoy.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			mLvHistoy.setStackFromBottom(true);

			userinfo = SystemVarTools
					.createContactFromRemoteParty(mRemoteParty);
			mTvName.setText(userinfo.name + "\n" + userinfo.mobileNo);

			mBtBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCallLayout.removeAllViews();
				}
			});

			mBtSend.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mSipService.isRegisteSessionConnected()
							&& !NgnStringUtils.isNullOrEmpty(mEtCompose
									.getText().toString())) {
						sendMessage();
					}
				}
			});

			// mBtadd_filetransfer_imagebutton.setOnClickListener(new
			// View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// mViewFiletransfer_view.setVisibility(View.VISIBLE);
			// mLinearLayoutFiletransfer_ll.setVisibility(View.VISIBLE);
			// }
			// });

			mEtCompose.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					mBtSend.setEnabled(!NgnStringUtils.isNullOrEmpty(mEtCompose
							.getText().toString()));

					if (!NgnStringUtils.isNullOrEmpty(mEtCompose.getText()
							.toString())) {
						// mBtSend.setVisibility(View.VISIBLE);
						// mBtadd_filetransfer_imagebutton.setVisibility(View.GONE);
						// mViewFiletransfer_view.setVisibility(View.GONE);
						// mLinearLayoutFiletransfer_ll.setVisibility(View.GONE);

						int length = mEtCompose.getText().toString().length();
						// �����������ݵı仯�ı�ʣ��������ʾ
						mTvContentCount.setText(length + "/" + 256);
					}
					// else {
					// mBtSend.setVisibility(View.GONE);
					// mBtadd_filetransfer_imagebutton.setVisibility(View.VISIBLE);
					// }
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			// BugFix: http://code.google.com/p/android/issues/detail?id=7189
			mEtCompose.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
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
		}

		setCallLayoutParams(50, 60, 2, 2);

		mCallLayout.removeAllViews();
		mCallLayout.addView(mViewChat);
		mCallLayout.bringChildToFront(mViewChat);

		// setCallButtonEnabled(false);
	}

	/**
	 * ���ú��д��ڲ���
	 */
	private static void setCallLayoutParams(float x, float y, int wp, int hp) {
		mCallLayout.setX(x);
		mCallLayout.setY(y);
		// android����ֻ���Ļ�Ŀ�Ⱥ͸߶�
		WindowManager windowManager = (WindowManager) context_map
				.getSystemService(context_map.WINDOW_SERVICE);
		int width = windowManager.getDefaultDisplay().getWidth();
		int height = windowManager.getDefaultDisplay().getHeight();
		// �������촰�ڳ�������
		RelativeLayout.LayoutParams lpParams = new RelativeLayout.LayoutParams(
				width / wp, height / hp);
		mCallLayout.setLayoutParams(lpParams);
	}

	/**
	 * ���ú��а�ť����Ч��
	 */
	private static void setCallButtonEnabled(boolean b) {
		mLlCallLayout.setEnabled(b);
		mBtnAudioCall.setEnabled(b);
		mBtnVideoCall.setEnabled(b);
		mBtnSendMessage.setEnabled(b);
		mBtnEndReport.setEnabled(b);
	}

	private static boolean sendMessage() {
		boolean ret = false;
		final String content = mEtCompose.getText().toString();
		final NgnHistorySMSEvent e = new NgnHistorySMSEvent(mRemoteParty,
				StatusType.Outgoing, "");

		long time = findMaxHistoryMsgTime();
		e.setEndTime(NgnDateTimeUtils.parseDate(NgnDateTimeUtils.now())
				.getTime());
		e.setStartTime(time);

		e.setContent(content);

		if (GlobalVar.bADHocMode) {
			String uri = SystemVarTools
					.createContactFromRemoteParty(mRemoteParty).uri;
			Log.e(TAG,
					"audio set cscf host:" + SystemVarTools.getIPFromUri(uri));
			((Engine) Engine.getInstance()).getSipService().ADHOC_SetPcscfHost(
					SystemVarTools.getIPFromUri(uri));
		}
		if (!mSipService.isRegisteSessionConnected()) {
			Log.e(TAG, "Not registered");
			return false;
		}
		final String remotePartyUri = NgnUriUtils.makeValidSipUri(mRemoteParty);
		final NgnMessagingSession imSession = NgnMessagingSession
				.createOutgoingSession(mSipService.getSipStack(),
						remotePartyUri);

		String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
		String mes = ctreateExpandedField(localMsgID) + "\n\n" + content;
		e.setLocalMsgID(localMsgID); // ������Ϣid
										// //UEdccc139c-d17c-41c5-9c83-bb3565e8706c
		if (!(ret = imSession.sendExTextMessage(mes))) {
			e.setStatus(StatusType.Failed);
		}
		NgnMessagingSession.releaseSession(imSession);

		mHistorytService.addEvent(e);
		mEtCompose.setText(NgnStringUtils.emptyValue());

		mTvContentCount.setText("0/256");

		return ret;
	}

	private static long findMaxHistoryMsgTime() {
		// ������ǰ�Ի���ʹ��Ҫ���͵���Ϣ�� //ʱ�����������Ϣ��ʱ�����
		long time = 0;
		List<NgnHistoryEvent> mEvents = mHistorytService.getObservableEvents()
				.filter(new HistoryEventChatMapFilter());
		try {
			if (mEvents != null && mEvents.size() > 0) {
				for (NgnHistoryEvent e1 : mEvents) {
					if (e1.getStartTime() > time) {
						time = e1.getStartTime();
					}
				}
			}
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
		return ++time;
	}

	public static class HistoryEventChatMapFilter implements
			NgnPredicate<NgnHistoryEvent> {
		@Override
		public boolean apply(NgnHistoryEvent event) {
			if (event != null && (event.getMediaType() == NgnMediaType.SMS)) {
				return NgnStringUtils.equals(mRemoteParty,
						event.getRemoteParty(), false);
			}
			return false;
		}
	}

	private static String ctreateExpandedField(String localMsgID) {
		String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
		String msgType = "IM";
		if (userinfo.isgroup) {
			msgType = "GM" + userinfo.mobileNo;
		}
		String msgReport = "Yes";
		String contentType = "text/plain";
		MessageBodyInfo msgBodyInfo = new MessageBodyInfo(nameSpace, msgType,
				msgReport, localMsgID, contentType);
		String strBody = msgBodyInfo.toString();
		return strBody;
	}

}
