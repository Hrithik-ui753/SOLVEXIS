package solvexis.service;

import solvexis.data.TripHistory;
import solvexis.model.Station;
import solvexis.service.transport.LastMileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileHandler {

    public static void saveTrip(String userName,
                                List<Station> route,
                                LastMileService.TransportOption lastMile,
                                String destination,
                                double metroFare) {
        TripHistory.saveCompleteTripForUser(userName, route, lastMile, destination, metroFare);
    }

    public static UserStatsService loadStats(String userName) {
        return new UserStatsService(userName);
    }

    public static void recordTrip(String userName,
                                  List<Station> route,
                                  double lastMileDistance,
                                  double co2Saved) {
        UserStatsService stats = new UserStatsService(userName);
        stats.recordTrip(route, lastMileDistance, co2Saved);
    }

    public static String loadTripHistory(String userName) {
        String fileName;
        if (userName != null && !userName.trim().isEmpty()) {
            fileName = "trip_history_" + userName.trim() + ".txt";
        } else {
            fileName = "trip_history.txt";
        }
        try {
            Path p = Paths.get(fileName);
            if (!Files.exists(p)) {
                return "No trip history yet.";
            }
            List<String> lines = Files.readAllLines(p);
            StringBuilder sb = new StringBuilder();
            for (String l : lines) {
                sb.append(l).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "Error reading trip history: " + e.getMessage();
        }
    }
}


