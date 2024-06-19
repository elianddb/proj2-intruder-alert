package org.cs440.ship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.cs440.ship.Tile.Type;
import org.cs440.ship.Tile.Location;

public class Ship {
    public static final Type BLOCK = new Tile.Type('X', Tile.Status.BLOCKED);
    public static final Type OPEN = new Tile.Type('.', Tile.Status.OPEN);
    public static final Type OCCUPIED = new Tile.Type('O', Tile.Status.OCCUPIED);

    protected Tile[][] tiles;
    protected HashMap<Type, HashSet<Tile>> tileSets;

    /**
     * Create a new Ship with the given width and height. All tiles are initially blocked.
     * 
     * @param width
     * @param height
     */
    public Ship(int width, int height) {
        tiles = new Tile[height][width];
        
        // Setup tile sets for the above common blocks
        tileSets = new HashMap<>();
        tileSets.put(OPEN, new HashSet<Tile>());
        tileSets.put(BLOCK, new HashSet<Tile>());
        tileSets.put(OCCUPIED, new HashSet<Tile>());

        // Fill the initial ship with blocked tiles
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(BLOCK, x, y);
                tileSets.get(BLOCK).add(tiles[y][x]);
            }
        }

        // Open one random tile
        int x = (int) (Math.random() * width);
        int y = (int) (Math.random() * height);
        openTile(x, y);

        // Generate paths
        
    }

    public boolean withinBounds(int x, int y) {
        return x >= 0 && x < tiles[0].length && y >= 0 && y < tiles.length;
    }

    public boolean withinBounds(Location location) {
        return withinBounds(location.x, location.y);
    }

    public void enforceBounds(int x, int y) {
        int height = tiles.length;
        int width = tiles[0].length;
        if (x < 0 || x >= tiles[0].length) {
            throw new IllegalArgumentException(String.format("x out of bounds: %d (width: %d)", x, width));
        }
        if (y < 0 || y >= tiles.length) {
            throw new IllegalArgumentException(String.format("y out of bounds: %d (height: %d)", y, height));
        }
    }

    public void enforceBounds(Location location) {
        enforceBounds(location.x, location.y);
    }

    public void enforceOwnership(int x, int y) {
        Tile tile = getTile(x, y);
        if (tile.is(OCCUPIED)) {
            throw new IllegalArgumentException(String.format("Tile already occupied %s", tile));
        }
    }

    public void enforceOwnership(Location location) {
        enforceOwnership(location.x, location.y);
    }

    public Tile getTile(int x, int y) {
        enforceBounds(x, y);
        return tiles[y][x];
    }

    public Tile getTile(Location location) {
        return getTile(location.x, location.y);
    }

    public void setTile(int x, int y, Tile.Type type) {
        enforceBounds(x, y);
        if (tiles[y][x].is(type)) {
            return;
        }
        enforceOwnership(x, y);
        tileSets.get(tiles[y][x].type).remove(tiles[y][x]);
        tileSets.get(type).add(tiles[y][x]);

        tiles[y][x].set(type);
    }

    public void setTile(Location location, Tile.Type type) {
        setTile(location.x, location.y, type);
    }

    public void openTile(int x, int y) {
        setTile(x, y, OPEN);
    }

    public void openTile(Location location) {
        openTile(location.x, location.y);
    }

    public void blockTile(int x, int y) {
        setTile(x, y, BLOCK);
    }

    public void blockTile(Location location) {
        blockTile(location.x, location.y);
    }

    public void occupyTile(int x, int y) {
        setTile(x, y, OCCUPIED);
    }

    public void occupyTile(Location location) {
        occupyTile(location.x, location.y);
    }

    public int numOfBlocks() {
        return tileSets.get(BLOCK).size();
    }

    public int numOfOpen() {
        return tileSets.get(OPEN).size();
    }

    public int numOfOccupied() {
        return tileSets.get(OCCUPIED).size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[y].length; x++) {
                sb.append(tiles[y][x].type.identifier);
                sb.append(x < tiles[y].length - 1 ? ' ' : '\n');
            }
        }
        return sb.toString();
    }
}
