package org.cs440.agent;

import org.cs440.agent.Agent.Movement;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Status;

// TODO Implement Movement & Action interface
public class StochasticMouse extends Agent implements Movement {
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
}
