/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.client.ui;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Thomas Freese
 */
@Controller
// @ThymeleafController
public class WebController
{
    /**
     * Erstellt ein neues {@link WebController} Object.
     */
    public WebController()
    {
        super();
    }

    /**
     * @param model {@link Model}
     * @param principal {@link Principal}
     * @return String
     */
    @RequestMapping("/")
    public String index(final Model model, final Principal principal)
    {
        return "index";
    }

    /**
     * @param model {@link Model}
     * @param principal {@link Principal}
     * @return String
     */
    @RequestMapping("/securedPage")
    public String securedPage(final Model model, final Principal principal)
    {
        return "securedPage";
    }
}
