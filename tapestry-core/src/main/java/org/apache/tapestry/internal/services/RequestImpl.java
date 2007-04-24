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

package org.apache.tapestry.internal.services;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Session;

/**
 * Basic implementation of {@link org.apache.tapestry.services.Request} that wraps around an
 * {@link javax.servlet.http.HttpServletRequest}.
 */
public class RequestImpl implements Request
{
    private final HttpServletRequest _request;

    public RequestImpl(HttpServletRequest request)
    {
        _request = request;
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.toList(_request.getParameterNames());
    }

    public String getParameter(String name)
    {
        return _request.getParameter(name);
    }

    public String[] getParameters(String name)
    {
        return _request.getParameterValues(name);
    }

    public String getPath()
    {
        return _request.getServletPath();
    }

    public String getContextPath()
    {
        return _request.getContextPath();
    }

    public Session getSession(boolean create)
    {
        HttpSession session = _request.getSession(create);

        return session == null ? null : new SessionImpl(session);
    }

    public Locale getLocale()
    {
        return _request.getLocale();
    }

    public long getDateHeader(String name)
    {
        return _request.getDateHeader(name);
    }

}
