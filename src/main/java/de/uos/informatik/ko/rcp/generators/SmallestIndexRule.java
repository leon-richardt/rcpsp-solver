package de.uos.informatik.ko.rcp.generators;

import java.util.HashSet;
import java.util.Collections;

import de.uos.informatik.ko.rcp.Instance;
import de.uos.informatik.ko.rcp.generators.PriorityRule;

public class SmallestIndexRule implements PriorityRule {
    public int chooseActivity(HashSet<Integer> eligible, Instance instance) {
        return Collections.min(eligible);
    }
}
