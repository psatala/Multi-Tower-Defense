package app;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Client;

import app.requests.*;
import app.responses.*;

public class GameClient {

    private Client client;
    private Client localClient;
    private Scanner inputScanner = null;
    public String playerName;
    private int roomID;
    private RoomList roomList = null;

    public GameClient(int tcpPortNumber, int udpPortNumber, int tcpSecondPortNumber, int udpSecondPortNumber, int maxDelay) throws IOException, InterruptedException {
        client = new Client();
        localClient = new Client();
        roomList = new RoomList();

        // register classes
        Kryo kryo = client.getKryo();
        // requests
        kryo.register(GameRequest.class);
        kryo.register(CreateRoomRequest.class);
        kryo.register(JoinRoomRequest.class);
        kryo.register(LeaveRoomRequest.class);
        kryo.register(GetRoomListRequest.class);
        kryo.register(GetRoomInfoRequest.class);
        // responses
        kryo.register(GameResponse.class);
        kryo.register(ControlResponse.class);
        kryo.register(RoomList.class);
        kryo.register(HashMap.class);
        kryo.register(GameRoom.class);
        kryo.register(HashSet.class);
        kryo.register(RoomCreatedResponse.class);
        kryo.register(RoomJoinedResponse.class);

        // add listener
        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof GameResponse) { // standard
                    GameResponse gameResponse = (GameResponse) object;
                    System.out.println(gameResponse.getMessage());

                } else if (object instanceof ControlResponse) {
                    ControlResponse controlResponse = (ControlResponse) object;
                    System.out.println(controlResponse.getMessage());

                } else if (object instanceof RoomList) {
                    RoomList mainServerRoomList = (RoomList) object;
                    roomList.putALL(mainServerRoomList);
                    synchronized(client) {
                        client.notify();
                    }

                } else if (object instanceof RoomCreatedResponse) {
                    RoomCreatedResponse roomCreatedResponse = (RoomCreatedResponse) object;
                    roomID = roomCreatedResponse.roomID;
                    synchronized(client) {
                        client.notify();
                    }
                } else if (object instanceof RoomJoinedResponse) {
                    synchronized(client) {
                        client.notify();
                    }
                }
            }
        });

        localClient.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof GameResponse) { // standard
                    GameResponse gameResponse = (GameResponse) object;
                    System.out.println(gameResponse.getMessage());

                } else if (object instanceof ControlResponse) {
                    ControlResponse controlResponse = (ControlResponse) object;
                    System.out.println(controlResponse.getMessage());

                } else if (object instanceof GameRoom) {
                    GameRoom gameRoom = (GameRoom) object;
                    
                    if(roomList.containsKey(gameRoom.roomID)) //if key already in list
                        gameRoom.roomID = roomList.getMaxKey() + 1; //change key so it is unique - keys are not important to local servers 
                    
                    roomList.add(gameRoom);
                    synchronized(localClient) {
                        client.notify();
                    }

                } else if (object instanceof RoomJoinedResponse) {
                    synchronized(localClient) {
                        localClient.notify();
                    }
                }
                
            }
        });


        //start connection to main server
        client.start();

        try {
            client.connect(maxDelay, "multitowerdefense.hopto.org", tcpPortNumber, udpPortNumber); //try to connect from the outside
        }
        catch(IOException e1) {
            String cannotConnectError = "Connection to main server could not be established";
            InetAddress mainServerAddress = client.discoverHost(tcpPortNumber, maxDelay); //discover host on LAN
            if(mainServerAddress == null)
                System.out.println(cannotConnectError);
            try {
                client.connect(maxDelay, mainServerAddress, tcpPortNumber, udpPortNumber); //try to connect from the inside
            }
            catch(IOException e2){
                System.out.println(cannotConnectError);
            }
        }
        
        //start connection to local server
        localClient.start();



        inputScanner = new Scanner (System.in);

        System.out.println("Enter your name");
        playerName = inputScanner.nextLine();
        
        menu(tcpSecondPortNumber, udpSecondPortNumber, maxDelay);

    }



    public void menu(int tcpSecondPortNumber, int udpSecondPortNumber, int maxDelay) throws InterruptedException, IOException {

        System.out.println("Enter 'j' to join a room, 'c' to create a global room, 'h' to host a local room, 'q' to quit");
        String input;
        ArrayList<Integer> arrayOfKeys;
        int roomNumber;
        inputScanner.reset();
        input = inputScanner.nextLine();
    
        if(input.equals("j")) {
            synchronized(client) {
                client.sendTCP(new GetRoomListRequest());
                client.wait();
            }

            List<InetAddress> hostList = localClient.discoverHosts(udpSecondPortNumber, maxDelay);
            for(InetAddress host: hostList) {
                localClient.connect(maxDelay, host, tcpSecondPortNumber, udpSecondPortNumber);
                synchronized(localClient) {
                    client.sendTCP(new GetRoomInfoRequest());
                    client.wait();
                }
                localClient.close();
            }

            roomList.print();
            arrayOfKeys = roomList.getArrayOfKeys();


            System.out.println("Type number of the room you want to join, or -1 to go back");
            roomNumber = inputScanner.nextInt();
            inputScanner.nextLine();
            if(-1 != roomNumber) {
                roomID = arrayOfKeys.get(roomNumber);
                if(roomList.get(roomID).gameType == GameRoom.GLOBAL) {
                    synchronized(client) {
                        client.sendTCP(new JoinRoomRequest(roomID));
                        client.wait();
                    }
                }
                else {
                    localClient.connect(maxDelay, roomList.get(roomID).ipOfHost, tcpSecondPortNumber, udpSecondPortNumber);
                    localClient.sendTCP(new JoinRoomRequest());
                }
                run(tcpSecondPortNumber, udpSecondPortNumber, maxDelay);
            }
            else
                menu(tcpSecondPortNumber, udpSecondPortNumber, maxDelay);

                
        }
        else if(input.equals("c")) {
            int maxPlayers;
            System.out.println("Enter how many players can enter the room");
            maxPlayers = inputScanner.nextInt();
            CreateRoomRequest createRoomRequest = new CreateRoomRequest(playerName, maxPlayers, GameRoom.GLOBAL);
            synchronized(client) {
                client.sendTCP(createRoomRequest);
                client.wait();
            }
            run(tcpSecondPortNumber, udpSecondPortNumber, maxDelay);
        }
        else if(input.equals("h")) {
            int maxPlayers;
            System.out.println("Enter how many players can enter the room");
            maxPlayers = inputScanner.nextInt();
            new LocalGameServer(tcpSecondPortNumber, udpSecondPortNumber, playerName, maxPlayers);
        }
        else {
            client.close();
        }
    }


    public void run(int tcpSecondPortNumber, int udpSecondPortNumber, int maxDelay) throws InterruptedException, IOException {
        
        System.out.println("Press 'q' to quit");
        GameRequest gameRequest = new GameRequest(roomID); //new general request
        while(true) {
            gameRequest.setMessage(inputScanner.nextLine());
            if(gameRequest.getMessage().equals("q")) { //quitting
                LeaveRoomRequest leaveRoomRequest = new LeaveRoomRequest(roomID); //request leaving
                client.sendTCP(leaveRoomRequest);
                break;
            }
            client.sendTCP(gameRequest); //send data
        }
        menu(tcpSecondPortNumber, udpSecondPortNumber, maxDelay); //go back to menu
    }
    
}