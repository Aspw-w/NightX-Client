package net.aspw.client.protocol.api;

/**
 * This interface is used to access platform specific fields.
 */
public interface VFPlatform {

    /**
     * @return the native version of the platform
     */
    int getGameVersion();
}