package de.uos.informatik.ko.rcp.geneticalgorithm;

import de.uos.informatik.ko.rcp.Instance;

import java.util.Random;

/**
 * @author Manedikte
 */
public class GeneticAlgorithm {

    public static int[] geneticAlgorithm(Instance instance, Random random) {

        // TODO ------------------------------- richtige Abbruchbedingung einfügen--------------------------------------

        int counter = 0;
        int popsize = instance.n()*2;
        //optimaler Schedule
        int[] optimum = new int[instance.n()];

        // In der Population gespeichert sind Reihenfolgen (in der zweiten Dimension)
        int[][] pop = new int[popsize][instance.n()];

        int[] zuwachs = new int[instance.n()];
        int[] schedule = new int[instance.n()];
        int dauer;
        double mutationswkeit;
        int sterbeplatz = -1;
        EarliestStartScheduleGenerator EssGen = new EarliestStartScheduleGenerator(instance);

        // bestimme Wkeit (Wert zwischen 0 und 1) dass eine Mutation auftritt
        mutationswkeit = random.nextDouble();

        // Population erstellen
        pop = GeneratePop.ReturnArray(GeneratePop.generatePop(instance, (Integer) popsize, random));


        while(counter < 100){
            // Kinderzeugung inkl. turnierbasierter Elternauswahl, Crossover und Mutation
            zuwachs = reproduktion(pop, instance, random, mutationswkeit);

            // aktualisiere Optimum, falls nötig
            schedule = EssGen.generateSchedule(zuwachs);
            dauer = schedule[schedule.length-1];
            if(dauer < optimum[optimum.length-1]){
                System.arraycopy(schedule, 0, optimum, 0, optimum.length);
            }

            // Füge das neu erzeugte Kind der Population hinzu (an einer zufälligen Stelle)
            sterbeplatz = random.nextInt(popsize);
            System.arraycopy(zuwachs, 0, pop[sterbeplatz], 0, zuwachs.length);

            counter++;
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
    public static int[] reproduktion(int[][] pop, Instance instance, Random random, double mutationswkeit){

        int[] kind = new int[instance.n()];
        int[] mutter = new int[instance.n()];
        int[] vater = new int[instance.n()];
        int besteMutter = Integer.MAX_VALUE;
        int besterVater = Integer.MAX_VALUE;
        int dummyZeit = 0;

        EarliestStartScheduleGenerator EssGen = new EarliestStartScheduleGenerator(instance);
        // suche unter (zwei mal) drei zufällig ausgewählten Reihenfolgen aus der Population die beste(n)
        for (int i = 0; i < 2; i++) {
            int mPos = random.nextInt(pop.length);
            int vPos = random.nextInt(pop.length);

            // finde Mutter
            dummyZeit = EssGen.generateSchedule(pop[mPos])[instance.n() - 1];
            if (dummyZeit < besteMutter) {
                System.arraycopy(pop[mPos], 0, mutter, 0, instance.n());
                besteMutter = dummyZeit;
            }
            // finde Vater
            dummyZeit = EssGen.generateSchedule(pop[vPos])[instance.n() - 1];
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
        int point = random.nextInt(mutter.length);
        int grenze = point;
        boolean gefunden = false;

        // fülle Kind bis zur gewünschten Position mit Einträgen der Mutter
        System.arraycopy(mutter, 0, kind, 0, point);
        // gehe durch Vater und schaue in jedem Eintrag, ob er schon durch die Mutter im Kind enthalten ist
        for (int k = 0; k < vater.length; k++) {
            for (int j = 0; j < grenze; j++) {
                // wenn betrachteter Eintrag schon in Kind enthalten, muss dieser nicht weiter betrachtet werden
                if (vater[k] == kind[j]) {
                    gefunden = true;
                    break;
                }
            }
            // wenn betrachteter Eintrag noch nicht in Kind enthalten, füge diesen an passender Stelle zu Kind hinzu
            if (!gefunden) {
                kind[point] = vater[k];
                //wenn wir noch nicht am Ende des Kindes sind, inkrementieren wir point
                if(point != kind.length - 1){
                    point++;
                //wenn wir dann schon die letzte Stelle im Kind gefüllt haben,
                //gehen wir aus der Schleife raus
                } else{
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
        boolean rechts = true;
        boolean links = true;
        int dummy = -1;
        // Wähle zufällig eine Position in der Reihenfolge
        int pos = random.nextInt(kind.length);
        System.out.println(pos);
        // Die Dummy-Aktivitäten müssen an ihren Stellen bleiben, wenn sie gewählt werden muss nichts überprüft werden
        if(pos != 0 && pos != kind.length-1){
            //Falls der rechte Nachbar von der Position die Aktivität an der Position als Vorgänger hat,
            // dürfen sie nicht getauscht werden
            if(pos == kind.length -2){
                //nix
            } else {
                for (int i = 0; i < instance.successors[kind[pos + 1]].length; i++) {
                    if (instance.successors[kind[pos + 1]][i] == kind[pos]) {
                        rechts = false;
                        break;
                    }
                }
            }
            // Wenn rechts nicht möglich: Überprüfe links
            if (!rechts){
                for (int i = 0; i < instance.successors[kind[pos]].length; i++) {
                    if(instance.successors[kind[pos]][i] == kind[pos-1]){
                        links = false;
                        break;
                    }
                }
            // Wenn rechts möglich: tausche die beiden Jobs im Kind
            } else{
                dummy = kind[pos];
                kind[pos] = kind[pos+1];
                kind[pos+1] = dummy;
            }
            //Falls rechts nicht möglich war, aber der linke Tausch möglich ist, führe ihn durch
            if(!rechts && links){
                dummy = kind[pos];
                kind[pos] = kind[pos-1];
                kind[pos-1] = dummy;
            }
        }
        return kind;
    }
}
