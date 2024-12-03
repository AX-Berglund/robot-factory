package fr.tp.inf112.projects.robotsim.model.shapes;

import fr.tp.inf112.projects.canvas.model.RectangleShape;


//import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// Add this annotation to handle polymorphic types
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
//@JsonSubTypes({@JsonSubTypes.Type(value = RectangularShape.class, name = "RectangularShape")})

public class RectangularShape extends PositionedShape implements RectangleShape {
	
	private static final long serialVersionUID = -6113167952556242089L;

	private int width;

	private int height;
	
	// Default constructor for Jackson
    public RectangularShape() {
        super(0, 0);
        this.width = 0;
        this.height = 0;
    }

	public RectangularShape(final int xCoordinate,
							final int yCoordinate,
							final int width,
							final int height) {
		super(xCoordinate, yCoordinate);
	
		this.width = width;
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}
	
	

	@Override
	public int getHeight() {
		return height;
	}
	
	// New setter for width
    public void setWidth(final int width) {
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative.");
        }
        this.width = width;
    }
    
    // New setter for height
    public void setHeight(final int height) {
        if (height < 0) {
            throw new IllegalArgumentException("Height must be non-negative.");
        }
        this.height = height;
    }

	@Override
	public String toString() {
		return super.toString() + " [width=" + width + ", height=" + height + "]";
	}
}
