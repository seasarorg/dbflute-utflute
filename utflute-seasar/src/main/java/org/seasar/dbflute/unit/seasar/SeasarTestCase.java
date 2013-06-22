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
package org.seasar.dbflute.unit.seasar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.core.InjectionTestCase;
import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.framework.container.ComponentNotFoundRuntimeException;
import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.container.TooManyRegistrationRuntimeException;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.container.servlet.S2ContainerServlet;
import org.seasar.framework.env.Env;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletConfigImpl;
import org.seasar.framework.mock.servlet.MockServletContextImpl;

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
    private static String _preparedConfigFile; // static cache: null means beginning or test execution

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    protected void xsetupBeforeContainer() {
        super.xsetupBeforeContainer();
        xprepareUnitTestEnv();
    }

    @Override
    protected void xprepareTestCaseContainer() {
        final String configFile = prepareConfigFile();
        if (xisInitializedContainer()) {
            if (configFile.equals(_preparedConfigFile)) { // no change
                return; // no need to initialize
            } else { // changed
                xdestroyContainer(); // to re-initialize
            }
        }
        xinitializeContainer(configFile);
        _preparedConfigFile = configFile;
    }

    /**
     * @return The pure file name of root dicon. (NotNull)
     */
    protected String prepareConfigFile() { // customize point
        return "app.dicon"; // as default
    }

    @Override
    protected TransactionResource beginNewTransaction() { // user method
        final TransactionManager manager = getComponent(TransactionManager.class);
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

    @Override
    protected void xclearCachedContainer() {
        _preparedConfigFile = null;
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
            xdoInitializeContainerAsWeb(configFile);
        }
    }

    protected boolean isSuppressWebMock() {
        return false;
    }

    protected void xdoInitializeContainerAsLibrary(String configFile) {
        SingletonS2ContainerFactory.setConfigPath(configFile);
        SingletonS2ContainerFactory.init();
    }

    protected void xdoInitializeContainerAsWeb(String configFile) {
        final MockServletConfigImpl servletConfig = xcreateMockServletConfigImpl();
        servletConfig.setInitParameter(S2ContainerServlet.CONFIG_PATH_KEY, configFile);
        final MockServletContextImpl servletContext = xcreateMockServletContextImpl();
        servletConfig.setServletContext(servletContext);
        final S2ContainerServlet containerServlet = xcreateS2ContainerServlet();
        try {
            containerServlet.init(servletConfig);
        } catch (ServletException e) {
            String msg = "Failed to initialize servlet config to servlet: " + servletConfig;
            throw new IllegalStateException(msg, e.getRootCause());
        }
        xregisterWebMockContext(servletContext);
    }

    protected S2ContainerServlet xcreateS2ContainerServlet() {
        return new S2ContainerServlet();
    }

    protected MockServletConfigImpl xcreateMockServletConfigImpl() {
        return new MockServletConfigImpl();
    }

    protected MockServletContextImpl xcreateMockServletContextImpl() {
        return new MockServletContextImpl("utservlet");
    }

    protected void xregisterWebMockContext(ServletContext servletContext) { // like S2ContainerFilter
        final S2Container container = SingletonS2ContainerFactory.getContainer();
        final ExternalContext externalContext = container.getExternalContext();
        final MockHttpServletRequestImpl request = xcreateMockHttpServletRequestImpl(servletContext);
        final MockHttpServletResponseImpl response = xcreateMockHttpServletResponseImpl(request);
        externalContext.setRequest(request);
        externalContext.setResponse(response);
    }

    protected MockHttpServletRequestImpl xcreateMockHttpServletRequestImpl(ServletContext servletContext) {
        return new MockHttpServletRequestImpl(servletContext, xgetServletPath());
    }

    protected MockHttpServletResponseImpl xcreateMockHttpServletResponseImpl(HttpServletRequest request) {
        return new MockHttpServletResponseImpl(request);
    }

    protected String xgetServletPath() {
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
    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
        return SingletonS2Container.getComponent(type);
    }

    @SuppressWarnings("unchecked")
    protected <COMPONENT> COMPONENT getComponent(String name) { // user method
        return (COMPONENT) SingletonS2Container.getComponent(name);
    }

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

    protected boolean hasComponent(String name) { // user method
        try {
            SingletonS2Container.getComponent(name);
            return true;
        } catch (ComponentNotFoundRuntimeException e) {
            return false;
        }
    }
}
