package org.openhab.binding.discovery;

public interface LifxLightIdentificationListener {

    void lightIdentified(LifxDeviceIdentifier ident);

    void lightIdFailed(LifxDeviceIdentifier ident);

}
