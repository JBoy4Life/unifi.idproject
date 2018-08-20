package id.unifi.service.common.api.http;

import id.unifi.service.common.api.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

public class PathNode {
    private Map<String, PathNode> valueMappings;
    private String paramName;
    private PathNode paramTarget;
    private ServiceRegistry.Operation operation;

    public PathNode() {}

    public PathNode addMapping(PathSegment segment) {
        return segment.isParam() ? addParamMapping(segment.getParamName()) : addValueMapping(segment.getValue());
    }

    private PathNode addValueMapping(String value) {
        if (paramName != null) throw new IllegalStateException();
        if (valueMappings == null) valueMappings = new HashMap<>();
        return valueMappings.computeIfAbsent(value, v -> new PathNode());
    }

    private PathNode addParamMapping(String paramName) {
        if (valueMappings != null || (this.paramName != null && !this.paramName.equals(paramName)))
            throw new IllegalStateException();
        if (this.paramName == null) {
            this.paramName = paramName;
            paramTarget = new PathNode();
        }
        return paramTarget;
    }

    public PathNodeMatch match(String input) {
        if (paramName != null) {
            return new PathNodeMatch(paramTarget, paramName, input);
        }

        if (valueMappings != null) {
            var match = valueMappings.get(input);
            return match == null ? null : new PathNodeMatch(match);
        }

        return null;
    }

    public void setOperation(ServiceRegistry.Operation operation) {
        if (this.operation != null) throw new IllegalStateException();
        this.operation = operation;
    }

    public ServiceRegistry.Operation getOperation() {
        return operation;
    }

    public static class PathNodeMatch {
        public final PathNode node;
        public final String paramName;
        public final String paramValue;

        private PathNodeMatch(PathNode node) {
            this.node = node;
            this.paramName = null;
            this.paramValue = null;
        }

        private PathNodeMatch(PathNode node, String paramName, String paramValue) {
            this.node = node;
            this.paramName = paramName;
            this.paramValue = paramValue;
        }
    }
}
