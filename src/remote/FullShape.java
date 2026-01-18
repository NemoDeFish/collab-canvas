package remote;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;

/**
 * Shared full shape class between user and object.
 * Contains all information required to render shapes and text
 *
 * @author Si Yong Lim
 * Student ID: 1507003
 */
@SuppressWarnings("exports")
public class FullShape {
	private Shape shape;
	private Color color;
	private BasicStroke stroke;
	private String string;
	private int x, y;
	private Font font;

	public FullShape(Shape shape, Color color, BasicStroke stroke, String string, int x, int y, Font font) {
		this.color = color;
		this.shape = shape;
		this.stroke = stroke;
		this.string = string;
		this.x = x;
		this.y = y;
		this.font = font;
	}

	/**
	 * @return the shape
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * @param shape the shape to set
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the stroke
	 */
	public BasicStroke getStroke() {
		return stroke;
	}

	/**
	 * @param stroke the stroke to set
	 */
	public void setStroke(BasicStroke stroke) {
		this.stroke = stroke;
	}

	/**
	 * @return the string
	 */
	public String getString() {
		return string;
	}

	/**
	 * @param string the string to set
	 */
	public void setString(String string) {
		this.string = string;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the font
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(Font font) {
		this.font = font;
	}

}
