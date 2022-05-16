package org.jlab.rfd.presentation.util;

/**
 *
 * @author ryans
 */
public class Paginator {
    private long offset;
    private long maxPerPage;
    private long totalRecords;
    
    public Paginator(long totalRecords, long offset, long maxPerPage) {
        this.totalRecords = totalRecords;
        this.offset = offset;
        this.maxPerPage = maxPerPage;
    }
    
    public long getTotalRecords() {
        return totalRecords;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public long getMaxPerPage() {
        return maxPerPage;
    }
    
    public long getStartNumber() {
        long startNumber = offset + 1;
        
        if(startNumber > totalRecords) {
            startNumber = totalRecords;
        }
        
        return startNumber;
    }
    
    public long getEndNumber() {
        long endNumber = offset + maxPerPage;
        
        if(endNumber > totalRecords) {
            endNumber = totalRecords;
        }
        
        return endNumber;
    }
    
    public boolean isPrevious() {
        boolean previous = false;
        
        if(offset > 0) {
            previous = true;
        }
        
        return previous;
    }
    
    public boolean isNext() {
        boolean next = false;
        
        if(totalRecords > offset + maxPerPage) {
            next = true;
        }    
        
        return next;
    }
    
    public long getPreviousOffset() {
        long previousOffset = offset - maxPerPage;
        
        if(previousOffset < 0) {
            previousOffset = 0;
        }
        
        return previousOffset;
    }
    
    public long getNextOffset() {        
        long nextOffset = offset + maxPerPage;
        
        if(nextOffset > (totalRecords - 1)) {
            nextOffset = totalRecords - 1;
        }        
        
        return nextOffset;
    }
}
