/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.selenium.browserstack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.AbstractDesiredCapabilitiesConfigurer;
import org.vividus.selenium.event.WebDriverQuitEvent;

public class BrowserStackCapabilitiesConfigurer extends AbstractDesiredCapabilitiesConfigurer
{
    private final IBddRunContext bddRunContext;
    private final BrowserStackLocalManager browserStackLocalManager;
    private boolean browserStackEnabled;
    private boolean browserStackLocalEnabled;

    public BrowserStackCapabilitiesConfigurer(IBddRunContext bddRunContext,
            BrowserStackLocalManager browserStackLocalManager)
    {
        this.bddRunContext = bddRunContext;
        this.browserStackLocalManager = browserStackLocalManager;
    }

    @Override
    public void configure(DesiredCapabilities desiredCapabilities)
    {
        if (browserStackEnabled)
        {
            Proxy proxy = (Proxy) desiredCapabilities.getCapability(CapabilityType.PROXY);
            if (browserStackLocalEnabled || proxy != null)
            {
                try
                {
                    if (proxy != null)
                    {
                        String localIdentifier = browserStackLocalManager.start(proxy.getHttpProxy());
                        putBstackOption(desiredCapabilities, "localIdentifier", localIdentifier);
                        desiredCapabilities.setCapability(CapabilityType.PROXY, (Object) null);
                    }
                    else
                    {
                        browserStackLocalManager.start();
                    }
                    putBstackOption(desiredCapabilities, "local", true);
                }
                catch (IOException e)
                {
                    throw new UncheckedIOException(e);
                }
            }

            Optional.ofNullable(bddRunContext.getRootRunningStory())
                    .map(RunningStory::getName)
                    .ifPresent(name -> putBstackOption(desiredCapabilities, "sessionName", name));
        }
    }

    @Subscribe
    public void stopSauceConnect(WebDriverQuitEvent event)
    {
        browserStackLocalManager.stop();
    }

    private void putBstackOption(DesiredCapabilities desiredCapabilities, String key, Object value)
    {
        putNestedCapability(desiredCapabilities, "bstack:options", key, value);
    }

    public void setBrowserStackEnabled(boolean browserStackEnabled)
    {
        this.browserStackEnabled = browserStackEnabled;
    }

    public void setBrowserStackLocalEnabled(boolean browserStackLocalEnabled)
    {
        this.browserStackLocalEnabled = browserStackLocalEnabled;
    }
}
