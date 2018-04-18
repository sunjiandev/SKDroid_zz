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

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;

import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistoryAVCallEvent.HistoryEventAVFilter;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.model.NgnHistorySMSEvent.HistoryEventSMSFilter;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.DialerUtils;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.CallAdapter;
import com.sunkaisens.skdroid.component.ClearEditText;
import com.sunkaisens.skdroid.model.ModelCall;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;

public class ScreenTabCall extends BaseScreen {
	private static String TAG = ScreenTabCall.class.getCanonicalName();

	private ClearEditText mEtNumber;

	// private ImageButton mIbInputType;
	private static final int MENU_CLEAR_ALL_CALLLIST = 1;
	private static final int MENU_CLEAR_CURRENT_CALLLIST = 2;
	//
	private ImageButton btnserch;

	private int position = 0;

	private ModelCall mModelCall;

	static enum PhoneInputType {
		Numbers, Text
	}

	private PhoneInputType mInputType;
	private InputMethodManager mInputMethodManager;

	private final INgnSipService mSipService;
	public final INgnHistoryService mHistorytService;

	public CallAdapter mAdapter;

	public ScreenTabCall() {
		super(SCREEN_TYPE.DIALER_T, TAG);

		mSipService = getEngine().getSipService();

		mInputType = PhoneInputType.Numbers;

		mHistorytService = getEngine().getHistoryService();

		mAdapter = new CallAdapter(this);
	}

