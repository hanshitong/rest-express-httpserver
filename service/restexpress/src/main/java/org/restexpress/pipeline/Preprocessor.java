/*
 * Copyright 2010, eCollege, Inc.  All rights reserved.
 */
package org.restexpress.pipeline;

import org.restexpress.Request;


/**
 * Defines the interface for processing that occurs after the request is created, but before
 * any other processing of the route occurs. The preprocessing chain is terminated 
 * if an exception is thrown.  In fact, if a Preprocessor throws an exception, the rest of 
 * the Preprocessors in the chain are skipped along with the entire route.  If an exception
 * occurs in a Preprocessor, Postprocessors are not called either.  However, MessageObservers
 * are.
 *  
 * @author toddf
 * @since Aug 31, 2010
 */
public interface Preprocessor
{
	public void process(Request request);
}
