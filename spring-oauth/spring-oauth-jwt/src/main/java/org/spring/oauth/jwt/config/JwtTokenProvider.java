/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.spring.oauth.jwt.exception.MyJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Resource
    private PasswordEncoder passwordEncoder = null;

    /**
     *
     */
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey = null;

    // /**
    // *
    // */
    // private SecretKey secretKey2 = null;

    /**
     *
     */
    @Resource
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
     * @param password String
     * @return String
     */
    public String createToken(final String userName, final String password)
    {
        String token = createToken(userName, password, null);

        return token;
    }

    /**
     * @param userName String
     * @param password String
     * @param roles {@link Collection}
     * @return String
     */
    public String createToken(final String userName, final String password, final Collection<? extends GrantedAuthority> roles)
    {
        Claims claims = Jwts.claims().setSubject(userName);

        if ((password != null) && !password.isBlank())
        {
            claims.put("password", password);
        }

        if (roles != null)
        {
            // claims.put("roles", roles.stream().filter(Objects::nonNull).map(r -> new SimpleGrantedAuthority(r)).collect(Collectors.toList()));
            claims.put("roles", roles);
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.validityInMilliseconds);

        // @formatter:off
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer("tommy")
                .setIssuedAt(now)
                .setExpiration(validity)
                //.compressWith(CompressionCodecs.GZIP)
                .signWith(SignatureAlgorithm.HS512, this.secretKey)
                .compact();
        // @formatter:on

        token = encodeToken(token);

        return token;
    }

    /**
     * @param token String
     * @return String
     */
    protected String decodeToken(final String token)
    {
        // byte[] bytes = Encryptors.stronger("gehaim", "abcd").decrypt(token.getBytes());
        // String t = new String(Base64.getDecoder().decode(bytes));
        //
        // String t = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        String t = token;

        return t;
    }

    /**
     * @param token String
     * @return String
     */
    protected String encodeToken(final String token)
    {
        // byte[] bytes = Encryptors.stronger("gehaim", "abcd").encrypt(token.getBytes());
        // String t = Base64.getEncoder().encodeToString(bytes);
        //
        // String t = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        String t = token;

        return t;
    }

    /**
     * @param token String
     * @return {@link Claims}
     */
    private Claims getAllClaimsFromToken(final String token)
    {
        String t = decodeToken(token);

        return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(t).getBody();
    }

    /**
     * @param token String
     * @return {@link Authentication}
     */
    public Authentication getAuthentication(final String token)
    {
        String userName = getUsername(token);
        String password = getPassword(token);

        UserDetails userDetails = this.userCache.getUserFromCache(userName);

        if (userDetails == null)
        {
            userDetails = this.userDetailsService.loadUserByUsername(userName);

            this.userCache.putUserInCache(userDetails);
        }

        if (!this.passwordEncoder.matches(password, userDetails.getPassword()))
        {
            throw new BadCredentialsException("Authentication failed: password does not match stored value");
        }

        // int start = userDetails.getPassword().indexOf("}");
        // String password = userDetails.getPassword().substring(start + 1);

        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(userName, password, userDetails.getAuthorities());
        // Authentication authenticationToken = new PreAuthenticatedAuthenticationToken(userName, "", userDetails.getAuthorities());

        // if (authenticationToken instanceof CredentialsContainer)
        // {
        // ((CredentialsContainer) authenticationToken).eraseCredentials();
        // }

        return authenticationToken;
    }

    /**
     * @param token String
     * @param claimsResolver {@link Function}
     * @return Object
     */
    public <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver)
    {
        final Claims claims = getAllClaimsFromToken(token);

        return claimsResolver.apply(claims);
    }

    /**
     * @param token String
     * @return {@link Date}
     */
    public Date getExpirationDate(final String token)
    {
        Date date = getClaimFromToken(token, Claims::getExpiration);

        return date;
    }

    /**
     * @param token String
     * @return {@link LocalDateTime}
     */
    public LocalDateTime getExpirationLocalDateTime(final String token)
    {
        Date date = getClaimFromToken(token, Claims::getExpiration);

        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        return localDateTime;
    }

    /**
     * @param token String
     * @return String
     */
    public String getPassword(final String token)
    {
        String password = getClaimFromToken(token, c -> (String) c.get("password"));

        return password;
    }

    /**
     * @param token String
     * @return {@link Set}
     */
    @SuppressWarnings("unchecked")
    public Set<? extends GrantedAuthority> getRoles(final String token)
    {
        Collection<? extends GrantedAuthority> col = getClaimFromToken(token, c -> (Collection<? extends GrantedAuthority>) c.get("roles"));

        Set<? extends GrantedAuthority> roles = new HashSet<>(col);

        return roles;
    }

    /**
     * @param token String
     * @return String
     */
    public String getUsername(final String token)
    {
        String userName = getClaimFromToken(token, Claims::getSubject);

        return userName;
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @PostConstruct
    protected void init() throws Exception
    {
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes());

        // byte[] salt = KeyGenerators.secureRandom(16).generateKey();
        //
        // PBEKeySpec keySpec = new PBEKeySpec(this.secretKey.toCharArray(), salt, 1024, 256);
        // SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        // this.secretKey2 = factory.generateSecret(keySpec);

        if (this.userCache == null)
        {
            this.userCache = new NullUserCache();
            // this.userCache = new SpringCacheBasedUserCache(new ConcurrentMapCache("userCache"));
        }
    }

    /**
     * @param token String
     * @return boolean
     */
    public boolean isTokenExpired(final String token)
    {
        final Date expiration = getExpirationDate(token);

        return expiration.before(new Date());
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
            String t = decodeToken(token);

            Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(t);

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
