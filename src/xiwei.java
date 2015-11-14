
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class xiwei {

	private static Map<String, Integer> hotels;
	private static Map<String, Map<String, Integer>> bookings;

	public static void main(String[] args) {
		String checkIn = null;
		String checkOut = null;
		String hotelFile = null;
		String bookingFile = null;

		for (int i = 0; i < args.length; i++) {
			if (i + 1 >= args.length) {
				printHelpInfo("error: not enough parameters, missing " + args[i]);
				System.exit(1);
			}

			String arg = args[i++];
			if (arg.equals("--hotels")) {
				hotelFile = args[i];
				if (!new File(hotelFile).exists()) {
					printHelpInfo("error: " + hotelFile + " does not exist.");
					System.exit(1);
				}

			} else if (arg.equals("--bookings")) {
				bookingFile = args[i];
				if (!new File(bookingFile).exists()) {
					printHelpInfo("error: " + bookingFile + " does not exist.");
					System.exit(1);
				}
			} else if (arg.equals("--checkin")) {
				checkIn = args[i];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setLenient(false);
				try {
					sdf.parse(checkIn);
				} catch (Exception e) {
					printHelpInfo("error: invalid check in date " + checkIn);
					System.exit(1);
				}
			} else if (arg.equals("--checkout")) {
				checkOut = args[i];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setLenient(false);
				try {
					sdf.parse(checkOut);
				} catch (Exception e) {
					printHelpInfo("error: invalid check out date " + checkOut);
					System.exit(1);
				}
			} else {
				printHelpInfo("error: unknown option " + arg);
				System.exit(1);
			}
		}

		if (hotelFile == null || bookingFile == null || checkIn == null || checkOut == null) {
			printHelpInfo("error: not enough paramters.");
		}
		
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date checkInDate = df.parse(checkIn);
			Date checkOutDate = df.parse(checkOut);
			if (!checkInDate.before(checkOutDate)) {
				System.out.println("error: check in date should be before check out date");
				System.exit(1);
			}
		} catch (Exception e) {
			System.exit(1);
		}
		
		
		if (!loadHotels(hotelFile)) {
			System.out.println("error: loading " + hotelFile + " failed");
			System.exit(1);
		}

		if (!loadBookings(bookingFile)) {
			System.out.println("error: loading " + bookingFile + " failed");
			System.exit(1);
		}

		List<String> result = findAvailableHotels(checkIn, checkOut);
		
		Collections.sort(new ArrayList<String>());
		for (String hotel : result) {
			System.out.println(hotel);
		}
	}

	public static List<String> findAvailableHotels(String checkIn, String checkOut) {
		List<String> result = new ArrayList<String>();

		List<String> dates = findBookingDays(checkIn, checkOut);
		for (String hotelName : hotels.keySet()) {
			if (!bookings.containsKey(hotelName)) {
				result.add(hotelName);
				continue;
			}
			Map<String, Integer> map = bookings.get(hotelName);

			boolean flag = true;
			for (String date : dates) {
				if (!map.containsKey(date))
					continue;
				if (map.get(date) >= hotels.get(hotelName)) {
					flag = false;
					break;
				}
			}
			if (flag)
				result.add(hotelName);
		}

		return result;
	}
	
	private static void printHelpInfo(String info) {
		System.out.println(info);
		System.out.println("Usage: java #program --hotels #f1 --bookings #f2 --checkin #d1 --checkout #d2");
		System.out.println("\t#f1: absolute directory of hotel file\n\t#f2: absolute directory of booking file\n"
		                   + "\t#d1: check in date (format: yyyy-mm-dd)\n\t#d2: check out date (format: yyyy-mm-dd)");
	}

	private static List<String> findBookingDays(String checkIn, String checkOut) {
		List<String> result = new ArrayList<String>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sdf.parse(checkIn));

			String endDate = sdf.format(sdf.parse(checkOut));
			while (true) {
				String date = sdf.format(calendar.getTime());
				if (date.equals(endDate))
					break;
				result.add(date);
				calendar.add(Calendar.DATE, 1);
			}
		} catch (Exception e) {
			return new ArrayList<String>();
		}
		return result;
	}

	private static boolean loadBookings(String fileName) {
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(fileName));
			br.readLine();

			bookings = new HashMap<String, Map<String, Integer>>();
			while ((line = br.readLine()) != null) {
				String[] strs = line.split(","); 
				if (strs.length != 3)
					return false;
				if (!bookings.containsKey(strs[0]))
					bookings.put(strs[0], new HashMap<String, Integer>());

				Map<String, Integer> map = bookings.get(strs[0]);
				List<String> dateList = findBookingDays(strs[1], strs[2]);
				for (String date : dateList) {
					if (map.containsKey(date))
						map.put(date, map.get(date) + 1);
					else
						map.put(date, 1);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("error: " + fileName + " not found");
			return false;
		} catch (Exception e) {
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean loadHotels(String fileName) {
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(fileName));
			br.readLine();

			hotels = new HashMap<String, Integer>();
			while ((line = br.readLine()) != null) {
				String[] strs = line.split(",");
				if (strs.length != 2)
					return false;
				hotels.put(strs[0], Integer.parseInt(strs[1].replaceAll("\\s+", "")));
			}
		} catch (FileNotFoundException e) {
			System.out.println("error: " + fileName + " not found");
			return false;
		} catch (Exception e) {
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					return false;
				}
			}
		}

		return true;
	}
}
