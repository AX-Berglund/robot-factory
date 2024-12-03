package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;
import fr.tp.inf112.projects.canvas.model.Shape;




@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
	@JsonSubTypes.Type(value = Machine.class, name = "Machine"),
    @JsonSubTypes.Type(value = Door.class, name = "Door"),
    @JsonSubTypes.Type(value = Room.class, name = "Room"),
    @JsonSubTypes.Type(value = Area.class, name = "Area"),
    @JsonSubTypes.Type(value = Robot.class, name = "Robot"),
    @JsonSubTypes.Type(value = ChargingStation.class, name = "ChargingStation"),
    @JsonSubTypes.Type(value = Conveyor.class, name = "Conveyor"),
    @JsonSubTypes.Type(value = Factory.class, name = "Factory"),
    @JsonSubTypes.Type(value = Puck.class, name = "Puck")
})
public abstract class Component implements Figure, Serializable, Runnable {
//    private static final Logger LOGGER = Logger.getLogger(Component.class.getName());

	
	private static final long serialVersionUID = -5960950869184030220L;

	private String id;
	
	@JsonBackReference("factory-components")
	protected Factory factory;

	@JsonProperty("shape") // Map "shape" in JSON to this field
    private PositionedShape positionedShape;
	
	private final String name;
	
	protected Component() {
		this.factory = null;
		this.positionedShape = new RectangularShape();
		this.name = "Default Component";
	}
	

	
	protected Component(final Factory factory,
						final PositionedShape shape,
						final String name) {
		this.factory = factory;
		this.positionedShape = shape;
		this.name = name;
		if (factory != null) {
			
			factory.addComponent(this);
		}
	}

	
	@Override
    public void run() {
        while (getFactory().isSimulationStarted()) {
            behave(); // Execute the component's behavior
            try {
                Thread.sleep(50); // Pause briefly to simulate time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Handle interruption
                break;
            }
        }
    }
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("shape")
	public PositionedShape getPositionedShape() {
	    if (positionedShape == null) {
	        positionedShape = new RectangularShape(); // Default to a basic shape
	    }
	    return positionedShape;
	}
	
	@JsonProperty("shape") // Explicitly map this to "shape" in JSON
	public void setPositionedShape(PositionedShape positionedShape) {
	    this.positionedShape = positionedShape;
	}
	
	// this cannot be null
	@JsonIgnore
	public Position getPosition() {
		return getPositionedShape().getPosition();
	}

	public Factory getFactory() {
	    return factory;
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}
	
	@Override
	public int getxCoordinate() {
	    return positionedShape == null ? -1 : positionedShape.getxCoordinate();
	}

	@Override
	public int getyCoordinate() {
	    return positionedShape == null ? -1 : positionedShape.getyCoordinate();
	}


	protected boolean setxCoordinate(int xCoordinate) {
		if ( getPositionedShape().setxCoordinate( xCoordinate ) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

//	@Override
//	public int getyCoordinate() {
//		return getPositionedShape().getyCoordinate();
//	}

	protected boolean setyCoordinate(final int yCoordinate) {
		if (getPositionedShape().setyCoordinate(yCoordinate) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

//	protected void notifyObservers() {
//		getFactory().notifyObservers();
//	}
	
	public void notifyObservers() {
	    if (getFactory() != null) {
	        getFactory().notifyObservers(); // Notify only if the factory is not null
	    } else {
	        System.err.println("Warning: In Component.notifyObservers, getFactory is null so we cannot notify observers.");
	    }
	}

	public String getName() {
		return name;
	}
	
//	Name is final so no need for setter
//	public void setName(String name) {
//		this.name = name;
//	}
//	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + " xCoordinate=" + getxCoordinate() + ", yCoordinate=" + getyCoordinate()
				+ ", shape=" + getPositionedShape();
	}
	
	@JsonIgnore
	public int getWidth() {
		return getPositionedShape().getWidth();
	}
	@JsonIgnore
	public int getHeight() {
		return getPositionedShape().getHeight();
	}
	
	public boolean behave() {
		return false;
	}
	@JsonIgnore
	public boolean isMobile() {
		return false;
	}
	
	public boolean overlays(final Component component) {
		return overlays(component.getPositionedShape());
	}
	
	public boolean overlays(final PositionedShape shape) {
		return getPositionedShape().overlays(shape);
	}
	
	public boolean canBeOverlayed(final PositionedShape shape) {
		return false;
	}
	
	@Override
	@JsonIgnore
	public Style getStyle() {
		return ComponentStyle.DEFAULT;
	}
	
	@Override
	public Shape getShape() {
		return getPositionedShape();
	}
	
	@JsonIgnore
	public boolean isSimulationStarted() {
	    return getFactory() != null && getFactory().isSimulationStarted();
	}


	

	
    
}
