package org.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Injector
{
	private final ConcurrentHashMap<Class<?>, Constructor<?>> mConstructorCache = 
		new ConcurrentHashMap<Class<?>, Constructor<?>>();
	private final ConcurrentHashMap<Class<?>, Method[]> mMethodCache = 
		new ConcurrentHashMap<Class<?>, Method[]>();
	
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
	public <T> T createObject(Class<T> aSuperType, String aClassName)
	{
		Class<T> classToInstanciate;
		try
		{
			classToInstanciate = (Class<T>)Class.forName(aClassName);
		} 
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		return (T) instanciate(classToInstanciate);
	}

	@SuppressWarnings("unchecked")
	private <T> T instanciate(Class<T> aClassToInstanciate)
	{
		Constructor<T> constructor = (Constructor<T>) mConstructorCache.get(aClassToInstanciate);
		if(constructor == null)
		{
			//no cached constructor find the one to use
			Constructor<T>[] constructors = (Constructor<T>[])aClassToInstanciate.getConstructors();
			Constructor<T> defaultConstructor = null;
			Constructor<T> annotatedConstructor = null;
			for(Constructor<T> c : constructors)
			{
				if(c.getAnnotation(Inject.class) != null)
				{
					annotatedConstructor = c;
					break;
				}
				if(c.getParameterTypes().length == 0)
				{
					defaultConstructor = c;
				}
			}
			constructor = annotatedConstructor != null ? annotatedConstructor : defaultConstructor;
			mConstructorCache.putIfAbsent(aClassToInstanciate, constructor);
		}
		
		T instance = null;
		try
		{
			Object[] actualParams = createActualParams(constructor.getParameterTypes());
			instance = constructor.newInstance(actualParams);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		
		if(instance == null)
		{
			throw new RuntimeException("Could not find a constructor to use for instanciation of class "+ aClassToInstanciate.getName());
		}
		
		//check for setters to inject
		Method[] injectMethod = mMethodCache.get(aClassToInstanciate);
		if(injectMethod == null)
		{
			List<Method> injectMethodList = new ArrayList<Method>();
			Method[] allMethods = aClassToInstanciate.getDeclaredMethods();
			for(Method m : allMethods)
			{
				if(m.getAnnotation(Inject.class) != null)
				{
					injectMethodList.add(m);
				}
			}
			injectMethod = new Method[injectMethodList.size()];
			System.arraycopy(injectMethodList.toArray(), 0, injectMethod, 0, injectMethodList.size());
			mMethodCache.putIfAbsent(aClassToInstanciate, injectMethod);
		}
		
		for(Method m : injectMethod)
		{
			Object[] params = createActualParams(m.getParameterTypes());
			try
			{
				m.invoke(instance, params);
			}
			catch(Exception e)
			{
				throw new RuntimeException("Could not inject in class "+aClassToInstanciate.getName() +" with method "+m.getName(), e);
			}
		}
		
		return instance;
	}

	private Object[] createActualParams(Class<?>[] paramsType) {
		Object[] actualParams = new Object[paramsType.length];
		for(int i=0; i<paramsType.length;i++)
		{
			actualParams[i] = createObject(paramsType[i]);
		}
		return actualParams;
	}
	
	public static class Builder
	{
		private final Map<Class<?>, Class<?>> mClassMapping = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Class<?>> mSingletonMapping = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Object> mObjectMapping = new HashMap<Class<?>, Object>();
		private boolean mWasBuilt = false;
		
		public Builder(){}
		
		public <T> Builder addClassMapping(Class<T> aKey, Class<? extends T> aValue)
		{
			check();
			mClassMapping.put(aKey, aValue);
			return this;
		}
		
		public <T> Builder addSingletonMapping(Class<T> aKey, Class<? extends T> aValue)
		{
			check();
			mSingletonMapping.put(aKey, aValue);
			return this;
		}
		
		public <T> Builder addObjectMapping(Class<T> aKey, Object aValue)
		{
			check();
			mObjectMapping.put(aKey, aValue);
			return this;
		}
		
		public Injector build()
		{
			check();
			mWasBuilt = true;
			return new Injector(mClassMapping, mSingletonMapping, mObjectMapping);
		}
		
		private void check()
		{
			if(mWasBuilt)
			{
				throw new RuntimeException("Can not reuse a builder after the injector was built.");
			}
		}
	}
}
