package solvexis.service;

import solvexis.model.Station;
import solvexis.service.transport.LastMileService;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserStatsService {
    private static final String STATS_FILE = "user_stats.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final String statsFile;
    private int totalTrips;
    private double totalKmTraveled;
    private double totalCO2Saved;
    private double longestRoute;
    private int pointsEarned;
    private String longestRouteDescription;
    
    public UserStatsService() {
        this.statsFile = STATS_FILE;
        loadStats();
    }

    public UserStatsService(String userName) {
        if (userName != null && !userName.trim().isEmpty()) {
            this.statsFile = "user_stats_" + userName.trim() + ".txt";
        } else {
            this.statsFile = STATS_FILE;
        }
        loadStats();
    }
    
    public void recordTrip(List<Station> route, double lastMileDistance, double co2Saved) {
        double metroDistance = calculateMetroDistance(route);
        double totalDistance = metroDistance + (lastMileDistance > 0 ? lastMileDistance : 0);
        
        totalTrips++;
        totalKmTraveled += totalDistance;
        totalCO2Saved += co2Saved;
        
        if (totalDistance > longestRoute) {
            longestRoute = totalDistance;
            if (route != null && route.size() >= 2) {
                longestRouteDescription = route.get(0).getName() + " -> " + 
                    route.get(route.size() - 1).getName();
            }
        }
        
        int tripPoints = (int)(totalDistance * 10) + 5;
        pointsEarned += tripPoints;
        
        saveStats();
    }
    
    private double calculateMetroDistance(List<Station> route) {
        if (route == null || route.size() <= 1) {
            return 0.0;
        }
        return (route.size() - 1) * 1.5; // 1.5 km per station
    }
    
    public void displayStats() {
        System.out.println("\n========================================");
        System.out.println("   🏆 Travel Stats Dashboard");
        System.out.println("========================================");
        System.out.println();
        System.out.printf("Total Trips Taken:     %d%n", totalTrips);
        System.out.printf("Total km Traveled:     %.1f km%n", totalKmTraveled);
        System.out.printf("Total CO₂ Saved:       %.2f kg%n", totalCO2Saved);
        System.out.printf("Longest Route:         %.1f km%n", longestRoute);
        if (longestRouteDescription != null) {
            System.out.printf("  (%s)%n", longestRouteDescription);
        }
        System.out.printf("Points Earned:         #%d points%n", pointsEarned);
        System.out.println();
        System.out.println("========================================");
    }
    
    public void displayLeaderboard() {
        System.out.println("\n========================================");
        System.out.println("   🏆 Leaderboard");
        System.out.println("========================================");
        System.out.println();
        System.out.printf("Rank #1: #%d points (%.1f km traveled)%n", pointsEarned, totalKmTraveled);
        System.out.println();
        System.out.println("Keep traveling to climb the leaderboard! 🚇");
        System.out.println("========================================");
    }
    
    private void loadStats() {
        try {
            File file = new File(statsFile);
            if (!file.exists()) {
                totalTrips = 0;
                totalKmTraveled = 0.0;
                totalCO2Saved = 0.0;
                longestRoute = 0.0;
                pointsEarned = 0;
                longestRouteDescription = null;
                return;
            }
            
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("Total Trips:")) {
                    totalTrips = Integer.parseInt(line.split(":")[1].trim());
                } else if (line.startsWith("Total km:")) {
                    totalKmTraveled = Double.parseDouble(line.split(":")[1].trim().replace(" km", ""));
                } else if (line.startsWith("Total CO2:")) {
                    totalCO2Saved = Double.parseDouble(line.split(":")[1].trim().replace(" kg", ""));
                } else if (line.startsWith("Longest Route:")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        String routeInfo = parts[1].trim();
                        if (routeInfo.contains("km")) {
                            longestRoute = Double.parseDouble(routeInfo.replace(" km", "").split("\\(")[0].trim());
                            if (routeInfo.contains("(")) {
                                longestRouteDescription = routeInfo.substring(
                                    routeInfo.indexOf("(") + 1, routeInfo.indexOf(")")
                                );
                            }
                        }
                    }
                } else if (line.startsWith("Points:")) {
                    pointsEarned = Integer.parseInt(line.split(":")[1].trim().replace(" points", ""));
                }
            }
            scanner.close();
        } catch (IOException e) {
            totalTrips = 0;
            totalKmTraveled = 0.0;
            totalCO2Saved = 0.0;
            longestRoute = 0.0;
            pointsEarned = 0;
            longestRouteDescription = null;
        }
    }
    
    private void saveStats() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(statsFile))) {
            writer.println("=== User Travel Statistics ===");
            writer.println("Last Updated: " + LocalDateTime.now().format(formatter));
            writer.println();
            writer.println("Total Trips: " + totalTrips);
            writer.println("Total km: " + String.format("%.1f km", totalKmTraveled));
            writer.println("Total CO2: " + String.format("%.2f kg", totalCO2Saved));
            writer.println("Longest Route: " + String.format("%.1f km", longestRoute) + 
                (longestRouteDescription != null ? " (" + longestRouteDescription + ")" : ""));
            writer.println("Points: " + pointsEarned + " points");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving stats: " + e.getMessage());
        }
    }
    
    public int getTotalTrips() { return totalTrips; }
    public double getTotalKmTraveled() { return totalKmTraveled; }
    public double getTotalCO2Saved() { return totalCO2Saved; }
    public double getLongestRoute() { return longestRoute; }
    public int getPointsEarned() { return pointsEarned; }
}

