package org.cs440.agent;

import org.cs440.App;

public class Sensor {
    protected Agent user;
    protected Agent target;
    protected double sensitivity; // Inversely proportional

    public Sensor(Agent user, Agent target, double sensitivity) {
        this.user = user;
        this.target = target;
        if (sensitivity < 0) {
            App.logger.warn("Sensitivity must be positive, setting to default value");
            sensitivity = 0.1;
        }
        this.sensitivity = sensitivity;
    }

    public Sensor(Agent user, Agent target, int sensitivity) {
        this(user, target, (double) sensitivity);
    }

    public Sensor(Agent user, Agent target) {
        this(user, target, 0.1);
    }

    public boolean sense() {
        // The nearer the bot is to the target, the more likely it is to sense it (beep)
        // The bot's d-distnace from the target is the Manhattan distance between the two
        // The probability of receiving a beep is e^((-alpha)*(d-1))
        //     for some constant alpha > 0, alpha = sensitivity
        // If the bot is immediately adjacent to the target, the probability of receiving a beep is 1

        int d = manhattanDistance();
        double probability = Math.exp((-sensitivity) * (d - 1));
        App.logger.debug("Probability of sensing target: " + probability);
        return Math.random() < probability;
    }

    public int manhattanDistance() {
        return Math.abs(user.location.x() - target.location.x()) + Math.abs(user.location.y() - target.location.y());
    }
}
