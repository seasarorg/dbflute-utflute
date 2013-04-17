package org.seasar.dbflute.unit.core.thread;

import org.seasar.dbflute.unit.core.transaction.TransactionResource;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public interface ThreadFireHelper {

    TransactionResource help_beginTransaction();

    void help_prepareAccessContext();

    void help_clearAccessContext();

    void help_assertEquals(Object expected, Object actual);

    void help_fail(String msg);

    void help_log(Object... msges);

    String help_ln();
}
