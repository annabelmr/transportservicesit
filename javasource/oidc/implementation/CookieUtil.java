package oidc.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.core.Core;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static final String NONCE_COOKIE_PATH = "/";
    public static final String NONCE_COOKIE_DOMAIN = "";
    public static final String COOKIE_HOST_PREFIX = "__Host-";
    public static final String NONCE_COOKIE_NAME = "OIDCSSONONCE";
    public static final String SET_COOKIE = "Set-Cookie";

    public static String createCookie(IMxRuntimeRequest request, String name, String value, String path, String domain, CookieHeaderNames.SameSite sameSite, int maxAge) throws URISyntaxException {
        var isHttps = isHttps(request);
        var secureCookieName = isHttps ? COOKIE_HOST_PREFIX + name : name;

        DefaultCookie nettyCookie = new DefaultCookie(secureCookieName, value);
        nettyCookie.setHttpOnly(true);
        nettyCookie.setSecure(isHttps);
        nettyCookie.setPath(path);
        nettyCookie.setSameSite(sameSite);
        nettyCookie.setDomain(domain);
        nettyCookie.setMaxAge(maxAge);

        // Use ServerCookieEncoder to format the cookie
        return ServerCookieEncoder.STRICT.encode(nettyCookie);
    }

    public static String getCookie(IMxRuntimeRequest request, String cookieName) throws URISyntaxException {
        HttpServletRequest servletRequest = request.getHttpServletRequest();
        var isHttps = isHttps(request);
        var secureCookieName = isHttps ? COOKIE_HOST_PREFIX + cookieName : cookieName;
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies == null)
            return null;

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(secureCookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    public static boolean isHttps(IMxRuntimeRequest request) throws URISyntaxException {
        var hasProtoHttps = hasHeaderValue(request, "X-Forwarded-Proto", "https");
        var hasSchemeHttps = hasHeaderValue(request, "X-Forwarded-Scheme", "https");
        return isSecure(Core.getConfiguration().getApplicationRootUrl()) || hasProtoHttps || hasSchemeHttps;
    }

    private static boolean hasHeaderValue(IMxRuntimeRequest request, String headerName, String value) {
        return value.equalsIgnoreCase(request.getHeader(headerName));
    }

    private static boolean isSecure(String rootUrl) throws URISyntaxException {
        URI uri = new URI(rootUrl);
        return uri.getScheme().equals("https");
    }

}
