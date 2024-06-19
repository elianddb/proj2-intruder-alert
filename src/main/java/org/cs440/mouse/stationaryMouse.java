package org.cs440.mouse;
import org.cs440.ship.Ship;
import java.util.logging.Level;
import java.util.logging.Logger;


public class stationaryMouse extends Mouse{
    private static final Logger logger = Logger.getLogger(stationaryMouse.class.getName());

    public stationaryMouse(int startX, int startY, Ship ship) {
        super(startX, startY, ship);
    }

}
