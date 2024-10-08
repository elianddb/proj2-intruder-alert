package org.cs440.agent;

import org.cs440.ship.Ship;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Location;
import org.cs440.ship.Tile.Status;
import org.cs440.ship.Tile.Type;

public abstract class Agent {
    protected char identifier;
    protected Location location;
    protected Ship ship;
    protected Type underneath;

    public Agent(char identifier) {
        this.identifier = identifier;
    }

    public void link(Ship ship) {
        this.ship = ship;
        Tile tile = ship.requestRandomTile(Status.OPEN);
        this.location = tile.location();
        this.underneath = tile.type();
        ship.setTile(location, Ship.OCCUPIED);
    }

    public Location getLocation() {
        return location;
    }

    public char getIdentifier() {
        return identifier;
    }

    public Ship getShip() {
        return ship;
    }

    public interface Capture {
        public boolean capture(int x, int y);
        public boolean isFree();
    }

    public interface Movement {
        public void move(Direction direction);

        public static enum Direction {
            UP(0, -1),
            DOWN(0, 1),
            LEFT(-1, 0),
            RIGHT(1, 0),
            NONE(0, 0);
    
            public final int dx;
            public final int dy;
    
            private Direction(int dx, int dy) {
                this.dx = dx;
                this.dy = dy;
            }
            public static Direction fromDx(int dx) {
                if (dx > 0) return RIGHT;
                else if (dx < 0) return LEFT;
                else return NONE;
            }
        
            public static Direction fromDy(int dy) {
                if (dy > 0) return DOWN;
                else if (dy < 0) return UP;
                else return NONE;
            }
        }
    }
    
    public static interface Action {
        // Coextensive = action stops when every other action stops
        //     closed() should simply return this value for the above
        public static final boolean COEXTENSIVE = true;

        public void perform();
        public boolean closed(); // Stops perform() when true
    }
}
