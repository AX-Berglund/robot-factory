package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;


public class Machine extends Component {

    private static final long serialVersionUID = -1568908860712776436L;
    
    private boolean mobile; // Field to hold the `mobile` property

    // Optional reference to the area, not serialized by default
    @JsonBackReference("area-machine") // Completes the relationship with Area.machine
    private Area area;

    // Default constructor for Jackson
    // Default constructor for Jackson
    public Machine() {
        super(new Factory(), null, "Default Machine"); // Provide a new Factory object
        this.area = null; // Default value for area
    }

    // Constructor with area reference
    
    public Machine(final Area area,
                   final RectangularShape shape,
                   final String name) {
        super(area.getFactory(), shape, name);
        this.area = area;
        area.setMachine(this); // Establish the relationship
    }

    // Constructor without area reference
    
    public Machine(final Factory factory,
                   final RectangularShape shape,
                   final String name) {
        super(factory, shape, name);
        this.area = null; // No Area assigned
    }

    @Override
    public String toString() {
        return super.toString() + "]";
    }

    @Override
    public boolean canBeOverlayed(final PositionedShape shape) {
        return true;
    }

    // Getter for `area` for internal use or serialization if needed
    public Area getArea() {
        return area;
    }

    // Setter for `area` for deserialization if needed
    public void setArea(Area area) {
        this.area = area;
    }

	public boolean isMobile() {
		return mobile;
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}
}
