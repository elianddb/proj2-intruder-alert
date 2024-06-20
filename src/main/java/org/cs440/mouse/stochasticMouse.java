package org.cs440.mouse;
import org.cs440.ship.Ship;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs440.ship.Tile;
import org.cs440.ship.Tile.Type;

public class stochasticMouse extends Mouse{
    private static final Logger logger = Logger.getLogger(stochasticMouse.class.getName());

    public stochasticMouse(int startX, int startY, Ship ship) {
        super(startX, startY, ship);
    }
    public boolean move(int x, int y) {
        try {
            ship.enforceBounds(x, y);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
        Tile tile = ship.getTile(x, y);
        if(!((tile.is(Tile.Status.BLOCKED)) && (tile.is(Tile.Status.OCCUPIED)))) {
            Tile currentTile = ship.getTile(currentX, currentY);
            currentTile.set(new Type('.', Tile.Status.OPEN));
            currentX = x;
            currentY = y;
            tile.set(new Type('%', Tile.Status.OCCUPIED));
            logger.log(Level.INFO, "Mouse moved to (" + x + ", " + y + "");
            return true;
        }
        return false;
    }
    public void moveLeft() {
        move(currentX - 1, currentY);
    }
    public void moveRight() {
        move(currentX + 1, currentY);
    }
    public void moveUp() {
        move(currentX, currentY - 1);
    }
    public void moveDown() {
        move(currentX, currentY + 1);
    }
}
