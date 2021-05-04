package de.uos.informatik.ko.rcp;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Io;
import de.uos.informatik.ko.rcp.Utils;

import de.uos.informatik.ko.rcp.generators.SmallestIndexRule;
import de.uos.informatik.ko.rcp.generators.ParallelScheduleGenerator;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: solver <instance path>");
            System.exit(1);
        }

        var instance = Io.readInstance(Paths.get(args[0]));

        // Fix wrong (?) predecessor relations
        fixInstance(instance);

        var rule = new SmallestIndexRule();
        final var schedule = ParallelScheduleGenerator.generateSchedule(instance, rule);

        System.out.println("Schedule:");
        for (int actIdx = 1; actIdx < instance.n() - 1; ++actIdx) {
            System.out.println(
                    "Activity " + actIdx + ": start time = " + schedule[actIdx]
                    + ", processing time = " + instance.processingTime[actIdx]);
        }

        System.out.println("Makespan: " + schedule[instance.n() - 1]);

        final boolean admissible = Utils.checkAdmissibility(instance, schedule);
        System.out.println("Admissible? " + admissible);
    }

    /**
     * Fix successor relation. Relation of the form i -> (n + 2) are turned into
     * i -> (n + 1). (This assumes that the non-dummy activities are i = 1, ..., n.)
     */
    private static void fixInstance(Instance instance) {
        final int n = instance.n();

        for (int actIdx = 0; actIdx < instance.n(); ++actIdx) {
            int[] successors = instance.successors[actIdx];

            for (int succIdx = 0; succIdx < successors.length; ++succIdx) {
                int succ = successors[succIdx];
                if (succ == n)
                    successors[succIdx] = n - 1;
            }
        }
    }

}
