/*
Copyright 2010-2012 Michael Braiman

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

package ui.auto.core.pagecomponent;

import java.lang.reflect.Field;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.AjaxElementLocator;
import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.SystemClock;

public class AjaxVisibleElementLocator extends AjaxElementLocator {
	
	public AjaxVisibleElementLocator(SearchContext searchContext, Field field, int timeOutInSeconds) {
		super(new SystemClock(), searchContext, field, timeOutInSeconds);
	}
	
	public AjaxVisibleElementLocator(Clock clock, WebDriver driver,Field field, int timeOutInSeconds) {
		super(clock, driver, field, timeOutInSeconds);
	}

	public AjaxVisibleElementLocator(WebDriver driver, Field field,int timeOutInSeconds) {
		super(driver, field, timeOutInSeconds);
	}

	@Override
	protected boolean isElementUsable(WebElement element) {
		try{
			return element.isDisplayed();
		} catch (Exception e){
			return false;
		}
	}

	@Override
	protected long sleepFor() {
		return 100;
	}
	
	
	
}
