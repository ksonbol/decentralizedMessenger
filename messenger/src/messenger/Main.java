package messenger;

public class Main {

	/**
	 * main entry point for the program
	 * @param args Takes the ID of the process as ID/PORT_NUMBER
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage java Main ID/PORT_NUMBER");
		}
		String id = args[0];
		ProcessImpl p = new ProcessImpl(id);
		p.start();
	}

}
