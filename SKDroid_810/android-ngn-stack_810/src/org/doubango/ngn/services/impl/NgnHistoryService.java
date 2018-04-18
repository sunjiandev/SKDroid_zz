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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryAVCallEvent.HistoryEventAVFilter;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryList;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistorySMSEvent.HistoryEventSMSFilter;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnObservableList;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.utils.MyLog;
import org.doubango.utils.MySQLiteHelper;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.sunkaisens.skdroid.util.GlobalVar;

import android.mtp.MtpStorageInfo;
import android.util.Log;

public class NgnHistoryService extends NgnBaseService implements
		INgnHistoryService {
	private final static String TAG = NgnHistoryService.class
			.getCanonicalName();
	// private final static String HISTORY_FILE = "history.xml";
	private static String HISTORY_FILE = "history.xml";

	private File mHistoryFile;
	private NgnHistoryList mEventsList;
	private final Serializer mSerializer;
	private boolean mLoadingHistory;
	private MySQLiteHelper mySQLHelper;

	public NgnHistoryService() {
		super();

		mSerializer = new Persister();
		mEventsList = new NgnHistoryList();

	}

	@Override
	public boolean start() {
		MyLog.d(TAG, "Starting...");
		boolean result = true;

		mySQLHelper = new MySQLiteHelper(NgnApplication.getContext(),
				"sunkaisens.db2", null, 1);
		MyLog.d(TAG, "MySQLHelper = "+mySQLHelper);
		/*
		 * http://code.google.com/p/dalvik/wiki/JavaxPackages Ensure the factory
		 * implementation is loaded from the application classpath (which
		 * contains the implementation classes), rather than the system
		 * classpath (which doesn't).
		 */
		Thread.currentThread().setContextClassLoader(
				getClass().getClassLoader());

		mHistoryFile = new File(String.format("%s/%s", NgnEngine.getInstance()
				.getStorageService().getCurrentDir(),
				NgnHistoryService.HISTORY_FILE));
//		MyLog.d(TAG, "历史记录文件：" + mHistoryFile.getPath());
		if (!mHistoryFile.exists()) {
			try {
				mHistoryFile.createNewFile();
				result = compute(); /* to create an empty but valid document */
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				mHistoryFile = null;
				result = false;
			}
		}

		return result;
	}

	@Override
	public boolean stop() {
		MyLog.d(TAG, "Stopping");
		return true;
	}

	@Override
	public boolean load(String mLocalNum) {
		boolean result = true;
		MyLog.d(TAG, "load(" + mLocalNum + ")");

		try {
			mLoadingHistory = true;
			MyLog.d(TAG, "Loading history");
			if (mEventsList.getList().getList().size() == 0) {
				mEventsList = mSerializer.read(mEventsList.getClass(),
						mHistoryFile);
				try {
					List<NgnHistoryAVCallEvent> results = mySQLHelper
							.queryAVEvents(mLocalNum);
					if (results != null && results.size() > 0) {
						for (NgnHistoryAVCallEvent avEvent : results) {
							mEventsList.addEvent(avEvent);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					List<NgnHistorySMSEvent> results = mySQLHelper
							.querySMSEvents(mLocalNum);
					if (results != null && results.size() > 0) {
						for (NgnHistorySMSEvent avEvent : results) {
							mEventsList.addEvent(avEvent);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				MyLog.d(TAG, "mEventsList is not null.");
			}
			MyLog.d(TAG, "History loaded");
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		}

		mLoadingHistory = false;
		return result;
	}

	@Override
	public boolean isLoading() {
		return mLoadingHistory;
	}

	@Override
	public void addEvent(NgnHistoryEvent event) {
		mEventsList.addEvent(event);
		//对于翰讯大终端复现的空指针问题解决方式
		if (mySQLHelper==null) {
			mySQLHelper = new MySQLiteHelper(NgnApplication.getContext(),
					"sunkaisens.db2", null, 1);
		}
		if (event instanceof NgnHistoryAVCallEvent) {
			
			mySQLHelper.insertAVEvent((NgnHistoryAVCallEvent) event);
			return;
		}
		if (event instanceof NgnHistorySMSEvent) {
			mySQLHelper.insertSMSEvent((NgnHistorySMSEvent) event);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void updateEvent(final NgnHistoryEvent event) {
		Log.e(TAG, "updateEvent()");
	//	mEventsList.getList().update(null, event);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (event instanceof NgnHistorySMSEvent) {
					mySQLHelper.updateSMSEvent((NgnHistorySMSEvent) event);

				}
			}
		}).start();
	}

	@Override
	public void deleteEvent(NgnHistoryEvent event) {

		mEventsList.removeEvent(event);

		// 事件不同，所需操作的表不同，调用的处理方法也不同
		if (event instanceof NgnHistoryAVCallEvent) {
			mySQLHelper.deleteAVEvent((NgnHistoryAVCallEvent) event);
			return;
		}
		if (event instanceof NgnHistorySMSEvent) {
			mySQLHelper.deleteSMSEvent((NgnHistorySMSEvent) event);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void deleteEvent(int location) {
		mEventsList.removeEvent(location);
		new Thread(new Runnable() {
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void deleteEvents(NgnPredicate<NgnHistoryEvent> predicate) {
		MyLog.d(TAG, "deleteEvents()");
		mEventsList.removeEvents(predicate);

		if (predicate instanceof HistoryEventSMSFilter) {
			mySQLHelper.deleteSMSEvents(GlobalVar.mLocalNum);
			return;
		}
		if (predicate instanceof HistoryEventAVFilter) {
			mySQLHelper.deleteAVEvents(GlobalVar.mLocalNum);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void clear() {
		mEventsList.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public List<NgnHistoryEvent> getEvents() {
		return mEventsList.getList().getList();
	}

	@Override
	public NgnObservableList<NgnHistoryEvent> getObservableEvents() {
		return mEventsList.getList();
	}

	private synchronized boolean compute() {
		synchronized (this) {
			if (mHistoryFile == null || mSerializer == null) {
				Log.e(TAG, "Invalid arguments");
				return false;
			}
			try {
				mSerializer.write(mEventsList, mHistoryFile);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	public void setHistoryFile(String fileName) {
		mEventsList.clear();
		stop();

		HISTORY_FILE = fileName + "_history.xml";

		start();
		load(fileName);
	}

	// /**
	// * 将历史记录刷新到文件中
	// * gzc 20140818
	// */
	// public void reflush() {
	// new Thread(new Runnable(){
	// @Override
	// public void run() {
	// compute();
	// }
	// }).start();
	// }

}
