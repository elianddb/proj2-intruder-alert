package org.cs440.agent;

import org.cs440.App;
import org.cs440.agent.Agent.Action;
import org.cs440.agent.Agent.Movement;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Status;

// TODO Implement Movement & Action interface
public class StochasticMouse extends Agent implements Movement, Action {
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
        ship.setTile(location, Ship.OPEN);
        location = destination.location();

        ship.setTile(location, Ship.OCCUPIED);
    }

    @Override
    public void perform() {
        int direction = (int) (Math.random() * 4);
        App.logger.debug("StochasticMouse " + identifier + " moving in direction " + direction);
        switch (direction) {
            case 0:
                move(Direction.UP);
                break;
            case 1:
                move(Direction.DOWN);
                break;
            case 2:
                move(Direction.LEFT);
                break;
            case 3:
                move(Direction.RIGHT);
                break;
        }
    }

    @Override
    public boolean closed() {
        return COEXTENSIVE;
    }

    @Override
    public String toString() {
        return "StochasticMouse [identifier=" + identifier + ", location=" + location + "]";
    }
}
