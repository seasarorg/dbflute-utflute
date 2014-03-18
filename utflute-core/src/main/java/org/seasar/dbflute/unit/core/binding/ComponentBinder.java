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
import java.util.Map;

import javax.annotation.Resource;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class ComponentBinder {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ComponentProvider _componentProvider;
    protected final BindingRuleProvider _bindingAnnotationProvider;
    protected final Map<Class<? extends Annotation>, BindingAnnotationRule> _bindingAnnotationRuleMap;
    protected Class<?> _terminalSuperClass;
    protected boolean _annotationOnlyBinding; // e.g. for Guice
    protected boolean _byTypeInterfaceOnly; // e.g. for Seasar
    protected boolean _looseBinding; // for test-case class
    protected final List<Object> _mockInstanceList = DfCollectionUtil.newArrayList();
    protected final List<Class<?>> _nonBindingTypeList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ComponentBinder(ComponentProvider componentProvider, BindingRuleProvider bindingAnnotationProvider) {
        _componentProvider = componentProvider;
        _bindingAnnotationProvider = bindingAnnotationProvider;
        _bindingAnnotationRuleMap = _bindingAnnotationProvider.provideBindingAnnotationRuleMap(); // cached
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void stopBindingAtSuper(Class<?> terminalSuperClass) {
        _terminalSuperClass = terminalSuperClass;
    }

    public void annotationOnlyBinding() {
        _annotationOnlyBinding = true;
    }

    public void cancelAnnotationOnlyBinding() {
        _annotationOnlyBinding = false;
    }

    public void byTypeInterfaceOnly() {
        _byTypeInterfaceOnly = true;
    }

    public void cancelByTypeInterfaceOnly() {
        _byTypeInterfaceOnly = false;
    }

    public void looseBinding() {
        _looseBinding = true;
    }

    public void cancelLooseBinding() {
        _looseBinding = false;
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
        final Annotation bindingAnno = findBindingAnnotation(field); // might be null
        if (bindingAnno != null || _looseBinding) {
            field.setAccessible(true);
            final Class<?> fieldType = field.getType();
            if (isNonBindingType(fieldType)) {
                return;
            }
            if (isNonBindingAnnotation(bindingAnno)) {
                return;
            }
            if (getFieldValue(field, bean) != null) {
                return;
            }
            final Object component = findInjectedComponent(field.getName(), fieldType, bindingAnno);
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
        final Class<?> propertyType = propertyDesc.getPropertyType();
        if (isNonBindingType(propertyType)) {
            return;
        }
        final Method writeMethod = propertyDesc.getWriteMethod();
        if (writeMethod == null) { // public field
            return; // unsupported fixedly
        }
        final Annotation bindingAnno = findBindingAnnotation(writeMethod); // might be null
        if (_annotationOnlyBinding && bindingAnno == null) {
            return; // e.g. Guice needs annotation to setter
        }
        if (isNonBindingAnnotation(bindingAnno)) {
            return;
        }
        if (!isBindTargetClass(writeMethod.getDeclaringClass())) {
            return;
        }
        if (propertyDesc.isReadable() && propertyDesc.getValue(bean) != null) {
            return;
        }
        final Object component = findInjectedComponent(propertyName, propertyType, bindingAnno);
        if (component == null) {
            return;
        }
        propertyDesc.setValue(bean, component);
        boundResult.addBoundProperty(propertyDesc);
    }

    // -----------------------------------------------------
    //                                        Find Component
    //                                        --------------
    protected Object findInjectedComponent(String propertyName, Class<?> propertyType, Annotation bindingAnno) {
        Object component = findMockInstance(propertyType);
        if (component != null) {
            return component;
        }
        if (isFindingByNameOnlyProperty(propertyName, propertyType, bindingAnno)) {
            return doFindInjectedComponentByName(propertyName, propertyType, bindingAnno);
        }
        if (hasComponent(propertyType)) {
            component = getComponent(propertyType);
        }
        if (component != null) {
            return component;
        }
        if (isByTypeOnlyAnnotation(bindingAnno)) {
            return null;
        }
        return doFindInjectedComponentByName(propertyName, propertyType, bindingAnno);
    }

    protected boolean isFindingByNameOnlyProperty(String propertyName, Class<?> propertyType, Annotation bindingAnno) {
        if (_looseBinding) {
            return false;
        }
        if (isByNameOnlyAnnotation(bindingAnno)) {
            return true;
        }
        if (extractSpecifiedName(bindingAnno) != null) {
            return true;
        }
        if (isLimitedPropertyAsByTypeInterfaceOnly(propertyName, propertyType)) {
            return true;
        }
        return false;
    }

    protected boolean isLimitedPropertyAsByTypeInterfaceOnly(String propertyName, Class<?> propertyType) {
        return _byTypeInterfaceOnly && !propertyType.isInterface();
    }

    protected Object doFindInjectedComponentByName(String propertyName, Class<?> propertyType, Annotation bindingAnno) {
        final String specifiedName = extractSpecifiedName(bindingAnno);
        final String realName;
        if (specifiedName != null) {
            realName = specifiedName;
        } else {
            final String normalized = normalizeName(propertyName);
            final String filtered = _bindingAnnotationProvider.filterByBindingNamingRule(normalized, propertyType);
            realName = filtered != null ? filtered : normalized;
        }
        return actuallyFindInjectedComponentByName(realName);
    }

    protected Object actuallyFindInjectedComponentByName(String name) {
        return hasComponent(name) ? getComponent(name) : null;
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
        if (_looseBinding) {
            return name.startsWith("_") ? name.substring("_".length()) : name;
        }
        return name;
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
        if (annotations == null || _bindingAnnotationRuleMap == null) { // just in case
            return null;
        }
        for (Annotation annotation : annotations) {
            if (_bindingAnnotationRuleMap.containsKey(annotation.annotationType())) {
                return annotation;
            }
        }
        return null;
    }

    protected boolean isNonBindingAnnotation(Annotation bindingAnno) {
        final BindingAnnotationRule rule = findBindingAnnotationRule(bindingAnno);
        if (rule == null) {
            return false;
        }
        final NonBindingDeterminer determiner = rule.getNonBindingDeterminer();
        return determiner != null && determiner.isNonBinding(bindingAnno);
    }

    protected boolean isByNameOnlyAnnotation(Annotation bindingAnno) {
        final BindingAnnotationRule rule = findBindingAnnotationRule(bindingAnno);
        return rule != null && rule.isByNameOnly();
    }

    protected boolean isByTypeOnlyAnnotation(Annotation bindingAnno) {
        final BindingAnnotationRule rule = findBindingAnnotationRule(bindingAnno);
        return rule != null && rule.isByTypeOnly();
    }

    protected BindingAnnotationRule findBindingAnnotationRule(Annotation bindingAnno) {
        return bindingAnno != null ? _bindingAnnotationRuleMap.get(bindingAnno.annotationType()) : null;
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
        if (bindingAnnotation instanceof Resource) { // only standard annotation here for now
            specifiedName = ((Resource) bindingAnnotation).name(); // might be empty string
        }
        return Srl.is_NotNull_and_NotTrimmedEmpty(specifiedName) ? specifiedName : null;
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
