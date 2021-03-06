/*
 * Copyright 2016 Adly Templeton
 *
 * This file is part of the AChem Simulator.
 *
 * The AChem Simulator is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The AChem Simulator is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 */

package adlytempleton.gui;

import adlytempleton.atom.Atom;
import adlytempleton.map.SquareLocation;
import adlytempleton.map.SquareMap;

import javax.swing.*;
import java.awt.*;

/**
 * Created by ATempleton on 11/7/2015.
 */
public class SquareMapPanel extends JPanel {

    //The map from which data is rendered
    SquareMap map;

    public SquareMapPanel(SquareMap map) {
        this.map = map;
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        //Basic initialization
        Graphics2D g = (Graphics2D) graphics;
        g.clearRect(0, 0, getWidth(), getHeight());

        //Width of individual grid cells
        int cellWidth = getWidth() / map.getSize();
        int cellHeight = getWidth() / map.getSize();

        //Set mouseover text
        if (getMousePosition() != null) {
            int mouseX = getMousePosition().x;
            int mouseY = getMousePosition().y;

            int mouseCellX = mouseX / cellWidth;
            int mouseCellY = mouseY / cellHeight;
            setToolTipText(String.format("(%d,%d)", mouseCellX, mouseCellY));
        }

        //Draw each atom onto the map
        for (Atom atom : map.getAllAtoms()) {

            //Cellular coordinates of the atoms
            int x = ((SquareLocation) atom.getLocation()).getX();
            int y = ((SquareLocation) atom.getLocation()).getY();

            //render each type of atom with a different color
            g.setColor(atom.type.color);
            if (atom.state == 0 && atom.bonds.isEmpty()) {

                g.fillOval(cellWidth * x, cellHeight * y, 6, 6);
            } else {

                g.fillOval(cellWidth * x, cellHeight * y, cellWidth, cellHeight);
            }

            //Render the state of the atom
            //The text should be at the atoms position + half of the cell width/height
            g.setFont(new Font("TimesRoman", Font.BOLD, 12));
            g.setColor(Color.BLACK);

            //The offset from cornet of the cell
            int textOffsetX = (cellWidth / 2);
            int textOffsetY = (cellHeight / 2);
            if (atom.state != 0) {
//                g.drawString("" + atom.state, x * cellWidth + textOffsetX, y * cellHeight + textOffsetY);
            }

            //Bonds
            //Find the position to render the center of the bonds
            int atomX = getCenter(x, cellWidth);
            int atomY = getCenter(y, cellHeight);

            for (Atom bondedAtom : atom.bonds) {
                //The location of the bonded atom
                SquareLocation bondedLocation = (SquareLocation) bondedAtom.getLocation();

                int bondedX = getCenter(bondedLocation.getX(), cellWidth);
                int bondedY = getCenter(bondedLocation.getY(), cellHeight);

                g.drawLine(atomX, atomY, bondedX, bondedY);
            }
        }

    }

    /**
     * Helped method to calculate the center of the atom. Primarily used for rendering bonds
     *
     * @param pos       The coordinates (gridwise) of the atom
     * @param cellWidth The width of each cell. Calculated from the window size / cell size
     * @return (Graphical) Coordinates of the center of the atom
     */
    public int getCenter(int pos, int cellWidth) {
        return pos * cellWidth + (cellWidth / 2);
    }
}
