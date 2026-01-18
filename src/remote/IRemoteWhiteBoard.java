package remote;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RMI remote interface shared and implemented by client and server
 *
 * @author Si Yong Lim
 * Student ID: 1507003
 */
@SuppressWarnings("exports")
public interface IRemoteWhiteBoard extends Remote {
	/**
	 * When someone else wants to connect to the active session
	 * @param username username of new client connecting
	 * @param client reference to client session
	 * @return returns a hash map of users mapped to their reference session
	 */
	public ConcurrentHashMap<String, IRemoteWhiteBoard> connect(String username, IRemoteWhiteBoard client) throws RemoteException;

	/**
	 * When other people want to disconnect
	 * @param username username of person disconnecting
	 */
	public void disconnect(String username) throws RemoteException;

	/**
	 * Receives message from others
	 * @param username username of person sending message
	 * @param message message sent by other user
	 */
	public void receiveMessage(String username, String message) throws RemoteException;
	
	/**
	 * Called when others want to inform this user of their operation
	 * @param username user executing the action
	 * @param operation operation they're doing
	 */
	public void inform(String username, Action operation) throws RemoteException;
	
	/**
	 * Called when others want to draw a shape on this user's window
	 * @param shape shape to be drawn
	 * @param color color used
	 * @param size shape's stroke size
	 */
	public void drawShape(Shape shape, Color color, float size) throws RemoteException;
	
	/**
	 * Called when user wants to send the existing state of the white board
	 * @param imageBytes an image converted into a stream of bytes 
	 */
	public void sendImage(byte[] imageBytes) throws RemoteException;
	
	/**
	 * Called when server wants to clear image on user's side
	 */
	public void clearImage() throws RemoteException;
	
	/**
	 * Called when others want to draw text on other's side
	 * @param string string to be rendered
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param color color used
	 * @param font font's stroke size
	 */
	public void drawText(String string, int x, int y, Color color, Font font) throws RemoteException;
	
	/**
	 * Called when others want to send a preview to be rendered on this side
	 * @param username user sending the preview
	 * @param shape shape to be drawn
	 * @param color color used
	 * @param size shape's stroke size
	 * @param string string to be rendered
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param font font's stroke size
	 */
	public void updatePreview(String username, Shape shape, Color color, float size, String string, int x, int y, Font font) throws RemoteException;
}