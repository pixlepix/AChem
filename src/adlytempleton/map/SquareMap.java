package adlytempleton.map;

import adlytempleton.atom.Atom;
import adlytempleton.gui.SquareMapFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ATempleton on 11/7/2015.
 * <p>
 * Basic implementation of ILocation on a square grid
 */
public class SquareMap extends AbstractMap {

    private int size;

    private SquareMapFrame renderer;

    //The main HashMap that stores locations of all atoms.
    //If an atom is not present at an location, the location should not be a key in the hashmap.
    private HashMap<SquareLocation, Atom> atomMap = new HashMap<SquareLocation, Atom>();

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
    public boolean addAtom(ILocation location, Atom atom) {

        assert location instanceof SquareLocation;

        if (!atomMap.containsKey(location)) {
            atom.setLocation(location);
            atomMap.put((SquareLocation) location, atom);
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
