package a2dv606.com.dv606hh222ixassignment3.WeatherWidget1.Assignment1Classes;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author jlnmsi
 *
 */
public class TimeUtils {

	public static Date getDate(String date_string) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = new Date();
		try {		
			date = format.parse(date_string);
		}
		catch (Exception e) {
			System.out.println("Time string: "+date_string);
			e.printStackTrace();
		}
		return date;
	}
	
	public static String getYYMMDD(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}
	
	public static String getHHMM(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		return format.format(date);
	}
}
