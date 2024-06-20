package org.cs440.entity;

import org.cs440.ship.Ship;

public class StationaryMouse extends Entity {
    public StationaryMouse(Ship ship, char identifier) {
        super(ship, identifier);
    }

    @Override
    public void move(Direction direction) {
        // StationaryMouse does not move
    }
}
