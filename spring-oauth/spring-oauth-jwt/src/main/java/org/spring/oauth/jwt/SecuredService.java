/**
 * Created: 25.09.2018
 */

package org.spring.oauth.jwt;

import java.security.Principal;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("rest")
@Api(tags = "demoService")
public class SecuredService
{
    /**
     *
     */
    private String message = "Hello World";

    /**
     * Erstellt ein neues {@link SecuredService} Object.
     */
    public SecuredService()
    {
        super();
    }

    /**
     * @return String
     */
    @GetMapping("message")
    @PreAuthorize("#oauth2.hasScope('read')")
    @Secured("ROLE_USER")
    @ApiOperation(value = "Returns the message.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
    })
    public String getMessage()
    {
        return this.message;
    }

    /**
     * @param message String
     */
    @PostMapping("message/{message}")
    @PreAuthorize("#oauth2.hasScope('write')")
    @Secured("ROLE_ADMIN")
    @ApiOperation(value = "Set the message.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
    })
    public void setMessage(@PathVariable("message") final String message)
    {
        this.message = message;
    }

    /**
     * @param principal {@link Principal}
     * @return {@link Principal}
     */
    @GetMapping("me")
    @PreAuthorize("#oauth2.hasScope('read')")
    @Secured("ROLE_USER")
    @ApiOperation(value = "Returns current Principal.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
    })
    public Principal user(final Principal principal)
    {
        return principal;
    }
}
