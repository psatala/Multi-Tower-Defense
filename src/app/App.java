package app;

import com.esotericsoftware.kryonet.Server;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello Java");
        Server server = new Server();
        server.start();
        server.bind(54555, 54777);
    }
}