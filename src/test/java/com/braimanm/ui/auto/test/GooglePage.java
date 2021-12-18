package com.braimanm.ui.auto.test;

import com.braimanm.ui.auto.pagecomponent.PageObject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.openqa.selenium.support.FindBy;
import com.braimanm.ui.auto.components.WebComponent;

@XStreamAlias("google-page")
public class GooglePage extends PageObject {
    @FindBy(css = "input[name=q]")
    private WebComponent search;
    @FindBy(css = "input[name=q]")
    private WebComponent search2;


    public void search() {
        setElementValue(search);
    }

    public void search2() {
        setElementValue(search2);
    }

}
