package net.carmgate.morph.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.carmgate.morph.model.solid.morph.Morph.MorphType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface MorphInfo {

	/**
	 * Default : 100
	 */
	int disableMass() default 10;

	/**
	 * Default : 100
	 */
	int initialMass() default 100;

	/**
	 * Default : 100
	 */
	int maxEnergy() default 100;

	/**
	 * Default : 100
	 */
	int maxMass() default 100;

	/**
	 * Default : 30
	 */
	int reEnableMass() default 30;

	/**
	 * Default : {@link MorphType#BASIC}
	 */
	MorphType type() default MorphType.BASIC;

	/**
	 * True if the morph is virtual.
	 * Default value: false.
	 * A virtual morph will not be considered when updating mass or energy, for instance.
	 */
	boolean virtual() default false;
}
