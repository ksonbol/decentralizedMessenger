package messenger;

public class Message {
	/**
	 * the textual content of the message
	 */
	private String transcript;
	
	/**
	 * the original sender id
	 */
	private String oid;
	
	/**
	 * Vector Clock of all processes in group
	 */
	private int[] vc;
	
	
	/**
	 * constructor, creates a new message object with the parameters.
	 * @param transcript a string containing the content of the message
	 * @param oid the original sender id
	 */
	public Message(String transcript, String oid, int[] vc) {
		this.transcript = transcript;
		this.oid = oid;
		this.vc = vc;
	}
	
	/**
	 * getter for transcript
	 * @return message transcript
	 */
	public String getTranscript() {
		return transcript;
	}
	
	/**
	 * getter for oid
	 * @return original sender id
	 */
	public String getOid() {
		return oid;
	}
	
	/**
	 * getter for VC
	 * @return vector clock of message
	 */
	public int[] getVC() {
		return vc;
	}
}
