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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ScreenNetwork extends BaseScreen {
	private final static String TAG = ScreenNetwork.class.getCanonicalName();

	private final INgnConfigurationService mConfigurationService;

	//configure_network
	private EditText mEtProxyHost;
	private EditText mEtProxyPort;
	private Spinner mSpTransport;
	private Spinner mSpProxyDiscovery;
	private CheckBox mCbSigComp;
	private CheckBox mCbWiFi;
	private CheckBox mCb3G;
	private RadioButton mRbIPv4;
	private RadioButton mRbIPv6;
	
	//contacts
	private RadioButton mRbLocal;
	private RadioButton mRbRemote;
	private EditText mEtXcapRoot;
	private EditText mEtXUI;
	private EditText mEtPassword;
	private RelativeLayout mRlRemote;
	
	//GPS
	private EditText mETSendGPSToHost;
	private EditText mETSendGPSToPort;

	private final static String[] sSpinnerTransportItems = new String[] {
			NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT.toUpperCase(),
			"TCP", "TLS"/* , "SCTP" */};
	private final static String[] sSpinnerProxydiscoveryItems = new String[] {
			NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY,
			NgnConfigurationEntry.PCSCF_DISCOVERY_DNS_SRV /* , "DHCPv4/v6", "Both" */};

	public ScreenNetwork() {
		super(SCREEN_TYPE.NETWORK_T, TAG);

		this.mConfigurationService = getEngine().getConfigurationService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_network);

		//configure_network
		mEtProxyHost = (EditText) findViewById(R.id.screen_network_editText_pcscf_host);
		mEtProxyPort = (EditText) findViewById(R.id.screen_network_editText_pcscf_port);
		mSpTransport = (Spinner) findViewById(R.id.screen_network_spinner_transport);
		mSpProxyDiscovery = (Spinner) findViewById(R.id.screen_network_spinner_pcscf_discovery);
		mCbSigComp = (CheckBox) findViewById(R.id.screen_network_checkBox_sigcomp);
		mCbWiFi = (CheckBox) findViewById(R.id.screen_network_checkBox_wifi);
		mCb3G = (CheckBox) findViewById(R.id.screen_network_checkBox_3g);
		mRbIPv4 = (RadioButton) findViewById(R.id.screen_network_radioButton_ipv4);
		mRbIPv6 = (RadioButton) findViewById(R.id.screen_network_radioButton_ipv6);

		// spinners
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, sSpinnerTransportItems);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpTransport.setAdapter(adapter);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,
				sSpinnerProxydiscoveryItems);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpProxyDiscovery.setAdapter(adapter);

		mEtProxyHost.setText(mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));
		mEtProxyPort.setText(Integer.toString(mConfigurationService.getInt(
				NgnConfigurationEntry.NETWORK_PCSCF_PORT,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT)));
		mSpTransport.setSelection(super.getSpinnerIndex(mConfigurationService
				.getString(NgnConfigurationEntry.NETWORK_TRANSPORT,
						sSpinnerTransportItems[0]), sSpinnerTransportItems));
		mSpProxyDiscovery.setSelection(super.getSpinnerIndex(
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
						sSpinnerProxydiscoveryItems[0]),
				sSpinnerProxydiscoveryItems));
		mCbSigComp.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP));

		mCbWiFi.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NETWORK_USE_WIFI,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI));
		mCb3G.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NETWORK_USE_3G,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G));
		mRbIPv4.setChecked(mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_IP_VERSION,
				NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION)
				.equalsIgnoreCase("ipv4"));
		mRbIPv6.setChecked(!mRbIPv4.isChecked());

		// add listeners (for the configuration)
		super.addConfigurationListener(mEtProxyHost);
		super.addConfigurationListener(mEtProxyPort);
		super.addConfigurationListener(mSpTransport);
		super.addConfigurationListener(mSpProxyDiscovery);
		super.addConfigurationListener(mCbSigComp);
		super.addConfigurationListener(mCbWiFi);
		super.addConfigurationListener(mCb3G);
		super.addConfigurationListener(mRbIPv4);
		super.addConfigurationListener(mRbIPv6);
		
		//Contacts
		// get controls
		mRbLocal = (RadioButton) findViewById(R.id.screen_contacts_radioButton_local);
		mRbRemote = (RadioButton) findViewById(R.id.screen_contacts_radioButton_remote);
		mEtXcapRoot = (EditText) findViewById(R.id.screen_contacts_editText_xcaproot);
		mEtXUI = (EditText) findViewById(R.id.screen_contacts_editText_xui);
		mEtPassword = (EditText) findViewById(R.id.screen_contacts_editText_password);
		mRlRemote = (RelativeLayout) findViewById(R.id.screen_contacts_relativeLayout_remote);

		// load values from configuration file (Do it before adding UI listeners)
		mRbRemote.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.XCAP_ENABLED,
				NgnConfigurationEntry.DEFAULT_XCAP_ENABLED));
		// rbRemote.setChecked(!rbLocal.isChecked());
		mEtXcapRoot.setText(mConfigurationService.getString(
				NgnConfigurationEntry.XCAP_XCAP_ROOT,
				NgnConfigurationEntry.DEFAULT_XCAP_ROOT));
		mEtXUI.setText(mConfigurationService.getString(
				NgnConfigurationEntry.XCAP_USERNAME,
				NgnConfigurationEntry.DEFAULT_XCAP_USERNAME));
		mEtPassword.setText(mConfigurationService.getString(
				NgnConfigurationEntry.XCAP_PASSWORD,
				NgnConfigurationEntry.DEFAULT_XCAP_PASSWORD));
		mRlRemote.setVisibility(mRbLocal.isChecked() ? View.INVISIBLE
				: View.VISIBLE);

		// add listeners (for the configuration)
		addConfigurationListener(mRbLocal);
		addConfigurationListener(mRbRemote);
		addConfigurationListener(mEtXcapRoot);
		addConfigurationListener(mEtXUI);
		addConfigurationListener(mEtPassword);

		mRbLocal.setOnCheckedChangeListener(rbLocal_OnCheckedChangeListener);
		
		//gps
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

	private OnCheckedChangeListener rbLocal_OnCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mRlRemote.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
		}
	};
	
	protected void onPause() {
		if (super.mComputeConfiguration) {

			//configure_network
			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_PCSCF_HOST, mEtProxyHost
							.getText().toString().trim());
			mConfigurationService.putInt(
					NgnConfigurationEntry.NETWORK_PCSCF_PORT,
					NgnStringUtils.parseInt(mEtProxyPort.getText().toString()
							.trim(),
							NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_TRANSPORT,
					ScreenNetwork.sSpinnerTransportItems[mSpTransport
							.getSelectedItemPosition()]);
			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
					ScreenNetwork.sSpinnerProxydiscoveryItems[mSpProxyDiscovery
							.getSelectedItemPosition()]);
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
					mCbSigComp.isChecked());
			mConfigurationService
					.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI,
							mCbWiFi.isChecked());
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.NETWORK_USE_3G, mCb3G.isChecked());
			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_IP_VERSION,
					mRbIPv4.isChecked() ? "ipv4" : "ipv6");
			
			//contacts
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.XCAP_ENABLED, mRbRemote.isChecked());
			mConfigurationService.putString(
					NgnConfigurationEntry.XCAP_XCAP_ROOT, mEtXcapRoot.getText()
							.toString());
			mConfigurationService.putString(
					NgnConfigurationEntry.XCAP_USERNAME, mEtXUI.getText()
							.toString());
			mConfigurationService.putString(
					NgnConfigurationEntry.XCAP_PASSWORD, mEtPassword.getText()
							.toString());

			//gps
			mConfigurationService.putString(
					NgnConfigurationEntry.GPS_SENDTO_HOST, mETSendGPSToHost
							.getText().toString().trim());
			mConfigurationService.putInt(NgnConfigurationEntry.GPS_SENDTO_PORT,
					NgnStringUtils.parseInt(mETSendGPSToPort.getText()
							.toString().trim(),
							NgnConfigurationEntry.DEFAULT_GPS_SENDTO_PORT));

			// Compute
			if (!mConfigurationService.commit()) {
				Log.e(TAG, "Failed to commit() configuration");
			}

			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
}
