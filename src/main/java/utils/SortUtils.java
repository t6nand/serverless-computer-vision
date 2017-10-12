package main.java.utils;

import com.tinkerpop.pipes.util.structures.Pair;

import java.util.*;

/**
 * Created by tapansharma on 12/10/17.
 */
public class SortUtils {
    /**
     * This method is used to sort set of pairs of integers based upon first value in pair i.e. {@link Pair#getA()}
     * in order specified.
     *
     * @param setOfPairs   Set to be sorted.
     * @param reverseOrder if true sorts in reverse order i.e. descending order of {@link Pair#getA()} values.
     * @return Sorted Set.
     */
    public static Set<Pair<Integer, Integer>> orderSetOfPairs(Set<Pair<Integer, Integer>> setOfPairs, boolean reverseOrder) {
        final boolean isReverseOrder = reverseOrder;
        List<Pair<Integer, Integer>> resizeSortStageList = new ArrayList<>(setOfPairs);
        Collections.sort(resizeSortStageList, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                return isReverseOrder ? o2.getA().compareTo(o1.getA()) : o1.getA().compareTo(o2.getA());
            }
        });
        setOfPairs.clear();
        setOfPairs.addAll(resizeSortStageList);
        return setOfPairs;
    }
}
