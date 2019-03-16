package messenger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The process class has list of neighbors and has functionality for sending and 
 * receiving messages using RMI
 * @author karim
 *
 */
public class Process {
	/**
	 * Process ID string formatted as IP/PORT_NUMBER
	 */
	private String id;
	
	/**
	 * list of other process IDs
	 */
	private String[] neighbors;
	
	/**
	 * location and name of group configuration file
	 */
	private String file = "peers.txt";
	
	/**
	 * Creates the process instance, setting its id
	 * @param id of process as IP/Port number
	 */
	public Process(String id) {
		this.id = id;
	}
	
	/**
	 * starts the process by reading the configuration file and starting listening threads
	 */
	public void start() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			line = reader.readLine().trim();
			int i = 0;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				neighbors[i] = line.trim();
				i++;
			}
				reader.close();
		} catch (IOException e) {
			System.out.println("Could not start. Check configuration file.");
		}
	}
	/**
	 * @return the neighbors
	 */
	public String[] getNeighbors() {
		return neighbors;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	

}
