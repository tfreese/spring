/**
 * Created: 28.10.2018
 */
package de.freese.spring.jwt.token;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
    private PasswordEncoder passwordEncoder;

    /**
     *
     */
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    // /**
    // *
    // */
    // private SecretKey secretKey2;

    /**
     * Default: 1 Stunde
     */
    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3_600_000;

    /**
     * @param username String
     * @return String
     */
    public String createToken(final String username)
    {
        String token = createToken(username, null);

        return token;
    }

    /**
     * @param username String
     * @param password String
     * @return String
     */
    public String createToken(final String username, final String password)
    {
        String token = createToken(username, password, null);

        return token;
    }

    /**
     * @param username String
     * @param password String
     * @param roles {@link Collection}
     * @return String
     */
    public String createToken(final String username, final String password, final Collection<? extends GrantedAuthority> roles)
    {
        Claims claims = Jwts.claims().setSubject(username);

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
//                .compressWith(CompressionCodecs.DEFLATE)
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
     * @param claims {@link Jws}
     * @return {@link Date}
     */
    public Date getExpirationDate(final Jws<Claims> claims)
    {
        Date date = claims.getBody().getExpiration();

        return date;
    }

    /**
     * @param claims {@link Jws}
     * @return String
     */
    public String getPassword(final Jws<Claims> claims)
    {
        String password = (String) claims.getBody().get("password");

        return password;
    }

    /**
     * @param claims {@link Jws}
     * @return {@link Set}
     */
    @SuppressWarnings("unchecked")
    public Set<? extends GrantedAuthority> getRoles(final Jws<Claims> claims)
    {
        Collection<? extends GrantedAuthority> col = (Collection<? extends GrantedAuthority>) claims.getBody().get("roles");

        Set<? extends GrantedAuthority> roles = new HashSet<>(col);

        return roles;
    }

    /**
     * @param claims {@link Jws}
     * @return String
     */
    public String getUsername(final Jws<Claims> claims)
    {
        String username = claims.getBody().getSubject();

        return username;
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @PostConstruct
    protected void init() throws Exception
    {
        this.secretKey = Base64.getEncoder().encodeToString(this.secretKey.getBytes(StandardCharsets.UTF_8));

        // byte[] salt = KeyGenerators.secureRandom(16).generateKey();
        //
        // PBEKeySpec keySpec = new PBEKeySpec(this.secretKey.toCharArray(), salt, 1024, 256);
        // SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        // this.secretKey2 = factory.generateSecret(keySpec);
    }

    /**
     * @param claims {@link Jws}
     * @return boolean
     */
    public boolean isTokenExpired(final Jws<Claims> claims)
    {
        final Date expiration = getExpirationDate(claims);

        return expiration.before(new Date());
    }

    /**
     * @param token String
     * @return boolean
     */
    public Jws<Claims> parseToken(final String token)
    {
        String t = decodeToken(token);

        Jws<Claims> jws = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(t);

        return jws;
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
}
