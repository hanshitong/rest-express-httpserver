package org.restexpress.serialization.json;

import static com.strategicgains.util.date.DateAdapterConstants.TIMESTAMP_INPUT_FORMATS;
import com.strategicgains.util.date.DateAdapter;

public class ZhCnTimeAdapter extends DateAdapter
{
	public ZhCnTimeAdapter()
	{
		super("yyyy-MM-dd HH:mm:ss", TIMESTAMP_INPUT_FORMATS);
	}
}

 
