package net.carmgate.morph.model;

/**
 * A vector class holding some common vector operations.
 * @author Carm
 */
public class Vect3D {

	public float x;
	public float y;
	public float z;

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
	public void add(Vect3D vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
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
			angle = (float) Math.atan(vect/scal);
		} else {
			angle = (float) Math.PI / 2;
		}
		if (scal < 0) {
			angle = (float) (Math.PI + angle);
		}

		// Convert in degrees before returning
		float angleInDegrees = (float) Math.toDegrees(angle);
		if (angleInDegrees > 180) {
			angleInDegrees = angleInDegrees - 360;
		}
		return angleInDegrees;
	}

	/**
	 * Copy the coordinates of the provided vector.
	 * @param vect3d
	 */
	public void copy(Vect3D vect3d) {
		x = vect3d.x;
		y = vect3d.y;
		z = vect3d.z;
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
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vect3D)) {
			return false;
		}

		Vect3D vect3d = (Vect3D) obj;

		return x == vect3d.x && y == vect3d.y && z == vect3d.z;
	}

	/**
	 * @return x.10^10 + y.10^5 + z 
	 */
	@Override
	public int hashCode() {
		return (int) (x * Math.pow(10, 10) + y * Math.pow(10, 5) + z);
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
	public void normalize(float newModulus) {
		float oldModulus = modulus();

		if (oldModulus == 0) {
			// impossible to normalize
			return;
		}

		x = x / oldModulus * newModulus;
		y = y / oldModulus * newModulus;
		z = z / oldModulus * newModulus;
	}

	/**
	 * @param vector
	 * @return the scalar product of the current vector and the provided one.
	 */
	public float prodScal(Vect3D vector) {
		return vector.x * x + vector.y * y + vector.z * z;
	}

	/**
	 * Rotates a vector by the given angles in degrees.
	 * @param angle the rotation angles in degrees along the 3 axis.
	 */
	public void rotate(Vect3D angle) {
		float newX = (float) (Math.cos(Math.toRadians(angle.z)) * x - Math.sin(Math.toRadians(angle.z)) * y);
		float newY = (float) (Math.sin(Math.toRadians(angle.z)) * x + Math.cos(Math.toRadians(angle.z)) * y);
		x = newX;
		y = newY;
	}

	/**
	 * Rotates a vector by the given angle in degrees.
	 * @param angle the angle along the z axis.
	 */
	public void rotateZ(float angle) {
		rotate(new Vect3D(0, 0, angle));
	}

	/**
	 * Substraction.
	 * @param vector
	 */
	public void substract(Vect3D vector) {
		x -= vector.x;
		y -= vector.y;
		z -= vector.z;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
