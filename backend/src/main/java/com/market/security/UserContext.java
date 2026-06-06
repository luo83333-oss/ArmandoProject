package com.market.security;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    public static int getRole() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getRole() : 0;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
