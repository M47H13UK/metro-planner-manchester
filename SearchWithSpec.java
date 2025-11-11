

import java.util.*;

/**
 * Searching algo using Dijkstra for a {@link MetroGraph}.
 * <p>
 * The class contains 2 implementation of Dijkstra search:
 * </p>
 * <ul>
 *   <li>{@link #shortestTime} for shortest time in minutes</li>
 *   <li>{@link #leastAmountOfChanges} for least amount of changes (line colour changes)</li>
 * </ul>
 *
 */
public class SearchWithSpec {

    // StationNode is declared as Java record so we get constructor, equals, hasCode, toString by default.
    // mainly used for equals.
    public record StationNode(String station, String line) { }

    /**
     * Object to represent the result of our search.
     *
     * @param path ordered list of {@link StationNode}s from start to goal (empty if no path exists)
     * @param totalMinutes total amount of minutes for the journey.
     * @param numOfChanges how many times the line colour changed along {@code path}
     */
    public static class DijkstraAnswer {
        public final List<StationNode> path;
        public final double totalMinutes;
        public final int numOfChanges;

        public DijkstraAnswer(List<StationNode> nodePath, double mins, int changes) {
            path = nodePath; 
            totalMinutes = mins; 
            numOfChanges = changes;
        }

        public String printOutFormatedRoute(boolean isFastestTime) {
            if (path.isEmpty()) return "No route found.";
            StringBuilder buildingTheString = new StringBuilder();
            String previousNodeForPathLine = path.get(0).line();
            
            if (isFastestTime) {
                buildingTheString.append("*** Minimal Time Route ***\n");
            } else {
                buildingTheString.append("*** Route with Fewest Changes ***\n");
            }
                
            for (StationNode currentNode : path) {
                if (!currentNode.line().equals(previousNodeForPathLine)) {
                    buildingTheString.append("** Change Line to ");
                    buildingTheString.append(currentNode.line());
                    buildingTheString.append(" line **\n");
                }
                buildingTheString.append(currentNode.station());
                buildingTheString.append(" on ");
                buildingTheString.append(currentNode.line());
                buildingTheString.append(" line\n");
                previousNodeForPathLine = currentNode.line();
            }
            buildingTheString.append("\nOverall Journey Time (mins) = ");
            buildingTheString.append(totalMinutes);
            buildingTheString.append("\nNumber of Changes = ");
            buildingTheString.append(numOfChanges);
            return buildingTheString.toString();
        }
    }

