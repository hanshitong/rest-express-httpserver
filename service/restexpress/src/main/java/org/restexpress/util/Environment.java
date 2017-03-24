/*
    Copyright 2010, Strategic Gains, Inc.

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
package org.restexpress.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.restexpress.common.exception.ConfigurationException;

/**
 * @author kevwil
 * @author toddf
 * @since Dec 16, 2010
 */
public abstract class Environment
{
	private static final String ENVIRONMENT_DIR = "config/";
	private static final String PROPERTIES_FILENAME = "/environment.properties";
	private static final String DEFAULT_ENVIRONMENT = "dev";

	public static <T extends Environment> T load(String[] args, Class<T> type)
    throws FileNotFoundException, IOException
    {
	    if (args.length > 0)
		{
			return from(args[0], type);
		}

	    return fromDefault(type);
    }

	public static <T extends Environment> T fromDefault(Class<T> type)
	throws FileNotFoundException, IOException
	{
		return from(DEFAULT_ENVIRONMENT, type);
	}

	public static <T extends Environment> T from(String environmentName, Class<T> type)
	throws FileNotFoundException, IOException
	{
		T instance = newEnvironment(type);

		if (environmentName != null)
		{
			instance.load(ENVIRONMENT_DIR + environmentName + PROPERTIES_FILENAME);
		}

		return instance;
	}

	protected void load(String filename)
	throws ConfigurationException, FileNotFoundException, IOException
	{
//		log.debug("loading environment properties from " + environmentFile);
		Properties p = readProperties(filename);
		fillValues(p);
	}
	
	protected abstract void fillValues(Properties p)
	throws ConfigurationException;

	protected Properties readProperties(String environmentFile)
	throws FileNotFoundException, IOException
	{
		Properties properties = new Properties();
		boolean wasLoadedFromClasspath = loadFromClasspath(environmentFile, properties);
		FileInputStream fis = null;

		try
		{
			File envFile = new File(environmentFile);
			fis = new FileInputStream(envFile);
			properties.load(fis);
			return properties;
		}
		catch (FileNotFoundException e)
		{
//			log.error("could not find properties file: " + e.getLocalizedMessage());
			if (!wasLoadedFromClasspath)
			{
				throw e;
			}

			return properties;
		}
		catch (IOException e)
		{
//			log.error("error reading properties file: ", e);
			throw e;
		}
		finally
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				// too late to care at this point
			}
		}
	}

	private boolean loadFromClasspath(String environmentFile, Properties properties)
	throws IOException
	{
		String modified = (environmentFile.startsWith("/") ? environmentFile : "/" + environmentFile);
		InputStream cpis = getClass().getResourceAsStream(modified);

		try
		{
			if (cpis != null)
			{
				properties.load(cpis);
				return true;
			}
	
			return false;
		}
		finally
		{
			try
			{
				if (cpis != null)
				{
					cpis.close();
				}
			}
			catch (IOException e)
			{
				// too late to care at this point
			}
		}
	}

	private static <T> T newEnvironment(Class<T> type)
    {
		T instance = null;

	    try
        {
	        instance = type.newInstance();
        }
        catch (InstantiationException e)
        {
	        e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
	        e.printStackTrace();
        }

        return instance;
    }
}
