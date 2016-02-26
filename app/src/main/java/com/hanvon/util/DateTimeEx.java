package com.hanvon.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间日期功能扩展类 注：(**代表次幂)<br/>
 * millisecond 1ms (毫秒) 1毫秒=0.001秒=10**-3秒 <br/>
 * microsecond 1μs (微秒) 1微秒=0.000001=10**-6秒 <br/>
 * nanosecond 1ns (纳秒) 1纳秒=0.0000000001秒=10**-9秒 <br/>
 * picosecond 1ps (皮秒) 1皮秒=0.0000000000001秒=10**-12秒 <br/>
 * femtosecond 1fs (飞秒) 1飞秒=0.000000000000001秒=10**-15秒<br/>
 * <br/>
 */
public class DateTimeEx {
	
	private static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 计算两个日期相间隔的“毫秒”数
	 * 
	 * @param oneDate
	 *            第一个日期参数
	 * @param anotherDate
	 *            第二个日期参数
	 * @return 两个日期相间隔的“毫秒”数（两个日期不区分先后）
	 */
	static public long millisecondsBetween(Date oneDate, Date anotherDate) {
		Date lowDate;
		Date highDate;
		if (oneDate.getTime() > anotherDate.getTime()) {
			lowDate = clearTime(anotherDate);
			highDate = clearTime(oneDate);
		} else {
			lowDate = clearTime(oneDate);
			highDate = clearTime(anotherDate);
		}

		return highDate.getTime() - lowDate.getTime();
	}

	/**
	 * 计算两个日期相间隔的“天”数
	 * 
	 * @param oneDate
	 *            第一个日期参数
	 * @param anotherDate
	 *            第二个日期参数
	 * @return 两个日期相间隔的“天”数（两个日期不区分先后）
	 */
	static public int daysBetween(Date oneDate, Date anotherDate) {
		return DateTimeEx.millisecondsToDays(millisecondsBetween(oneDate, anotherDate));
	}

	/**
	 * 计算两个日期相间隔的“分钟”数
	 * 
	 * @param oneDate
	 *            第一个日期参数
	 * @param anotherDate
	 *            第二个日期参数
	 * @return 两个日期相间隔的“分钟”数（两个日期不区分先后）
	 */
	static public int minutesBetween(Date oneDate, Date anotherDate) {
		return DateTimeEx.millisecondsToMinutes(millisecondsBetween(oneDate, anotherDate));
	}

	/**
	 * 将相间隔的毫秒值转化为”分钟“数
	 * 
	 * @param intervalMs
	 *            相间隔的毫秒值
	 * @return 相间隔的”分钟“数
	 */
	static public int millisecondsToMinutes(long intervalMs) {
		if (intervalMs < 0) {
			intervalMs = 0 - intervalMs;
		}
		/* todo:modify */
		return (int) (intervalMs / (1000 * 60));
	}

	/**
	 * 将相间隔的毫秒值转化为”天“数
	 * 
	 * @param intervalMs
	 *            相间隔的毫秒值
	 * @return 相间隔的”天“数
	 */
	static public int millisecondsToDays(long intervalMs) {
		if (intervalMs < 0) {
			intervalMs = 0 - intervalMs;
		}
		return (int) (intervalMs / (1000 * 86400));
	}

	/**
	 * 清除日期中的时间部分（即将时间设置为午夜0点）
	 * 
	 * @param d
	 *            需要清除的日期
	 * @return 返回清除时间部分的日期对象。
	 */
	static public Date clearTime(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTime();
	}

	/**
	 * 清空时间中的秒
	 * 
	 * @param d
	 * @return
	 */
	static public Date clearSecond(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * 设定时间对象中的时间部分
	 * 
	 * @param d
	 *            需要设置的日期对象
	 * @return 返回设置了时间部分的日期对象。
	 */
	static public Date setTime(Date d, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTime();
	}

	/**
	 * 设定时间对象中的日期部分
	 * 
	 * @param d
	 *            需要设置的日期对象
	 * @return 返回设置了日期部分的日期对象。
	 */
	static public Date setDate(Date d, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		return c.getTime();
	}

	/**
	 * 设定时间对象中的日期部分和时间
	 * 
	 * @param d
	 *            需要设置的日期对象
	 * @return 返回设置了日期部分和时间的日期对象。
	 */
	static public Date setDateTime(Date d, int year, int month, int day, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * 取得给定的时间距离午夜0点的毫秒数
	 * 
	 * @param d
	 *            需要计算的日期时间
	 * @return 给定的时间距离午夜0点的毫秒数
	 */
	static public long millisecondsOffsetMidnight(Date d) {
		Date d2 = clearTime(d);
		return d.getTime() - d2.getTime();
	}

	/**
	 * 获取两个日期间的工作日天数
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	static public long workDaysBetween(Date startTime, Date endTime) {
		Calendar begin = Calendar.getInstance();
		begin.setTime(startTime);
		Calendar end = Calendar.getInstance();
		begin.setTime(endTime);
		end.add(Calendar.DAY_OF_YEAR, 1);
		long days = 0;
		while (begin.before(end)) {
			if (begin.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
					|| begin.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				begin.add(Calendar.DAY_OF_YEAR, 1);
				continue;
			}
			days++;
			begin.add(Calendar.DAY_OF_YEAR, 1);
		}
		return days;
	}

	/**
	 * 设置一天中的最早时间即0点0分0秒
	 * 
	 * @param d
	 * @return
	 */
	static public Date setBeginTimeOfDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTime();
	}

	/**
	 * 设置一天中最晚的时间即23点59分59秒
	 * 
	 * @param d
	 * @return
	 */
	static public Date setEndTimeOfDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);

