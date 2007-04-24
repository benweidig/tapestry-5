// Copyright 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.internal.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentEventResultProcessor;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class ObjectComponentEventResultProcessorTest extends TapestryTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void invocation_is_failure()
    {
        ComponentResources resources = newComponentResources();
        Component component = newComponent();
        String result = "*INVALID*";

        train_getComponentResources(component, resources);
        train_getCompleteId(resources, "foo.Bar:gnip.gnop");

        List classes = Arrays.asList(String.class, List.class, Map.class);

        replay();

        ComponentEventResultProcessor p = new ObjectComponentEventResultProcessor(classes);

        try
        {
            p.processComponentEvent(result, component, "foo.component.Gnop.blip()");
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "An event handler for component foo.Bar:gnip.gnop returned the value *INVALID* (from method foo.component.Gnop.blip()).  "
                            + "Return type java.lang.String can not be handled.  "
                            + "Configured return types are java.lang.String, java.util.List, java.util.Map.");
        }
    }
}
