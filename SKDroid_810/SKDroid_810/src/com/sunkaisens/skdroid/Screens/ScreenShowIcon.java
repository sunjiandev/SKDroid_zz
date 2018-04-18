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

import org.doubango.utils.MyLog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;

import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.RoundProgressBar;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;

public class ScreenShowIcon extends Activity {

	public static String TAG = ScreenShowIcon.class.getCanonicalName();

	public ImageView showImageView = null;

	public static ModelContact showContact = null;

	public Handler progressHandler = null;

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_icon);

		showImageView = (ImageView) findViewById(R.id.showicon_imageView);

		final RoundProgressBar roundProgressBar = (RoundProgressBar) findViewById(R.id.showicon_progress);

		LayoutParams params = showImageView.getLayoutParams();
		params.height = params.width;

		progressHandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {
					case Main.FILEDOWNLOADPROGRESS:

						if (roundProgressBar != null) {

							int progress = msg.getData().getInt(
									"fileTransferProgress");

							if ((progress % 20) == 0) {
								Log.d(TAG, "upload progress=" + progress);
							}

							if (progress < 100) {
								roundProgressBar.setVisibility(View.VISIBLE);
								roundProgressBar.setProgress(progress);

							} else {
								roundProgressBar.setVisibility(View.GONE);
							}

						}
						break;

					case Main.FILEDOWNLOAD_SUCCESS:
						SystemVarTools.showBigicon(showImageView, showContact,
								getApplicationContext(), null);

						break;

					case Main.FILEUPLOAD_FAILED:

						break;

					default:
						break;

					}
				} catch (Exception e) {
					MyLog.d(TAG,
							"ScreenChat Exception line 940" + e.getMessage());
				}
			}
		};

		if (showContact != null) {
			SystemVarTools.showBigicon(showImageView, showContact,
					getApplicationContext(), progressHandler);
		}
	}

}
