package org.cs440;

import java.io.IOException;

import org.cs440.ship.Ship;

public class App {
    public static void main(String[] args) throws IOException {
        Ship ship = new Ship(40, 10);
        System.out.println(ship);
        
        System.out.println("Press Enter to exit...");
        System.in.read();
    }
}
