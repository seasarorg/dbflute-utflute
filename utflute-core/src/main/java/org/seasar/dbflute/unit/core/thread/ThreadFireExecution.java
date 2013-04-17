package org.seasar.dbflute.unit.core.thread;

/**
 * @param <RESULT> The type of execution result.
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public interface ThreadFireExecution<RESULT> {

    /**
     * Execute the application code as thread-fire.
     * @param resource The resource for the thread-fire. (NotNull)
     * @return The result of the execution. (NullAllowed)
     */
    RESULT execute(ThreadFireResource resource);
}
