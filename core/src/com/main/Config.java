package com.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

/**
 * This class contains Maps with values loaded from the config file. The config file (config.txt)
 * contains the information about available types of units, towers, missiles and maps. Entities and missiles load some of
 * their basic attributes and textures using this class based on the provided type (as defined in the config file.
 * This class also stores paths to map textures and their occupancy grid (boolean array containing information
 * which grid cells are blocked by default on a certain map).<p>
 *
 * The code in this class loads all the information from the config file to static maps. The key
 * in every map is the type name (as defined in the config file).<br>
 * Comments are possible in the config file - they must start with '#' as the first char in the line.<p>
 * Sample syntax of a type defined in the config file:<br>
 *     type_name:<br>
 *         cost: 1<br>
 *         reward: 1<br>
 *         width: 1<br>
 *         height: 1<br>
 *         damage: 1<br>
 *         range: 1<br>
 *         reload: 1<br>
 *         hp: 1<br>
 *         speed: 1<br>
 *         representativeTexture: relative/path/to/texture.png<br>
 *         fullTexture: relative/path/to/texture<br>
 *<br>
 * Sample syntax of a map defined in the config file:<br>
 *     map_name:<br>
 *         representativeTexture: relative/path/to/texture.png<br>
 *     grid:<br>
 *         010001000<br>
 *         010101010<br>
 *         010101010<br>
 *         000100010<br>
 *             <br>
 * Every attribute is optional for a given type_name or map_name (grid only makes sense for maps, speed only makes
 * sense for moving objects - units and missiles)
 * @author Piotr Libera
 */
public class Config {
    public static final Map<String, Integer> objectCost;
    public static final Map<String, Integer> objectReward;
    public static final Map<String, Float> width;
    public static final Map<String, Float> height;
    public static final Map<String, Integer> damage;
    public static final Map<String, Float> range;
    public static final Map<String, Float> reloadTime;
    public static final Map<String, Integer> hp;
    public static final Map<String, Float> speed;
    public static final Map<String, String> representativeTexture;
    public static final Map<String, String> fullTexture;
    public static final Map<String, Boolean[][]> mapGrid;

    public static final int startingCoins = 2000;
    public static final float refreshRate = 10f;
    /**
     *  These parameters set how far main towers will be from map borders. By default, main towers
     *  are placed at different corners of the map. These parameters set the distance from the border
     *  both in the x axis and y axis.
      */
    public static final int mainTowerToMapBorderX = 80;
    public static final int mainTowerToMapBorderY = 100;


    /**
     * Creates occupancy grid array from Vector of rows represented as Strings - these Strings consist
     * of 0s and 1s and are read from the config file.
     * @param gridRows Rows of the grid stored in Strings
     * @return 2D Boolean array containing the grid (<code>true</code> means that a cell is blocked by default)
     */
    static Boolean[][] createGrid(Vector<String> gridRows) {
        if(gridRows.size() > 0) {
            Boolean[][] grid = new Boolean[gridRows.size()][gridRows.elementAt(0).length()];
            for(int x = 0; x < gridRows.size(); ++x) {
                for(int y = 0; y < gridRows.elementAt(x).length(); ++y) {
                    if(gridRows.elementAt(x).charAt(y) == '0') {
                        grid[x][y] = false;
                    }
                    else {
                        grid[x][y] = true;
                    }
                }
            }
            return grid;
        }
        return new Boolean[0][0];
    }


    static {
        Map<String, Integer> tempObjectCost = new HashMap<>();
        Map<String, Integer> tempObjectReward = new HashMap<>();
        Map<String, Float> tempWidth = new HashMap<>();
        Map<String, Float> tempHeight = new HashMap<>();
        Map<String, Integer> tempDamage = new HashMap<>();
        Map<String, Float> tempRange = new HashMap<>();
        Map<String, Float> tempReloadTime = new HashMap<>();
        Map<String, Integer> tempHp = new HashMap<>();
        Map<String, Float> tempSpeed = new HashMap<>();
        Map<String, String> tempRepresentativeTexture = new HashMap<>();
        Map<String, String> tempFullTexture = new HashMap<>();
        Map<String, Boolean[][]> tempMapGrid = new HashMap<>();

        try {
            File configFile = new File("core/assets/config/config.txt");
            Scanner reader = new Scanner(configFile);
            String type = "";
            String line;
            String attribute;
            String value;
            String[] data;
            int counter = 0;
            boolean readingGrid = false;
            Vector<String> gridRows = new Vector<>();
            while (reader.hasNextLine()) {
                counter++;
                line = reader.nextLine().trim();
                if(line.length() == 0) {
                    if(readingGrid) {
                        readingGrid = false;
                        tempMapGrid.put(type, createGrid(gridRows));
                    }
                    continue;
                }
                if(line.charAt(0) == '#')
                    continue;
                data = line.split(":");
                if(data.length == 1) {
                    data[0] = data[0].trim();
                    if(readingGrid) {
                        if(data[0].charAt(0) == '0' || data[0].charAt(0) == '1') {
                            gridRows.add(data[0]);
                        }
                        else {
                            readingGrid = false;
                            tempMapGrid.put(type, createGrid(gridRows));
                            type = data[0];
                        }
                    }
                    else if(data[0].equals("grid")) {
                        readingGrid = true;
                        gridRows = new Vector<String>();
                    }
                    else
                        type = data[0];
                }
                else if(data.length == 2) {
                    readingGrid = false;
                    attribute = data[0].trim();
                    value = data[1].trim();
                    if(attribute.equals("cost"))
                        tempObjectCost.put(type, Integer.parseInt(value));
                    else if(attribute.equals("reward"))
                        tempObjectReward.put(type, Integer.parseInt(value));
                    else if(attribute.equals("width"))
                        tempWidth.put(type, Float.parseFloat(value));
                    else if(attribute.equals("height"))
                        tempHeight.put(type, Float.parseFloat(value));
                    else if(attribute.equals("damage"))
                        tempDamage.put(type, Integer.parseInt(value));
                    else if(attribute.equals("range"))
                        tempRange.put(type, Float.parseFloat(value));
                    else if(attribute.equals("reload"))
                        tempReloadTime.put(type, Float.parseFloat(value));
                    else if(attribute.equals("hp"))
                        tempHp.put(type, Integer.parseInt(value));
                    else if(attribute.equals("speed"))
                        tempSpeed.put(type, Float.parseFloat(value));
                    else if(attribute.equals("representativeTexture"))
                        tempRepresentativeTexture.put(type, value);
                    else if(attribute.equals("fullTexture"))
                        tempFullTexture.put(type, value);
                }
                else {
                    readingGrid = false;
                    System.out.println("Syntax error found at line "+counter+" of the config file");
                }
            }
            if(readingGrid) {
                tempMapGrid.put(type, createGrid(gridRows));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        objectCost = Collections.unmodifiableMap(tempObjectCost);
        objectReward = Collections.unmodifiableMap(tempObjectReward);
        width = Collections.unmodifiableMap(tempWidth);
        height = Collections.unmodifiableMap(tempHeight);
        damage = Collections.unmodifiableMap(tempDamage);
        range = Collections.unmodifiableMap(tempRange);
        reloadTime = Collections.unmodifiableMap(tempReloadTime);
        hp = Collections.unmodifiableMap(tempHp);
        speed = Collections.unmodifiableMap(tempSpeed);
        representativeTexture = Collections.unmodifiableMap(tempRepresentativeTexture);
        fullTexture = Collections.unmodifiableMap(tempFullTexture);
        mapGrid = Collections.unmodifiableMap(tempMapGrid);
    }
}
