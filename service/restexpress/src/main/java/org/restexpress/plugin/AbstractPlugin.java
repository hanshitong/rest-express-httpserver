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
package org.restexpress.plugin;

import org.restexpress.RestExpress;

/**
 * @author toddf
 * @since Jul 20, 2011
 */
public abstract class AbstractPlugin
implements Plugin
{
	private boolean isRegistered;

	@Override
	public AbstractPlugin register(RestExpress server)
	{
		if (!isRegistered())
		{
			setRegistered(true);
			server.registerPlugin(this);
		}

		return this;
	}
	
	@Override
	public void bind(RestExpress server)
	{
		// default behavior is to do nothing.
	}

	@Override
	public void shutdown(RestExpress server)
	{
		// default behavior is to do essentially nothing.
		setRegistered(false);
	}

	/**
	 * This AbstractPlugin is assumed to be equal to other when:
	 * 1) This class is assignable from other and<br/>
	 * 2) other has the same simple class name.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null) return false;

		if (AbstractPlugin.class.isAssignableFrom(other.getClass()))
		{
			return equals((AbstractPlugin) other);
		}

		return false;
	}
	
	public boolean equals(AbstractPlugin plugin)
	{
		if (plugin == null) return false;

		return this.getClass().getSimpleName().equals(plugin.getClass().getSimpleName());
	}

	/**
	 * Generates a hash code based on the class of this instance.  All instances
	 * of the same class will have the same hash code.
	 */
	public int hashCode()
	{
		return this.getClass().hashCode() ^ 17;
	}
	
	protected boolean isRegistered()
	{
		return isRegistered;
	}
	
	protected void setRegistered(boolean value)
	{
		this.isRegistered = value;
	}
}
