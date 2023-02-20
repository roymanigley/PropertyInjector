package ch.bytecrowd.propertyinjector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface InjectedProperty {

    String dependencyInstantiation() default "";
    boolean singleton() default true;
}
