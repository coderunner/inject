package org.inject;

import org.inject.Injector;

import junit.framework.TestCase;

public class TestInjector extends TestCase
{
	public void testDirectClassMapping() throws Exception
	{
		Injector.Builder builder = new Injector.Builder();
		
		builder.addClassMapping(String.class, String.class);
		Injector i = builder.build();
		assertTrue(i.createObject(String.class) instanceof String);
	}
	
	public void testNoExistantMapping() throws Exception
	{
		Injector.Builder builder = new Injector.Builder();
		
		Injector i = builder.build();
		try
		{
			i.createObject(String.class);
			fail();
		}
		catch(RuntimeException e)
		{}
	}
	
	public void testSubClassClassMapping() throws Exception
	{
		Injector.Builder builder = new Injector.Builder();
		
		builder.addClassMapping(Exception.class, RuntimeException.class);
		Injector i = builder.build();
		assertTrue(i.createObject(Exception.class) instanceof RuntimeException);
	}
	
	public void testSingletonClassMapping() throws Exception
	{
		Injector.Builder builder = new Injector.Builder();
		
		builder.addSingletonMapping(Exception.class, RuntimeException.class);
		Injector i = builder.build();
		Object o = i.createObject(Exception.class);
		assertTrue(o instanceof RuntimeException);
		Object o2 = i.createObject(Exception.class);
		assertTrue(o == o2);
	}
	
	public void testObjectClassMapping() throws Exception
	{
		Injector.Builder builder = new Injector.Builder();
		
		RuntimeException ex = new RuntimeException();
		builder.addObjectMapping(Exception.class, ex);
		Injector i = builder.build();
		Object o = i.createObject(Exception.class);
		assertTrue(o instanceof RuntimeException);
		assertTrue(o == ex);
	}

}
