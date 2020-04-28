package app;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Client;

import app.requests.*;
import app.responses.*;

public class GameClient {
    private Client client;
    private Scanner inputScanner = null;
    public String playerName;
    private int roomID;
    private boolean isGoBackChosen = false;

    public GameClient(int tcpPortNumber, int maxDelay) throws IOException, InterruptedException {
        client = new Client();

        // register classes
        Kryo kryo = client.getKryo();
        // requests
        kryo.register(GameRequest.class);
        kryo.register(CreateRoomRequest.class);
        kryo.register(JoinRoomRequest.class);
        kryo.register(LeaveRoomRequest.class);
        kryo.register(GetRoomListRequest.class);
        // responses
        kryo.register(GameResponse.class);
        kryo.register(ControlResponse.class);
        kryo.register(RoomList.class);
        kryo.register(HashMap.class);
        kryo.register(GameRoom.class);
        kryo.register(HashSet.class);

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
                    RoomList roomList = (RoomList) object;
                    roomList.print();

                    System.out.println("Type id of the room you want to join, or -1 to go back");
                    roomID = inputScanner.nextInt();
                    inputScanner.nextLine();

                    if (roomID != -1){
                        synchronized (client) {
                            isGoBackChosen = false;
                            client.notify();
                        }
                        client.sendTCP(new JoinRoomRequest(roomID));
                    }
                    else {
                        synchronized (client) {
                            isGoBackChosen = true;
                            client.notify();
                        }
                    }
                }
            }
        });

        //start
        client.start();
        client.connect(maxDelay, "127.0.0.1", tcpPortNumber);

        inputScanner = new Scanner (System.in);

        System.out.println("Enter your name");
        playerName = inputScanner.nextLine();

        menu();

    }



    public void menu() throws InterruptedException {

        System.out.println("Enter 'j' to join a room, 'c' to create room");
        String input;
        inputScanner.reset();
        input = inputScanner.nextLine();
        isGoBackChosen = false;
    
        if(input.equals("j")) {
            client.sendTCP(new GetRoomListRequest());
            synchronized(client) {
                client.wait();
            }
            if(isGoBackChosen)
                menu();
            else
                run();
        }
        else {
            int maxPlayers;
            System.out.println("Enter how many players can enter the room");
            maxPlayers = inputScanner.nextInt();
            CreateRoomRequest createRoomRequest = new CreateRoomRequest(playerName, maxPlayers, GameRoom.GLOBAL);
            client.sendTCP(createRoomRequest);
            run();
        }
    }


    public void run() {
        
        GameRequest gameRequest = new GameRequest(roomID);
        while(true) {
            gameRequest.setMessage(inputScanner.nextLine());
            client.sendTCP(gameRequest);
        }        
    }
    
}