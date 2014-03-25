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
package org.seasar.dbflute.unit.seasar.recycle;

import org.seasar.dbflute.unit.seasar.ContainerTestCase;

/**
 * @author jflute
 * @since 0.4.1 (2014/03/25 Tuesday)
 */
public class Recycle3rdConfigChangeTest extends ContainerTestCase {

    @Override
    protected String prepareConfigFile() {
        return "unitbean.dicon";
    }

    public void test_first() throws Exception {
        // confirm log
    }

    public void test_second() throws Exception {
        // confirm log
    }
}
