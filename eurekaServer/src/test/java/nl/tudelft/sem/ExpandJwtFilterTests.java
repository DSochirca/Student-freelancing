package nl.tudelft.sem;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.netflix.discovery.converters.Auto;
import com.netflix.zuul.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpandJwtFilterTests {

    private final transient JwtConfig jwtConfig = Mockito.mock(JwtConfig.class);;

    private final transient ExpandJWTFilter expandJWTFilter = new ExpandJWTFilter(jwtConfig);

    private final transient RequestContext ctx = Mockito.mock(RequestContext.class);
    private final transient HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private final String jwtSecret = "test-jwt-secret";

    @BeforeEach
    void setUp() {
        Mockito.when(ctx.getRequest()).thenReturn(request);
        Mockito.when(jwtConfig.getJwtSecret()).thenReturn(jwtSecret);
        RequestContext.testSetCurrentContext(ctx);
    }

    @Test
    void testFilterTypeIsPre() {
        assertEquals(expandJWTFilter.filterType(), "pre");
    }

    @Test
    void testFilterOrder() {
        assertEquals(expandJWTFilter.filterOrder(), 999);
    }

    @Test
    void testShouldFilter(){
        assertTrue(expandJWTFilter.shouldFilter());
    }

    @Test
    void testRunNormalToken(){
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getJwtSecret());
        String token = JWT.create()
                .withClaim("userRole", "STUDENT")
                .withClaim("userName", "test1")
                .sign(algorithm);

        Mockito.when(request.getHeader("Authorization")).thenReturn(token);

        expandJWTFilter.run();

        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-role", "STUDENT");
        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-name", "test1");
    }

    @Test
    void testRunBearerToken(){
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getJwtSecret());
        String token = JWT.create()
                .withClaim("userRole", "STUDENT")
                .withClaim("userName", "test1")
                .sign(algorithm);

        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        expandJWTFilter.run();

        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-role", "STUDENT");
        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-name", "test1");
    }

    @Test
    void testRunInvalidJwtSecret(){
        Algorithm algorithm = Algorithm.HMAC256("different-jwt-secret");
        String token = JWT.create()
                .withClaim("userRole", "STUDENT")
                .withClaim("userName", "test1")
                .sign(algorithm);

        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer" + token);

        expandJWTFilter.run();

        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-role", null);
        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-name", null);
    }

    @Test
    void testRunEmptyHeader(){
        Mockito.when(request.getHeader("Authorization")).thenReturn("");

        expandJWTFilter.run();

        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-role", null);
        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-name", null);
    }

    @Test
    void testRunNoHeader(){
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        expandJWTFilter.run();

        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-role", null);
        Mockito.verify(ctx, Mockito.times(1)).addZuulRequestHeader("x-user-name", null);
    }
}
