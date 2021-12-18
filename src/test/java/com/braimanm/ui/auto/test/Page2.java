package com.braimanm.ui.auto.test;

import com.braimanm.ui.auto.pagecomponent.PageObject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.openqa.selenium.support.FindBy;
import com.braimanm.ui.auto.components.WebComponent;

@XStreamAlias("page2")
public class Page2 extends PageObject {
    @FindBy(xpath = "//page1")
    Page1 page1;
    @FindBy(xpath = "//comp3")
    WebComponent comp3;
}
