package de.uos.informatik.ko.rcp.generators;

import java.lang.Integer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Utils;

public class EarliestStartScheduleGenerator {
    private Instance instance;
    private HashMap<Integer, HashSet<Integer>> predMap;

    /**
     * Construct a earliest start schedule generator for `instance`. Schedules are generated
     * according to the earliest start schedule generation scheme described in subsection 3.2.1 of
     *
     * P. Brucker, S. Knust (2012). "Complex Scheduling", 2nd edition. Springer.
     *
     * The instance caches auxiliary information about the instance to speed up calculations.
     *
     * @param instance `Instance` to generate schedule for.
     */
    public EarliestStartScheduleGenerator(Instance instance) {
        this.instance = instance;
        this.predMap = Utils.buildPredecessorMap(instance);
    }

    /**
     * Generate an activity schedule by way of earliest start schedule generation for `order`.
     *
     * @param order An array of length `instance.n()` containing the activities in the order they
     *              should be considered for scheduling. The array must contain the dummy activities
     *              as well; with the dummy start activity in the first place and the dummy end
     *              activity in the last place. The order in which the activities occur must be
     *              predecessor-admissible.
     *
     * @return A schedule that contains the start time of activity i at `schedule[i - 1]`. Since the
     *         dummy activities are contained in the schedule, the makespan of the schedule can be
     *         obtained as the starting time by checking `schedule[schedule.length - 1]`.
     */
    public int[] generateSchedule(int[] order) {
        int[] startTimes = new int[this.instance.n()];

        // jumpTimes[resIdx] stores all time points where resIdx's availability changes
        var jumpTimes = (ArrayList<Integer>[]) Array.newInstance(ArrayList.class,
                                                                 this.instance.r());
        for (int resIdx = 0; resIdx < jumpTimes.length; ++resIdx) {
            jumpTimes[resIdx] = new ArrayList<Integer>();
        }

        // Upper bound on the makespan (assuming resource capacities are ignored)
        int maxTime = 0;
        for (int actIdx = 0; actIdx < this.instance.n(); ++actIdx) {
            maxTime += this.instance.processingTime[actIdx];
        }

        int[][] availableRessources = new int[this.instance.r()][maxTime + 1];
        // Set up initial resource availabilities
        for (int resIdx = 0; resIdx < this.instance.r(); ++resIdx) {
            for (int t = 0; t <= maxTime; ++t) {
                availableRessources[resIdx][t] = this.instance.resources[resIdx];
            }
        }

        for (int lambda = 0; lambda < this.instance.n(); ++lambda) {
            // The read-in indices are 1, ..., n. But since Java is 0-indexed, we do an index shift
            // so that `curActIdx` refers to the position of the activity in the `Instance` arrays.
            final int curActIdx = order[lambda] - 1;
            final int curProcTime = this.instance.processingTime[curActIdx];
            final var curDemands = this.instance.demands[curActIdx];
            // Undo index shift because predecessor map uses 1, ..., n 🤡
            final var curPreds = this.predMap.get(curActIdx + 1);

            int startTime = 0;

            // Determine when all predecessors of `curActIdx` have finished
            for (int predActIdx : curPreds) {
                final int predActEndTime = startTimes[predActIdx - 1]
                                            + this.instance.processingTime[predActIdx - 1];
                startTime = Math.max(startTime, predActEndTime);
            }

            int violatedResIdx = -1;

            // Only do resource checks for non-dummy activities
            if (!(curActIdx == 0 || curActIdx == this.instance.n() - 1)) {
                violatedResIdx = this.getViolatedRessource(availableRessources, curActIdx,
                                                           startTime);
            }

            while (violatedResIdx != -1) {
                final int constStartTime = startTime;

                var possibleStartTimes = jumpTimes[violatedResIdx]
                                             .stream()
                                             .filter(jumpTime -> jumpTime > constStartTime)
                                             .collect(Collectors.toList());

                final int demand = curDemands[violatedResIdx];
                final int[] availibilityByTime = availableRessources[violatedResIdx];

                for (int possibleStartTime : possibleStartTimes) {
                    boolean admissible = true;
                    for (int tau = possibleStartTime; tau < possibleStartTime + curProcTime; ++tau) {
                        if (demand > availibilityByTime[tau]) {
                            admissible = false;
                            break;
                        }
                    }

                    if (admissible) {
                        startTime = possibleStartTime;
                        break;
                    }
                }

                // Check if all resources are satisfied now
                violatedResIdx = this.getViolatedRessource(availableRessources, curActIdx,
                                                           startTime);
            }

            // We found a resource-admissible start time, so schedule the activity now
            startTimes[curActIdx] = startTime;

            // Update resource profiles
            for (int resIdx = 0; resIdx < this.instance.r(); ++resIdx) {
                var availibilityByTime = availableRessources[resIdx];
                final var demand = curDemands[resIdx];

                for (int tau = startTime; tau < startTime + curProcTime; ++tau) {
                    availibilityByTime[tau] = availibilityByTime[tau] - demand;
                }

                jumpTimes[resIdx].add(startTime);
                jumpTimes[resIdx].add(startTime + curProcTime);
            }
        }

        return startTimes;
    }

    // Returns -1 if no resource is violated
    private int getViolatedRessource(int[][] resourceAvailabilities, int actIdx, int startTime) {
        final int procTime = this.instance.processingTime[actIdx];

        for (int resIdx = 0; resIdx < this.instance.r(); ++resIdx) {
            final int demand = this.instance.demands[actIdx][resIdx];
            final int[] availibilityByTime = resourceAvailabilities[resIdx];

            for (int tau = startTime; tau < startTime + procTime; ++tau) {
                if (demand > availibilityByTime[tau]) {
                    return resIdx;
                }
            }
        }

        return -1;
    }

}
