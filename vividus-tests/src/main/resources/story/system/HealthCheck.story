Meta: @proxy

Scenario: Healthcheck
Given I am on a page with the URL 'https://example.com'
Then the text 'Example Domain' exists
Then number of HTTP GET requests with URL pattern `.*` is greater than `1`