    /**
     * Given a graph we find the journey with the shortest amount of minutes.
     * Use Dijkstra implementation for fastest route.
     * Every line change adds {@code changeTime} minutes in addition to the
     * time specified in the csv.
     *
     * @param metroGraph the graph built from the csv
     * @param changeTime minutes to add when switching line colours.
     * @param startStation name of the start station
     * @param goalStation name of the end station
     * @return returns DijkstraAnswer object with the fastest journey and route. Empty if no result found.
     */
    public static DijkstraAnswer shortestTime(MetroGraph metroGraph, double changeTime, String startStation, String goalStation) {
        
        // store in map fastest mins for each station
        Map<StationNode, Double> fastestMinutesToNodeMap = new HashMap<>();

        // stores the fastest previous StationNode to reconstruct the path later. 
        Map<StationNode, StationNode> previousNodeForPathMap = new HashMap<>();

        // knownUnexploredNodes is all StationNode we knwo about but havent processed yet.
        List<StationNode> knownUnexploredNodes = new ArrayList<>();

        
        // for each colour at our start station we create a new StationNode, set it to minutes 0 and add it to the knownUnexploredNodes.
        for (String colour : metroGraph.linesColoursAtStation(startStation)) {
            StationNode startNode = new StationNode(startStation, colour);
            fastestMinutesToNodeMap.put(startNode, 0.0);
            knownUnexploredNodes.add(startNode);
        }

        StationNode bestNode = null; // will hold the fastest StationNode. 

        while (!knownUnexploredNodes.isEmpty()) {

            // here we find the knownUnexploredNodes StationNode with the least amount of minutes.
            //btw when using hashmap get gets the value for the key passed in the ().
            // and for array lsit, get gets the index passed in the ().
            StationNode current = knownUnexploredNodes.get(0);
            for (StationNode node : knownUnexploredNodes) {
                if (fastestMinutesToNodeMap.get(node) < fastestMinutesToNodeMap.get(current)) {
                    current = node;
                }
            }
            knownUnexploredNodes.remove(current); //remove since we going to process it. So its no longer unexplored.


            //if the current which is the fasterst on the knownUnexploredNodes is the same station
            //as the goal station then we done.
            if (current.station().equals(goalStation)) {
                bestNode = current;
                break;
            }

            double currentMinutes = fastestMinutesToNodeMap.get(current);

            
            //For every neighbouring Edge that starts at current.station()
            for (MetroGraph.Edge edge : metroGraph.neighbouringStations(current.station())) {
                if (edge.lineColour.equals(current.line())) {
                    StationNode neighbour = new StationNode(edge.endStation, current.line()); // same colour
                    double newMinutes = currentMinutes + edge.minutesTaken;

                    //if this route is faster, record it and push surrounding neighbours to knownUnexploredNodes
                    if (!fastestMinutesToNodeMap.containsKey(neighbour) || newMinutes < fastestMinutesToNodeMap.get(neighbour)) {
                        //put replaces the existing value(if it already exists, adds it if it doens't exist yet). 
                        //So we update it with the new fastest minutes and keep track of the
                        //new fastest path we took to get here.
                        fastestMinutesToNodeMap.put(neighbour, newMinutes);
                        previousNodeForPathMap.put(neighbour, current);
                        knownUnexploredNodes.add(neighbour);
                    }
                } else {
                    StationNode neighbourDifColour = new StationNode(current.station(), edge.lineColour);
                    double newMinutes = currentMinutes + changeTime;

                    if (!fastestMinutesToNodeMap.containsKey(neighbourDifColour) || newMinutes < fastestMinutesToNodeMap.get(neighbourDifColour)) {
                        fastestMinutesToNodeMap.put(neighbourDifColour, newMinutes);
                        previousNodeForPathMap.put(neighbourDifColour, current);
                        if (!knownUnexploredNodes.contains(neighbourDifColour)) {
                            knownUnexploredNodes.add(neighbourDifColour);
                        }
                    }
                }

                
            }

        }

        // if there is no fastest node, then there is no path.
        if (bestNode == null) {
            List<StationNode> emptyPath = new ArrayList<>();
            return new DijkstraAnswer(emptyPath, 0, 0);
        } 

        
        // reconstruct the path by going backwards on previousNodeForPathMap.
        // Then after the for look we reverse it so that its backwards and therefore from start to end.
        List<StationNode> path = new ArrayList<>();
        for (StationNode fastestNode = bestNode; fastestNode != null; fastestNode = previousNodeForPathMap.get(fastestNode)) {
            path.add(fastestNode);
        }
        // index 0 would we our end so we don't want to start with that, we need to
        // reverse it so that we start with out starting point. So the print is corrent when we display the lines.
        Collections.reverse(path);



        //Count how many times we changed colours.
        int changes = 0;
        for (int i = 1; i < path.size(); i++) {
            if (!path.get(i).line().equals(path.get(i - 1).line())) {
                changes++;
            }
        }


        //return everything packaged in a DijkstraAnswer object.
        double totalMinutes = fastestMinutesToNodeMap.get(bestNode);
        return new DijkstraAnswer(path, totalMinutes, changes);
    }




