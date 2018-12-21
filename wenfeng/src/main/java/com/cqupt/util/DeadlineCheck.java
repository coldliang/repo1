
package com.cqupt.util;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DeadlineCheck {
	
	/**
	 * check if the paper finished
	 */
	public static boolean check(String endDate){
		boolean flag = false;
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = format.format(date);
		int re = endDate.compareTo(time);
		if(re < 0) flag = true;
		return flag;
	}

}
