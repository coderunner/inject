package org.inject.example;

import org.inject.Inject;

public class SystemOutConsoleWriter implements ConsoleWriter
{
	public MessageFormatter mFormatter;
	
	@Inject
	public void setMessageFormatter(MessageFormatter aMessageFormatter)
	{
		mFormatter = aMessageFormatter;
	}
	
	@Override
	public void write(String aMessage)
	{
		System.out.println(mFormatter.format(aMessage));		
	}

}
