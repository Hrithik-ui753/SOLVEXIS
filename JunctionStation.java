package solvexis.model;

import java.util.List;

public class JunctionStation extends Station {
    private static final double BASE_FARE = 25.0;
    private List<Integer> corridors;

    public JunctionStation(String name, List<Integer> corridors) {
        super(name, corridors.get(0));
        this.corridors = corridors;
    }

    public List<Integer> getCorridors() {
        return corridors;
    }

    @Override
    public double calculateFare() {
        return BASE_FARE;
    }
}
