package org.datadog.monitoring.traffic;

import java.util.Arrays;
import java.util.Optional;

public enum HttpMethod {
    GET,
    PUT,
    POST,
    DELETE,
    OPTIONS,
    HEAD,
    TRACE,
    OTHER;

    public static Optional<HttpMethod> getHttpMethod(String methodName) {
        return Arrays.stream(HttpMethod.values())
                .filter(httpMethod -> httpMethod.name().equalsIgnoreCase(methodName))
                .findFirst();
    }
}
