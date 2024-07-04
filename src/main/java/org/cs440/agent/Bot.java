package org.cs440.agent;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.cs440.App;
import org.cs440.agent.Agent.Action;
import org.cs440.agent.Agent.Movement;
import org.cs440.agent.algorithm.Algorithm;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Location;
import org.cs440.ship.Tile.Status;

public class Bot extends Agent implements Movement, Action  {
    protected Sensor sensor;
    private Capture[] targets;
    private double[][] probabilityMap;
    private final static int GRID_SIZE = 40;
    private Queue<Direction> moveQueue = new LinkedList<>();
    private boolean firstIteration = true;
    private int moveCount = 0;
    private int senseCount = 0;
    private Algorithm algorithm;
    private boolean botMove = false;

    public Bot(char identifier, Agent[] targets, double sensorSensitivity, Algorithm algorithm) {
        super(identifier);
        this.sensor = new Sensor(this, targets, sensorSensitivity);
        this.probabilityMap = new double[GRID_SIZE][GRID_SIZE];
        this.algorithm = algorithm;
        this.targets = new Capture[targets.length];
        for (int i = 0; i < targets.length; i++) {
            this.targets[i] = (Capture) targets[i];
        }
    }

    public Bot(char identifier, Agent target, double sensorSensitivity, Algorithm algorithm) {
        super(identifier);
        this.sensor = new Sensor(this, target, sensorSensitivity);
        this.targets = new Capture[] { (Capture) target };
        this.probabilityMap = new double[GRID_SIZE][GRID_SIZE];
        this.algorithm = algorithm;
    }

    public Bot(char identifier, Agent target, Algorithm algorithm) {
        this(identifier, target, 0.1, algorithm);
    }

