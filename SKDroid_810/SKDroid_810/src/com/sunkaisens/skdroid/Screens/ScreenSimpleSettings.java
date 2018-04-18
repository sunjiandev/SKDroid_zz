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

import com.sunkaisens.skdroid.R;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

public class ScreenSimpleSettings  extends BaseScreen {
	private final static String TAG = ScreenSimpleSettings.class.getCanonicalName();
	private final INgnConfigurationService mConfigurationService;
	
	private Spinner mSpVsize;
	private CheckBox mCbNR;
	private CheckBox mCbAutoStart;
	private CheckBox mCbFullScreenVideo;
	private CheckBox mCbFFC;
	private CheckBox mCbEnableIce;
	private CheckBox mCbHackAoR;
	
	private EditText mEtVideoFps;
	
	
	private final static ScreenQoSVsize[] sSpinnerVsizeItems = new ScreenQoSVsize[] {
//		new ScreenQoSVsize("SQCIF (128 x 98)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_sqcif),
		new ScreenQoSVsize("QCIF (176 x 144)",
				tmedia_pref_video_size_t.tmedia_pref_video_size_qcif),
//		new ScreenQoSVsize("QVGA (320 x 240)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_qvga),
		new ScreenQoSVsize("Á÷³©",
				tmedia_pref_video_size_t.tmedia_pref_video_size_cif),
//		new ScreenQoSVsize("HVGA (480 x 320)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_hvga),
		new ScreenQoSVsize("±êÇå",
				tmedia_pref_video_size_t.tmedia_pref_video_size_vga),
//		new ScreenQoSVsize("4CIF (704 x 576)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_4cif),
//		new ScreenQoSVsize("SVGA (800 x 600)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_svga),
//		new ScreenQoSVsize("480P (852 x 480)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_480p),
		new ScreenQoSVsize("720P (1280 x 720)",
				tmedia_pref_video_size_t.tmedia_pref_video_size_720p),
//		new ScreenQoSVsize("16CIF (1408 x 1152)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_16cif),
//		new ScreenQoSVsize("1080P (1920 x 1080)",
//				tmedia_pref_video_size_t.tmedia_pref_video_size_1080p)
		};
	
	public ScreenSimpleSettings() {
		super(SCREEN_TYPE.IDENTITY_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
	}

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_simple_settings);

