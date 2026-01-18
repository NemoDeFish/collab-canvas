package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import remote.*;
import remote.Action;

/**
 * This is the class for the remote white board responsible for implementing the RMI methods
 * and calling other RMI methods
 *
 * @author Si Yong Lim
 * Student ID: 1507003
 */
public class RemoteWhiteBoard extends UnicastRemoteObject implements IRemoteWhiteBoard {
    private static final long serialVersionUID = 1L;
    private final ClientGUI frame;
    private ConcurrentHashMap<String, IRemoteWhiteBoard> clients = new ConcurrentHashMap<>();
    private String host, username;
    private DrawingPanel drawingPanel;
    
	/**
	 * Constructor to initialize white board
	 * @param frame reference to client GUI frame
	 * @param username username of client
	 * @param drawingPanel drawing panel of client GUI
	 */
	public RemoteWhiteBoard(ClientGUI frame, String username, DrawingPanel drawingPanel) throws RemoteException {
		this.frame = frame;
		this.username = username;
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
	 * @param username username of this client
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
		clients.put(username, client);
		SwingUtilities.invokeLater(() -> frame.addUser(username));
		return new ConcurrentHashMap<>(clients);
	}

	/**
	 * When other people want to disconnect
	 * @param username username of person disconnecting
	 */
	@Override
	public void disconnect(String username) throws RemoteException {
		clients.remove(username);
		SwingUtilities.invokeLater(() -> {
			if (username.equals(host)) {
				JOptionPane.showMessageDialog(new JFrame(), "Manager has closed the whiteboard", "Dialog", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			} else if (username.equals(this.username)) {
				JOptionPane.showMessageDialog(new JFrame(), "You have been kicked from the session", "Dialog", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			} else {
				clients.remove(username);
				frame.removeUser(username);
			}
		});
	}
	
	/**
	 * Called when this client wants to disconnect
	 * @param username username of this client
	 */
	public void disconnectAll(String username) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.disconnect(username);
		}
		clients.clear();
	}

	/**
	 * Sets client when server sends over current active users in session
	 * @param clients all map of clients from username to client reference
	 */
	public void setClients(ConcurrentHashMap<String, IRemoteWhiteBoard> clients) {
		this.clients = clients;
	}

	/**
	 * Remember which one is the server host
	 * @param host the host session
	 */
	public void setHost(IRemoteWhiteBoard host) {
		for (var entry : clients.entrySet()) {
            if (entry.getValue().equals(host)) {
                this.host = entry.getKey();
                break; 
            }
        }
	}
	
	/**
	 * Informs everyone in the active session of this client's action
	 * @param operation operation executed
	 */
	public void informAll(Action operation) throws RemoteException {
		for (IRemoteWhiteBoard c : clients.values()) {
			c.inform(username, operation);
		}
	}
	
	/**
	 * Called when others want to inform this client of their operation
	 * @param username user executing the action
	 * @param operation operation they're doing
	 */
	@Override
	public void inform(String username, Action operation) throws RemoteException {
		SwingUtilities.invokeLater(() -> frame.updateUserOperation(username, operation));
	}

	/**
	 * Called when others want to draw a shape on this client's window
	 * @param shape shape to be drawn
	 * @param color color used
	 * @param size shape's stroke size
	 */
	@Override
	public void drawShape(Shape shape, Color color, float size) throws RemoteException {
		SwingUtilities.invokeLater(() -> drawingPanel.drawShape(shape, color, size));
    }

	/**
	 * Sends shape drawn by this client to everyone in active session
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
	 * Called when user wants to send the existing state of the white board
	 * @param imageBytes an image converted into a stream of bytes 
	 */
	@Override
	public void sendImage(byte[] imageBytes) throws RemoteException {
		SwingUtilities.invokeLater(() -> {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                drawingPanel.setImage(ImageIO.read(bais));
            } catch (IOException e) {
            	JOptionPane.showMessageDialog(new JFrame(), e, "Dialog", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

	/**
	 * Sends the text from this client to everyone in active session
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
	 * Called when server wants to clear image on user's side
	 */
	@Override
	public void clearImage() throws RemoteException {
		SwingUtilities.invokeLater(() -> drawingPanel.clearImage());
	}

	/**
	 * Called when others want to draw text on client side
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
	 * Called when others want to send a preview to be rendered on this client
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
	
	/**
	 * Sends preview from this client to everyone in active session
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
}
