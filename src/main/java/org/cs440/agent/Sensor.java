package org.cs440.agent;

public class Sensor {
    protected Agent user;
    protected Agent target;

    public Sensor(Agent user, Agent target) {
        this.user = user;
        this.target = target;
    }

    
}
