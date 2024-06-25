package org.cs440.agent;

import org.cs440.agent.Agent.Target;

public class StationaryMouse extends Agent implements Target {
    private boolean alive = true;

    public StationaryMouse(char identifier) {
        super(identifier);
    }

    @Override
    public void interact(Interaction interaction) {
        switch (interaction) {
            case KILL:
                alive = false;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean alive() {
        return alive;
    }
}
