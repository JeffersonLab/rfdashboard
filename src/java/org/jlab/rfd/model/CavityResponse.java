
package org.jlab.rfd.model;

/**
 * This class represents a CavityDataPoint object 
 * , and other objects which may have a more granular time resolution and are not included in the
 * CavityDataPoint in memory cache.
 * 
 * Note: This class is needed since CavityDataPoint objects are restricted to timestamps with day resolution (no HH:MM:SS)
 * portion  due to cache restrictions), and some other elements like Comments need a more precise time resolution and don't
 * work well with the current caching scheme.
 * @author adamc
 */
public class CavityResponse extends CavityDataPoint {
    private final Comment comment;
    
    public CavityResponse(CavityDataPoint cdp, Comment comment) {
        super(cdp);
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }
}
