package org.cs440.ship;

public class Ship {
    public static final Tile.Type BLOCK = new Tile.Type('X', Tile.Status.BLOCKED);
    public static final Tile.Type OPEN = new Tile.Type('.', Tile.Status.OPEN);
    public static final Tile.Type OCCUPIED = new Tile.Type('O', Tile.Status.OCCUPIED);

    protected Tile[][] tiles;

    public Ship(int width, int height) {
        tiles = new Tile[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(BLOCK, x, y);
            }
        }
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

    public void enforceOwnership(int x, int y) {
        Tile tile = getTile(x, y);
        if (tile.is(OCCUPIED)) {
            throw new IllegalArgumentException(String.format("Tile already occupied %s", tile));
        }
    }

    public Tile getTile(int x, int y) {
        enforceBounds(x, y);
        return tiles[y][x];
    }

    public void setTile(int x, int y, Tile.Type type) {
        enforceBounds(x, y);
        enforceOwnership(x, y);
        tiles[y][x].set(type);
    }

    public void openTile(int x, int y) {
        setTile(x, y, OPEN);
    }

    public void blockTile(int x, int y) {
        setTile(x, y, BLOCK);
    }

    public void occupyTile(int x, int y) {
        setTile(x, y, OCCUPIED);
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
