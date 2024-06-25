package org.cs440.agent;

import java.util.ArrayList;

import org.cs440.App;
import org.cs440.agent.Agent.Action;
import org.cs440.agent.Agent.Movement;
import org.cs440.agent.Agent.Target;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Status;

public class StochasticMouse extends Agent implements Movement, Action, Target {
    private boolean alive = true;

    public StochasticMouse(char identifier) {
        super(identifier);
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
        if (!alive) {
            return;
        }
        
        ArrayList<Direction> availableDirections = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            int x = location.x() + direction.dx;
            int y = location.y() + direction.dy;
            if (!ship.withinBounds(x, y) || !ship.getTile(x, y).is(Status.OPEN)) {
                continue;
            }

            availableDirections.add(direction);
        }
        availableDirections.add(Direction.NONE);
        int randomIndex = (int) (Math.random() * availableDirections.size());
        Direction direction = availableDirections.get(randomIndex);
        App.logger.debug("StochasticMouse " + identifier + " moving in direction " + direction);
        move(direction);
    }

    @Override
    public boolean closed() {
        return COEXTENSIVE;
    }

    @Override
    public void interact(Interaction interaction) {
        switch (interaction) {
            case KILL:
                alive = false;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean alive() {
        return alive;
    }

    @Override
    public String toString() {
        return "StochasticMouse [identifier=" + identifier + ", location=" + location + "]";
    }
}
