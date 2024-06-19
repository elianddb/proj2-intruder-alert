package com.cs440.ship;

import com.cs440.ship.Tile.Status;
import com.cs440.ship.Tile.Type;

public class Ship {
    public static final Tile.Type BLOCK = new Tile.Type('X', Tile.Status.BLOCKED);

    protected Tile[][] tiles;

    public Ship(int width, int height) {
        tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[y][x] = new Tile(BLOCK, x, y);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < tiles[0].length; y++) {
            for (int x = 0; x < tiles.length; x++) {
                sb.append(tiles[y][x].type.identifier + " ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
