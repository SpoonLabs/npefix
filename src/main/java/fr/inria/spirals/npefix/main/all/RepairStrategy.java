package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.selector.Selector;
import spoon.processing.AbstractProcessor;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/07/17
 */
public interface RepairStrategy {

	List<AbstractProcessor> getListOfProcessors();

	NPEOutput run(Selector selector, List<String> methodTests);

}
