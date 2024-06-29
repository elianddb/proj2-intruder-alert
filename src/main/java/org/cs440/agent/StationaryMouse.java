package org.cs440.agent;

import org.cs440.agent.Agent.Mortality;

public class StationaryMouse extends Agent implements Mortality {
    private boolean alive = true;

    public StationaryMouse(char identifier) {
        super(identifier);
    }

    @Override
    public boolean kill(int x, int y) {
        if (location.x() == x && location.y() == y) {
            alive = false;
            identifier = '%';
        }

        return !alive;
    }

    @Override
    public boolean alive() {
        return alive;
    }
}
