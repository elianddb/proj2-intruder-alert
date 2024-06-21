package org.cs440.entity;

public interface Movement {
    public static enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        public final int dx;
        public final int dy;

        private Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    public void move(Direction direction);
}
