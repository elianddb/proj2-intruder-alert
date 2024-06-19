package com.cs440;

public class Tile {
    public enum Type {
        EMPTY,
        BLOCK
    }

    public enum Status {
        OPEN,
        CLOSED
    }

    public class Location {
        public int x;
        public int y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    protected char sprite;
    protected Type type;
    protected Status status;
    protected Location location;

    public Tile(int x, int y, char sprite, Type type, Status status) {
        this.sprite = sprite;
        this.type = type;
        this.status = Status.OPEN;
        this.location = new Location(x, y);
    }
}
