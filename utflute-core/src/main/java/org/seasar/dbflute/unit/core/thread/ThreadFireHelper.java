/*
 * Copyright 2004-2013 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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
