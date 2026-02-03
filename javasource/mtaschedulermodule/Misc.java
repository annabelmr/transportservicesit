package mtaschedulermodule;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.objectmanagement.member.MendixBoolean;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class Misc {



	static class MFSerialExecutor {

		private static MFSerialExecutor _instance = new MFSerialExecutor();

		private final ExecutorService executor;

		public static MFSerialExecutor instance() {
			return _instance;
		}

		private MFSerialExecutor() {
			executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

				private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

				@Override
				public Thread newThread(Runnable runnable) {
					Thread t = defaultFactory.newThread(runnable);
					t.setPriority(Thread.MIN_PRIORITY);
					t.setName("Menditect background thread");
					return t;
				}

			});
		}

		public void execute(final Runnable command) {
			if (command == null) {
				throw new NullPointerException("command");
			}

			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						command.run();
					} catch (RuntimeException e) {
						throw e; 
					}
				}
			});
		}
	}


	public static Boolean runMicroflowInBackground(final IContext context, final String microflowName,
		final IMendixObject paramObject) {

		MFSerialExecutor.instance().execute(new Runnable() {

			@Override
			public void run() {
				try {
					IContext c = Core.createSystemContext();
					if (paramObject != null) {
						Core.executeAsync(c, microflowName, true, paramObject).get();
					} else {
						Core.executeAsync(c, microflowName, true, new HashMap<>()).get();
					}
				} catch (CoreException | InterruptedException | ExecutionException e) {
					throw new RuntimeException("Failed to run Async: " + microflowName + ": " + e.getMessage(), e);
				}

			}

		});
		return true;
	}
}
