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

import java.util.List;

import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.core.InjectionTestCase;
import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.dbflute.util.DfCollectionUtil;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public abstract class GuiceTestCase extends InjectionTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          Static Cache
    //                                          ------------
    /** The cached injector for DI container. (NullAllowed: null means beginning or test execution) */
    protected static Injector _xcachedInjector;

    // -----------------------------------------------------
    //                                          Guice Object
    //                                          ------------
    /** The current active injector for DI container. {Guice Object} */
    protected Injector _xcurrentActiveInjector;

    /** The transaction manager for platform. (NotNull: after injection) */
    protected TransactionManager _xtransactionManager;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    // -----------------------------------------------------
    //                                     Prepare Container
    //                                     -----------------
    @Override
    protected void xprepareTestCaseContainer() {
        if (isUseOneTimeContainer()) {
            xdestroyContainer();
        }
        if (_xcachedInjector != null) {
            _xcurrentActiveInjector = _xcachedInjector;
            return;
        }
        final List<Module> moduleList = prepareModuleList();
        _xcurrentActiveInjector = Guice.createInjector(moduleList.toArray(new Module[] {}));
        _xcachedInjector = _xcurrentActiveInjector;
    }

    /**
     * Prepare module list for Google Guice. <br />
     * You should add DataSource and TransactionManager to the module. 
     * @return The list of module. (NotNull)
     */
    protected List<Module> prepareModuleList() { // customize point
        return DfCollectionUtil.emptyList(); // as default
    }

    @Override
    protected void xclearCachedContainer() {
        _xcachedInjector = null;
    }

    // ===================================================================================
    //                                                                         Transaction
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransactionResource beginNewTransaction() { // user method
        if (_xtransactionManager == null) { // no use transaction (just in case)
            return null;
        }
        try {
            _xtransactionManager.begin();
        } catch (Exception e) {
            throw new TransactionFailureException("Failed to begin the transaction.", e);
        }
        final GuiceTransactionResource resource = new GuiceTransactionResource();
        resource.setTransactionManager(_xtransactionManager);
        return resource; // for thread-fire's transaction or manual transaction
    }

    // ===================================================================================
    //                                                                      Guice Handling
    //                                                                      ==============
    @Override
    protected void xdestroyContainer() {
        _xcachedInjector = null;
        _xcurrentActiveInjector = null;
    }

    /**
     * {@inheritDoc}
     */
    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
        return _xcurrentActiveInjector.getInstance(type);
    }

    /**
     * {@inheritDoc}
     */
    protected <COMPONENT> COMPONENT getComponent(String name) { // user method
        throw new IllegalStateException("The guice does not support by-name component: " + name);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean hasComponent(Class<?> type) { // user method
        try {
            getComponent(type);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean hasComponent(String name) { // user method
        try {
            getComponent(name);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
