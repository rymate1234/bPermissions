package de.bananaco.bpermissions.api;

/**
 * Interface to register a listener on a World so that plugins can respond to calculable changes
 */
public interface CalculableChangeListener {
    /**
     * Method called whenever a calculable inside a world changes
     *
     * @param change The change made to the calculable
     */
    void onChange(CalculableChange change);
}
