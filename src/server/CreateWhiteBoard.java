package server;

import java.awt.EventQueue;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.ExportException;

import javax.swing.*;

/**
 * ~ WhiteBoard Server  ~
 * This is the main driver for the white board server.
 *
 * @author Si Yong Lim
 */
public class CreateWhiteBoard {
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
        
        
		try {
            // Launch ServerGUI
            EventQueue.invokeLater(new Runnable() {
    			public void run() {
    				try {
    					// Starts the registry
    					System.setProperty("java.rmi.server.hostname", serverAddress);
    					Registry registry = LocateRegistry.createRegistry(port);
    					ServerGUI frame = new ServerGUI(username, registry);
    					frame.setVisible(true);
    					
    					// Bind to registry
    					RemoteWhiteBoard remoteWhiteBoard = new RemoteWhiteBoard(frame, username, frame.getDrawingPanel());
    					frame.setRemote(remoteWhiteBoard);
    		            registry.bind("WhiteBoard", remoteWhiteBoard);
    		            
    		            // Shutdown hook
    		            Thread shutdownHook = new Thread(() -> {
    		    			try {
    		    				remoteWhiteBoard.disconnectAll(username);
    		    				registry.unbind("WhiteBoard");
    		    			} catch (RemoteException | NotBoundException e1) {
    		    				JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
    		    			}
    		    		});
    		            Runtime.getRuntime().addShutdownHook(shutdownHook);
    				} catch (AlreadyBoundException | ExportException e) {
    					JOptionPane.showMessageDialog(new JFrame(), e + ". Check that no other server is running", "Dialog", JOptionPane.ERROR_MESSAGE);
    				} catch (RemoteException e) {
    					JOptionPane.showMessageDialog(new JFrame(), e + ". Check that the RMI has started", "Dialog", JOptionPane.ERROR_MESSAGE);
					}
    			}
    		});
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), e, "Dialog", JOptionPane.ERROR_MESSAGE);
		}
	}
}
