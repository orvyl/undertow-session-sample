package me.orvyl.security;

import io.undertow.server.session.SessionCookieConfig;

public class Configuration {

    public static final SessionCookieConfig SESSION_COOKIE_CONFIG = new SessionCookieConfig();
    static {
        SESSION_COOKIE_CONFIG.setCookieName("rules_engine_session_cookie");
    }




}
