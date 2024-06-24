package org.cs440.agent;

import org.cs440.ship.Ship;
import org.cs440.ship.Tile.Location;

public abstract class Agent {
    protected char identifier;
    protected Location location;
    protected Ship ship;

    public Agent(char identifier) {
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

    public interface Movement {
        public static enum Direction {
            UP(0, -1),
            DOWN(0, 1),
            LEFT(-1, 0),
            RIGHT(1, 0);
    
            public final int dx;
            public final int dy;
    
            private Direction(int dx, int dy) {
                this.dx = dx;
                this.dy = dy;
            }
        }
    
        public void move(Direction direction);
    }
    
    public static interface Action {
        // Coextensive = action stops when every other action stops
        //     closed() should simply return this value for the above
        public static final boolean COEXTENSIVE = true;

        public void perform();
        public boolean closed(); // No reason to continue performing actions
    }
}
