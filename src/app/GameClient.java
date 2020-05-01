package app;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Client;

import app.requests.*;
import app.responses.*;

public class GameClient {

    private Client client;
    private Client localClient;
    private Client activeClient;
    private Scanner inputScanner = null;
    public String playerName;
    private int roomID;
    private RoomList roomList = null;

    public GameClient(int tcpPortNumber, int udpPortNumber, int tcpSecondPortNumber, int udpSecondPortNumber, int maxDelay) throws IOException, InterruptedException {
        client = new Client();
        localClient = new Client();
        roomList = new RoomList();

        // register classes
        Network.register(client);
        Network.register(localClient);

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
                    gameRoom.ipOfHost = connection.getRemoteAddressTCP().getAddress();
                    if(roomList.containsKey(gameRoom.roomID)) //if key already in list
                        gameRoom.roomID = roomList.getMaxKey() + 1; //change key so it is unique - keys are not important to local servers 
                    
                    roomList.add(gameRoom);
                    synchronized(localClient) {
                        localClient.notify();
                    }

                } else if (object instanceof RoomJoinedResponse) {
                    synchronized(localClient) {
                        localClient.notify();
                    }
                } else if (object instanceof RoomClosedResponse) {
                    localClient.close();
                }
                
            }
        });


        //start connection to main server
        client.start();

        try {
            //TODO: fix incorrect timeout
            client.connect(maxDelay, "multitowerdefense.hopto.org", tcpPortNumber, udpPortNumber); //try to connect from the outside
        }
        catch(IOException e1) {
            String cannotConnectError = "Connection to main server could not be established";
            InetAddress mainServerAddress = client.discoverHost(tcpPortNumber, maxDelay); //discover host on LAN
            if(mainServerAddress == null)
                System.out.println(cannotConnectError);
            else {
                try {
                    client.connect(maxDelay, mainServerAddress, tcpPortNumber, udpPortNumber); //try to connect from the inside
                }
                catch(IOException e2){
                    System.out.println(cannotConnectError);
                }
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

        String input;
        List<InetAddress> hostList = null;
        ArrayList<Integer> arrayOfKeys;
        int roomNumber;
        inputScanner.reset();
        while(true) {
            System.out.println("Enter 'j' to join a room, 'c' to create a global room, 'h' to host a local room, 'q' to quit");
            input = inputScanner.nextLine();
    
            if(input.equals("j")) {
                if(client.isConnected()) {
                    synchronized(client) {
                        client.sendTCP(new GetRoomListRequest());
                        client.wait();
                    }
                }

                if(hostList != null)
                    hostList.clear();
                hostList = localClient.discoverHosts(udpSecondPortNumber, maxDelay);
                for(InetAddress host: hostList) {
                    localClient.connect(maxDelay, host, tcpSecondPortNumber, udpSecondPortNumber);
                    synchronized(localClient) {
                        localClient.sendTCP(new GetRoomInfoRequest());
                        localClient.wait();
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
                        activeClient = client;
                        synchronized(client) {
                            client.sendTCP(new JoinRoomRequest(roomID));
                            client.wait();
                        }
                    }
                    else {
                        activeClient = localClient;
                        localClient.connect(maxDelay, roomList.get(roomID).ipOfHost, tcpSecondPortNumber, udpSecondPortNumber);
                        localClient.sendTCP(new JoinRoomRequest());
                    }
                    run(tcpSecondPortNumber, udpSecondPortNumber, maxDelay);
                }
                else
                    menu(tcpSecondPortNumber, udpSecondPortNumber, maxDelay);
                roomList.clear();

                    
            }
            else if(input.equals("c")) {
                int maxPlayers;

                if(client.isConnected()) {
                    activeClient = client;
                    System.out.println("Enter how many players can enter the room");
                    maxPlayers = inputScanner.nextInt();
                    CreateRoomRequest createRoomRequest = new CreateRoomRequest(playerName, maxPlayers, GameRoom.GLOBAL);
                    synchronized(client) {
                        client.sendTCP(createRoomRequest);
                        client.wait();
                    }
                    run(tcpSecondPortNumber, udpSecondPortNumber, maxDelay);
                }
                else
                    System.out.println("This option requires connection to the main server");
            }
            else if(input.equals("h")) {
                int maxPlayers;
                System.out.println("Enter how many players can enter the room");
                maxPlayers = inputScanner.nextInt();
                new LocalGameServer(tcpSecondPortNumber, udpSecondPortNumber, maxDelay, playerName, maxPlayers, inputScanner);
            }
            else if(input.equals("q")) {
                client.close();
                client.stop();
                localClient.close();
                localClient.stop();
                break;
            }
        }
        
    }


    public void run(int tcpSecondPortNumber, int udpSecondPortNumber, int maxDelay) throws InterruptedException, IOException {
        
        System.out.println("Press 'q' to quit");
        GameRequest gameRequest = new GameRequest(roomID); //new general request
        while(true) {
            gameRequest.setMessage(inputScanner.nextLine());
            if(gameRequest.getMessage().equals("q")) { //quitting
                LeaveRoomRequest leaveRoomRequest = new LeaveRoomRequest(roomID); //request leaving
                activeClient.sendTCP(leaveRoomRequest);
                break; //connection terminated by client
            }
            if(activeClient.isConnected())
                activeClient.sendTCP(gameRequest); //send data
            else
                break; //connection terminated by host (local only)
        }
    }
    
}