package org.cs440.ship;

public class Tile {
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

    public Location location() {
        // Prevents agents from modifying each others OCCUPIED tiles
        if (is(Status.OCCUPIED)) {
            throw new IllegalStateException("Cannot access location of occupied tile");
        }

        return location;
    }

    public char identifier() {
        return type.identifier;
    }

    public boolean is(Status status) {
        return type.status == status;
    }

    public boolean is(Type type) {
        return this.type == type;
    }
    
    public void set(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%c(%d, %d): %s", type.identifier, location.x, location.y, type.status);
    }

    public enum Status {
        OPEN,
        BLOCKED,
        OCCUPIED
    }

    public static class Type {
        protected final char identifier;
        protected final Status status;

        public Type(char identifier, Status status) {
            this.identifier = identifier;
            this.status = status;
        }
    }

    public static class Location {
        protected int x;
        protected int y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public Location up() {
            return new Location(x, y - 1);
        }

        public Location down() {
            return new Location(x, y + 1);
        }

        public Location left() {
            return new Location(x - 1, y);
        }

        public Location right() {
            return new Location(x + 1, y);
        }

        public Location[] cardinalNeighbors() {
            return new Location[] {up(), down(), left(), right()};
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Location location = (Location) obj;
            return x == location.x && y == location.y;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", x, y);
        }
    }
}
