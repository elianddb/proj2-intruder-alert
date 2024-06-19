package com.cs440;

import java.io.IOException;

import com.cs440.ship.Ship;

public class App {
    public static void main(String[] args) throws IOException {
        Ship ship = new Ship(40, 40);
        System.out.println(ship);

        System.in.read();
    }
}
