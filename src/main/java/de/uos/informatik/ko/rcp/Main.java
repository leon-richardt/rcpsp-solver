package de.uos.informatik.ko.rcp;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Io;
import de.uos.informatik.ko.rcp.Utils;

import de.uos.informatik.ko.rcp.generators.EarliestStartScheduleGenerator;
import de.uos.informatik.ko.rcp.geneticalgorithm.GeneticAlgorithm;
import de.uos.informatik.ko.rcp.geneticalgorithm.GeneratePop;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Main {

    public static void main(String[] args){
        if (args.length != 1) {
            System.out.println("Wrong usage");
            return;
        }

        final String path = args[0];

        final Instance instance = Io.readInstance(Paths.get(path));

        var essGen = new EarliestStartScheduleGenerator(instance);

        for (int i = 0; i < instance.r(); i++) {
            System.out.printf("res %d has %d available units\n", i, instance.resources[i]);
        }

        //TODO richtigen Random vom Nutzer nutzen
        Random random = new Random();

        int[] solution = new int[instance.n()];

        System.out.println();

        solution = GeneticAlgorithm.geneticAlgorithm(instance, random);

        System.out.println("Makespan: " + solution[solution.length-1]);

        Io.writeSolution(solution, Paths.get("solGA.txt"));

        System.out.println("Solution:");
        for (int i = 0; i < solution.length; ++i) {
            System.out.println("Activity " + (i + 1) + ": " + solution[i]);
        }

        final boolean admissible = Utils.checkAdmissibility(instance, solution);
        System.out.println("Admissible? " + admissible);
    }

}
