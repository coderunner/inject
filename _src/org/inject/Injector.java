package org.inject;

import java.util.HashMap;
import java.util.Map;

public class Injector
{
	private final Map<Class<?>, Class<?>> mClassMapping;
	private final Map<Class<?>, Class<?>> mSingletonMapping;
	private final Map<Class<?>, Object> mObjectMapping;
	
	public Injector(Map<Class<?>, Class<?>> aClassMapping,
			Map<Class<?>, Class<?>> aSingletonMapping,
			Map<Class<?>, Object> aObjectMapping)
	{
		mClassMapping = aClassMapping;
		mSingletonMapping = aSingletonMapping;
		mObjectMapping = aObjectMapping;
	}

	public <T> T createObject(Class<T> aClass) throws Exception
	{
		return aClass.newInstance();
	}
	
	public class Builder
	{
		private final Map<Class<?>, Class<?>> mClassMapping = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Class<?>> mSingletonMapping = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Object> mObjectMapping = new HashMap<Class<?>, Object>();
		
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
