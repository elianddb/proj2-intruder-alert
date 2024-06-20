package org.cs440.entity;

import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;

public abstract class Entity {
    public enum Direction {
        UP(0, 1),
        DOWN(0, -1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        public final int dx;
        public final int dy;

        private Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    protected Ship ship;
    protected char identifier;
    protected Location location;

    public Entity(Ship ship, char identifier) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship cannot be null");
        }

        this.ship = ship;
        this.location = ship.requestOpen();
        ship.occupyTile(this.location);
        this.identifier = identifier;
    }

    public void move(Direction direction) {
        Location targetLoc = new Location(location.x + direction.dx, location.y + direction.dy);
        if (!ship.withinBounds(targetLoc)) {
            return;
        }

        this.location = ship.request(this.location, targetLoc);
        if (this.location.equals(targetLoc)) {
            ship.occupyTile(this.location);
        }
    }
}
