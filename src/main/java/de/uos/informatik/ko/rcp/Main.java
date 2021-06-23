package de.uos.informatik.ko.rcp;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Io;
import de.uos.informatik.ko.rcp.Utils;

import de.uos.informatik.ko.rcp.generators.EarliestStartScheduleGenerator;
import de.uos.informatik.ko.rcp.geneticalgorithm.GeneticAlgorithm;
import de.uos.informatik.ko.rcp.geneticalgorithm.GeneratePop;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Random;

public class Main {

    public static void main(String[] arguments){
        Args args = null;

        try {
            args = Args.parseArgs(arguments);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println("Usage: java -jar rcp-solver-0.1.0.jar <instance-path> "
                             + "<solution-path> <time-limit> <seed>");
            System.exit(1);
        }

        final Instance instance = Io.readInstance(args.instancePath);
        Random random = new Random(args.seed);

        int[] solution = GeneticAlgorithm.geneticAlgorithm(instance, random, args.timeLimit);

        final int makespan = solution[solution.length - 1];

        if (makespan == Integer.MAX_VALUE) {
            System.err.println("Time limit too strict, could not even generate an initial "
                             + "schedule. Not writing a solution.");
            System.exit(2);
        }

        System.out.println("Makespan: " + makespan);

        final var isAdmissible = Utils.checkAdmissibility(instance, solution);
        if (!isAdmissible) {
            System.err.println("Solution is not admissible!");
            System.exit(2);
        }

        Io.writeSolution(solution, args.solutionPath);
    }

    private static class Args {
        public final Path instancePath;
        public final Path solutionPath;
        public final long timeLimit;
        public final long seed;

        private Args(String[] args) {
            if (args.length != 4) {
                throw new IllegalArgumentException("Must provide four arguments.");
            }

            try {
                this.instancePath = Paths.get(args[0]);
                this.solutionPath = Paths.get(args[1]);
            } catch (InvalidPathException e) {
                throw new IllegalArgumentException("Cannot convert \"" + e.getInput() + "\" to a "
                                                 + "path");
            }

            try {
                this.timeLimit = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse \"" + args[2] + "\" as a long");
            }

            try {
                this.seed = Long.parseLong(args[3]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse \"" + args[3] + "\" as a long");
            }
        }

        public static Args parseArgs(String[] args) {
            // ---- The first 4 args must be provided
            var fixedArgs = new Args(Arrays.copyOfRange(args, 0, 4));

            // ---- Optional flags _may_ be provided
            var crossover = Config.Crossover.TWO_POINT;
            double mutationProbability = 0.4;
            int noImprovementThreshold = 10;
            var parentSelection = Config.ParentSelection.RANDOM_SIZE;
            var cacheMakespans = true;
            var shouldLog = false;

            var flags = Arrays.stream(args).skip(4).iterator();
            while (flags.hasNext()) {
                var flag = flags.next();
                var key_val = flag.split("=");
                var key = key_val[0];
                var val = key_val[1];

                switch (key) {
                    case "--crossover": {
                        if (val.equals("one-point")) {
                            crossover = Config.Crossover.ONE_POINT;
                        } else if (val.equals("two-point")) {
                            crossover = Config.Crossover.TWO_POINT;
                        } else {
                            System.err.println("Unknown crossover method \"" + val + "\"");
                            System.exit(1);
                        }
                    }
                    break;
                    case "--mutation-probability": {
                        try {
                            mutationProbability = Double.parseDouble(val);
                        } catch (NumberFormatException e) {
                            System.err.println("Cannot parse double from \"" + val + "\"");
                            System.exit(1);
                        }

                        if (mutationProbability < 0 || mutationProbability > 1) {
                            System.err.println("Mutation probability must be between 0 and 1");
                            System.exit(1);
                        }
                    }
                    break;
                    case "--no-improvement-threshold": {
                        try {
                            noImprovementThreshold = Integer.parseInt(val);
                        } catch (NumberFormatException e) {
                            System.err.println("Cannot parse int from \"" + val + "\"");
                            System.exit(1);
                        }

                        if (noImprovementThreshold < 0) {
                            System.err.println("Threshold for no improvements cannot be negative");
                            System.exit(1);
                        }
                    }
                    break;
                    case "--parent-selection": {
                        if (val.equals("fixed-size")) {
                            parentSelection = Config.ParentSelection.FIXED_SIZE;
                        } else if (val.equals("random-size")) {
                            parentSelection = Config.ParentSelection.RANDOM_SIZE;
                        } else {
                            System.err.println("Unknown parent selection method \"" + val + "\"");
                            System.exit(1);
                        }
                    }
                    break;
                    case "--cache-makespans": {
                        if (val.equals("true")) {
                            cacheMakespans = true;
                        } else if (val.equals("false")) {
                            cacheMakespans = false;
                        } else {
                            System.err.println("Value of cache-makespans must be true or false, " +
                                               "not \"" + val + "\"");
                            System.exit(1);
                        }
                    }
                    break;
                    case "--should-log": {
                        if (val.equals("true")) {
                            shouldLog = true;
                        } else if (val.equals("false")) {
                            shouldLog = false;
                        } else {
                            System.err.println("Value of should-log must be true or false, not " +
                                               "\"" + val + "\"");
                            System.exit(1);
                        }
                    }
                    break;
                    default: {
                        System.err.println("Unknown flag \"" + flag + "\"");
                        System.exit(1);
                    }
                    break;
                }
            }

            Config.init(crossover, mutationProbability, noImprovementThreshold, parentSelection,
                        cacheMakespans, shouldLog);

            return fixedArgs;
        }
    }
}
