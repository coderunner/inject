package org.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>This class creates objects according to predefined class bindings and inject any dependencies
 * into the newly created class.
 * 
 * <p>Currently supported bindings are:<br>
 * <ul>
 * 	<li><strong>Class Binding:</strong> Binds a class to a subclass. 
 * Usually, an interface or an abstract class to one of its implementation.</li> 
 *  <li><strong>Singleton Class Binding:</strong>
 * Same as class binding, but only one instance is created and returned at subsequent calls.</li>
 * 	<li><string>Object Binding:</strong>Binds a class to an object. The same object is return every time.</li>
 * </ul>
 * 
 * <p>When creating an object or a dependency, the bindings are consulted in the following order:<br>
 * If an Object Binding exist for the class, then the object is returned.<br>
 * Else if a Singleton Class Binding exists for the class, it is used to create the object.<br>
 * Esle if a Class Binding exists, it is used to create the object.<br>
 * Otherwise, a RuntimeException is thrown.<br>
 * 
 * <p>To define the bindings, a Injector.Builder must be created. Then bindings are added by calling
 * the builder's methods. Once all bindings have been defined, the build() method is called in
 * order to create the Injector object.
 * 
 * <p>On the Injector object, objects are created by calling the createObject() methods.
 * 
 * <pre>public <T> T createObject(Class<T> aClass)</pre>
 * 
 * <p>This method will create an object of depending on the defined bindings (see above).
 * 
 * <pre>public <T> T createObject(String aClassName)</pre>
 * 
 * <p>This method will create an object of the given class name. No bindings necessary.
 * It is assumed that the caller know the type (or a super type) of the class.
 * 
 * It is important to note that the Builder class is not thread-safe, while the injector class is thread-safe.
 * 
 * @author felix trepanier
 *
 */
public class Injector
{
	private static final ThreadLocal<LinkedList<Class<?>>> CURRENT_CREATION_CHAIN = 
        new ThreadLocal <LinkedList<Class<?>>> ()
        {
            @Override
            protected LinkedList<Class<?>> initialValue() {
                return new LinkedList<Class<?>>();
        }
    };
    
	private final ConcurrentHashMap<Class<?>, Constructor<?>> mConstructorCache = 
		new ConcurrentHashMap<Class<?>, Constructor<?>>();
	private final ConcurrentHashMap<Class<?>, Method[]> mMethodCache = 
		new ConcurrentHashMap<Class<?>, Method[]>();
	
	private final Map<Class<?>, Class<?>> mClassBindings;
	private final Map<Class<?>, Class<?>> mSingletonBindings;
	
	//use concurrent hash map to avoid returning different objects,
	//if create called in more than one thread simultaneously. 
	private final ConcurrentHashMap<Class<?>, Object> mObjectBindings;
	
	private Injector(Map<Class<?>, Class<?>> aClassBindings,
			Map<Class<?>, Class<?>> aSingletonBindings,
			Map<Class<?>, Object> aObjectBindings)
	{
		mClassBindings = aClassBindings;
		mSingletonBindings = aSingletonBindings;
		mObjectBindings = new ConcurrentHashMap<Class<?>, Object>(aObjectBindings);
	}

	/**
	 * Create an object for the binding associated with the class passed as a parameter.
	 * All of the object dependencies will be injected.
	 * 
	 * @param <T> A super type of the created object
	 * @param aClass The binding key
	 * @return an Object of type T bound to aBoundToClass in the Injector's bindings
	 */
	public <T> T createObject(Class<T> aClass)
	{
		try
		{
			return internCreateObject(aClass);
		}
		finally
		{
			CURRENT_CREATION_CHAIN.remove();
		}
	}
	
