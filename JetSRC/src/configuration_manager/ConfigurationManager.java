// This class handles all configuration file import and quality control

package configuration_manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import utils.StdOut;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class ConfigurationManager {
	
	// CONSTANTS
	
	final static String AIRCRAFT_TYPES_FILE = "src/configuration_files/aircraft_types.txt";
	final static String STORES_CODES_FILE = "src/configuration_files/store_codes.txt";
	final static String AIRCRAFT_CONFIGURATIONS_FILE = "src/configuration_files/aircraft_configurations.txt";
	
	// CLASS INVARIANTS
	
	// C1 (aircraftTypes) : aircraftTypes contains the list of supported aircraft types 
	public ArrayList<String> aircraftTypes = new ArrayList<>();
	// C2 (aircraftConfigs) : aircraftConfigs contains the configuration file for each aircraft
	public HashMap<String, ArrayList<Integer>> aircraftConfigs = new HashMap<>();
	// C3 (storesCodes) : storesCodes contains all of the allowed stores for the program and their details
	public HashMap<Integer, String> storesCodes = new HashMap<>();
	
	public ConfigurationManager() {}
	
	// ConfigFileFormatExtension is a exception class to help deal with the complex formatting required in the configuration files.
	private class ConfigFileFormatException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		// ConfigFileException constructor
		// precondition : two strings are passed to the exception
		// postcondition 1 : a date stamped error log is saved 
		// postcondition 2 : a string is printed on the console
		ConfigFileFormatException(String file, String error){
			super(error);
			try (PrintWriter outfilepw = new PrintWriter("src/configuration_files/error_log.txt");) 
		    {  
				Date myDate = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
				String myDateString = sdf.format(myDate);
				outfilepw.format("Config File Error at %s at file %s with error %s", myDateString, file, error); // postcondition 1
		                 
		    }
		    catch (FileNotFoundException ex)
		    {
		        ex.printStackTrace();
		    }
			StdOut.println("Formatting excpetion in the "+ file +" configuration file:"); // postcondition 2
			StdOut.println(error);
		}
	}
	
	// helper method to call all sub methods and catch errors
	public void setup() {
		// setup aircraft types file
		try {
			this.getAircraftTypes();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigFileFormatException e) {
			e.printStackTrace();
		}
		try {
			this.configureWeaponCodes();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigFileFormatException e) {
			e.printStackTrace();
		}
		try {
			this.configureAircraft();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ConfigFileFormatException e) {
			e.printStackTrace();
		}
	}
	
	// getAircraftMethod - retrieves the aircraft types from a file
	// Precondition : a correctly formatted file is saved at AIRCRAFT_TYPES_FILE
	// Postcondition : C1 (aircraftTypes) is filled
	private void getAircraftTypes() throws FileNotFoundException, ConfigFileFormatException {
		Scanner infile;
		infile = new Scanner(new File(AIRCRAFT_TYPES_FILE)); // precondition (throws FileNotFoundException)
		while (infile.hasNext())
	       {         
			aircraftTypes.add(infile.next());
	       }

	    infile.close();
	}
	
	// configureWeaponCodes - retrieves the weapon codes from a file
	// Precondition : a correctly formatted file is saved at STORES_CODES_FILE
	// Postcondition : C3 (storesCodes) is filled
	private void configureWeaponCodes() throws FileNotFoundException, ConfigFileFormatException{ // TODO add release limits
		Scanner infile;
		
			infile = new Scanner(new File(STORES_CODES_FILE)); // precondition (throws FileNotFoundException)
			while (infile.hasNextLine())
		       {         
				String line = infile.nextLine();
				String[] list = line.split("\\|");
				if (list.length == 4) { // check for incorrect file format 1 (incorrect delimiter)
					try {
						storesCodes.put(Integer.valueOf(list[0]), ""+list[1] + "|" +list[2]+ "|"+list[3]+""); // Postcondition (throws ConfigFileFormatException)
					} catch (NumberFormatException e) { // check for incorrect file format 2 (first value not number)
						infile.close();
						throw new ConfigFileFormatException(
								STORES_CODES_FILE, 
								"Make sure you the first value on the line is a unique number"
								);
					}
					
				} else { // incorrect file format 1
					infile.close();
					throw new ConfigFileFormatException(
							STORES_CODES_FILE, 
							"Make sure you have 4 values for the store delimited by |"
							);
				}
		       }

		       infile.close();
	}

	// configureWeaponCodes - retrieves the weapon codes from a file
	// Precondition : a correctly formatted file is saved at STORES_CODES_FILE
	// Postcondition : C3 (storesCodes) is filled
	private void configureAircraft() throws FileNotFoundException, ConfigFileFormatException{
		Scanner infile;
		
			infile = new Scanner(new File(AIRCRAFT_CONFIGURATIONS_FILE));
			while (infile.hasNextLine())
		       {
				String line = infile.nextLine();
				String[] list = line.split(" ");
				ArrayList<Integer> config = new ArrayList<>();
				if (aircraftTypes.contains(list[0])) { // check aircraft is in aircraft list
					try {
						for (int i = 1; i < list.length; i++) {
							Integer integer = Integer.valueOf(list[i]);
							config.add(integer);
						}
					} catch (NumberFormatException e) {
						infile.close();
						config.clear();
						throw new ConfigFileFormatException(
								AIRCRAFT_CONFIGURATIONS_FILE, 
								"Error in the file format, make sure all items after aircraft name are numbers."
								);
					}
					aircraftConfigs.put(list[0], config);
					
				} else {
					infile.close();
					throw new ConfigFileFormatException(
							AIRCRAFT_CONFIGURATIONS_FILE, 
							"Could not find corresponding aircraft in AIRCRAFT_CONFIGURATION_FILE."
							);
				}
	       }

	       infile.close();
		
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Aircraft \n");
		this.aircraftTypes.forEach(s -> stringBuilder.append(s));
		stringBuilder.append("\n Weapons \n");
		this.storesCodes.forEach((k,v) -> stringBuilder.append("weapon code - key: "+k+" value:"+v));
		stringBuilder.append("\n Configs");
		this.aircraftConfigs.forEach((k,v) -> stringBuilder.append("aircraft config - key: "+k+" value:"+v));
		return stringBuilder.toString();
	}
	
	public static void main(String[] args) {
		ConfigurationManager fileImporter = new ConfigurationManager();
		fileImporter.setup();
		fileImporter.aircraftTypes.forEach(s -> System.out.println(s));
		fileImporter.storesCodes.forEach((k,v) -> System.out.println("weapon code - key: "+k+" value:"+v));
		fileImporter.aircraftConfigs.forEach((k,v) -> System.out.println("aircraft config - key: "+k+" value:"+v));
		
	}
}