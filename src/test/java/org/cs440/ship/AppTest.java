package org.cs440.ship;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void locationsMatch() {
        Tile tile = new Tile('X', Tile.Status.BLOCKED, 0, 0);
        assertTrue(tile.location.x == 0);
        assertTrue(tile.location.y == 0);

        Ship ship = new Ship(40, 40);
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                assertTrue(ship.tiles[y][x].location.x == x);
                assertTrue(ship.tiles[y][x].location.y == y);
            }
        }
    }

    @Test
    public void correctTilesGot() {
        Ship ship = new Ship(40, 40);
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                assertTrue(ship.getTile(x, y) == ship.tiles[y][x]);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsOutOfBounds() {
        Ship ship = new Ship(40, 40);
        ship.getTile(-1, -1);
    }
}
