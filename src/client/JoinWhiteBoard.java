package client;

import java.awt.EventQueue;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import remote.*;

/**
 * ~ WhiteBoard Client  ~
 * This is the main driver for the white board client.
 *
 * @author Si Yong Lim
 * Student ID: 1507003
 */
public class JoinWhiteBoard {
	private static String serverAddress;
	private static String username;
	private static int port;
	
	public static void main(String[] args) {
		// Parse command line arguments
        if (args.length != 3) {
            System.out.println("EXPECTED: java JoinWhiteBoard <serverIPAddress> <serverPort> <username>");
            return;
        }

        serverAddress = args[0];
        
        // Check valid port
        try {
            port = Integer.parseInt(args[1]);
            if (port < 1 || port > 65535) {
                System.out.println("ERROR: Port number out of range");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Port number must be a valid integer");
            return;
        }
        
        username = args[2];
        
		// Launch ClientGUI
        EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI frame = new ClientGUI(username);
					frame.setVisible(true);
					
					RemoteWhiteBoard remoteClient = new RemoteWhiteBoard(frame, username, frame.getDrawingPanel());
					frame.setRemote(remoteClient);
					
					// Check RMI registry
					Registry registry = LocateRegistry.getRegistry(serverAddress, port);
					IRemoteWhiteBoard remoteWhiteBoard = (IRemoteWhiteBoard) registry.lookup("WhiteBoard");
					
					// Connect to server
					ConcurrentHashMap<String, IRemoteWhiteBoard> clients = remoteWhiteBoard.connect(username, (IRemoteWhiteBoard) remoteClient);
					if (clients == null) {
						JOptionPane.showMessageDialog(new JFrame(), "Manager rejected join in request or username is already taken", "Dialog", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
					
					// Get existing list of users in session
					frame.addUser(username);
					for (ConcurrentHashMap.Entry<String, IRemoteWhiteBoard> c : clients.entrySet()) {
						frame.addUser(c.getKey());
					}
					remoteClient.setClients(clients);
					remoteClient.setHost(remoteWhiteBoard);
					
					// Shutdown hook
					Thread shutdownHook = new Thread(() -> {
						try {
							remoteClient.disconnectAll(username);
						} catch (RemoteException e) {
							JOptionPane.showMessageDialog(new JFrame(), e, "Dialog", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}
					});
					Runtime.getRuntime().addShutdownHook(shutdownHook);
				} catch (NotBoundException e) {
					JOptionPane.showMessageDialog(new JFrame(), "No server bound on RMI registry", "Dialog", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				} catch (RemoteException e) {
					JOptionPane.showMessageDialog(new JFrame(), e + ". Check that the RMI has started", "Dialog", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
	}
}
