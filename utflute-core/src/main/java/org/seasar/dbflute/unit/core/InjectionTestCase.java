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
package org.seasar.dbflute.unit.core;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.unit.core.binding.BoundResult;
import org.seasar.dbflute.unit.core.binding.ComponentBinder;
import org.seasar.dbflute.unit.core.binding.ComponentProvider;
import org.seasar.dbflute.unit.core.mocklet.Mocklet;
import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;

/**
 * @author jflute
 * @since 0.1.2 (2011/09/16 Friday)
 */
public abstract class InjectionTestCase extends PlainTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                    Transaction Object
    //                                    ------------------
    /** The object that has transaction resources for test case. */
    protected TransactionResource _xtestCaseTransactionResource;

    // -----------------------------------------------------
    //                                     Component Binding
    //                                     -----------------
    /** The binder of component for the test case. (NotNull) */
    protected final ComponentBinder _xtestCaseComponentBinder = xcreateTestCaseComponentBinder();

    /** The result of bound component for the test case. (NullAllowed: before binding, after destroy) */
    protected BoundResult _xtestCaseBoundResult;

    /** The list of mock instance injected to component. (NullAllowed: when no mock) */
    protected List<Object> _xmockInstanceList; // lazy-loaded

    /** The list of non-binding type NOT injected to component. (NullAllowed: when no mock) */
    protected List<Class<?>> _xnonBindingTypeList; // lazy-loaded

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    public void setUp() throws Exception {
        super.setUp();
        xsetupBeforeContainer();
        xprepareTestCaseContainer();
        xprepareTestCaseComponent();
        xbeginTestCaseTransaction();
    }

    protected void xsetupBeforeContainer() {
    }

    protected abstract void xprepareTestCaseContainer();

    /**
     * Does it use one-time container? (re-initialize container per one test case?)
     * @return The determination, true or false.
     */
    protected boolean isUseOneTimeContainer() { // customize point
        return false;
    }

    protected void xprepareTestCaseComponent() {
        _xtestCaseBoundResult = _xtestCaseComponentBinder.bindComponent(this);
    }

    protected void xbeginTestCaseTransaction() {
        if (isSuppressTestCaseTransaction()) {
            return;
        }
        _xtestCaseTransactionResource = beginNewTransaction();
    }

    /**
     * Does it suppress transaction for the test case? (non-transaction as default?)
     * @return The determination, true or false.
     */
    protected boolean isSuppressTestCaseTransaction() { // customize point
        return false; // default is to use the transaction
    }

    @Override
    public void tearDown() throws Exception {
        xrollbackTestCaseTransaction();
        xdestroyTestCaseComponent();
        xdestroyTestCaseContainer();
        _xmockInstanceList = null;
        _xnonBindingTypeList = null;
        super.tearDown();
    }

    protected void xrollbackTestCaseTransaction() {
        if (isSuppressTestCaseTransaction()) {
            return;
        }
        if (isCommitTestCaseTransaction()) {
            commitTransaction(_xtestCaseTransactionResource);
        } else {
            rollbackTransaction(_xtestCaseTransactionResource);
        }
        _xtestCaseTransactionResource = null;
    }

    /**
     * Does it commit transaction for the test case? (commit updated data?)
     * @return The determination, true or false.
     */
    protected boolean isCommitTestCaseTransaction() { // customize point
        return false; // default is to roll-back always
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void commitTransaction(TransactionResource resource) { // user method
        xassertTransactionResourceNotNull(resource);
        try {
            resource.commit();
        } catch (Exception e) {
            String msg = "Failed to commit the transaction: " + resource;
            throw new TransactionFailureException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void rollbackTransaction(TransactionResource resource) { // user method
        xassertTransactionResourceNotNull(resource);
        try {
            resource.rollback();
        } catch (Exception e) {
            String msg = "Failed to roll-back the transaction: " + resource;
            throw new TransactionFailureException(msg, e);
        }
    }

    protected void xdestroyTestCaseComponent() {
        _xtestCaseComponentBinder.unbindComponent(this, _xtestCaseBoundResult);
        _xtestCaseBoundResult = null;
    }

    protected void xdestroyTestCaseContainer() {
        if (isUseOneTimeContainer() || isDestroyContainerAtTearDown()) {
            xdestroyContainer();
            xclearCachedContainer();
        }
    }

    /**
     * Does it destroy container instance at tear-down? (next test uses new-created container?)
     * @return The determination, true or false.
     */
    protected boolean isDestroyContainerAtTearDown() { // customize point
        return false; // default is to cache the instance
    }

    protected abstract void xclearCachedContainer();

    // ===================================================================================
    //                                                                   Component Binding
    //                                                                   =================
    protected ComponentBinder createBasicComponentBinder() { // customize point
        return new ComponentBinder(xcreateComponentProvider());
    }

    protected ComponentProvider xcreateComponentProvider() {
        return new ComponentProvider() {
            public <COMPONENT> COMPONENT provideComponent(Class<COMPONENT> type) {
                return getComponent(type);
            }

            @SuppressWarnings("unchecked")
            public <COMPONENT> COMPONENT provideComponent(String name) {
                return (COMPONENT) getComponent(name);
            }

            public boolean existsComponent(Class<?> type) {
                return hasComponent(type);
            }

            public boolean existsComponent(String name) {
                return hasComponent(name);
            }
        };
    }

    protected ComponentBinder xcreateTestCaseComponentBinder() { // customize point
        final ComponentBinder componentBinder = createBasicComponentBinder();
        componentBinder.stopBindingAtSuper(InjectionTestCase.class);
        return componentBinder;
    }

    /**
     * Register the mock instance for injection.
     * <pre>
     * FooAction action = new FooAction();
     * <span style="color: #FD4747">registerMockInstance</span>(new FooBhv());
     * inject(action); <span style="color: #3F7E5E">// the new-created behavior is injected</span>
     * </pre>
     * @param mock The mock instance injected to component. (NotNull)
     */
    public void registerMockInstance(Object mock) { // user method
        assertNotNull(mock);
        if (_xmockInstanceList == null) {
            _xmockInstanceList = new ArrayList<Object>();
        }
        _xmockInstanceList.add(mock);
    }

    /**
     * Suppress the binding of the type for injection.
     * <pre>
     * FooAction action = new FooAction();
     * <span style="color: #FD4747">suppressBindingOf</span>(FooBhv.class);
     * inject(action); <span style="color: #3F7E5E">// not injected about the behavior type</span>
     * </pre>
     * @param nonBindingType The non-binding type NOT injected to component. (NotNull)
     */
    public void suppressBindingOf(Class<?> nonBindingType) { // user method
        assertNotNull(nonBindingType);
        if (_xnonBindingTypeList == null) {
            _xnonBindingTypeList = new ArrayList<Class<?>>();
        }
        _xnonBindingTypeList.add(nonBindingType);
    }

    /**
     * Inject dependencies for the bean.
     * <pre>
     * FooAction action = new FooAction();
     * <span style="color: #FD4747">inject</span>(action);
     * 
     * action.submit();
     * ...
     * </pre>
     * @param bean The instance of bean. (NotNull)
     * @return The information of bound result. (NotNull)
     */
    protected BoundResult inject(Object bean) { // user method
        return xdoInject(bean, xcreateOuterComponentBinder(bean));
    }

    protected ComponentBinder xcreateOuterComponentBinder(Object bean) { // customize point
        final ComponentBinder componentBinder = createBasicComponentBinder();
        componentBinder.suppressPrivateBinding();
        // adjust mock components
        final List<Object> mockInstanceList = newArrayList();
        if (_xmockInstanceList != null) {
            mockInstanceList.addAll(_xmockInstanceList);
        }
        prepareMockInstance(mockInstanceList);
        for (Object mockInstance : mockInstanceList) {
            if (mockInstance == bean) { // check instance so uses '=='
                continue;
            }
            if (isInjectionTargetMock(mockInstance)) {
                inject(mockInstance);
            }
            componentBinder.addMockInstance(mockInstance);
        }
        // adjust no binding components
        final List<Class<?>> nonBindingTypeList = newArrayList();
        if (_xnonBindingTypeList != null) {
            nonBindingTypeList.addAll(_xnonBindingTypeList);
        }
        prepareNoBindingType(nonBindingTypeList);
        for (Class<?> nonBindingType : nonBindingTypeList) {
            componentBinder.addNonBindingType(nonBindingType);
        }
        return componentBinder;
    }

    protected boolean isInjectionTargetMock(Object mockInstance) {
        return !(mockInstance instanceof Mocklet);
    }

    /**
     * @param mockInstanceList The list of mock instance. (NotNull)
     * @deprecated You can use registerMockInstance().
     */
    protected void prepareMockInstance(List<Object> mockInstanceList) { // option by overriding
    }

    /**
     * @param nonBindingTypeList The list of non-binding type. (NotNull)
     * @deprecated You can suppressBindingOf().
     */
    protected void prepareNoBindingType(List<Class<?>> nonBindingTypeList) { // option by overriding
    }

    protected BoundResult xdoInject(Object bean, ComponentBinder componentBinder) {
        return componentBinder.bindComponent(bean);
    }

    // ===================================================================================
    //                                                                  Container Handling
    //                                                                  ==================
    protected abstract void xdestroyContainer();

    /**
     * Get component from DI container for the type.
     * @param type The type of component to find. (NotNull)
     * @return The instance of the component. (NotNull: if not found, throws exception)
     */
    protected abstract <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type); // user method

    /**
     * Get component from DI container for the name.
     * @param name The name of component to find. (NotNull)
     * @return The instance of the component. (NotNull: if not found, throws exception)
     */
    protected abstract <COMPONENT> COMPONENT getComponent(String name); // user method

    /**
     * Does it have the component on the DI container for the type.
     * @param type The type of component to find. (NotNull)
     * @return The determination, true or false.
     */
    protected abstract boolean hasComponent(Class<?> type); // user method

    /**
     * Does it have the component on the DI container for the name.
     * @param name The name of component to find. (NotNull)
     * @return The determination, true or false.
     */
    protected abstract boolean hasComponent(String name); // user method
}
