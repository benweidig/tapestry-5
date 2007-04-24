// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.services;

import java.net.URL;

/**
 * An API agnostic version of {@link javax.servlet.ServletContext}, used to bridge the gaps between
 * the Servlet API and the Portlet API.
 */
public interface Context
{
    /**
     * Returns a URL to a resource stored within the context. The path should start with a leading
     * slash.
     * 
     * @param path
     * @return the URL for the path, or null if the path does not correspond to a file.
     */
    URL getResource(String path);

    /** Returns an initial parameter value defined by servlet. */
    String getInitParameter(String name);
}
