package org.inject.example;

import org.inject.Inject;

public class MessageFormatter
{
	private final String mName;
	
	@Inject
	public MessageFormatter(String aName)
	{
		mName = aName;
	}
	
	public String format(String aMessage)
	{
		return mName + ": " + aMessage;
	}
}
