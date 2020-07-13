package org.datadog.monitoring.stats;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class StatsSummary {
    private int totalRequestCount;

    private int invalidRequestCount;

    private int totalRequestContentSize;

    private final Map<String, Integer> sectionCount = new HashMap<>();

    public void incrementRequestCount() { this.totalRequestCount++; }

    public void incrementRequestContentSize(int size) { this.totalRequestContentSize += size; }

    public void print() {
        System.out.println(String.format("Total requests served: %s", totalRequestCount));
        System.out.println(String.format("Total content size: %s", totalRequestContentSize));

        if (sectionCount.size() > 0) {
            System.out.println("Hits by section");
            sectionCount.forEach((k,v) -> System.out.println(String.format("\t\t%s -> %s", k, v)));
        }
    }
}
