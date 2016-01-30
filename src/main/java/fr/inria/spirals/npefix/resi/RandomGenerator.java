package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.config.Config;

import java.util.Random;

public class RandomGenerator {
	public static long seed = Config.CONFIG.getRandomSeed();

	private static Random generator = new Random(seed);

	public static int nextInt(){
		return generator.nextInt();
	}

	public static int nextInt(int max){
		return generator.nextInt(max);
	}

	public static int nextInt(int min, int max){
		return generator.nextInt(max - min) + min;
	}

	public static double nextDouble() {
		return generator.nextDouble();
	}

	public static void reset() {
		generator = new Random(seed);
	}

	public static Random getGenerator() {
		return generator;
	}
}
