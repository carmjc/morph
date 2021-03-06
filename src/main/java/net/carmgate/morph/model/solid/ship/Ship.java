package net.carmgate.morph.model.solid.ship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.impl.morph.prop.PropulsorsLost;
import net.carmgate.morph.model.behavior.impl.ship.EnergyDiffusing;
import net.carmgate.morph.model.behavior.impl.ship.GettingEnergyFromSources;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.solid.AISupport;
import net.carmgate.morph.model.solid.MovementSupport;
import net.carmgate.morph.model.solid.RotationSupport;
import net.carmgate.morph.model.solid.WorldPositionSupport;
import net.carmgate.morph.model.solid.WorldSolid;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.impl.PropulsorMorph;
import net.carmgate.morph.model.solid.ship.listener.ShipEvent;
import net.carmgate.morph.model.solid.ship.listener.ShipListener;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.model.user.User;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.util.collections.ModifiableIterable;

import org.apache.log4j.Logger;

/**
 * TODO : Il faut ajouter un centre d'inertie et modifier les calculs des forces pour gérer le vrai centre d'inertie.
 */
public abstract class Ship extends WorldSolid implements WorldPositionSupport,
		AISupport<Ship>, MovementSupport, RotationSupport {

	private static final Logger LOGGER = Logger.getLogger(Ship.class);

	/** Radius of the ship. */
	private float radius = 0;

	/** The ship position in the world. */
	private Vect3D pos;

	private Vect3D posSpeed;
	private Vect3D posAccel;
	/** The ship orientation in the world. */
	private float rot;

	private float rotSpeed;
	private float rotAccel;
	/** The drag factor. The lower, the more it's dragged. */
	private float dragFactor = ModelConstants.INITIAL_DRAG_FACTOR;

	/** The list of always active behaviors of the ship. */
	private List<Behavior<Ship>> alwaysActiveBehaviors = new ArrayList<Behavior<Ship>>();

	/**
	 * The list of the forces attached to the ship or a constituant of the ship.
	 * This is a force generated by the ship. On the contrary external forces are applied to the ship but not generated by it.
	 * Example: the force generated by a propulsor.
	 * Mathematically, it differs from external forces in that a rotation of the ship leads to a rotation of the force.
	 */
	private final List<Force> ownForceList = new ArrayList<Force>();

	/** The list of this ship's morphs. */
	private final Map<Long, Morph> morphsById = new HashMap<Long, Morph>();

	/** The list of this ships's morphs byt type. */
	private final Map<Class<? extends Morph>, List<Morph>> morphsByType = new HashMap<Class<? extends Morph>, List<Morph>>();

	/** The list of this ship's morphs. */
	private final Map<Vect3D, Morph> morphsByPosInShipGrid = new HashMap<Vect3D, Morph>();

	/** List of ships IAs. */
	private final ModifiableIterable<AI<Ship>> iaList = new ModifiableIterable<AI<Ship>>(new ArrayList<AI<Ship>>());

	/** List of ship's listeners. */
	private final List<ShipListener> shipListeners = new ArrayList<ShipListener>();

	/** The center of mass of the ship, in ship coordinates */
	private Vect3D comInShip = new Vect3D(Vect3D.NULL);

	/** The center of mass of the ship, in world coordinates */
	private Vect3D center = new Vect3D(Vect3D.NULL);

	/** Timestamp of last time the ship's position was calculated. */
	private long lastUpdateTS;

	/** Owner of the ship. */
	private User owner;

	/** Mass in reserve for future use by ship's morphs. This can be increased by mining. */
	private float storedMass;

	public Ship(float x, float y, float z, User owner) {
		pos = new Vect3D(x, y, z);
		posSpeed = new Vect3D(0, 0, 0);
		posAccel = new Vect3D(0, 0, 0);
		rot = 0;
		rotSpeed = 0;
		rotAccel = 0;

		// Init lastUpdateTS
		lastUpdateTS = World.getWorld().getCurrentTS();

		// assign user
		this.owner = owner;

		// assign always active behaviors
		alwaysActiveBehaviors.add(new EnergyDiffusing(this));
		alwaysActiveBehaviors.add(new GettingEnergyFromSources(this));
	}

	/**
	 * Adds the provided morph to the ship at the given position.
	 * The "in ship" coordinates axis are the horizontal axis (x axis) 
	 *  and an axis from 7 o'clock to 1 o'clock (y axis).
	 * @param newMorph the morph to add to the ship
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate (unused for now)
	 * @param replace true if the morph should replace the morph at the given position if there is one already.
	 * @return true if the morph has been properly added to the ship
	 */
	public final boolean addMorph(Morph newMorph, float x, float y, float z, boolean replace) {
		Morph existingMorph = findShipMorph(x, y, z);

		if (replace || existingMorph == null) {

			// if there already is a morph at this position in ship, remove it.
			if (existingMorph != null) {
				removeMorph(existingMorph);
			}

			// Attach the morph to the ship
			newMorph.setShip(this);

			// Insert it the different maps
			putMorphInMapsAndLists(newMorph, x, y, z);

			// set position in ship
			newMorph.setPosInShipGrid(new Vect3D(x, y, z));

			// Position and rotation in ship and world
			newMorph.setShip(this);
			newMorph.updatePosFromGridPos();

			// Recompute differente elements of the ship.
			if (!newMorph.getClass().getAnnotation(MorphInfo.class).virtual()) {
				// Compute com and center
				computeCOM();
				computeRadiusIncremental(newMorph, true);
			}

			return true;
		}

		return false;
	}

	/**
	 * Adds the provided morph to the ship at the given position.
	 * See {@link #addMorph(Morph, float, float, float, boolean)}.
	 * If there already is a morph at the specified location, it is removed and replaced
	 * by the provided morph.
	 * @param morph the morph to add to the ship
	 * @param posInShip the position in ship grid
	 * @return true if the morph has been properly added to the ship
	 */
	public final boolean addMorph(Morph morph, Vect3D posInShip) {
		return addMorph(morph, posInShip, true);
	}

	/**
	 * Adds the provided morph to the ship at the given position.
	 * See {@link #addMorph(Morph, float, float, float, boolean)}.
	 * @param newMorph the morph to add to the ship
	 * @param posInShip the coordinate in ship
	 * @param replace true if the morph should replace the morph at the given position if there is one already.
	 * @return true if the morph has been properly added to the ship
	 */
	public final boolean addMorph(Morph newMorph, Vect3D posInShip, boolean replace) {
		return addMorph(newMorph, posInShip.x, posInShip.y, posInShip.z, replace);
	}

	/**
	 * Adds a collection of morphs.
	 * See {@link #addMorph(Morph, Vect3D)}.
	 * @param morphs the morphs to add
	 */
	public final void addMorphs(Collection<? extends Morph> morphs) {
		for (Morph m : morphs) {
			// m.setShip(this);
			// putMorphInMapsAndLists(m, m.getPosInShipGrid().x, m.getPosInShipGrid().y, m.getPosInShipGrid().z);
			// LOGGER.trace("morph added: " + m);
			addMorph(m, m.getPosInShipGrid());
		}
		computeCOM();
	}

	/**
	 * Add a ship listener to be notified of ship events.
	 * See {@link ShipListener}.
	 * @param shipListener the ship listener
	 */
	public final void addShipListener(ShipListener shipListener) {
		shipListeners.add(shipListener);
	}

	/**
	 * <p>Apply currently known forces to the ship.
	 * Each force vector is added to the ship acceleration vector.
	 * Furthermore, each force vector momentum on the vertical axis
	 * coming from the com si applied to the rotation acceleration.</p>
	 * <p>Any remaining position acceleration or rotation acceleration
	 * is firstly removed.</p>
	 */
	private void applyForces() {
		posAccel.copy(Vect3D.NULL);
		rotAccel = 0;

		// Calculate com in world
		Vect3D comInWorld = new Vect3D(getComInShip());
		transformShipToWorldCoords(comInWorld);

		// Initialize forceTarget vector
		Vect3D forceTarget = new Vect3D();

		for (Force f : ownForceList) {

			// the acceleration caused by the force is applied to the ship's inertia center.
			// In fact, it's not applied to the com, but it does not matter since this is only the linear
			// acceleration component
			Vect3D forceVector = new Vect3D(f.getVector());
			// Remove the effect of morph and ship rotation.
			// The added acceleration vector must be expressed in ship coordinates
			// and we must compensate for a target morph's rotation.
			forceVector.rotate(f.getTarget().getRotInShip() + rot);
			forceVector.normalize(forceVector.modulus() / getTotalMass());
			posAccel.add(forceVector);

			// the tangential element of the force generates a rotation of the ship
			// with intensity proportional to the force moment
			// the force momentum is computed along a vertical axis coming from the com
			forceTarget.copy(f.getTarget().getPosInWorld());
			forceTarget.substract(comInWorld);
			rotAccel += forceTarget.prodVectOnZ(forceVector) / getTotalMass();

		}

		// Then we clean the force list
		// At the beginning of each frame, there is no force
		// applied on anything.
		ownForceList.clear();
	}

	/**
	 * Calculates the COM (center of mass).
	 * The COM vector origin is the morph with shipgrid coordinates (0,0)
	 * The current computation is an approximation and assumes that each and every morph in
	 * the ship is at full mass.
	 */
	public final void computeCOM() {
		// Compute new com position
		comInShip.copy(Vect3D.NULL);
		float shipMass = 0;
		for (Morph m : morphsById.values()) {
			// Add the weighted pos in ship of the morph
			Vect3D weightedPosInShip = new Vect3D(m.getPosInShip());
			weightedPosInShip.prodScal(m.getMaxMass());
			comInShip.add(weightedPosInShip);

			// add the morph's mass to the ship's mass
			shipMass += m.getMaxMass();
		}
		comInShip.normalize(comInShip.modulus() / shipMass);

		// update position
		Vect3D posDelta = new Vect3D(comInShip).rotate(rot).substract(comInShip);
		pos.add(posDelta);
	}

	/**
	 * Computes the radius of the ship.
	 * This computing is done incrementally. We take the former radius of the ship and 
	 * compute the change brought by the morph addition/deletion.
	 * FIXME Implement deletion
	 * @param newMorph the morph added to the ship 
	 * @param addition denotes if the morph in an addition or a deletion from to the ship.
	 */
	public final void computeRadiusIncremental(Morph newMorph, boolean addition) {
		if (morphsById.size() == 1) {
			radius = 16; // FIXME put that morph radius into the Morph class
		}

		if (addition) {
			float distance = 0;
			for (Morph m : morphsById.values()) {
				distance = newMorph.getPosInShip().distance(m.getPosInShip());
				if (distance > (radius - 16) * 2) {
					radius = distance / 2 + 16;
					center = new Vect3D();
					center.add(newMorph.getPosInShip());
					center.add(m.getPosInShip());
					center.normalize(center.modulus() / 2);
				}
			}
		} else {
			List<Long> morphIds = new ArrayList<Long>(morphsById.keySet());
			float distance = 0;
			radius = 0;

			for (int i = 0; i < morphIds.size(); i++) {
				Morph refMorph = morphsById.get(morphIds.get(i));
				for (int j = i; j < morphIds.size(); j++) {
					Morph m = morphsById.get(morphIds.get(j));
					distance = refMorph.getPosInShip().distance(m.getPosInShip());
					if (distance > (radius - 16) * 2) {
						radius = distance / 2 + 16;
						center = new Vect3D();
						center.add(refMorph.getPosInShip());
						center.add(m.getPosInShip());
						center.normalize(center.modulus() / 2);
					}
				}
			}
		}
	}

	/**
	 * Get the neighbors of the provided Morph.
	 * Works in 2D only for now.
	 * Should be put into MorphUtil
	 * @param morph
	 * @return
	 */
	public final List<Morph> findNeighbors(Morph morph) {
		List<Morph> neighbors = new ArrayList<Morph>();
		// .1 2
		// 3...4
		// .5 6
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x - 1, (int) morph.getPosInShipGrid().y + 1, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x, (int) morph.getPosInShipGrid().y + 1, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x - 1, (int) morph.getPosInShipGrid().y, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x + 1, (int) morph.getPosInShipGrid().y, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x, (int) morph.getPosInShipGrid().y - 1, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x + 1, (int) morph.getPosInShipGrid().y - 1, (int) morph.getPosInShipGrid().z));
		return neighbors;
	}

	/**
	 * See {@link #findShipMorph(Vect3D)}.
	 * @param x the x position in ship of the morph we look for.
	 * @param y the y position in ship of the morph we look for.
	 * @param z the z position in ship of the morph we look for.
	 */
	public final Morph findShipMorph(float x, float y, float z) {
		return findShipMorph(new Vect3D(x, y, z));
	}

	/**
	 * Looks for the {@link Morph} at the specified position in the ship.
	 * If there is no morph at the given position, it returns null.
	 * @param pos the position in the ship
	 * @return null if there is no morph at the specified location.
	 */
	public final Morph findShipMorph(Vect3D pos) {
		return morphsByPosInShipGrid.get(pos);
	}

	/**
	 * Fire a ship stopped event to all ship listeners
	 * @param shipEvent
	 */
	private void fireShipStopped(ShipEvent shipEvent) {
		for (ShipListener shipListener : shipListeners) {
			shipListener.shipStopped(shipEvent);
		}
	}

	@Override
	public final ModifiableIterable<AI<Ship>> getAIList() {
		return iaList;
	}

	/**
	 * @return the geometric center the of the ship.
	 * The center of the circum circle of the ship.
	 */
	public final Vect3D getCenter() {
		return center;
	}

	/**
	 * @return COM ship coordinates
	 */
	public final Vect3D getComInShip() {
		return comInShip;
	}

	/**
	 * @return the drag factor
	 */
	private final float getDragFactor() {
		return dragFactor;
	}

	/**
	 * @return a unmodifiable map of the ship morphs organised by their id.
	 */
	public final Map<Long, Morph> getMorphsByIds() {
		return Collections.unmodifiableMap(morphsById);
	}

	/**
	 * @return a unmodifiable map of the ships morphs 
	 * organised by position in ship grid (the keys are instances of Vect3D using whole numbers)
	 */
	public final Map<Vect3D, Morph> getMorphsByPosInShipGrid() {
		return Collections.unmodifiableMap(morphsByPosInShipGrid);
	}

	/**
	 * This works if <b>type</b> is exactly the type of the desired morphs. 
	 * @param type
	 * @return the list of morph of type <b>type</b> in the ship's morphs.
	 */
	public final <T extends Morph> List<T> getMorphsByType(Class<T> type) {
		// This is dangerous if we don't know what we are doing.
		// In order for this to work, we must be sure that the morphsByType map
		// is properly filled.
		List<Morph> list = morphsByType.get(type);
		if (list == null) {
			return Collections.unmodifiableList(Collections.<T>emptyList());
		}
		return (List<T>) Collections.unmodifiableList(list);
	}

	/**
	 * @return the ship owner
	 */
	public final User getOwner() {
		return owner;
	}

	/**
	 * @return the list of the forces applied to the ship during the current frame.
	 */
	public final List<Force> getOwnForceList() {
		return ownForceList;
	}

	@Override
	public final Vect3D getPos() {
		return pos;
	}

	@Override
	public final Vect3D getPosAccel() {
		return posAccel;
	}

	@Override
	public final Vect3D getPosSpeed() {
		return posSpeed;
	}

	/**
	 * The radius is only computed when a morph is added/removed from the ship.
	 * @return the radius of the ship.
	 */
	public final float getRadius() {
		return radius;
	}

	@Override
	public final float getRot() {
		return rot;
	}

	@Override
	public final float getRotAccel() {
		return rotAccel;
	}

	@Override
	public final float getRotSpeed() {
		return rotSpeed;
	}

	/**
	 * @return the amount of mass stored in the ship.
	 * This mass can increase when mining for instance.
	 * This mass is available for all morphs of the ship in need of matter.
	 */
	public final float getStoredMass() {
		return storedMass;
	}

	/**
	 * @return the ship total mass (morph's mass + stored mass).
	 */
	public final float getTotalMass() {
		float totalMass = 0;
		for (Morph m : getMorphsByIds().values()) {
			totalMass += m.getMass();
		}
		totalMass += storedMass;

		return totalMass;
	}

	/**
	 * Adds a morph to the ship's morphs.
	 * We maintain several maps / lists.
	 * This method is used to ease adding a morph to all these containers.
	 * @param newMorph the morph to add
	 * @param x position along ship grid x axis
	 * @param y position along ship grid y axis
	 * @param z position along ship grid é axis (not used for now)
	 */
	private final void putMorphInMapsAndLists(Morph newMorph, float x, float y, float z) {
		morphsById.put(newMorph.getId(), newMorph);
		morphsByPosInShipGrid.put(new Vect3D(x, y, z), newMorph);
		List<Morph> list = morphsByType.get(newMorph.getClass());
		if (list == null) {
			list = new ArrayList<Morph>();
			morphsByType.put(newMorph.getClass(), list);
		}
		list.add(newMorph);
	}

	/**
	 * Removes a morph from the ship
	 * @param morph the morph to remove.
	 */
	public final void removeMorph(Morph morph) {
		UIModel.getUiModel().getSelectionModel().removeMorphFromSelection(morph);
		morphsById.remove(morph.getId());
		morphsByPosInShipGrid.remove(morph.getPosInShipGrid());
		morphsByType.get(morph.getClass()).remove(morph);

		// check if this is the last propulsor morph
		// if this is the case, we have to add a PropulsorsLost behavior to one of the ship's morphs
		if (morphsByType.get(PropulsorMorph.class).size() == 0 && morphsById.size() > 0) {
			final Morph standingMorph = morphsById.values().iterator().next();
			standingMorph.getActivationIsolatedBehaviorList().add(new PropulsorsLost(standingMorph));
		}
	}

	/**
	 * Remove a collection of morphs from the ship.
	 * @param morphs the morphs to remove.
	 */
	public final void removeMorphs(Collection<? extends Morph> morphs) {
		if (morphs == null) {
			return;
		}

		for (Morph morph : morphs) {
			removeMorph(morph);
		}
	}

	/**
	 * Remove a listener from the ship listener's list.
	 * @param shipListener the ship listener to remove.
	 */
	public final void removeShipListeners(ShipListener shipListener) {
		shipListeners.remove(shipListener);
	}

	/**
	 * For now, this is used only in the {@link PropulsorsLost} behavior.
	 * It is obviously sub-optimal.
	 */
	public final void resetDragFactor() {
		dragFactor = ModelConstants.INITIAL_DRAG_FACTOR;
	}

	/**
	 * @param dragFactor the drag factor
	 */
	public void setDragFactor(float dragFactor) {
		this.dragFactor = dragFactor;
	}

	/**
	 * @param owner the user owner of the ship.
	 */
	protected void setOwner(User owner) {
		this.owner = owner;
	}

	/** 
	 * @param pos the position of the ship in the world.
	 */
	public final void setPos(Vect3D pos) {
		this.pos = new Vect3D(pos);
	}

	/**
	 * @param posAccel the acceleration of the ship.
	 */
	protected final void setPosAccel(Vect3D posAccel) {
		this.posAccel = new Vect3D(posAccel);
	}

	/**
	 * @param posSpeed the speed of the ship.
	 */
	protected final void setPosSpeed(Vect3D posSpeed) {
		this.posSpeed = new Vect3D(posSpeed);
	}

	/**
	 * @param rot the rotation angle of the ship in degrees (0-360)
	 */
	protected final void setRot(float rot) {
		this.rot = rot;
	}

	/**
	 * @param rotAccel the rotation acceleration of the ship, in degrees^2/s.
	 */
	protected final void setRotAccel(float rotAccel) {
		this.rotAccel = rotAccel;
	}

	/**
	 * @param rotSpeed the rotation speed of the ship, in degrees/s.
	 */
	protected final void setRotSpeed(float rotSpeed) {
		this.rotSpeed = rotSpeed;
	}

	/**
	 * @param storedMass the new value for the amount of stored mass.
	 */
	public final void setStoredMass(float storedMass) {
		this.storedMass = storedMass;
	}

	@Override
	public String toString() {
		return "ship:" + pos.toString();
	}

	/**
	 * Transforms the provided vector from ship referential coordinates to world referential coordinates.
	 * @param coords the coordinates in ship referential
	 */
	public final void transformShipToWorldCoords(Vect3D coords) {
		coords.substract(getComInShip());
		coords.rotate(rot);
		coords.add(pos);
		coords.add(getComInShip());
	}

	/**
	 * Transforms the provided vector from world referential coordinates to ship referential coordinates.
	 * @param coords the coordinates in world referential
	 */
	public final void transformWorldToShipCoords(Vect3D coords) {
		coords.substract(pos);
		coords.rotate(-rot);
	}

	/**
	 * Update the ship position, speed, acceleration, rotation, rotation speed, 
	 * rotation acceleration, applies forces, update morphs, etc.
	 */
	public final void update() {
		// timestamp of last update
		float secondsSinceLastUpdate = ((float) World.getWorld().getCurrentTS() - lastUpdateTS) / 1000;
		lastUpdateTS = World.getWorld().getCurrentTS();
		if (secondsSinceLastUpdate == 0f) {
			return;
		}

		applyForces();

		posSpeed.x += posAccel.x * secondsSinceLastUpdate;
		posSpeed.y += posAccel.y * secondsSinceLastUpdate;
		posSpeed.z += posAccel.z * secondsSinceLastUpdate;
		LOGGER.trace("posAccel: " + posAccel + ", posSpeed: " + posSpeed);

		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
		float reducedDragFactor = 1 - (1 - getDragFactor()) * secondsSinceLastUpdate;
		posSpeed.x = Math.abs(posSpeed.x * reducedDragFactor) > ModelConstants.MIN_SPEED ? posSpeed.x * reducedDragFactor : 0;
		posSpeed.y = Math.abs(posSpeed.y * reducedDragFactor) > ModelConstants.MIN_SPEED ? posSpeed.y * reducedDragFactor : 0;
		posSpeed.z = Math.abs(posSpeed.z * reducedDragFactor) > ModelConstants.MIN_SPEED ? posSpeed.z * reducedDragFactor : 0;

		// If this ship is stopped, fire a shipStopped event
		if (posSpeed.x == 0 && posSpeed.y == 0 && posSpeed.z == 0) {
			fireShipStopped(new ShipEvent(this));
		}

		pos.x += posSpeed.x * secondsSinceLastUpdate;
		pos.y += posSpeed.y * secondsSinceLastUpdate;
		pos.z += posSpeed.z * secondsSinceLastUpdate;

		rotSpeed += rotAccel * secondsSinceLastUpdate;
		// rotSpeed = 5; // FIXME Remove that

		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
		float reducedRotDragFactor = 1 - (1 - ModelConstants.ROT_DRAG_FACTOR) * secondsSinceLastUpdate;
		rotSpeed = Math.abs(rotSpeed * reducedRotDragFactor * 0.995f) > ModelConstants.MIN_SPEED ? rotSpeed * reducedRotDragFactor * 0.995f : 0;

		rot = (rot + rotSpeed * secondsSinceLastUpdate) % 360;
		// rot = 90; // FIXME remove that

		updateMorphs();

		// execute the behaviors
		for (Behavior<Ship> behavior : alwaysActiveBehaviors) {
			behavior.tryToExecute();
		}
	}

	/**
	 * Update the morphs of the ship.
	 */
	private final void updateMorphs() {
		// We duplicate the collection so as to avoid ConcurrentModificationException
		// TODO We should think about fixing this otherwise
		Collection<Morph> morphs = new ArrayList<Morph>(morphsById.values());

		// Count the number of suboptimally massive morphs
		int nbMorphsNeedingMass = 0;
		for (Morph m : morphs) {
			if (m.getMass() < m.getClass().getAnnotation(MorphInfo.class).maxMass()) {
				nbMorphsNeedingMass++;
			}
		}

		for (Morph m : morphs) {

			// Updating the mass of the ship's morph if needed
			// TODO transform this in a behavior
			if (m.getMass() < m.getClass().getAnnotation(MorphInfo.class).maxMass()
					&& nbMorphsNeedingMass > 0) {

				if (getStoredMass() > 0) {
					m.setMass(m.getMass() + getStoredMass() / nbMorphsNeedingMass);
				}
			}

			m.update();
		}
	}
}
