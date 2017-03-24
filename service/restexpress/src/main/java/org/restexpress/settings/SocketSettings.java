/*
    Copyright 2012, Strategic Gains, Inc.

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
package org.restexpress.settings;

/**
 * @author toddf
 * @since May 31, 2012
 */
public class SocketSettings
{
	private boolean useTcpNoDelay = true;
	private int soLinger = -1; // disabled by default
	private int receiveBufferSize = 262140; // Java default
	private int connectTimeoutMillis = 10000; // netty default
	private int sendBufferSize = 8096; //default

	public boolean useTcpNoDelay()
	{
		return useTcpNoDelay;
	}

	public void setUseTcpNoDelay(boolean useTcpNoDelay)
	{
		this.useTcpNoDelay = useTcpNoDelay;
	}

	public int getSoLinger()
	{
		return soLinger;
	}

	public void setSoLinger(int soLinger)
	{
		this.soLinger = soLinger;
	}

	public int getReceiveBufferSize()
	{
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize)
	{
		this.receiveBufferSize = receiveBufferSize;
	}

	public int getConnectTimeoutMillis()
	{
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(int connectTimeoutMillis)
	{
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

}
