package com.main.Networking;


/**
 * The NamePair class is used to represent a player connected to a server.
 * It consists of a connection id and player name, hence the name of this class.
 * @see MainServer
 * @see LocalServer
 * @author Piotr Satała
 */
public class NamePair {
    private Integer id;
    private String name;


    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public NamePair() {
    }


    /**
     * Public constructor for NamePair class.
     * @param id id of player connection to the server
     * @param name player name
     */
    public NamePair(Integer id, String name) {
        this.id = id;
        this.name = name;
    }


    /**
     * Getter for connection id
     * @return id of the connection
     */
    public Integer getId() {
        return id;
    }


    /**
     * Setter for connection id
     * @param id new id of the connection
     */
    public void setId(Integer id) {
        this.id = id;
    }


    /**
     * Getter for player name
     * @return player name
     */
    public String getName() {
        return name;
    }


    /**
     * Setter for player name
     * @param name new player name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Getter for key - connection id
     * @return connection id
     */
    public Integer getKey() {
        return getId();
    }


    /**
     * Getter for value - player name
     * @return player name
     */
    public String getValue() {
        return getName();
    }
}
