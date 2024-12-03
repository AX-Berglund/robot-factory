package fr.tp.inf112.projects.robotsim.model;



import java.util.ArrayList;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.tp.inf112.projects.canvas.controller.Observable;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@factory")
public class Factory extends Component implements Canvas, Observable {

	private static final long serialVersionUID = 5156526483612458192L;
	
	private static final ComponentStyle DEFAULT = new ComponentStyle(5.0f);

	private boolean isInitialized = false;

	

	@JsonManagedReference("factory-components")
	private final List<Component> components;



	@JsonCreator
	public Factory(@JsonProperty("components") List<Component> components) {
	    this.components = components != null ? components : new ArrayList<>();
	    reestablishRelationships(); // Restore relationships after deserialization
	}

	@JsonIgnore
	private void reestablishRelationships() {
	    for (Component component : components) {
	        if (component != null && component.getFactory() != this) {
	            component.setFactory(this); // Restore the `factory` reference
	        }
	    }
	}



	public void initialize() {
        this.isInitialized = true;
    }


	
	@JsonIgnore // Exclude transient fields
	private transient List<Observer> observers;
	
	@JsonIgnore
	private transient boolean simulationStarted;
	
	
	public Factory() {
	    this.components = new ArrayList<>();
	    
	    this.simulationStarted = false;
	}


	public Factory(final int width,
				   final int height,
				   final String name ) {
		super(null, new RectangularShape(0, 0, width, height), name);
		
		components = new ArrayList<>();
		//components = components;
		observers = null;
		simulationStarted = false;
	}
	
	public List<Observer> getObservers() {
		if (observers == null) {
			observers = new ArrayList<>();
		}
		
		return observers;
	}
	
	public void setFactory(Factory factory) {
	    this.factory = factory;
	}
	
	@Override
	public boolean addObserver(Observer observer) {
		return getObservers().add(observer);
	}

	@Override
	public boolean removeObserver(Observer observer) {
		return getObservers().remove(observer);
	}
	
	public void notifyObservers() {
		if (!isInitialized) {
            return;
        }
		if (factory != null) {
	        factory.notifyObservers();
		}
//		else {throw new IllegalStateException("Factory is null; cannot notify observers.");}
	}
	
	public boolean addComponent(final Component component) {
	    if (components.add(component)) {
	        component.setFactory(this); // Set the factory reference for the component
	        notifyObservers(); // Notify observers after setting the reference
	        return true;
	    }
	    return false;
	}



	public boolean removeComponent(final Component component) {
		if (components.remove(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}
	//changed to public
	public List<Component> getComponents() {
		return components;
	}
	
//	 Added this
	public Component getMobileComponentAt(final Position position, final Component movingComponent) {
	    for (final Component component : getComponents()) {
	        if (component != movingComponent && component.isMobile() && component.getPosition().equals(position)) {
	            return component;
	        }
	    }
	    return null; // No mobile component found at the specified position
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@JsonIgnore
	public Collection<Figure> getFigures() {
		return (Collection) components;
	}
	

//	@Override
//	public String toString() {
//		return super.toString() + " components=" + components + "]";
//	}
	
	@Override
	public String toString() {
	    return "Factory [name=" + getName() + ", components=" + components + "]";
	}

	
	public boolean isSimulationStarted() {
		return simulationStarted;
	}

	
	public void startSimulation() {
	    if (!isSimulationStarted()) {
	        this.simulationStarted = true;
	        notifyObservers();

	        for (Component component : getComponents()) {
	            new Thread(component).start(); // Start a new thread for each component
	        }
	    }
	}


	public void stopSimulation() {
		if (isSimulationStarted()) {
			this.simulationStarted = false;
			
			notifyObservers();
		}
	}

	@Override
	public boolean behave() {
		boolean behaved = true;
		
		for (final Component component : getComponents()) {
			behaved = component.behave() || behaved;
		}
		
		return behaved;
	}
	
	public synchronized int moveComponent(final Motion motion, final Component componentToMove) {
	    // Derive the target shape from the next position in the motion
	    Position targetPosition = motion.getTargetPosition();// Assume Motion has getNextPosition()
	    if (targetPosition == null) {
	        return 0; // No movement if there's no valid next position
	    }

	    // Define the target shape based on the target position
	    PositionedShape targetShape = new RectangularShape(targetPosition.getxCoordinate(),
	                                                       targetPosition.getyCoordinate(),
	                                                       2, // Width of the shape
	                                                       2); // Height of the shape

	    // Check if the target position is free
	    if (hasObstacleAt(targetShape)) {
	        return 0; // No movement if the position is blocked
	    }
	    
	    int displacement = motion.moveToTarget();
	    if (displacement != 0) {
	        notifyObservers(); // Notify the GUI of movement
	    }

	    // Move the component to the target position
	    return displacement; // Execute the motion
	}


	
	@Override
	public Style getStyle() {
		return DEFAULT;
	}
	
	public boolean hasObstacleAt(final PositionedShape shape) {
		for (final Component component : getComponents()) {
			if (component.overlays(shape) && !component.canBeOverlayed(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasMobileComponentAt(final PositionedShape shape,
										final Component movingComponent) {
		for (final Component component : getComponents()) {
			if (component != movingComponent && component.isMobile() && component.overlays(shape)) {
				return true;
			}
		}
		
		return false;
	}
}
