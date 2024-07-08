package org.cs440.agent;

import org.cs440.agent.Agent.Action;
import org.cs440.agent.Agent.Movement;
import org.cs440.agent.algorithm.Algorithm;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Status;

public class Bot extends Agent implements Movement, Action  {
    protected Sensor sensor;
    private Capture[] targets;
    private double[][] probabilityMap;
    private Algorithm algorithm;
    private int moveCount = 0;
    private int senseCount = 0;

    public Bot(char identifier, Agent[] targets, double sensorSensitivity, Algorithm algorithm) {
        super(identifier);
        this.sensor = new Sensor(this, targets, sensorSensitivity);
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

    public void updateMoveCount() {
        moveCount += 1;
    }

    public void updateSenseCount() {
        senseCount += 1;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getSenseCount() {
        return senseCount;
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
}
