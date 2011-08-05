package org.inject;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>This annotation can be used on methods and constructors.
 * 
 * <p>It should only be present on <b>one constructor</b> in the class. The {@link Injector} will
 * call this constructor in order to create the object.
 * 
 * <p>This annotation can be used on any number of methods in a class. Right after the
 * creation of the object, the {@link Injector} will call all annotated methods.
 * 
 * @author felix trepanier
 *
 */
@Target({ METHOD, CONSTRUCTOR})
@Retention(RUNTIME)
public @interface Inject
{

}
