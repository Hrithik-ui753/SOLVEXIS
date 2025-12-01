package solvexis.model;

public class TerminalStation extends Station {
    private static final double BASE_FARE = 30.0;

    public TerminalStation(String name, int corridorNumber) {
        super(name, corridorNumber);
    }

    @Override
    public double calculateFare() {
        return BASE_FARE;
    }
}
