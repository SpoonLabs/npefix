package fr.inria.spirals.npefix.main;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.selector.GreedySelector;
import fr.inria.spirals.npefix.resi.selector.Selector;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DecisionServer {

	private Thread thread;

	public static void main(String[] args) {
		try {
			Selector selector = new GreedySelector();
			System.out.println("Start selector " + selector);
			Selector skeleton = (Selector) UnicastRemoteObject.exportObject(selector, 10000);
			Registry registry = LocateRegistry.createRegistry(10000);
			registry.rebind("Selector", skeleton);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Selector selector;
	private int port = Config.CONFIG.getServerPort();

	public DecisionServer(Selector selector) {
		this.selector = selector;
	}

	public void startServer() {
		this.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Start selector " + selector);
					Selector skeleton = (Selector) UnicastRemoteObject.exportObject(selector, port);
					Registry registry = LocateRegistry.createRegistry(port);
					registry.rebind("Selector", skeleton);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		thread.run();
	}

	public void stopServer() {
		thread.interrupt();
	}
}
