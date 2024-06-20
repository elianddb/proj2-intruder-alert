package org.cs440.entity;

import org.cs440.ship.Ship;

public class StochasticMouse extends Entity {
    public StochasticMouse(Ship ship, char identifier) {
        super(ship, identifier);
    }

    @Override
    public void move(Direction direction) {
        // StochasticMouse moves in a random direction
        Direction[] directions = Direction.values();
        int randomIndex = (int) (Math.random() * directions.length);
        super.move(directions[randomIndex]);
    }
}
