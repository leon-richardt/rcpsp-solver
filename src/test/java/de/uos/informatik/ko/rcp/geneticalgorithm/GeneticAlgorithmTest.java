package de.uos.informatik.ko.rcp.geneticalgorithm;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Io;

import java.nio.file.Paths;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GeneticAlgorithmTest {

    public static void main (String[] args){
        final String path = args[0];

        final Instance instance = Io.readInstance(Paths.get(path));

        int[] mutter = {1,2,3,4,5,6,7,8,9,10};
        int[] vater = {3,4,6,8,5,9,10,1,2,7};
        int[] crossed = new int[10];
        int[] kind = {1, 4, 5, 2, 9, 10, 15, 11, 16, 3, 7, 13, 26, 21, 6, 18, 20, 25, 8, 27, 28, 31, 19, 29, 12, 14, 17, 22, 23, 24, 30, 32};
        Random random=new Random();
        int popsize = instance.n()*2;
        int[][] pop;
        int[] mutiert;
        int[] reproduziert;

        // Generation-Ersteller testen
        pop = GeneratePop.ReturnArray(GeneratePop.generatePop(instance, (Integer) popsize, random));

        for (int i = 0; i < popsize; i++) {
            for (int j = 0; j < instance.n(); j++) {
                System.out.print(pop[i][j] + " ");
            }
            System.out.println();
        }


        //Crossover testen ----> funktioniert
        /*
        System.out.print("crossed: ");
        crossed = GA.crossover(mutter, vater, random);
        for (int i = 0; i < 10; i++) {
            System.out.print(crossed[i] + " ");
        }

         */


        System.out.println();



        //Mutation testen ---> funktioniert
        /*
        mutiert = GA.mutation(kind, random, instance);
        System.out.print("mutiert: ");
        for (int i = 0; i < instance.n(); i++) {
            System.out.print(mutiert[i] + " ");
        }

         */

        //Reproduktion testen
        reproduziert = GA.reproduktion(pop, instance,random,0.5);
        for (int i = 0; i < instance.n(); i++) {
            System.out.print(reproduziert[i] + " ");
        }

        int[] copy = new int[instance.n()];
        System.out.println("ArrayCopy:");
        System.arraycopy(pop[5], 0, copy, 0, instance.n());
        for (int i = 0; i < instance.n(); i++) {
            System.out.print(copy[i] + " ");
        }
    }
}
