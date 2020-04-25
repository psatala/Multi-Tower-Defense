package app;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Client;


public class GameClient {
    private Client client;
    private Scanner inputScanner = null;
    public String playerName;
    private int roomID;

    public GameClient(int tcpPortNumber, int maxDelay) throws IOException {
        client = new Client();

        //register classes
        Kryo kryo = client.getKryo();
        //requests
        kryo.register(GameRequest.class);
        kryo.register(CreateRoomRequest.class);
        kryo.register(JoinRoomRequest.class);
        kryo.register(LeaveRoomRequest.class);
        kryo.register(GetRoomListRequest.class);
        //responses
        kryo.register(GameResponse.class);
        kryo.register(ControlResponse.class);
        kryo.register(RoomList.class);
        kryo.register(HashMap.class);
        kryo.register(GameRoom.class);
        kryo.register(HashSet.class);
                

        //add listener
        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if(object instanceof GameResponse) { //standard
                    GameResponse gameResponse = (GameResponse)object;
                    System.out.println(gameResponse.getMessage());

                }
                else if(object instanceof ControlResponse) {
                    ControlResponse controlResponse = (ControlResponse)object;
                    System.out.println(controlResponse.getMessage());
                }
                else if(object instanceof RoomList) {
                    RoomList roomList = (RoomList)object;
                    roomList.print();

                    System.out.println("Type id of the room you want to join, or -1 to go back");
                    roomID = 0;
                    //if(inputScanner.hasNext())
                     //   roomID = inputScanner.nextInt();
                    if(roomID != -1)
                        client.sendTCP(new JoinRoomRequest(roomID));
                }
            }
        });

        //start
        client.start();
        client.connect(maxDelay, "multitowerdefense.hopto.org", tcpPortNumber);

        inputScanner = new Scanner (System.in);

        System.out.println("Enter your name");
        playerName = inputScanner.nextLine();

        menu();

    }



    public void menu() {

        System.out.println("Enter 'j' to join a room, 'c' to create room");
        String input;
        input = inputScanner.nextLine();
    
        if(input.equals("j"))
            client.sendTCP(new GetRoomListRequest());
        else
        {
            int maxPlayers;
            System.out.println("Enter how many players can enter the room");
            maxPlayers = inputScanner.nextInt();
            CreateRoomRequest createRoomRequest = new CreateRoomRequest(playerName, maxPlayers, GameRoom.GLOBAL);
            client.sendTCP(createRoomRequest);
        }
        run();
    }


    public void run() {
        
        inputScanner = new Scanner(System.in);
        GameRequest gameRequest = new GameRequest(roomID);
        while(true) {
            gameRequest.setMessage(inputScanner.nextLine());
            client.sendTCP(gameRequest);
        }        
    }
    
}