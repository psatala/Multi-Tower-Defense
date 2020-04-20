package app;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;


public class GameServer {
    private Server server;
    
    public GameServer(int tcpPortNumber, int udpPortNumber) throws IOException {
        server = new Server();
        
        //register classes
        Kryo kryo = server.getKryo();
        kryo.register(GameRequest.class);
        kryo.register(GameResponse.class);

        //add listener
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if(object instanceof GameRequest) {
                    GameRequest gameRequest = (GameRequest)object;
                    System.out.println(gameRequest.getMessage());

                    GameResponse gameResponse = new GameResponse("Thanks!");
                    connection.sendTCP(gameResponse);
                }
            }
        });
        

        //start
        server.start();
        server.bind(tcpPortNumber, udpPortNumber);
    }
    public static void main(String[] args) throws Exception {
        new GameServer(54555, 54777);       
    }
}