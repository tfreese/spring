/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.controller;

import java.security.Principal;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.spring.oauth.jwt.model.MutableUser;
import org.spring.oauth.jwt.service.UserService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("/users")
@Api(tags = "users")
public class UserController
{
    /**
     *
     */
    @Resource
    private UserService userService = null;

    /**
     * @param userName String
     * @return String
     */
    @DeleteMapping(value = "/{userName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes specific user by username.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")
    })
    public String delete(@ApiParam("userName") @PathVariable final String userName)
    {
        this.userService.delete(userName);

        return userName;
    }

    /**
     * @param userName String
     * @param password String
     * @return String
     */
    @PostMapping("/login")
    @ApiOperation(value = "Authenticates user and returns its JWT token.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 400, message = "Something went wrong"),//
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })

    public String login(@ApiParam("userName") @RequestParam final String userName, @ApiParam("password") @RequestParam final String password)
    {
        return this.userService.signin(userName, password);
    }

    /**
     * @param userDetails {@link UserDetails}
     * @return String
     */
    @PostMapping("/register")
    @ApiOperation(value = "Creates user and returns its JWT token.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Username is already in use"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")
    })
    public String register(@ApiParam("Signup User") @RequestBody final UserDetails userDetails)
    {
        return this.userService.signup(userDetails);
    }

    /**
     * @param userName String
     * @return {@link UserDetails}
     */
    @GetMapping(value = "/{userName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Returns specific user by username.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")
    })
    public UserDetails search(@ApiParam("userName") @PathVariable final String userName)
    {
        UserDetails userDetails = this.userService.search(userName);

        return userDetails;
    }

    /**
     * @param req {@link HttpServletRequest}
     * @param principal {@link Principal}
     * @return {@link MutableUser}
     */
    @GetMapping(value = "/me")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @Secured("ROLE_USER")
    @ApiOperation(value = "Returns current user's data.")
    @ApiResponses(value =
    {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")
    })
    // public MutableUser whoami(final HttpServletRequest req)
    // {
    // UserDetails userDetails = this.userService.whoami(req);
    //
    // return userDetails;
    // }
    public Principal whoami(final Principal principal)
    {
        return principal;
    }
}
