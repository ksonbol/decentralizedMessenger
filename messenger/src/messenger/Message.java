package messenger;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * the textual content of the message
	 */
	private String transcript;
	
	/**
	 * the original sender id (IP and port number)
	 */
	private String oid;
	
	/**
	 * the original sender index (in the list of nodes)
	 */
	private int index;
	
	/**
	 * Vector Clock of all processes in group
	 */
	private int[] vc;
	
	
	/**
	 * constructor, creates a new message object with the parameters.
	 * @param transcript a string containing the content of the message
	 * @param oid the original sender id
	 */
	public Message(String transcript, String oid, int[] vc, int index) {
		this.transcript = transcript;
		this.oid = oid;
		this.vc = vc;
		this.index = index;
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
	 * @return original sender id (IP and port number)
	 */
	public String getOid() {
		return oid;
	}
	
	/**
	 * getter for index
	 * @return original sender index
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * getter for VC
	 * @return vector clock of message
	 */
	public int[] getVC() {
		return vc;
	}
	
	@Override
	public String toString() {
		return getOid() + ": " + getTranscript();
	}
}
