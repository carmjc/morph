package net.carmgate.morph.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface BehaviorInfo {
	/** 
	 * Used when the behavior has been deactivated.
	 * Default value : 0ms.
	 * FIXME Rename this so that it is easier to understand
	 * @Return the number of milliseconds to wait before the behavior can be activated. 
	 */
	int activationCoolDownTime() default 0;

	/**
	 * True if the behavior is always active.
	 * This means that it cannot be deactivated, always has the ACTIVE state
	 * and can always be executed.
	 * @return
	 */
	boolean alwaysActive() default false;

	/**
	 * Used when the behavior has been activated.
	 * Default value : 0ms.
	 * FIXME Rename this so that it is easier to understand
	 * @return the number of milliseconds to wait before the behavior can be deactivated again.
	 */
	int deactivationCoolDownTime() default 0;

	/** 
	 * Used when the behavior has been activated.
	 * Default value : 0ms.
	 * @Return the number of milliseconds to wait before the behavior can be activated again. 
	 */
	int reactivationCoolDownTime() default 0;
}
