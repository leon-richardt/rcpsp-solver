package de.uos.informatik.ko.rcp.geneticalgorithm;

import de.uos.informatik.ko.rcp.Config;
import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.generators.EarliestStartScheduleGenerator;

import java.util.Random;
import java.util.LinkedHashMap;

/**
 * @author Manedikte
 */
public class GeneticAlgorithm {

    public static int[] geneticAlgorithm(Instance instance, Random random, long timeLimit) {
        int popsize = instance.n()*2;
        //optimaler Schedule
        int[] optimum = new int[instance.n()];
        optimum[optimum.length - 1] = Integer.MAX_VALUE;

        // In der Population gespeichert sind Reihenfolgen (in der zweiten Dimension)
        int[][] pop = new int[popsize][instance.n()];
        int anzahl_iterationen =0;
        int[] zuwachs = new int[instance.n()];
        int[] schedule = new int[instance.n()];
        int[] aktuell = new int[instance.n()];
        int dauer;
        int sterbeplatz = -1;
        EarliestStartScheduleGenerator essGen = new EarliestStartScheduleGenerator(instance);

        // Only for debug purposes
        var updateDeltas = new LinkedHashMap<Long, Integer>(); // <update delta, new makespan>

        // bestimme Wkeit (Wert zwischen 0 und 1) dass eine Mutation auftritt
        final double mutationswkeit = Config.instance().mutationProbability;
        System.out.println("Mutationswkeit: " + mutationswkeit);

        // Population erstellen
        pop = GeneratePop.ReturnArray(GeneratePop.generatePop(instance, (Integer) popsize, random));

        final long timeout = 1_000_000_000L * (timeLimit - 1);  // in nanoseconds + one second buffer
        final long startTime = System.nanoTime();

        // Durch die 1. Generation gehen, die jeweiligen Makespans berechnen und daraus vorläufig
        // optimalen Schedule bestimmen
        for (int i = 0; i < popsize && (System.nanoTime() - startTime < timeout); ++i) {
            aktuell = pop[i];
            schedule = essGen.generateSchedule(aktuell);
            dauer = schedule[schedule.length-1];

            if (dauer < optimum[optimum.length - 1]) {
                final long updateTime = System.nanoTime();
                updateDeltas.put(updateTime - startTime, dauer);
                System.arraycopy(schedule, 0, optimum, 0, optimum.length);
            }
        }
        anzahl_iterationen++;

        while (System.nanoTime() - startTime < timeout) {
            anzahl_iterationen++;
            // Kinderzeugung inkl. turnierbasierter Elternauswahl, Crossover und Mutation
            zuwachs = reproduktion(pop, instance, random, essGen, mutationswkeit);

            // aktualisiere Optimum, falls nötig
            schedule = essGen.generateSchedule(zuwachs);
            dauer = schedule[schedule.length-1];
            if (dauer < optimum[optimum.length-1]){
                final long updateTime = System.nanoTime();
                updateDeltas.put(updateTime - startTime, dauer);
                System.arraycopy(schedule, 0, optimum, 0, optimum.length);
            }

            // Füge das neu erzeugte Kind der Population hinzu (an einer zufälligen Stelle)
            sterbeplatz = random.nextInt(popsize);
            System.arraycopy(zuwachs, 0, pop[sterbeplatz], 0, zuwachs.length);
        }

        if (Config.instance().shouldLog) {
            for (var entry : updateDeltas.entrySet()) {
                System.out.println("time: " + entry.getKey() + " " + entry.getValue());
            }

            System.out.println("iterations: " + anzahl_iterationen);
        }

        return optimum;
    }


