package de.uos.informatik.ko.rcp;

import java.lang.reflect.Array;  
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.uos.informatik.ko.rcp.Instance;

public class Utils {
    private Utils() {}

    public static HashMap<Integer, HashSet<Integer>> buildPredecessorMap(Instance instance) {
        var predMap = new HashMap<Integer, HashSet<Integer>>();

        for (int actIdx = 0; actIdx < instance.n(); ++actIdx) {
            predMap.put(actIdx, new HashSet<Integer>());
        }

        for (int actIdx = 0; actIdx < instance.n(); ++actIdx) {
            for (Integer succ : instance.successors[actIdx]) {
                predMap.get(succ).add(actIdx);
            }
        }

        return predMap;
    }

    public static boolean checkAdmissibility(Instance instance, int[] schedule) {
        // Maps an activity to its finish time
        final var finishTime = new HashMap<Integer, Integer>(instance.n());

        final var predMap = Utils.buildPredecessorMap(instance);
        final int makespan = schedule[instance.n() - 1];

        var activeByTime = (ArrayList<Integer>[]) Array.newInstance(ArrayList.class, makespan);
        for (int i = 0; i < activeByTime.length; ++i) {
            activeByTime[i] = new ArrayList<Integer>();
        }

        // Build time-indexed version of schedule
        for (int actIdx = 0; actIdx < instance.n(); ++actIdx) {
            final int startTime = schedule[actIdx];
            final int endTime = instance.processingTime[actIdx];

            for (int t = startTime; t < endTime; ++t) {
                activeByTime[t].add(actIdx);
            }

            finishTime.put(actIdx, endTime);
        }

        // Check pred. relations
        for (int actIdx = 0; actIdx < instance.n(); ++actIdx) {
            final int startTime = schedule[actIdx];

            for (Integer predIdx : predMap.get(actIdx)) {
                final int predEndTime = finishTime.get(predIdx);
                if (startTime < predEndTime)
                    return false;
            }
        }

        // Check ressource constraints
        for (int t = 0; t < makespan; ++t) {
            ArrayList<Integer> actives = activeByTime[t];

            for (int resIdx = 0; resIdx < instance.r(); ++resIdx) {
                int cumDemand = 0;
                for (Integer activeIdx : actives) {
                    cumDemand += instance.demands[activeIdx][resIdx];
                }

                if (cumDemand > instance.resources[resIdx])
                    return false;
            }
        }

        return true;
    }
}
