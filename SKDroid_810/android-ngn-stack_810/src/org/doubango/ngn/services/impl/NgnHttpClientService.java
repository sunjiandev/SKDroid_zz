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
package org.doubango.ngn.services.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.doubango.ngn.services.INgnHttpClientService;
import org.doubango.utils.MyLog;

import android.util.Log;

/**@page NgnHttpClientService_page HTTP/HTTPS Service
 * The HTTP/HTTPS service is used to send and retrieve data to/from remote server using HTTP/HTTPS protocol.
 */

/**
 * HTTP/HTTPS service.
 */
public class NgnHttpClientService extends NgnBaseService implements INgnHttpClientService{
	private static final String TAG = NgnHttpClientService.class.getCanonicalName();
	
	private static final int sTimeoutConnection = 3000;
	private static final int sTimeoutSocket = 5000;

	private HttpClient mClient;
	
	public NgnHttpClientService(){
		super();
	}
	
	@Override
	public boolean start() {
		MyLog.d(TAG, "Starting...");
		
		if(mClient == null){
			mClient = new DefaultHttpClient();
			final HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, sTimeoutConnection);
			HttpConnectionParams.setSoTimeout(params, sTimeoutSocket);
			((DefaultHttpClient)mClient).setParams(params);
			return true;
		}
		Log.e(TAG, "Already started");
		return false;
	}

	@Override
	public boolean stop() {
		if(mClient != null){
			mClient.getConnectionManager().shutdown();
		}
		mClient = null;
		return true;
	}

	@Override
	public String get(String uri) {
		try{
			HttpGet getRequest = new HttpGet(uri);
			final HttpResponse resp = mClient.execute(getRequest);
			if(resp != null){
				return getResponseAsString(resp);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String post(String uri, String contentUTF8, String contentType){
		try{
			HttpPost postRequest = new HttpPost(uri);
			final StringEntity entity = new StringEntity(contentUTF8,"UTF-8");
			if(contentType != null){
				entity.setContentType(contentType);
			}
			postRequest.setEntity(entity);
			final HttpResponse resp = mClient.execute(postRequest);
			if(resp != null){
				return getResponseAsString(resp);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String put(String uri, String contentUTF8,Map<String, String> headers){
		try{
			HttpPut putRequest = new HttpPut(uri);
			final StringEntity entity = new StringEntity(contentUTF8,"UTF-8");
			putRequest.setEntity(entity);
			if(headers != null && headers.keySet().size() != 0){
				for(String key : headers.keySet()){
					putRequest.addHeader(key, headers.get(key));
				}
			
			}
			final HttpResponse resp = mClient.execute(putRequest);
			if(resp != null){
				return getResponseAsString(resp);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String delete(String uri,Map<String, String> headers){
		try{
			HttpDelete deleteRequest = new HttpDelete(uri);
			if(headers != null && headers.keySet().size() != 0){
				for(String key : headers.keySet()){
					deleteRequest.addHeader(key, headers.get(key));
				}
			
			}
			final HttpResponse resp = mClient.execute(deleteRequest);
			if(resp != null){
				return getResponseAsString(resp);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public InputStream getBinary(String uri) {
		try{
			HttpGet getRequest = new HttpGet(uri);
			final HttpResponse resp = mClient.execute(getRequest);
			if(resp != null){
				return  resp.getEntity().getContent();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	

	public static String getResponseAsString(HttpResponse resp){
        String result = "";
        try{
        	StatusLine statusLine = resp.getStatusLine();
            InputStream in = resp.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
            result = statusLine.getStatusCode()+";"+statusLine.getReasonPhrase()+"&"+result;
        }catch(Exception ex){
            result = null;
        }
        return result;
    }

	@Override
	public boolean setParams(HttpParams params) {
		// TODO Auto-generated method stub
		if(mClient != null && params != null){
			((DefaultHttpClient)mClient).setParams(params);
			return true;
		}else {
			return false;
		}
	}
}
