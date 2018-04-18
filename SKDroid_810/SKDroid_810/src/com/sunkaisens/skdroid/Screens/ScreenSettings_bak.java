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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenSettings_bak extends BaseScreen {
	private static String TAG = ScreenSettings_bak.class.getCanonicalName();

	private GridView mGridView;

	public ScreenSettings_bak() {
		super(SCREEN_TYPE.SETTINGS_T, TAG);
	}

	@Override 
	protected void onResume()
	{
		super.onResume();
		Log.e("ScreenSettings","ScreenSetting OnResume");
		//((ScreenSettingsAdapter)mGridView.getAdapter()).notifyDataSetChanged();
		mGridView = (GridView) findViewById(R.id.screen_settings_gridview);
		setContentView(R.layout.screen_settings_bak);

		mGridView = (GridView) findViewById(R.id.screen_settings_gridview);
		mGridView.setAdapter(new ScreenSettingsAdapter(this));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final ScreenSettingsItem item = (ScreenSettingsItem) parent
						.getItemAtPosition(position);
				if (item != null) {
					mScreenService.show(item.mClass);
				}
			}
		});
	}

	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_settings_bak);

		ImageView back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		mGridView = (GridView) findViewById(R.id.screen_settings_gridview);
		mGridView.setAdapter(new ScreenSettingsAdapter(this));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final ScreenSettingsItem item = (ScreenSettingsItem) parent
						.getItemAtPosition(position);
				if (item != null) {
					mScreenService.show(item.mClass);
				}
			}
		});
	}

	/**
	 * ScreenSettingsItem
	 */
 class ScreenSettingsItem {
		final int mIconResId;
		final String mText;
		final Class<? extends Activity> mClass;

		private ScreenSettingsItem(int iconResId, String text,
				Class<? extends Activity> _class) {
			mIconResId = iconResId;
			mText = text;
			mClass = _class;
		}
	}

	/**
	 * ScreenSettingsAdapter
	 */
	class ScreenSettingsAdapter extends BaseAdapter {
		
		final private ScreenSettingsItem[] sItems = new ScreenSettingsItem[] { // “选项设置”Screen能启动的各个子屏Screen的信息
				new ScreenSettingsItem(R.drawable.general_48, "基本设置",
						ScreenGeneral.class), // 启动子屏的按钮的图片、说明文字、子屏的类名称
				// new ScreenSettingsItem(R.drawable.identity_48, "认证信息",
				// ScreenIdentity.class ),
				new ScreenSettingsItem(R.drawable.network_48, "网络设置",
						ScreenNetwork.class),
//				new ScreenSettingsItem(R.drawable.network_48, "网络",
//						ScreenNetwork.class),
				new ScreenSettingsItem(R.drawable.lock_48, "安全",
						ScreenSecurity.class),
//				new ScreenSettingsItem(R.drawable.eab_48, "通讯录",
//						ScreenContacts.class),
//				new ScreenSettingsItem(R.drawable.im_invisible_user_48,
//						"呈现服务（Presence）", ScreenPresence.class),
				new ScreenSettingsItem(R.drawable.codecs_48, "编解码",
						ScreenCodecs.class),
//				new ScreenSettingsItem(R.drawable.messaging_48, "短消息",
//						ScreenMessaging.class),
				new ScreenSettingsItem(R.drawable.qos_qoe_48, "通信质量（QOS）",
						ScreenQoS.class),
				new ScreenSettingsItem(R.drawable.natt_48, "网络地址转换服务（NATT）",
						ScreenNatt.class),
//				new ScreenSettingsItem(R.drawable.gps_1, "GPS设置",
//						ScreenGPS.class), // 添加ScreenGPS启动项。
		};

		private final LayoutInflater mInflater;

		ScreenSettingsAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return sItems.length;
		}
		
	
		@Override
		public Object getItem(int position) {
			return sItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ScreenSettingsItem item = (ScreenSettingsItem) getItem(position);

			if (item == null) {
				return null;
			}

			if (view == null) {
				view = mInflater.inflate(R.layout.screen_settings_item, null);
			}

			((ImageView) view.findViewById(R.id.screen_settings_item_icon))
					.setImageResource(item.mIconResId);
			((TextView) view.findViewById(R.id.screen_settings_item_text))
					.setText(item.mText);

			return view;
		}

	}
}
