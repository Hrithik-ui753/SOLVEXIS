package solvexis.app;
import solvexis.service.*;
import solvexis.service.transport.*;
import solvexis.data.TripHistory;
import solvexis.exceptions.*;
import solvexis.model.Station;

import java.util.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MetroApp {
    private static AlertsService alertsService;
    private static LastMileService lastMileService;
    private static PlatformCrowdDensityService crowdService;
    private static CarbonFootprintService carbonService;
    private static UserStatsService statsService;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MetroRoutePlanner planner = new MetroRoutePlanner();
        alertsService = new AlertsService();
        lastMileService = new LastMileService();
        crowdService = new PlatformCrowdDensityService();
        carbonService = new CarbonFootprintService();
        statsService = new UserStatsService();

        System.out.println("========================================");
        System.out.println("   SOLVEXIS Metro Route Planner");
        System.out.println("========================================");

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1) Book Metro Ticket & Last Mile");
            System.out.println("2) List All Stations");
            System.out.println("3) View Recent Trips");
            System.out.println("4) Check Platform Crowd Density");
            System.out.println("5) View Travel Stats & Leaderboard");
            System.out.println("6) Toggle Journey Alerts");
            System.out.println("7) Exit");
            System.out.print("\nSelect an option (1-7): ");
            String opt = sc.nextLine().trim();

            if (opt.equals("1")) {
                System.out.println("\n===== Book Metro Ticket =====");
                System.out.print("Enter Start Station: ");
                String start = sc.nextLine().trim();
                System.out.print("Enter Destination Station: ");
                String end = sc.nextLine().trim();
                
                try {
                    List<String> path = planner.findRoute(start, end);
                    double fare = planner.calculateFareBetween(start, end);
                    int minutes = planner.estimateTimeMinutes(path);

                    System.out.println("\n--- Route Details ---");
                    System.out.println("Route: " + String.join(" -> ", path));
                    System.out.println("Estimated time: " + minutes + " minutes");
                    System.out.println("Metro Fare: ₹" + String.format("%.0f", fare));
                    
                    List<Station> stations = planner.getStationsOnRoute(start, end);
                    crowdService.displayCrowdDensityForStations(stations);
                    carbonService.displayCO2Saved(stations);
                    
                    System.out.println("------------------------");

                    System.out.print("\nDo you want to book this metro ticket? (yes/no): ");
                    String book = sc.nextLine().trim();
                    
                    if (book.equalsIgnoreCase("yes") || book.equalsIgnoreCase("y")) {
                        System.out.println("\n✓ Metro ticket booked successfully!");
                        System.out.print("\nReady to start your ride? (yes/no): ");
                        String startRide = sc.nextLine().trim();
                        
                        if (startRide.equalsIgnoreCase("yes") || startRide.equalsIgnoreCase("y")) {
                            System.out.println("\n===== Starting Journey =====");
                            
                            alertsService.startJourneyAlerts(stations);
                            
                            try {
                                Thread.sleep(5500);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            System.out.println("\n===== Last Mile Connection =====");
                            System.out.print("Enter your home/final destination address: ");
                            String finalDest = sc.nextLine().trim();
                            
                            if (!finalDest.isEmpty()) {
                                System.out.print("Enter distance in kilometers: ");
                                double lastMileDistance = 0;
                                try {
                                    lastMileDistance = Double.parseDouble(sc.nextLine().trim());
                                    if (lastMileDistance <= 0) {
                                        System.out.println("Invalid distance. Using default 2 km.");
                                        lastMileDistance = 2.0;
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input. Using default 2 km.");
                                    lastMileDistance = 2.0;
                                }
                                
                                List<LastMileService.TransportOption> options = lastMileService.getAvailableOptions(finalDest, lastMileDistance);
                                
                                System.out.println("\n--- Available Transport Options to " + finalDest + " ---");
                                System.out.println("Distance: " + String.format("%.1f km", lastMileDistance));
                                System.out.println();
                                
                                List<LastMileService.TransportOption> filteredOptions = new ArrayList<>();
                                for (LastMileService.TransportOption transportOpt : options) {
                                    TransportMode mode = transportOpt.getMode();
                                    if (mode == TransportMode.RTC_BUS || mode == TransportMode.OLA || 
                                        mode == TransportMode.UBER || mode == TransportMode.RAPIDO) {
                                        filteredOptions.add(transportOpt);
                                    }
                                }
                                
                                Collections.sort(filteredOptions);
                                
                                for (int i = 0; i < filteredOptions.size(); i++) {
                                    System.out.println((i + 1) + ") " + filteredOptions.get(i).getFormattedDetails());
                                }
                                
                                System.out.print("\nSelect a transport option (1-" + filteredOptions.size() + ") or 0 to skip: ");
                                try {
                                    int choice = Integer.parseInt(sc.nextLine().trim());
                                    
                                    LastMileService.TransportOption selectedOption = null;
                                    if (choice > 0 && choice <= filteredOptions.size()) {
                                        selectedOption = filteredOptions.get(choice - 1);
                                        System.out.println("\n✓ Selected: " + selectedOption.getMode().getDisplayName());
                                        System.out.println("  Fare: ₹" + String.format("%.0f", selectedOption.getFare()));
                                        System.out.println("  Estimated Time: " + selectedOption.getEstimatedMinutes() + " minutes");
                                    }
                                    
                                    double metroDistance = carbonService.calculateDistance(stations);
                                    double lastMileDist = selectedOption != null ? lastMileDistance : 0;
                                    double co2Saved = carbonService.calculateCO2Saved(metroDistance);
                                    
                                    statsService.recordTrip(stations, lastMileDist, co2Saved);
                                    
                                    TripHistory.saveCompleteTrip(stations, selectedOption, finalDest, fare);
                                    System.out.println("\n✓ Trip completed and saved to trip_history.txt");
                                    int points = (int)((metroDistance + lastMileDist) * 10 + 5);
                                    System.out.println("✓ Stats updated! Earned #" + points + " points!");
                                    
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid selection. Skipping last-mile booking.");
                                    double metroDistance = carbonService.calculateDistance(stations);
                                    double co2Saved = carbonService.calculateCO2Saved(metroDistance);
                                    statsService.recordTrip(stations, 0, co2Saved);
                                    TripHistory.saveCompleteTrip(stations, null, finalDest, fare);
                                }
                            } else {
                                System.out.println("No destination entered. Saving metro trip only.");
                                double metroDistance = carbonService.calculateDistance(stations);
                                double co2Saved = carbonService.calculateCO2Saved(metroDistance);
                                statsService.recordTrip(stations, 0, co2Saved);
                                TripHistory.saveCompleteTrip(stations, null, null, fare);
                            }
                        } else {
                            System.out.println("Ride not started. Ticket remains booked.");
                        }
                    } else {
                        System.out.println("Ticket booking cancelled.");
                    }

                } catch (StationNotFoundException | InvalidRouteException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (opt.equals("2")) {
                System.out.println("\n===== All Hyderabad Metro Stations =====");
                planner.printStations();
                System.out.println("\nTotal stations: " + planner.getStationNames().size());

            } else if (opt.equals("3")) {
                System.out.println("\n===== Recent Trips =====");
                try {
                    java.nio.file.Path p = java.nio.file.Paths.get("trip_history.txt");
                    if (!java.nio.file.Files.exists(p)) {
                        System.out.println("No trip history found.");
                    } else {
                        java.util.List<String> lines = java.nio.file.Files.readAllLines(p);
                        if (lines.isEmpty()) {
                            System.out.println("No trips recorded yet.");
                        } else {
                            for (String l : lines) System.out.println(l);
                        }
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Error reading trip history: " + e.getMessage());
                }

            } else if (opt.equals("4")) {
                System.out.println("\n===== Platform Crowd Density Check =====");
                System.out.print("Enter station name (or 'all' for route stations): ");
                String stationInput = sc.nextLine().trim();
                
                if (stationInput.equalsIgnoreCase("all")) {
                    System.out.print("Enter start station: ");
                    String start = sc.nextLine().trim();
                    System.out.print("Enter end station: ");
                    String end = sc.nextLine().trim();
                    try {
                        List<Station> stations = planner.getStationsOnRoute(start, end);
                        crowdService.displayCrowdDensityForStations(stations);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                } else {
                    crowdService.displayCrowdDensityForStation(stationInput);
                }
                
            } else if (opt.equals("5")) {
                statsService.displayStats();
                System.out.print("\nView leaderboard? (yes/no): ");
                String viewLeaderboard = sc.nextLine().trim();
                if (viewLeaderboard.equalsIgnoreCase("yes") || viewLeaderboard.equalsIgnoreCase("y")) {
                    statsService.displayLeaderboard();
                }
                
            } else if (opt.equals("6")) {
                alertsService.toggleAlerts();
                System.out.println("\nJourney alerts have been " + 
                    (alertsService.isEnabled() ? "enabled" : "disabled"));
                
            } else if (opt.equals("7")) {
                System.out.println("\nThank you for using SOLVEXIS Metro Route Planner!");
                System.out.println("Have a safe journey!");
                alertsService.shutdown();
                break;

            } else {
                System.out.println("\nInvalid option. Please select 1-7.");
            }
        }

        sc.close();
    }
}
