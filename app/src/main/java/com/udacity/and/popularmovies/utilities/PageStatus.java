package com.udacity.and.popularmovies.utilities;

/**
 * Cache for page status to keep track of page scrolling
 */
public class PageStatus {
    private int mUpperSide;
    private int mLowerSide;

    public PageStatus() {
        mUpperSide = 1;
        mLowerSide = 2;
    }

    public int getUpperSide() {
        return mUpperSide;
    }

    public void setUpperSide(int page) {
        mUpperSide = page;
    }

    public int getLowerSide() {
        return mLowerSide;
    }

    public void setLowerSide(int page) {
        mLowerSide = page;
    }

    /**
     * Increments page status
     */
    public void increment() {
        mUpperSide++;
        mLowerSide++;
    }

    /**
     * Decrements page status
     */
    public void decrement() {
        mUpperSide--;
        mLowerSide--;
    }
}