		return c.getTime();
	}

	/**
	 * 取date的日期和time的时间组成一个新的日期返回
	 * 
	 * @param date
	 * @param time
	 * @return
	 */
	static public Date mergeDates(Date date, Date time) {
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		Calendar ct = Calendar.getInstance();
		ct.setTime(time);

		cd.set(Calendar.HOUR_OF_DAY, ct.get(Calendar.HOUR_OF_DAY));
		cd.set(Calendar.MINUTE, ct.get(Calendar.MINUTE));
		cd.set(Calendar.SECOND, ct.get(Calendar.SECOND));

		return cd.getTime();
	}

	/**
	 * 判断date是否在startDate和endDate之间
	 * 
	 * @param date
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	static public boolean isDateBetween(Date date, Date startDate, Date endDate) {
		if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0) {
			return true;
		}
		return false;
	}

	static public String getYearAndMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String year = calendar.get(Calendar.YEAR) + "";
		String month = "";
		if ((calendar.get(Calendar.MONTH) + 1) < 10) {
			month += "0" + (calendar.get(Calendar.MONTH) + 1);
		} else {
			month += (calendar.get(Calendar.MONTH) + 1);
		}
		return year + month;
	}

	static public String getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String year = calendar.get(Calendar.YEAR) + "";
		return year;
	}

	static public List<Date> getDatesFromPeriod(Date startDate, Date endDate) {
		startDate = setBeginTimeOfDay(startDate);
		endDate = setBeginTimeOfDay(endDate);
		List<Date> dateList = new ArrayList<Date>();
		if (startDate.compareTo(endDate) == 1) {
			return dateList;
		}
		while (startDate.compareTo(endDate) == -1) {
			dateList.add(DateTimeEx.setBeginTimeOfDay(startDate));
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTime(startDate);
			startCalendar.add(Calendar.DAY_OF_YEAR, 1);
			startDate = startCalendar.getTime();
		}
		dateList.add(DateTimeEx.setBeginTimeOfDay(endDate));
		return dateList;
	}

	/**
	 * 由年月信息获取某月的第一天
	 * 
	 * @param month
	 * @return
	 */
	static public Date getFirstDayOfMonth(String month) {
		String y = month.substring(0, 4);
		String m = month.substring(4);
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.set(Integer.parseInt(y), Integer.parseInt(m) - 1, 1);
		return setBeginTimeOfDay(startCalendar.getTime());

	}

	/**
	 * 由年月信息获取某月的最后一天
	 * 
	 * @param month
	 * @return
	 */
	static public Date getLastDayOfMonth(String month) {
		String y = month.substring(0, 4);
		String m = month.substring(4);
		Calendar endCalendar = Calendar.getInstance();
		if (m.equals("12")) {
			endCalendar.set(Integer.parseInt(y) + 1, 0, 1);
		} else {
			endCalendar.set(Integer.parseInt(y), Integer.parseInt(m), 1);
		}
		endCalendar.add(Calendar.DAY_OF_YEAR, -1);
		return setEndTimeOfDay(endCalendar.getTime());
	}

	/**
	 * 计算两个日期相间隔的“毫秒”数 不清空日期的时间，分，秒
	 */
	static public float getMillisecondsBetween(Date oneDate, Date anotherDate) {
		Date lowDate;
		Date highDate;
		if (oneDate.getTime() > anotherDate.getTime()) {
			lowDate = anotherDate;
			highDate = oneDate;
		} else {
			lowDate = oneDate;
			highDate = anotherDate;
		}

		return highDate.getTime() - lowDate.getTime();
	}

	/**
	 * 计算两个日期相间隔的“分钟”数 不清空日期时间
	 */
	static public int getMinutesBetween(Date oneDate, Date anotherDate) {
		return (int) Math.ceil(DateTimeEx.millisecondsToMinutes(getMillisecondsBetween(oneDate, anotherDate)));
	}

	/**
	 * 将相间隔的毫秒值转化为”分钟“数
	 * 
	 * @param intervalMs
	 *            相间隔的毫秒值
	 * @return 相间隔的”分钟“数
	 */
	static public float millisecondsToMinutes(float intervalMs) {
		if (intervalMs < 0) {
			intervalMs = 0 - intervalMs;
		}
		/* todo:modify */
		return intervalMs / (1000 * 60);
	}
	
	/**
	 * 返回 2011-01-01类型的string
	 * @param date
	 * @return
	 */
	public static String getDateStr(Date date){
		return formater.format(date);
	}
	
	/**
	 * 返回 2011-01-01 19:00:10类型的string
	 * @param date
	 * @return
	 */
	public static String getDateTimeStr(Date date){
		return formatDateTime.format(date);
	}
}
