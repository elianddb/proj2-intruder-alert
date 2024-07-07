package org.cs440.agent.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import org.cs440.App;
import org.cs440.agent.Agent.Movement.Direction;
import org.cs440.agent.Bot;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;
import org.cs440.ship.Tile.Status;

public class Bot3 implements Algorithm {
    private static final double EPSILON = 1e-11; // Small constant for smoothing
    private static final int MCMC_ITERATIONS = 1000;
    private static final double TEMPERATURE = 0.1;

    private LinkedList<Direction> moveQueue;
    private double probabilityMap[][];
    private double transitionModel[][][];
    private boolean sense = true;

    private Location lastMaxProbabilityLocation;

    private Ship ship;

    public Bot3(Ship ship) {
        this.moveQueue = new LinkedList<Direction>();
        this.ship = ship;
        int height = ship.getHeight();
        int width = ship.getWidth();
        // Since the is empty with a bot in it, the probability of a mouse being in any
        // open tile is uniform
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
                if (ship.getTile(j, i).is(Status.BLOCKED))
                    continue;
                List<Direction> validMoves = getValidMoves(j, i);
                double moveProbability = 1.0 / (validMoves.size());
                for (Direction dir : validMoves) {
                    // adjust with alpha val worth
                    transitionModel[i][j][dir.ordinal()] = moveProbability;
                }
            }
        }
    }

    @Override
    public void execute(Bot bot) {
        if (!sense) {
            if (moveQueue.isEmpty()) {
                planPath(bot);
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
                adjustProbabilitiesAfterCapture(bot);
                return;
            }
            sense = true;
            return;
        }
        
        boolean sensorBeeped = bot.getSensor().beeped();

        predict(bot);
        update(bot, sensorBeeped);

        refineProbabilitiesWithMCMC(bot);

        sense = false;
    }

    private void refineProbabilitiesWithMCMC(Bot bot) {
        double[][] newProbabilityMap = new double[ship.getHeight()][ship.getWidth()];
        Random random = new Random();
        for (int iter = 0; iter < MCMC_ITERATIONS; iter++) {
            Location current = sampleLocation();
            Location proposed = proposeNeighbor(current);
            
            double currentProb = getProbability(current);
            double proposedProb = getProbability(proposed);
            
            double acceptanceRatio = Math.min(1, proposedProb / currentProb);
            
            if (random.nextDouble() < acceptanceRatio) {
                newProbabilityMap[proposed.y()][proposed.x()] += 1;
            } else {
                newProbabilityMap[current.y()][current.x()] += 1;
            }
        }
        
        normalizeProbabilityMap(newProbabilityMap, MCMC_ITERATIONS);
        blendProbabilityMaps(newProbabilityMap, 0.3);
    }

    private Location sampleLocation() {
        double totalProb = Arrays.stream(probabilityMap)
                                 .flatMapToDouble(Arrays::stream)
                                 .sum();
        Random random = new Random();
        double randomValue = random.nextDouble() * totalProb;
        
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                randomValue -= probabilityMap[i][j];
                if (randomValue <= 0) {
                    return new Location(j, i);
                }
            }
        }
        
        return new Location(0, 0);  // Fallback, should rarely happen
    }

    private Location proposeNeighbor(Location current) {
        Random random = new Random();
        List<Direction> validMoves = getValidMoves(current.x(), current.y());
        if (validMoves.size() <= 1) return current;
        Direction randomDirection = validMoves.get(random.nextInt(validMoves.size()));
        return new Location(current.x() + randomDirection.dx, current.y() + randomDirection.dy);
    }

    private double getProbability(Location loc) {
        return probabilityMap[loc.y()][loc.x()];
    }

    private void blendProbabilityMaps(double[][] newMap, double weight) {
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                probabilityMap[i][j] = (1 - weight) * probabilityMap[i][j] + weight * newMap[i][j];
            }
        }
    }

    public void adjustProbabilitiesAfterCapture(Bot bot) {
        
        double totalProbability = 0.0;
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                int manhattanDistance = bot.getLocation().manhattanDistance(j, i);
                double beepProbability = Math.exp(-bot.getSensor().getSensitivity() * (manhattanDistance - 1));
                probabilityMap[i][j] *= (1 - beepProbability) * 0.001;
                totalProbability += probabilityMap[i][j];
            }
        }

        normalizeProbabilityMap(probabilityMap, totalProbability);
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
                    map[i][j] = (map[i][j]) / totalProbability;
                }
            }
        }
    }

    private void predict(Bot bot) {
        double[][] newProbabilityMap = new double[ship.getHeight()][ship.getWidth()];
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED))
                    continue;
                for (Direction dir : Direction.values()) {
                    int prevX = j - dir.dx;
                    int prevY = i - dir.dy;
                    if (ship.withinBounds(prevX, prevY) && !ship.getTile(prevX, prevY).is(Status.BLOCKED)) {
                        newProbabilityMap[i][j] += probabilityMap[prevY][prevX]
                                * transitionModel[prevY][prevX][dir.ordinal()];
                    }
                }
            }
        }

        probabilityMap = newProbabilityMap;
    }

    private void update(Bot bot, boolean sensorBeeped) {
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

        normalizeProbabilityMap(newProbabilityMap, totalProbability);
        probabilityMap = newProbabilityMap;

        App.logger.debug("\n" + toString());
    }

    private Location findMaxProbabilityLocation() {
        double maxProbability = 0.0;
        Location maxLocation = null;
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                if (probabilityMap[i][j] > maxProbability) {
                    maxProbability = probabilityMap[i][j];
                    maxLocation = new Location(j, i);
                }
            }
        }
        this.lastMaxProbabilityLocation = maxLocation;
        return maxLocation;
    }

    private void applyMCMC(Bot bot) {
        Random random = new Random();
        for (int iter = 0; iter < MCMC_ITERATIONS; iter++) {
            Location currentLocation = sampleFromProbabilityMap();
            Location proposedLocation = proposeNewLocation(currentLocation);
            
            double currentProbability = probabilityMap[currentLocation.y()][currentLocation.x()];
            double proposedProbability = probabilityMap[proposedLocation.y()][proposedLocation.x()];
            
            double acceptanceProbability = Math.min(1, proposedProbability / currentProbability);
            
            if (random.nextDouble() < acceptanceProbability) {
                // Accept the proposed location
                probabilityMap[currentLocation.y()][currentLocation.x()] *= 0.99;
                probabilityMap[proposedLocation.y()][proposedLocation.x()] *= 1.01;
            }
        }
    }

    private Location sampleFromProbabilityMap() {
        Random random = new Random();
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;
        
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                cumulativeProbability += probabilityMap[i][j];
                if (cumulativeProbability >= randomValue) {
                    return new Location(j, i);
                }
            }
        }
        
        // This should never happen if the probability map is normalized
        return new Location(0, 0);
    }

    private Location proposeNewLocation(Location currentLocation) {
        Random random = new Random();
        List<Direction> validMoves = getValidMoves(currentLocation.x(), currentLocation.y());
        if (validMoves.size() == 0 || validMoves.size() == 1) {
            return currentLocation;
        }
        Direction randomDirection = validMoves.get(random.nextInt(validMoves.size()));
        int newX = currentLocation.x() + randomDirection.dx;
        int newY = currentLocation.y() + randomDirection.dy;
        return new Location(newX, newY);
    }

    // Follow path that maximizes information gained (tiles with higher validMoves)
    public void planPath(Bot bot) {
        Location target = findMaxProbabilityLocation();

        Queue<Location> fringe = new LinkedList<>();
        HashSet<Location> visited = new HashSet<>();
        HashMap<Location, Location> parent = new HashMap<>();
        fringe.add(bot.getLocation());
        while (!fringe.isEmpty()) {
            Location current = fringe.poll();
            visited.add(current);
            // App.logger.debug("Current: " + current.toString() + " Target: " +
            // target.toString());

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
