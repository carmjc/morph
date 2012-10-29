package net.carmgate.morph.ia;

/**
 * <p>An AI is meant to drive one element of type T.
 * (if the element is a ship, it is also meant to (de)activate 
 * some of its morphs depending on the situation)</p>
 * <p>An AI is assigned one and only one goal (for instance, kill a morph).
 * It will continue as long as requirements for continuing are met
 * and as long as its assignment is not complete.</p>
 * <p>Once its job is complete, it should clean everything it has set up.
 * For instance, a GoSomewhereAI should deactivate the PropulsorMorphs once its job is done.</p>
 */
public interface AI<T> {
	/**
	 * This method really does the job.
	 * As long as the AI is active, this method will be evaluated every cycle.
	 */
	void compute();

	/**
	 * Evaluated every cycle to see if the AI's job is done.
	 * @return true if it's done.
	 */
	boolean done();

}
