/*
Copyright 2010-2021 Michael Braiman braimanm@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.braimanm.ui.auto.pagecomponent;


import net.sf.cglib.proxy.Enhancer;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.*;
import com.braimanm.ui.auto.data.ComponentData;
import com.braimanm.ui.auto.data.DataTypes;

import java.lang.reflect.Field;
import java.util.Map;

public class ComponentFieldDecorator extends DefaultFieldDecorator {
    PageObject page;

    public ComponentFieldDecorator(ElementLocatorFactory factory, PageObject page) {
        super(factory);
        this.page = page;
    }

    private By modifyLocator(ElementLocator locator, By parentLocator) {
        By bys = null;
        if (DefaultElementLocator.class.isAssignableFrom(locator.getClass())) {
            try {
                Field by = DefaultElementLocator.class.getDeclaredField("by");
                by.setAccessible(true);
                By childLocator = (By) by.get(locator);
                if (parentLocator != null) {
                    bys = new ByChained(parentLocator, childLocator);
                    by.set(locator, bys);
                } else {
                    bys = childLocator;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bys;
    }

    @Override
    public Object decorate(ClassLoader loader, final Field field) {
        String dataValue = null;
        String initialValue = null;
        String expectedValue = null;
        Map<String, String> customData = null;

        field.setAccessible(true);
        if (PageComponent.class.isAssignableFrom(field.getType())) {
            ElementLocator locator = factory.createLocator(field);
            if (locator == null) return null;

            By by = modifyLocator(locator, page.getLocator());

            ComponentData componentData;
            try {
                componentData = (ComponentData) field.get(page);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (componentData != null) {
                dataValue = componentData.getData(DataTypes.Data, false);
                initialValue = componentData.getData(DataTypes.Initial, false);
                expectedValue = componentData.getData(DataTypes.Expected, false);
                customData = componentData.getCustomData();
            }

            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(field.getType());
            enhancer.setCallback(new ComponentMethodInterceptor(locator));
            Object componentProxy = enhancer.create();
            ((ComponentData) componentProxy).initializeData(dataValue, initialValue, expectedValue);
            ((ComponentData) componentProxy).addCustomData(customData);
            ((PageComponent) componentProxy).fieldName = field.getName();
            ((PageComponent) componentProxy).selector = by;
            return componentProxy;
        }

        //PageObject as Web Component
        if (PageObject.class.isAssignableFrom(field.getType()) &&
                (
                        field.isAnnotationPresent(FindBy.class) ||
                        field.isAnnotationPresent(FindAll.class) ||
                        field.isAnnotationPresent(FindBys.class) ||
                        field.isAnnotationPresent(InitPage.class)
                )
        ) {
            PageObject po;
            try {
                po = (PageObject) field.get(page);
                if (po == null) {
                    Object value = field.getType().newInstance();
                    po = (PageObject) value;
                    field.set(page, value);
                    po.dataProvided = false;
                } else {
                    po.dataProvided = true;
                }
                po.ajaxIsUsed = page.ajaxIsUsed;
                if (!field.isAnnotationPresent(InitPage.class)) {
                    Annotations annotations = new Annotations(field);
                    po.locator = annotations.buildBy();
                }
                PageFactory.initElements(new ComponentFieldDecorator(factory, po), po);
                po.context = page.getContext();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return super.decorate(loader, field);
    }
}
