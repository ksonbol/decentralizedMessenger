package messenger;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {

	private static Process stub = null;
	
	private static ProcessImpl pobj = null;
	
	private static Registry registry = null;
	
	public static String getIP(String pid) {
		return pid.split("/")[0];
	}
	
	public static int getPort(String pid) {
		return Integer.parseInt(pid.split("/")[1]);
	}
	
	/**
	 * main entry point for the program
	 * @param args Takes the ID of the process as ID/PORT_NUMBER
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java Main IP/PORT_NUMBER");
			System.exit(0);
		}
		//System.setProperty("java.rmi.server.hostname", "localhost");
		String id = args[0];
		try {
			System.out.println("Initializing process...");
			pobj = new ProcessImpl(id);
			System.out.println("Exporting stub object...");
			stub = (Process) UnicastRemoteObject.exportObject(pobj, getPort(id));
			registry = LocateRegistry.createRegistry(getPort(id));
			registry.bind("Process", stub);
		} catch (RemoteException | AlreadyBoundException e) {
			System.out.println("Failed to export object with RMI");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Starting process...");
		pobj.start();
	}
}