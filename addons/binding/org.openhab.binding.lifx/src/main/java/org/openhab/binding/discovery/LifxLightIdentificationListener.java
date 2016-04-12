package org.openhab.binding.discovery;

public interface LifxLightIdentificationListener {

    void lightIdentified(LifxDeviceAnalyzer ident);

    void lightIdFailed(LifxDeviceAnalyzer ident);

}
