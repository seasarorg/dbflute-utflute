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

import java.io.File;

import org.seasar.dbflute.unit.core.filesystem.FileLineHandler;
import org.seasar.dbflute.unit.core.policestory.javaclass.PoliceStoryJavaClassHandler;
import org.seasar.dbflute.unit.core.policestory.jspfile.PoliceStoryJspFileHandler;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class PoliceStoryTest extends PlainTestCase {

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public void test_policeStoryOfJavaClassChase_copyright() throws Exception {
        policeStoryOfJavaClassChase(new PoliceStoryJavaClassHandler() {
            public void handle(File srcFile, Class<?> clazz) {
                markHere("called");
                final StringBuilder sb = new StringBuilder();
                readLine(srcFile, "UTF-8", new FileLineHandler() {
                    public void handle(String line) {
                        sb.append(line).append(ln());
                    }
                });
                String text = sb.toString();
                log(clazz);
                assertContains(text, "Copyright 2004-2014");
            }
        });
        assertMarked("called");
    }

    public void test_policeStoryOfJspFileChase_notExists() throws Exception {
        try {
            policeStoryOfJspFileChase(new PoliceStoryJspFileHandler() {
                public void handle(File jspFile) {
                    fail();
                }
            });
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        }
    }
}
