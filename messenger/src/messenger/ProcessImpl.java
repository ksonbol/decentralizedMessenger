package messenger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The process class has list of neighbors and has functionality for sending and 
 * receiving messages using RMI	
 * @author karim
 *
 */
public class ProcessImpl implements Process {
	/**
	 * Process ID string formatted as IP/PORT_NUMBER
	 */
	private String id;
	
	/**
	 * list of other process IDs
	 */
	private ArrayList<String> processes;
	
	/**
	 * index of the process in the process list. This is consistent across all processes
	 */
	private int index;
	/**
	 * location and name of group configuration file
	 */
	private String file = "peers.txt";
	
	/**
	 * Vector Clock of all processes in group
	 */
	private int[] vc;
	
	/**
	 * Process stub object for RMI operations
	 */
	private Process stub;
	
	
	/**
	 * Creates the process instance, setting its id
	 * @param id of process as IP/Port number
	 */
	
	/**
	 * Listener objects that listens to user input
	 */
	private Listener listener;
	
	public ProcessImpl(String id) {
		this.id = id;
		this.processes = new ArrayList<String>();
	}
	
	/**
	 * @return the neighbors
	 */
	public ArrayList<String> getProcesses() {
		return processes;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	private void setIndex(int index) {
		this.index = index;
	}

	/**
	 * 
	 * @return stored vector clock
	 */
	public int[] getVC() {
		return vc;
	}

	/**
	 * set vector clock to vc
	 * @param vc: given vector clock
	 */
	private void setVC(int[] vc) {
		this.vc = vc;
	}
	

	/**
	 * helper method to print vector clock
	 */
	private void printVC() {
		for (int e: getVC())
			System.out.print(e + " ");
		System.out.print("\n");
	}
	
	/**
	 * starts the process by reading the configuration file and starting listening threads
	 */
	public void start() {
		System.out.println("Reading configuration file...");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals(getId())) {
					setIndex(i);
				}
//				System.out.println(line);
				processes.add(line);
				i++;
			}
			reader.close();
			vc = new int[i]; // initialize vector clock to 0 for all processes
			System.out.println("Group has " + i + " members");
			System.out.println("Initial vector clock");
			printVC();
		} catch (IOException e) {
			System.out.println("Could not start. Check configuration file.");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Starting listening thread..");
		listener = new Listener(this);
		listener.start();
		System.out.println("Enter a message to send to the group.");
		System.out.print(getId() + ": ");
	}
	
	/**
	 * Create a new message object using the given transcript, attach sender id, and updated
	 * vector clock
	 * @param msg: transcript of the message
	 * @return the message object
	 */
	protected Message createMessage(String msg) {
		Message msgObj = new Message(msg, getId(), getVC());
		return msgObj;
	}
	
	private void send(Message msg, String pid) {
		try {
			Registry reg = LocateRegistry.getRegistry(Main.getIP(pid), Main.getPort(pid));
			stub = (Process) reg.lookup("Process");
			stub.messagePost(msg);
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Failed to do RMI with " + pid);
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	protected void multicast(Message msg) {
		for (int i=0; i<processes.size(); i++) {
			if (i != getIndex()) {
				send(msg, processes.get(i));
			}
		}
	}

	@Override
	public void messagePost(Message msg) throws RemoteException {
		System.out.println("\n" + msg.toString());
		System.out.print(getId() + ": ");
	}
}

class Listener implements Runnable {
	/**
	 * Corresponding process object
	 */
	private ProcessImpl process;
	
	/**
	 * Thread object that will listen to user messages
	 */
	private Thread th;
	
	/**
	 * Scanner object that reads user messages
	 */
	private Scanner sc;
	
	/**
	 * msg entered by user
	 */
	private String msg;
	
	/**
	 * Constructor, stores the process object and creates a new thread
	 * @param process
	 */
	
	public Listener(ProcessImpl process) {
		this.process = process;
		th = new Thread(this);
		sc = new Scanner(System.in);
	}
	
	public void start() {
		th.start();
	}
	
	@Override
	public void run() {
		while(true) {
			msg = sc.nextLine().trim();
			System.out.print(process.getId() + ": ");
			if (!msg.isEmpty()) {
				Message msgObj = process.createMessage(msg);
				process.multicast(msgObj);
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}
}
