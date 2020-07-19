package org.datadog.monitoring.stats;


import lombok.Data;

import java.util.*;

@Data
public class StatsSummary {
    private int totalRequestCount;

    private int invalidRequestCount;

    private int totalRequestContentSize;

    private final Map<String, Integer> sectionWiseHits = new HashMap<>();

    private final Map<HttpMethod, Integer> httpMethodsWiseHits = new HashMap<>();

    public void incrementRequestCount() { this.totalRequestCount++; }

    public void incrementRequestContentSize(int size) { this.totalRequestContentSize += size; }

    private static final int numberOfTopSections = 5;

    public StatsSummary() { }

    public StatsSummary(int totalRequestCount) {
        this.totalRequestCount = totalRequestCount;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("****** Traffic stats  ******\n");

        output.append(String.format("Total requests served: %s\n", totalRequestCount));
        output.append(String.format("Total content size: %s KB\n", getContentSizeInKB()));

        if (sectionWiseHits.size() > 0) {
            output.append(String.format("Top %s sections by hits:\n", numberOfTopSections));

            getTopSections().forEach((section, hitCount) -> output.append(String.format("\t%s -> %s\n", section, hitCount)));
        }

        if (httpMethodsWiseHits.size() > 0) {
            output.append("HTTP Methods by hits:\n");

            httpMethodsWiseHits.forEach((httpMethod, count) -> output.append(String.format("\t%s -> %s\n", httpMethod.name(), count)));
        }

        output.append("****** End of traffic stats ******\n");

        return output.toString();
    }

    private Map<String, Integer> getTopSections() {
        final Map<String, Integer> topSections = new HashMap<>();
        final NavigableMap<Integer, Set<String>> countVsSectionsMap = new TreeMap<>(Comparator.reverseOrder());

        sectionWiseHits.forEach((k,v) -> {
            countVsSectionsMap.putIfAbsent(v, new HashSet<>());
            countVsSectionsMap.get(v).add(k);
        });

        Set<Integer> topHits = countVsSectionsMap.keySet();
        for (Integer topHit: topHits) {
            for (String section: countVsSectionsMap.get(topHit)) {
                topSections.put(section, topHit);

                if (topSections.size() == numberOfTopSections) {
                    return topSections;
                }
            }
        }

        return topSections;
    }

    private int getContentSizeInKB() {
        return Math.round((float) totalRequestContentSize / 1024);
    }
}
