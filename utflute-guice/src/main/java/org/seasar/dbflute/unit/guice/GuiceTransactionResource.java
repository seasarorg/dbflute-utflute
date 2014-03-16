/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.unit.guice;

import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class GuiceTransactionResource implements TransactionResource {

    protected TransactionManager _transactionManager;

    public void commit() {
        try {
            _transactionManager.commit();
        } catch (RuntimeException e) {
            throw new TransactionFailureException("Failed to commit the transaction.", e);
        } catch (Exception e) {
            throw new TransactionFailureException("Failed to commit the transaction.", e);
        }
    }

    public void rollback() {
        try {
            _transactionManager.rollback();
        } catch (RuntimeException e) {
            throw new TransactionFailureException("Failed to roll-back the transaction.", e);
        } catch (Exception e) {
            throw new TransactionFailureException("Failed to roll-back the transaction.", e);
        }
    }

    public TransactionManager getTransactionManager() {
        return _transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this._transactionManager = transactionManager;
    }
}
