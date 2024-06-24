package org.cs440.agent;

import org.cs440.App;
import org.cs440.agent.Agent.Action;
import org.cs440.agent.Agent.Movement;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;

public class Bot extends Agent implements Movement, Action {
    protected Agent target;
    protected Sensor sensor;

    public Bot(char identifier, Agent target, double sensorSensitivity) {
        super(identifier);
        this.target = target;
        this.sensor = new Sensor(this, target, sensorSensitivity);
    }

    public Bot(char identifier, Agent target) {
        this(identifier, target, 0.1);
    }

    @Override
    public void move(Direction direction) {
        int x = location.x() + direction.dx;
        int y = location.y() + direction.dy;
        if (!ship.withinBounds(x, y)) {
            return;
        }

        Tile target = ship.getTile(x, y);
        if (!target.is(Ship.OPEN)) {
            return;
        }

        // You cannot change the state of a Tile without
        // the original Location object
        ship.setTile(location, Ship.OPEN);
        location = target.location();

        ship.setTile(location, Ship.OCCUPIED);
    }

    @Override
    public String toString() {
        return "Bot [identifier=" + identifier + ", location=" + location + "]";
    }

    @Override
    public void perform() {
        // TODO implement Bot1 action
        if (sensor.sense()) {
            App.logger.debug("Bot " + identifier + " sensed target " + target.identifier);
        }
    }

    @Override
    public boolean closed() {
        // TODO implement Bot1 completion condition
        return false;
    }
}
