package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Lapse;

import java.rmi.RemoteException;

public interface RegressionSelector extends Selector {
	void setLapse(Lapse lapse) throws RemoteException;
}
