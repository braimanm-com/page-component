package com.braimanm.ui.auto.test;

import com.braimanm.ui.auto.pagecomponent.PageObject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.openqa.selenium.support.FindBy;
import com.braimanm.ui.auto.components.WebComponent;

@XStreamAlias("Data-set-3")
public class DataSet3 extends PageObject {
    @FindBy(xpath = "locator")
    public WebComponent component;
}
