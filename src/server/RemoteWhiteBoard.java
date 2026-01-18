package server;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import remote.*;

/**
 * This is the class for the remote white board responsible for implementing the RMI methods
 * and calling other RMI methods
 *
 * @author Si Yong Lim
 */
public class RemoteWhiteBoard extends UnicastRemoteObject implements IRemoteWhiteBoard {
	private static final long serialVersionUID = 1L;
	private final ServerGUI frame;
	private final ConcurrentHashMap<String, IRemoteWhiteBoard> clients = new ConcurrentHashMap<>();
	private final String username;
	private final DrawingPanel drawingPanel;
	
	/**
	 * Constructor to initialize white board
	 * @param frame reference to server GUI frame
	 * @param username username of server
	 * @param drawingPanel drawing panel of server GUI
	 */
	protected RemoteWhiteBoard(ServerGUI frame, String username, DrawingPanel drawingPanel) throws RemoteException {
		this.frame = frame;
		this.username = username;
		SwingUtilities.invokeLater(() -> frame.addUser(username));
		this.drawingPanel = drawingPanel;
	}

	/**
	 * Receives message from others
	 * @param username username of person sending message
	 * @param message message sent by other user
	 */
	@Override
	public void receiveMessage(String username, String message) throws RemoteException {
		SwingUtilities.invokeLater(() -> frame.displayChat(username, message));
    }
	
	/**
	 * Sends message to other people in session
	 * @param username username of this server
	 * @param message message to be sent
	 */
	public void sendMessage(String username, String message) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.receiveMessage(username, message);
		}
	}
	
	/**
	 * When someone else wants to connect to the active session
	 * @param username username of new client connecting
	 * @param client reference to client session
	 * @return returns a hash map of users mapped to their reference session
	 */
	@Override
	public ConcurrentHashMap<String, IRemoteWhiteBoard> connect(String username, IRemoteWhiteBoard client) throws RemoteException {
		if (!clients.containsKey(username)) {
			int choice = JOptionPane.showConfirmDialog(frame, username + " wants to share your whiteboard", "Connection Request", JOptionPane.YES_NO_OPTION);
	        if (choice == JOptionPane.YES_OPTION) {
	        	// Inform existing clients that new client has connected
	        	for (IRemoteWhiteBoard c : clients.values()) {
	    			c.connect(username, client);
	    		}
	        	
	        	// Make a new copy without new client and with server to hand over to new client
	        	ConcurrentHashMap<String, IRemoteWhiteBoard> newClients = new ConcurrentHashMap<String, IRemoteWhiteBoard>(clients);
	        	newClients.put(this.username, this);				
	        	
	            clients.put(username, client);
	            SwingUtilities.invokeLater(() -> frame.addUser(username));
				
				// Send existing white board state to client
				client.sendImage(drawingPanel.sendImage());
				
				return newClients;
	        }
		}
		return null;
	}
	
	/**
	 * When other people want to disconnect
	 * @param username username of person disconnecting
	 */
	@Override
	public void disconnect(String username) throws RemoteException {
		clients.remove(username);
		SwingUtilities.invokeLater(() -> {
			frame.removeUser(username);
		});
    }
	
	/**
	 * Called when this server wants to disconnect
	 * @param username username of this server
	 */
	public void disconnectAll(String username) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.disconnect(username);
		}
		clients.clear();
	}
	
	/**
	 * Kicks client from the active session
	 * @param username username of person to be kicked
	 */
	public void kick(String username) throws RemoteException {
		IRemoteWhiteBoard client = clients.get(username);
        if (client != null) {
            client.disconnect(username);
            clients.remove(username);
        }
	}
	
	/**
	 * Informs everyone in the active session of this server's action
	 * @param operation operation executed
	 */
	public void informAll(Action action) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.inform(username, action);
		}
	}
	
	/**
	 * Called when others want to inform this server of their operation
	 * @param username user executing the action
	 * @param operation operation they're doing
	 */
	@Override
	public void inform(String username, Action operation) throws RemoteException {
		SwingUtilities.invokeLater(() -> frame.updateUserOperation(username, operation));
    }

	/**
	 * Called when others want to draw a shape on this server's window
	 * @param shape shape to be drawn
	 * @param color color used
	 * @param size shape's stroke size
	 */
	@Override
	public void drawShape(Shape shape, Color color, float size) throws RemoteException {
		SwingUtilities.invokeLater(() -> drawingPanel.drawShape(shape, color, size));
    }
	
	/**
	 * Sends shape drawn by this server to everyone in active session
	 * @param shape shape to be sent
	 * @param color color used
	 * @param size shape's stroke size
	 */
	public void sendShapeAll(Shape shape, Color  color, float size) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.drawShape(shape, color, size);
		}
	}

	/**
	 * Server does not process images from clients directly
	 * @param imageBytes 
	 */
	@Override
	public void sendImage(byte[] imageBytes) throws RemoteException {
		return;
	}

	/**
	 * Server does not directly clear
	 */
	@Override
	public void clearImage() throws RemoteException {
		return;
	}

	/**
	 * Called when others want to draw text on server side
	 * @param string string to be rendered
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param color color used
	 * @param font font's stroke size
	 */
	@Override
	public void drawText(String string, int x, int y, Color color, Font font) throws RemoteException {
		SwingUtilities.invokeLater(() -> drawingPanel.drawText(string, x, y, color, font));
    }
	
	/**
	 * Asks everyone in session to clear their image
	 */
	public void clearAll() throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.clearImage();
		}
	}
	
	/**
	 * Sends the text from this server to everyone in active session
	 * @param text text to be sent
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param color color used
	 * @param font font's stroke size
	 */
	public void sendTextAll(String text, int x, int y, Color color, Font font) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.drawText(text, x, y, color, font);
		}
	}
	
	/**
	 * Sends existing state of white board to everyone in active session
	 */
	public void sendImageAll() throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.sendImage(drawingPanel.sendImage());
		}
	}
	
	/**
	 * Sends preview from this server to everyone in active session
	 * @param shape shape to be drawn
	 * @param color color used
	 * @param size shape's stroke size
	 * @param string string to be rendered
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param font font's stroke size
	 */
	public void sendPreviewAll(Shape shape, Color color, float size, String string, int x, int y, Font font) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.updatePreview(username, shape, color, size, string, x, y, font);
		}		
	}
	
	/**
	 * Called when others want to send a preview to be rendered on this server
	 * @param username user sending the preview
	 * @param shape shape to be drawn
	 * @param color color used
	 * @param size shape's stroke size
	 * @param string string to be rendered
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param font font's stroke size
	 */
	@Override
	public void updatePreview(String username, Shape shape, Color color, float size, String string, int x, int y, Font font) throws RemoteException {
		SwingUtilities.invokeLater(() -> drawingPanel.drawPreview(username, shape, color, size, string, x, y, font));
    }
}
