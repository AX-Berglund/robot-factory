package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {
	
	private static final long serialVersionUID = -1218857231970296747L;

	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});
	
	private final Battery battery;
	
	private int speed;
	
	private List<Component> targetComponents;
	
	@JsonIgnore
	private transient Iterator<Component> targetComponentsIterator;
	
	private Component currTargetComponent;
	
	@JsonIgnore
	private transient Iterator<Position> currentPathPositionsIter;
	
	@JsonIgnore
	private transient boolean blocked;
	
	private Position nextPosition;
	
	private FactoryPathFinder pathFinder;
	
	// Default constructor for Jackson
//    public Robot() {
//        super(null, null, "Default Robot");
//        this.pathFinder = null;
//        this.battery = new Battery();
//        this.speed = 0;
//        this.targetComponents = new ArrayList<>();
//        this.currTargetComponent = null;
//        this.currentPathPositionsIter = null;
//        this.blocked = false;
//        this.nextPosition = null;
//    }
    
    @JsonCreator
    public Robot(
            @JsonProperty("battery") Battery battery,
            @JsonProperty("speed") int speed) {
        super(null, null, "Default Robot");
        this.battery = battery;
        this.speed = speed;
    }

	public Robot(final Factory factory,
				 final FactoryPathFinder pathFinder,
				 final CircularShape shape,
				 final Battery battery,
				 final String name ) {
		super(factory, shape, name);
		
		this.pathFinder = pathFinder;
		
		this.battery = battery;
		
		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		blocked = false;
		nextPosition = null;
	}

	@Override
	public String toString() {
		return super.toString() + " battery=" + battery + "]";
	}
	
	@JsonProperty
    public Battery getBattery() {
        return battery;
    }

//	@JsonProperty
	protected int getSpeed() {
		return speed;
	}
//	@JsonProperty
	protected void setSpeed(final int speed) {
		this.speed = speed;
	}
	
	
	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}
		
		return targetComponents;
	}
	
	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}
	
	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}
	
	@Override
	public boolean isMobile() {
		return true;
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}
		
		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();
		}
		
		computePathToCurrentTargetComponent();

		return moveToNextPathPosition() != 0;
	}
		
	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}
		
		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}
	
	
	private int moveToNextPathPosition() {
	    final Motion motion = computeMotion();

	    // Step 1: Determine displacement
	    final int displacement = motion == null ? 0 : motion.moveToTarget();

	    if (displacement != 0) {
	        // Notify observers if the robot has moved
	        notifyObservers();
	    } else if (isLivelyLocked()) {
	        // Step 2: Handle livelock
	        final Position freeNeighbouringPositions = findFreeNeighbouringPosition();

	        if (freeNeighbouringPositions != null) {
	            nextPosition = freeNeighbouringPositions; // Move to free position
	            moveToNextPathPosition(); // Retry moving
	            computePathToCurrentTargetComponent(); // Recompute path
	        }
	    }

	    return displacement;
	}
	
	private Position findFreeNeighbouringPosition() {
	    for (final Position neighbour : getPosition().getNeighbours()) {
	        if (!getFactory().hasObstacleAt(new RectangularShape(neighbour.getxCoordinate(),
	                                                             neighbour.getyCoordinate(),
	                                                             2,
	                                                             2))) {
	            return neighbour;
	        }
	    }
	    return null; // No free neighboring position found
	}


	
	private void computePathToCurrentTargetComponent() {
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.iterator();
	}
	
	
	private Motion computeMotion() {
	    if (!currentPathPositionsIter.hasNext()) {
	        blocked = true;
	        return null;
	    }

	    final Position nextPosition = this.nextPosition == null ? currentPathPositionsIter.next() : this.nextPosition;
	  

	    if (getFactory().moveComponent(new Motion(getPosition(), nextPosition), this) == 0) {
	        this.nextPosition = nextPosition; // Save the blocked position for retry
	        return null;
	    }

	    this.nextPosition = null;
	    return new Motion(getPosition(), nextPosition);
	}

	
	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}
	
	public Position getNextPosition() {
		return this.nextPosition;
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
	
	@Override
	public Style getStyle() {
		return blocked ? BLOCKED_STYLE : STYLE;
	}
	
	@JsonIgnore
	public boolean isLivelyLocked() {
		final Position nextPosition = getNextPosition();
	    
		// Step 1: Check if the robot is targeting a position
	    if (nextPosition == null) {
	        // No next position, so no livelock
	        return false;
	    }
		
	    // Step 2: Find the robot at the next position
	    final Component otherRobot = getFactory().getMobileComponentAt(nextPosition, this);
	    if (otherRobot == null) {
	        // No other robot at the next position
	        return false;
	    }
	    // Step 3: Check if the other robot is targeting this robot's position
	    return getPosition().equals(((Robot) otherRobot).getNextPosition());
		
	}
}
