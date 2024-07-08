package org.cs440.agent.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import org.cs440.App;
import org.cs440.agent.Agent.Movement.Direction;
import org.cs440.agent.Bot;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;
import org.cs440.ship.Tile.Status;

public class Bot1RV implements Algorithm{
    private static final double EPSILON = 1e-10; // Small constant for smoothing

    private LinkedList<Direction> moveQueue;
    private double probabilityMap[][];

    public Bot1RV(Ship ship) {
        this.moveQueue = new LinkedList<Direction>();

        int height = ship.getHeight();
        int width = ship.getWidth();
        // Since the is empty with a bot in it, the probability of a mouse being in any open tile is uniform
        double uniformProbability = 1.0 / (ship.numOfOpen() + 1);
        probabilityMap = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }
                
                if (!ship.getTile(j, i).is(Status.BLOCKED)) {
                    probabilityMap[i][j] = uniformProbability;
                }
            }
        }
    }

    @Override
    public void execute(Bot bot) {
        if (!moveQueue.isEmpty()) {
            Direction direction = moveQueue.peek();
            bot.move(moveQueue.poll());
            int x = bot.getLocation().x() + direction.dx;
            int y = bot.getLocation().y() + direction.dy;
            App.logger.debug("Attempting to move to: (" + x + ", " + y + ")");
            bot.attemptCapture(x, y);
            bot.updateMoveCount();
            return;
        }

        moveQueue.clear();
        boolean sensorBeeped = bot.getSensor().beeped();
        // Update probability map
        double totalProbability = 0.0;
        int height = bot.getShip().getHeight();
        int width = bot.getShip().getWidth();
        double[][] newProbabilityMap = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (bot.getShip().getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                if (bot.getLocation().equals(j, i)) {
                    newProbabilityMap[i][j] = 0.0;
                    continue;
                }

                int manhattanDistance = bot.getLocation().manhattanDistance(j, i);
                double beepProbability = Math.exp(-bot.getSensor().getSensitivity() * (manhattanDistance - 1));
                double likelihood = sensorBeeped ? beepProbability : 1 - beepProbability;

                newProbabilityMap[i][j] = probabilityMap[i][j] * likelihood;
                totalProbability += newProbabilityMap[i][j];
            }
        }

        // Normalize probability map
        totalProbability += EPSILON; // Adding smoothing constant to total probability; ensures no division by zero
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (bot.getShip().getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }
                
                newProbabilityMap[i][j] = (newProbabilityMap[i][j] + EPSILON) / totalProbability; // Adding epsilon before normalization
            }
        }

        probabilityMap = newProbabilityMap;
        bot.updateSenseCount();

        planPath(bot);
        
        App.logger.debug("\n" + toString());
    }

    public void planPath(Bot bot) {
        double maxProbability = 0.0;
        Location target = bot.getLocation();
        for (int i = 0; i < bot.getShip().getHeight(); i++) {
            for (int j = 0; j < bot.getShip().getWidth(); j++) {
                if (bot.getShip().getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }
                
                if (probabilityMap[i][j] > maxProbability) {
                    maxProbability = probabilityMap[i][j];
                    target = new Location(j, i);
                }
            }
        }

        Queue<Location> fringe = new LinkedList<>();
        HashSet<Location> visited = new HashSet<>();
        HashMap<Location, Location> parent = new HashMap<>();
        fringe.add(bot.getLocation());
        while (!fringe.isEmpty()) {
            Location current = fringe.poll();
            visited.add(current);
            // App.logger.debug("Current: " + current.toString() + " Target: " + target.toString());

            if (current.equals(target)) {
                // Backtrack path
                while (parent.containsKey(current)) {
                    Location next = parent.get(current);
                    // Evaluate direction manually
                    if (next.x() - current.x() < 0) {
                        moveQueue.addFirst(Direction.RIGHT);
                    } else if (next.x() - current.x() > 0) {
                        moveQueue.addFirst(Direction.LEFT);
                    } else if (next.y() - current.y() < 0) {
                        moveQueue.addFirst(Direction.DOWN);
                    } else if (next.y() - current.y() > 0) {
                        moveQueue.addFirst(Direction.UP);
                    }
                    current = next;
                }
                break;
            }

            for (Location neighbor : current.cardinalNeighbors()) {
                if (!bot.getShip().withinBounds(neighbor.x(), neighbor.y())) {
                    continue;
                }

                if (bot.getShip().getTile(neighbor).is(Status.BLOCKED)) {
                    continue;
                }

                if (visited.contains(neighbor)) {
                    continue;
                }

                fringe.add(neighbor);
                parent.put(neighbor, current);
            }
        }
    }

    @Override
    public String toString() {
        // probability map
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < probabilityMap.length; i++) {
            for (int j = 0; j < probabilityMap[i].length; j++) {
                double probability = probabilityMap[i][j];
                boolean isProbability = probability < 0.00001;
                sb.append(isProbability ? "" : ANSI_RED);
                sb.append(String.format("%.5f ", probabilityMap[i][j]));
                sb.append(isProbability ? "" : ANSI_RESET);
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