	/**
	 * Create an object of the class aClassName.
	 * All of the object dependencies will be injected.
	 * 
	 * @param <T> A super type of the created object
	 * @param aClassName The class of the object to be created
	 * @return an object of class aClassName
	 */
	public <T> T createObject(String aClassName)
	{
		try
		{
			return internCreateObject(aClassName);
		}
		finally
		{
			CURRENT_CREATION_CHAIN.remove();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T internCreateObject(Class<T> aClass)
	{
		T o = (T) mObjectBindings.get(aClass);
		if(o != null)
		{
			return o;
		}

		Class<T> classToInstanciate = (Class<T>) mSingletonBindings.get(aClass);
		if(classToInstanciate != null)
		{
			T instance = instanciate(classToInstanciate);
			T previousValue = (T)mObjectBindings.putIfAbsent(aClass, instance);
			return previousValue != null ? previousValue : instance;
		}

		classToInstanciate = (Class<T>) mClassBindings.get(aClass);
		if(classToInstanciate != null)
		{
			return instanciate(classToInstanciate);
		}

		throw new RuntimeException("Could not instanciate class (no explicit mapping defined) "+aClass.getName());
	}
	
	@SuppressWarnings("unchecked")
	private <T> T internCreateObject(String aClassName)
	{
		Class classToInstanciate;
		try
		{
			classToInstanciate = Class.forName(aClassName);
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
		
		if(CURRENT_CREATION_CHAIN.get().contains(aClassToInstanciate))
		{
			throw new RuntimeException("Circular dependency detected while trying to instanciate "+
					CURRENT_CREATION_CHAIN.get().get(0)+": " + printStack() + aClassToInstanciate.getName());
		}
		CURRENT_CREATION_CHAIN.get().addLast(aClassToInstanciate);
		T instance = createAndInject(aClassToInstanciate, constructor);
		CURRENT_CREATION_CHAIN.get().removeLast();
		
		return instance;
	}

	private <T> T createAndInject(Class<T> aClassToInstanciate, Constructor<T> constructor)
	{
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

	private Object[] createActualParams(Class<?>[] paramsType)
	{
		Object[] actualParams = new Object[paramsType.length];
		for(int i=0; i<paramsType.length;i++)
		{
			actualParams[i] = internCreateObject(paramsType[i]);
		}
		return actualParams;
	}
	
	private static String printStack()
	{
		List<Class<?>> chain = CURRENT_CREATION_CHAIN.get();
		StringBuffer sb = new StringBuffer("Creation stack: ");
		for(Class<?> c : chain)
		{
			sb.append(c.getName()).append("->");
		}
		return sb.toString();
	}
	
	/**
	 * Builder class for {@link Injector}.
	 * 
	 * @author felix trepanier
	 *
	 */
	public static class Builder
	{
		private final Map<Class<?>, Class<?>> mClassBindings = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Class<?>> mSingletonBindings = new HashMap<Class<?>, Class<?>>();
		private final Map<Class<?>, Object> mObjectBindings = new HashMap<Class<?>, Object>();
		private boolean mWasBuilt = false;
		
		public Builder(){}
		
		/**
		 * Add a class binding. A new instance of the mapped class will be created every time.
		 * 
		 * @param <T>
		 * @param aFrom The key
		 * @param aTo The class to instantiate
		 * @return the builder
		 */
		public <T> Builder addClassBinding(Class<T> aFrom, Class<? extends T> aTo)
		{
			check();
			mClassBindings.put(aFrom, aTo);
			return this;
		}
		
		/**
 		 * Add a singleton class binding. A single instance of the mapped class will be created, but it
 		 * will be return all the time.
 		 * 
		 * @param <T>
		 * @param aFrom The key
		 * @param aTo The class to instantiate
		 * @return the builder
		 */
		public <T> Builder addSingletonMapping(Class<T> aFrom, Class<? extends T> aTo)
		{
			check();
			mSingletonBindings.put(aFrom, aTo);
			return this;
		}
		
		/**
		 * Add an object binding. The exact same object will be used every time a binding
		 * of class aFrom is needed.
		 *  
		 * @param <T>
		 * @param aFrom The key
		 * @param aTo The object
		 * @return the builder
		 */
		public <T> Builder addObjectMapping(Class<T> aFrom, Object aTo)
		{
			check();
			mObjectBindings.put(aFrom, aTo);
			return this;
		}
		
		/**
		 * Build the injector
		 * @return the injector
		 */
		public Injector build()
		{
			check();
			mWasBuilt = true;
			return new Injector(mClassBindings, mSingletonBindings, mObjectBindings);
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
