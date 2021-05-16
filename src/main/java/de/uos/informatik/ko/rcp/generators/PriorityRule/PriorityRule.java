package de.uos.informatik.ko.rcp.generators.PriorityRule;

import java.util.HashSet;

import de.uos.informatik.ko.rcp.Instance;

public interface PriorityRule {
    /**
     * Return the index of the next activity that should be scheduled.
     *
     * @param eligible Activities that can be scheduled
     * @param instance Instance the activities belong to
     */
    int chooseActivity(HashSet<Integer> eligible, Instance instance);
}
