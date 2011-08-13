Inject
------

Inject is a simple dependency injection library.

What is dependency injection?
-----------------------------
Dependency injection (DI) is a design pattern in object-oriented computer programming whose purpose is to reduce the coupling between software components. It is similar to the factory method pattern. Frequently an object uses (depends on) work produced by another part of the system. With DI, the object does not need to know in advance about how the other part of the system works. Instead, the programmer provides (injects) the relevant system component in advance along with a contract that it will behave in a certain way.
http://en.wikipedia.org/wiki/Dependency_injection

With Inject, the programmer do not need to manually inject the dependencies to an object, Inject does it for you if you provide the dependency bindings.
For example, if a class A depends on a interface B, you just need to bind B to the concrete implementation for an application and let Inject create an instance of A for you!

Why is it useful?
-----------------
Dependency injection force the decoupling of components for each others and simplify unit testing since test objects (mocks, stubs) can be injected when writing the test code.  

How does inject work?
---------------------
See the example in the _example folder for more details...or look at the unit tests!

	//create the injector builder
	Injector.Builder builder = new Injector.Builder();
		
	//add the bindings
	builder.addClassBinding(ConsoleWriter.class, SystemOutConsoleWriter.class);
	builder.addSingletonMapping(MessageFormatter.class, MessageFormatter.class);
	builder.addObjectMapping(String.class, "Inject Says");

	//create the injector
	Injector inject = builder.build();

	//create the ConsoleWritter
	ConsoleWriter writer = inject.createObject(ConsoleWriter.class);
	
	/*
	 * When creating the ConsoleWriter, the injector will use the bindings and create an
 	* instance of SystemOutConsoleWriter. But SystemOutCosoleWriter needs to be injected 
 	* a MessageFormatter. So the injector will instantiate a MessageFormatter, that in turn
 	* needs a String. Since String.class is bound to "Inject Says", this string will be 
 	* injected in the MessageFormatter.
 	* 
 	* So when calling writer.write("hello!"), "Inject Says: hello!" will be printed!
 	*/

	writer.write("hello!");

Limitations
-----------
Circular dependencies (A -> B -> A) are detected but not resolved.
Raw type support only.
