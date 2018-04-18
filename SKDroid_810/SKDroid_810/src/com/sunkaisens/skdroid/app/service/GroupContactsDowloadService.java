package com.sunkaisens.skdroid.app.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import com.sunkaisens.skdroid.Engine;

import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

public class GroupContactsDowloadService extends Service {
	private static final String TAG = "GroupContactsDowloadService";

	private final INgnConfigurationService mConfigurationService = (Engine
			.getInstance()).getConfigurationService(); // ��ȡ������Ϣ��Preferences���ķ������;

	private Boolean ContactsHasChanged = true; // ��ϵ�������Ƿ����

	private ServiceDownloader downloader = new ServiceDownloader();

	public class ServiceDownloader extends Thread {
		public void run() {
			HttpClient httpClient = new DefaultHttpClient();
			HttpRequest httpRequest = null;

			String localSipUri = "sip:watcher@sunkaisens.com";
			String xcapHeader = "X-XCAP-Asserted-Identity";
			// Header IdentityHeader = new Header("X-XCAP-Asserted-Identity",
			// etag);

			// HttpGet httpGet = new
			// HttpGet("http://192.168.1.144:1000/services/resource-lists/users/sip:watcher@192.168.1.72/index");
			httpRequest = new HttpGet(
					"http://192.168.1.20:1000/services/org.openmobilealliance.group-usage-list/users/sip:watcher@sunkaisens.com/index");
			httpRequest.addHeader(xcapHeader, localSipUri);
			HttpHost targethost = new HttpHost("192.168.1.20", 1000);

			try {
				System.out.println("����download����������������");
				// HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpResponse httpResponse = httpClient.execute(targethost,
						httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					System.out.println("״̬OK��");
					HttpEntity entity = httpResponse.getEntity();
					if (entity != null) {
						// System.out.println("ContenType�ǣ�"+entity.getContentType());
						BufferedReader br = new BufferedReader(
								new InputStreamReader(entity.getContent()));
						String line = null;
						String result = "";
						while ((line = br.readLine()) != null) {
							result += line + "\n";
						}
						// System.out.println(result);
						/*
						 * PrintWriter pw = new
						 * PrintWriter("/res/XML/index.xml"); pw.write(result);
						 * pw.close();
						 */

						// ���ظ��³ɹ���
						// mConfigurationService.putBoolean(NgnConfigurationEntry.XCAP_GroupContacts_Need_Update,
						// false); //���ñ��ص�Ⱥ����ϵ���ĵ�״̬Ϊ���£�

						// ���ڱ��ر���ͨ��¼�ļ��������ӷ�������ȡ��ͨ��¼��Ϣ�������ڴ��У�
						mConfigurationService
								.putString(
										NgnConfigurationEntry.XCAP_GroupContacts_For_Single,
										result);
					}
				} else {
					System.out.println("����δ�õ���Ӧ~������");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onCreate() {
		super.onCreate();
		if (ContactsHasChanged) {
			downloader.start();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
