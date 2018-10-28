/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.config;

import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.spring.oauth.jwt.exception.MyJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Thomas Freese
 */
@Component
public class JwtTokenProvider
{
    /**
     *
     */
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey = null;

    /**
     *
     */
    private UserCache userCache = null;

    /**
     *
     */
    @Resource
    private UserDetailsService userDetailsService = null;

    /**
     * Default: 1 Stunde
     */
    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3600000;

    /**
     * Erstellt ein neues {@link JwtTokenProvider} Object.
     */
    public JwtTokenProvider()
    {
        super();
    }

    /**
     * @param userName String
     * @return String
     */
    public String createToken(final String userName)
    {
        String token = createToken(userName, null);

        return token;
    }

    /**
     * @param userName String
     * @param roles {@link Collection}
     * @return String
     */
    public String createToken(final String userName, final Collection<? extends GrantedAuthority> roles)
    {
        Claims claims = Jwts.claims().setSubject(userName);

        if (roles != null)
        {
            // claims.put("auth", roles.stream().filter(Objects::nonNull).map(r -> new SimpleGrantedAuthority(r)).collect(Collectors.toList()));
            claims.put("auth", roles);
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.validityInMilliseconds);

        // @formatter:off
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, this.secretKey)
                .compact();
        // @formatter:on

        return token;
    }

    /**
     * @param token String
     * @return {@link Authentication}
     */
    public Authentication getAuthentication(final String token)
    {
        String userName = getUsername(token);

        UserDetails userDetails = this.userCache.getUserFromCache(userName);

        if (userDetails == null)
        {
            userDetails = this.userDetailsService.loadUserByUsername(userName);

            this.userCache.putUserInCache(userDetails);
        }

        int start = userDetails.getPassword().indexOf("}");
        String password = userDetails.getPassword().substring(start + 1);

        // Authentication authenticationToken = new UsernamePasswordAuthenticationToken(userName, password, userDetails.getAuthorities());
        Authentication authenticationToken = new PreAuthenticatedAuthenticationToken(userName, "", userDetails.getAuthorities());

        // if (authenticationToken instanceof CredentialsContainer)
        // {
        // ((CredentialsContainer) authenticationToken).eraseCredentials();
        // }

        return authenticationToken;
    }

    /**
     * @param token String
     * @return String
     */
    public String getUsername(final String token)
    {
        String userName = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody().getSubject();

        return userName;
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @PostConstruct
    protected void init() throws Exception
    {
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes());
        this.userCache = new SpringCacheBasedUserCache(new ConcurrentMapCache("userCache"));
    }

    /**
     * @param req {@link HttpServletRequest}
     * @return String
     */
    public String resolveToken(final HttpServletRequest req)
    {
        String bearerToken = req.getHeader("Authorization");

        if ((bearerToken != null) && bearerToken.startsWith("Bearer "))
        {
            return bearerToken.substring(7, bearerToken.length());
        }

        return null;
    }

    /**
     * @param token String
     * @return boolean
     */
    public boolean validateToken(final String token)
    {
        try
        {
            Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token);

            return true;
        }
        catch (JwtException | IllegalArgumentException ex)
        {
            throw new MyJwtException("Expired or invalid JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // catch(RuntimeException rex)
        // {
        // throw rex;
        // }
        // catch(Exception ex)
        // {
        // throw new RuntimeException(ex);
        // }
    }
}
