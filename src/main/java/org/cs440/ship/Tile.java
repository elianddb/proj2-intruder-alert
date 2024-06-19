package org.cs440.ship;

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

    public Tile(Type type, int x, int y) {
        this(type, new Location(x, y));
    }

    public Tile(char identifier, Status status, int x, int y) {
        this(new Type(identifier, status), new Location(x, y));
    }

    public boolean is(Status status) {
        return type.status == status;
    }

    public boolean is(Type type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        return String.format("%c(%d, %d): %s", type.identifier, location.x, location.y, type.status);
    }
}
