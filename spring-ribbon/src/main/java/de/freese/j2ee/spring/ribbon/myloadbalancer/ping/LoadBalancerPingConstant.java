// Created: 22.03.2018
package de.freese.j2ee.spring.ribbon.myloadbalancer.ping;

/**
 * Diese Implementierung liefert immer den gesetzten Wert.<br>
 * Default: true
 *
 * @author Thomas Freese
 */
public class LoadBalancerPingConstant implements LoadBalancerPing
{
    /**
     *
     */
    private boolean constant = true;

    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancerPingConstant}.
     */
    public LoadBalancerPingConstant()
    {
        super();
    }

    /**
     * @see de.freese.j2ee.spring.ribbon.myloadbalancer.ping.LoadBalancerPing#isAlive(java.lang.String)
     */
    @Override
    public boolean isAlive(final String server)
    {
        return this.constant;
    }

    /**
     * @return boolean
     */
    public boolean isConstant()
    {
        return this.constant;
    }

    /**
     * @param constant boolean
     */
    public void setConstant(final boolean constant)
    {
        this.constant = constant;
    }
}
