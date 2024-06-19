package org.cs440.ship;

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

    public Tile getTile(int x, int y) {
        enforceBounds(x, y);
        return tiles[y][x];
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
