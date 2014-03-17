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
package org.seasar.dbflute.unit.guice.web;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.seasar.dbflute.unit.guice.ContainerTestCase;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequest;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequestImpl;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponse;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponseImpl;
import org.seasar.dbflute.unit.mocklet.MockletServletConfig;
import org.seasar.dbflute.unit.mocklet.MockletServletConfigImpl;
import org.seasar.dbflute.unit.mocklet.MockletServletContext;
import org.seasar.dbflute.unit.mocklet.MockletServletContextImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public abstract class WebContainerTestCase extends ContainerTestCase {

    // ===================================================================================
    //                                                                             Mocklet
    //                                                                             =======
    @Override
    protected List<Module> prepareModuleList() {
        final List<Module> moduleList = super.prepareModuleList();
        if (!isSuppressWebMock()) {
            final Module mockletModule = new Module() {
                public void configure(Binder binder) {
                    final MockletServletConfig servletConfig = createMockletServletConfig();
                    final MockletServletContext servletContext = createMockletServletContext();
                    xregisterWebMockContext(servletConfig, servletContext, binder);
                }
            };
            moduleList.add(mockletModule);
        }
        return moduleList;
    }

    /**
     * Does it suppress web mock? e.g. HttpServletRequest, HttpSession
     * @return The determination, true or false.
     */
    protected boolean isSuppressWebMock() {
        return false;
    }

    protected void xregisterWebMockContext(MockletServletConfig servletConfig, MockletServletContext servletContext,
            Binder binder) {
        final MockletHttpServletRequest request = createMockletHttpServletRequest(servletContext);
        final MockletHttpServletResponse response = createMockletHttpServletResponse(request);
        final HttpSession session = request.getSession(true);
        binder.bind(HttpServletRequest.class).toInstance(request);
        binder.bind(HttpServletResponse.class).toInstance(response);
        binder.bind(HttpSession.class).toInstance(session);
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
}
