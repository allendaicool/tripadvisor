import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HotelBookingApp {

	private static final String hotelArg = "--hotels";
	private static final String bookingArg = "--bookings";
	private static final String checkInArg = "--checkin";
	private static final String checkOutArg = "--checkout";
	private static final String errorSignal = "input error: ";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static Calendar c = Calendar.getInstance();
	
	public static void main(String[] args)
	{
		sdf.setLenient(false);
		String checkInDate = null;
		String checkOutDate = null;
		String hotelFile = null;
		String bookingFile = null; 
		boolean checkInDateChecked = false;
		boolean checkOutDateChecked = false;
		boolean hotelFileChecked = false;
		boolean bookingFileChecked = false;
		
		//hoteMapping keep hotel and its number of rooms as key-value pair
		Map<String, Integer> hotelMapping = new HashMap<String, Integer>();
		
		//hotelBookingMapping keeps hotel and mapping of date to number of rooms reserved
		// e.x <hilton, <2010-05-6, 6>>  indicates that on 2010-05-06, hilton has 6 rooms that is already reserved
		Map<String, Map<String, Integer>> hotelBookingMapping = new HashMap<String, Map<String, Integer>>();
		int i = 0;
		while (i < args.length)
		{
			String argumentName = args[i];
			if (++i == args.length)
			{
				System.out.println(errorSignal + args[i] + " argument is missing");
				System.exit(-1);
			}
			if (argumentName.equals(hotelArg))
			{
				if (hotelFileChecked)
				{
					System.out.println(errorSignal + "duplicate argument " + hotelArg);
				}
				hotelFileChecked = true;
				hotelFile = args[i];
				if (!checkFileExist(hotelFile))
				{
					System.exit(-1);
				}
			}
			else if (argumentName.equals(bookingArg))
			{
				if (bookingFileChecked)
				{
					System.out.println(errorSignal + "duplicate argument " + bookingArg);
				}
				bookingFileChecked = true;
				bookingFile = args[i];
				if (!checkFileExist(bookingFile))
				{
					System.exit(-1);
				}
			}
			else if (argumentName.equals(checkInArg))
			{
				if (checkInDateChecked)
				{
					System.out.println(errorSignal + "duplicate argument " + checkInArg);
				}
				checkInDateChecked = true;
				checkInDate = args[i];
				if (!checkDateValid(checkInDate))
				{
					System.out.println(errorSignal + checkInDate + " is not valid date");
					System.exit(-1);
				}
			}
			else if (argumentName.equals(checkOutArg))
			{
				if (checkOutDateChecked)
				{
					System.out.println(errorSignal + "duplicate argument " + checkOutArg);
				}
				checkOutDateChecked = true;
				checkOutDate = args[i];
				if (!checkDateValid(checkOutDate))
				{
					System.out.println(errorSignal + checkOutDate + " is not valid date");
					System.exit(-1);
				}
			}
			else
			{
				System.out.println(errorSignal + " invalid argument " + argumentName);
				System.exit(-1);
			}
			i++;
		}
		if (checkInDate == null || checkOutDate == null || bookingFile == null || hotelFile == null)
		{
			System.out.println("some argument is missing");
			System.exit(-1);
		}
		if (checkInDate.compareTo(checkOutDate)  >= 0)
		{
			System.out.println(errorSignal + " checkIn date must be before checkOut date");
			System.exit(-1);
		}
		if (!parseHotelFile(hotelFile, hotelMapping))
		{
			System.exit(-1);
		}
		if (!parseHotelRoomFile(bookingFile, hotelBookingMapping, hotelMapping))
		{
			System.exit(-1);
		}
		List<String> ret = getAvailableHotel(checkInDate, checkOutDate, hotelBookingMapping, hotelMapping);
		for (String tmp : ret)
		{
			System.out.println("hotel is " + tmp);
		}
	}

	/**
	 * This method query the booking status of each hotel to get list of hotels that is available during the
	 * period specified by user
	 * @param checkInDate guest checked in date
	 * @param checkOutDate guest checked out date
	 * @param hotelBookingMapping hotel and its booking information map
	 * @param hotelMapping hotel and its number of rooms map
	 * @return list of available hotel during the period
	 */
	private static List<String> getAvailableHotel(String checkInDate, String checkOutDate, 
			Map<String, Map<String, Integer>> hotelBookingMapping, Map<String, Integer> hotelMapping)
	{
		List<String> ret = new ArrayList<String>();
		for (Map.Entry<String, Integer> hotelNumEntry : hotelMapping.entrySet())
		{
			if (!hotelBookingMapping.containsKey(hotelNumEntry.getKey()))
			{
				ret.add(hotelNumEntry.getKey());
				continue;
			}
			boolean addIn = true;
			try {
				c.setTime(sdf.parse(checkInDate));
				while (!sdf.format(c.getTime()).equals(checkOutDate))
				{
					String date = sdf.format(c.getTime());
					Map<String, Integer> dateMapping = hotelBookingMapping.get(hotelNumEntry.getKey());
					if (!dateMapping.containsKey(date))
					{
						c.add(Calendar.DATE, 1);
						continue;
					}
					if (dateMapping.get(date) >= hotelNumEntry.getValue())
					{
						addIn = false;
						break;
					}
					c.add(Calendar.DATE, 1);
				}
			}
			catch (ParseException e) {
				System.out.println(errorSignal + " parseing execution fails.");
				return null;
			}
			if (addIn)
			{
				ret.add(hotelNumEntry.getKey());
			}
		}
		return ret;
	}
	
	/**
	 * Helper method used to record the booking information for a particular hotel
	 * @param checkInDate checked in date in booking.csv 
	 * @param checkOutDate checked out date in booking.csv 
	 * @param dateBooking booking information for a particular hotel
	 * @return true if there is no input error, false otherwise
	 */
	private static boolean fulfillBookingMap (String checkInDate, String checkOutDate, 
			Map<String, Integer> dateBooking)
	{
		try {
			c.setTime(sdf.parse(checkInDate));

			while (!sdf.format(c.getTime()).equals(checkOutDate))
			{
				String date = sdf.format(c.getTime());
				if (!dateBooking.containsKey(date))
				{
					dateBooking.put(date, 1);
				}
				else
				{
					dateBooking.put(date, dateBooking.get(date) + 1);
				}
				c.add(Calendar.DATE, 1);
			}
		}
		catch (ParseException e) {
			System.out.println(errorSignal + " parseing execution fails.");
			return false;
		}
		
		return true;
	}

	/**
	 * This method read the metropolis_bookings.csv file and keep down the number of rooms reserved for a specific 
	 * date associated with a specific hotel
	 * @param fileName metropolis_bookings.csv
	 * @param hotelBookingMapping booking associated with hotel mapping
	 * @param hotelMapping hotel and number of room pair
	 * @return
	 */
	private static boolean parseHotelRoomFile(String fileName, Map<String, Map<String, Integer>> hotelBookingMapping,
			Map<String, Integer> hotelMapping)
	{
		String line = null;
		BufferedReader bufferedReader = null;
		try {
			FileReader fileReader = new FileReader(fileName);

			bufferedReader = new BufferedReader(fileReader);
			
			line = bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) 
			{
		
				line = line.trim();
				line = line.replaceAll("\\s+", "");
				String[] hotelNum = line.split(",");
				if (hotelNum.length != 3)
				{
					System.out.println("error here");
					return false;
				}
				if (!hotelMapping.containsKey(hotelNum[0]))
				{
					System.out.println(errorSignal + " hotel " + hotelNum[0] + " is not in hotel.csv file");
					return false;
				}
				if (!hotelBookingMapping.containsKey(hotelNum[0]))
				{
					hotelBookingMapping.put(hotelNum[0], new HashMap<String, Integer>());
				}
				String checkInDate = hotelNum[1];
				String checkOutDate = hotelNum[2];
				if (!fulfillBookingMap(checkInDate, checkOutDate, hotelBookingMapping.get(hotelNum[0])))
				{
					return false;
				}
			}   
		}
		catch(FileNotFoundException ex) 
		{
			System.out.println("Unable to open file '" +  fileName + "'");                
		}
		catch(IOException ex) 
		{
			System.out.println( "Error reading file '" + fileName + "'");                  
		}
		finally 
		{
			if (bufferedReader != null) 
			{
				try 
				{
					bufferedReader.close();
				} catch (Exception e) 
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * This method read the given metropolis_hotels.csv file and keep hotel and number of rooms pair
	 * in hashmap
	 * @param fileName metropolis_hotels.csv
	 * @param hotelMapping hotel and number of rooms pair
	 * @return true if there is no file read error, false otherwise
	 */
	private static boolean parseHotelFile(String fileName, Map<String, Integer> hotelMapping)
	{
		String line = null;
		BufferedReader bufferedReader = null;
		try {
			FileReader fileReader = new FileReader(fileName);

			bufferedReader = new BufferedReader(fileReader);
			line = bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) 
			{
				line = line.trim();
				line = line.replaceAll("\\s+", "");
				String[] hotelNum = line.split(",");
				if (hotelNum.length != 2)
				{
					return false;
				}
				if (!hotelMapping.containsKey(hotelNum[0]))
				{
					try
					{
						hotelMapping.put(hotelNum[0], Integer.parseInt(hotelNum[1]));
					}
					catch (NumberFormatException excep)
					{
						System.out.println(errorSignal + " hotel room number for " + hotelNum[0] + " is not valid" );
						return false;
					}
				}
			}   
		}
		catch(FileNotFoundException ex) {
			System.out.println(
					"Unable to open file '" + 
							fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println(
					"Error reading file '" 
							+ fileName + "'");                  
		}
		finally 
		{
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * This method checks if date matches the yyyy-mm-dd expression
	 * @param date input by user
	 * @return true if date matches the format
	 * false otherwise
	 */
	private static boolean checkDateValid(String date)
	{
		if (!date.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})"))
			return false;
		try 
		{
			sdf.parse(date);
			return true;
		} catch (ParseException ex) 
		{
			return false;
		}
	}

	/**
	 * This method checks if such file exists
	 * @param fileName file name to be checked
	 * @return true if file exist, false otherwise
	 */
	private static boolean checkFileExist(String fileName)
	{
		File f = new File(fileName);
		if(!f.exists() || f.isDirectory()) 
		{ 
			System.out.println("input error: file " + fileName + " does not exist");
			return false;
		}
		return true;
	}
}
