package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface Selector extends Remote {

	<T> Decision<T> select(List<Decision<T>> decisions) throws RemoteException;

	boolean startLaps(Lapse lapse) throws RemoteException;

	boolean restartTest(Lapse lapse) throws RemoteException;

	List<Strategy> getStrategies() throws RemoteException;

	Set<Decision> getSearchSpace() throws RemoteException;

	List<Lapse> getLapses() throws RemoteException;

	Lapse getCurrentLapse() throws RemoteException;

	Lapse updateCurrentLapse(Lapse updatedLapse) throws RemoteException;

	void reset() throws RemoteException;
}
