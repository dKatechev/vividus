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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class BrowserStackCapabilitiesConfigurerTests
{
    private static final String BSTACK_KEY = "bstack:options";
    private static final String NAME = "name";
    private static final String SESSION_NAME = "sessionName";

    @Mock private IBddRunContext bddRunContext;
    @Mock private BrowserStackLocalManager browserStackLocalManager;
    @Spy private DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
    @InjectMocks private BrowserStackCapabilitiesConfigurer configurer;

    @Test
    void shouldNotConfigureCapabilitiesIfBrowserStackInDisabled()
    {
        configurer.setBrowserStackEnabled(false);
        configurer.configure(desiredCapabilities);
        verifyNoInteractions(bddRunContext, desiredCapabilities, browserStackLocalManager);
    }

    @Test
    void shouldUseBrowserStackLocal() throws IOException
    {
        configurer.setBrowserStackEnabled(true);
        configurer.setBrowserStackLocalEnabled(true);
        String httpProxy = "localhost:59338";
        String localIdentifier = "local-identifier";

        Proxy proxy = mock(Proxy.class);
        RunningStory runningStory = mockRunningStory(NAME);

        when(desiredCapabilities.getCapability(CapabilityType.PROXY)).thenReturn(proxy);
        when(proxy.getHttpProxy()).thenReturn(httpProxy);
        when(browserStackLocalManager.start(httpProxy)).thenReturn(localIdentifier);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of(BSTACK_KEY, Map.of(
                SESSION_NAME, NAME,
                "localIdentifier", localIdentifier,
                "local", true
        )), desiredCapabilities.asMap());
        verifyNoMoreInteractions(bddRunContext, runningStory);
    }

    @Test
    void shouldAddRunningStoryNameAsSessionName()
    {
        configurer.setBrowserStackEnabled(true);

        RunningStory runningStory = mockRunningStory(NAME);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of(BSTACK_KEY, Map.of(SESSION_NAME, NAME)), desiredCapabilities.asMap());
        verifyNoMoreInteractions(bddRunContext, runningStory);
    }

    @Test
    void shouldNotAddRunningStoryNameItItsNull()
    {
        configurer.setBrowserStackEnabled(true);
        RunningStory runningStory = mockRunningStory(null);

        configurer.configure(desiredCapabilities);

        verifyNoMoreInteractions(bddRunContext, runningStory);
        verifyNoInteractions(desiredCapabilities);
    }

    private RunningStory mockRunningStory(String name)
    {
        RunningStory runningStory = mock(RunningStory.class);

        when(bddRunContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(name);

        return runningStory;
    }
}
