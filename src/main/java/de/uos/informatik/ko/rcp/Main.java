package de.uos.informatik.ko.rcp;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.Io;
import de.uos.informatik.ko.rcp.Utils;

import de.uos.informatik.ko.rcp.generators.EarliestStartScheduleGenerator;

import de.uos.informatik.ko.rcp.generators.PriorityRule.SmallestIndexRule;
import de.uos.informatik.ko.rcp.generators.SerialScheduleGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: solver <instance path>");
            System.exit(1);
        }

        var instance = Io.readInstance(Paths.get(args[0]));

        final var essGenerator = new EarliestStartScheduleGenerator(instance);

        int[] order = new int[instance.n()];
        for (int i = 0; i < instance.n(); ++i) {
            order[i] = i + 1;
        }

        final var start = System.currentTimeMillis();
        final var schedule = essGenerator.generateSchedule(order);
        final var end = System.currentTimeMillis();

        System.out.println("Schedule:");
        for (int actIdx = 1; actIdx < instance.n() - 1; ++actIdx) {
            System.out.println(
                    "Activity " + actIdx + ": start time = " + schedule[actIdx]
                    + ", processing time = " + instance.processingTime[actIdx]);
        }

        System.out.println("Makespan: " + schedule[instance.n() - 1]);

        final boolean admissible = Utils.checkAdmissibility(instance, schedule);
        System.out.println("Admissible? " + admissible);

        System.out.println("Took " + (end - start) + " milliseconds.");
    }
}
