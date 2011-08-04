package org.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Injector
{
	private final Map<Class<?>, Class<?>> mClassMapping;
	private final Map<Class<?>, Class<?>> mSingletonMapping;
	private final Map<Class<?>, Object> mObjectMapping;
	
	private Injector(Map<Class<?>, Class<?>> aClassMapping,
			Map<Class<?>, Class<?>> aSingletonMapping,
			Map<Class<?>, Object> aObjectMapping)
	{
		mClassMapping = aClassMapping;
		mSingletonMapping = aSingletonMapping;
		mObjectMapping = aObjectMapping;
	}

	@SuppressWarnings("unchecked")
	public <T> T createObject(Class<T> aClass)
	{
		T o = (T) mObjectMapping.get(aClass);
		if(o != null)
		{
			return o;
		}
		
		Class<T> classToInstanciate = (Class<T>) mSingletonMapping.get(aClass);
		if(classToInstanciate != null)
		{
			try
			{
				T instance = instanciate(classToInstanciate);
				mObjectMapping.put(aClass, instance);
				return instance;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		classToInstanciate = (Class<T>) mClassMapping.get(aClass);
		if(classToInstanciate != null)
		{
			return instanciate(classToInstanciate);
		}
		
		throw new RuntimeException("Could not instanciate class (no explicit mapping defined) "+aClass.getName());		
	}

	@SuppressWarnings("unchecked")
	private <T> T instanciate(Class<T> aClassToInstanciate)
	{
		Constructor<T>[] constructors = (Constructor<T>[])aClassToInstanciate.getConstructors();
		Constructor<T> defaultConstructor = null;
		Constructor<T> annotatedConstructor = null;
		for(Constructor<T> c : constructors)
		{
			Annotation[] annotations = c.getDeclaredAnnotations();
			for(Annotation a : annotations)
			{
				if(a.annotationType().equals(Inject.class))
				{
					annotatedConstructor = c;
					break;
				}
			}
			if (annotatedConstructor != null)
			{
				break;
			}
			if(c.getParameterTypes().length == 0)
			{
				defaultConstructor = c;
			}
		}
		
		try
		{
			//try annotated constructor
			if(annotatedConstructor != null)
			{
				Class<?>[] paramsType = annotatedConstructor.getParameterTypes();
				Object[] actualParams = new Object[paramsType.length];
				for(int i=0; i<paramsType.length;i++)
				{
					actualParams[i] = createObject(paramsType[i]);
				}
				return annotatedConstructor.newInstance(actualParams);
			}
			//fall back on default
			if(defaultConstructor != null)
			{
				return defaultConstructor.newInstance();
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		throw new RuntimeException("Could not find a constructor to use for instanciation of class "+ aClassToInstanciate.getName());
	}
	
	public static class Builder
	{
		private final Map<Class<?>, Class<?>> mClassMapping = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Class<?>> mSingletonMapping = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Object> mObjectMapping = new HashMap<Class<?>, Object>();
		
		public Builder(){}
		
		public <T> Builder addClassMapping(Class<T> aKey, Class<? extends T> aValue)
		{
			mClassMapping.put(aKey, aValue);
			return this;
		}
		
		public <T> Builder addSingletonMapping(Class<T> aKey, Class<? extends T> aValue)
		{
			mSingletonMapping.put(aKey, aValue);
			return this;
		}
		
		public <T> Builder addObjectMapping(Class<T> aKey, Object aValue)
		{
			mObjectMapping.put(aKey, aValue);
			return this;
		}
		
		public Injector build()
		{
			return new Injector(mClassMapping, mSingletonMapping, mObjectMapping);
		}
	}
}
