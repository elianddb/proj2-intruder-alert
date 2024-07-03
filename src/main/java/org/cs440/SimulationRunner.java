package org.cs440;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cs440.agent.Bot;
import org.cs440.agent.StochasticMouse;
import org.cs440.agent.algorithm.Algorithm;
import org.cs440.agent.algorithm.Bot1RV;
import org.cs440.agent.algorithm.Bot2RV;
import org.cs440.agent.algorithm.Bot3;
import org.cs440.ship.Ship;

public class SimulationRunner {
    private static final Logger logger = Logger.getLogger(SimulationRunner.class.getName());
    private static final int NUM_SIMULATIONS = 1000;
    private static final int THREAD_POOL_SIZE = 30; // Adjust thread pool size based on your CPU cores and load

    public static void main(String[] args) {
        int sum;

        // Run simulations for Bot3
        sum = runSimulations(new Bot3Factory(), "Bot3");
        logger.info("Bot3 Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);

        // Run simulations for Bot1RV
        sum = runSimulations(new Bot1RVFactory(), "Bot1RV");
        logger.info("Bot1RV Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);

        // Run simulations for Bot2RV
        sum = runSimulations(new Bot2RVFactory(), "Bot2RV");
        logger.info("Bot2RV Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);
    }

    private static int runSimulations(BotFactory botFactory, String botName) {
        int sum = 0;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < NUM_SIMULATIONS; i++) {
            futures.add(executor.submit(new SimulationTask(botFactory)));
        }

        for (Future<Integer> future : futures) {
            try {
                sum += future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.SEVERE, "Simulation task interrupted or failed", e);
            }
        }

        executor.shutdown();
        return sum;
    }

    private static class SimulationTask implements Callable<Integer> {
        private final BotFactory botFactory;

        public SimulationTask(BotFactory botFactory) {
            this.botFactory = botFactory;
        }

        @Override
        public Integer call() {
            Ship ship = new Ship(40, 40);
            StochasticMouse mouse = new StochasticMouse('M');
            Bot bot = new Bot('B', mouse, botFactory.createBot(ship));
            Simulation simulation = new Simulation(ship);
            simulation.addAgent(bot);
            simulation.addAgent(mouse);
            simulation.run();
            return simulation.stepsTaken();
        }
    }

    private interface BotFactory {
        Algorithm createBot(Ship ship);
    }

    private static class Bot3Factory implements BotFactory {
        @Override
        public Algorithm createBot(Ship ship) {
            return new Bot3(ship);
        }
    }

    private static class Bot1RVFactory implements BotFactory {
        @Override
        public Algorithm createBot(Ship ship) {
            return new Bot1RV(ship);
        }
    }

    private static class Bot2RVFactory implements BotFactory {
        @Override
        public Algorithm createBot(Ship ship) {
            return new Bot2RV(ship);
        }
    }
}
