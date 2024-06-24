package org.cs440;

import java.io.IOException;

import org.cs440.Log.Level;
import org.cs440.agent.Bot;
import org.cs440.agent.StochasticMouse;
import org.cs440.ship.Ship;

public class App {
    public static final Log logger = new Log("App");

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.setLevel(Level.DEBUG);
        
        Ship ship = new Ship(40, 40);
        StochasticMouse mouse = new StochasticMouse('M');
        Bot bot = new Bot('A', mouse);
        
        Simulation simulation = new Simulation(ship);
        simulation.addAgent(bot);
        simulation.addAgent(mouse);
        
        if (logger.is(Level.DEBUG)) { // Delay to read initial state logs
            logger.debug("Debugging is enabled...");
            Thread.sleep(3000);
        }

        simulation.run(100); // Exclude delay to run without drawing frames

        System.out.println("Press Enter to exit...");
        System.in.read();
    }
}
