package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class Position implements Serializable {

	private static final long serialVersionUID = 7274819087013715987L;

	private int xCoordinate;

	private int yCoordinate;
	
	public Position() {
		super();
		this.xCoordinate = 0;
		this.yCoordinate = 0;
	}

	public Position(final int xCoordinate, 
					final int yCoordinate) {
		super();

		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	public int getxCoordinate() {
		return xCoordinate;
	}

	public int getyCoordinate() {
		return yCoordinate;
	}
	
	@JsonIgnore // Prevent serialization of this method to avoid cyclic dependencies
	public List<Position> getNeighbours() {
	    final int stepSize = 5; // Movement scale based on simulation
	    List<Position> neighbours = new ArrayList<>();

	    // Add positions in all four cardinal directions with step size
	    neighbours.add(new Position(this.getxCoordinate(), this.getyCoordinate() - stepSize)); // North
	    neighbours.add(new Position(this.getxCoordinate(), this.getyCoordinate() + stepSize)); // South
	    neighbours.add(new Position(this.getxCoordinate() - stepSize, this.getyCoordinate())); // West
	    neighbours.add(new Position(this.getxCoordinate() + stepSize, this.getyCoordinate())); // East

	    return neighbours;
	}



	public boolean setxCoordinate(final int xCoordinate) {
		if (this.xCoordinate == xCoordinate) {
			return false;
		}
		
		this.xCoordinate = xCoordinate;
		
		return true;
	}

	public boolean setyCoordinate(final int yCoordinate) {
		if (this.yCoordinate == yCoordinate) {
			return false;
		}
		
		this.yCoordinate = yCoordinate;
		
		return true;
	}
	
	@Override
	public boolean equals(final Object objectToCompare) {
		if (objectToCompare == null) {
			return false;
		}
		
		final Position position = (Position) objectToCompare;
		
		return getxCoordinate() == position.getxCoordinate() && getyCoordinate() == position.getyCoordinate();
	}
	
	@Override
	public String toString() {
		final StringBuilder strBuild = new StringBuilder("Position = (");
		strBuild.append(getxCoordinate());
		strBuild.append(", ");
		strBuild.append(getyCoordinate());
		strBuild.append(")");
		
		return strBuild.toString();
	}
}
