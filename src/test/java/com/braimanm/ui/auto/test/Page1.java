package com.braimanm.ui.auto.test;

import com.braimanm.ui.auto.pagecomponent.PageObject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.openqa.selenium.support.FindBy;
import com.braimanm.ui.auto.components.WebComponent;

@XStreamAlias("page1")
public class Page1 extends PageObject {
    @FindBy(xpath = "//comp1")
    WebComponent comp1;
    Page3 page3;
    @FindBy(css = "page4")
    Page3 page4;
}
