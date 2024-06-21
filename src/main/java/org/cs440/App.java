package org.cs440;

import java.io.IOException;

import org.cs440.entity.Bot;
import org.cs440.ship.Ship;

public class App {
    public static void main(String[] args) throws IOException {
        Ship ship = new Ship(40, 40);
        Simulation simulation = new Simulation(ship);
        Bot bot = new Bot('B');
        simulation.addEntity(bot);
        System.out.println(simulation);

        System.out.println("Press Enter to exit...");
        System.in.read();
    }
}
