package org.datadog.monitoring.stats;


import lombok.Data;

import java.util.*;

@Data
public class StatsSummary {
    private int totalRequestCount;

    private int invalidRequestCount;

    private int totalRequestContentSize;

    private final Map<String, Integer> sectionWiseHits = new HashMap<>();

    public void incrementRequestCount() { this.totalRequestCount++; }

    public void incrementRequestContentSize(int size) { this.totalRequestContentSize += size; }

    private static final int numberOfTopSections = 5;

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("****** Traffic stats  ******\n");

        output.append(String.format("Total requests served: %s\n", totalRequestCount));
        output.append(String.format("Total content size: %s KB\n", getContentSizeInKB()));

        if (sectionWiseHits.size() > 0) {
            output.append(getTopSections());
        }

        output.append("****** End of traffic stats ******\n");

        return output.toString();
    }

    private StringBuilder getTopSections() {
        StringBuilder output = new StringBuilder();
        output.append(String.format("Top %s sections by hits:\n", numberOfTopSections));

        final NavigableMap<Integer, Set<String>> countVsSectionsMap = new TreeMap<>(Comparator.reverseOrder());

        sectionWiseHits.forEach((k,v) -> {
            countVsSectionsMap.putIfAbsent(v, new HashSet<>());
            countVsSectionsMap.get(v).add(k);
        });

        int count = 0;
        Set<Integer> topHits = countVsSectionsMap.keySet();
        for (Integer topHit: topHits) {
            for (String section: countVsSectionsMap.get(topHit)) {
                output.append(String.format("\t%s -> %s\n", section, topHit));

                count += 1;

                if (count == numberOfTopSections) {
                    return output;
                }
            }
        }

        return output;
    }

    private int getContentSizeInKB() {
        return Math.round((float) totalRequestContentSize / 1024);
    }
}
