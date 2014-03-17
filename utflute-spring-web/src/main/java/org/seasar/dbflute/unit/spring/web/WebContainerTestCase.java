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
package org.seasar.dbflute.unit.spring.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequest;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequestImpl;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponse;
import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponseImpl;
import org.seasar.dbflute.unit.mocklet.MockletServletConfig;
import org.seasar.dbflute.unit.mocklet.MockletServletConfigImpl;
import org.seasar.dbflute.unit.mocklet.MockletServletContext;
import org.seasar.dbflute.unit.mocklet.MockletServletContextImpl;
import org.seasar.dbflute.unit.spring.ContainerTestCase;

/**
 * @author jflute
 * @since 0.1.1 (2011/07/25 Monday)
 */
public abstract class WebContainerTestCase extends ContainerTestCase {

    @Override
    protected void xinitializeContainer(String[] configFiles) {
        super.xinitializeContainer(configFiles);
        final MockletServletConfig servletConfig = createMockletServletConfig();
        final MockletServletContext servletContext = createMockletServletContext();
        servletConfig.setServletContext(servletContext);
        xregisterWebMockContext(servletConfig, servletContext);
    }

    protected void xregisterWebMockContext(MockletServletConfig servletConfig, MockletServletContext servletContext) { // like RequestContextFilter
        final MockletHttpServletRequest request = createMockletHttpServletRequest(servletContext);
        final MockletHttpServletResponse response = createMockletHttpServletResponse(request);
        final HttpSession session = request.getSession(true);
        // I don't know how to set request and response to Spring DI system
        // so register them as mock instance for now
        // (but they cannot be injected to normal component)
        registerMockInstance(request);
        registerMockInstance(response);
        registerMockInstance(session);
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
