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
package org.seasar.dbflute.unit.seasar;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.core.InjectionTestCase;
import org.seasar.dbflute.unit.core.binding.BindingAnnotationRule;
import org.seasar.dbflute.unit.core.binding.ComponentBinder;
import org.seasar.dbflute.unit.core.binding.NonBindingDeterminer;
import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequest;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequestImpl;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponse;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponseImpl;
import org.seasar.dbflute.unit.mocklet.MockletServletConfig;
import org.seasar.dbflute.unit.mocklet.MockletServletConfigImpl;
import org.seasar.dbflute.unit.mocklet.MockletServletContext;
import org.seasar.dbflute.unit.mocklet.MockletServletContextImpl;
import org.seasar.dbflute.util.Srl;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.container.TooManyRegistrationRuntimeException;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.servlet.S2ContainerServlet;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.env.Env;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public abstract class SeasarTestCase extends InjectionTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                      Container Object
    //                                      ----------------
    /** The cached configuration file for DI container. (NullAllowed: null means beginning or test execution) */
    protected static String _xpreparedConfigFile;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    // -----------------------------------------------------
    //                                      Before Container
    //                                      ----------------
    @Override
    protected void xsetupBeforeContainer() {
        super.xsetupBeforeContainer();
        xprepareUnitTestEnv();
    }

    // -----------------------------------------------------
    //                                     Prepare Container
    //                                     -----------------
    @Override
    protected void xprepareTestCaseContainer() {
        if (isUseOneTimeContainer()) {
            xdestroyContainer();
        }
        final String configFile = prepareConfigFile();
        if (xisInitializedContainer()) {
            if (configFile.equals(_xpreparedConfigFile)) { // no change
                return; // no need to initialize
            } else { // changed
                xdestroyContainer(); // to re-initialize
            }
        }
        xinitializeContainer(configFile);
        _xpreparedConfigFile = configFile;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isUseOneTimeContainer() {
        return false;
    }

    /**
     * Prepare configuration file as root for Seasar.
     * @return The pure file name of root dicon. (NotNull)
     */
    protected String prepareConfigFile() { // customize point
        return "app.dicon"; // as default
    }

    @Override
    protected void xclearCachedContainer() {
        _xpreparedConfigFile = null;
    }

    // ===================================================================================
    //                                                                         Transaction
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransactionResource beginNewTransaction() { // user method
        final Class<TransactionManager> managerType = TransactionManager.class;
        if (!hasComponent(managerType)) {
            return null;
        }
        final TransactionManager manager = getComponent(managerType);
        final Transaction suspendedTx;
        try {
            if (manager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                suspendedTx = manager.suspend(); // because Seasar's DBCP doesn't support nested transaction
            } else {
                suspendedTx = null;
            }
        } catch (SystemException e) {
            throw new TransactionFailureException("Failed to suspend current", e);
        }
        TransactionResource resource = null;
        try {
            manager.begin();
            resource = new TransactionResource() {
                public void commit() {
                    try {
                        manager.commit();
                    } catch (Exception e) {
                        throw new TransactionFailureException("Failed to commit the transaction.", e);
                    } finally {
                        xresumeSuspendedTxQuietly(manager, suspendedTx);
                    }
                }

                public void rollback() {
                    try {
                        manager.rollback();
                    } catch (Exception e) {
                        throw new TransactionFailureException("Failed to roll-back the transaction.", e);
                    } finally {
                        xresumeSuspendedTxQuietly(manager, suspendedTx);
                    }
                }
            }; // for thread-fire's transaction or manual transaction
        } catch (NotSupportedException e) {
            throw new TransactionFailureException("Failed to begin new transaction.", e);
        } catch (SystemException e) {
            throw new TransactionFailureException("Failed to begin new transaction.", e);
        }
        return resource;
    }

    protected void xresumeSuspendedTxQuietly(TransactionManager manager, Transaction suspendedTx) {
        try {
            if (suspendedTx != null) {
                manager.resume(suspendedTx);
            }
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                   Component Binding
    //                                                                   =================
    @Override
    protected ComponentBinder createOuterComponentBinder(Object bean) {
        final ComponentBinder binder = super.createOuterComponentBinder(bean);
        binder.byTypeInterfaceOnly();
        return binder;
    }

    @Override
    protected Map<Class<? extends Annotation>, BindingAnnotationRule> xprovideBindingAnnotationRuleMap() {
        final Map<Class<? extends Annotation>, BindingAnnotationRule> ruleMap = newHashMap();
        ruleMap.put(Resource.class, new BindingAnnotationRule());
        ruleMap.put(Binding.class, new BindingAnnotationRule().determineNonBinding(new NonBindingDeterminer() {
            public boolean isNonBinding(Annotation bindingAnno) {
                return BindingType.NONE.equals(((Binding) bindingAnno).bindingType());
            }
        }));
        return ruleMap;
    }

    @Override
    protected String xfilterByBindingNamingRule(String propertyName, Class<?> propertyType) {
        // e.g. [root].logic.foo.bar.QuxLogic
        final String packageExp = Srl.substringLastFront(propertyType.getName(), ".");
        final NamingConvention convention = getComponent(NamingConvention.class);
        try {
            final String componentName = convention.fromClassNameToComponentName(propertyType.getName());
            if (componentName.endsWith(propertyName)) { // e.g. foo_bar_quxLogic ends with quxLogic
                return componentName;
            }
            // e.g. foo_bar_quxLogic ends with quxService but the class exists in container
        } catch (RuntimeException ignored) { // unfortunately no exception that means not found explicitly
            // the class does not exist in container
        }
        // deriving component name (might be already unnecessary...but just in case)
        final String[] packageNames = convention.getRootPackageNames();
        for (String rootPackage : packageNames) {
            if (packageExp.startsWith(rootPackage)) {
                continue;
            }
            // e.g. [root].logic.foo.bar -> logic.foo.bar -> foo.bar -> foo_bar -> foo_bar_QuxLogic
            if (packageExp.startsWith(rootPackage + ".")) {
                final String rearPackage = Srl.trim(Srl.substringFirstRear(packageExp, rootPackage), ".");
                final String nextRearPackage = Srl.substringFirstRear(rearPackage, "."); // remove e.g. logic
                final String packagePrefix = Srl.replace(nextRearPackage, ".", "_"); // foo.bar -> foo_bar
                return packagePrefix + "_" + propertyName; // e.g. foo_bar_QuxLogic
            }
        }
        return null;
    }

    // ===================================================================================
    //                                                                     Seasar Handling
    //                                                                     ===============
    protected void xprepareUnitTestEnv() {
        Env.setFilePath("env_ut.txt");
        Env.setValueIfAbsent("ut");
    }

    // -----------------------------------------------------
    //                                            Initialize
    //                                            ----------
    protected boolean xisInitializedContainer() {
        return SingletonS2ContainerFactory.hasContainer();
    }

    protected void xinitializeContainer(String configFile) {
        if (isSuppressWebMock()) { // library
            xdoInitializeContainerAsLibrary(configFile);
        } else { // web
            // Seasar contains web components as default
            xdoInitializeContainerAsWeb(configFile);
        }
    }

    /**
     * Does it suppress web mock? e.g. HttpServletRequest, HttpSession
     * @return The determination, true or false.
     */
    protected boolean isSuppressWebMock() {
        return false;
    }

    protected void xdoInitializeContainerAsLibrary(String configFile) {
        SingletonS2ContainerFactory.setConfigPath(configFile);
        SingletonS2ContainerFactory.init();
    }

    protected void xdoInitializeContainerAsWeb(String configFile) {
        final MockletServletConfig servletConfig = createMockletServletConfig();
        final MockletServletContext servletContext = createMockletServletContext();
        servletConfig.setServletContext(servletContext);
        servletConfig.setInitParameter(S2ContainerServlet.CONFIG_PATH_KEY, configFile);
        final S2ContainerServlet containerServlet = xcreateS2ContainerServlet();
        try {
            containerServlet.init(servletConfig);
        } catch (ServletException e) {
            String msg = "Failed to initialize servlet config to servlet: " + servletConfig;
            throw new IllegalStateException(msg, e.getRootCause());
        }
        xregisterWebMockContext(servletConfig, servletContext);
    }

    protected S2ContainerServlet xcreateS2ContainerServlet() {
        return new S2ContainerServlet();
    }

    protected void xregisterWebMockContext(MockletServletConfig servletConfig, MockletServletContext servletContext) { // like S2ContainerFilter
        final S2Container container = SingletonS2ContainerFactory.getContainer();
        final ExternalContext externalContext = container.getExternalContext();
        final MockletHttpServletRequest request = createMockletHttpServletRequest(servletContext);
        final MockletHttpServletResponse response = createMockletHttpServletResponse(request);
        externalContext.setRequest(request);
        externalContext.setResponse(response);
    }

    protected MockletServletConfig createMockletServletConfig() {
        return new MockletServletConfigImpl();
    }

    protected MockletServletContext createMockletServletContext() {
        return new MockletServletContextImpl("utservlet");
    }

    protected MockletHttpServletRequest createMockletHttpServletRequest(ServletContext servletContext) {
        return new MockletHttpServletRequestImpl(servletContext, prepareServletPath());
    }

    protected MockletHttpServletResponse createMockletHttpServletResponse(HttpServletRequest request) {
        return new MockletHttpServletResponseImpl(request);
    }

    protected String prepareServletPath() { // customize point
        return "/utflute";
    }

    // -----------------------------------------------------
    //                                               Destroy
    //                                               -------
    protected void xdestroyContainer() {
        SingletonS2ContainerFactory.destroy();
    }

    // -----------------------------------------------------
    //                                             Component
    //                                             ---------
    /**
     * {@inheritDoc}
     */
    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
        return SingletonS2Container.getComponent(type);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected <COMPONENT> COMPONENT getComponent(String name) { // user method
        return (COMPONENT) SingletonS2Container.getComponent(name);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean hasComponent(Class<?> type) { // user method
        try {
            SingletonS2Container.getComponent(type);
            return true;
        } catch (ComponentNotFoundRuntimeException e) {
            return false;
        } catch (TooManyRegistrationRuntimeException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean hasComponent(String name) { // user method
        try {
            SingletonS2Container.getComponent(name);
            return true;
        } catch (ComponentNotFoundRuntimeException e) {
            return false;
        }
    }
}
