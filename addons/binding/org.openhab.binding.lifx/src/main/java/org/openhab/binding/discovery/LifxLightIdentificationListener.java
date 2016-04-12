package org.openhab.binding.discovery;

public interface LifxLightIdentificationListener {

    void lightIdentified(LifxDiscoveryIdentity ident);

    void lightIdFailed(LifxDiscoveryIdentity ident);

}
