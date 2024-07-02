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

public class Bot1RV implements Algorithm {
    private LinkedList<Direction> moveQueue;
    private double probabilityMap[][];

    private int sensorCount = 0;
    private int moveCount = 0;

    public Bot1RV(Ship ship) {
        this.moveQueue = new LinkedList<Direction>();

        int height = ship.getHeight();
        int width = ship.getWidth();
        probabilityMap = new double[height][width];
        for (int i = 0; i < height; i++) { // Base probability
            for (int j = 0; j < width; j++) {
                if (!ship.getTile(i, j).is(Status.BLOCKED)) {
                    probabilityMap[i][j] = 1.0 / (ship.numOfOpen() + 1);
                }
            }
        }
    }

    @Override
    public void execute(Bot bot) {
        App.logger.debug("Bot1RV moveQueue is empty: " + moveQueue.isEmpty());
        if (!moveQueue.isEmpty()) {
            Direction direction = moveQueue.peek();
            bot.move(moveQueue.poll());
            int x = bot.getLocation().x() + direction.dx;
            int y = bot.getLocation().y() + direction.dy;
            bot.getTarget().capture(x, y);
            ++moveCount;
            return;
        }

        ++sensorCount;
        boolean sensorBeeped = bot.getSensor().beeped();
        double normalizationFactor = 0.0;
        // Update probability map
        int height = bot.getShip().getHeight();
        int width = bot.getShip().getWidth();
        double[][] newProbabilityMap = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (bot.getShip().getTile(i, j).is(Status.BLOCKED)) {
                    continue;
                }

                int manhattanDistance = bot.getLocation().manhattanDistance(j, i);
                double beepProbabilityForGivenTile = Math.exp(-bot.getSensor().getSensitivity() * (manhattanDistance - 1));
                double likelihood = sensorBeeped ? beepProbabilityForGivenTile : 1 - beepProbabilityForGivenTile;
                double prior = probabilityMap[i][j];

                newProbabilityMap[i][j] = prior * likelihood;
                normalizationFactor += newProbabilityMap[i][j];
            }
        }

        // Normalize probability map
        double maxProbability = -1.0;
        Location target = new Location(0, 0);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (bot.getShip().getTile(i, j).is(Status.BLOCKED)) {
                    continue;
                }

                newProbabilityMap[i][j] /= normalizationFactor;
                if (newProbabilityMap[i][j] > maxProbability) {
                    target = new Location(j, i);
                    maxProbability = newProbabilityMap[i][j];
                }
            }
        }

        Stack<Location> fringe = new Stack<>();
        HashSet<Location> visited = new HashSet<>();
        HashMap<Location, Location> parent = new HashMap<>();
        fringe.add(bot.getLocation());
        while (!fringe.isEmpty()) {
            Location current = fringe.pop();
            visited.add(current);

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
        
        App.logger.debug(toString());
        App.logger.writeTo("bot1rv");
    }

    @Override
    public String toString() {
        // probability map
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < probabilityMap.length; i++) {
            for (int j = 0; j < probabilityMap[i].length; j++) {
                sb.append(String.format("%.5f ", probabilityMap[i][j]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
