package me.orvyl;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.server.session.*;

import java.time.LocalDateTime;
import java.util.Objects;

public class Application {

    private static final SessionCookieConfig sessionConfig = new SessionCookieConfig();
    static {
        sessionConfig.setCookieName("my-cookie");
    }

    public static void main(String[] args) {
        PathHandler pathHandler = new PathHandler();
        pathHandler.addExactPath("/", secureHandler(new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
                final SessionManager manager = httpServerExchange.getAttachment(SessionManager.ATTACHMENT_KEY);
                System.out.println("++ Active Sessions ++");
                manager.getActiveSessions().forEach(s -> System.out.print(s + " "));
                System.out.println();
                System.out.println("++ end ++");

                httpServerExchange.getResponseSender().send("I'm protected!");
            }
        }));
        pathHandler.addExactPath("/logout", new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange serverExchange) throws Exception {
                final SessionManager manager = serverExchange.getAttachment(SessionManager.ATTACHMENT_KEY);

                Session currentSession = manager.getSession(serverExchange, sessionConfig);
                if (Objects.nonNull(currentSession)) {
                    currentSession.invalidate(serverExchange);

                    HttpHandler httpHandler = new RedirectHandler("/");
                    httpHandler.handleRequest(serverExchange);
                }

                System.out.println("session is null");
            }
        });
        pathHandler.addExactPath("/login", new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
                final SessionManager manager = httpServerExchange.getAttachment(SessionManager.ATTACHMENT_KEY);
                 Session currentSession = manager.getSession(httpServerExchange, sessionConfig);

                 if (Objects.isNull(currentSession)) {
                     currentSession = manager.createSession(httpServerExchange, sessionConfig);
                     currentSession.setAttribute("username", LocalDateTime.now());
                     System.out.println("New session created --> " + currentSession.getId());
                 } else {
                     System.out.println("session not null. redirecting ...");
                 }

                 HttpHandler httpHandler = new RedirectHandler("/");

                 httpHandler.handleRequest(httpServerExchange);
            }
        });

        Undertow server = Undertow.builder().addHttpListener(8080, "localhost")
                .setHandler(new SessionAttachmentHandler(new ErrorHandler(pathHandler), new InMemorySessionManager("SessionManager"), new SessionCookieConfig())).build();
        server.start();
    }

    public static HttpHandler secureHandler(HttpHandler next) {

        return serverExchange -> {
            final SessionManager manager = serverExchange.getAttachment(SessionManager.ATTACHMENT_KEY);

            if (Objects.isNull(manager)) {
                System.out.println("sessionManager is null");
            } else {
                Session currentSession = manager.getSession(serverExchange, sessionConfig);

                if (Objects.isNull(currentSession)) {
                    System.out.println("session is null. redirecting to login page");

                    HttpHandler httpHandler = new RedirectHandler("/login");
                    httpHandler.handleRequest(serverExchange);

                } else {
                    System.out.println("currentSession --> " + currentSession.getId() + " username " + currentSession.getAttribute("username"));
                }
            }


            next.handleRequest(serverExchange);
        };
    }

}

//https://www.programcreek.com/java-api-examples/?api=io.undertow.server.session.SessionAttachmentHandler
//http://useof.org/java-open-source/io.undertow.server.session.SessionAttachmentHandler
//https://www.pac4j.org/docs/clients.html