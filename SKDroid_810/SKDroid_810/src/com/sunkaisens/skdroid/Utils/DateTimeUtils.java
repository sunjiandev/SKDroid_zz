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
package com.sunkaisens.skdroid.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.doubango.ngn.utils.NgnDateTimeUtils;




import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;

public class DateTimeUtils extends NgnDateTimeUtils {

	static final DateFormat sDateFormat = DateFormat.getInstance();
	static final DateFormat sDateTimeFormat = DateFormat.getDateTimeInstance(
			DateFormat.DEFAULT,DateFormat.DEFAULT,Locale.getDefault());
	private static SimpleDateFormat sDateTimeFormat2 = 
			new SimpleDateFormat("HH:mm:ss",Locale.getDefault());// 日志的输出格式
	static final DateFormat sTimeFormat = 
			DateFormat.getTimeInstance(DateFormat.DEFAULT,Locale.getDefault());
	
	static String sTodayName;
	static String sYesterdayName;

	public static String getTodayName() {
		if (sTodayName == null) {
			sTodayName = SKDroid.getContext().getResources()
					.getString(R.string.day_today);
		}
		return sTodayName;
	}

	public static String getYesterdayName() {
		if (sYesterdayName == null) {
			sYesterdayName = SKDroid.getContext().getResources()
					.getString(R.string.day_yesterday);
		}
		return sYesterdayName;
	}

	public static String getFriendlyDateString(final Date date) {
		//根据系统的时间制式、时区调整时间显示
		boolean is12Format = android.text.format.DateFormat.is24HourFormat(SKDroid.getContext());
		if(!is12Format){
			final Date today = new Date();
			if (DateTimeUtils.isSameDay(date, today)) {
				return String.format("%s %s", getTodayName(),
						sTimeFormat.format(date));
			} else if ((today.getDay() - date.getDay()) == 1) {
				return String.format("%s %s", getYesterdayName(),
						sTimeFormat.format(date));
			} else {
				return sDateTimeFormat.format(date);
			}
		}else {
			final Date today = new Date();
			if (DateTimeUtils.isSameDay(date, today)) {
				return String.format("%s %s", getTodayName(),
						sDateTimeFormat2.format(date));
			} else if ((today.getDay() - date.getDay()) == 1) {
				return String.format("%s %s", getYesterdayName(),
						sDateTimeFormat2.format(date));
			} else {
				return sDateTimeFormat2.format(date);
			}
		}
		
	}
}
