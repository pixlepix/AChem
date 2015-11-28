package adlytempleton.map;

import adlytempleton.atom.Atom;
import adlytempleton.reaction.ReactionManager;
import adlytempleton.simulator.SimulatorConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by ATempleton on 11/7/2015.
 * <p>
 * Performs all the heavy logic of updating and moving atoms
 */
public class Simulator {

    //Constants of simulation

    //Stores Locations which have been updated by a reaction or movement
    //And should be reachecked next tick
    public ArrayList<ILocation> updatedLocations = new ArrayList<>();
    AbstractMap map;

    /**
     * Constructs a new map
     *
     * @param map The AbstractMap used for a grid
     */
    public Simulator(AbstractMap map) {
        this.map = map;
    }

    /**
     * Main simulation method. Updates all elements of the simulation
     */
    public void tick() {

        Random rand = new Random();

        //Update all atoms which have changed
        //We want to add in more locations while this check is ongoing
        //Which wont be processed until the next tick
        ArrayList<ILocation> updatedLocationsCopy = (ArrayList<ILocation>) updatedLocations.clone();
        updatedLocations.clear();
        for (ILocation location : updatedLocationsCopy) {
            reactAround(location);
        }

        //Move all atoms
        for (Atom atom : map.getAllAtoms()) {
            if (rand.nextDouble() < SimulatorConstants.MOVEMENT_CHANCE) {
                ArrayList<ILocation> nearbySpaces = map.getAdjacentLocations(atom.getLocation());
                ILocation newLocation = nearbySpaces.get(rand.nextInt(nearbySpaces.size()));



                //Emptyness of resulting space is checked in move function
                //Note that this means that the actial chance of movement is significantly less that MOVEMENT_CHANCE
                if (!willStretchBonds(atom, newLocation) && !willCrossBonds(atom, newLocation)) {

                    //Checks if the atom is an enzyme
                    //if it is, mark all cells for update which are now in it's range
                    //Note that this is only marking them for future use - so we call this before we move the enzyme
                    if(atom.isEnzyme()){
                        updateReactions(atom.getLocation(), newLocation);
                    }

                    map.move(atom, newLocation);
                    reactAround(newLocation);
                }


            }
        }

        //Re-render after components have changed
        map.render();
    }

    /**
     * Uses the result of AbstractMap.newlyInRange
     * To perform all potential reactions
     * On the next tick. Uses updatedLocations as a queue
     */
    public void updateReactions(ILocation start, ILocation end){
        updatedLocations.addAll(map.newlyInRange(start, end, SimulatorConstants.ENZYME_RANGE));
    }

    /**
     * Checks if a movement would stretch bonds beyond capacity
     *
     * @param atom        The atom to be moved
     * @param newLocation The location to which the atom will be moved
     * @return True if a movement is invalid, false otherwise
     */
    private boolean willStretchBonds(Atom atom, ILocation newLocation) {
        for (Atom bondedAtom : atom.bonds) {
            if (map.getDistance(newLocation, bondedAtom.getLocation()) > 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a movement will cross a bond
     *
     * @param atom        The atom to be moved
     * @param newLocation The location in which the atom will be moved
     * @return True if the movement is invalid
     */
    private boolean willCrossBonds(Atom atom, ILocation newLocation) {
        for (Atom bondedAtom : atom.bonds) {

            //We want to look at all atoms adjacent to one of the components
            ArrayList<Atom> nearbyAtoms = map.getAdjacentAtoms(newLocation);
            nearbyAtoms.addAll(map.getAdjacentAtoms(bondedAtom.getLocation()));
            //We remove the original atom twice as we are searching around the new location, not the current one
            nearbyAtoms.remove(atom);
            nearbyAtoms.remove(atom);
            nearbyAtoms.remove(bondedAtom);

            //Cycle through all atoms bonded to these
            //This is inefficient by a factor of two
            //But this shouldn't be a performance intensive step
            for (Atom nearbyAtom : nearbyAtoms) {
                for (Atom nearbyBondedAtom : nearbyAtom.bonds) {
                    if (nearbyBondedAtom != atom && nearbyBondedAtom != bondedAtom && map.crossed(newLocation, bondedAtom.getLocation(), nearbyAtom.getLocation(), nearbyBondedAtom.getLocation())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Checks all atoms adjacent to the given location
     * For reactions
     *
     * @param centralLocation Location of the main atom
     */
    public void reactAround(ILocation centralLocation) {
        Atom centralAtom = map.getAtomAtLocation(centralLocation);

        for (ILocation location : map.getAdjacentLocations(centralLocation)) {
            Atom atom = map.getAtomAtLocation(location);

            if (atom != null && centralAtom != null) {
                if (ReactionManager.react(atom, centralAtom, map, this)) {
                    //Because the state has changed, we must check atoms around to propagate reactions
                    //We want this to take effect once per tick, to preserve locality, among other things (such as infinite recursion
                    updatedLocations.add(location);
                    updatedLocations.add(centralLocation);
                }
            }
        }
    }

}