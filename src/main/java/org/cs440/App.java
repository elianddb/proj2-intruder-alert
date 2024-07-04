package org.cs440;

import java.io.IOException;

import org.cs440.Log.Level;
import org.cs440.agent.Agent;
import org.cs440.agent.Bot;
import org.cs440.agent.StationaryMouse;
import org.cs440.agent.StochasticMouse;
import org.cs440.agent.algorithm.*;
import org.cs440.ship.Ship;

public class App {
    public static final Log logger = new Log("App");

    public static void main(String[] args) throws IOException {
        logger.setLevel(Level.INFO);
        
        Ship ship = new Ship(40, 40);
        // StochasticMouse mouse1 = new StochasticMouse('M');
        // StochasticMouse mouse2 = new StochasticMouse('M');
        StationaryMouse mouse1 = new StationaryMouse('M');
        Bot bot = new Bot('A', new Agent[] {mouse1}, 0.1, new Bot3(ship));
        
        Simulation simulation = new Simulation(ship);
        simulation.addAgent(bot);
        simulation.addAgent(mouse1);
        // simulation.addAgent(mouse2);
        
        if (logger.is(Level.DEBUG)) { // Delay for human to read initial state logs
            logger.debug("Debugging is enabled...");
            logger.debug("Press Enter to start simulation...");
            System.in.read();
        }

        simulation.run(6); // Exclude delay to run without drawing frames
        //logger.debug("\n" + simulation.toString());

        logger.info("Simulation completed in " + simulation.stepsTaken() + " steps");
        System.out.println("Press Enter to exit...");
        System.in.read();

        logger.writeTo("App");
        logger.info("Log messages written to file: App.log");
    }
}
