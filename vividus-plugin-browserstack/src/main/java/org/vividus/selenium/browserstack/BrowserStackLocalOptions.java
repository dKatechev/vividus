package org.vividus.selenium.browserstack;

import java.util.Objects;

public class BrowserStackLocalOptions
{
    private String proxy;
    private String skipProxyUrlsPattern;

    public String getProxy()
    {
        return proxy;
    }

    public void setProxy(String proxy)
    {
        this.proxy = proxy;
    }

    public String getSkipProxyUrlsPattern()
    {
        return skipProxyUrlsPattern;
    }

    public void setSkipProxyUrlsPattern(String skipProxyUrlsPattern)
    {
        this.skipProxyUrlsPattern = skipProxyUrlsPattern;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BrowserStackLocalOptions))
        {
            return false;
        }
        BrowserStackLocalOptions that = (BrowserStackLocalOptions) o;
        return Objects.equals(proxy, that.proxy)
                && Objects.equals(skipProxyUrlsPattern, that.skipProxyUrlsPattern);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(proxy, skipProxyUrlsPattern);
    }
}
