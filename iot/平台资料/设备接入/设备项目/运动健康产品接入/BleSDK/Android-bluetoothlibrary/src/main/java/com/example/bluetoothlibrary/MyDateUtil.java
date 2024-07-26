package com.example.bluetoothlibrary;

import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDateUtil {
	public static final int EVERYTIME = 0;
	public static final int EVERYDAY = 1;
	public static final int EVERYWEEK = 2;
	public static final int EVERYMONTH = 3;

	// 获得系统当前日期时间：yyyy-MM-dd HH:mm:ss
	public static String getSystemDate3() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sDateFormat.format(new Date());
		return date;
	}
	
	/** 如果不传格式。。就用默认的格式:yyyy-MM-dd HH-mm-ss.sss */
	public static String getDateFormatToString(String format) {
		Date date = new Date();
		if (format == null) {
			format = "yyyy-MM-dd HH:mm:ss.SSS";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		String dateStr = dateFormat.format(date);
		return dateStr;
	}

	/** :yyyy-MM-dd */
	public static String getStringDD(String date) {
		String[] dateStr = date.split(" ");
		return dateStr[0];
	}

	/** 显示日期格式为：yyyy-MM-dd */
	public static String getDateFormatToYMD(String date) {
		String targetString = "";
		if (date != null) {
			date = getStringDD(date);
			String[] dateStr = date.split("/");
			targetString = dateStr[0] + "-" + dateStr[1] + "-" + dateStr[2];
		}
		return targetString;
	}

	public static int getDateDays(String date1, String date2) {
		System.out.println("date2=" + date2);
		System.out.println("date1=" + date1);
		date1 = date1.split(" ")[0];
		date2 = date2.split(" ")[0];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		int days = 0;
		try {
			Date date = sdf.parse(date1);// 通过日期格式的parse()方法将字符串转换成日期
			Date dateBegin = sdf.parse(date2);
			long betweenTime = date.getTime() - dateBegin.getTime();
			days = (int) (betweenTime / 1000 / 60 / 60 / 24);
		} catch (Exception e) {
		}
		return days;
	}

	public static String getDate(Context context, String date) {
		int days = getDateDays(getDateFormatToString(null), date);
		String[] dateStr = date.split(" ");
		String[] i = dateStr[1].split(":");
		int h, m;
		h = Integer.valueOf(i[0]);
		m = Integer.valueOf(i[1]);
		dateStr[1] = i[0] + ":" + i[1];
		if (days == 0) {
			if (h > 12) {
				h = h - 12;
				date = "今天，下午 " + h + ":" + m;
			} else {
				date = "今天，上午 " + h + ":" + m;
			}
		} else if (days == 1) {
			if (h > 12) {
				h = h - 12;
				date = "昨天，下午 " + h + ":" + m;
			} else {
				date = "昨天，上午 " + h + ":" + m;
			}
		} else {
			date = dateStr[0];
		}
		return date;
	}

	/*
	 * *
	 * 两个时间相差距离多少天多少小时多少分多少秒
	 * 
	 * @param str1 时间参数 1 格式：1990-01-01 12:00:00
	 * 
	 * @param str2 时间参数 2 格式：2009-01-01 12:00:00
	 * 
	 * @return String 返回值为：xx天xx小时xx分xx秒
	 */
	public static String getDistanceTime(String str2) {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date one;
		Date two;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		try {
			one = new Date();
			two = df.parse(str2);
			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return day > 0 ? day + "天前" : "今天";
	}

	public static String getH_Min(String h_min) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Date date = null;
		try {
			date = dateFormat.parse(h_min);
		} catch (Exception e) {
		}
		h_min = dateFormat.format(date);
		return h_min;
	}

	/** 是否早于今天 */
	public static boolean isBeforeToDay(String y_m_d) {
		int compareTo = 0;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = dateFormat.parse(y_m_d);

			Date nowDate = dateFormat.parse(dateFormat.format(new Date()));
			compareTo = date.compareTo(nowDate);

		} catch (Exception e) {
		}
		return compareTo < 0;
	}

	/** 返回拼写好格式为2008-11-05 21:33 */
	public static String getSpellTime(int curYears, int curMonths, int curDays,
			int curHours, int curMinutes) {
		StringBuilder y_m_d = getSpell_Y_M_D(curYears, curMonths, curDays);
		StringBuilder h_M = getSpell_H_M(curHours, curMinutes);
		return y_m_d.toString() + h_M.toString();
	}

	public static StringBuilder getSpell_H_M(int curHours, int curMinutes) {
		StringBuilder h_M = new StringBuilder();
		if (curHours > 9) {
			h_M.append(curHours);
		} else {
			h_M.append("0" + curHours);
		}
		h_M.append(":");
		if (curMinutes > 9) {
			h_M.append(curMinutes);
		} else {
			h_M.append("0" + curMinutes);
		}

		return h_M;
	}

	public static StringBuilder getSpell_Y_M_D(int curYears, int curMonths,
			int curDays) {
		StringBuilder y_m_d = new StringBuilder();
		y_m_d.append(curYears);
		y_m_d.append("-");
		if (curMonths + 1 > 9) {
			y_m_d.append(curMonths + 1);
		} else {
			y_m_d.append("0" + (curMonths + 1));
		}
		y_m_d.append("-");
		if (curDays > 9) {
			y_m_d.append(curDays);
		} else {
			y_m_d.append("0" + curDays);
		}
		y_m_d.append(" ");
		return y_m_d;
	}

	public static int[] formateDate(String date) {
		int[] dates = new int[3];
		String[] dateStr = date.split("-");
		for (int i = 0; i < dateStr.length; i++)
			dates[i] = Integer.valueOf(dateStr[i]);
		return dates;
	}

	public static StringBuilder getSpell_M_D(int curMonths, int curDays) {
		StringBuilder y_m_d = new StringBuilder();

		if (curMonths + 1 > 9) {
			y_m_d.append(curMonths + 1);
		} else {
			y_m_d.append("0" + (curMonths + 1));
		}
		y_m_d.append("-");
		if (curDays > 9) {
			y_m_d.append(curDays);
		} else {
			y_m_d.append("0" + curDays);
		}
		y_m_d.append(" ");
		return y_m_d;
	}

	// 给定一个日期，返回指定格式的日期
	public static String formateDate2(Date date) {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		String formateDate = dateformat.format(date);
		return formateDate;
	}

	// 给定一个日期，返回指定格式的日期
	public static String formateDate3(Date date) {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String formateDate = dateformat.format(date);
		return formateDate;
	}

	/** :yyyy-MM-dd:HH:mm:ss */
	public static String getStringSS(String date) {
		String[] dateStr = date.split("\\.");
		return dateStr[0];
	}

	public static String[] getYMDString(String dateStr) {
		String[] ymd = dateStr.split("-");
		return ymd;

	}

	public static Date getStringToDate(String dateString) {
		Calendar calendar = Calendar.getInstance();
		if (dateString != null) {
			String[] ymdString = dateString.split("/");
			calendar.set(Integer.parseInt(ymdString[0]),
					Integer.parseInt(ymdString[1]) - 1,
					Integer.parseInt(ymdString[2]));
		}
		return calendar.getTime();

	}

	public static Date getStringToDate2(String dateString) {
		Calendar calendar = Calendar.getInstance();
		if (dateString != null) {
			String[] ymdString = dateString.split("-");
			calendar.set(Integer.parseInt(ymdString[0]),
					Integer.parseInt(ymdString[1]) - 1,
					Integer.parseInt(ymdString[2]));
		}
		return calendar.getTime();

	}

	public static int getIntWeek(String dateString) {
		Calendar calendar = Calendar.getInstance();
		if (dateString != null) {
			String[] ymdString = dateString.split("-");
			calendar.set(Integer.parseInt(ymdString[0]),
					Integer.parseInt(ymdString[1]) - 1,
					Integer.parseInt(ymdString[2]));
		}
		return calendar.get(Calendar.DAY_OF_WEEK);

	}

	// 获得当前日期与本周一相差的天数
	private static int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		// 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 1) {
			return -6;
		} else {
			return 2 - dayOfWeek;
		}
	}

	// 获得本周星期一的日期
	public static Calendar getCurrentMonday() {
		int mondayPlus = getMondayPlus();
		Calendar currentDate = Calendar.getInstance();
		currentDate.add(Calendar.DATE, mondayPlus);
		return currentDate;
	}

	// 获得下周星期一的日期
	public static Calendar getForWeekMonday(int week) {
		int mondayPlus = getMondayPlus();
		Calendar NextDate = Calendar.getInstance();
		NextDate.add(Calendar.DATE, mondayPlus + 7 * week);
		return NextDate;
	}

	// 获得相应周的周日的日期
	public static Calendar getSunday(int week) {
		int mondayPlus = getMondayPlus();
		Calendar currentDate = Calendar.getInstance();
		currentDate.add(Calendar.DATE, mondayPlus + 7 * week + 6);
		return currentDate;
	}

	public static int daysBetween(Date early, Date late) {
		Calendar calst = Calendar.getInstance();
		Calendar caled = Calendar.getInstance();
		calst.setTime(early);
		caled.setTime(late);
		// 设置时间为0时
		calst.set(Calendar.HOUR_OF_DAY, 0);
		calst.set(Calendar.MINUTE, 0);
		calst.set(Calendar.SECOND, 0);
		caled.set(Calendar.HOUR_OF_DAY, 0);
		caled.set(Calendar.MINUTE, 0);
		caled.set(Calendar.SECOND, 0);
		// 得到两个日期相差的天数
		int days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst
				.getTime().getTime() / 1000)) / 3600 / 24;
		return days;

	}
	
	// 获取过去时间点距离现在多久
	public static String getTimeInterval(String date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat df2 = new SimpleDateFormat("yyyy/MM/dd");
		DateFormat df3 = new SimpleDateFormat("MM/dd");
		Date now;
		Date beforeTime = null;
		String string = "";
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		int now_year = 0, before_year = 0;
		try {
			now = new Date();
			beforeTime = df.parse(date);
			now_year = now.getYear();
			before_year = beforeTime.getYear();
			long time1 = now.getTime();
			System.out.println("time1" + time1);
			long time2 = beforeTime.getTime();
			System.out.println("time2" + time2);
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			System.out.println("diff" + diff);
			day = diff / (24 * 60 * 60 * 1000);
			hour = diff / (60 * 60 * 1000);
			min = diff / (60 * 1000);
			sec = diff / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (sec >= 60) {
			if (min >= 60) {
				if (hour >= 24) {
					if (day > 2) {
						if (now_year == before_year) {
							string = df3.format(beforeTime);
						} else {
							string = df2.format(beforeTime);
						}
					} else {
						string = day + "天前";
					}

				} else {
					string = hour + "小时前";
				}
			} else {
				string = min + "分钟前";
			}
		} else {
			string = "刚刚";
		}

		return string;
	}
}
