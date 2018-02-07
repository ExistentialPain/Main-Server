package com.SCI.net;

public final class HttpMethod {
    private HttpMethod() {

    }

    public static int fromString(String method) {
        switch (method.toLowerCase()) {
            case "get":
                return HttpMethod.GET;
            case "post":
                return HttpMethod.POST;
            case "put":
                return HttpMethod.PUT;
            case "delete":
                return HttpMethod.DELETE;
            default:
                return HttpMethod.UNKNOWN;
        }
    }

    public static final int UNKNOWN = 0;
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    public static final int DELETE = 4;
}
