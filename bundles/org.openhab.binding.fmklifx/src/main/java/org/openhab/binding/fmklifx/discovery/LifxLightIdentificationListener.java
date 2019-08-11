package org.openhab.binding.fmklifx.discovery;

public interface LifxLightIdentificationListener {

    void lightIdentified(LifxDeviceAnalyzer ident);

    void lightIdFailed(LifxDeviceAnalyzer ident);

}
