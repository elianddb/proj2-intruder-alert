package com.cs440;

public class Ship {
    protected Tile[][] tiles;

    public Ship(int width, int height) {
        tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(x, y, 'x', Tile.Type.BLOCK, Tile.Status.CLOSED);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < tiles[0].length; y++) {
            for (int x = 0; x < tiles.length; x++) {
                sb.append(tiles[x][y].sprite + " ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