	@Override
	public boolean refresh() {
		ListView list = (ListView) findViewById(R.id.calllist);
		if (list != null) {
			((CallAdapter) list.getAdapter()).setSearchKey(mEtNumber.getText()
					.toString());
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_call);
		MyLog.d(TAG, "onCreate()");

		// list数据填充
		ListView list = (ListView) findViewById(R.id.calllist);
		// updateCallList();
		list.setAdapter(mAdapter);

		registerForContextMenu(list);// gzc 20141025

		// keybutton
		CheckBox keybutton = (CheckBox) findViewById(R.id.keybutton);
		keybutton
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton view,
							boolean checked) {

						TranslateAnimation mHiddAnimation = new TranslateAnimation(
								Animation.RELATIVE_TO_SELF, 0.0f,
								Animation.RELATIVE_TO_SELF, 0.0f,
								Animation.RELATIVE_TO_SELF, 1.0f,
								Animation.RELATIVE_TO_SELF, 0.0f);
						mHiddAnimation.setDuration(500);

						LinearLayout l = (LinearLayout) findViewById(R.id.keylayoutmain);
						l.startAnimation(mHiddAnimation);
						l.setVisibility(View.VISIBLE);

					}

				});
		//
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		mEtNumber = (ClearEditText) findViewById(R.id.screen_tab_dialer_editText_number);
		mEtNumber.setClearIconVisible(true);

		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_0,
				"0", "+", DialerUtils.TAG_0, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_1,
				"1", "", DialerUtils.TAG_1, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_2,
				"2", "ABC", DialerUtils.TAG_2, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_3,
				"3", "DEF", DialerUtils.TAG_3, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_4,
				"4", "GHI", DialerUtils.TAG_4, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_5,
				"5", "JKL", DialerUtils.TAG_5, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_6,
				"6", "MNO", DialerUtils.TAG_6, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_7,
				"7", "PQRS", DialerUtils.TAG_7, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_8,
				"8", "TUV", DialerUtils.TAG_8, mOnDialerClick);
		DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_9,
				"9", "WXYZ", DialerUtils.TAG_9, mOnDialerClick);
		DialerUtils.setDialerTextButton(this,
				R.id.screen_tab_dialer_button_star, "*", "",
				DialerUtils.TAG_STAR, mOnDialerClick);
		DialerUtils.setDialerTextButton(this,
				R.id.screen_tab_dialer_button_sharp, "#", "",
				DialerUtils.TAG_SHARP, mOnDialerClick);

		DialerUtils.setDialerImageButton(this,
				R.id.screen_tab_dialer_button_audio, R.drawable.callkey_audio2,
				DialerUtils.TAG_AUDIO_CALL, mOnDialerClick);
		DialerUtils.setDialerImageButton(this,
				R.id.screen_tab_dialer_button_video, R.drawable.callkey_video2,
				DialerUtils.TAG_VIDEO_CALL, mOnDialerClick);
		DialerUtils.setDialerImageButton(this,

		R.id.screen_tab_dialer_button_del, R.drawable.callkey_back2,
				DialerUtils.TAG_DELETE, mOnDialerClick);
		//
		DialerUtils.setDialerImageButton(this,
				R.id.screen_tab_dialer_button_hide,
				R.drawable.n_maintab_callkey2, DialerUtils.TAG_HIDE,
				mOnDialerClick);
		//

		mEtNumber.setInputType(InputType.TYPE_NULL);
		mEtNumber.setFocusable(false);
		mEtNumber.setFocusableInTouchMode(false);
		mEtNumber.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (mInputType == PhoneInputType.Numbers) {
					final boolean bShowCaret = mEtNumber.getText().toString()
							.length() > 0;

					if (bShowCaret) {
						mEtNumber.setVisibility(View.VISIBLE);
						mEtNumber.setFocusableInTouchMode(bShowCaret);
						mEtNumber.setFocusable(bShowCaret);
						refresh();
					} else {
						mEtNumber.setVisibility(View.GONE);
						mEtNumber.setFocusableInTouchMode(bShowCaret);
						mEtNumber.setFocusable(bShowCaret);
						refresh();
					}
				}
			}
		});

		findViewById(R.id.screen_tab_dialer_button_0).setOnLongClickListener(
				new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						appendText("+");
						return true;
					}
				});

		btnserch = (ImageButton) findViewById(R.id.screen_tab_call_search_bt);
		// searchedit = (EditText)
		// findViewById(R.id.screen_tab_contact_searchedit);

		btnserch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// if(searchedit.getVisibility()==View.GONE){
				// searchedit.setVisibility(View.VISIBLE);
				// }else {
				// searchedit.setVisibility(View.GONE);
				// }
				Log.e("我被点击了", "我被点击了");
				mScreenService.show(ScreenSearch.class);
			}
		});

		BroadcastReceiver bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：

				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {

					ScreenTabCall.this.refresh();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));

		/*
		 * mIbInputType.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { switch (mInputType) { case
		 * Numbers: mInputType = PhoneInputType.Text;
		 * mIbInputType.setImageResource(R.drawable.input_text);
		 * mEtNumber.setInputType(InputType.TYPE_CLASS_TEXT);
		 * 
		 * mEtNumber.setFocusableInTouchMode(true);
		 * mEtNumber.setFocusable(true);
		 * mInputMethodManager.showSoftInput(mEtNumber, 0); break;
		 * 
		 * case Text: default: mInputType = PhoneInputType.Numbers;
		 * mIbInputType.setImageResource(R.drawable.input_numbers);
		 * mEtNumber.setInputType(InputType.TYPE_NULL);
		 * 
		 * final boolean bShowCaret = mEtNumber.getText().toString() .length() >
		 * 0; mEtNumber.setFocusableInTouchMode(bShowCaret);
		 * mEtNumber.setFocusable(bShowCaret);
		 * mInputMethodManager.hideSoftInputFromWindow(
		 * mEtNumber.getWindowToken(), 0); break; } } });
		 */
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private final View.OnClickListener mOnDialerClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int tag = Integer.parseInt(v.getTag().toString());
			final String number = mEtNumber.getText().toString();
			if (tag == DialerUtils.TAG_CHAT) {
				if (mSipService.isRegisteSessionConnected()
						&& !NgnStringUtils.isNullOrEmpty(number)) {
					// ScreenChat.startChat(number);
					mEtNumber.setText(NgnStringUtils.emptyValue());
				}

			} else if (tag == DialerUtils.TAG_HIDE) {
				TranslateAnimation mHiddAnimation = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 1.0f);
				mHiddAnimation.setDuration(500);

				LinearLayout l = (LinearLayout) findViewById(R.id.keylayoutmain);
				l.startAnimation(mHiddAnimation);
				l.setVisibility(View.GONE);
			} else if (tag == DialerUtils.TAG_DELETE) {

				final int selStart = mEtNumber.getSelectionStart();
				if (selStart > 0) {
					final StringBuffer sb = new StringBuffer(number);
					sb.delete(selStart - 1, selStart);
					mEtNumber.setText(sb.toString());
					mEtNumber.setSelection(selStart - 1);
				}
			} else if (tag == DialerUtils.TAG_AUDIO_CALL) {
				// if (mSipService.isRegisteSessionConnected()
				// && !NgnStringUtils.isNullOrEmpty(number)) {
				if (number == null || number.isEmpty())
					return;
				ModelContact mc = SystemVarTools
						.getContactFromPhoneNumber(number);
				// if(mc !=null && mc.isgroup)
				// {
				// ScreenAV.makeCall(number,
				// NgnMediaType.Audio,SessionType.GroupAudioCall);
				// }
				// else
				// {
				// ScreenAV.makeCall(number,
				// NgnMediaType.Audio,SessionType.AudioCall);
				// }
				if (mc != null) {
					if (mc.isgroup) {
						ServiceAV.makeCall(number, NgnMediaType.Audio,
								SessionType.GroupAudioCall);
					} else {
						ServiceAV.makeCall(number, NgnMediaType.Audio,
								SessionType.AudioCall);
					}
				} else {
					ServiceAV.makeCall(number, NgnMediaType.Audio,
							SessionType.AudioCall);
				}
				mEtNumber.setText(NgnStringUtils.emptyValue());
				// }
			} else if (tag == DialerUtils.TAG_VIDEO_CALL) {
				if (mSipService.isRegisteSessionConnected()
						&& !NgnStringUtils.isNullOrEmpty(number)) {
					ModelContact mc = SystemVarTools
							.getContactFromPhoneNumber(number);
					if (mc != null && mc.isgroup) {
						ServiceAV.makeCall(number, NgnMediaType.AudioVideo,
								SessionType.GroupVideoCall);
					} else {
						ServiceAV.makeCall(number, NgnMediaType.AudioVideo,
								SessionType.VideoCall);
					}
					// ScreenAV.makeCall(number,
					// NgnMediaType.Audio,SessionType.EncryptCall);//just for
					// encryptcall test

					mEtNumber.setText(NgnStringUtils.emptyValue());
				}
			} else {
				final String textToAppend = tag == DialerUtils.TAG_STAR ? "*"
						: (tag == DialerUtils.TAG_SHARP ? "#" : Integer
								.toString(tag));
				appendText(textToAppend);
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.d(TAG, "onResume()");

		if (mHistorytService.isLoading()) {
			Toast.makeText(this, "Loading history...", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public boolean hasBack() {
		return true;
	}

	@Override
	public boolean back() {
		return super.back();
	}

	private void appendText(String textToAppend) {
		final int selStart = mEtNumber.getSelectionStart();
		final StringBuffer sb = new StringBuffer(mEtNumber.getText().toString());
		sb.insert(selStart, textToAppend);
		mEtNumber.setText(sb.toString());
		mEtNumber.setSelection(selStart + 1);
	}

	@Override
	public boolean hasMenu() {
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v != null && v.getTag() != null) {
			mModelCall = (ModelCall) v.getTag();
			NgnHistoryEvent event = mModelCall.mEvent;
			if (event != null) {
				ModelContact userinfo = SystemVarTools
						.createContactFromRemoteParty(event.getRemoteParty());
				menu.setHeaderTitle(userinfo.name);
				menu.addSubMenu(1, MENU_CLEAR_CURRENT_CALLLIST, Menu.NONE,
						ScreenTabCall.this
								.getString(R.string.delete_current_history));
				menu.addSubMenu(1, MENU_CLEAR_ALL_CALLLIST, Menu.NONE,
						ScreenTabCall.this
								.getString(R.string.delete_all_history));

				Log.d(TAG, "删除通话记录  NO=" + userinfo.mobileNo);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		NgnHistoryAVCallEvent event = (NgnHistoryAVCallEvent) mModelCall.mEvent;
		switch (item.getItemId()) {
		case MENU_CLEAR_CURRENT_CALLLIST:
			mHistorytService.deleteEvent(event);
			this.refresh();
			Log.d(TAG, "删除当前记录");
			break;
		case MENU_CLEAR_ALL_CALLLIST:
			mHistorytService.deleteEvents(new HistoryEventAVFilter());
			Log.d(TAG, "删除全部通话记录");
			this.refresh();
			break;
		}
		return true;
	}

}
