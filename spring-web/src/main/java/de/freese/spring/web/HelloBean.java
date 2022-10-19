package de.freese.spring.web;

import javax.faces.bean.ManagedBean;
import javax.faces.view.ViewScoped;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@ManagedBean
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
