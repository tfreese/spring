package de.freese.spring.web.scope;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * This uses the FacesContext view map as a datastore for a scope in the Spring Framework More simply this is a view scope implementation that works in spring.
 *
 * @author Optimus Prime(From the primefaces team)(http://blog.primefaces.org/?p=702)
 */
public class ViewScope implements Scope
{
    /**
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String, org.springframework.beans.factory.ObjectFactory)
     */
    @Override
    public Object get(final String name, final ObjectFactory<?> objectFactory)
    {
        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        Object obj = null;

        if (viewMap.containsKey(name))
        {
            obj = viewMap.get(name);
        }
        else
        {
            obj = objectFactory.getObject();
            viewMap.put(name, obj);
        }

        return obj;
    }

    /**
     * @see org.springframework.beans.factory.config.Scope#getConversationId()
     */
    @Override
    public String getConversationId()
    {
        return null;
    }

    /**
     * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String, java.lang.Runnable)
     */
    @Override
    public void registerDestructionCallback(final String name, final Runnable callback)
    {
        // Not supported
    }

    /**
     * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
     */
    @Override
    public Object remove(final String name)
    {
        return FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove(name);
    }

    /**
     * @see org.springframework.beans.factory.config.Scope#resolveContextualObject(java.lang.String)
     */
    @Override
    public Object resolveContextualObject(final String key)
    {
        return null;
    }
}
