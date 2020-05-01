/**
 * The LocalGameServer class allows user to host a server accessible on his LAN. The local server uses a port
 * different to the one used by the main server.
 * @author Piotr Satała
 */

package app;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import app.requests.*;
import app.responses.*;

public class LocalGameServer {
    private Server localServer;
    private GameRoom gameRoom;
    private Scanner inputScanner;
    
    /**
     * Public constructor for LocalGameServer class
     * @param tcpSecondPortNumber tcp port for local server
     * @param udpSecondPortNumber udp port for local server
     * @param hostName name of user who is hosting the local server
     * @param maxPlayers max number of players on server
     * @param inputScanner input scanner for host
     * @throws IOException
     * @throws InterruptedException
     */
    public LocalGameServer(int tcpSecondPortNumber, int udpSecondPortNumber, String hostName,
            int maxPlayers, Scanner inputScanner) throws IOException, InterruptedException {

        localServer = new Server();
        gameRoom = new GameRoom(hostName, maxPlayers, GameRoom.LOCAL, -1); //add host to game room

        this.inputScanner = inputScanner;

        //register classes
        Network.register(localServer);

        //add listener
        localServer.addListener(new Listener() {
            public void received(Connection connection, Object object) { //client sends a game message
                connection.setTimeout(0); //never timeout - TODO: find proper solution for connection timeout
                if(object instanceof GameRequest) { //request with game data
                    GameRequest gameRequest = (GameRequest)object;

                    System.out.println(gameRequest.getMessage());

                    GameResponse gameResponse = new GameResponse(gameRequest.getMessage());
                    if(gameRoom != null)
                        for(Integer connectionID: gameRoom.connectionSet) //for every connection to the server
                            if(connectionID != connection.getID() && connectionID != -1) //that is not host or sender
                                localServer.sendToTCP(connectionID, gameResponse);
                    
                }
                else if(object instanceof JoinRoomRequest) { //client wants to join a room

                    try {
                        gameRoom.addPlayer(connection.getID());
                        RoomJoinedResponse roomJoinedResponse = new RoomJoinedResponse();
                        localServer.sendToTCP(connection.getID(), roomJoinedResponse);
                    }
                    catch(Exception e) { //room full
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        localServer.sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof LeaveRoomRequest) { //client wants to leave a room
                    
                    try {
                        connection.close();
                        gameRoom.removePlayer(connection.getID());
                    }
                    catch(Exception e) { //room empty
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        localServer.sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof GetRoomInfoRequest) { //client wants to get info about the room
                    localServer.sendToTCP(connection.getID(), gameRoom);
                }
            }
        });
        

        //start
        localServer.start();
        try {
            localServer.bind(tcpSecondPortNumber, udpSecondPortNumber);
            run();
        }
        catch(BindException e) { //another server already running on this computer
            System.out.println("Another server is already running on this computer");
            //TODO: fix incorrect quitting after this exception
        }

        
    }



    /**
     * After creating the room, run the game in it
     * @throws InterruptedException
     * @throws IOException
     */
    public void run() throws InterruptedException, IOException {
        
        System.out.println("Press 'q' to quit");

        GameResponse gameResponse = new GameResponse(); //new response to other clients
        while(true) {
            gameResponse.setMessage(inputScanner.nextLine());
            if(gameResponse.getMessage().equals("q")) { //quitting
                for(Integer connectionID: gameRoom.connectionSet) //to all connected inform about room closing
                    if(connectionID != -1)
                        localServer.sendToTCP(connectionID, new RoomClosedResponse());
                localServer.close(); //close the room
                localServer.stop();
                break;
            }
            else {
                for(Integer connectionID: gameRoom.connectionSet) //send data to everyone connected apart from host
                    if(connectionID != -1)
                        localServer.sendToTCP(connectionID, gameResponse);
            }
        }
    }
    
}