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

package adlytempleton.mutation;

import adlytempleton.atom.Atom;
import adlytempleton.atom.EnumType;
import adlytempleton.map.AbstractMap;
import adlytempleton.map.Simulator;
import adlytempleton.reaction.MutableReactionData;
import adlytempleton.reaction.ReactionData;
import adlytempleton.simulator.SimulatorConstants;

import java.util.*;

/**
 * Created by ATempleton on 1/31/2016.
 */
public class InsertionMutation implements IMutation{
    @Override
    public ReactionData[] mutate(ReactionData[] original, Random random, Atom atom, AbstractMap map) {
        List reactions = new ArrayList<>(Arrays.asList(original));
        reactions.removeAll(Collections.singleton(null));

        //If there was a null element
        if(reactions.size() < SimulatorConstants.ENZYME_CAPACITY){
            //The ReactionData to break up
            ReactionData toSplit = (ReactionData) reactions.get(random.nextInt());

            //The Reaction whose product will catalyze a reaction
            ReactionData catalyst = (ReactionData) reactions.get(random.nextInt());
            int catalystState = random.nextBoolean() ? catalyst.postState1 : catalyst.postState2;

            //The offset of the intermediate state
            int offset = (int) (random.nextGaussian() * 5D);

            //Whether to splt the first atom or the second atom
            boolean modifySecondAtom = random.nextBoolean();

            MutableReactionData part2;
            MutableReactionData part1 = MutableReactionData.fromReaction(toSplit);

            int finalState;
            if(modifySecondAtom){
                part1.postState2 += offset;
                part2 = new MutableReactionData(part1.type1, EnumType.Y, toSplit.postState2 + offset, catalystState, toSplit.postState2, catalystState, random.nextBoolean(), random.nextBoolean(), false);
            }else{
                part1.postState1 += offset;
                part2 = new MutableReactionData(part1.type1, EnumType.Y, toSplit.postState2 + offset, catalystState, toSplit.postState1, catalystState, random.nextBoolean(), random.nextBoolean(), false);
            }

            reactions.remove(toSplit);
            reactions.add(part1);
            reactions.add(part2);


        }
        return (ReactionData[]) reactions.toArray(new ReactionData[SimulatorConstants.ENZYME_CAPACITY]);
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public boolean isValidMutation(ReactionData[] original) {
        return false;
    }
}