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
package org.seasar.dbflute.unit.mocklet.helper;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @param <ELEMENT> The type of element.
 * @author modified by jflute (originated in Seasar)
 */
public class MockletEmptyIterator<ELEMENT> implements Iterator<ELEMENT> {

    public MockletEmptyIterator() {
    }

    public void remove() {
        throw new IllegalStateException();
    }

    public boolean hasNext() {
        return false;
    }

    public ELEMENT next() {
        throw new NoSuchElementException();
    }
}