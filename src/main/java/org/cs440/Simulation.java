package org.cs440;

import java.util.ArrayList;

import org.cs440.entity.Entity;
import org.cs440.ship.Ship;
import org.cs440.ship.Tile;

public class Simulation {
    private Ship ship;
    private ArrayList<Entity> entities;

    public Simulation(Ship ship) {
        this.ship = ship;
        this.entities = new ArrayList<Entity>();
    }

    public void addEntity(Entity entity) {
        entity.link(ship);
        entities.add(entity);
    }

    public Entity getEntity(int x, int y) {
        for (Entity entity : entities) {
            if (entity.location().x() == x && entity.location().y() == y) {
                return entity;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < ship.height(); y++) {
            for (int x = 0; x < ship.width(); x++) {
                Tile tile = ship.getTile(x, y);
                if (tile.is(Ship.OCCUPIED)) {
                    Entity entity = getEntity(x, y);
                    sb.append(entity.identifier());
                } else {
                    sb.append(tile.identifier());
                }

                sb.append(x < ship.width() - 1 ? ' ' : '\n');
            }
        }

        return sb.toString();
    }
}
