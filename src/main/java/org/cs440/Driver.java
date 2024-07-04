package org.cs440;

import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.math3.stat.StatUtils;
import org.cs440.Log.Level;
import org.cs440.agent.Agent;
import org.cs440.agent.Bot;
import org.cs440.agent.StationaryMouse;
import org.cs440.agent.StochasticMouse;
import org.cs440.agent.algorithm.Algorithm;
import org.cs440.agent.algorithm.Bot1;
import org.cs440.agent.algorithm.Bot1RV;
import org.cs440.agent.algorithm.Bot2RV;
import org.cs440.agent.algorithm.Bot3;
import org.cs440.ship.Ship;

public class Driver {
    private static final Log logger = new Log("Driver");
    private static String input = "";
    
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to the Bot Vs. Mice simulation!");
        System.out.println("This simulation will run a bot algorithm to capture mice based on probability.");
        System.out.println("Type 'exit' to exit this interface at any time.");
        System.out.println("Here are you're following options: ");
        Scanner scanner = new Scanner(System.in);
        while (!input.equals("exit")) {
            System.out.println("1. Run simulation");
            System.out.println("2. Run benchmark");
            System.out.println("exit. Exit");

            if (input.equals("exit")) {
                break;
            }

            input = scanner.nextLine();
            if (input.equals("1")) {
                runSimulation();
                scanner.nextLine();
                continue;
            } else if (input.equals("2")) {
                runBenchmark();
                continue;
            } else if (input.equals("debug")) {
                logger.setLevel(Level.DEBUG);
                logger.debug("Debugging enabled...");
            } else if (!input.equals("exit")) {
                System.out.println("Invalid input. Please try again.");
                continue;
            }
        }
        scanner.close();
    }

    private static void runBenchmark() {
        Scanner scanner = new Scanner(System.in);
        int iterations = 0;
        while (!input.equals("exit")) {
            System.out.println("How many iterations would you like to run per alpha value?");
            System.out.println("Enter the number of iterations: ");
            input = scanner.nextLine().trim();
            if (input.equals("exit")) {
                break;
            }

            try {
                iterations = Integer.parseInt(input);
                if (iterations <= 0) {
                    System.out.println("Invalid input. Please try again.");
                    continue;
                }
                int threadPoolSize = Runtime.getRuntime().availableProcessors();
                logger.info("Running benchmark with " + iterations + " iterations and " + threadPoolSize + " threads");
                SimulationRunner.main(new String[] {String.valueOf(iterations), String.valueOf(threadPoolSize)});
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please try again.");
                continue;
            }
        }
    }

    private static void runSimulation() throws IOException {
        Ship ship = new Ship(40, 40);
        Algorithm algorithm = null;
        Scanner scanner = new Scanner(System.in);
        while (!input.equals("exit")) {
            System.out.println("Which bot algorithm would you like to run?");
            System.out.println("1. Bot1RV");
            System.out.println("2. Bot2RV");
            System.out.println("3. Bot3");

            System.out.println("Enter the number of the bot you would like to run: ");
            input = scanner.nextLine().trim();

            if (input.equals("exit")) {
                break;
            }

            if (input.equals("1")) {
                algorithm = new Bot1RV(ship);
                break;
            } else if (input.equals("2")) {
                algorithm = new Bot2RV(ship);
                break;
            } else if (input.equals("3")) {
                algorithm = new Bot3(ship);
                break;
            } else if (!input.equals("exit")) {
                System.out.println("Invalid input. Please try again.");
                continue;
            }
        }

        int type = 0;
        while (!input.equals("exit")) {
            System.out.println("What type of mice would you like to run?");
            System.out.println("1. Stochastic Mouse");
            System.out.println("2. Stationary Mouse");

            System.out.println("Enter the number of the mice you would like to run: ");
            input = scanner.nextLine().trim();

            if (input.equals("exit")) {
                scanner.close();
                return;
            }

            
            if (input.equals("1")) {
                type = 1;
                break;
            } else if (input.equals("2")) {
                type = 2;
                break;
            } else if (!input.equals("exit")) {
                System.out.println("Invalid input. Please try again.");
                continue;
            }
        }

        while (!input.equals("exit")) {
            System.out.println("How many mice would you like to run?");
            System.out.println("Enter the number of mice you would like to run: ");
            input = scanner.nextLine().trim();
            if (input.equals("exit")) {
                break;
            }

            if (input.equals("1") || input.equals("2")) {
                runParams(ship, algorithm, type, Integer.parseInt(input));
                break;
            } else if (!input.equals("exit")) {
                System.out.println("Invalid input. Please try again.");
                continue;
            }
        }
    }

    public static void runParams(Ship ship, Algorithm algo, int mouseType, int noOfMice) throws IOException {
        logger.info("Running simulation with " + noOfMice + " mice and " + algo.getClass().getSimpleName() + " algorithm");
        Agent[] mice = new Agent[noOfMice];
        for (int i = 0; i < noOfMice; i++) {
            if (mouseType == 1) {
                mice[i] = new StochasticMouse('M');
            } else if (mouseType == 2) {
                mice[i] = new StationaryMouse('M');
            }
        }
        
        Bot bot = new Bot('A', mice, 0.1, algo);
        
        Simulation simulation = new Simulation(ship);
        simulation.addAgent(bot);
        for (Agent mouse : mice) {
            simulation.addAgent(mouse);
        }
        
        if (logger.is(Level.DEBUG)) { // Delay for human to read initial state logs
            logger.debug("Debugging is enabled...");
            logger.debug("Press Enter to start simulation...");
            System.in.read();
        }

        logger.info("Starting simulation...");
        simulation.run(45); // Exclude delay to run without drawing frames
        //logger.debug("\n" + simulation.toString());

        logger.info("Simulation completed in " + simulation.stepsTaken() + " steps");
        System.out.println("Press Enter to exit...");
        System.in.read();

        logger.writeTo("Driver");
        logger.info("Log messages written to file: Driver.log");
    }
}
