package app;

import java.io.IOException;
import java.util.Scanner;

import com.esotericsoftware.minlog.Log;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Enter 'c' to become client, 's' to become server");

        Log.set(Log.LEVEL_INFO);
        String input = "s";
        Scanner inputScanner = null;

        inputScanner = new Scanner (System.in);
        input = inputScanner.nextLine();

        if(input.equals("s"))
            new GameServer(54545, 54545);
        else
            new GameClient(54545, 54545, 54546, 54546, 5000);

        inputScanner.close();
    }

}