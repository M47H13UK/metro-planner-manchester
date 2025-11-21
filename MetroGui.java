
//For javadocs
//javadoc -d javaDocs MetroGui.java SearchWithSpec.java MetroGraph.java

import java.awt.Dimension;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;

/**
 * Uses swing libarary for the UI.
 * <p>GUI lets the user pick:</p>
 * <ul>
 *   <li>A start station</li>
 *   <li>A end station</li>
 *   <li>A journey constraint: “Fastest Time” or “Least Amount Of Changes”</li>
 * </ul>
 * 
 * <p>It then calls the corresponding search algorithm in
 * {@link SearchWithSpec} and prints a formatted itinerary.</p>
 */
public class MetroGui extends JFrame {

    //reminder static means it belongs to that class and final is you can't change it.
    private static final String csvPath = "Metrolink_times_linecolour.csv";

    //Store the station names for use in the GUI.
    private final List<String> allStationNames;

    private static final double timeToChangeTrain = 2.0;
    private MetroGraph metroGraphLoaded;


    /**
     * This gets and parses all station names from the csv to
     * show them in the drop downs for start and end station.
     *
     * @param csvPath File path to the Metrolink CSV file
     * @return a {@code List} of distinct station names, already sorted.
     */
    static List<String> getAndParseStationNamesFromCSV(String csvPath) {
        // here treeset is an implementation of a set to force uniquness.
        // its better than hashSet here since it keeps the items alphabetically sorted. So in a way a human would
        // read it in a drop down. We could also use hashSet then turn to list then sort.
        Set<String> stationNames = new TreeSet<>();

        try (BufferedReader csvLine = Files.newBufferedReader(Paths.get(csvPath))) {
            csvLine.readLine(); //to skip the first line in csv with title values.
            String line;
            while ((line = csvLine.readLine()) != null) {
                String[] parts = line.split(",");
                stationNames.add(parts[0]);
                stationNames.add(parts[1]);
            }
        } catch (IOException error){
            System.out.println("Error reading: "+ csvPath);
        }
        // here arrayList from java.util converts Set<String> to List<String> so we can
        // output correct for getAndParseStationNamesFromCSV.
        return new ArrayList<>(stationNames);
    }

    /**
     * Main function that launches the GUI and initiates the graph construction.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            List<String> allStations = getAndParseStationNamesFromCSV(csvPath);

            MetroGraph metroGraphLoaded = new MetroGraph(csvPath);

            // call constructor for the calss. THis builds and shows the window.
            new MetroGui(allStations, metroGraphLoaded); 

        });
        
    }

    /**
     * Constructs the window, lays out all Swing widgets, and sets
     * the action listener to trigger search for the user based on their parameters. 
     *
     * @param allStations Alphabetically sorted station name list
     * @param metroGraphLoaded graph built from the same csv. Can be found in {@link MetroGraph}
     */
    public MetroGui(List<String> allStations, MetroGraph metroGraphLoaded) {
        //calls constructor of JFrame which takes in a string to set a title of window.
        //you cna always set title later btw, like calling setTitle()
        super("Metro Planner Summer Project");

        this.allStationNames = allStations; // initialise the allStationNames var with the ones we get from getAndParseStationNamesFromCSV.
        this.metroGraphLoaded = metroGraphLoaded;


        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(350, 600));
        setLocationRelativeTo(null); // this is to center it.

        //create the GUI componenets. Using drop down so JComboBox.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // vertical stack

        JLabel startLabel = new JLabel("Start station:");
        JComboBox<String> startCombo = new JComboBox<>(allStationNames.toArray(new String[0]));

        JLabel endLabel = new JLabel("End station:");
        JComboBox<String> endCombo = new JComboBox<>(allStationNames.toArray(new String[0]));

        JLabel constraintLabel = new JLabel("Select journey constraints:");
        String[] constraintOptions = { "Fastest Time", "Least Amount Of Changes" };
        JComboBox<String> constraintCombo = new JComboBox<>(constraintOptions);

        JButton confirmBtn = new JButton("Confirm");
        //using JTextArea for the confrim message instead of JLabel cuz JLabel didn't want to wrap.
        JTextArea messageArea = new JTextArea(" ");
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setAlignmentX(CENTER_ALIGNMENT);
    

        //To make the dropdowns less big.
        // copies the prefered height into the max. 
        int prefH = startCombo.getPreferredSize().height;
        Dimension max = new Dimension(Integer.MAX_VALUE, prefH);
        startCombo.setMaximumSize(max);
        endCombo.setMaximumSize(max);
        constraintCombo.setMaximumSize(max);


        // add widgets to the panel
        panel.add(startLabel);
        panel.add(startCombo);
        panel.add(Box.createVerticalStrut(25)); // this is 25px spacing.
        panel.add(endLabel);
        panel.add(endCombo);
        panel.add(Box.createVerticalStrut(25));
        panel.add(constraintLabel);
        panel.add(constraintCombo);
        panel.add(Box.createVerticalStrut(25));
        panel.add(confirmBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(messageArea);


        // logic for confirm btn
        confirmBtn.addActionListener(e -> {
            String from = (String) startCombo.getSelectedItem();
            String to = (String) endCombo.getSelectedItem();

            if (from.equals(to)) {
                messageArea.setText("Start and end station can't be the same. You are already here.");
            } else {

                SearchWithSpec.DijkstraAnswer answer;
                if (constraintCombo.getSelectedItem().equals("Fastest Time")) {
                    answer = SearchWithSpec.shortestTime(metroGraphLoaded, timeToChangeTrain, from, to);
                    messageArea.setText(answer.printOutFormatedRoute(true));
                } else {
                    answer = SearchWithSpec.leastAmountOfChanges(metroGraphLoaded, timeToChangeTrain, from, to);
                    messageArea.setText(answer.printOutFormatedRoute(false));
                }

                
            }
        });

        add(panel);

        setVisible(true);

    }

}
