package org.cs440;

import java.io.IOException;

import org.cs440.agent.Bot;
import org.cs440.agent.StationaryMouse;
import org.cs440.ship.Ship;

public class App {
    public static final Log logger = new Log("App");

    public static void main(String[] args) throws IOException {
        logger.setLevel(Log.Level.DEBUG);
        
        Ship ship = new Ship(40, 40);
        Simulation simulation = new Simulation(ship);

        Bot bot = new Bot('A');
        StationaryMouse mouse = new StationaryMouse('M');

        simulation.addAgent(bot);
        simulation.addAgent(mouse);
        simulation.run(100);

        System.out.println("Press Enter to exit...");
        System.in.read();
    }
}
