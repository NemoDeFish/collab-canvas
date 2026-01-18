package server;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;
import remote.Action;

/**
 * ~ WhiteBoard Server GUI ~
 * This is the class for the white board server GUI.
 *
 * @author Si Yong Lim
 * Student ID: 1507003
 */
public class ServerGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane, usersListPanel;
	private final ButtonGroup actionsGroup = new ButtonGroup();
	private final ButtonGroup sizesGroup = new ButtonGroup();
	private JTextField chatTextField;
	private JTextArea chatArea;
	private RemoteWhiteBoard remoteWhiteBoard;
	private DrawingPanel drawingPanel = new DrawingPanel(this);
	private boolean saved = true;
	private String username;
	private HashMap<String, JPanel> users = new HashMap<String, JPanel>();

	/**
	 * Create the GUI frame.
	 * @param username 
	 * @param registry 
	 */
	public ServerGUI(String username, Registry registry) {
		this.username = username;

		// Triggered when the server wants to close the white board
		this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            	if (saved == false) {
    				int choice = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to save changes?", "Distributed Shared White Board", JOptionPane.YES_NO_CANCEL_OPTION);
    		        if (choice == JOptionPane.YES_OPTION) {
    		        	saved = drawingPanel.saveImage();
    		        	if (saved == false) {
    		        		return;
    		        	}
    		        } else if (choice == JOptionPane.CANCEL_OPTION) {
    		        	return;
    		        }
    			}
            	System.exit(0);
            }
        });
		
		setTitle("Whiteboard Server");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 954, 434);
		
		// Initialize menu bar at the top for leaving
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check if user has saved current white board
				if (saved == false) {
					int choice = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to save changes?", "Distributed Shared White Board", JOptionPane.YES_NO_CANCEL_OPTION);
			        if (choice == JOptionPane.YES_OPTION) {
			        	saved = drawingPanel.saveImage();
			        	if (saved == false) {
			        		return;
			        	}
			        } else if (choice == JOptionPane.CANCEL_OPTION) {
			        	return;
			        }
				}
				drawingPanel.clearImage();
				try {
					remoteWhiteBoard.clearAll();
				} catch (RemoteException e1) {
					JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
				}
				saved = true;
			}
		});
		
		mnFile.add(mntmNew);
		
		JMenuItem mntnOpen = new JMenuItem("Open");
		mntnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check if user has saved the current white board
				if (saved == false) {
					int choice = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to save changes?", "Distributed Shared White Board", JOptionPane.YES_NO_CANCEL_OPTION);
			        if (choice == JOptionPane.YES_OPTION) {
			        	saved = drawingPanel.saveImage();
			        	if (saved == false) {
			        		return;
			        	}
			        } else if (choice == JOptionPane.CANCEL_OPTION) {
			        	return;
			        }
				}
				// Loads image to drawing panel
				boolean status = drawingPanel.loadImage();
				if (status == false) {
					return;
				}
				// Sends image to everyone in active session
				try {
					remoteWhiteBoard.sendImageAll();
				} catch (RemoteException e1) {
					JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mnFile.add(mntnOpen);
		
		JMenuItem mntnSave = new JMenuItem("Save");
		mntnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Checking of whether it is recently saved is done inside saveImage()
		        saved = drawingPanel.saveImage();
			}
		});
		mnFile.add(mntnSave);
		
		JMenuItem mntmSaveAs = new JMenuItem("Save as");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saved = drawingPanel.saveAsImage();
			}
		});
		mnFile.add(mntmSaveAs);
		
		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check if user has saved current white board
				if (saved == false) {
					int choice = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to save changes?", "Distributed Shared White Board", JOptionPane.YES_NO_CANCEL_OPTION);
			        if (choice == JOptionPane.YES_OPTION) {
			        	saved = drawingPanel.saveImage();
			        	if (saved == false) {
			        		return;
			        	}
			        } else if (choice == JOptionPane.CANCEL_OPTION) {
			        	return;
			        }
				}
				System.exit(0);
			}
		});
		mnFile.add(mntmClose);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// Assign customized panel to frame
		drawingPanel.setBounds(5, 46, 576, 291);
		drawingPanel.setBackground(Color.WHITE);
		contentPane.add(drawingPanel);
		
		JPanel panel = new JPanel();
		panel.setBounds(5, 5, 576, 31);
		contentPane.add(panel);
		
		// Declare all shape and text buttons
		JToggleButton tglbtnRectangle = new JToggleButton("Rectangle");
		tglbtnRectangle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// Set draw mode as rectangle
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setAction(Action.RECTANGLE);
				}
			}
		});
		actionsGroup.add(tglbtnRectangle);
		panel.add(tglbtnRectangle);
		
		JToggleButton tglbtnCircle = new JToggleButton("Circle");
		tglbtnCircle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// Set draw mode as circle
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setAction(Action.CIRCLE);
				}
			}
		});
		actionsGroup.add(tglbtnCircle);
		panel.add(tglbtnCircle);
		
		JToggleButton tglbtnLine = new JToggleButton("Line");
		tglbtnLine.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// Set draw mode as circle
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setAction(Action.LINE);
				}
			}
		});
		actionsGroup.add(tglbtnLine);
		panel.add(tglbtnLine);

		JToggleButton tglbtnTriangle = new JToggleButton("Triangle");
		tglbtnTriangle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// Set draw mode as triangle
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setAction(Action.TRIANGLE);
				}
			}
		});
		actionsGroup.add(tglbtnTriangle);
		panel.add(tglbtnTriangle);
		
		JToggleButton tglbtnFreeDraw = new JToggleButton("Free Draw");
		tglbtnFreeDraw.setSelected(true);
		tglbtnFreeDraw.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// Set draw mode as free draw
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setAction(Action.FREEDRAW);
				}
			}
		});
		actionsGroup.add(tglbtnFreeDraw);
		panel.add(tglbtnFreeDraw);
		
		JToggleButton tglbtnEraser = new JToggleButton("Eraser");
		tglbtnEraser.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// Set draw mode as eraser
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setAction(Action.ERASER);
				}
			}
		});
		actionsGroup.add(tglbtnEraser);
		panel.add(tglbtnEraser);
		
		JToggleButton tglbtnText = new JToggleButton("Text");
		tglbtnText.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setAction(Action.TEXT);
				}
			}
		});
		actionsGroup.add(tglbtnText);
		panel.add(tglbtnText);
		
		// Color picker
		JLabel lblColor = new JLabel("Color:");
		lblColor.setBounds(5, 347, 50, 13);
		contentPane.add(lblColor);
		
		JButton btnColor = new JButton("");
		btnColor.setBackground(new Color(0, 0, 0));
		btnColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(ServerGUI.this, "Pick color", Color.WHITE);
				btnColor.setBackground(color);
				drawingPanel.setColor(color);
			}
		});
		btnColor.setBounds(45, 342, 21, 21);
		contentPane.add(btnColor);
		
		// Size picker
		JLabel lblSize = new JLabel("Size:");
		lblSize.setBounds(365, 347, 50, 13);
		contentPane.add(lblSize);
		
		JToggleButton tglbtnSmall = new JToggleButton("●");
		tglbtnSmall.setSelected(true);
		tglbtnSmall.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setSize(1.0f);
				}
			}
		});
		sizesGroup.add(tglbtnSmall);
		tglbtnSmall.setFont(new Font("Arial", Font.PLAIN, 5));
		tglbtnSmall.setBounds(405, 343, 55, 21);
		contentPane.add(tglbtnSmall);
		
		JToggleButton tglbtnMed = new JToggleButton("●");
		tglbtnMed.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setSize(2.5f);
				}
			}
		});
		sizesGroup.add(tglbtnMed);
		tglbtnMed.setFont(new Font("Arial", Font.PLAIN, 10));
		tglbtnMed.setBounds(466, 343, 55, 21);
		contentPane.add(tglbtnMed);
		
		JToggleButton tglbtnLarge = new JToggleButton("●");
		tglbtnLarge.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					drawingPanel.setSize(5.0f);
				}
			}
		});
		sizesGroup.add(tglbtnLarge);
		tglbtnLarge.setFont(new Font("Arial", Font.PLAIN, 15));
		tglbtnLarge.setBounds(526, 343, 55, 21);
		contentPane.add(tglbtnLarge);
		
		// Chat window
		JPanel chatWindow = new JPanel(new BorderLayout());
		chatWindow.setBounds(591, 5, 186, 362);
		chatWindow.setBorder(new TitledBorder("Chat Window:"));
		
		chatArea = new JTextArea();
		chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatWindow.add(scrollPane, BorderLayout.CENTER);
        
        chatTextField = new JTextField();
        chatTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatTextField.getText();
                if (!message.trim().isEmpty()) {
                	displayChat(username, message);
                    chatTextField.setText("");
                    chatTextField.requestFocusInWindow();
                    try {
						remoteWhiteBoard.sendMessage(username, message);
					} catch (RemoteException e1) {
						JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
					}
                }
            }
        });
		chatWindow.add(chatTextField, BorderLayout.SOUTH);
		chatTextField.setColumns(10);
		
		contentPane.add(chatWindow);
		
		// Connected users panel
		JPanel usersWindow = new JPanel();
		usersWindow.setBorder(new TitledBorder("Connected Users:"));
		usersWindow.setBounds(781, 5, 155, 360);
		contentPane.add(usersWindow);
		usersWindow.setLayout(new BorderLayout()); 
		contentPane.add(usersWindow);
		
		JScrollPane scrollPaneUsers = new JScrollPane();
        Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		scrollPaneUsers.setBorder(emptyBorder);
		usersWindow.add(scrollPaneUsers, BorderLayout.CENTER);
		
		usersListPanel = new JPanel();
		usersListPanel.setLayout(new BoxLayout(usersListPanel, BoxLayout.Y_AXIS));
		usersListPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPaneUsers.setViewportView(usersListPanel);
	}
	
	/**
     * Display messages in the chat window
     * @param username username of user who sent the text
	 * @param message message sent by user
     */
    public void displayChat(String username, String message) {
        // Ensure that multiple threads are synchronizing on the results
        // area be done on the EDT to avoid inconsistent states.
        SwingUtilities.invokeLater(() -> {
            chatArea.append(username + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    /**
     * Add user to the users panel
     * @param username user to be added
     */
    public void addUser(String username) {
        // Ensure that multiple threads are synchronizing on the results
        // area be done on the EDT to avoid inconsistent states.
        SwingUtilities.invokeLater(() -> {
        	JPanel userPanel = new JPanel(new BorderLayout());
        	userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        	
        	JLabel nameLabel = new JLabel(username + ": Idle ");
        	userPanel.add(nameLabel, BorderLayout.WEST);
        	if (!username.equals(this.username)) {
        		JButton kickButton = new JButton("Kick");
            	kickButton.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent e) {
            			int choice = JOptionPane.showConfirmDialog(new JFrame(), "Are you sure you want to kick this user?", "Distributed Shared White Board", JOptionPane.YES_NO_OPTION);
        		        if (choice == JOptionPane.YES_OPTION) {
        		        	usersListPanel.remove(userPanel);
        		        	usersListPanel.revalidate();
        		        	usersListPanel.repaint();
        		        	try {
    							remoteWhiteBoard.kick(username);
    						} catch (RemoteException e1) {
    							JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
    						}
        		        }
            		}
            	});
            	userPanel.add(kickButton, BorderLayout.EAST);
        	}        	
        	
        	usersListPanel.add(userPanel);
        	usersListPanel.revalidate();
        	usersListPanel.repaint();
        	
        	users.put(username, userPanel);
        });
    }
    
    /**
     * Setter method for remoteWhiteBoard
	 * @param remoteWhiteBoard the remoteWhiteBoard to set
	 */
	public void setRemote(RemoteWhiteBoard remoteWhiteBoard) {
		this.remoteWhiteBoard = remoteWhiteBoard;
	}
	
	/**
	 * Getter method for remoteWhiteboard
	 * @return remoteWhiteBoard
	 */
	public RemoteWhiteBoard getRemote() {
		return remoteWhiteBoard;
	}

	/**
	 * Setter method to set saved state
	 * @param saved the saved to set
	 */
	public void setSaved(boolean saved) {
		this.saved = saved;
	}
	
	/**
     * Remove user from the users panel
     * @param username user to be removed
     */
	public void removeUser(String username) {
		usersListPanel.remove(users.get(username));
		users.remove(username);
		usersListPanel.revalidate();
    	usersListPanel.repaint();
	}
	
	/**
     * Update user's operation on the users panel
     * @param username user performing the operation
     * @param operation user's current operation
     */
	public void updateUserOperation(String username, Action operation) {
    	if (operation == Action.ERASER) {
    		((JLabel) users.get(username).getComponent(0)).setText(username + ": " + "Erasing ");
    	} else if (operation == Action.TEXT) {
    		((JLabel) users.get(username).getComponent(0)).setText(username + ": " + "Typing ");
    	} else if (operation == null) {
    		((JLabel) users.get(username).getComponent(0)).setText(username + ": " + "Idle ");
    	} else {
    		((JLabel) users.get(username).getComponent(0)).setText(username + ": " + "Drawing ");
    	}
    }
	
	/**
	 * Getter method for username
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Getter method for drawing panel
	 * @return the drawing panel
	 */
	protected DrawingPanel getDrawingPanel() {
		return drawingPanel;
	}
}
