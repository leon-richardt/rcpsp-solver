package de.uos.informatik.ko.rcp.geneticalgorithm;

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
        // the predecessor Map
        HashMap<Integer,HashSet<Integer>> mypredecessors = Utils.buildPredecessorMap(instance);
        // the predecessor Map for the current iteration, changes are made to this map
        // new for every iteration
        HashMap<Integer,HashSet<Integer>> currentpredecessors;
        HashSet<Integer> candidates = new HashSet<Integer>();
        //scheduled activities
        ArrayList<Integer> scheduled = new ArrayList<Integer>();
        //variable witch defines how often the loop will be executed variable*pop_size
        // five is just a first guess, not further experimentation
        int numretries = 5;
        //continues member generation until the pop-size*numretries iterarion is reached, or
        //pop_size unique members are generated
        for (int i=0; i< pop_size*numretries; i++) {
            currentpredecessors = copyHashMap(mypredecessors);
            for (Map.Entry<Integer, HashSet<Integer>> entry : currentpredecessors.entrySet()) {
                if (entry.getValue().isEmpty() && (!(scheduled.contains(entry.getKey())))) {
                    candidates.add(entry.getKey());
                }
            }
            Integer mycandidate = chooseone(candidates, myGenerator);
            scheduled.add(mycandidate);
            candidates.remove(mycandidate);
            //delete scheduled activity from predecessor list
            for (Map.Entry<Integer, HashSet<Integer>> entry : currentpredecessors.entrySet()) {
                entry.getValue().remove(mycandidate);
            }
            while (scheduled.size() < instance.n()) {
                for (Map.Entry<Integer, HashSet<Integer>> entry : currentpredecessors.entrySet()) {
                    if (entry.getValue().isEmpty()&& (!(scheduled.contains(entry.getKey())))) {
                        candidates.add(entry.getKey());
                    }
                }
                Integer actcandidate = chooseone(candidates, myGenerator);
                scheduled.add(actcandidate);
                candidates.remove(actcandidate);
                // delete scheduled activity from predecessor list
                for (Map.Entry<Integer, HashSet<Integer>> entry : currentpredecessors.entrySet()) {
                    entry.getValue().remove(actcandidate);
                }
            }
            ArrayList<Integer> resultsList = new ArrayList<Integer>(scheduled);
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
        int index = myGenerator.nextInt(candidates.size());
        Iterator<Integer> iter = candidates.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
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

    public static HashMap<Integer, HashSet<Integer>> copyHashMap(HashMap<Integer, HashSet<Integer>> targetMap){
        HashMap<Integer, HashSet<Integer>> newMap = new HashMap<Integer, HashSet<Integer>>();
        // copy the keys und the values (here a HashMap)
        for (Integer elem : targetMap.keySet()){
            newMap.put(elem,new HashSet<Integer>(targetMap.get(elem)));
        }
        return newMap;
    }

}
