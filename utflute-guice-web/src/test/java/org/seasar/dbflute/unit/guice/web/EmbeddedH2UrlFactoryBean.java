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

import java.io.File;
import java.io.IOException;

import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfResourceUtil;

/**
 * The bean for resolving a path to a database of H2 Database. <br />
 * This is NOT an important class as example. So you don't need to read this.
 * @author jflute
 */
public class EmbeddedH2UrlFactoryBean {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _urlSuffix;
    protected String _referenceClassName;

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public Object getObject() throws Exception {
        return buildUrl();
    }

    public Class<?> getObjectType() {
        return String.class;
    }

    public boolean isSingleton() {
        return true;
    }

    protected String buildUrl() {
        try {
            final File buildDir = getBuildDir();
            final String canonicalPath = buildDir.getCanonicalPath();
            return "jdbc:h2:file:" + canonicalPath.replace('\\', '/') + _urlSuffix;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getBuildDir() {
        final Class<?> clazz = DfReflectionUtil.forName(_referenceClassName);
        return DfResourceUtil.getBuildDir(clazz);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setReferenceClassName(String referenceClassName) {
        _referenceClassName = referenceClassName;
    }

    public void setUrlSuffix(String urlSuffix) {
        _urlSuffix = urlSuffix;
    }
}
