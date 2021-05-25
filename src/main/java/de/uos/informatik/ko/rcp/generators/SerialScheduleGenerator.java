package de.uos.informatik.ko.rcp.generators;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Utils;
import de.uos.informatik.ko.rcp.generators.PriorityRule.PriorityRule;
import java.util.HashSet;
import java.util.Map;

public class SerialScheduleGenerator {


    public static int[] generateSerialSchedule(final Instance instance, PriorityRule rule){
        //create Map for the Predecessors of the Activities
        final var predMap = Utils.buildPredecessorMap(instance);
        // Initizialize Set E_\lambda  ( Activities without Predecessor)
        HashSet<Integer> SetELambda = new HashSet<Integer>();
        // Initialize Set of scheduled Activities
        HashSet<Integer> Scheduled = new HashSet<Integer>();
        for (Map.Entry<Integer,HashSet<Integer>> entry: predMap.entrySet()){
            if (entry.getValue().isEmpty()){
                SetELambda.add(entry.getKey());
            }
        }
        // Start times indexed by job indices
        int[] startTimes = new int[instance.n()];
        // time at which the current activity could start
        int t;
        // Big For-Loop
        boolean feasible = true;
        for (int lambda= 1; lambda<= instance.n(); lambda++){
            Integer currAct= rule.chooseActivity(SetELambda, instance);
            //
            int max= 0;
            for (Integer entry : predMap.get(currAct)){
                if (startTimes[entry]+instance.processingTime[entry] > max){
                    max = startTimes[entry]+instance.processingTime[entry];
                }
            }
            t = max;
            feasible = false;
            while(!feasible) {
                feasible = true;
                // Demands of Ressources from scheduled Activities at time t
                int[] demands = new int[instance.r()];
                for (Integer entry : Scheduled) {
                    if ((startTimes[entry] < t && t < startTimes[entry] + instance.processingTime[entry])
                            || (startTimes[entry] == t && t < startTimes[entry] + instance.processingTime[entry]) ) {
                        for (int i = 0; i < demands.length; i++) {
                            demands[i] += instance.demands[entry][i];
                        }
                    }
                }
                // here demands for time t
                int[] result = new int[demands.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = instance.resources[i] - demands[i];
                }
                // ergaenze Result mit Anfragen der aktuellen Aktivitaet
                for (int i = 0; i < demands.length; i++) {
                    result[i] -= instance.demands[currAct][i];
                }
                for (int i = 0; i < demands.length; i++) {
                    if (result[i] < 0) {
                        feasible = false;
                    }
                }
                if (!feasible) {
                    t++;
                }
            }
            //hier starTime für
            startTimes[currAct]= t;
            Scheduled.add(currAct);
            SetELambda.remove(currAct);
            for(int i=0; i < instance.successors[currAct].length;i++) {
                if (!Scheduled.contains(instance.successors[currAct][i])) {
                    //alle Vorgänger schon eingeplant?
                    boolean ready = true;
                        var PredecessorSet=predMap.get(instance.successors[currAct][i]);
                        for (Integer entry : PredecessorSet) {
                            if (!Scheduled.contains(entry)) {
                                ready = false;
                            }
                        }
                    if (ready){
                        SetELambda.add(instance.successors[currAct][i]);
                    }
                }
            }
        }
        return startTimes;
    }
}