    /**
     * Finde zwei Eltern-Reihenfolgen, die dann durch Crossover und ggf. Mutation eine Kind-Lösung bilden
     * @param pop Gesamtpopulation
     * @param instance die übergebene Instanz
     * @param random der vom Nutzer eingegebene Java.Random
     * @param mutationswkeit nur mit einer gewissen Wkeit wird mutiert
     * @return ein Kind (Reihenfolge[])von zwei turnierbasiert ausgewählten Eltern
     */
    public static int[] reproduktion(int[][] pop, Instance instance, Random random,
                                     EarliestStartScheduleGenerator gen, double mutationswkeit) {

        int[] kind = new int[instance.n()];
        int[] mutter = new int[instance.n()];
        int[] vater = new int[instance.n()];
        int besteMutter = Integer.MAX_VALUE;
        int besterVater = Integer.MAX_VALUE;
        int dummyZeit = 0;

        // suche unter (zwei mal) drei zufällig ausgewählten Reihenfolgen aus der Population die beste(n)
        for (int i = 0; i < 2; i++) {
            int mPos = random.nextInt(pop.length);
            int vPos = random.nextInt(pop.length);

            // finde Mutter
            dummyZeit = gen.generateSchedule(pop[mPos])[instance.n() - 1];
            if (dummyZeit < besteMutter) {
                System.arraycopy(pop[mPos], 0, mutter, 0, instance.n());
                besteMutter = dummyZeit;
            }

            // finde Vater
            dummyZeit = gen.generateSchedule(pop[vPos])[instance.n() - 1];
            if (dummyZeit < besterVater) {
                System.arraycopy(pop[vPos], 0, vater, 0, instance.n());
                besterVater = dummyZeit;
            }
        }

        // ONS Mutter und Vater
        System.arraycopy(crossover(mutter, vater, random), 0, kind, 0, instance.n());

        // bestimme zufällig, ob gerade (in dieser Iteration) mutiert werden soll
        if(random.nextDouble() <= mutationswkeit){
            System.arraycopy(mutation(kind, random, instance),0, kind, 0, instance.n());
        }
        return kind;
    }

    /**
     * Führt den One-Point-Crossover durch
     * @param mutter Elternteil Nr.1
     * @param vater Elternteil Nr. 2
     * @param random Ein Zufallsgenerator
     * @return Eine Reihenfolge, die weiterhin vorrangsbeziehungsverträglich ist
     */
    public static int[] crossover(int[] mutter, int[] vater, Random random){
        int[] kind = new int[mutter.length];
        int point = random.nextInt((mutter.length)-2); //Der Fall in dem die Mutter komplett kopiert wird, bringt keinen Nutzen
        int grenze = point;
        boolean gefunden = false;

        // fülle Kind bis zur gewünschten Position mit Einträgen der Mutter
        System.arraycopy(mutter, 0, kind, 0, point);
        // gehe durch Vater und schaue in jedem Eintrag, ob er schon durch die Mutter im Kind enthalten ist
        for (int i : vater) {
            for (int j = 0; j < grenze; j++) {
                // wenn betrachteter Eintrag schon in Kind enthalten, muss dieser nicht weiter betrachtet werden
                if (i == kind[j]) {
                    gefunden = true;
                    break;
                }
            }
            // wenn betrachteter Eintrag noch nicht in Kind enthalten, füge diesen an passender Stelle zu Kind hinzu
            if (!gefunden) {
                kind[point] = i;
                //wenn wir noch nicht am Ende des Kindes sind, inkrementieren wir point
                if (point != kind.length - 1) {
                    point++;
                    //wenn wir dann schon die letzte Stelle im Kind gefüllt haben,
                    //gehen wir aus der Schleife raus
                } else {
                    break;
                }
            }
            gefunden = false;
        }
        return kind;
    }

    /**
     *
     * @param kind Eine Liste von Aktivitäten, vorrangbeziehungsverträglich
     * @param random Ein Zufallsgenerator
     * @param instance Die betrachtete Instanz, um die Vorrangsbeziehugen bei der Mutation nicht zu verletzen
     * @return Ein mutiertes Kind, falls die zufällig gewählte Position einen Tausch mit einem Nachbarn zulässt, d.h.
     *          die Reihenfolge dann vorrangsbeziehungsverträglich bleibt
     */
     public static int[] mutation(int[] kind, Random random, Instance instance){
        int pos = random.nextInt(instance.n()-2)+1; //[1,Anzahl richtiger Aktivitäten]
        boolean rechtsOk = true;
        boolean linksOk = true;
        int dummy;
        if(pos != instance.n()-2){
            //hat kind[pos] den Job bei kind[pos+1] als Nachfolger? Dann geht kein tausch nach rechts
            for (int number : instance.successors[kind[pos]-1]) {
                if (number == kind[pos + 1]) {
                    rechtsOk = false;
                    break;
                }
            }
            // wenn die Aktivität aber nicht unter den nachfolgern gefunden wurde, können wir tauschen
            if (rechtsOk) {
                dummy = kind[pos + 1];
                kind[pos + 1] = kind[pos];
                kind[pos] = dummy;
                return kind;
            }
        }
        // Falls dieser Tausch nicht ging: gucke nach links
        if(pos == 1){
            return kind;
        }
        //mit dem selben Verfahren wie beim Tausch nach rechts
        for (int number: instance.successors[kind[pos-1]-1]) {
            if(number == kind[pos]){
                linksOk = false;
                break;
            }
        }
        if(!linksOk){
            return kind;
        }
        dummy = kind[pos];
        kind[pos]=kind[pos-1];
        kind[pos-1]=dummy;

        return kind;
    }
}
