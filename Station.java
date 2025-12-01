package solvexis.model;

import java.util.*;

public abstract class Station {
    protected String name;
    protected int corridorNumber;
    protected Set<Station> connections;

    public Station(String name, int corridorNumber) {
        this.name = name;
        this.corridorNumber = corridorNumber;
        this.connections = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public int getCorridorNumber() {
        return corridorNumber;
    }

    public void addConnection(Station station) {
        connections.add(station);
    }

    public Set<Station> getConnections() {
        return connections;
    }

    public abstract double calculateFare();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station station = (Station) o;
        return name.equals(station.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
