package org.cs440.agent;

import org.cs440.agent.Agent.Capture;

public class StationaryMouse extends Agent implements Capture {
    private boolean free = true;

    public StationaryMouse(char identifier) {
        super(identifier);
    }

    @Override
    public boolean capture(int x, int y) {
        if (location.x() == x && location.y() == y) {
            free = false;
            identifier = '@';
        }

        return !free;
    }

    @Override
    public boolean isFree() {
        return free;
    }
}
