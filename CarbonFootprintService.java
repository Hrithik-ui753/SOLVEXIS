package solvexis.service;

import solvexis.model.Station;
import java.util.List;

public class CarbonFootprintService {
    private static final double CO2_EMISSION_FACTOR = 0.12;
    private static final double AVG_DISTANCE_BETWEEN_STATIONS = 1.5;
    
    public double calculateDistance(List<Station> route) {
        if (route == null || route.size() <= 1) {
            return 0.0;
        }
        return (route.size() - 1) * AVG_DISTANCE_BETWEEN_STATIONS;
    }
    
    public double calculateCO2Saved(double distanceKm) {
        return distanceKm * CO2_EMISSION_FACTOR;
    }
    
    public double calculateCO2Saved(List<Station> route) {
        double distance = calculateDistance(route);
        return calculateCO2Saved(distance);
    }
    
    public String formatCO2Saved(double co2Kg) {
        if (co2Kg < 1.0) {
            return String.format("%.2f g", co2Kg * 1000);
        } else {
            return String.format("%.2f kg", co2Kg);
        }
    }
    
    public void displayCO2Saved(List<Station> route) {
        double distance = calculateDistance(route);
        double co2Saved = calculateCO2Saved(distance);
        
        System.out.println("\n════════ Carbon Footprint Tracker ════════");
        System.out.printf("Distance Traveled:     %.1f km%n", distance);
        System.out.printf("CO₂ Saved vs. Car:     %s%n", formatCO2Saved(co2Saved));
        System.out.println("\nYou're making an eco-friendly choice! 🌱");
        System.out.println("Metro is cleaner than private vehicles.");
        System.out.println("═════════════════════════════════════════");
    }
}

