package org.cs440.agent;

import org.cs440.App;
import org.cs440.agent.Agent.Capture;

public class Sensor {
    public static double DEFAULT_SENSITIVITY = 0.1;
    
    protected Agent user;
    private Agent[] targets;
    protected double sensitivity; // Inversely proportional

    public Sensor(Agent user, Agent[] targets, double sensitivity) {
        this.user = user;
        this.targets = targets;
        if (sensitivity <= 0) {
            App.logger.warn(
                String.format("Sensitivity must be positive, setting to default (%f)", DEFAULT_SENSITIVITY)
            );
            sensitivity = DEFAULT_SENSITIVITY;
        }
        this.sensitivity = sensitivity;
    }

    public Sensor(Agent user, Agent target, double sensitivity) {
        this.user = user;
        this.targets = new Agent[] { target };
        if (sensitivity <= 0) {
            App.logger.warn(
                String.format("Sensitivity must be positive, setting to default (%f)", DEFAULT_SENSITIVITY)
            );
            sensitivity = DEFAULT_SENSITIVITY;
        }
        this.sensitivity = sensitivity;
    }

    public Sensor(Agent user, Agent target) {
        this(user, target, DEFAULT_SENSITIVITY);
    }

    public boolean beeped() {
        // The nearer the bot is to the target, the more likely it is to sense it (beep)
        // The bot's d-distance from the target is the Manhattan distance between the two
        // The probability of receiving a beep is e^((-alpha)*(d-1))
        //     for some constant alpha > 0, alpha = sensitivity
        // If the bot is immediately adjacent to the target, the probability of receiving a beep is 1
        for (Agent target : targets) {
            if (!(target instanceof Capture) || target.identifier == '@') continue;
            int d = user.getLocation().manhattanDistance(target.getLocation());
            double probability = Math.exp((-sensitivity) * (d - 1));
            App.logger.debug("Probability of sensing target: " + probability);
            if (Math.random() <= probability) {
                return true;
            }
        }
        return false;
    }

    public double getSensitivity() {
        return sensitivity;
    }
}
