package solvexis.data;

import solvexis.model.Station;
import solvexis.service.transport.*;
import solvexis.service.CarbonFootprintService;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TripHistory {
    private static final String FILE_NAME = "trip_history.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CarbonFootprintService carbonService = new CarbonFootprintService();

    public static void saveCompleteTrip(List<Station> route, LastMileService.TransportOption lastMile, 
                                      String destination, double metroFare) {
        saveCompleteTripForUser(null, route, lastMile, destination, metroFare);
    }

    public static void saveCompleteTripForUser(String userName, List<Station> route,
                                               LastMileService.TransportOption lastMile,
                                               String destination, double metroFare) {
        String fileName = FILE_NAME;
        if (userName != null && !userName.trim().isEmpty()) {
            fileName = "trip_history_" + userName.trim() + ".txt";
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, true))) {
            LocalDateTime now = LocalDateTime.now();
            String from = route.get(0).getName();
            String to = route.get(route.size() - 1).getName();
            
            StringBuilder sb = new StringBuilder();
            sb.append("\n════════════════════════════════════════════════\n");
            sb.append("   TRIP DETAILS\n");
            sb.append("════════════════════════════════════════════════\n\n");
            sb.append("Date & Time: ").append(formatter.format(now)).append("\n\n");
            sb.append("Metro Journey:\n");
            sb.append("  From: ").append(from).append("\n");
            sb.append("  To: ").append(to).append("\n");
            sb.append("  Via: ");
            for (Station station : route) {
                sb.append(station.getName());
                if (station != route.get(route.size() - 1)) {
                    sb.append(" -> ");
                }
            }
            sb.append("\n");
            sb.append("  Metro Fare: ₹").append(String.format("%.0f", metroFare)).append("\n");
            
            if (lastMile != null && destination != null) {
                sb.append("\nLast Mile Connection:\n");
                sb.append("  To: ").append(destination).append("\n");
                sb.append("  Mode: ").append(lastMile.getMode().getDisplayName()).append("\n");
                sb.append("  Fare: ₹").append(String.format("%.0f", lastMile.getFare())).append("\n");
                sb.append("  Est. Time: ").append(lastMile.getEstimatedMinutes()).append(" minutes\n");
            }
            
            double totalFare = metroFare + (lastMile != null ? lastMile.getFare() : 0);
            double metroDistance = carbonService.calculateDistance(route);
            double lastMileDistance = lastMile != null ? lastMile.getEstimatedMinutes() / 2.0 : 0;
            double totalDistance = metroDistance + lastMileDistance;
            double co2Saved = carbonService.calculateCO2Saved(metroDistance);
            
            sb.append("\nTrip Summary:\n");
            sb.append("  Total Distance: ").append(String.format("%.1f km", totalDistance)).append("\n");
            sb.append("  Total Fare: ₹").append(String.format("%.0f", totalFare)).append("\n");
            sb.append("  CO₂ Saved: ").append(carbonService.formatCO2Saved(co2Saved)).append("\n");
            sb.append("════════════════════════════════════════════════\n");
            
            writer.write(sb.toString());
            writer.flush();
            
        } catch (IOException e) {
            System.err.println("Error saving trip history: " + e.getMessage());
        }
    }
}
