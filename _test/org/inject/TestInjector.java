package org.inject;

import org.inject.Injector;

import org.junit.Test;
import static org.junit.Assert.* ;

public class TestInjector
{
	private final Injector.Builder mBuilder = new Injector.Builder();
	
	@Test
	public void createObjectWithClassMapping() throws Exception
	{
		mBuilder.addClassMapping(String.class, String.class);
		Injector i = mBuilder.build();
		assertTrue(i.createObject(String.class) instanceof String);
	}
	
	@Test
	public void reusingTheBuilderFails() throws Exception
	{
		mBuilder.addClassMapping(String.class, String.class);
		mBuilder.build();
		try
		{
			mBuilder.build();
			fail();
		}
		catch(RuntimeException e)
		{}
	}
	
	@Test
	public void failToCreateIfNoMapping() throws Exception
	{		
		Injector i = mBuilder.build();
		try
		{
			i.createObject(String.class);
			fail();
		}
		catch(RuntimeException e)
		{}
	}
	
	@Test
	public void createSubClass() throws Exception
	{		
		mBuilder.addClassMapping(Exception.class, RuntimeException.class);
		Injector i = mBuilder.build();
		assertTrue(i.createObject(Exception.class) instanceof RuntimeException);
	}
	
	@Test
	public void createSingletonClass() throws Exception
	{		
		mBuilder.addSingletonMapping(Exception.class, RuntimeException.class);
		Injector i = mBuilder.build();
		Object o = i.createObject(Exception.class);
		assertTrue(o instanceof RuntimeException);
		Object o2 = i.createObject(Exception.class);
		assertTrue(o == o2);
	}
	
	@Test
	public void createObjectwithObjectMapping() throws Exception
	{		
		RuntimeException ex = new RuntimeException();
		mBuilder.addObjectMapping(Exception.class, ex);
		Injector i = mBuilder.build();
		Object o = i.createObject(Exception.class);
		assertTrue(o instanceof RuntimeException);
		assertTrue(o == ex);
	}
	
	@Test
	public void failToCreateNoConstructorNoAnnotation()
	{		
		mBuilder.addClassMapping(NoDefaultConstructorNoAnnotation.class, NoDefaultConstructorNoAnnotation.class);
		Injector i = mBuilder.build();
		
		try
		{
			i.createObject(NoDefaultConstructorNoAnnotation.class);
			fail();
		}
		catch(RuntimeException e)
		{}
		
	}
	
	@Test
	public void createWithAnnotatedConstructor()
	{
		String injected = "injected";
		
		mBuilder.addClassMapping(ClassWithAnnotatedConstructor.class, ClassWithAnnotatedConstructor.class);
		mBuilder.addObjectMapping(String.class, injected);
		Injector i = mBuilder.build();

		ClassWithAnnotatedConstructor o = 
			i.createObject(ClassWithAnnotatedConstructor.class);
		
		assertEquals(injected, o.getString());		
	}
	
	@Test
	public void createClassWithDependency()
	{		
		mBuilder.addClassMapping(ClassWithDependency.class, ClassWithDependency.class);
		mBuilder.addClassMapping(ClassWithAnnotatedConstructor.class, ClassWithAnnotatedConstructor.class);
		mBuilder.addClassMapping(String.class, String.class);
		Injector i = mBuilder.build();

		ClassWithDependency o = i.createObject(ClassWithDependency.class);
		
		assertNotNull(o.getDependency());
		assertEquals("", o.getDependency().getString());
	}
	
	@Test
	public void createClassWithSetter()
	{		
		mBuilder.addClassMapping(ClassWithSetterAnnotated.class, ClassWithSetterAnnotated.class);
		mBuilder.addClassMapping(ClassWithAnnotatedConstructor.class, ClassWithAnnotatedConstructor.class);
		mBuilder.addClassMapping(String.class, String.class);
		Injector i = mBuilder.build();

		ClassWithSetterAnnotated o = i.createObject(ClassWithSetterAnnotated.class);
		
		assertNotNull(o.getDependency());
		assertEquals("", o.getDependency().getString());
	}
	
	@Test
	public void createClassByName()
	{
		String value = "value";		
		mBuilder.addObjectMapping(String.class, value);
		Injector i = mBuilder.build();

		Object o = i.createObject(Object.class, ClassWithAnnotatedConstructor.class.getName());
		
		assertEquals(value, o.toString());
	}
	
	@Test
	public void failToCreateClassByNameDoNotExists()
	{
		Injector i = mBuilder.build();

		try
		{
			i.createObject(Object.class, "a.fake.class");
			fail();
		}
		catch(RuntimeException e)
		{}
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
		
		@Override
		public String toString()
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
	
	public static class ClassWithSetterAnnotated
	{
		private ClassWithAnnotatedConstructor mDependency;
		
		@Inject
		public void setDependency(ClassWithAnnotatedConstructor aDependency)
		{
			mDependency = aDependency;
		}
		
		public ClassWithAnnotatedConstructor getDependency()
		{
			return mDependency;
		}
	}
}
