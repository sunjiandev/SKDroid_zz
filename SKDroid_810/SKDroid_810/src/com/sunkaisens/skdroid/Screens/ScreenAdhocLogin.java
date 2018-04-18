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

//author  duhaitao
package com.sunkaisens.skdroid.Screens;

import java.io.IOException;
import java.util.HashMap;

import org.doubango.ngn.NgnApplication;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;

public class ScreenAdhocLogin extends BaseScreen {
	private static String TAG = ScreenAdhocLogin.class.getCanonicalName();
	//
	private EditText mEditName;// displayname
	private EditText mEditAccount;// mobileno
	private Button mBtLogin;// login button
	private HashMap<String, Object> mMessageREPORTHashMap = null;

	//
	public ScreenAdhocLogin() {
		super(SCREEN_TYPE.LOGIN_ADHOC, TAG);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		SystemVarTools.clear();

		setContentView(R.layout.adhoc_login_account);

		mEditName = (EditText) findViewById(R.id.login_et1);
		mEditAccount = (EditText) findViewById(R.id.login_et2);
		mBtLogin = (Button) findViewById(R.id.login_bt);

		mMessageREPORTHashMap = Tools_data.readData();
		String account = "";
		String name = "";
		if (mMessageREPORTHashMap != null) {
			account = (String) mMessageREPORTHashMap.get("recent_user");
			name = (String) mMessageREPORTHashMap.get("recent_name");
		} else {
			mMessageREPORTHashMap = new HashMap<String, Object>();
		}

		//
		mEditName.setText(name);
		mEditAccount.setText(account);
		//
		((ImageView) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {//
					@Override
					public void onClick(View v) {
						back();
					}
				});

		mBtLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.isActive()
						&& ScreenAdhocLogin.this.getCurrentFocus() != null)
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(ScreenAdhocLogin.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);

				if (mEditAccount.getText().toString().trim().isEmpty()) {

					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.input_acount_hint));
					return;
				}

				if (mEditName.getText().toString().trim().isEmpty()) {

					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.input_user_name_hint));
					return;
				}

				mMessageREPORTHashMap.put("recent_user", mEditAccount.getText()
						.toString().trim());
				mMessageREPORTHashMap.put("recent_name", mEditName.getText()
						.toString().trim());
				ServiceLoginAccount.getInstance().adhoc_Login(
						mEditName.getText().toString().trim(),
						mEditAccount.getText().toString().trim());
				try {
					Tools_data.writeData(mMessageREPORTHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mScreenService.show(ScreenTabHome.class);
				Engine.getInstance().getHistoryService()
						.setHistoryFile(SystemVarTools.getLocalParty());
			}

		});

	}

}