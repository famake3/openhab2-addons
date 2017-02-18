package org.openhab.binding.lifx.discovery;

public interface LifxLightIdentificationListener {

    void lightIdentified(LifxDeviceAnalyzer ident);

    void lightIdFailed(LifxDeviceAnalyzer ident);

}
