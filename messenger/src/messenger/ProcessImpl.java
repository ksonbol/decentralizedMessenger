package messenger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The process class has list of neighbors and has functionality for sending and
 * receiving messages using RMI
 * 
 * @author Karim, Ilyas
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
	 * list of messages in the queue to be displayed (for causal ordering)
	 */
	private ArrayList<Message> messageQueue;

	/**
	 * index of the process in the process list. This is consistent across all
	 * processes
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
	 * 
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
	 * 
	 * @return stored vector clock as a String
	 */
	public String getVCString() {
		return getVCString(getVC());
	}

	/**
	 * 
	 * @return a given vector clock as a String
	 */
	public String getVCString(int[] vc) {
		String res = "[";
		for (int e : vc)
			res += e + " ";
		return res.substring(0, res.length()-1) + "]";
	}
	
	/**
	 * increment vector clock
	 */
	private void incrementVC() {
		this.vc[index] = this.vc[index] + 1;
	}

	/**
	 * helper method to print vector clock
	 */
	private void printVC() {
		System.out.print("Updated VC: ");
		System.out.println(getVCString());
		printReady();
	}

	
	/**
	 * initialize the VC array to zeros
	 * @param count: number of processes in the group
	 */
	private void initializeVC(int count) {
		vc = new int[count];
		for (int i=0; i<count; i++) {
			vc[i] = 0;
		}
	}
	
	/**
	 * print the ready for writing line to user
	 */
	protected void printReady() {
		System.out.print(" (P" + getIndex() + "): ");
	}
	
	/**
	 * starts the process by reading the configuration file and starting listening
	 * threads
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
					System.out.println("My index is: " + i);
				}
				processes.add(line);
				i++;
			}
			reader.close();
			initializeVC(i);
			System.out.println("Group has " + i + " members");
			System.out.println("Initial vector clock");
			printVC();
		} catch (IOException e) {
			System.out.println("Could not start. Check configuration file.");
			e.printStackTrace();
			System.exit(0);
		}
		messageQueue = new ArrayList<Message>();
		System.out.println("Starting listening thread..");
		listener = new Listener(this);
		listener.start();
		System.out.println("Enter a message to send to the group.");
		printReady();
	}

	/**
	 * Create a new message object using the given transcript, attach sender id, and
	 * updated vector clock
	 * 
	 * @param msg: transcript of the message
	 * @return the message object
	 */
	protected Message createMessage(String msg) {
		incrementVC();
		printVC();
		Message msgObj = new Message(msg, getId(), getVC(), getIndex());
		return msgObj;
	}

	/**
	 * send message to process with ID = pid. This is achieved by RMI.
	 * @param msg: Message object to be sent.
	 * @param pid: id of receiver process
	 */
	private void send(Message msg, String pid) {
//		System.out.println("sending message to " + pid);
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

	/**
	 * send message to all other processes in the group, using RMI
	 * @param msg
	 */
	protected void multicast(Message msg) {
		for (int i = 0; i < processes.size(); i++) {
			if (i != getIndex() && !processes.get(i).contains(getId())) {
				send(msg, processes.get(i));
				// USE FOR TESTING causality
//				if (getIndex() == 0) {
//					try { Thread.sleep(5000); } catch (InterruptedException e) {}
//				 }
			}
		}
	}

	/**
	 * Check all messages in the queue, deliver any that satisfy the causal-ordering requirement
	 */
	private void releaseQueue() {
		System.out.println("queue release " + messageQueue.size());
		printVC();
		Message msg = null;
		boolean removedAny = false;
		for (int i=0; i<messageQueue.size(); i++) {
			msg = messageQueue.get(i);
			int senderInd = msg.getIndex();
			boolean toRemove = true;
			if (msg.getVC()[senderInd] == (getVC()[senderInd] + 1)) {
				for (int j = 0; j < getVC().length; j++) {
					if ((j != senderInd) && (msg.getVC()[j] > getVC()[j])) {
						toRemove = false;
						break;
					}
				}
				if (toRemove) {
					deliverMsg(msg);
					messageQueue.remove(i);
					removedAny = true;
				}
			}
		}
		if (removedAny && !messageQueue.isEmpty()) {
			releaseQueue(); // other messages may be deliverable now
		}
	}

	/**
	 * display message to the user
	 * @param msg: Message object
	 */
	private void deliverMsg(Message msg) {
		System.out.println("\n" + msg.toString());
		printReady();
		vc[msg.getIndex()] += 1;
	}
	
	/**
	 * This method is used by remote processes on Process stubs using RMI. It delivers a message
	 * to the user if it satisfies the causality requirements. Otherwise, it adds the message to
	 * the queue.
	 */
	@Override
	public void messagePost(Message msg) throws RemoteException {
		int senderInd = msg.getIndex();
		if (msg.getVC()[senderInd] != (getVC()[senderInd] + 1)) {
			messageQueue.add(msg);
			return;
		}
		for (int i = 0; i < getVC().length; i++) {
			if ((i != senderInd) && (msg.getVC()[i] > getVC()[i])) {
				System.out.println("message: " + msg.toString() + " added to queue");
				messageQueue.add(msg);
				return;
			}
		}
		deliverMsg(msg);
		if (!messageQueue.isEmpty()) {
			releaseQueue();
		}
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
	 * 
	 * @param process
	 */

	public Listener(ProcessImpl process) {
		this.process = process;
		th = new Thread(this);
		sc = new Scanner(System.in);
	}

	/**
	 * start associated thread
	 */
	public void start() {
		th.start();
	}

	/**
	 * infinitely waits for user input, creates a message and delivers it on user input
	 */
	@Override
	public void run() {
		while (true) {
			msg = sc.nextLine().trim();
			process.printReady();
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