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
import java.util.Random;

public class Main {

    public static void main(String[] arguments){
        Args args = null;

        try {
            args = new Args(arguments);
        } catch (IllegalArgumentException e) {
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

        Io.writeSolution(solution, args.solutionPath);
    }

    private static class Args {
        public final Path instancePath;
        public final Path solutionPath;
        public final long timeLimit;
        public final long seed;

        public Args(String[] args) {
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
    }
}
