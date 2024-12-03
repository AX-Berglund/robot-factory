package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Battery implements Serializable {
	
	private static final long serialVersionUID = 5744149485828674046L;

//	@JsonInclude
	private final float capacity;
	
	private float level;
	
//	public Battery(float capacity) {
//		this.capacity = capacity;
//	}
	
	public Battery() {
		this.capacity = 0;
//		level = 0;
		
	}

	// Constructor for deserialization
    @JsonCreator
    public Battery(@JsonProperty("capacity") float capacity) {
        this.capacity = capacity;
        this.level = capacity;
    }
	
	
	// Getter for the capacity
    @JsonProperty
    public float getCapacity() {
        return capacity;
    }

    // Getter and setter for level
    @JsonProperty
    public float getLevel() {
        return level;
    }

    @JsonProperty
    public void setLevel(float level) {
        this.level = level;
    }
	
	
	public float consume(float energy) {
		level-= energy;
		
		return level;
	}
	
	public float charge(float energy) {
		level+= energy;
		
		return level;
	}

	@Override
	public String toString() {
		return "Battery [capacity=" + capacity + "]";
	}
}
