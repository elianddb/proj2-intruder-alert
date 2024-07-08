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

import org.cs440.agent.Agent;
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
    private static int numSimulations = 100;
    private static int threadPoolSize = 16; // Adjust thread pool size based on your CPU cores and load

    public enum MouseType {
        STOCHASTIC(1, "Stochastic Mouse"),
        STATIONARY(2, "Stationary Mouse");

        private final int type;
        private final String description;

        MouseType(int type, String description) {
            this.type = type;
            this.description = description;
        }
    }

    public static void main(String[] args) {
        int mouseType = 1;
        int noOfMice = 1;
        if (args.length >= 2) {
            numSimulations = Integer.parseInt(args[0]);
            threadPoolSize = Integer.parseInt(args[1]);
            mouseType = Integer.parseInt(args[2]);
            noOfMice = Integer.parseInt(args[3]);
        }
        // int sum;

        // Run simulations for Bot3
        // sum = runSimulations(new Bot3Factory(), "Bot3");
        // logger.info("Bot3 Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);

        // Run simulations for Bot1RV
        // sum = runSimulations(new Bot1RVFactory(), "Bot1RV");
        // logger.info("Bot1RV Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);

        // Run simulations for Bot2RV
        // sum = runSimulations(new Bot2RVFactory(), "Bot2RV");
        // logger.info("Bot2RV Average steps taken: " + sum * 1.0 / NUM_SIMULATIONS);

        double[] alphaValues = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 }; // Example α values

        double[] avgMovesBot1 = new double[alphaValues.length];
        double[] avgMovesBot2 = new double[alphaValues.length];
        double[] avgMovesBot3 = new double[alphaValues.length];

        for (int i = 0; i < alphaValues.length; i++) {
            double alpha = alphaValues[i];
            avgMovesBot1[i] = simulateBots(new Bot1RVFactory(), alpha, mouseType, noOfMice);
            logger.info(String.format("Alpha %.2f - Bot1 average moves: %.2f", alpha, avgMovesBot1[i]));

            avgMovesBot2[i] = simulateBots(new Bot2RVFactory(), alpha, mouseType, noOfMice);
            logger.info(String.format("Alpha %.2f - Bot2 average moves: %.2f", alpha, avgMovesBot2[i]));

            avgMovesBot3[i] = simulateBots(new Bot3Factory(), alpha, mouseType, noOfMice);
            logger.info(String.format("Alpha %.2f - Bot3 average moves: %.2f", alpha, avgMovesBot3[i]));
        }

        Table results = Table.create("Average Moves vs. Alpha")
                .addColumns(
                        DoubleColumn.create("Alpha", alphaValues),
                        DoubleColumn.create("Avg Moves (Bot1)", avgMovesBot1),
                        DoubleColumn.create("Avg Moves (Bot2)", avgMovesBot2),
                        DoubleColumn.create("Avg Moves (Bot3)", avgMovesBot3));

        plotAverageMoves(results, numSimulations + " iterations : " + MouseType.values()[mouseType-1].description + " [" + noOfMice + " mice]");

        Bot1RVFactory.count = 0;
        Bot2RVFactory.count = 0;
        Bot3Factory.count = 0;
    }

    private static double simulateBots(AlgorithmFactory botFactory, double alpha, int mouseType, int noOfMice) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < numSimulations; i++) {
            futures.add(executor.submit(new SimulationTask(botFactory, alpha)));
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

        return sum * 1.0 / numSimulations;
    }

    public static double simulateBots(AlgorithmFactory botFactory, double alpha) {
        return simulateBots(botFactory, alpha, 2, 1);
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
        return;
    }

    private static class SimulationTask implements Callable<Integer> {
        private final AlgorithmFactory algoFactory;
        private final double alpha;
        private final int botType;
        private final int noOfMice;

        public SimulationTask(AlgorithmFactory algoFactory, double alpha, int botType, int noOfMice) {
            this.algoFactory = algoFactory;
            this.alpha = alpha;
            this.botType = botType;
            this.noOfMice = noOfMice;
        }

        public SimulationTask(AlgorithmFactory algoFactory, double alpha) {
            this(algoFactory, alpha, 2, 1);
        }

        @Override
        public Integer call() {
            Ship ship = new Ship(40, 40);
            // StochasticMouse mouse = new StochasticMouse('M');
            Agent[] mice = new Agent[noOfMice];
            for (int i = 0; i < noOfMice; i++) {
                if (botType == 1) {
                    mice[i] = new StochasticMouse('M');
                } else if (botType == 2) {
                    mice[i] = new StationaryMouse('M');
                }
            }

            Bot bot = new Bot('B', mice, alpha, algoFactory.createAlgorithm(ship));
            Simulation simulation = new Simulation(ship);
            simulation.addAgent(bot);
            for (Agent mouse : mice) {
                simulation.addAgent(mouse);
            }

            simulation.run();
            return simulation.stepsTaken();
        }

        // public Integer call(Algorithm algorithm, double alpha, int mouseType, int noOfMice) {
        //     Ship ship = new Ship(40, 40);
        //     Agent[] mice = new Agent[noOfMice];
        //     for (int i = 0; i < noOfMice; i++) {
        //         if (mouseType == 1) {
        //             mice[i] = new StochasticMouse('M');
        //         } else if (mouseType == 2) {
        //             mice[i] = new StationaryMouse('M');
        //         }
        //     }

        //     Bot bot = new Bot('A', mice, alpha, algorithm);

        //     Simulation simulation = new Simulation(ship);
        //     simulation.addAgent(bot);
        //     for (Agent mouse : mice) {
        //         simulation.addAgent(mouse);
        //     }

        //     simulation.run();
        //     return simulation.stepsTaken();
        // }
    }

    private interface AlgorithmFactory {
        Algorithm createAlgorithm(Ship ship);
    }

    private static class Bot3Factory implements AlgorithmFactory {
        public static int count = 0;

        @Override
        public Algorithm createAlgorithm(Ship ship) {
            System.out.println("Bot3Factory count: " + count++);
            return new Bot3(ship);
        }
    }

    private static class Bot1RVFactory implements AlgorithmFactory {
        public static int count = 0;

        @Override
        public Algorithm createAlgorithm(Ship ship) {
            System.out.println("Bot1RVFactory count: " + count++);
            return new Bot1RV(ship);
        }
    }

    private static class Bot2RVFactory implements AlgorithmFactory {
        public static int count = 0;

        @Override
        public Algorithm createAlgorithm(Ship ship) {
            System.out.println("Bot2RVFactory count: " + count++);
            return new Bot2RV(ship);
        }
    }
}