    /**
     * Given a graph we find the journey with the least amount of line changes.
     * If multiple choices have the same minimal number of changes, the one
     * with the shorter total minutes is used.
     *
     * @param metroGraph the graph built from the csv
     * @param changeTime minutes to add when switching line colours.
     * @param startStation start station
     * @param goalStation end station
     * @return returns DijkstraAnswer object with journey with least amount of line changes. Empty if no result found.
     */
    public static DijkstraAnswer leastAmountOfChanges(MetroGraph metroGraph, double changeTime, String startStation, String goalStation) {
        // store the fewest changes needed to reach a station
        Map<StationNode, Integer> fewestChangesToNodeMap = new HashMap<>();

        // store in map fastest mins for each station
        Map<StationNode, Double> fastestMinutesToNodeMap = new HashMap<>();

        // stores the fastest previous StationNode to reconstruct the path later. 
        Map<StationNode, StationNode> previousNodeForPathMap = new HashMap<>();

        // knownUnexploredNodes is all StationNode we knwo about but havent processed yet.
        List<StationNode> knownUnexploredNodes = new ArrayList<>();

        // for each colour at our start station we create a new StationNode, set it to minutes 0 and add it to the knownUnexploredNodes.
        for (String colour : metroGraph.linesColoursAtStation(startStation)) {
            StationNode startNode = new StationNode(startStation, colour);
            fewestChangesToNodeMap.put(startNode, 0);
            fastestMinutesToNodeMap.put(startNode, 0.0);
            knownUnexploredNodes.add(startNode);
        }

        StationNode bestNode = null; // will hold the fastest StationNode. 

        while (!knownUnexploredNodes.isEmpty()) {

            //Find the node with the least amount of changes from the knownUnexploredNodes.
            StationNode current = knownUnexploredNodes.get(0);
            for (StationNode node : knownUnexploredNodes) {
                int nChanges = fewestChangesToNodeMap.containsKey(node) ? fewestChangesToNodeMap.get(node) : Integer.MAX_VALUE;

                int currentChange = fewestChangesToNodeMap.containsKey(current) ? fewestChangesToNodeMap.get(current) : Integer.MAX_VALUE;

                if (nChanges < currentChange) {
                    current = node;
                } else if (nChanges == currentChange && fastestMinutesToNodeMap.get(node) < fastestMinutesToNodeMap.get(current)) { // if equal take the one with fastest time.
                    current = node;
                }
            }
            knownUnexploredNodes.remove(current); //remove since we going to process it. So its no longer unexplored.

            //if same as goal then we are done.
            if (current.station().equals(goalStation)) {
                bestNode = current;
                break;
            }


            int currentChanges = fewestChangesToNodeMap.get(current);
            double currentMinutes = fastestMinutesToNodeMap.get(current);

            // for every neighbouring edge from our current station.
            for (MetroGraph.Edge edge : metroGraph.neighbouringStations(current.station())) {

                if (edge.lineColour.equals(current.line())) {
                    // same line colour no extra time.
                    StationNode neighbour = new StationNode(edge.endStation, current.line());
                    int newChanges = currentChanges; // don't modify changes count since didn't change line colour.
                    double newMinutes = currentMinutes + edge.minutesTaken;

                    // If that route is with less changes then add to unexplored nodes to proeccess later, 
                    // also if they have same changes number then pick the faster one.
                    if (!fewestChangesToNodeMap.containsKey(neighbour) || newChanges < fewestChangesToNodeMap.get(neighbour) || (newChanges == fewestChangesToNodeMap.get(neighbour) && newMinutes < fastestMinutesToNodeMap.get(neighbour))) {
                        fewestChangesToNodeMap.put(neighbour, newChanges);
                        fastestMinutesToNodeMap.put(neighbour, newMinutes);
                        previousNodeForPathMap.put(neighbour, current);
                        knownUnexploredNodes.add(neighbour);
                    }

                } else {
                    // Different line colour so add extra 2 mins and add +1 to changes.
                    StationNode switchColour = new StationNode(current.station(), edge.lineColour);
                    int newChanges = currentChanges + 1; // Since we change add 1
                    double newMinutes = currentMinutes + changeTime;

                    if (!fewestChangesToNodeMap.containsKey(switchColour) || newChanges < fewestChangesToNodeMap.get(switchColour) || (newChanges == fewestChangesToNodeMap.get(switchColour) && newMinutes < fastestMinutesToNodeMap.get(switchColour))) {
                        fewestChangesToNodeMap.put(switchColour, newChanges);
                        fastestMinutesToNodeMap.put(switchColour, newMinutes);
                        previousNodeForPathMap.put(switchColour, current);
                        if (!knownUnexploredNodes.contains(switchColour)) {
                            knownUnexploredNodes.add(switchColour);
                        }
                    }
                }
            }
        }

        // if there is no node, then there is no path.
        if (bestNode == null) {
            List<StationNode> emptyPath = new ArrayList<>();
            return new DijkstraAnswer(emptyPath, 0, 0);
        } 

        
        // reconstruct the path by going backwards on previousNodeForPathMap.
        // Then after the for look we reverse it so that its backwards and therefore from start to end.
        List<StationNode> path = new ArrayList<>();
        for (StationNode fewestChgsNode = bestNode; fewestChgsNode != null; fewestChgsNode = previousNodeForPathMap.get(fewestChgsNode)) {
            path.add(fewestChgsNode);
        }
        // index 0 would we our end so we don't want to start with that, we need to
        // reverse it so that we start with out starting point. So the print is corrent when we display the lines.
        Collections.reverse(path);



        //Count how many times we changed colours.
        int totalChanges = fewestChangesToNodeMap.get(bestNode);


        //return everything packaged in a DijkstraAnswer object.
        double totalMinutes = fastestMinutesToNodeMap.get(bestNode);
        return new DijkstraAnswer(path, totalMinutes, totalChanges);


    }

    

}