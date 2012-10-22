package net.carmgate.morph.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface BehaviorRendererInfo {

	/**
	 * @return true if the behavior should be rendered before all morphs
	 */
	boolean preRendering() default false;

}
