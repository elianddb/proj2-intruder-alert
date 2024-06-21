package org.cs440.entity;

import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;

public abstract class Entity {
    protected Ship ship;
    protected char identifier;
    protected Location location;

    public Entity(Ship ship, char identifier) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship cannot be null");
        }

        this.ship = ship;
        this.location = ship.requestOpen(); // Returns new Location object
        ship.setTile(location, Ship.OCCUPIED);
        this.identifier = identifier;
    }
}
