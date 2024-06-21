package org.cs440.entity;

import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;

public abstract class Entity {
    protected char identifier;
    protected Location location;
    protected Ship ship;

    public Entity(char identifier) {
        this.identifier = identifier;
    }

    public Location location() {
        return location;
    }

    public char identifier() {
        return identifier;
    }

    public void link(Ship ship) {
        this.ship = ship;
        this.location = ship.requestOpen();
        ship.setTile(location, Ship.OCCUPIED);
    }
}
