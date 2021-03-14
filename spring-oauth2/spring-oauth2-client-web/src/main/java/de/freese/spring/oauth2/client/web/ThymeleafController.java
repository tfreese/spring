/**
 * Created: 31.10.2019
 */
package de.freese.spring.oauth2.client.web;

import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Thomas Freese
 */
@Controller
public class ThymeleafController
{
    /**
     * @param model {@link Model}
     * @param principal {@link Principal}
     * @return String
     */
    @GetMapping("/")
    public String index(final Model model, final Principal principal)
    {
        return "index";
    }

    /**
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @throws ServletException Falls was schief geht.
     * @return String
     */
    @PostMapping("logout")
    public String logout(final HttpServletRequest request, final HttpServletResponse response) throws ServletException
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null)
        {
            authentication.setAuthenticated(false);

            new SecurityContextLogoutHandler().logout(request, response, authentication);

            SecurityContextHolder.clearContext();
            request.logout();
            request.getSession().invalidate();
        }

        return "redirect:/";
    }

    /**
     * @param model {@link Model}
     * @param principal {@link Principal}
     * @return String
     */
    @GetMapping("securedPage")
    public String securedPage(final Model model, final Principal principal)
    {
        return "securedPage";
    }
}
