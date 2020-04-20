package app;

import java.io.IOException;
import java.util.Scanner;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Client;


public class GameClient {
    private Client client;

    public GameClient(int tcpPortNumber, int udpPortNumber, int maxDelay) throws IOException {
        client = new Client();

        //register classes
        Kryo kryo = client.getKryo();
        kryo.register(GameRequest.class);
        kryo.register(GameResponse.class);

        //add listener
        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if(object instanceof GameResponse) {
                    GameResponse gameResponse = (GameResponse)object;
                    System.out.println(gameResponse.getMessage());

                }
            }
        });

        //start
        client.start();
        client.connect(maxDelay, "127.0.0.1", tcpPortNumber, udpPortNumber);

        run();
    }

    public void run() {
        
        Scanner gameScanner = null;
        try {
            gameScanner = new Scanner(System.in);
            GameRequest gameRequest = new GameRequest();
            while(true) {
                gameRequest.setMessage(gameScanner.nextLine());
                client.sendTCP(gameRequest);
            }
        }
        finally {
            if(gameScanner != null)
                gameScanner.close();
        }
        
    }

    public static void main(String[] args) throws Exception {
        new GameClient(54555, 54777, 5000);
    }
}