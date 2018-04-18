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
			.getInstance()).getConfigurationService(); // 获取配置信息（Preferences）的服务对象;

	private Boolean ContactsHasChanged = true; // 联系人数据是否更新

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
				System.out.println("进入download！！！！！！！！");
				// HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpResponse httpResponse = httpClient.execute(targethost,
						httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					System.out.println("状态OK！");
					HttpEntity entity = httpResponse.getEntity();
					if (entity != null) {
						// System.out.println("ContenType是："+entity.getContentType());
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

						// 下载更新成功后：
						// mConfigurationService.putBoolean(NgnConfigurationEntry.XCAP_GroupContacts_Need_Update,
						// false); //设置本地的群组联系人文档状态为最新！

						// 不在本地保存通信录文件！仅将从服务器获取的通信录信息保存在内存中：
						mConfigurationService
								.putString(
										NgnConfigurationEntry.XCAP_GroupContacts_For_Single,
										result);
					}
				} else {
					System.out.println("请求未得到响应~！！！");
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
