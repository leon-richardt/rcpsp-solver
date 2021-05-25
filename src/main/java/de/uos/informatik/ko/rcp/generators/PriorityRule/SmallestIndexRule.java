package de.uos.informatik.ko.rcp.generators.PriorityRule;

import java.util.HashSet;
import java.util.Collections;

import de.uos.informatik.ko.rcp.Instance;

public class SmallestIndexRule implements PriorityRule {
    public int chooseActivity(HashSet<Integer> eligible, Instance instance) {
        return Collections.min(eligible);
    }
}