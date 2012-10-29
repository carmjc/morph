package net.carmgate.morph.model;

/**
 * A vector class holding some common vector operations.
 * 
 * Base :
 *   -1
 * -1   0   +1
 *        +1
 * 
 * @author Carm
 */
public class Vect3D {

	public static final Vect3D NORTH = new Vect3D(0, -1, 0);

	public static final Vect3D NULL = new Vect3D(0, 0, 0);

	public float x;
	public float y;

	public float z;

	public Vect3D() {
	}

	/**
	 * Simple constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vect3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Recopy constructor.
	 * @param p3d
	 */
	public Vect3D(Vect3D p3d) {
		this(p3d.x, p3d.y, p3d.z);
	}

	/**
	 * Addition
	 * @param vector
	 */
	public Vect3D add(Vect3D vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
		return this;
	}

	/**
	 * @param vector vector 2.
	 * @return the angle (in degrees) between current vector and the one provided.
	 */
	public float angleWith(Vect3D vector) {
		float scal = x * vector.x + y * vector.y;
		float vect = x * vector.y - y * vector.x;

		float angle; // in radians
		if (Math.abs(scal) > 0.0000001) { // the minimal value must be big enough to get rid of float rounding errors
			angle = (float) Math.atan(vect / scal);
		} else {
			angle = (float) Math.PI / 2;
		}
		if (scal < 0) {
			angle = (float) (Math.PI + angle);
		}

		// Convert in degrees before returning
		// and adjust to the interval [-180;180]
		float angleInDegrees = (float) Math.toDegrees(angle);
		if (angleInDegrees > 180) {
			angleInDegrees = angleInDegrees - 360;
		}
		if (angleInDegrees < -180) {
			angleInDegrees = angleInDegrees + 360;
		}

		return angleInDegrees;
	}

	/**
	 * Copy the coordinates of the provided vector.
	 * @param vect3d
	 */
	public Vect3D copy(Vect3D vect3d) {
		x = vect3d.x;
		y = vect3d.y;
		z = vect3d.z;
		return this;
	}

	/**
	 * @param vect
	 * @return returns the distance between two points given as Vect3D.
	 */
	public float distance(Vect3D vect) {
		Vect3D tmpVect = new Vect3D(this);
		tmpVect.substract(vect);
		return tmpVect.modulus();
	}

	/**
	 * A vector is considered equal to the current one if their coordinates are exactly the same.
	 * PERFORMANCE: Rewrite this to a faster solution.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Vect3D other = (Vect3D) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
			return false;
		}
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
			return false;
		}
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z)) {
			return false;
		}
		return true;
	}

	/**
	 * PERFORMANCE: Rewrite this to a faster solution
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	/**
	 * @return the modulus of current vector.
	 */
	public float modulus() {
		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}

	/**
	 * Changes coordinates so that returnedVector = (newModulus/currentModulus).currentVector.
	 * The new vector has a modulus equal to newModulus.
	 * @param newModulus
	 */
	public Vect3D normalize(float newModulus) {
		float oldModulus = modulus();

		if (oldModulus == 0) {
			// impossible to normalize
			return this;
		}

		x = x / oldModulus * newModulus;
		y = y / oldModulus * newModulus;
		z = z / oldModulus * newModulus;

		return this;
	}

	public Vect3D prodScal(float operand) {
		x *= operand;
		y *= operand;
		z *= operand;
		return this;
	}

	/**
	 * @param vector
	 * @return the scalar product of the current vector and the provided one.
	 */
	public float prodScal(Vect3D vector) {
		return vector.x * x + vector.y * y + vector.z * z;
	}

	/**
	 * @param vect
	 * @return the project on the Z-axis of the cross product of currentVect and parameter vect
	 */
	public float prodVectOnZ(Vect3D vect) {
		return x * vect.y - y * vect.x;
	}

	/**
	 * Rotates a vector by the given angles in degrees.
	 * @param angleBetweenRdVAndCTT the rotation angles in degrees along the z axis.
	 */
	public Vect3D rotate(double angleBetweenRdVAndCTT) {
		float newX = (float) (Math.cos(Math.toRadians(angleBetweenRdVAndCTT)) * x - Math.sin(Math.toRadians(angleBetweenRdVAndCTT)) * y);
		float newY = (float) (Math.sin(Math.toRadians(angleBetweenRdVAndCTT)) * x + Math.cos(Math.toRadians(angleBetweenRdVAndCTT)) * y);
		x = newX;
		y = newY;
		return this;
	}

	/**
	 * Substraction.
	 * @param vector
	 */
	public Vect3D substract(Vect3D vector) {
		x -= vector.x;
		y -= vector.y;
		z -= vector.z;
		return this;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
