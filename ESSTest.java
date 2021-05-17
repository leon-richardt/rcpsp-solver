package de.uos.informatik.ko.rcp.example;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Io;

import java.nio.file.Paths;

/**
 * @author Manedikte
 * Wendet auf RCPSP-Lib-Instanzen den ESS-Algorithmus an
 */
public class ESSTest {

        public static void main(String[] args) {

            if (args.length != 1) {
                System.out.println("usage: java Example <instance-path>");
                return;
            }
            final String path = args[0];

            final Instance instance = Io.readInstance(Paths.get(path));

            int[] reihenfolge = new int[instance.n()]; // Wir wollen die erste Dummy-Aktivität bei Stelle 1 haben

            for (int i = 0; i < instance.r(); i++) {
                System.out.printf("res %d has %d available units\n", i, instance.resources[i]);
            }

            System.out.println();

            int[] solution = new int[instance.n()];

            //Bevor der GA implementiert ist: "normale"/lexikographische Reihenfolge
            for (int i = 0; i <= instance.n()-1; i++) {
                reihenfolge[i] = i+1;
            }

            solution = ess(instance, reihenfolge);

            Io.writeSolution(solution, Paths.get("sol2.txt"));

            System.out.println("ok");
        }

        /**
         *
         * @param instance eine RCPSP-Instanz
         * @param reihenfolge die Reihenfolge, in denen die Aktivitäten betrachtet werden sollen, vorrangsbeziehungs-korrekt
         * @return ein earliest start schedule in Form eines int[]
         */
        static int[] ess(Instance instance, int[] reihenfolge){
            int[] sol = new int[instance.n()];
            int anzahl = instance.n();
            int j = 0;
            int max = 0;
            int t = 0;
            int horizont = 0;
            boolean knapp = true;

            // Berechne Zeithorizont T
            for (int i = 0; i < anzahl; i++) {
                horizont += instance.processingTime[i];
            }

            // Kapazitäten aller Ressourcen initialisieren (R_k(tau))
            int[][] restKap = new int[instance.r()][horizont+1];
            for (int i = 0; i < instance.r(); i++) {
                for (int k = 1; k <= horizont; k++) {
                    restKap[i][k] = instance.resources[i];
                }
            }

            // gehe durch die Reihenfolge der Jobs
            for (int i = 0; i < instance.n(); i++) {
                knapp = true;
                max = 0;
                j = reihenfolge[i];
                // gucke, ob jemand der bereits abgearbeitet ist (bzw. in der Reihenfolge vorher war) , den aktuellen Job als Nachfolger hat
                for (int k = 1; k < i; k++) {
                    for (int l = 0; l < instance.successors[reihenfolge[k]].length; l++) {
                        // Falls jemand diesen Nachfolger hat und die Fertigstellungszeit unseren earliest start für den aktuellen Job verzögert, aktualisiere den earliest start
                        if (instance.successors[reihenfolge[k]][l] == j) {
                            if (sol[reihenfolge[k]] + instance.processingTime[reihenfolge[k]] >= max) {
                                max = sol[reihenfolge[k]] + instance.processingTime[reihenfolge[k]];
                            }
                        }
                    }
                }
                // Setze earliest start auf den richtigen Wert (bzgl Vorrang)
                t = max;

                // läuft durch, solange die Ressourcenbeschränkungen noch nicht bei allen Ressourcen passen
                while(knapp){
                    // Dummy-Knoten abfangen
                    if(j == 1 || j == instance.n()){
                        knapp = false;
                    } else {
                        outerloop:
                        for (int l = 0; l < instance.r(); l++) {
                            for (int tau = t + 1; tau <= t + instance.processingTime[j-1]; tau++) {
                                if (instance.demands[j-1][l] > restKap[l][tau]) {
                                    // Falls unser Bedarf höher ist als die Restkapazität, gehe einen Zeitschritt weiter und unter suche nochmal alle Ressourcen
                                    t++;
                                    knapp = true;
                                    break outerloop;
                                } else {
                                    knapp = false;
                                }
                            }
                        }
                    }
                }

                //Trage Startzeit der j-ten Aktivität ein
                sol[j-1] = t;

                // Verbrauchten Ressourcen abziehen
                for (int p = t+1; p <= t+instance.processingTime[j-1]; p++) {
                    for (int l = 0; l < instance.r(); l++) {
                        restKap[l][p] -= instance.demands[j-1][l];
                    }
                }

            }

            return sol;

        }

    }

