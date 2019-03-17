package messenger;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Process extends Remote {
	public void	messagePost(Message msg) throws RemoteException;
}
