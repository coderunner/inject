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
	
	public void testClassNoConstructorNoAnnotation()
	{
		Injector.Builder builder = new Injector.Builder();
		
		builder.addClassMapping(NoDefaultConstructorNoAnnotation.class, NoDefaultConstructorNoAnnotation.class);
		Injector i = builder.build();
		
		try
		{
			i.createObject(NoDefaultConstructorNoAnnotation.class);
			fail();
		}
		catch(RuntimeException e)
		{}
		
	}
	
	public void testClassWithAnnotatedConstructor()
	{
		String injected = "injected";
		Injector.Builder builder = new Injector.Builder();
		
		builder.addClassMapping(ClassWithAnnotatedConstructor.class, ClassWithAnnotatedConstructor.class);
		builder.addObjectMapping(String.class, injected);
		Injector i = builder.build();

		ClassWithAnnotatedConstructor o = 
			i.createObject(ClassWithAnnotatedConstructor.class);
		
		assertEquals(injected, o.getString());		
	}
	
	public void testClassWithDependency()
	{
		Injector.Builder builder = new Injector.Builder();
		
		builder.addClassMapping(ClassWithDependency.class, ClassWithDependency.class);
		builder.addClassMapping(ClassWithAnnotatedConstructor.class, ClassWithAnnotatedConstructor.class);
		builder.addClassMapping(String.class, String.class);
		Injector i = builder.build();

		ClassWithDependency o = i.createObject(ClassWithDependency.class);
		
		assertNotNull(o.getDependency());
		assertEquals("", o.getDependency().getString());
	}
	
	public static class NoDefaultConstructorNoAnnotation
	{
		public NoDefaultConstructorNoAnnotation(String aString)
		{}
	}
	
	public static class ClassWithAnnotatedConstructor
	{
		private final String mString;
		
		@Inject
		public ClassWithAnnotatedConstructor(String aString)
		{
			mString = aString;
		}
		
		public String getString()
		{
			return mString;
		}
	}
	
	public static class ClassWithDependency
	{
		private final ClassWithAnnotatedConstructor mDependency;
		
		@Inject
		public ClassWithDependency(ClassWithAnnotatedConstructor aDependency)
		{
			mDependency = aDependency;
		}
		
		public ClassWithAnnotatedConstructor getDependency()
		{
			return mDependency;
		}
	}
	
	//TODO test with setters with Inject annotation

}
