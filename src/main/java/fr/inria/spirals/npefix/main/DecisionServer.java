package fr.inria.spirals.npefix.main;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.selector.ExplorerSelector;
import fr.inria.spirals.npefix.resi.selector.Selector;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat4;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DecisionServer {

	private Thread thread;

	public static void main(String[] args) {
		try {

			Selector selector = new ExplorerSelector(new Strat4(ReturnType.NULL), new Strat4(ReturnType.VAR), new Strat4(ReturnType.NEW), new Strat4(ReturnType.VOID));
			System.out.println("Start selector " + selector);

			startRMI(selector);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Registry startRMI(Selector selector) {
		Registry registry;

		int port = Config.CONFIG.getServerPort();
		String host = Config.CONFIG.getServerHost();

		Selector skeleton;
		try {
			skeleton = (Selector) UnicastRemoteObject.exportObject(selector, port);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		try{
			LocateRegistry.getRegistry(host, port).list();
			registry = LocateRegistry.getRegistry(host, port);
		}catch(Exception ex){
			try{
				registry = LocateRegistry.createRegistry(port);
			} catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		try {
			registry.rebind(Config.CONFIG.getServerName(), skeleton);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		return registry;
	}

	private Selector selector;
	private int port = Config.CONFIG.getServerPort();
	private String host = Config.CONFIG.getServerHost();

	public DecisionServer(Selector selector) {
		this.selector = selector;
	}

	public void startServer() {
		this.thread = new Thread(new Runnable() {
			Registry registry;

			@Override
			public void run() {
				startRMI(selector);
			}


		});
		thread.run();
	}

	public void stopServer() {
		thread.interrupt();
	}
}
