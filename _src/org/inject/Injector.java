package org.inject;

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
				T instance = classToInstanciate.newInstance();
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
			try
			{
				return classToInstanciate.newInstance();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		classToInstanciate = (Class<T>) mSingletonMapping.get(aClass);
		if(classToInstanciate != null)
		{
			
		}
		throw new RuntimeException("Missing mapping for class "+aClass.getName());
		
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
