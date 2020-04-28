package app;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Enter 'c' to become client, 's' to become server");

        String input = "s";
        Scanner inputScanner = null;

        inputScanner = new Scanner (System.in);
        input = inputScanner.nextLine();

        if(input.equals("s"))
            new GameServer(54545);
        else
            new GameClient(54545, 5000);

        inputScanner.close();
    }

}