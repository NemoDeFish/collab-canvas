package client;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import remote.Action;
import remote.FullShape;

/**
 * This is the class for the drawing panel on the client.
 *
 * @author Si Yong Lim
 */
public class DrawingPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JFrame frame = null;
	Action action = Action.FREEDRAW;
	Color color = Color.BLACK;
	BasicStroke size = new BasicStroke(1.0f);
	private BufferedImage image;
	private Font font = new Font("Arial", Font.PLAIN, 5);
	private ConcurrentHashMap<String, FullShape> previews = new ConcurrentHashMap<>();

	// Declare points
	Point2D.Float pt1 = null;
	Point2D.Float pt2 = null;
	Path2D.Double freedrawPath = null;

	/**
	 * Setter method for receiving image from server
	 * @param image image to be shown
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
		Graphics2D g2 = image.createGraphics();
		g2.dispose();
	    repaint();
	}
	
	/**
	 * Setter method for actions
	 * @param action action to be set
	 */
	public void setAction(Action action) {
		this.action = action;
	}
	
	/**
	 * Setter method for color
	 * @param color color to be set
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * Setter method for stroke's size
	 * @param size size of stroke to be set
	 */
	public void setSize(float size) {
		this.size = new BasicStroke(size);
		this.font = new Font("Arial", Font.PLAIN, (int) size * 5);
	}
	
	/**
	 * Create the panel.
	 * @param Parent parent swing window
	 */
	public DrawingPanel(JFrame Parent) {
		frame = Parent;
		
		// Handles all mouse actions
		MouseAdapter mouseHandler = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (action == Action.FREEDRAW || action == Action.ERASER) {
					freedrawPath = new Path2D.Double();
					freedrawPath.moveTo(e.getX(), e.getY());
				}
				pt1 = new Point2D.Float(e.getX(), e.getY());
				try {
					// Inform everyone that the user is drawing
					((ClientGUI) frame).getRemote().informAll(action);
					((ClientGUI) frame).updateUserOperation(((ClientGUI) frame).getUsername(), action);
				} catch (RemoteException e1) {
					JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				Graphics2D g2 = image.createGraphics();

				if (action == Action.FREEDRAW || action == Action.ERASER) {
					if (action == Action.FREEDRAW) {
						try {
							// Resets the preview on other side and sends the final confirmed shape
							((ClientGUI) frame).getRemote().sendPreviewAll(null, null, 0, null, 0, 0, null);
							((ClientGUI) frame).getRemote().sendShapeAll(freedrawPath, color, size.getLineWidth());
						} catch (RemoteException e1) {
							JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
						}
						g2.setColor(color);
					} else {
						try {
							// Resets the preview on the other side and sends the final confirmed shape
							((ClientGUI) frame).getRemote().sendPreviewAll(null, null, 0, null, 0, 0, null);
							((ClientGUI) frame).getRemote().sendShapeAll(freedrawPath, Color.WHITE, size.getLineWidth()); // color is white for an eraser
						} catch (RemoteException e1) {
							JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
						}
						g2.setColor(Color.WHITE);
					}
					g2.setStroke(size);
					g2.draw(freedrawPath);
				} else if (action == Action.TEXT) {
					// Spawns new text field at user's mouse position
					JTextField textField = new JTextField();
			        textField.setForeground(color);
			        textField.setFont(font);
			        textField.setColumns(15);
			        textField.setOpaque(false);
			        textField.setBorder(null);
			        textField.setBounds(e.getX(), e.getY(), 150, (int)(font.getSize() * 1.5));
			        DrawingPanel.this.add(textField);
			        
			        // Automatically focus on text field so user can type immediately
			        textField.requestFocusInWindow();
			        DrawingPanel.this.repaint();

			        final int x = e.getX();
			        final int y = e.getY();
			        
			        // Invoked when the user types or deletes anything in the text field
			        textField.getDocument().addDocumentListener(new DocumentListener() {
			            private void update() {
			                String text = textField.getText();
			                try {
			                	((ClientGUI) frame).getRemote().informAll(action);
			                	((ClientGUI) frame).updateUserOperation(((ClientGUI) frame).getUsername(), action);
			                    ((ClientGUI) frame).getRemote().sendPreviewAll(null, color, 0, text, x, y + (int)(font.getSize() * 1.0), font);
			                } catch (RemoteException ex) {
			                    JOptionPane.showMessageDialog(new JFrame(), ex, "Dialog", JOptionPane.ERROR_MESSAGE);
			                }
			                DrawingPanel.this.repaint();
			            }

			            @Override
			            public void insertUpdate(DocumentEvent e) { update(); }

			            @Override
			            public void removeUpdate(DocumentEvent e) { update(); }

			            @Override
			            public void changedUpdate(DocumentEvent e) { update(); }
			        });
			        
			        // When the user clicks away, the text is committed onto the image
			        textField.addFocusListener(new FocusAdapter() {
			            @Override
			            public void focusLost(FocusEvent fe) {
			                String text = textField.getText();
		                    Graphics2D g2 = image.createGraphics();
		                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		                    g2.setColor(color);
		                    g2.setFont(font);
		                    g2.drawString(text, x, y + (int)(font.getSize() * 1.0));

		                    try {
		                    	((ClientGUI) frame).getRemote().informAll(null);
		                    	((ClientGUI) frame).updateUserOperation(((ClientGUI) frame).getUsername(), null);
		                    	((ClientGUI) frame).getRemote().sendPreviewAll(null, null, 0, null, 0, 0, null);
		                        ((ClientGUI) frame).getRemote().sendTextAll(text, x, y + (int)(font.getSize() * 1.0), color, font);
		                    } catch (RemoteException ex) {
		                        JOptionPane.showMessageDialog(new JFrame(), ex, "Dialog", JOptionPane.ERROR_MESSAGE);
		                    }

		                    DrawingPanel.this.remove(textField);
		                    DrawingPanel.this.repaint();
			            }
			        });
				} else {
					// Register second point
					pt2 = new Point2D.Float(e.getX(), e.getY());
					Shape shape = createShape(pt1, pt2);
					g2.setStroke(size);
			        g2.setColor(color);
			        g2.draw(shape);
					try {
						if (action == Action.ERASER) {
							((ClientGUI) frame).getRemote().sendPreviewAll(null, null, 0, null, 0, 0, null);
							((ClientGUI) frame).getRemote().sendShapeAll(shape, Color.WHITE, size.getLineWidth());
						} else {
							((ClientGUI) frame).getRemote().sendPreviewAll(null, null, 0, null, 0, 0, null);
							((ClientGUI) frame).getRemote().sendShapeAll(shape, color, size.getLineWidth());
						}
					} catch (RemoteException e1) {
						JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
					}
				}
				
				g2.dispose();
				repaint();
				
				// Reset operation on other side
				try {
					((ClientGUI) frame).getRemote().informAll(null);
					((ClientGUI) frame).updateUserOperation(((ClientGUI) frame).getUsername(), null);
				} catch (RemoteException e1) {
					JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
				}
				
				// Resets pt1 and pt2 so that the next click doesn't cause any issues
				pt1 = null;
				pt2 = null;
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// Sets current pt2 for the preview
				pt2 = new Point2D.Float(e.getX(), e.getY());
		        try {
		        	if (action == Action.ERASER) {
		        		((ClientGUI) frame).getRemote().sendPreviewAll(createShape(pt1, pt2), Color.WHITE, size.getLineWidth(), null, 0, 0, null);
					} else {
						((ClientGUI) frame).getRemote().sendPreviewAll(createShape(pt1, pt2), color, size.getLineWidth(), null, 0, 0, null);
					}
				} catch (RemoteException e1) {
					JOptionPane.showMessageDialog(new JFrame(), e1, "Dialog", JOptionPane.ERROR_MESSAGE);
				}
			    repaint();
			}
		};
		
		addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
	}

	/**
	 * Helper function for handling each shape
	 * 
	 * @param p1 point 1
	 * @param p2 point 2
	 * @return the shape created
	 */
	private Shape createShape(Point2D.Float p1, Point2D.Float p2) {
        if (p1 == null || p2 == null || action == null) {
        	return null;
        }

        switch (action) {
            case CIRCLE:
                return new Ellipse2D.Float(
                        Math.min(p1.x, p2.x),
                        Math.min(p1.y, p2.y),
                        Math.abs(p1.x - p2.x),
                        Math.abs(p1.x - p2.x));
            case RECTANGLE:
                return new Rectangle2D.Float(
                        Math.min(p1.x, p2.x),
                        Math.min(p1.y, p2.y),
                        Math.abs(p1.x - p2.x),
                        Math.abs(p1.y - p2.y));
            case LINE:
                return new Line2D.Float(p1, p2);
			case ERASER:
				freedrawPath.lineTo(p2.x, p2.y);
                return freedrawPath;
			case FREEDRAW:
				freedrawPath.lineTo(p2.x, p2.y);
                return freedrawPath;
			case TRIANGLE:
				// Formula for generating a triangle by taking the first point as the top vertex
				// and the second point as the right vertex
				Path2D.Double triangle = new Path2D.Double();
                triangle.moveTo(p1.x, p1.y);
                triangle.lineTo(p2.x, p2.y);
                triangle.lineTo(2 * p1.x - p2.x, p2.y);
                triangle.closePath();
                return triangle;
			default:
				break;
        }
        return null;
    }

	/**
	 * Draws a preview on the drawing panel
	 * @param username user performing the operation
	 * @param shape shape being drawn
	 * @param color color being used
	 * @param size size of shape
	 * @param string string in case of text
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param font the font size of the shape
	 */
	public void drawPreview(String username, Shape shape, Color color, float size, String string, int x, int y, Font font) {
		// Checks is previews currently contains a shape by the user, otherwise add it into previews list to render
		previews.replace(username, new FullShape(shape, color, new BasicStroke(size), string, x, y, font));
		if (!previews.containsKey(username)) {
			previews.put(username, new FullShape(shape, color, new BasicStroke(size), string, x, y, font));
		}
		repaint();
	}
	
	/**
	 * Overridden function that is called each time when repaint() is called to render on the white board
	 */
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // To ensure smooth drawing
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.drawImage(image, 0, 0, this);
        
        // Preview of own's drawing
        if (pt1 != null && pt2 != null && action != Action.TEXT) {
            Shape preview = createShape(pt1, pt2);
            if (preview != null) {
                g2.setStroke(size);
                g2.setColor(action == Action.ERASER ? Color.WHITE : color);
                g2.draw(preview);
            }
        }
        
        // Preview of other user's drawing
        for (FullShape s : previews.values()) {
        	// Drawing shapes
        	if (s.getStroke() != null) g2.setStroke(s.getStroke());
        	if (s.getColor() != null) g2.setColor(s.getColor());
        	if (s.getShape() != null) g2.draw(s.getShape());
        	
        	// Drawing text
        	if (s.getColor() != null) g2.setColor(s.getColor());
        	if (s.getFont() != null) g2.setFont(s.getFont());
        	if (s.getString() != null && s.getX() != 0 && s.getY() != 0) g2.drawString(s.getString(), s.getX(), s.getY());
		}
    }
	
	/**
	 * Draw a shape on the white board
	 * @param shape shape to be drawn
	 * @param color color used
	 * @param size size of shape's stroke
	 */
	public void drawShape(Shape shape, Color color, float size) {
		Graphics2D g2 = image.createGraphics();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(size));
        g2.draw(shape);
        g2.dispose();
		frame.repaint();
	}
	
	/**
	 * Clears white board when called on server side
	 */
	public void clearImage() {
		Graphics2D g2 = image.createGraphics();
	    g2.setColor(Color.WHITE);
	    g2.fillRect(0, 0, image.getWidth(), image.getHeight());
	    g2.dispose();
		pt1 = null;
		pt2 = null;
		freedrawPath = null;
		repaint();
	}
	
	/**
	 * Draws a text on the white board
	 * @param text text to be drawn
	 * @param x x coordinate of text
	 * @param y y coordinate of text
	 * @param color color used
	 * @param font font's size of text
	 */
	public void drawText(String text, int x, int y, Color color, Font font) {
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(color);
        g2.setFont(font);
        g2.drawString(text, x, y);
		frame.repaint();
	}
}