package solvexis.app;

import solvexis.service.MetroRoutePlanner;

public class ListStations {
    public static void main(String[] args) {
        MetroRoutePlanner planner = new MetroRoutePlanner();
        planner.printStations();
    }
}
