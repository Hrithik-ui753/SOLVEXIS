package solvexis.service;

import solvexis.model.Station;
import java.util.*;

public class PlatformCrowdDensityService {
    private static final double PLATFORM_AREA = 250.0;
    private Map<String, Double> stationCrowdDensity;
    
    public PlatformCrowdDensityService() {
        stationCrowdDensity = new HashMap<>();
        initializeCrowdLevels();
    }
    
    private void initializeCrowdLevels() {
        Random random = new Random();
        for (String stationName : getAllStationNames()) {
            double pcdi = 0.5 + random.nextDouble() * 4.0;
            stationCrowdDensity.put(stationName, pcdi);
        }
    }
    
    private List<String> getAllStationNames() {
        return Arrays.asList(
            "Miyapur", "JNTU College", "KPHB Colony", "Kukatpally", "Balanagar",
            "Moosapet", "Bharat Nagar", "Erragadda", "ESI Hospital", "SR Nagar",
            "Ameerpet", "Punjagutta", "Irrum Manzil", "Khairatabad", "Lakdi-ka-pul",
            "Assembly", "Nampally", "Gandhi Bhavan", "Osmania Medical College", "MGBS",
            "Malakpet", "New Market", "Musarambagh", "Dilsukhnagar", "Chaitanyapuri",
            "Victoria Memorial", "LB Nagar",
            "Nagole", "Uppal", "Stadium", "NGRI", "Habsiguda", "Tarnaka",
            "Mettuguda", "Secunderabad East", "Parade Ground", "Paradise", "Rasoolpura",
            "Prakash Nagar", "Begumpet", "Madhura Nagar", "Yusufguda",
            "Road No. 5 Jubilee Hills", "Jubilee Hills Check Post", "Peddamma Gudi",
            "Madhapur", "Durgam Cheruvu", "Hitec City", "Raidurg",
            "JBS Parade Ground", "Secunderabad West", "Gandhi Hospital", "Musheerabad",
            "RTC Cross Roads", "Chikkadpally", "Narayanaguda", "Sultan Bazaar"
        );
    }
    
    public double getCrowdDensity(String stationName) {
        return stationCrowdDensity.getOrDefault(stationName, 1.0);
    }
    
    public int getCrowdLevel(String stationName) {
        double pcdi = getCrowdDensity(stationName);
        if (pcdi < 1.0) {
            return 1;
        } else if (pcdi < 2.0) {
            return 2;
        } else if (pcdi < 3.0) {
            return 3;
        } else if (pcdi < 4.0) {
            return 4;
        } else {
            return 5;
        }
    }
    
    public String getCrowdLevelDescription(int level) {
        switch (level) {
            case 1: return "Comfortable - Plenty of Space";
            case 2: return "Moderate Crowding";
            case 3: return "Busy but Manageable";
            case 4: return "Uncomfortable - Movement Restricted";
            case 5: return "Critical Level - Unsafe Conditions";
            default: return "Unknown Level";
        }
    }
    
    public String getCrowdLevelEmoji(int level) {
        switch (level) {
            case 1: return "🟢";
            case 2: return "🟡";
            case 3: return "🟠";
            case 4: return "🔴";
            case 5: return "⛔";
            default: return "⚪";
        }
    }
    
    public void displayCrowdDensityForStations(List<Station> stations) {
        System.out.println("\n════════ Platform Crowd Density Levels ════════");
        System.out.println("PCDI (Platform Crowd Density Index) = Persons per m²");
        System.out.println();
        
        for (Station station : stations) {
            String stationName = station.getName();
            double pcdi = getCrowdDensity(stationName);
            int level = getCrowdLevel(stationName);
            String emoji = getCrowdLevelEmoji(level);
            String description = getCrowdLevelDescription(level);
            
            System.out.printf("%s %-25s PCDI: %.2f persons/m² (Level %d - %s)%n", 
                emoji, stationName, pcdi, level, description);
        }
        System.out.println("═════════════════════════════════════════════");
    }
    
    public void displayCrowdDensityForStation(String stationName) {
        double pcdi = getCrowdDensity(stationName);
        int level = getCrowdLevel(stationName);
        String emoji = getCrowdLevelEmoji(level);
        String description = getCrowdLevelDescription(level);
        
        System.out.println("\n════════ Station Crowd Information ════════");
        System.out.printf("%s Station: %s%n", emoji, stationName);
        System.out.printf("   PCDI Level: %.2f persons per m² (Level %d)%n", pcdi, level);
        System.out.printf("   Status: %s%n", description);
        System.out.println("═════════════════════════════════════════");
    }
}

