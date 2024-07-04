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

import tech.tablesaw.api.*;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.ScatterPlot;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;


import org.cs440.agent.Bot;
import org.cs440.agent.StationaryMouse;
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
        //sum = runSimulations(new Bot1RVFactory(), "Bot1RV");
        //logger.info("Bot1RV Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);

        // Run simulations for Bot2RV
        //sum = runSimulations(new Bot2RVFactory(), "Bot2RV");
        //logger.info("Bot2RV Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);
        
        double[] alphaValues = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9}; // Example α values

        double[] avgMovesBot1 = new double[alphaValues.length];
        double[] avgMovesBot2 = new double[alphaValues.length];
        double[] avgMovesBot3 = new double[alphaValues.length];

        for (int i = 0; i < alphaValues.length; i++) {
            double alpha = alphaValues[i];
            avgMovesBot1[i] = simulateBots(new Bot1RVFactory(), alpha);
            logger.info(String.format("Alpha %.2f - Bot1 average moves: %.2f", alpha, avgMovesBot1[i]));

            avgMovesBot2[i] = simulateBots(new Bot2RVFactory(), alpha);
            logger.info(String.format("Alpha %.2f - Bot2 average moves: %.2f", alpha, avgMovesBot2[i]));

            avgMovesBot3[i] = simulateBots(new Bot3Factory(), alpha);
            logger.info(String.format("Alpha %.2f - Bot3 average moves: %.2f", alpha, avgMovesBot3[i]));
        }

        Table results = Table.create("Average Moves vs. Alpha")
                .addColumns(
                        DoubleColumn.create("Alpha", alphaValues),
                        DoubleColumn.create("Avg Moves (Bot1)", avgMovesBot1),
                        DoubleColumn.create("Avg Moves (Bot2)", avgMovesBot2),
                        DoubleColumn.create("Avg Moves (Bot3)", avgMovesBot3)
                );

        plotAverageMoves(results, "Stationary Mouse");
    }
    private static double simulateBots(BotFactory botFactory, double alpha) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < NUM_SIMULATIONS; i++) {
            futures.add(executor.submit(new SimulationTask(botFactory)));
        }

        int sum = futures.stream()
                .mapToInt(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.SEVERE, "Simulation task interrupted or failed", e);
                        return 0;
                    }
                })
                .sum();

        executor.shutdown();

        return sum * 1.0 / NUM_SIMULATIONS;
    }
    private static void plotAverageMoves(Table results, String mouseType) {
        double[] alphaValues = results.doubleColumn("Alpha").asDoubleArray();
        double[] avgMovesBot1 = results.doubleColumn("Avg Moves (Bot1)").asDoubleArray();
        double[] avgMovesBot2 = results.doubleColumn("Avg Moves (Bot2)").asDoubleArray();
        double[] avgMovesBot3 = results.doubleColumn("Avg Moves (Bot3)").asDoubleArray();

        ScatterTrace bot1Trace = ScatterTrace.builder(alphaValues, avgMovesBot1)
                .name("Bot1")
                .mode(ScatterTrace.Mode.LINE)
                .build();

        ScatterTrace bot2Trace = ScatterTrace.builder(alphaValues, avgMovesBot2)
                .name("Bot2")
                .mode(ScatterTrace.Mode.LINE)
                .build();

        ScatterTrace bot3Trace = ScatterTrace.builder(alphaValues, avgMovesBot3)
                .name("Bot3")
                .mode(ScatterTrace.Mode.LINE)
                .build();

        Layout layout = Layout.builder()
                .title("Average Moves vs. Alpha (" + mouseType + ")")
                .xAxis(Axis.builder().title("Alpha").build())
                .yAxis(Axis.builder().title("Average Moves").build())
                .build();

        Figure figure = new Figure(layout, bot1Trace, bot2Trace, bot3Trace);
        Plot.show(figure);
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
            StationaryMouse mouse = new StationaryMouse('M');
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
