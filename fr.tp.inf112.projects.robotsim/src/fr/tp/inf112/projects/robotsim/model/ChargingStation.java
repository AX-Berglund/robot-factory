package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class ChargingStation extends Component {
	
	private static final long serialVersionUID = -154228412357092561L;
	
	@JsonProperty
	private boolean charging;
	
	
	public ChargingStation() {
	    super(null, null, "Default Charging Station"); // Call the parent constructor with default or null values
	    this.charging = false; // Explicitly initialize charging to its default value
	}

	
	public ChargingStation(final Room room,
						   final RectangularShape shape,
						   final String name) {
		this(room.getFactory(), shape, name);
	}

	public ChargingStation(final Factory factory,
						   final RectangularShape shape,
						   final String name) {
		super(factory, shape, name);
		
		charging = false;
	}

	@Override
	public String toString() {
		return super.toString() + "]";
	}

	protected boolean isCharging() {
		return charging;
	}

	protected void setCharging(boolean charging) {
		this.charging = charging;
	}

	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
}
