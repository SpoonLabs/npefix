package fr.inria.spirals.npefix.transformer.utils;

import fr.inria.spirals.npefix.resi.context.MethodContext;
import fr.inria.spirals.npefix.resi.context.TryContext;

public interface IConstants{
	public interface Class {
		public static final String THROWABLE = "java.lang.Throwable";

		public static final String SUBMARINE_EXCEPTION = "bcornu.resi.exception.SubMarineException";
		public static final String INJECTED_EXCEPTION = "bcornu.resi.exception.InjectedException";
		public static final String CONSTRUCTOR_MANAGER = "bcornu.resi.manager.ConstructorManager";
		public static final String ACTION_PERFORMED_MANAGER = "bcornu.resi.manager.ActionPerformedManager";
		public static final String FAULT_INJECTOR ="bcornu.resi.manager.FaultInjector";
		public static final String RESILIENCE_MANAGER ="bcornu.resi.manager.ResilienceManager";
		public static final String FAKE_EXCEPTION_THROWER ="bcornu.resi.utils.FakeExceptionThrower";
		public static final String STATIC_MANAGER ="bcornu.resi.manager.StaticManager";
		public static final String SUBMARINE_MANAGER ="bcornu.resi.manager.SubMarineManager";
		public static final String EXCEPTION_CREATOR ="bcornu.resi.utils.ExceptionCreator";
		public static final String ABEND_MANAGER ="bcornu.resi.manager.AbendManager";
		public static final String CATCH_MANAGER ="bcornu.resi.manager.CatchManager";
		public static final String TRY_CONTEXT = TryContext.class.getCanonicalName();
		public static final String TRY_CONTEXT_IMPL = "bcornu.resi.context.TryContextImpl";
		public static final String FINALLY_MANAGER = "bcornu.resi.manager.FinallyManager";
		public static final String BLOCK_CONTEXT = "bcornu.resi.context.BlockContext";
		public static final String STATIC_CONTEXT = "bcornu.resi.context.StaticContext";
		public static final String CONSTRUCTOR_CONTEXT = "bcornu.resi.context.ConstructorContext";
		public static final String METHODE_CONTEXT = MethodContext.class.getCanonicalName();
		public static final String FAKE_INITIALIZER = "bcornu.resi.utils.FakeInitializer";
		public static final String THROW_MANAGER = "bcornu.resi.context.ThrowManager";
	}
	public interface Method{
		public static final String CONTEXT_LEARN_TRY_THROWN_EXCEPTION = "learnsThatTryHasThrownTheException";
		public static final String CONTEXT_CATCH_ACTIVATED = "setCatchActivated";
		public static final String CONTEXT_CATCH_VISITED = "setCatchVisited";
		public static final String CONTEXT_FINALLY_VISITED = "setFinallyVisited";
		public static final String CONTEXT_FINALLY_ACTIVATED = "setFinallyActivated";
		public static final String CONTEXT_LEARN_FINALLY_THROWN_EXCEPTION = "learnsThatFinallyHasThrownTheException";
		public static final String CONTEXT_TRY_FINISHED = "learnsThatTryHasFinished";
		public static final String BLOCK_CONTEXT_THROWS = "blockThrows";
		public static final String BLOCK_CONTEXT_COMPUTE = "compute";
		public static final String BLOCK_CONTEXT_START = "blockStart";
		public static final String BLOCK_CONTEXT_END = "blockEnd";
		public static final String TRY_CONTEXT_COMPUTE_ALL = "computeAll";
		public static final String TRY_CONTEXT_TRY_START = "tryStart";
		public static final String TRY_CONTEXT_TRY_THROWS = "tryThrows";
		public static final String TRY_CONTEXT_TRY_END = "tryEnd";
		public static final String TRY_CONTEXT_TRY_COMPUTE = "tryCompute";
		public static final String TRY_CONTEXT_CATCH_START = "catchStart";
		public static final String TRY_CONTEXT_CATCH_THROWS = "catchThrows";
		public static final String TRY_CONTEXT_CATCH_END = "catchEnd";
		public static final String TRY_CONTEXT_CATCH_COMPUTE = "catchCompute";
		public static final String TRY_CONTEXT_FINALLY_START = "finallyStart";
		public static final String TRY_CONTEXT_FINALLY_THROWS = "finallyThrows";
		public static final String TRY_CONTEXT_FINALLY_END = "finallyEnd";
		public static final String TRY_CONTEXT_FINALLY_COMPUTE = "finallyCompute";
		public static final String FAKE_INITIALIZER_INITALIZE = "initialize";
		public static final String TRY_CONTEXT_ALL_THROWS = "allThrows";
	}
	public interface Var{
		public static final String DEFAULT_THROWABLE = "_bcornu_t";
		public static final String TRY_CONTEXT_PREFIX = "_bcornu_try_context_";
		public static final String STATIC_CONTEXT = "_bcornu_static_context";
		public static final String METHODE_CONTEXT = "_bcornu_methode_context";
		public static final String CONSTRUCTOR_CONTEXT = "_bcornu_constructor_context";
		public static final String THROW_CONTEXT = "_bcornu_throw_context";
	}
	
}
