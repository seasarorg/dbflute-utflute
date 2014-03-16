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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
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
    protected final Set<Class<? extends Annotation>> _bindingAnnotationSet;
    protected Class<?> _terminalSuperClass;
    protected boolean _looseInjection;
    protected boolean _byTypeInterfaceOnly;
    protected final List<Object> _mockInstanceList = DfCollectionUtil.newArrayList();
    protected final List<Class<?>> _nonBindingTypeList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ComponentBinder(ComponentProvider componentProvider) {
        _componentProvider = componentProvider;
        _bindingAnnotationSet = _componentProvider.getBindingAnnotationSet();
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void stopBindingAtSuper(Class<?> terminalSuperClass) {
        _terminalSuperClass = terminalSuperClass;
    }

    public void looseInjection() {
        _looseInjection = true;
    }

    public void byTypeInterfaceOnly() {
        _byTypeInterfaceOnly = true;
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
    // -----------------------------------------------------
    //                                                 Entry
    //                                                 -----
    public BoundResult bindComponent(Object bean) {
        final BoundResult boundResult = new BoundResult();
        doBindFieldComponent(bean, boundResult);
        doBindPropertyComponent(bean, boundResult);
        return boundResult;
    }

    // -----------------------------------------------------
    //                                         Field Binding
    //                                         -------------
    protected void doBindFieldComponent(Object bean, BoundResult boundResult) {
        for (Class<?> clazz = bean.getClass(); isBindTargetClass(clazz); clazz = clazz.getSuperclass()) {
            if (clazz == null) {
                break;
            }
            final Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                fireFieldBinding(bean, field, boundResult);
            }
        }
    }

    protected void fireFieldBinding(Object bean, Field field, BoundResult boundResult) {
        if (!isModifiersAutoBindable(field)) {
            return;
        }
        final Annotation bindingAnnotation = findBindingAnnotation(field);
        if (bindingAnnotation != null || _looseInjection) {
            field.setAccessible(true);
            if (getFieldValue(field, bean) != null) {
                return;
            }
            final Class<?> type = field.getType();
            if (isNonBindingType(type)) {
                return;
            }
            final String specifiedName = extractSpecifiedName(bindingAnnotation);
            final Object component = findInjectedComponent(field.getName(), type, specifiedName);
            if (component != null) {
                setFieldValue(field, bean, component);
                boundResult.addBoundField(field);
            }
        }
    }

    protected boolean isModifiersAutoBindable(Field field) {
        final int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers) && !field.getType().isPrimitive();
    }

    // -----------------------------------------------------
    //                                      Property Binding
    //                                      ----------------
    protected void doBindPropertyComponent(Object bean, BoundResult boundResult) {
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(bean.getClass());
        final List<String> proppertyNameList = beanDesc.getProppertyNameList();
        for (String propertyName : proppertyNameList) {
            firePropertyBinding(bean, beanDesc, propertyName, boundResult);
        }
    }

    protected void firePropertyBinding(Object bean, DfBeanDesc beanDesc, String propertyName, BoundResult boundResult) {
        final DfPropertyDesc propertyDesc = beanDesc.getPropertyDesc(propertyName);
        if (!propertyDesc.isWritable()) {
            return;
        }
        if (propertyDesc.isReadable() && propertyDesc.getValue(bean) != null) {
            return;
        }
        final Class<?> propertyType = propertyDesc.getPropertyType();
        if (isNonBindingType(propertyType)) {
            return;
        }
        final Method readMethod = propertyDesc.getReadMethod();
        if (readMethod != null && !isBindTargetClass(readMethod.getDeclaringClass())) {
            return;
        }
        final Method writeMethod = propertyDesc.getWriteMethod();
        if (writeMethod != null && !isBindTargetClass(writeMethod.getDeclaringClass())) {
            return;
        }
        String specifiedName = null;
        if (readMethod != null) {
            final Annotation annotation = findBindingAnnotation(readMethod);
            specifiedName = extractSpecifiedName(annotation);
        }
        if (specifiedName == null && writeMethod != null) {
            final Annotation annotation = findBindingAnnotation(writeMethod);
            specifiedName = extractSpecifiedName(annotation);
        }
        final Object component = findInjectedComponent(propertyName, propertyType, specifiedName);
        if (component == null) {
            return;
        }
        propertyDesc.setValue(bean, component);
        boundResult.addBoundProperty(propertyDesc);
    }

    // -----------------------------------------------------
    //                                        Find Component
    //                                        --------------
    protected Object findInjectedComponent(String propertyName, Class<?> propertyType, String specifiedName) {
        Object component = findMockInstance(propertyType);
        if (component != null) {
            return component;
        }
        if (hasComponent(propertyType)) {
            component = getComponent(propertyType);
        }
        if (component != null) {
            return component;
        }
        if (_byTypeInterfaceOnly && propertyType.isInterface()) {
            return null;
        }
        final String name = specifiedName != null ? specifiedName : normalizeName(propertyName);
        if (hasComponent(name)) {
            component = getComponent(name);
        }
        return component;
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

    protected String normalizeName(String name) {
        return name.startsWith("_") ? name.substring("_".length()) : name;
    }

    // -----------------------------------------------------
    //                                  Injection Annotation
    //                                  --------------------
    protected Annotation findBindingAnnotation(Field field) {
        return doFindBindingAnnotation(field.getAnnotations());
    }

    protected Annotation findBindingAnnotation(Method method) {
        return doFindBindingAnnotation(method.getAnnotations());
    }

    protected Annotation doFindBindingAnnotation(Annotation[] annotations) {
        if (annotations == null || _bindingAnnotationSet == null) { // just in case
            return null;
        }
        for (Annotation annotation : annotations) {
            if (_bindingAnnotationSet.contains(annotation.annotationType())) {
                return annotation;
            }
        }
        return null;
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected boolean isBindTargetClass(Class<?> clazz) {
        return _terminalSuperClass == null || !clazz.isAssignableFrom(_terminalSuperClass);
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

    protected String extractSpecifiedName(Annotation bindingAnnotation) {
        String specifiedName = null;
        if (bindingAnnotation instanceof Resource) {
            specifiedName = ((Resource) bindingAnnotation).name();
        }
        return specifiedName;
    }

    // -----------------------------------------------------
    //                                       Release Binding
    //                                       ---------------
    public void releaseBoundComponent(Object bean, BoundResult boundResult) {
        final List<Field> boundFieldList = boundResult.getBoundFieldList();
        for (Field field : boundFieldList) {
            try {
                field.set(bean, null);
            } catch (Exception ignored) {
            }
        }
        boundFieldList.clear();
        final List<DfPropertyDesc> boundPropertyList = boundResult.getBoundPropertyList();
        for (DfPropertyDesc propertyDesc : boundPropertyList) {
            try {
                propertyDesc.setValue(bean, null);
            } catch (Exception ignored) {
            }
        }
        boundPropertyList.clear();
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
