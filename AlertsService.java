package solvexis.service;

import solvexis.model.Station;
import java.util.*;
import java.util.concurrent.*;

public class AlertsService {
    private volatile boolean isEnabled = true;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> alertTask;
    private List<Station> currentRoute;

    public AlertsService() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void startJourneyAlerts(List<Station> route) {
        if (route == null || route.isEmpty()) {
            return;
        }
        
        stopAlerts();
        currentRoute = new ArrayList<>(route);

        int totalStations = route.size();
        if (totalStations == 0) return;
        
        long totalTime = 5000;
        long delayBetweenAlerts = totalTime / totalStations;

        alertTask = executor.schedule(() -> {
            if (isEnabled && currentRoute != null) {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < currentRoute.size(); i++) {
                    Station current = currentRoute.get(i);
                    String message;
                    
                    if (i == 0) {
                        message = "✓ Departing from: " + current.getName();
                    } else if (i == currentRoute.size() - 1) {
                        message = "✓ Arrived at destination: " + current.getName();
                    } else {
                        message = "→ Next station: " + current.getName() + 
                                 " (Corridor " + current.getCorridorNumber() + ")";
                    }
                    
                    System.out.println("[ALERT] " + message);
                    
                    // Check for lane/corridor change
                    if (i > 0 && i < currentRoute.size()) {
                        Station previous = currentRoute.get(i - 1);
                        if (previous.getCorridorNumber() != current.getCorridorNumber()) {
                            String corridorName = getCorridorName(current.getCorridorNumber());
                            String prevCorridorName = getCorridorName(previous.getCorridorNumber());
                            System.out.println("[LANE CHANGE ALERT] ⚠️  Lane changed from " + prevCorridorName + 
                                             " to " + corridorName + " at " + current.getName());
                        }
                    }
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remainingTime = totalTime - elapsed;
                    if (i < currentRoute.size() - 1) {
                        long nextDelay = Math.min(delayBetweenAlerts, remainingTime / (currentRoute.size() - i - 1));
                        if (nextDelay > 0) {
                            try {
                                Thread.sleep(nextDelay);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
                System.out.println("[ALERT] Journey alerts completed!\n");
                stopAlerts();
            }
        }, 0, TimeUnit.MILLISECONDS);
    }
    
    private String getCorridorName(int corridorNumber) {
        switch(corridorNumber) {
            case 1: return "Red Line (Corridor I)";
            case 2: return "Blue Line (Corridor II)";
            case 3: return "Green Line (Corridor III)";
            default: return "Unknown Corridor";
        }
    }

    public void stopAlerts() {
        if (alertTask != null && !alertTask.isCancelled()) {
            alertTask.cancel(true);
        }
    }

    public void toggleAlerts() {
        isEnabled = !isEnabled;
        System.out.println("\nAlerts " + (isEnabled ? "enabled" : "disabled"));
    }

    public void shutdown() {
        stopAlerts();
        executor.shutdownNow();
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}