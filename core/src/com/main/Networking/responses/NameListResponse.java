package com.main.Networking.responses;

import java.util.ArrayList;


/**
 * The NameListResponse class is a response from server to client informing him about
 * names of other players in current room. This is used in the waiting room of the game.
 * @author Piotr Satała
 */
public class NameListResponse {
    public ArrayList<String> arrayList;

    /**
     * Public empty constructor for NameListResponse class
     */
    public NameListResponse() {
        arrayList = new ArrayList<>();
    }
}
