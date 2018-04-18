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
import java.util.List;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.adapter.MoreAdapter;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ScreenTabMore_bak extends BaseScreen {
	private static String TAG = ScreenTabMore_bak.class.getCanonicalName();

	private List<String> morelist = new ArrayList<String>();

	static enum PhoneInputType {
		Numbers, Text
	}
	//
	//private final INgnSipService mSipService;

	public ScreenTabMore_bak() {
		super(SCREEN_TYPE.DIALER_T, TAG);

		//mSipService = getEngine().getSipService();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_more);

		//list数据填充
		ListView list = (ListView) findViewById(R.id.morelist);
		updateMoreList();
		list.setAdapter(new MoreAdapter(this,morelist));
		//
		list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//list.getChildAt(arg2);
				switch(arg2)
				{
				case 0:
					mScreenService.show(ScreenIdentity.class);
					break;
				case 1:
					mScreenService.show(ScreenAbout.class);
					break;
				case 2:
					mScreenService.show(ScreenSettings.class);
					break;
				default:
					break;
				}
			}
			
		});
		//
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean hasBack() {
		return true;
	}
	
	@Override
	public boolean back() {
		return super.back();
	}

	//
	private void updateMoreList()
	{		
		morelist.add("个人资料");
		morelist.add("关于");
		morelist.add("设置");
	}
}
