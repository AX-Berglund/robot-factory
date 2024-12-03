package fr.tp.inf112.projects.robotsim.model;

//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonInclude;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Area extends Component {
	
	private static final long serialVersionUID = 5022214804847296168L;
	
	private static final Style STYLE = new ComponentStyle( ComponentStyle.DEFAULT_DASH_PATTERN );
	
	@JsonBackReference("room-areas")
    private Room room;
	
	// The machine located in this area
    @JsonManagedReference("area-machine") // Indicates the "owner" side of a bidirectional relationship
    private Machine machine;
    
	public Area() {
		super(null, null, "Default Area"); // Provide default or null values
        this.machine = null;
	}
	
	public Area(final Room room,
				final RectangularShape shape,
				final String name) {
		super(room.getFactory(), shape, name);
		
		room.addArea(this);
		
		machine = null;
	}
	
	protected void setMachine( final Machine machine ) {
		this.machine = machine;
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + " machine=" + machine + "]";
	}
	
//	@JsonIgnore
	@Override
	public Style getStyle() {
		return STYLE;
	}
	
	
}
