package com.cs440;

public class Tile {
    public enum Type {
        EMPTY,
        WALL
    }
    
    protected char sprite;
    protected Type type;
    protected int x;
    protected int y;

    public Tile(int x, int y, char sprite, Type type) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
        this.type = type;
    }

    public Tile(int x, int y) {
        this(x, y, ' ', Type.EMPTY);
    }
}
