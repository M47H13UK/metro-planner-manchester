

//So we want a graph where each station is a node and traveling between each station is
// an edge. Its undirected as specified in the instructions becuase traveling both directions
// take the same time and its weighted since travel take different time between staitons.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * Undirected weighted graph built from given csv.
 * 
 * Each csv line is an edge with:
 * <ul>
 *    <li>Start Station</li>
 *    <li>End Station</li>
 *    <li>Line Colour</li>
 *    <li>Minutes Takes</li>
 * </ul>
 * 
 * This graph is used in the search algo here: {@link SearchWithSpec}
 * 
 * Can call methods:
 * <ul>
 *    <li>{@link #neighbouringStations(String)} returns all neighbouring outdoing edges from a station.</li>
 *    <li>{@link #linesColoursAtStation(String)} returns the colours present in that station.</li>
 * </ul>
 * 
 */

public class MetroGraph{

    public final String csvPath;

    public static class Edge {
        public final String startStation;
        public final String endStation;
        public final String lineColour;
        public final double minutesTaken;

        public Edge(String startStation, String endStation, String lineColour, double minsTaken) {
            this.startStation = startStation;
            this.endStation = endStation;
            this.lineColour = lineColour;
            this.minutesTaken = minsTaken;
        }

    }

    // so here we map string which is station name to a list of edges going OUT of that station.
    private Map<String, List<Edge>> outgoingStationsFromAStation = new HashMap<>();

    // here its a map from stations to line colours it has. Used for the start known nodes unexplored edges.
    // since we got to know all colours for the start station.
    private Map<String, Set<String>> lineColoursAtAStation = new HashMap<>();


    /**To add an edge into the Map */
    private void addEdgeToMap(String startStation, String endStation, String colour, double mins) {

        List<Edge> list = outgoingStationsFromAStation.get(startStation);
        if (list == null) { // if that station doesnt' exist yet make a new one.
            list = new ArrayList<>();
            outgoingStationsFromAStation.put(startStation, list);
        }
        list.add(new Edge(startStation, endStation, colour, mins));

        // same thing with colour, add it to existing list or make a new one if not found.
        Set<String> colours = lineColoursAtAStation.get(startStation);
        if (colours == null) {
            colours = new HashSet<>();
            lineColoursAtAStation.put(startStation, colours);
        }
        colours.add(colour);
    }
    
    public MetroGraph(String csvPath) {
        this.csvPath = csvPath;
        makeEdgesMapFromCsv();
    }


    /** Reads the csv and adds the data as edges. Adds for both directions.*/
    public void makeEdgesMapFromCsv() {
        try (BufferedReader csvLine = new BufferedReader(new FileReader(csvPath))) {

            csvLine.readLine(); //to skip the first line in csv with title values stuff.

            String row;
            while ((row = csvLine.readLine()) != null) {
                String[] p = row.split(",");
                String startSatation = p[0].trim();
                String endStation = p[1].trim();
                String lineColour = p[2].trim();
                double minutesTaken = Double.parseDouble(p[3].trim());

                //store both directions so the graph is undirected
                addEdgeToMap(startSatation, endStation, lineColour, minutesTaken);
                addEdgeToMap(endStation, startSatation, lineColour, minutesTaken);
            }

        } catch (IOException error) {
            System.out.println("Problem reading CSV: " + error.getMessage());
        }
    }

    /**
     * @param station station name
     * @return Returns all outgoing edgest from a station. Empty list if none.
     */
    public List<Edge> neighbouringStations(String station) {
        List<Edge> list = outgoingStationsFromAStation.get(station);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    /**
     * @param station station name
     * @return Returns all line colours that stop at a station. Empty if none.
     */
    //get line colours for a station, return empty set if none.
    public Set<String> linesColoursAtStation(String station) {
        Set<String> set = lineColoursAtAStation.get(station);
        if (set == null) {
            set = Collections.emptySet();
        }
        return set;
    }


}