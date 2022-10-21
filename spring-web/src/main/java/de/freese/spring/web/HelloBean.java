package de.freese.spring.web;

import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * @author Thomas Freese
 */
//@Component
@Named
@ViewScoped
public class HelloBean
{
    @Resource
    private DataService dataService;

    public String getDate()
    {
        return dataService.getDate();
    }
}
