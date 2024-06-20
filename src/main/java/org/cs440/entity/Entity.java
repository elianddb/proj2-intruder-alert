package org.cs440.entity;

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

    protected char identifier;
    protected Location location;

    public Entity(char identifier, Location location) {
        this.identifier = identifier;
        this.location = location;
    }

    public Entity(char identifier, int x, int y) {
        this(identifier, new Location(x, y));
    }

    public void move(Direction direction) {
        location.x += direction.dx;
        location.y += direction.dy;
    }
}
