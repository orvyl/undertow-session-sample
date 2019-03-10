package me.orvyl;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class ErrorHandler implements HttpHandler {

    private final HttpHandler next;

    public ErrorHandler(final HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(final HttpServerExchange ex) throws Exception {
        ex.addDefaultResponseListener(exchange -> {
            if (!exchange.isResponseChannelAvailable()) {
                return false;
            }
            final int code = exchange.getStatusCode();
            if (code == 401) {
                exchange.getResponseSender().send(error401Page());
                return true;
            } else if (code == 403) {
                exchange.getResponseSender().send(error403Page());
                return true;
            } else if (code == 500) {
                exchange.getResponseSender().send(error500Page());
                return true;
            }
            return false;
        });
        next.handleRequest(ex);
    }

    private static String error401Page() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>unauthorized</h1>");
        sb.append("<br />");
        sb.append("<a href='/'>Home</a>");
        return sb.toString();
    }

    private static String error403Page() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>forbidden</h1>");
        sb.append("<br />");
        sb.append("<a href='/'>Home</a>");
        return sb.toString();
    }

    private static String error500Page() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>internal error</h1>");
        sb.append("<br />");
        sb.append("<a href='/'>Home</a>");
        return sb.toString();
    }
}
