package de.uos.informatik.ko.rcp.geneticAlgo;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Io;

import java.nio.file.Paths;
import java.util.Random;

public class GAmain {

    public static void main(String[] args){
        if (args.length != 1) {
            System.out.println("Wrong usage");
            return;
        }

        final String path = args[0];

        final Instance instance = Io.readInstance(Paths.get(path));
        
        EarliestStartScheduleGenerator EssGen = new EarliestStartScheduleGenerator(instance);


        for (int i = 0; i < instance.r(); i++) {
            System.out.printf("res %d has %d available units\n", i, instance.resources[i]);
        }

        //TODO richtigen Random vom Nutzer nutzen
        Random random = new Random();
        int[] solution = new int[instance.n()];

        System.out.println();

        int[] reihenfolge = new int[instance.n()];

        // TODO Deep Copy nötig? Eigentlich nicht, oder?
        reihenfolge = GA.geneticAlgorithm(instance, random);





        for (int i = 0; i < reihenfolge.length; i++) {
            System.out.println(reihenfolge[i]);
        }

        solution = EssGen.generateSchedule(reihenfolge);

        System.out.println("Makespan" + solution[solution.length-1]);



        

        Io.writeSolution(solution, Paths.get("sol2.txt"));

        System.out.println("ok");

    }

}
