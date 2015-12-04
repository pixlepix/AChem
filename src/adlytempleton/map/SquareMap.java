package adlytempleton.map;

import adlytempleton.atom.Atom;
import adlytempleton.gui.SquareMapFrame;
import adlytempleton.reaction.ReactionData;
import com.google.gson.annotations.Expose;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ATempleton on 11/7/2015.
 * <p>
 * Basic implementation of ILocation on a square grid
 */
public class SquareMap extends AbstractMap {

    private int size;

    private transient SquareMapFrame renderer;

    //The main HashMap that stores locations of all atoms.
    //If an atom is not present at an location, the location should not be a key in the hashmap.
    private ConcurrentHashMap<SquareLocation, Atom> atomMap = new ConcurrentHashMap<SquareLocation, Atom>();

    public SquareMap(int size) {
        this.size = size;

        renderer = new SquareMapFrame(this);
    }

    @Override
    public boolean move(Atom atom, ILocation newLocation) {
        assert newLocation instanceof SquareLocation;


        if (!atomMap.containsKey(newLocation)) {


            //Remove the atom from the current map
            atomMap.remove(atom.getLocation());

            //Update the location in the Atom object
            atom.setLocation(newLocation);

            //Insert the atom in it's new location
            atomMap.put((SquareLocation) newLocation, atom);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Class<? extends ILocation> getLocationType() {
        return SquareLocation.class;
    }

    @Override
    public Atom getAtomAtLocation(ILocation location) {
        assert location instanceof SquareLocation;

        return atomMap.get(location);
    }

    @Override
    public ArrayList<ILocation> getAdjacentLocations(ILocation location) {

        assert location instanceof SquareLocation;

        int x = ((SquareLocation) location).getX();
        int y = ((SquareLocation) location).getY();

        ArrayList<ILocation> result = new ArrayList<ILocation>();

        //A constant 'table' of nearby positions is used
        result.add(new SquareLocation(x - 1, y));
        result.add(new SquareLocation(x + 1, y));
        result.add(new SquareLocation(x, y - 1));
        result.add(new SquareLocation(x, y + 1));
        result.add(new SquareLocation(x - 1, y - 1));
        result.add(new SquareLocation(x + 1, y + 1));
        result.add(new SquareLocation(x + 1, y - 1));
        result.add(new SquareLocation(x - 1, y + 1));

        //Check that all these locations lie within the boundaries of the grid
        Iterator iter = result.iterator();
        while (iter.hasNext()) {
            if (!isOnGrid((ILocation) iter.next())) {
                iter.remove();
            }
        }

        return result;
    }


    @Override
    public boolean crossed(ILocation loc11, ILocation loc12, ILocation loc21, ILocation loc22) {
        /**
         * Two bonds are crossed if their coordinates are interwoven.
         * And they are not parallel
         */

        //Convert ILocations
        SquareLocation sq11 = (SquareLocation) loc11;
        SquareLocation sq12 = (SquareLocation) loc12;
        SquareLocation sq21 = (SquareLocation) loc21;
        SquareLocation sq22 = (SquareLocation) loc22;

        boolean xInterwoven = numbersInterwoven(sq11.getX(), sq12.getX(), sq21.getX(), sq22.getX());
        boolean yInterwoven = numbersInterwoven(sq11.getY(), sq12.getY(), sq21.getY(), sq22.getY());

        //Check if lines are parallel
        //The ternary operator is user as a zero check
        double slope1 = sq11.getY() == sq12.getY() ? Double.MAX_VALUE : (sq11.getX() - sq12.getX()) / (sq11.getY() - sq12.getY());

        double slope2 = sq21.getY() == sq22.getY() ? Double.MAX_VALUE : (sq21.getX() - sq22.getX()) / (sq21.getY() - sq22.getY());
        boolean parallel = slope1 == slope2;

        return (xInterwoven && yInterwoven) && !parallel;
    }

    /**
     * Helper method to determine if four coordinates are crossed on one axis
     *
     * @param a1 First coord of first bond
     * @param a2 Second coord of first bond
     * @param b1 First coord of second bond
     * @param b2 Second coord of second bond
     * @return
     */
    private boolean numbersInterwoven(int a1, int a2, int b1, int b2) {

        //Ensure that all the coords are in the proper order
        //ie. That a1 <= a2, and that b1 <= b2
        if (a1 > a2) {
            int c = a2;
            a2 = a1;
            a1 = c;
        }

        if (b1 > b2) {
            int c = b2;
            b2 = b1;
            b1 = c;
        }

        /**
         * If the atoms are not interwoven
         * The lower value of one is more than the highest value of the other, or vice-versa
         */
        return !(a2 < b1 || a1 > b2);
    }

    @Override
    /**
     * Implementation of getDistance based on distance
     * For this calculation, one unit distance is defined as:
     * One square horizontally, vertically, and diagonally at a 45-degree
     * This choice is made for certain purposes - particuarily the calculation of Morre neighborhoods for bonds
     */
    public int getDistance(ILocation loc1, ILocation loc2) {

        assert loc1 instanceof SquareLocation;
        assert loc2 instanceof SquareLocation;

        int diffX = Math.abs(((SquareLocation) loc1).getX() - ((SquareLocation) loc2).getX());
        int diffY = Math.abs(((SquareLocation) loc1).getY() - ((SquareLocation) loc2).getY());

        return Math.max(diffX, diffY);
    }

    @Override
    public List<Atom> getAllAtoms() {
        return new ArrayList<>(atomMap.values());
    }

    @Override
    public boolean isOnGrid(ILocation location) {
        assert location instanceof SquareLocation;

        return ((SquareLocation) location).getX() < size && ((SquareLocation) location).getX() > -1
                && ((SquareLocation) location).getY() < size && ((SquareLocation) location).getY() > -1;
    }

    @Override
    public List<ILocation> newlyInRange(ILocation startLoc, ILocation endLoc, int range) {
        assert endLoc instanceof SquareLocation;
        assert startLoc instanceof SquareLocation;

        SquareLocation end = (SquareLocation) endLoc;
        SquareLocation start = (SquareLocation) startLoc;

        int deltaX = end.getX() - start.getX();
        int deltaY = end.getY() - start.getY();

        LinkedList<ILocation> result = new LinkedList<>();

        //We want a line of length 2*range + 1
        //This line is the edge of all cells contained within the range
        //If we shift in multiple dimensions, we simply take the union
        if (deltaX != 0) {
            //deltaX is used as the sign
            int x = end.getX() + range * deltaX;
            for (int y = end.getY() - range; y <= end.getY() + range; y++) {
                result.add(new SquareLocation(x, y));
            }
        }

        //Same calculations as above
        if (deltaY != 0) {
            int y = end.getY() + range * deltaY;
            for (int x = end.getX() - range; x <= end.getX() + range; x++) {
                result.add(new SquareLocation(x, y));
            }
        }

        //Remove the overlap
        if (deltaX != 0 && deltaY != 0) {
            result.remove(new SquareLocation(end.getX() + deltaX * range, end.getY() + deltaY * range));
        }

        //Check to make sure that all elements are contained in the list
        Iterator iter = result.iterator();
        while (iter.hasNext()) {
            if (!isOnGrid((ILocation) iter.next())) {
                iter.remove();
            }
        }

        return result;
    }

    @Override
    public ArrayList<ILocation> getAllLocations() {
        ArrayList result = new ArrayList();

        for(int x = 0; x < size; x ++){
            for(int y = 0; y < size; y ++){
                result.add(new SquareLocation(x, y));
            }
        }
        return result;
    }

    @Override
    public void updateEnzymes(Atom atom, ReactionData[] newReaction) {
        //First, take it out of all current enzyme mappings
        for (ReactionData rxn : atom.getReactions()) {
            if (rxn != null) {
                enzymes.remove(rxn, atom);
            }
        }

        //Then, add the new ones
        for (ReactionData rxn : newReaction) {
            if (rxn != null) {
                enzymes.put(rxn, atom);
            }
        }
    }

    @Override
    public boolean addAtom(ILocation location, Atom atom) {

        assert location instanceof SquareLocation;

        if (!atomMap.containsKey(location)) {
            atom.setLocation(location);
            atomMap.put((SquareLocation) location, atom);

            //Add to enzyme map
            if (atom.isEnzyme()) {
                for (ReactionData rxn : atom.getReactions()) {
                    if (rxn != null) {
                        enzymes.put(rxn, atom);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void render() {
        renderer.repaint();
    }

    public int getSize() {
        return size;
    }
}
