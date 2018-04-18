//Created By rockman 2012/7/24
package com.sunkaisens.skdroid.Screens;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.sunkaisens.skdroid.R;

public class ScreenGPS extends BaseScreen {
	private final static String TAG = ScreenGPS.class.getCanonicalName();

	private final INgnConfigurationService mConfigurationService; // 配置信息（Preferences）的服务对象

	private EditText mETSendGPSToHost;
	private EditText mETSendGPSToPort;

	public ScreenGPS() {
		super(SCREEN_TYPE.GPSScreen_T, TAG);
		this.mConfigurationService = getEngine().getConfigurationService(); // 获取配置信息（Preferences）的服务对象
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_gps); // 初始化界面：布局、控件

		mETSendGPSToHost = (EditText) findViewById(R.id.screen_gps_edittext_SendGPSToHost);
		mETSendGPSToPort = (EditText) findViewById(R.id.screen_gps_edittext_SendGPSToPort);
		// 从preferences中提取控件默认值
		mETSendGPSToHost.setText(mConfigurationService.getString(
				NgnConfigurationEntry.GPS_SENDTO_HOST,
				NgnConfigurationEntry.DEFAULT_GPS_SENDTO_HOST));
		mETSendGPSToPort.setText(Integer.toString(mConfigurationService.getInt(
				NgnConfigurationEntry.GPS_SENDTO_PORT,
				NgnConfigurationEntry.DEFAULT_GPS_SENDTO_PORT)));

		super.addConfigurationListener(mETSendGPSToHost); // 为控件添加状态更改的监听器，使更改保存至preferences。
		super.addConfigurationListener(mETSendGPSToPort);
	}

	protected void onPause() // Called as part of the activity lifecycle when an
								// activity is going into the background, but
								// has not (yet) been killed. The counterpart to
								// onResume.
	{
		if (super.mComputeConfiguration) // 控件状态有所改变
		{ // 将本界面所有控件状态都提交到SharedPreferences.Editor
			mConfigurationService.putString(
					NgnConfigurationEntry.GPS_SENDTO_HOST, mETSendGPSToHost
							.getText().toString().trim());
			mConfigurationService.putInt(NgnConfigurationEntry.GPS_SENDTO_PORT,
					NgnStringUtils.parseInt(mETSendGPSToPort.getText()
							.toString().trim(),
							NgnConfigurationEntry.DEFAULT_GPS_SENDTO_PORT));

			if (!mConfigurationService.commit()) // SharedPreferences.Editor写入Preferences中
			{
				Log.e(TAG, "Failed to commit() configuration;GPS_Screen");
			}

			super.mComputeConfiguration = false; // 是否修改标志置空
		}

		super.onPause();
	}
}
