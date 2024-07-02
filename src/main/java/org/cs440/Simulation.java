package org.cs440;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cs440.agent.Agent;
import org.cs440.agent.Agent.Action;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;

public class Simulation {
    private Ship ship;
    private ArrayList<Agent> agents;
    private HashMap<Agent, Action> actions;
    private boolean running = false;
    private int frameCount = 0;
    private int noOfSteps = 0;
    private final int frameBufferSize;

    public Simulation (Ship ship, int frameBufferSize) {
        this.ship = ship;
        this.agents = new ArrayList<>();
        this.actions = new HashMap<>();
        this.frameBufferSize = frameBufferSize;
    }

    public Simulation(Ship ship) {
        this(ship, 200);
    }

    public void addAgent(Agent agent) {
        agent.link(ship);
        agents.add(agent);
        if (agent instanceof Action) {
            actions.put(agent, (Action) agent);
        }
    }

    public Agent getAgent(int x, int y) {
        for (Agent agent : agents) {
            if (agent.getLocation().x() == x && agent.getLocation().y() == y) {
                return agent;
            }
        }

        return null;
    }

    public int stepsTaken() {
        return noOfSteps;
    }

    public void stop() {
        running = false;
    }

    public void run(int ms) {
        running = true;
        System.out.print("\033[2J"); // Clear command line prompt
        System.out.flush();
        // Drawing frames on a separate thread helps avoid hitching
        // from calculating agents' actions
        Queue<String> frameBuffer = new LinkedList<>();
        ScheduledExecutorService drawScheduler = Executors.newSingleThreadScheduledExecutor();
        drawScheduler.scheduleWithFixedDelay(() -> {
            if (!frameBuffer.isEmpty()) {
                System.out.print("\033[H"); // Move cursor to top left
                String frame;
                synchronized (frameBuffer) {
                    frame = frameBuffer.poll();
                }
                System.out.printf("%s\n", frame);
                System.out.flush();
                
                App.logger.debug(String.format("frameBufferSize = %d", frameBuffer.size()));
                App.logger.info(String.format("Frame %d", ++frameCount));
            }
        }, 100, ms, TimeUnit.MILLISECONDS); // Initial delay to allow frame buffer to fill
        
        while (running) {
            // We only want to lock the frame buffer when necessary.
            // Including agents' actions in this synchronized block
            // could cause a deadlock (Thread waiting for another
            // thread to release a piece of memory)
            synchronized (frameBuffer) {
                if (frameBuffer.size() < frameBufferSize) {
                    frameBuffer.add(toString()); // Queue new frame state
                } else {
                    continue;
                }
            }

            int closedCount = 0;
            for (Action action : actions.values()) {
                // Perform an agent's action first to account for coexistence
                action.perform();

                if (action.closed()) {
                    ++closedCount;
                }
            }
            
            App.logger.debug("closedCount = " + closedCount);
            if (closedCount == actions.size()) {
                stop();
            }
            ++noOfSteps;
        }
        
        // Add last frame with updated states
        frameBuffer.add(toString());
        // Delay shutdown to allow final frame to be drawn
        try {
            while (!frameBuffer.isEmpty()) {
                Thread.sleep(100); // 100ms avoids constant CPU polling
            }
        } catch (InterruptedException e) {
            App.logger.error("Failed to delay shutdown: " + e.getMessage());
        }
        drawScheduler.shutdown();
        
        --noOfSteps; // Exclude final step

        App.logger.debug("Draw scheduler shutdown");
    }

    public void run() { // Runs the run(ms) above but without draw/delay
        running = true;

        while (running) {
            int closedCount = 0;
            for (Action action : actions.values()) {
                action.perform();

                if (action.closed()) {
                    ++closedCount;
                }
            }
            
            if (closedCount == actions.size()) {
                stop();
            }
            ++noOfSteps;
        }

        --noOfSteps; // Exclude final step
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[K"); // Sometimes init line doesn't clear correctly
        for (int y = 0; y < ship.getHeight(); y++) {
            for (int x = 0; x < ship.getWidth(); x++) {
                Tile tile = ship.getTile(x, y);
                if (tile.is(Ship.OCCUPIED)) {
                    Agent entity = getAgent(x, y);
                    sb.append(entity.getIdentifier());
                } else {
                    sb.append(tile.identifier());
                }

                sb.append(x < ship.getWidth() - 1 ? " " : "");
            }
            sb.append(y < ship.getHeight() - 1 ? "\n" : "");
            sb.append("\033[K"); // Clear end of line
        }

        return sb.toString();
    }
}
