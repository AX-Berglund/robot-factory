package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Room extends Component {
	
	private static final long serialVersionUID = 1449569724908316962L;

	public static enum WALL {LEFT, TOP, RIGHT, BOTTOM};
	
	private static final int WALL_THICKNESS = 5;
	
	@JsonIgnore // These fields are derived and should not be serialized
	private final PositionedShape leftWall;
	
	@JsonIgnore // These fields are derived and should not be serialized
	private final PositionedShape rightWall;
	
	@JsonIgnore // These fields are derived and should not be serialized
	private final PositionedShape topWall;
	
	@JsonIgnore // These fields are derived and should not be serialized
	private final PositionedShape bottomWall;
	
	@JsonManagedReference("room-areas")
	private final List<Area> areas;
	
	@JsonManagedReference("room-doors")
	private final List<Door> doors;
	
	// Default constructor for Jackson
    public Room() {
        super(null, null, "Default Room");

        // Initialize walls with default values
        this.leftWall = null;
        this.rightWall = null;
        this.topWall = null;
        this.bottomWall = null;

        // Initialize empty lists
        this.areas = new ArrayList<>();
        this.doors = new ArrayList<>();
    }

	public Room(final Factory factory,
				final RectangularShape shape,
				final String name) {
		super(factory, shape, name);
		
		leftWall = new RectangularShape(getxCoordinate(), getyCoordinate(), WALL_THICKNESS, getHeight() + WALL_THICKNESS);
		rightWall = new RectangularShape(getxCoordinate() + getWidth(), getyCoordinate(), WALL_THICKNESS, getHeight() + WALL_THICKNESS);
		topWall = new RectangularShape(getxCoordinate(), getyCoordinate(), getWidth(), WALL_THICKNESS);
		bottomWall = new RectangularShape(getxCoordinate(), getyCoordinate() + getHeight(), getWidth(), WALL_THICKNESS);
		
		areas = new ArrayList<>();
		doors = new ArrayList<>();
	}
	//changed to public
	public boolean addArea(final Area area) {
		return areas.add(area);
	}
	//changed to public
	public boolean addDoor(final Door door) {
		return doors.add(door);
	}
	
	@JsonIgnore
	public List<Area> getAreas() {
		return areas;
	}
	
	@JsonIgnore
	public List<Door> getDoors() {
		return doors;
	}

	@Override
	public boolean overlays(final PositionedShape shape) {
		return leftWall.overlays(shape) || rightWall.overlays(shape) || 
			   topWall.overlays(shape) || bottomWall.overlays(shape);
	}

	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		final Door overlayedDoor = getOverlayedDoor(shape);
		
		if (overlayedDoor != null) {
			return overlayedDoor.canBeOverlayed(shape);
		}
		
		if (leftWall.overlays(shape) || rightWall.overlays(shape) || 
			topWall.overlays(shape) || bottomWall.overlays(shape)) {
			return false;
		}
		
		return true;
	}
	
	private Door getOverlayedDoor(final PositionedShape shape) {
		for (final Door door : getDoors()) {
			if (door.overlays(shape)) {
				return door;
			}
		}
		
		return null;
	}

	@Override
	public String toString() {
		return super.toString() + " areas=" + areas + "]";
	}
}
