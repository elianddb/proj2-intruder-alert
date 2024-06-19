package com.cs440;

public class Tile {
    public enum Status {
        OPEN,
        BLOCKED,
        OCCUPIED
    }

    public static class Type {
        protected char identifier;
        protected Status status;

        public Type(char identifier, Status status) {
            this.identifier = identifier;
            this.status = status;
        }
    }

    public static class Location {
        public int x;
        public int y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    protected Type type;
    protected Location location;

    public Tile(Type type, Location location) {
        this.type = type;
        this.location = location;
    }

    public Tile(char identifier, Status status, int x, int y) {
        this(new Type(identifier, status), new Location(x, y));
    }
}
