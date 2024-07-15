package org.cs440.agent.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.cs440.App;
import org.cs440.agent.Agent.Movement.Direction;
import org.cs440.agent.Bot;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;
import org.cs440.ship.Tile.Status;

public class Bot3 implements Algorithm {
    private static final double EPSILON = 1e-11; // Small constant for smoothing
    private static double MOVE_THRESHOLD = 0.005;
    
    private HashSet<Location> captured;
    private LinkedList<Direction> moveQueue;
    private double probabilityMap[][];
    private double transitionModel[][][];
    private double gradientMap[][];
    private double gradientTarget;
    private Location target;
    private boolean sense = true;
    
    private Ship ship;
    private Bot bot;

    public Bot3(Ship ship) {
        this.captured = new HashSet<>();
        this.moveQueue = new LinkedList<Direction>();
        this.ship = ship;
        int height = ship.getHeight();
        int width = ship.getWidth();
        // Since the is empty with a bot in it, the probability of a mouse being in any
        // open tile is uniform
        double uniformProbability = 1.0 / (ship.numOfOpen() + 1);
        probabilityMap = new double[height][width];
        gradientMap = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                probabilityMap[i][j] = uniformProbability;
                gradientMap[i][j] = 0.0;
            }
        }

        transitionModel = new double[5][height][width]; // 5 for 4 directions + stay in place
    }

    private boolean shouldSense(Bot bot) {
        double maxProbability = findMaxProbability();
        return sense;
    }

    @Override
    public void execute(Bot bot) {
        this.bot = bot;
        if (!shouldSense(bot)) {
            if (moveQueue.isEmpty() || gradientTarget + MOVE_THRESHOLD <= findMaxProbGradient()
                || probabilityMap[target.y()][target.x()] <= EPSILON) {
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
                adjustProbabilitiesAfterCapture(bot, x, y);
                bot.updateMoveCount();
                return;
            }
            bot.updateMoveCount();
            sense = true;
            return;
        }
        
        boolean sensorBeeped = bot.getSensor().beeped();

        update(bot, sensorBeeped);
        predict(bot);
        bot.updateSenseCount();
        sense = false;
    }

    public void updateTransitionModel() {
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED) || bot.getLocation().equals(j, i) || captured.contains(new Location(j, i))) {
                    continue;
                }

                List<Direction> validMoves = getValidMoves(j, i);
                double moveProbability = 1.0 / (validMoves.size() + EPSILON);
                for (Direction dir : validMoves) {
                    int newX = j + dir.dx;
                    int newY = i + dir.dy;
                    transitionModel[dir.ordinal()][i][j] = moveProbability * (1 - gradientMap[newY][newX]) * (1 - bot.getSensor().getSensitivity());
                }
            }
        }
        normalizeProbabilityMap(probabilityMap);
    }

    public void adjustProbabilitiesAfterCapture(Bot bot, int x, int y) {
        captured.add(new Location(x, y));
        update(bot, false);
        predict(bot);
    }

    private List<Direction> getValidMoves(int x, int y) {
        List<Direction> validMoves = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            int newX = x + dir.dx;
            int newY = y + dir.dy;
            if (ship.withinBounds(newX, newY) && !ship.getTile(newX, newY).is(Status.BLOCKED)) {
                if (bot != null && bot.getLocation().equals(newX, newY)) {
                    continue;
                }

                if (captured.contains(new Location(newX, newY))) {
                    continue;
                }

                validMoves.add(dir);
            }
        }
        return validMoves;
    }

    private void normalizeProbabilityMap(double[][] map) {
        double totalProbability = EPSILON;
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                totalProbability += map[i][j];
            }
        }

        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                map[i][j] /= totalProbability;
            }
        }
    }

    private void predict(Bot bot) {
        updateTransitionModel();
        double[][] newProbabilityMap = new double[ship.getHeight()][ship.getWidth()];
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED))
                    continue;

                if (captured.contains(new Location(j, i))) {
                    continue;
                }

                List<Direction> validMoves = getValidMoves(j, i);
                for (Direction dir : validMoves) {
                    int newX = j + dir.dx;
                    int newY = i + dir.dy;
                    newProbabilityMap[newY][newX] += probabilityMap[i][j] * transitionModel[dir.ordinal()][i][j] * (1 - gradientMap[i][j]);
                }
            }
        }
        probabilityMap = newProbabilityMap;
        normalizeProbabilityMap(probabilityMap);
    }

    private void update(Bot bot, boolean sensorBeeped) {
        double[][] newProbabilityMap = new double[ship.getHeight()][ship.getWidth()];

        double sensitivity = bot.getSensor().getSensitivity();
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                if (bot.getLocation().equals(j, i)) {
                    probabilityMap[i][j] = 0.0;
                }

                if (captured.contains(new Location(j, i))) {
                    newProbabilityMap[i][j] = 0.0;
                    continue;
                }

                int manhattanDistance = bot.getLocation().manhattanDistance(j, i);
                double beepProbability = Math.exp(-sensitivity * (manhattanDistance - 1));
                double likelihood = sensorBeeped ? beepProbability : 1 - beepProbability;
                newProbabilityMap[i][j] = probabilityMap[i][j] * likelihood;
                gradientMap[i][j] = Math.abs(newProbabilityMap[i][j] - probabilityMap[i][j]) / (probabilityMap[i][j] + EPSILON);
            }
        }
        probabilityMap = newProbabilityMap;

        App.logger.debug("\n" + toString());
    }

    private Location findMaxProbabilityLocation() {
        double maxProbability = 0.0;
        Location maxLocation = new Location(0, 0);
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                if (captured.contains(new Location(j, i))) {
                    continue;
                }

                if (probabilityMap[i][j] == 0.0) {
                    continue;
                }

                if (probabilityMap[i][j] * (1 - gradientMap[i][j]) > maxProbability) {
                    maxProbability = probabilityMap[i][j] * (1 - gradientMap[i][j]);
                    maxLocation = new Location(j, i);
                }
            }
        }
        return maxLocation;
    }

    private double findMaxProbGradient() {
        double maxPG = 0.0;
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                if (captured.contains(new Location(j, i))) {
                    continue;
                }

                if (probabilityMap[i][j] * (1 - gradientMap[i][j]) > maxPG) {
                    maxPG = probabilityMap[i][j] * (1 - gradientMap[i][j]);
                }
            }
        }
        return maxPG;
    }

    public Location findMaxProbGradientLocation() {
        double maxPG = 0.0;
        Location maxLocation = new Location(0, 0);
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                if (captured.contains(new Location(j, i))) {
                    continue;
                }

                if (probabilityMap[i][j] * (1 - gradientMap[i][j]) > maxPG) {
                    maxPG = probabilityMap[i][j] * (1 - gradientMap[i][j]);
                    maxLocation = new Location(j, i);
                }
            }
        }
        return maxLocation;
    }

    private double findMaxProbability() {
        double maxProbability = 0.0;
        for (int i = 0; i < ship.getHeight(); i++) {
            for (int j = 0; j < ship.getWidth(); j++) {
                if (ship.getTile(j, i).is(Status.BLOCKED)) {
                    continue;
                }

                if (probabilityMap[i][j] > maxProbability) {
                    maxProbability = probabilityMap[i][j];
                }
            }
        }
        return maxProbability;
    }

    public void planPath(Bot bot) {
        moveQueue.clear();
        Location target = findMaxProbGradientLocation();
        gradientTarget = (1 - gradientMap[target.y()][target.x()]) * probabilityMap[target.y()][target.x()];
        this.target = target;

        Queue<Location> fringe = new LinkedList<>();
        HashSet<Location> visited = new HashSet<>();
        HashMap<Location, Location> parent = new HashMap<>();
        fringe.add(bot.getLocation());
        while (!fringe.isEmpty()) {
            Location current = fringe.poll();
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

                if (visited.contains(neighbor) || captured.contains(neighbor)) {
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
