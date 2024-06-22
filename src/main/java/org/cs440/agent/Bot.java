package org.cs440.agent;

import org.cs440.App;
import org.cs440.agent.Agent.Action;
import org.cs440.agent.Agent.Movement;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;

public class Bot extends Agent implements Movement, Action {
    public Bot(char identifier) {
        super(identifier);
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
        // the original Location object.
        ship.setTile(location, Ship.OPEN);
        location = target.location();

        ship.setTile(location, Ship.OCCUPIED);
    }

    @Override
    public String toString() {
        return "Bot [identifier=" + identifier + ", location=" + location + "]";
    }

    @Override
    public void act() {
        // TODO implement Bot action
        App.logger.debug("Bot action executed!");
    }
}
