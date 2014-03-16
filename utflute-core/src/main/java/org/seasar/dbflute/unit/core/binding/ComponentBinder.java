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
package org.seasar.dbflute.unit.core.binding;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class ComponentBinder {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ComponentProvider _componentProvider;
    protected Class<?> _terminalSuperType;
    protected boolean _suppressPrivateBinding;
    protected boolean _limitBindBehaviorOnly;
    protected final List<Object> _mockInstanceList = DfCollectionUtil.newArrayList();
    protected final List<Class<?>> _nonBindingTypeList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ComponentBinder(ComponentProvider componentProvider) {
        _componentProvider = componentProvider;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void stopBindingAtSuper(Class<?> terminalSuperType) {
        _terminalSuperType = terminalSuperType;
    }

    public void suppressPrivateBinding() {
        _suppressPrivateBinding = true;
    }

    public void limitBindBehaviorOnly() {
        _limitBindBehaviorOnly = true;
    }

    public void addMockInstance(Object mockInstance) {
        if (mockInstance == null) {
            String msg = "The argument 'mockInstance' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        _mockInstanceList.add(mockInstance);
    }

    public void addNonBindingType(Class<?> nonBindingType) {
        if (nonBindingType == null) {
            String msg = "The argument 'nonBindingType' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        _nonBindingTypeList.add(nonBindingType);
    }

    // ===================================================================================
    //                                                                   Component Binding
    //                                                                   =================
    public BoundResult bindComponent(Object bean) {
        final BoundResult boundResult = new BoundResult();
        for (Class<?> clazz = bean.getClass(); isBindTarget(clazz); clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i) {
                doBindComponent(bean, fields[i], boundResult);
            }
        }
        return boundResult;
    }

    protected boolean isBindTarget(Class<?> clazz) {
        return clazz != null && clazz != _terminalSuperType;
    }

    protected void doBindComponent(Object bean, Field field, BoundResult boundResult) {
        if (!isAutoBindable(field)) {
        }
        field.setAccessible(true);
        if (getFieldValue(field, bean) != null) {
            return;
        }
        if (_limitBindBehaviorOnly && !isBehaviorField(field)) {
            return;
        }
        final Class<?> type = field.getType();
        if (isNonBindingType(type)) {
            return;
        }
        Object component = findMockInstance(type);
        if (component == null) {
            if (hasComponent(type)) {
                component = getComponent(type);
            } else {
                final String name = normalizeAsPropertyName(field.getName());
                if (hasComponent(name)) {
                    component = getComponent(name);
                }
            }
        }
        if (component != null) {
            setFieldValue(field, bean, component);
            boundResult.addBoundField(field);
        }
    }

    protected boolean isNonBindingType(Class<?> type) {
        final List<Class<?>> nonBindingTypeList = _nonBindingTypeList;
        for (Class<?> nonBindingType : nonBindingTypeList) {
            if (nonBindingType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    protected Object findMockInstance(Class<?> type) {
        final List<Object> mockInstanceList = _mockInstanceList;
        for (Object mockInstance : mockInstanceList) {
            if (type.isInstance(mockInstance)) {
                return mockInstance;
            }
        }
        return null;
    }

    protected boolean isBehaviorField(Field field) {
        return field.getName().endsWith("Bhv");
    }

    protected String normalizeAsPropertyName(String name) {
        return name.startsWith("_") ? name.substring("_".length()) : name;
    }

    protected boolean isAutoBindable(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers) && !field.getType().isPrimitive();
    }

    public void unbindComponent(Object bean, BoundResult boundResult) {
        final List<Field> boundFieldList = boundResult.getBoundFieldList();
        for (int i = 0; i < boundFieldList.size(); ++i) {
            Field field = boundFieldList.get(i);
            try {
                field.set(bean, null);
            } catch (Exception continued) {
                final Class<?> type = field.getType();
                final String name = field.getName();
                String msg = "Failed to unbind the field: " + type.getName() + "(" + name + ") -> " + continued;
                System.err.println(msg);
            }
        }
        boundFieldList.clear();
    }

    // ===================================================================================
    //                                                                        Field Helper
    //                                                                        ============
    protected Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalArgumentException e) {
            throwIllegalArgumentFieldGet(field, target, e);
            return null; // unreachable
        } catch (IllegalAccessException e) {
            throwIllegalAccessFieldGet(field, target, e);
            return null; // unreachable
        }
    }

    protected void throwIllegalArgumentFieldGet(Field field, Object target, IllegalArgumentException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal argument to get the field.");
        br.addItem("Field");
        br.addElement(field);
        br.addItem("Target");
        br.addElement(target);
        final String msg = br.buildExceptionMessage();
        throw new IllegalArgumentException(msg, e);
    }

    protected void throwIllegalAccessFieldGet(Field field, Object target, IllegalAccessException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal access to get the field.");
        br.addItem("Field");
        br.addElement(field);
        br.addItem("Target");
        br.addElement(target);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    protected void setFieldValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalArgumentException e) {
            throwIllegalArgumentFieldSet(field, target, value, e);
        } catch (IllegalAccessException e) {
            throwIllegalAccessFieldSet(field, target, value, e);
        }
    }

    protected void throwIllegalArgumentFieldSet(Field field, Object target, Object value, IllegalArgumentException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal argument to set the field.");
        br.addItem("Field");
        br.addElement(field);
        br.addItem("Target");
        br.addElement(target);
        br.addItem("Value");
        br.addElement(value != null ? value.getClass() : null);
        br.addElement(value);
        final String msg = br.buildExceptionMessage();
        throw new IllegalArgumentException(msg, e);
    }

    protected void throwIllegalAccessFieldSet(Field field, Object target, Object value, IllegalAccessException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal access to set the field.");
        br.addItem("Field");
        br.addElement(field);
        br.addItem("Target");
        br.addElement(target);
        br.addItem("Value");
        br.addElement(value != null ? value.getClass() : null);
        br.addElement(value);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    // ===================================================================================
    //                                                                       Bean Handling
    //                                                                       =============
    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return _componentProvider.provideComponent(type);
    }

    @SuppressWarnings("unchecked")
    protected <COMPONENT> COMPONENT getComponent(String name) {
        return (COMPONENT) _componentProvider.provideComponent(name);
    }

    protected boolean hasComponent(Class<?> type) {
        return _componentProvider.existsComponent(type);
    }

    protected boolean hasComponent(String name) {
        return _componentProvider.existsComponent(name);
    }
}
