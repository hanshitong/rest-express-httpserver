/*
    Copyright 2011, Strategic Gains, Inc.

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
package org.restexpress.pipeline;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.restexpress.ContentType;
import org.restexpress.Parameters;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.route.Action;
import org.restexpress.serialization.SerializationSettings;

/**
 * @author toddf
 * @since Feb 2, 2011
 */
public class MessageContext
{
	private Request request;
	private Response response;
	private Action action = null;

	public MessageContext(Request request, Response response)
	{
		super();
		this.request = request;
		this.response = response;
	}

	public Request getRequest()
	{
		return request;
	}

	public Response getResponse()
	{
		return response;
	}

	public Action getAction()
	{
		return action;
	}

	public boolean hasAction()
	{
		return (getAction() != null);
	}

	public void setAction(Action action)
	{
		this.action = action;
		addUrlParametersAsHeaders(getRequest(), action.getParameters());
		getRequest().setResolvedRoute(action.getRoute());
		getResponse().setIsSerialized(action.shouldSerializeResponse());
	}

	public Throwable getException()
	{
		return getResponse().getException();
	}

	public void setException(Throwable throwable)
	{
		getResponse().setException(throwable);
	}

	public void setHttpStatus(HttpResponseStatus httpStatus)
	{
		getResponse().setResponseStatus(httpStatus);
	}
	
	public String getRequestedFormat()
	{
		String format=null;

		if (hasAction())
		{
			format = getAction().getParameter(Parameters.Query.FORMAT);
		}

		if (format == null || format.trim().isEmpty())
		{
			format = getRequest().getHeader(Parameters.Query.FORMAT);
		}
		
		return format;
	}

	/**
	 * @return
	 */
	public boolean supportsRequestedFormat()
	{
		if (!hasAction()) return false;

		return getAction().getRoute().supportsFormat(getRequest().getFormat());
	}

	/**
     * @return
     */
    public Collection<String> getSupportedRouteFormats()
    {
    	if (!hasAction()) return Collections.emptyList();

    	return getAction().getRoute().getSupportedFormats();
    }

	private void addUrlParametersAsHeaders(Request request, Collection<Entry<String, String>> parameters)
    {
		for (Entry<String, String> entry : parameters)
		{
			try
            {
	            request.addHeader(entry.getKey(), URLDecoder.decode(entry.getValue(), ContentType.ENCODING));
            }
            catch (Exception e)
            {
	            request.addHeader(entry.getKey(), entry.getValue());
            }
		}
    }

	public void setSerializationSettings(SerializationSettings settings)
    {
		response.setSerializationSettings(settings);
    }
}
