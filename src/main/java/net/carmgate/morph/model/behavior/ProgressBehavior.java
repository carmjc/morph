package net.carmgate.morph.model.behavior;

/**
 * This should be used for behaviors that take some time to execute.
 * This allows to follow the progress of the behavior. 
 */
public interface ProgressBehavior {

	/**
	 * @return the percentage of progress through execution.
	 */
	float getProgress();
}
