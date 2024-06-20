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
    public static final Type UNKNOWN = new Tile.Type('?', null);

    protected Tile[][] tiles;
    protected HashSet<Tile> deadEnds;
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
        // Make sure to do first before using tile modification methods
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
        // Gather all potential paths (BLOCKs) around the first OPEN tile
        // Essentially satisfying that all BLOCKs are next to *one* OPEN tile 
        HashSet<Tile> potentialPaths = new HashSet<>();
        for (Tile tile : tileSets.get(OPEN)) {
            for (Location location : tile.location.cardinalNeighbors()) {
                if (withinBounds(location) && getTile(location).is(BLOCK)) {
                    potentialPaths.add(getTile(location));
                }
            }
        }

        // Open potential paths until no more are available on the following conditions:
        // 1. BLOCK has one open neighbor
        HashSet<Tile> obseletePotentialPaths = new HashSet<>(); // BLOCKs that have more than one OPEN neighbor
        HashSet<Tile> deadEnds = new HashSet<>(); // OPEN tiles with one OPEN neighbor
        HashSet<Tile> obseleteDeadEnds = new HashSet<>(); // OPEN tiles with more than one OPEN neighbor
        while (!potentialPaths.isEmpty()) {
            // Randomly open a potential path
            ArrayList<Tile> rand = new ArrayList<>(potentialPaths); // Ramdom access
            Tile path = rand.get((int) (Math.random() * rand.size()));
            openTile(path.location);
            potentialPaths.remove(path);

            // Append new neighboring potential paths from the newly opened path
            int openNeighbors = 0;
            for (Location location : path.location.cardinalNeighbors()) {
                if (!withinBounds(location)) {
                    continue;
                }
                
                Tile neighbor = getTile(location);
                if (neighbor.is(OPEN)) { // Keeps track of deadEnds
                    ++openNeighbors;
                    // The OPEN neighbor would have two OPEN neighbors implcitly now
                    // since the path above is now OPEN
                    if (deadEnds.contains(neighbor)) { 
                        deadEnds.remove(neighbor);
                        obseleteDeadEnds.add(neighbor);
                    }
                    continue;
                }
                
                if (obseletePotentialPaths.contains(neighbor)) {
                    continue;
                }
                
                // Remove BLOCKs already in the potential paths because this means
                // they neighbor more than one OPEN tile
                if (potentialPaths.contains(neighbor)) {
                    potentialPaths.remove(neighbor);
                    obseletePotentialPaths.add(neighbor);
                    continue;
                }

                potentialPaths.add(neighbor);
            }

            if (openNeighbors == 1) {
                deadEnds.add(path);
            }
        }

        // Pick half of dead ends and open a random neighbor
        ArrayList<Tile> deadEndsItems = new ArrayList<>(deadEnds); // Random access
        for (int i = 0; i < deadEnds.size() / 2; i++) {
            Tile deadEnd = deadEndsItems.get(i);
            ArrayList<Location> blockNeighbors = new ArrayList<>();
            for (Location cardinalNeighbor : deadEnd.location.cardinalNeighbors()) { // Filter out OPEN neighbors
                if (withinBounds(cardinalNeighbor) && getTile(cardinalNeighbor).is(BLOCK)) {
                    blockNeighbors.add(cardinalNeighbor);
                }
            }
            Location neighbor = blockNeighbors.get((int) (Math.random() * blockNeighbors.size()));
            openTile(neighbor);

            for (Location cardinalNeighbor : neighbor.cardinalNeighbors()) { // Remove new obselete dead ends
                if (withinBounds(cardinalNeighbor) && getTile(cardinalNeighbor).is(OPEN)) {
                    deadEnds.remove(getTile(cardinalNeighbor));
                    deadEndsItems.remove(getTile(cardinalNeighbor));
                }
            }

            deadEnds.remove(deadEnd); // Initial deadEnd is now OPEN due to new open neighbor
        }

        this.deadEnds = deadEnds;
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

    public void requestTransfer(Location location, Location target) {
        enforceBounds(location);
        if (!getTile(location).is(OCCUPIED)) {
            throw new IllegalArgumentException(String.format("Entity not at OCCUPIED tile %s", location));
        }

        enforceBounds(target);
        if (getTile(location).is(OPEN)) {
            location.x = target.x;
            location.y = target.y;
        }
    }

    public Location requestOpen() {
        ArrayList<Tile> openTiles = new ArrayList<>(tileSets.get(OPEN));
        return openTiles.get((int) (Math.random() * openTiles.size())).location;
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

    public int numOfDeadEnds() {
        return deadEnds.size();
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
