package com.vonchange.utao.gecko.util;

import android.util.Log;

/**
 * 安全的日志工具类，避免 NullPointerException
 */
public class LogUtil {
    private static final String DEFAULT_TAG = "UtaoApp";
    private static final String NULL_MESSAGE = "null";

    /**
     * 记录调试信息
     */
    public static void d(String tag, String message) {
        Log.d(tag != null ? tag : DEFAULT_TAG, message != null ? message : NULL_MESSAGE);
    }

    /**
     * 记录信息
     */
    public static void i(String tag, String message) {
        Log.i(tag != null ? tag : DEFAULT_TAG, message != null ? message : NULL_MESSAGE);
    }

    /**
     * 记录警告
     */
    public static void w(String tag, String message) {
        Log.w(tag != null ? tag : DEFAULT_TAG, message != null ? message : NULL_MESSAGE);
    }

    /**
     * 记录错误
     */
    public static void e(String tag, String message) {
        Log.e(tag != null ? tag : DEFAULT_TAG, message != null ? message : NULL_MESSAGE);
    }

    /**
     * 记录错误和异常
     */
    public static void e(String tag, String message, Throwable throwable) {
        String safeTag = tag != null ? tag : DEFAULT_TAG;
        String safeMessage = message != null ? message : NULL_MESSAGE;

        if (throwable != null) {
            String throwableMessage = throwable.getMessage();
            if (throwableMessage != null) {
                Log.e(safeTag, safeMessage + ": " + throwableMessage, throwable);
            } else {
                Log.e(safeTag, safeMessage + ": " + throwable.getClass().getName(), throwable);
            }
        } else {
            Log.e(safeTag, safeMessage);
        }
    }

    /**
     * 使用默认标签记录调试信息
     */
    public static void d(String message) {
        d(DEFAULT_TAG, message);
    }

    /**
     * 使用默认标签记录信息
     */
    public static void i(String message) {
        i(DEFAULT_TAG, message);
    }

    /**
     * 使用默认标签记录警告
     */
    public static void w(String message) {
        w(DEFAULT_TAG, message);
    }

    /**
     * 使用默认标签记录错误
     */
    public static void e(String message) {
        e(DEFAULT_TAG, message);
    }

    /**
     * 使用默认标签记录错误和异常
     */
    public static void e(String message, Throwable throwable) {
        e(DEFAULT_TAG, message, throwable);
    }

    /**
     * 记录异常
     */
    public static void exception(String tag, Throwable throwable) {
        if (throwable != null) {
            e(tag, "Exception", throwable);
        } else {
            e(tag, "Unknown exception");
        }
    }

    /**
     * 使用默认标签记录异常
     */
    public static void exception(Throwable throwable) {
        exception(DEFAULT_TAG, throwable);
    }
}