		ImageView back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});
		
		mSpVsize = (Spinner) findViewById(R.id.screen_qos_Spinner_vsize);	
		mCbNR = (CheckBox) this.findViewById(R.id.screen_general_checkBox_NR);
		mCbAutoStart = (CheckBox) findViewById(R.id.screen_general_checkBox_autoStart);
		mCbFullScreenVideo = (CheckBox) findViewById(R.id.screen_general_checkBox_fullscreen);
		mCbFFC = (CheckBox) findViewById(R.id.screen_general_checkBox_ffc);
		mCbEnableIce = (CheckBox) findViewById(R.id.screen_natt_checkBox_ice);
		mCbHackAoR = (CheckBox) findViewById(R.id.screen_natt_checkBox_hack_aor);
		
		mEtVideoFps = (EditText)findViewById(R.id.screen_qos_editText_videofps);
		
		
		ArrayAdapter<ScreenQoSVsize> adapterVsize = new ArrayAdapter<ScreenQoSVsize>(
				this, android.R.layout.simple_spinner_item,
				ScreenSimpleSettings.sSpinnerVsizeItems);
		adapterVsize
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpVsize.setAdapter(adapterVsize);
		mSpVsize.setSelection(ScreenQoSVsize.getSpinnerIndex(tmedia_pref_video_size_t
				.valueOf(mConfigurationService.getString(
						NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
						NgnConfigurationEntry.DEFAULT_QOS_PREF_VIDEO_SIZE))));
		
		mCbNR.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_NR,
				NgnConfigurationEntry.DEFAULT_GENERAL_NR));
		
		mCbAutoStart.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_AUTOSTART,
				NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART));
		
		mCbFullScreenVideo.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO,
				NgnConfigurationEntry.DEFAULT_GENERAL_FULL_SCREEN_VIDEO));
		
		mCbFFC.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_USE_FFC,
				NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC));
		
		mCbEnableIce.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NATT_USE_ICE,
				NgnConfigurationEntry.DEFAULT_NATT_USE_ICE));
		
		mCbHackAoR.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NATT_HACK_AOR,
				NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR));
		
		mEtVideoFps.setText(Integer.toString(mConfigurationService.getInt(NgnConfigurationEntry.GENERAL_VIDDO_FPS, 
				NgnConfigurationEntry.DEFAULT_GENERAL_VIDDO_FPS)));
		
		addConfigurationListener(mSpVsize);
		addConfigurationListener(mCbNR);
		addConfigurationListener(mCbAutoStart);
		addConfigurationListener(mCbFullScreenVideo);
		addConfigurationListener(mCbFFC);
		addConfigurationListener(mCbEnableIce);
		addConfigurationListener(mCbHackAoR);
		
		addConfigurationListener(mEtVideoFps);
        
	}	

	protected void onPause() {
		
		mConfigurationService
			.putString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
				sSpinnerVsizeItems[mSpVsize.getSelectedItemPosition()].mValue.toString());
		
		mConfigurationService.putBoolean(
				NgnConfigurationEntry.GENERAL_NR,
				mCbNR.isChecked());
		
		MediaSessionMgr.defaultsSetNoiseSuppEnabled(mCbNR.isChecked());
		
		mConfigurationService.putBoolean(
				NgnConfigurationEntry.GENERAL_AUTOSTART,
				mCbAutoStart.isChecked());
		
		mConfigurationService.putBoolean(
				NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO,
				mCbFullScreenVideo.isChecked());
		
		mConfigurationService.putBoolean(
				NgnConfigurationEntry.GENERAL_USE_FFC, 
				mCbFFC.isChecked());
		
		mConfigurationService.putBoolean(
				NgnConfigurationEntry.NATT_USE_ICE,
				mCbEnableIce.isChecked());
		mConfigurationService
		.putBoolean(NgnConfigurationEntry.NATT_HACK_AOR,
				mCbHackAoR.isChecked());
		
		mConfigurationService.putInt(NgnConfigurationEntry.GENERAL_VIDDO_FPS, 
				NgnStringUtils.parseInt(mEtVideoFps.getText().toString().trim(),
						NgnConfigurationEntry.DEFAULT_GENERAL_VIDDO_FPS));
		
		int fps = mConfigurationService.getInt(NgnConfigurationEntry.GENERAL_VIDDO_FPS,
				NgnConfigurationEntry.DEFAULT_GENERAL_VIDDO_FPS);
		Log.d(TAG,"set FPS="+mEtVideoFps.getText().toString()+"---"+fps);
		
				
		if (!mConfigurationService.commit()) {
			Log.e(TAG, "Failed to commit() configuration");
		} else {
			MediaSessionMgr.defaultsSetIceEnabled(mCbEnableIce.isChecked());
		}

		super.mComputeConfiguration = false;
		
		super.onPause();
	}
	
	private static class ScreenQoSVsize {
		private final String mDescription;
		private final tmedia_pref_video_size_t mValue;

		private ScreenQoSVsize(String description,
				tmedia_pref_video_size_t value) {
			mValue = value;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}

		@Override
		public boolean equals(Object o) {
			return mValue.equals(((ScreenQoSVsize) o).mValue);
		}

		static int getSpinnerIndex(tmedia_pref_video_size_t value) {
			for (int i = 0; i < sSpinnerVsizeItems.length; i++) {
				if (value == sSpinnerVsizeItems[i].mValue) {
					return i;
				}
			}
			return 0;
		}
	}
	
}
