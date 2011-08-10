package org.inject.example;

import org.inject.Injector;

public class ShowInject
{
	public static void main(String aArgs[])
	{
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
	}
	
}
