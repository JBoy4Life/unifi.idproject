package id.unifi.service.common.api.access;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AccessUtils {
    private static final Pattern operationPartDelimiter = Pattern.compile("\\.");

    /**
     * Checks if any operation in a collection subsumes the given operation.
     *
     * @see AccessUtils#getAllSubsumingOperations(String)
     */
    public static boolean subsumesOperation(Collection<String> operationCollection, String operation) {
        return getAllSubsumingOperations(operation).anyMatch(operationCollection::contains);
    }

    /**
     * Returns all operations that subsume the given operation.
     *
     * An operation A subsumes operation B ("&lt;vertical&gt;.&lt;service&gt;.&lt;operation&gt;")
     * iff A = B or A = "&lt;vertical&gt;.&lt;service&gt;.*" or A = "&lt;vertical&gt;.*.*".
     * <p>
     * E.g.:
     * <ul>
     * <li>"core.*.*", subsumes "core.operator.list-operators"</li>
     * <li>"core.*.*", subsumes "core.operator.*"</li>
     * <li>"core.operator.*", subsumes "core.operator.list-operators"</li>
     * <li>"core.operator.list-operators", subsumes "core.operator.list-operators"</li>
     * </ul>
     *
     * @param operation operation name (potentially wildcard)
     * @return all operations that subsume the given operation
     */
    public static Stream<String> getAllSubsumingOperations(String operation) {
        var parts = operationPartDelimiter.split(operation);
        return Stream.of(operation, parts[0] + "." + parts[1] + ".*", parts[0] + ".*.*").distinct();
    }
}
