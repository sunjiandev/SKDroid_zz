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

	private final INgnConfigurationService mConfigurationService; // ������Ϣ��Preferences���ķ������

	private EditText mETSendGPSToHost;
	private EditText mETSendGPSToPort;

	public ScreenGPS() {
		super(SCREEN_TYPE.GPSScreen_T, TAG);
		this.mConfigurationService = getEngine().getConfigurationService(); // ��ȡ������Ϣ��Preferences���ķ������
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_gps); // ��ʼ�����棺���֡��ؼ�

		mETSendGPSToHost = (EditText) findViewById(R.id.screen_gps_edittext_SendGPSToHost);
		mETSendGPSToPort = (EditText) findViewById(R.id.screen_gps_edittext_SendGPSToPort);
		// ��preferences����ȡ�ؼ�Ĭ��ֵ
		mETSendGPSToHost.setText(mConfigurationService.getString(
				NgnConfigurationEntry.GPS_SENDTO_HOST,
				NgnConfigurationEntry.DEFAULT_GPS_SENDTO_HOST));
		mETSendGPSToPort.setText(Integer.toString(mConfigurationService.getInt(
				NgnConfigurationEntry.GPS_SENDTO_PORT,
				NgnConfigurationEntry.DEFAULT_GPS_SENDTO_PORT)));

		super.addConfigurationListener(mETSendGPSToHost); // Ϊ�ؼ����״̬���ĵļ�������ʹ���ı�����preferences��
		super.addConfigurationListener(mETSendGPSToPort);
	}

	protected void onPause() // Called as part of the activity lifecycle when an
								// activity is going into the background, but
								// has not (yet) been killed. The counterpart to
								// onResume.
	{
		if (super.mComputeConfiguration) // �ؼ�״̬�����ı�
		{ // �����������пؼ�״̬���ύ��SharedPreferences.Editor
			mConfigurationService.putString(
					NgnConfigurationEntry.GPS_SENDTO_HOST, mETSendGPSToHost
							.getText().toString().trim());
			mConfigurationService.putInt(NgnConfigurationEntry.GPS_SENDTO_PORT,
					NgnStringUtils.parseInt(mETSendGPSToPort.getText()
							.toString().trim(),
							NgnConfigurationEntry.DEFAULT_GPS_SENDTO_PORT));

			if (!mConfigurationService.commit()) // SharedPreferences.Editorд��Preferences��
			{
				Log.e(TAG, "Failed to commit() configuration;GPS_Screen");
			}

			super.mComputeConfiguration = false; // �Ƿ��޸ı�־�ÿ�
		}

		super.onPause();
	}
}
