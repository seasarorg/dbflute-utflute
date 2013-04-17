package org.seasar.dbflute.unit.core;

import java.util.List;

import org.seasar.dbflute.unit.core.binding.BoundResult;
import org.seasar.dbflute.unit.core.binding.ComponentBinder;
import org.seasar.dbflute.unit.core.binding.ComponentProvider;
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
    private TransactionResource _testCaseTransactionResource;

    // -----------------------------------------------------
    //                                     Component Binding
    //                                     -----------------
    private final ComponentBinder _testCaseComponentBinder = xcreateTestCaseComponentBinder();
    private BoundResult _testCaseBoundResult;

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

    protected void xprepareTestCaseComponent() {
        _testCaseBoundResult = _testCaseComponentBinder.bindComponent(this);
    }

    protected void xbeginTestCaseTransaction() {
        if (isSuppressTestCaseTransaction()) {
            return;
        }
        _testCaseTransactionResource = beginTransaction();
    }

    protected boolean isSuppressTestCaseTransaction() { // option by overriding
        return false; // default is to use the transaction
    }

    @Override
    public void tearDown() throws Exception {
        xrollbackTestCaseTransaction();
        xdestroyTestCaseComponent();
        xdestroyTestCaseContainer();
        super.tearDown();
    }

    protected void xrollbackTestCaseTransaction() {
        if (isSuppressTestCaseTransaction()) {
            return;
        }
        if (isCommitTestCaseTransaction()) {
            commitTransaction(_testCaseTransactionResource);
        } else {
            rollbackTransaction(_testCaseTransactionResource);
        }
        _testCaseTransactionResource = null;
    }

    protected boolean isCommitTestCaseTransaction() {
        return false; // default is to roll-back always
    }

    @Override
    protected void commitTransaction(TransactionResource resource) { // user method
        xassertTransactionResourceNotNull(resource);
        try {
            resource.commit();
        } catch (Exception e) {
            String msg = "Failed to commit the transaction.";
            throw new IllegalStateException(msg, e);
        }
    }

    @Override
    protected void rollbackTransaction(TransactionResource resource) { // user method
        xassertTransactionResourceNotNull(resource);
        try {
            resource.rollback();
        } catch (Exception e) {
            String msg = "Failed to roll-back the transaction.";
            throw new IllegalStateException(msg, e);
        }
    }

    protected void xdestroyTestCaseComponent() {
        _testCaseComponentBinder.unbindComponent(this, _testCaseBoundResult);
        _testCaseBoundResult = null;
    }

    protected void xdestroyTestCaseContainer() {
        if (isDestroyContainerAtTearDown()) {
            xdestroyContainer();
            xclearCachedContainer();
        }
    }

    protected boolean isDestroyContainerAtTearDown() { // option by overriding
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
     * Inject dependencies for the bean.
     * <pre>
     * FooAction action = new FooAction();
     * inject(action);
     * </pre>
     * @param bean The instance of bean. (NotNull)
     * @return The information of bound result. (NotNull)
     */
    protected BoundResult inject(Object bean) { // user method
        return xdoInject(bean, xcreateOuterComponentBinder());
    }

    protected ComponentBinder xcreateOuterComponentBinder() { // customize point
        final ComponentBinder componentBinder = createBasicComponentBinder();
        componentBinder.suppressPrivateBinding();
        // adjust mock components
        final List<Object> mockInstanceList = newArrayList();
        prepareMockInstance(mockInstanceList);
        for (Object mockInstance : mockInstanceList) {
            componentBinder.addMockInstance(mockInstance);
        }
        // adjust no binding components
        final List<Class<?>> noBindingTypeList = newArrayList();
        prepareNoBindingType(noBindingTypeList);
        for (Class<?> noBindingType : noBindingTypeList) {
            componentBinder.addNoBindingType(noBindingType);
        }
        return componentBinder;
    }

    protected void prepareMockInstance(List<Object> mockInstanceList) { // option by overriding
    }

    protected void prepareNoBindingType(List<Class<?>> noBindTypeList) { // option by overriding
    }

    protected BoundResult xdoInject(Object bean, ComponentBinder componentBinder) {
        return componentBinder.bindComponent(bean);
    }

    // ===================================================================================
    //                                                                  Container Handling
    //                                                                  ==================
    protected abstract void xdestroyContainer();

    protected abstract <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type); // user method

    protected abstract <COMPONENT> COMPONENT getComponent(String name); // user method

    protected abstract boolean hasComponent(Class<?> type); // user method

    protected abstract boolean hasComponent(String name); // user method
}
