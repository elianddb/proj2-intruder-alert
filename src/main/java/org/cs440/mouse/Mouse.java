package org.cs440.mouse;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Type;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Mouse {
    protected int currentX = 0;
    protected int currentY = 0;
    protected Ship ship;

    private static final Logger logger = Logger.getLogger(Mouse.class.getName());

    public Mouse(int startX, int startY, Ship ship) {
        this.currentX = startX;
        this.currentY = startY;
        this.ship = ship;
    }

    public int getX() {
        return currentX;
    }
    public int getY() {
        return currentY;
    }
    public void kill() {
        Tile currentTile = ship.getTile(currentX, currentY);
        currentTile.set(new Type('*', Tile.Status.OCCUPIED));
    }
    public boolean isDead() {
        Tile currentTile = ship.getTile(currentX, currentY);
        return currentTile.is(new Type('*', Tile.Status.OCCUPIED));
    }

}
