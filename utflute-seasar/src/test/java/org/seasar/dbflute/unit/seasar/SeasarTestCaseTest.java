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

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class SeasarTestCaseTest extends SeasarTestCase {

    public void test_xcanUseComponentNameByBindingNamingRule_basic() throws Exception {
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar", "bar"));
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar", "foo_bar"));
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar_qux", "qux"));
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar_qux", "bar_qux"));
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar_qux", "foo_bar_qux"));
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "quxLogic"));
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "bar_quxLogic"));
        assertTrue(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "foo_bar_quxLogic"));

        assertFalse(xcanUseComponentNameByBindingNamingRule("bar", "bar")); // not smart deploy component
        assertFalse(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "Logic"));
        assertFalse(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "uxLogic"));
        assertFalse(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "ar_quxLogic"));
        assertFalse(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "oo_bar_quxLogic"));
        assertFalse(xcanUseComponentNameByBindingNamingRule("foo_bar_quxLogic", "_quxLogic"));
    }
}
