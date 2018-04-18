package com.sunkaisens.skdroid.Screens;

import java.net.URLEncoder;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryPushEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnContactService;
import org.doubango.ngn.services.INgnHistoryService;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.R.id;
import com.sunkaisens.skdroid.Screens.ScreenPushInfo.ScreenPushinfoAdapter;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class ScreenPushInfoLink extends BaseScreen {
	private static String TAG = ScreenPushInfoLink.class.getCanonicalName();

	private final INgnHistoryService mHistoryService;
	private final INgnContactService mContactService;
	private static INgnConfigurationService mConfigurationService;

	private static String mLink = null;
	public static String mTitle = null;

	WebView mWebView;
	final String mimeType = "text/html";
	final String encoding = "utf-8";

	public ScreenPushInfoLink() {
		super(SCREEN_TYPE.TAB_MESSAGES_T, TAG);
		mConfigurationService = getEngine().getConfigurationService();
		mHistoryService = (INgnHistoryService) getEngine().getHistoryService();
		mContactService = (INgnContactService) getEngine().getContactService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_pushinfo_link);

		TextView mTvName = (TextView) findViewById(R.id.screen_pushinfo_link_textview_name);

		ImageView mBtBack = (ImageView) findViewById(id.screen_pushinfo_link_linearLayout_top_back);

		mTvName.setText(mTitle);
		mTvName.setSelected(true);
		mBtBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
			}
		});

		Log.d(TAG, "onCreate()");
	}

	// public void setIntent(Intent intent) {
	// mLink = intent.getStringExtra("link");
	//
	// Log.i(TAG, "mLink = " + mLink);
	// }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mWebView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onAttachedToWindow() {
		mWebView = (WebView) findViewById(R.id.pushinfo_wvLink);

		WebSettings ws = mWebView.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setAllowFileAccess(true);
		ws.setBuiltInZoomControls(true);
		ws.setSupportZoom(true);

		ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		ws.setDefaultTextEncodingName("utf-8");
		ws.setAppCacheEnabled(true);
		ws.setCacheMode(WebSettings.LOAD_DEFAULT);

		mWebView.requestFocus();

		ws.setUseWideViewPort(true);
		ws.setLoadWithOverviewMode(true);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int mDensity = metrics.densityDpi;

		if (mDensity == 240) {
			ws.setDefaultZoom(ZoomDensity.FAR);
		} else if (mDensity == 160) {
			ws.setDefaultZoom(ZoomDensity.MEDIUM);
		} else if (mDensity == 120) {
			ws.setDefaultZoom(ZoomDensity.CLOSE);
		} else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
			ws.setDefaultZoom(ZoomDensity.FAR);
		} else if (mDensity == DisplayMetrics.DENSITY_TV) {
			ws.setDefaultZoom(ZoomDensity.FAR);
		} else {
			ws.setDefaultZoom(ZoomDensity.MEDIUM);
		}

		ws.setTextSize(WebSettings.TextSize.LARGEST);

		webHtml();
		//
		// webImage();
		//
		// localHtmlZh();
		//
		// localHtmlBlankSpace();
		//
		// localHtml();
		//
		// localImage();
		//
		// localHtmlImage();
	}

	private void webHtml() {
		try {
			mWebView.loadUrl(mLink); // "http://www.google.com"
										// http://192.168.1.222:8080/ipp/viewWholeInfo.action?id=22
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void webImage() {
		try {
			mWebView.loadUrl("http://www.gstatic.com/codesite/ph/images/code_small.png");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void localHtmlZh() {
		try {
			String data = "localHtmlZh";
			mWebView.loadData(URLEncoder.encode(data, encoding), mimeType,
					encoding);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void localHtmlBlankSpace() {
		try {
			String data = "XXXX";
			mWebView.loadData(URLEncoder.encode(data, encoding), mimeType,
					encoding);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void localImage() {
		try {
			mWebView.loadUrl("file:///android_asset/icon.png");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 閺勫墽銇氶張顒�勾缂冩垿銆夐弬鍥︽
	 */
	private void localHtml() {
		try {
			mWebView.loadUrl("file:///android_asset/test.html");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void localHtmlImage() {
		try {
			String data = "XXX";
			mWebView.loadDataWithBaseURL("about:blank", data, mimeType,
					encoding, "");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void startPushInfoLink(final String link) {
		final Engine engine = (Engine) NgnEngine.getInstance();

		mLink = link;

		engine.getScreenService().show(ScreenPushInfoLink.class);
	}
}