package tracker.TaskManager;

import tracker.issue.ReadableIssue;

import java.util.ArrayList;
import java.util.List;

public class MergeSorted {
    public static List<ReadableIssue> mergeSorted(List<ReadableIssue> first, List<ReadableIssue> second) {
        List<ReadableIssue> merged = new ArrayList<>(first.size() + second.size());
        int i = 0, j = 0;

        while (i < first.size() && j < second.size()) {
            if (first.get(i).getStartTime().orElseThrow().isBefore(second.get(j).getStartTime().orElseThrow())) {
                merged.add(first.get(i++));
            } else {
                merged.add(second.get(j++));
            }
        }

        while (i < first.size()) merged.add(first.get(i++));
        while (j < second.size()) merged.add(second.get(j++));

        return merged;
    }
}
