package de.uos.informatik.ko.rcp.generators;

import java.lang.Integer;
import java.lang.reflect.Array;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.generators.PriorityRule;
import de.uos.informatik.ko.rcp.Utils;

public class ParallelScheduleGenerator {

    /**
     * Generate an activity schedule by way of parallel schedule generation, as
     * described in chapter 3.2 of
     *
     * P. Brucker, S. Knust (2012). "Complex Scheduling", 2nd edition. Springer.
     *
     * Assumes that the non-dummy activities are i = 1, ..., n. However, the instance
     * must also contain a dummy start node 0 and a dummy end node (n + 1) with the
     * semantics commonly described in the literature.
     *
     * @param instance Instance to generate schedule for.
     * @param rule PriorityRule to use when choosing activities when multiple are eligible.
     *
     * @return A schedule that contains the start time of activity i at schedule[i].
     */
    public static int[] generateSchedule(final Instance instance, PriorityRule rule) {
        final var predMap = Utils.buildPredecessorMap(instance);

        // Set up inital resource capacities
        var timeCapacities = (ArrayList<Integer>[]) Array.newInstance(ArrayList.class, instance.r());

        // Init arrays
        for (int resIdx = 0; resIdx < instance.r(); ++resIdx) {
            timeCapacities[resIdx] = new ArrayList<Integer>();
            timeCapacities[resIdx].add(instance.resources[resIdx]);
        }

        int[] startTimes = new int[instance.n()]; // Start times indexed by job indices
        int stage = 1;
        int curTime = 0;

        var finished = new HashSet<Integer>();
        var active = new HashSet<Integer>();
        var eligible = new HashSet<Integer>();

        // Force dummy node to be scheduled first
        eligible.add(0);

        // Schedule activities until none are left
        while (finished.size() != instance.n()) {
            while (!eligible.isEmpty()) {
                // Schedule new activity
                var selectedAct = rule.chooseActivity(eligible, instance);
                startTimes[selectedAct] = curTime;

                // Update resource profiles
                final var procTime = instance.processingTime[selectedAct];
                final var demands = instance.demands[selectedAct];

                for (int resIdx = 0; resIdx < instance.r(); ++resIdx) {
                    final var demand = demands[resIdx];

                    // Early out if we don't need to update
                    if (demand == 0)
                        continue;

                    var capOverTime = timeCapacities[resIdx];

                    // Ensure list is long enough
                    while (capOverTime.size() < curTime + procTime + 1)
                        capOverTime.add(instance.resources[resIdx]);

                    // Update resource
                    for (int t = curTime; t < curTime + procTime; ++t)
                        capOverTime.set(t, capOverTime.get(t) - demand);
                }

                // Update sets
                active.add(selectedAct);
                eligible.remove(selectedAct);

                var it = eligible.iterator();
                while (it.hasNext()) {
                    final Integer actIdx = it.next();

                    for (int resIdx = 0; resIdx < instance.r(); ++resIdx) {
                        final var demand = instance.demands[actIdx][resIdx];

                        if (demand == 0)
                            continue;

                        var capOverTime = timeCapacities[resIdx];
                        // Ensure list is long enough
                        while (capOverTime.size() < curTime + procTime + 1)
                            capOverTime.add(instance.resources[resIdx]);

                        // Check whether resource constraint is violated now
                        if (capOverTime.get(curTime + 1) < demand ||
                            capOverTime.get(curTime + procTime) < demand) {
                            it.remove();
                            break;
                        }
                    }
                }
            }

            // Jump to next decision time
            int nextFinishingTime = Integer.MAX_VALUE;
            for (Integer actIdx : active) {
                final int startTime = startTimes[actIdx];
                final int procTime = instance.processingTime[actIdx];

                final int endTime = startTime + procTime;
                if (endTime < nextFinishingTime) {
                    nextFinishingTime = endTime;
                }
            }
            curTime = nextFinishingTime;
            stage += 1;

            // Update "finished" set
            var it = active.iterator();
            while (it.hasNext()) {
                var actIdx = it.next();

                if (startTimes[actIdx] + instance.processingTime[actIdx] <= curTime) {
                    // Activity finished
                    it.remove();
                    finished.add(actIdx);
                }
            }

            // Update "eligible" set
            for (int actIdx = 0; actIdx < instance.n(); ++actIdx) {
                // Eligible activities may not be active or finished already
                if (active.contains(actIdx) || finished.contains(actIdx))
                    continue;

                // Every predecessor must have finished for an activity to be eligible
                boolean predsFinished = true;
                for (Integer predIdx : predMap.get(actIdx)) {
                    if (!finished.contains(predIdx)) {
                        predsFinished = false;
                        break;
                    }
                }

                if (!predsFinished)
                    continue;

                // Check resource constraints
                final var procTime = instance.processingTime[actIdx];
                final var demands = instance.demands[actIdx];

                boolean ok = true;
                for (int resIdx = 0; resIdx < instance.r(); ++resIdx) {
                    final var demand = demands[resIdx];

                    if (demand == 0)
                        continue;

                    var capOverTime = timeCapacities[resIdx];

                    // Ensure list is long enough
                    while (capOverTime.size() < curTime + procTime + 1)
                        capOverTime.add(instance.resources[resIdx]);

                    for (int t = curTime; t < curTime + procTime; ++t) {
                        if (capOverTime.get(t) < demand) {
                            // Not enough resource capacity anymore
                            ok = false;
                            break;
                        }
                    }
                }

                // Only add the activity if it satisfies all resource constraints
                if (ok)
                    eligible.add(actIdx);
            }
        }

        return startTimes;
    }
}
