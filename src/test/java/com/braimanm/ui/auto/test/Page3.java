package com.braimanm.ui.auto.test;

import com.braimanm.ui.auto.pagecomponent.PageObject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.openqa.selenium.support.FindBy;
import com.braimanm.ui.auto.components.WebComponent;

@XStreamAlias("page3")
public class Page3 extends PageObject {
    @FindBy(css = "web3")
    WebComponent web3;
}
