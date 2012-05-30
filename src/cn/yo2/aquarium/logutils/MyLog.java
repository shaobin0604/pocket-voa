package cn.yo2.aquarium.logutils;

import android.util.Log;

/**
 * This class defines the Logger
 */

public final class MyLog {
	private static String TAG = "POCKET VOA";
	private static boolean LOG_ENABLE = true;
	private static boolean DETAIL_ENABLE = true;

	private MyLog() {
	}

	private static String buildMsg(String msg) {
		StringBuilder buffer = new StringBuilder();

		if (DETAIL_ENABLE) {
			final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];

			buffer.append("[ ");
			buffer.append(Thread.currentThread().getName());
			buffer.append(": ");
			buffer.append(stackTraceElement.getFileName());
			buffer.append(": ");
			buffer.append(stackTraceElement.getLineNumber());
			buffer.append(": ");
			buffer.append(stackTraceElement.getMethodName());
		}

		buffer.append("() ] _____ ");

		buffer.append(msg);

		return buffer.toString();
	}


	public static void v(String msg) {
		if (LOG_ENABLE && Log.isLoggable(TAG, Log.VERBOSE)) {
			Log.v(TAG, buildMsg(msg));
		}
	}


	public static void d(String msg) {
		if (LOG_ENABLE && Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, buildMsg(msg));
		}
	}


	public static void i(String msg) {
		if (LOG_ENABLE && Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, buildMsg(msg));
		}
	}


	public static void w(String msg) {
		if (LOG_ENABLE && Log.isLoggable(TAG, Log.WARN)) {
			Log.w(TAG, buildMsg(msg));
		}
	}


	public static void w(String msg, Exception e) {
		if (LOG_ENABLE && Log.isLoggable(TAG, Log.WARN)) {
			Log.w(TAG, buildMsg(msg), e);
		}
	}


	public static void e(String msg) {
		if (LOG_ENABLE && Log.isLoggable(TAG, Log.ERROR)) {
			Log.e(TAG, buildMsg(msg));
		}
	}


	public static void e(String msg, Exception e) {
		if (LOG_ENABLE && Log.isLoggable(TAG, Log.ERROR)) {
			Log.e(TAG, buildMsg(msg), e);
		}
	}

	public static void initLog(String tag, boolean logEnable, boolean detailEnable) {
		TAG = tag;
		LOG_ENABLE = logEnable;
		DETAIL_ENABLE = detailEnable;
	}
}
