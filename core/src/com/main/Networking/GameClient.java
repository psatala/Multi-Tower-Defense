/**
  The GameClient class is the main class to be run by the user. It handles connecting with main server,
  creating global games and hosting local games by starting a local server.
  @author Piotr Satała
 */

package com.main.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Client;

import com.main.GameManager;
import com.main.Networking.requests.*;
import com.main.Networking.responses.*;

public class GameClient {

    private Client client;
    private Client localClient;
    private Client activeClient;
    private Scanner inputScanner = null;
    public String playerName;
    public int roomID;
    private RoomList roomList = null;
    private GameManager observer;

    private int tcpSecondPortNumber;
    private int udpSecondPortNumber;
    private int maxDelay;

    /**
     * Public constructor for GameClient class
     * @param tcpPortNumber tcp port of main server
     * @param udpPortNumber udp port of main server
     * @param tcpSecondPortNumber tcp port to host local games on
     * @param udpSecondPortNumber udp port to host local games on
     * @param maxDelay timeout in milliseconds for connecting and host discovery
     */
    public GameClient(int tcpPortNumber, int udpPortNumber, int tcpSecondPortNumber, int udpSecondPortNumber,
            int maxDelay) throws IOException, InterruptedException {

        this.tcpSecondPortNumber = tcpSecondPortNumber;
        this.udpSecondPortNumber = udpSecondPortNumber;
        this.maxDelay = maxDelay;

        client = new Client();
        localClient = new Client();
        roomList = new RoomList();

        // register classes
        Network.register(client);
        Network.register(localClient);



        // listener for client connected to the global server
        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof GameResponse) { //response with game data
                    GameResponse gameResponse = (GameResponse) object;
                    if(gameResponse != null & observer != null)
                        observer.updatesListener.updatesReceived(gameResponse);

                } else if(object instanceof RewardResponse) { //response with rewards
                    RewardResponse rewardResponse = (RewardResponse)object;
                    if(rewardResponse != null & observer != null)
                        observer.updatesListener.updatesReceived(rewardResponse);

                } else if (object instanceof ControlResponse) { //control response
                    ControlResponse controlResponse = (ControlResponse) object;
                    System.out.println(controlResponse.getMessage());

                } else if (object instanceof RoomList) { //list of available rooms on global server
                    RoomList mainServerRoomList = (RoomList) object;
                    roomList.putALL(mainServerRoomList);
                    synchronized(client) {
                        client.notify();
                    }

                } else if (object instanceof RoomCreatedResponse) { //room successfully created
                    RoomCreatedResponse roomCreatedResponse = (RoomCreatedResponse) object;
                    roomID = roomCreatedResponse.roomID;
                    synchronized(client) {
                        client.notify();
                    }
                } else if (object instanceof RoomJoinedResponse) { //room successfully joined
                    synchronized(client) {
                        client.notify();
                    }
                }
            }
        });

        
        
        //listener for client connected to local server
        localClient.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof GameResponse) { //response with game data
                    GameResponse gameResponse = (GameResponse) object;
                    System.out.println(gameResponse.getMessage());

                } else if (object instanceof ControlResponse) { //control response
                    ControlResponse controlResponse = (ControlResponse) object;
                    System.out.println(controlResponse.getMessage());

                } else if (object instanceof GameRoom) { //room available on local server
                    GameRoom gameRoom = (GameRoom) object;
                    gameRoom.ipOfHost = connection.getRemoteAddressTCP().getAddress();
                    if(roomList.containsKey(gameRoom.roomID)) //if key already in list
                        gameRoom.roomID = roomList.getMaxKey() + 1; //change key so it is unique - keys are not important to local servers 
                    
                    roomList.add(gameRoom);
                    synchronized(localClient) {
                        localClient.notify();
                    }

                } else if (object instanceof RoomJoinedResponse) { //room successfully joined
                    synchronized(localClient) {
                        localClient.notify();
                    }
                } else if (object instanceof RoomClosedResponse) { //room closed
                    localClient.close();
                }
                
            }
        });


        //start connection to main server
        client.start();

        try { //to connect with main server from the outside

            //to change the address of the main server, simply change the second argument of the following
            //function into whatever domain name or IP address you desire
            client.connect(maxDelay, "multitowerdefense.hopto.org", tcpPortNumber, udpPortNumber);
            //TODO: fix incorrect timeout
        }
        catch(IOException e1) {
            String cannotConnectError = "Connection to main server could not be established";
            InetAddress mainServerAddress = client.discoverHost(udpPortNumber, maxDelay); //discover host on LAN
            if(mainServerAddress == null)
                System.out.println(cannotConnectError);
            else {
                try { //to connect with main server from the inside
                    client.connect(maxDelay, mainServerAddress, tcpPortNumber, udpPortNumber);
                }
                catch(IOException e2){ //could not connect
                    System.out.println(cannotConnectError);
                }
            }
        }
        
        //start connection to local server
        localClient.start();


        //scanner for input
        inputScanner = new Scanner (System.in);

        //get name
        System.out.println("Enter your name");
        playerName = inputScanner.nextLine();
        
        menu(); //run menu

    }


    /**
     * Menu for client which allows for:
     * * searching and joining global and local rooms(games)
     * * creating global rooms(games) hosted by the main server
     * * hosting local rooms(games)
     * * quitting
     * @throws InterruptedException
     * @throws IOException
     */
    public void menu() throws InterruptedException, IOException {

        String input;
        inputScanner.reset();
        //while(true)
        {
            System.out.println("Enter 'j' to join a room, 'c' to create a global room, 'h' to host a local room, 'q' to quit");
            input = inputScanner.nextLine();
    
            if(input.equals("j"))
                joinGame();
            else if(input.equals("c"))
                createGlobalGame();
            else if(input.equals("h"))
                hostLocalGame();
            else if(input.equals("q")) {
                quit();
                //break;
            }
        }
        
    }



    /**
     * Allow user to join global or local games
     * @throws InterruptedException
     * @throws IOException
     */
    private void joinGame() throws InterruptedException, IOException {
        
        List<InetAddress> hostList = null;
        ArrayList<Integer> arrayOfKeys;
        int roomNumber;
        
        if(client.isConnected()) { //if connection to global server is established
            synchronized(client) {
                client.sendTCP(new GetRoomListRequest());
                client.wait();
            }
        }

        //TODO: limit interfaces to wireless
        //search for hosts on LAN
        hostList = localClient.discoverHosts(udpSecondPortNumber, maxDelay);
        for(InetAddress host: hostList) {
            localClient.connect(maxDelay, host, tcpSecondPortNumber, udpSecondPortNumber);
            synchronized(localClient) {
                localClient.sendTCP(new GetRoomInfoRequest()); //get info about room from every host on LAN
                localClient.wait();
            }
            localClient.close();
        }

        roomList.print(); //print available rooms
        arrayOfKeys = roomList.getArrayOfKeys();


        System.out.println("Type number of the room you want to join, or -1 to go back");
        roomNumber = inputScanner.nextInt(); //choose which room to join
        inputScanner.nextLine();
        if(-1 != roomNumber) {
            roomID = arrayOfKeys.get(roomNumber);
            if(roomList.get(roomID).gameType == GameRoom.GLOBAL) { //if room is global
                activeClient = client;
                synchronized(client) {
                    client.sendTCP(new JoinRoomRequest(roomID));
                    client.wait();
                }
            }
            else { //if room is local
                activeClient = localClient;
                localClient.connect(maxDelay, roomList.get(roomID).ipOfHost, tcpSecondPortNumber, udpSecondPortNumber);
                localClient.sendTCP(new JoinRoomRequest());
            }
            //run(); //run the game
        }
        else //-1 chosen
            menu(); //go back to menu
        roomList.clear();

        
    }


    
    /**
     * Allow user to create global games hosted by the main server
     * @throws InterruptedException
     * @throws IOException
     */
    private void createGlobalGame() throws InterruptedException, IOException {
        int maxPlayers;

            if(client.isConnected()) { //connection to the main server must be established
                activeClient = client;
                System.out.println("Enter how many players can enter the room");
                maxPlayers = inputScanner.nextInt();
                //request to create a room
                CreateRoomRequest createRoomRequest = new CreateRoomRequest(playerName, maxPlayers, GameRoom.GLOBAL);
                synchronized(client) {
                    //send and wait for response
                    client.sendTCP(createRoomRequest);
                    client.wait();
                }
                //run();
            }
            else //no connection
                System.out.println("This option requires connection to the main server");
    }



    /**
     * Allow user to host local games - only one game can be hosted on each computer
     * @throws IOException
     * @throws InterruptedException
     */
    private void hostLocalGame() throws IOException, InterruptedException {
        int maxPlayers;
        System.out.println("Enter how many players can enter the room");
        maxPlayers = inputScanner.nextInt();
        new LocalGameServer(tcpSecondPortNumber, udpSecondPortNumber, playerName, maxPlayers, inputScanner);
    }



    /**
     * Quit from the game
     */
    private void quit() {
        client.close();
        client.stop();
        localClient.close();
        localClient.stop();
    }


    public void addObserver(GameManager observer) {
        this.observer = observer;
    }


    /**
     * After joining or creating a room, send request with client updates
     */
    public void send(GameRequest gameRequest) {

        if(activeClient.isConnected())
            activeClient.sendTCP(gameRequest);

        /*System.out.println("Press 'q' to quit");
        GameRequest gameRequest = new GameRequest(roomID); //new request with game data
        while(true) {
            gameRequest.setMessage(inputScanner.nextLine()); //get data here
            if(gameRequest.getMessage().equals("q")) { //quitting
                LeaveRoomRequest leaveRoomRequest = new LeaveRoomRequest(roomID); //request leaving
                activeClient.sendTCP(leaveRoomRequest);
                break; //connection terminated by client
            }
            if(activeClient.isConnected())
                activeClient.sendTCP(gameRequest); //send data
            else
                break; //connection terminated by host (local only)
        }*/
    }
    
}