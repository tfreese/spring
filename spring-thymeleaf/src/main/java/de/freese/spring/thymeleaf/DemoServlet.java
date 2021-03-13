/**
 * 05.11.2013
 */
package de.freese.spring.thymeleaf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Thomas Freese
 */
@WebServlet(description = "DemoServlet", name = "demoServlet", urlPatterns = "/demo-servlet", loadOnStartup = 1)
public class DemoServlet extends HttpServlet
{
    /**
     *
     */
    private static final long serialVersionUID = 891637777095320320L;

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");

        try (PrintWriter writer = resp.getWriter())
        {
            writer.append("<b>").append(new Date().toString()).append("</b>");
        }
    }
}
