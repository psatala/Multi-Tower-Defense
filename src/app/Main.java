/**
 * Main class for networking. Subject to change.
 */

package app;

import java.io.IOException;
import java.util.Scanner;

import com.esotericsoftware.minlog.Log;


public class Main {

    /**
     * Main function to start the program
     * @param args Usual args to be passed to main
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Enter 'c' to become client, 's' to become server");

        Log.set(Log.LEVEL_INFO);
        String input = "s";
        Scanner inputScanner = null;

        inputScanner = new Scanner (System.in);
        input = inputScanner.nextLine();

        if(input.equals("s"))
            new GameServer(54545, 54545); //start main server
        else
            new GameClient(54545, 54545, 54546, 54546, 500); //start client

        inputScanner.close();
    }

}