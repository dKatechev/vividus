/*
 * Copyright 2019-2019-2020 the original author or authors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.browserstack.local.Local;

import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.proxy.util.ProxyUtils;
import org.vividus.testcontext.TestContext;

public class BrowserStackLocalManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserStackLocalManager.class);

    private static final Object KEY = BrowserStackLocalDescriptor.class;

    private String sauceLabsAccessKey;
    private Local local;
    private TestContext testContext;

    private final AtomicInteger atomicInteger = new AtomicInteger();
    private final Object monitor = new Object();

    public BrowserStackLocalManager(String sauceLabsAccessKey, Local local, TestContext testContext)
    {
        this.sauceLabsAccessKey = sauceLabsAccessKey;
        this.local = local;
        this.testContext = testContext;
    }

    
    
    
    
    
    
    
    private final Map<BrowserStackLocalOptions, BrowserStackLocalConnectionDescriptor> activeConnections = new HashMap<>();

    public void start(BrowserStackLocalOptions options)
    {
        BrowserStackLocalConnectionDescriptor currentDescriptor = getCurrentDescriptor();
        if (currentDescriptor == null)
        {
            currentDescriptor = activeConnections.get(options);
            if(currentDescriptor == null)
            {
                synchronized (activeConnections)
                {
                    currentDescriptor = activeConnections.get(options);
                    if (currentDescriptor == null)
                    {
                        currentDescriptor = new BrowserStackLocalConnectionDescriptor();
                        putCurrentDescriptor(options, currentDescriptor);
                    }
                }
            }
        }
    }

    private BrowserStackLocalConnectionDescriptor getCurrentDescriptor()
    {
        return testContext.get(KEY);
    }

    private void putCurrentDescriptor(BrowserStackLocalOptions key, BrowserStackLocalConnectionDescriptor descriptor)
    {
        testContext.put(key, descriptor);
    }

    private static class BrowserStackLocalConnectionDescriptor
    {
        private final String localIdentifier;
        private final AtomicInteger connectionsCount;

        public BrowserStackLocalConnectionDescriptor()
        {
            this.localIdentifier = UUID.randomUUID().toString();
            this.connectionsCount = new AtomicInteger(0);
        }

        public String getLocalIdentifier()
        {
            return localIdentifier;
        }

        public AtomicInteger getConnectionsCount()
        {
            return connectionsCount;
        }
    }
    
    
    
    
    
    
    
    
    public void start()
    {
        if (atomicInteger.get() == 0)
        {
            synchronized (monitor)
            {
                if (atomicInteger.get() == 0)
                {
                    Map<String, String> parameters = Map.of(
                        "forcelocal", "true",
                        "key", sauceLabsAccessKey
                    );
                    LOGGER.atInfo().addArgument(parameters).log("Starting shared BrowserStack Local");
                    wrapInvocation(() -> local.start(parameters));
                    atomicInteger.incrementAndGet();
                }
            }
        }
    }

    public String start(String httpProxy) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        Map<String, String> parameters = Map.of(
            "localIdentifier", uuid,
            "forcelocal", "true",
            "key", sauceLabsAccessKey,
            "-pac-file", ProxyUtils.createPacFile(uuid, httpProxy, List.of("*.browserstack.com.*")).toString()
        );

        testContext.put(KEY, new BrowserStackLocalDescriptor(parameters));
        synchronized (local)
        {
            LOGGER.atInfo().addArgument(parameters).log("Starting BrowserStack Local for {}");
            wrapInvocation(() -> local.start(parameters));
        }

        return uuid;
    }

    public void stop()
    {
        BrowserStackLocalDescriptor descriptor = testContext.get(KEY);
        if (descriptor != null)
        {
            synchronized (local)
            {
                LOGGER.atInfo().addArgument(descriptor::getParameters).log("Shutting down BrowserStack Local for {}");
                wrapInvocation(() -> local.stop(descriptor.getParameters()));
            }
        }
        else
        {
            if (atomicInteger.get() != 0)
            {
                synchronized (monitor)
                {
                    if (atomicInteger.get() != 0)
                    {
                        int connections = atomicInteger.decrementAndGet();
                        if (connections == 0)
                        {
                            LOGGER.atInfo().log("Shutting down shared BrowserStack Local");
                            wrapInvocation(local::stop);
                        }
                    }
                }
            }
        }
    }

    private static void wrapInvocation(FailableRunnable<Exception> runnable)
    {
        try
        {
            runnable.run();
        }
        catch (Exception exception)
        {
            throw new BrowserStackLocalException(exception);
        }
    }

    private class BrowserStackLocalDescriptor
    {
        private final Map<String, String> parameters;

        BrowserStackLocalDescriptor(Map<String, String> parameters)
        {
            this.parameters = parameters;
        }

        Map<String, String> getParameters()
        {
            return parameters;
        }
    }
}
