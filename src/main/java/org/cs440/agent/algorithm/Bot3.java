package org.cs440.agent.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import org.cs440.App;
import org.cs440.agent.Agent.Movement.Direction;
import org.cs440.agent.Bot;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;
import org.cs440.ship.Tile.Status;

public class Bot3 implements Algorithm{
    private static final double EPSILON = 1e-10; // Small constant for smoothing

    private LinkedList<Direction> moveQueue;
    private double probabilityMap[][];
    private double transitionModel[][][];
    private boolean sense = true;
    private int recalculatePath = 0;

    private Ship ship;

    public Bot3(Ship ship) {
        this.moveQueue = new LinkedList<Direction>();
        this.ship = ship;
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
                
                probabilityMap[i][j] = uniformProbability;
            }
        }

        transitionModel = new double[height][width][5]; // 5 for 4 directions + stay
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) continue;
                List<Direction> validMoves = getValidMoves(j, i);
                double moveProbability = 1.0 / (validMoves.size());
                for (Direction dir : validMoves) {
                    transitionModel[i][j][dir.ordinal()] = moveProbability;
                }
            }
        }
    }

    private List<Direction> getValidMoves(int x, int y) {
        List<Direction> validMoves = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            int newX = x + dir.dx;
            int newY = y + dir.dy;
            if (ship.withinBounds(newX, newY) && ship.getTile(newX, newY).is(Status.OPEN)) {
                validMoves.add(dir);
            }
        }
        return validMoves;
    }

    private void normalizeProbabilityMap(double[][] map, double totalProbability) {
        totalProbability += EPSILON;
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (!ship.getTile(j, i).is(Status.BLOCKED)) {
                    map[i][j] = (map[i][j] + EPSILON) / totalProbability;
                }
            }
        }
    }

    private void predict() {
        double[][] newProbabilityMap = new double[ship.getHeight()][ship.getWidth()];
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) continue;
                for (Direction dir : Direction.values()) {
                    int prevX = j - dir.dx;
                    int prevY = i - dir.dy;
                    if (ship.withinBounds(prevX, prevY) && !ship.getTile(prevX, prevY).is(Status.BLOCKED)) {
                        newProbabilityMap[i][j] += probabilityMap[prevY][prevX] * transitionModel[prevY][prevX][dir.ordinal()];
                    }
                }
            }
        }
        
        probabilityMap = newProbabilityMap;
    }

    private void update(Bot bot) {
        boolean sensorBeeped = bot.getSensor().beeped();
        double[][] newProbabilityMap = new double[ship.getHeight()][ship.getWidth()];
        double totalProbability = 0.0;

        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED) || bot.getLocation().equals(j, i)) {
                    continue;
                }

                int manhattanDistance = bot.getLocation().manhattanDistance(j, i);
                double beepProbability = Math.exp(-bot.getSensor().getSensitivity() * (manhattanDistance - 1));
                double likelihood = sensorBeeped ? beepProbability : 1 - beepProbability;

                newProbabilityMap[i][j] = probabilityMap[i][j] * likelihood;
                totalProbability += newProbabilityMap[i][j];
            }
        }

        normalizeProbabilityMap(newProbabilityMap, totalProbability + EPSILON);
        probabilityMap = newProbabilityMap;
        
        App.logger.debug("\n" + toString());
    }

    @Override
    public void execute(Bot bot) {
        if (!sense) {
            if (moveQueue.isEmpty() || recalculatePath >= 8) {
                moveQueue.clear();
                planPath(bot);
                recalculatePath = 0;
            }
            StringBuilder sb = new StringBuilder();
            for (Direction direction : moveQueue) {
                sb.append(direction.toString() + " ");
            }
            App.logger.debug("Move Queue: {" + sb.toString() + "}");
            Direction direction = moveQueue.poll();
            bot.move(direction);
            int x = bot.getLocation().x() + direction.dx;
            int y = bot.getLocation().y() + direction.dy;
            App.logger.debug("Attempting to move to: (" + x + ", " + y + ")");
            if (bot.attemptCapture(x, y)) {
                probabilityMap[y][x] = 0.0;
                // Lower the probability of nearer tiles to bot with beep formula
                double totalProbability = 0.0;
                for (int i = 0; i < ship.getHeight(); i++) {
                    for (int j = 0; j < ship.getWidth(); j++) {
                        if (ship.getTile(j, i).is(Status.BLOCKED)) {
                            continue;
                        }

                        int manhattanDistance = bot.getLocation().manhattanDistance(j, i);
                        double beepProbability = Math.exp(-bot.getSensor().getSensitivity() * (manhattanDistance - 1));
                        probabilityMap[i][j] *= 1 - beepProbability * 0.5;
                        totalProbability += probabilityMap[i][j];
                    }
                }

                normalizeProbabilityMap(probabilityMap, totalProbability);
            }
            sense = true;
            ++recalculatePath;
            return;
        }

        // Update probability map
        predict();
        update(bot);

        sense = false;
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
