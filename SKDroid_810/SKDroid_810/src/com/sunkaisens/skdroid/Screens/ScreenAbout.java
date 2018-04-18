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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class ScreenAbout extends BaseScreen {
	private static final String TAG = ScreenAbout.class.getCanonicalName();

	private TextView funcdesc;
	private TextView updatelist;
	private TextView buglist;
	private TextView feedback;
	private TextView sunkaisens_version;

	public ScreenAbout() {
		super(SCREEN_TYPE.ABOUT_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_about);

		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		sunkaisens_version = (TextView) this
				.findViewById(R.id.sunkaisens_version);
		String copyright = sunkaisens_version.getText().toString();
		// textView.setText(String.format(copyright, SKDroid.getVersionName(),
		// this.getString(R.string.doubango_revision)));
		sunkaisens_version.setText(copyright + SKDroid.getVersionName());

		updatelist = (TextView) this.findViewById(R.id.update_list);
		updatelist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenAboutExpanded.class, "updatelist");
			}
		});

		funcdesc = (TextView) this.findViewById(R.id.func_desc);

		funcdesc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenAboutExpanded.class, "funclist");
			}
		});

		buglist = (TextView) this.findViewById(R.id.bug_list);

		buglist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenAboutExpanded.class, "buglist");
			}
		});

		feedback = (TextView) findViewById(R.id.feedback_but);
		feedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent((Main) Engine.getInstance()
						.getMainActivity(), ScreenFeedback.class));
			}
		});
		if (!SystemVarTools.useFeedback) {
			feedback.setVisibility(View.GONE);
		}
	}
}
