package app;

import java.io.IOException;
import java.util.Scanner;

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
                }
            }
        });
        

        //start
        server.start();
        server.bind(tcpPortNumber);

        run();
    }

    public void run() {
        Scanner gameScanner = null;
        try {
            gameScanner = new Scanner(System.in);
            GameResponse gameResponse = new GameResponse();
            while(true) {
                gameResponse.setMessage(gameScanner.nextLine());
                server.sendToAllTCP(gameResponse);
            }
        }
        finally {
            if(gameScanner != null)
                gameScanner.close();
        }
    }

}