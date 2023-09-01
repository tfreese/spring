// Created: 05.11.2013
package de.freese.spring.thymeleaf;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.time.LocalDateTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Thomas Freese
 */
@WebServlet(description = "DemoServlet", name = "demoServlet", urlPatterns = "/demo-servlet", loadOnStartup = 1)
public class DemoServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 891637777095320320L;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        try (PrintWriter writer = resp.getWriter()) {
            writer.append("<b>").append(LocalDateTime.now().toString()).append("</b>");
        }
    }
}
