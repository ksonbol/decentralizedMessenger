package messenger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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
	private String[] processes;
	
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
	 * Stub this process is exporting to other processes
	 */
	private Process exportedStub;
	
	/**
	 * Local registry used for exporting objects
	 */
	private Registry localReg;
	
	/**
	 * Creates the process instance, setting its id
	 * @param id of process as IP/Port number
	 */
	public ProcessImpl(String id) {
		this.id = id;
	}
	
	/**
	 * @return the neighbors
	 */
	public String[] getProcesses() {
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
	
	public String getIP() {
		return id.split("/")[0];
	}
	
	public int getPort() {
		return Integer.parseInt(id.split("/")[1]);
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
	}
	
	/**
	 * starts the process by reading the configuration file and starting listening threads
	 */
	public void start() {
		System.out.println("Starting process..");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals(getId())) {
					setIndex(i);
				}
				System.out.println(line);
				processes[i] = line.trim();
				i++;
			}
			reader.close();
			vc = new int[i]; // initialize vector clock to 0 for all processes
			System.out.println("Group has " + i + " members");
			System.out.println("Initial vector clock");
		} catch (IOException e) {
			System.out.println("Could not start. Check configuration file.");
			System.exit(0);
		}
		try {
			exportedStub = (Process) UnicastRemoteObject.exportObject(this, getPort());
			localReg = LocateRegistry.getRegistry(getPort());
			localReg.bind("Process", exportedStub);
		} catch (RemoteException | AlreadyBoundException e) {
			System.out.println("Failed to export object with RMI");
			System.exit(0);
		}
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
	
	protected void send(Message msg) {
		for (int i=0; i<processes.length; i++) {
			if (i != getIndex()) {
				try {
					Registry reg = LocateRegistry.getRegistry(getIP(), getPort());
					stub = (Process) reg.lookup("Process");
					stub.messagePost(msg);
				} catch (RemoteException | NotBoundException e) {
					System.out.println("Failed to do RMI with " + processes[i]);
					System.exit(0);
				}
			}
		}
	}

	@Override
	public void messagePost(Message msg) throws RemoteException {
		System.out.println(msg.toString());
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
	 * Scanner object tha reads user messages
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
			if (!msg.isEmpty()) {
				Message msgObj = process.createMessage(msg);
				process.send(msgObj);
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}
}