    public boolean attemptCapture(int x, int y) {
        for (Capture target : targets) {
            if (!target.isFree()) continue;
            if (target.capture(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void move(Direction direction) {
        int x = location.x() + direction.dx;
        int y = location.y() + direction.dy;
        if (!ship.withinBounds(x, y)) {
            return;
        }

        Tile destination = ship.getTile(x, y);
        if (!destination.is(Status.OPEN)) {
            return;
        }

        // You cannot change the state of an Status.OCCUPIED 
        // Tile without the original Location object
        ship.setTile(location, underneath);
        location = destination.location();

        underneath = ship.getTile(location).type();
        ship.setTile(location, Ship.OCCUPIED);
    }

    @Override
    public void perform() {
        if (closed()) { // Don't want to keep moving if goal is complete
            return;
        }

        algorithm.execute(this);
    }

    @Override
    public boolean closed() {
        for (Capture target : targets) {
            if (target.isFree()) {
                return false;
            }
        }
        return true;
    }

    public void bot1() {
        if(firstIteration == true) {
            initializeProbabilityMap();
            firstIteration = false;
        }

        if (moveQueue.isEmpty()) {
            App.logger.debug("MoveQueue is empty, planning path.");
            boolean sensed = sensor.beeped();
            senseCount++;
            updateProbabilityMap(sensed);
            planPath();
        } else {
            App.logger.debug("Executing move from queue.");
            Direction direction = moveQueue.poll();
            int x = location.x() + direction.dx;
            int y = location.y() + direction.dy;
            move(direction);
            if (attemptCapture(x, y)) {
                App.logger.debug("Bot " + identifier + " encountered target at location " + location);
                // App.logger.debug("Bot " + identifier + " killed target " + ((Agent)targets).getIdentifier());
                App.logger.debug("Killed target in " + moveCount + " moves and " + senseCount + " senses for a total of " + (moveCount + senseCount) + " actions.");
                return; // Didn't move target was in way
            }
            moveCount++;
        } 
    }

    public void bot2() {
        if(firstIteration == true) {
            initializeProbabilityMap();
            firstIteration = false;
        }
        if (moveQueue.isEmpty()) {
            App.logger.debug("MoveQueue is empty, planning path.");
            boolean sensed = sensor.beeped();
            senseCount++;
            updateProbabilityMap(sensed);
            planPath();
        } else if (botMove == true) {
            App.logger.debug("Moved previously, sensing.");
            boolean sensed = sensor.beeped();
            senseCount++;
            updateProbabilityMap(sensed);
            botMove = false;
        } else {
            App.logger.debug("Executing move from queue.");
            Direction direction = moveQueue.poll();
            int x = location.x() + direction.dx;
            int y = location.y() + direction.dy;
            move(direction);
            if (attemptCapture(x, y)) {
                App.logger.debug("Bot " + identifier + " encountered target at location " + location);
                // App.logger.debug("Bot " + identifier + " killed target " + ((Agent)targets).getIdentifier());
                App.logger.debug("Killed target in " + moveCount + " moves and " + senseCount + " senses for a total of " + (moveCount + senseCount) + " actions.");
                return; // Didn't move target was in way
            }
            moveCount++;
            botMove = true;
        }
    }

    public void bot3() {
        //TO DO: Decide and implement bot3
        return;
    }

    private void planPath() {
        Location nextLocation = getMaxProbabilityLocation();
        moveQueue.clear();
        findPath(nextLocation);
    }

    public int getMoves() {
        return moveCount;
    }

    public int getSenses() {
        return senseCount;
    }
    
    private Location getMaxProbabilityLocation() {
        Location maxLocation = location;  
        double maxProbability = 0.0;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                double currentProbability = probabilityMap[i][j];
                int distance = Math.abs(i - location.x()) + Math.abs(j - location.y());
                int maxDistance = Math.abs(maxLocation.x() - location.x()) + Math.abs(maxLocation.y() - location.y());
                if (probabilityMap[i][j] > maxProbability || (currentProbability == maxProbability && distance < maxDistance)) {
                    maxProbability = currentProbability;
                    maxLocation = new Location(i, j);
                }
            }
        }
        return maxLocation;
    }

    private void findPath(Location targetLocation) {
        bfs(location, targetLocation);
    }

    private void bfs(Location start, Location goal) {
        if (start.equals(goal)) {
            App.logger.debug("Start and goal are the same.");
            return;
        }
        Queue<Location> queue = new LinkedList<>();
        Map<Location, Location> starter = new HashMap<>();
        Set<Location> visited = new HashSet<>();
    
        queue.add(start);
        visited.add(start);
        starter.put(start, null);
    
        while (!queue.isEmpty()) {
            Location current = queue.poll();
            App.logger.debug("Current location: " + current);
    
            if (current.equals(goal)) {
                App.logger.debug("Goal reached: " + goal);
                reconstructPath(starter, current);
                return;
            }
            for (Direction direction : Direction.values()) {
                Location next = new Location(current.x() + direction.dx, current.y() + direction.dy);
                if (isValid(next, visited)) {
                    if (!visited.contains(next)) { 
                        queue.add(next);
                        visited.add(next);
                        starter.put(next, current);
                        App.logger.debug("Adding to queue: " + next);
                    }
                }
            }
        }
    
        App.logger.debug("No path found from " + start + " to " + goal);
    }
    
    private boolean isValid(Location loc, Set<Location> visited) {
        return ship.withinBounds(loc.x(), loc.y()) && !visited.contains(loc) && (ship.getTile(loc.x(), loc.y()).is(Tile.Status.OPEN)|| ship.getTile(loc.x(), loc.y()).is(Tile.Status.OCCUPIED));
    }
    
    private void reconstructPath(Map<Location, Location> cameFrom, Location current) {
        Deque<Direction> path = new LinkedList<>();
        while (current != null) {
            Location prev = cameFrom.get(current);
            if (prev == null) break;
    
            Direction direction = determineDirectionFromLocations(prev, current);
            path.addFirst(direction); 
            current = prev;
        }
    
        moveQueue.addAll(path); 
        App.logger.debug("Path reconstructed: " + path);
    }
    private Direction determineDirectionFromLocations(Location from, Location to) {
        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        if (dx > 0) return Direction.RIGHT;
        if (dx < 0) return Direction.LEFT;
        if (dy > 0) return Direction.DOWN;
        if (dy < 0) return Direction.UP;
        return Direction.NONE;
    }
    private void updateProbabilityMap(boolean sensed) {
        double normalizationFactor = 0.0;

        double[][] tempProbabilityMap = new double[GRID_SIZE][GRID_SIZE];

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (ship.getTile(i, j).is(Tile.Status.OPEN)) {  
                    int distance = Math.abs(i - location.x()) + Math.abs(j - location.y());
                    double probability = Math.exp(-sensor.getSensitivity() * (distance - 1));
                if (!sensed) {
                    probability = 1 - probability;
                }
                tempProbabilityMap[i][j] = probabilityMap[i][j] * probability;
                normalizationFactor += tempProbabilityMap[i][j];
                } else {
                    tempProbabilityMap[i][j] = 0; 
                }
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (ship.getTile(i, j).is(Tile.Status.OPEN)) {
                    probabilityMap[i][j] = tempProbabilityMap[i][j] / normalizationFactor;
                } else {
                    probabilityMap[i][j] = 0;
                }
            }
        }
    }

    private void initializeProbabilityMap() {
        double initialProbability = 1.0 / (GRID_SIZE * GRID_SIZE);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if(ship.getTile(i, j).is(Tile.Status.OPEN)) {
                    probabilityMap[i][j] = initialProbability;
                } else {
                    probabilityMap[i][j] = 0;
                }   

            }
        }
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Capture[] getTargets() {
        return targets;
    }

    @Override
    public String toString() {
        return "Bot [identifier=" + identifier + ", location=" + location + "]";
    }

    public int numOfOpen() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'numOfOpen'");
    }
}
