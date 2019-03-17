package messenger;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface to be used as stubs by remote processes.
 * @author karim
 *
 */
public interface Process extends Remote {
	/**
	 * defines the method responsible for message delivery
	 * @param msg: Message object to be received
	 * @throws RemoteException
	 */
	public void	messagePost(Message msg) throws RemoteException;
}
