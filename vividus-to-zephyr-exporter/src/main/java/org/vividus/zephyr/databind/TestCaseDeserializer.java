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

package org.vividus.zephyr.databind;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.vividus.util.json.JsonPathUtils;
import org.vividus.zephyr.model.TestCase;

public class TestCaseDeserializer extends StdDeserializer<TestCase>
{
    private static final long serialVersionUID = 7820826665413256040L;

    public TestCaseDeserializer()
    {
        super(TestCase.class);
    }

    @Override
    public TestCase deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException
    {
        String node = parser.getCodec().readTree(parser).toString();
        String status = JsonPathUtils.getData(node, "$.status");
        List<String> testCaseIds = JsonPathUtils.getData(node, "$..[?(@.name=='testCaseId')].value");
        return new TestCase(testCaseIds, status);
    }
}
