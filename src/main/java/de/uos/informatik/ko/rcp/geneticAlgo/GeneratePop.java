package de.uos.informatik.ko.rcp.geneticAlgo;

import java.lang.reflect.Array;
import java.lang.Integer;
import java.util.*;
import de.uos.informatik.ko.rcp.Utils;
import de.uos.informatik.ko.rcp.Instance;


public class GeneratePop {

    private GeneratePop() {}
    //change to Hashset of ArrayLists
    public static HashSet<ArrayList<Integer>> generatePop(Instance instance, Integer pop_size,Random myGenerator){
        // all activities without predecessor are possible candidates
        HashSet<ArrayList<Integer>> results = new HashSet<ArrayList<Integer>>();
        HashMap<Integer,HashSet<Integer>> mypredecessors;
        HashSet<Integer> candidates = new HashSet<Integer>();
        //scheduled activities
        ArrayList<Integer> scheduled = new ArrayList<Integer>();
        for (int i=0; i< pop_size*5; i++) {
            mypredecessors = Utils.buildPredecessorMap(instance);
            for (Map.Entry<Integer, HashSet<Integer>> entry : mypredecessors.entrySet()) {
                if (entry.getValue().isEmpty() && (!(scheduled.contains(entry.getKey())))) {
                    candidates.add(entry.getKey());
                }
            }
            Integer mycandidate = chooseone(candidates, myGenerator);
            scheduled.add(mycandidate);
            candidates.remove(mycandidate);
            //delete scheduled activity from predecessor list
            for (Map.Entry<Integer, HashSet<Integer>> entry : mypredecessors.entrySet()) {
                entry.getValue().remove(mycandidate);
            }
            while (scheduled.size() < instance.n()) {
                for (Map.Entry<Integer, HashSet<Integer>> entry : mypredecessors.entrySet()) {
                    if (entry.getValue().isEmpty()&& (!(scheduled.contains(entry.getKey())))) {
                        candidates.add(entry.getKey());
                    }
                }
                Integer actcandidate = chooseone(candidates, myGenerator);
                scheduled.add(actcandidate);
                candidates.remove(actcandidate);
                // delete scheduled activity from predecessor list
                for (Map.Entry<Integer, HashSet<Integer>> entry : mypredecessors.entrySet()) {
                    entry.getValue().remove(actcandidate);
                }
            }
            ArrayList<Integer> resultsList = new ArrayList<Integer>();
            for (Integer elem: scheduled){
                resultsList.add(elem);
            }
            results.add(resultsList);
            scheduled.clear();
            candidates.clear();
            if (results.size() >= pop_size){
                break;
            }
        }
        return results;
    }
    // chooses randomly one Integer out of the Integer in the given Set, the Random Generator is given
    public static Integer chooseone( HashSet<Integer> candidates, Random myGenerator){
        Integer returncanidate;
        int mycandidate=0;
        int item = myGenerator.nextInt(candidates.size());
        int i = 0;
        for(Integer elem: candidates) {
            if (i == item) {
                mycandidate = elem;
                break;
            }
            i++;
        }
        returncanidate = mycandidate;
        return returncanidate;
    }
    //Returns a Hashset as an Array
    //the rows are the lists from left to right
    public static int[][] ReturnArray(HashSet<ArrayList<Integer>> mySet){
        int length = mySet.iterator().next().size();
        int[][] myArray = new int[mySet.size()][length];
        Iterator<ArrayList<Integer>> myIterator = mySet.iterator();
        ArrayList<Integer> currentList;
        for (int i= 0; i< mySet.size(); i++){
            currentList = myIterator.next();
            for (int j= 0; j< length; j++){
                myArray[i][j]= currentList.get(j);
            }
        }
        return myArray;
    }
    public static int GetRowCount(HashSet<ArrayList<Integer>> mySet){
        return mySet.size();
    }

    public static int GetColumnCount(HashSet<ArrayList<Integer>> mySet){
        return mySet.iterator().next().size();
    }

}
