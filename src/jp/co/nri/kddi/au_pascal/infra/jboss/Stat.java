/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.co.nri.kddi.au_pascal.infra.jboss;

/**
 *
 * @author k-ota
 */
public class Stat {
    private String DSName;
    private int ActiveCount;
    private int AvailableCount;
    private int AverageBlockingTime;
    private int AverageCreationTime;
    private int CreatedCount;
    private int DestroyedCount;
    private int InUseCount;
    private int MaxCreationTime;
    private int MaxUsedCount;
    private int MaxWaitCount;
    private int MaxWaitTime;
    private int TimedOut;
    private int TotalBlockingTime;
    private int TotalCreationTime;
    
    Stat() {
        DSName = null;
        ActiveCount = -1;
        AvailableCount = -1;
        AverageBlockingTime = -1;
        AverageCreationTime = -1;
        CreatedCount = -1;
        DestroyedCount = -1;
        InUseCount = -1;
        MaxCreationTime = -1;
        MaxUsedCount = -1;
        MaxWaitTime = -1;
        TimedOut = -1;
        TotalBlockingTime = -1;
        TotalCreationTime = -1;
    }
    
    /*
    getter/setter section
    */

    /**
     * @return the DSName
     */
    public String getDSName() {
        return DSName;
    }

    /**
     * @param DSName the DSName to set
     */
    public void setDSName(String DSName) {
        this.DSName = DSName;
    }

    /**
     * @return the ActiveCount
     */
    public int getActiveCount() {
        return ActiveCount;
    }

    /**
     * @param ActiveCount the ActiveCount to set
     */
    public void setActiveCount(int ActiveCount) {
        this.ActiveCount = ActiveCount;
    }

    /**
     * @return the AvailableCount
     */
    public int getAvailableCount() {
        return AvailableCount;
    }

    /**
     * @param AvailableCount the AvailableCount to set
     */
    public void setAvailableCount(int AvailableCount) {
        this.AvailableCount = AvailableCount;
    }

    /**
     * @return the AverageBlockingTime
     */
    public int getAverageBlockingTime() {
        return AverageBlockingTime;
    }

    /**
     * @param AverageBlockingTime the AverageBlockingTime to set
     */
    public void setAverageBlockingTime(int AverageBlockingTime) {
        this.AverageBlockingTime = AverageBlockingTime;
    }

    /**
     * @return the AverageCreationTime
     */
    public int getAverageCreationTime() {
        return AverageCreationTime;
    }

    /**
     * @param AverageCreationTime the AverageCreationTime to set
     */
    public void setAverageCreationTime(int AverageCreationTime) {
        this.AverageCreationTime = AverageCreationTime;
    }

    /**
     * @return the CreatedCount
     */
    public int getCreatedCount() {
        return CreatedCount;
    }

    /**
     * @param CreatedCount the CreatedCount to set
     */
    public void setCreatedCount(int CreatedCount) {
        this.CreatedCount = CreatedCount;
    }

    /**
     * @return the DestroyedCount
     */
    public int getDestroyedCount() {
        return DestroyedCount;
    }

    /**
     * @param DestroyedCount the DestroyedCount to set
     */
    public void setDestroyedCount(int DestroyedCount) {
        this.DestroyedCount = DestroyedCount;
    }

    /**
     * @return the InUseCount
     */
    public int getInUseCount() {
        return InUseCount;
    }

    /**
     * @param InUseCount the InUseCount to set
     */
    public void setInUseCount(int InUseCount) {
        this.InUseCount = InUseCount;
    }

    /**
     * @return the MaxCreationTime
     */
    public int getMaxCreationTime() {
        return MaxCreationTime;
    }

    /**
     * @param MaxCreationTime the MaxCreationTime to set
     */
    public void setMaxCreationTime(int MaxCreationTime) {
        this.MaxCreationTime = MaxCreationTime;
    }

    /**
     * @return the MaxUsedCount
     */
    public int getMaxUsedCount() {
        return MaxUsedCount;
    }

    /**
     * @param MaxUsedCount the MaxUsedCount to set
     */
    public void setMaxUsedCount(int MaxUsedCount) {
        this.MaxUsedCount = MaxUsedCount;
    }

    /**
     * @return the MaxWaitCount
     */
    public int getMaxWaitCount() {
        return MaxWaitCount;
    }

    /**
     * @param MaxWaitCount the MaxWaitCount to set
     */
    public void setMaxWaitCount(int MaxWaitCount) {
        this.MaxWaitCount = MaxWaitCount;
    }

    /**
     * @return the MaxWaitTime
     */
    public int getMaxWaitTime() {
        return MaxWaitTime;
    }

    /**
     * @param MaxWaitTime the MaxWaitTime to set
     */
    public void setMaxWaitTime(int MaxWaitTime) {
        this.MaxWaitTime = MaxWaitTime;
    }

    /**
     * @return the TimedOut
     */
    public int getTimedOut() {
        return TimedOut;
    }

    /**
     * @param TimedOut the TimedOut to set
     */
    public void setTimedOut(int TimedOut) {
        this.TimedOut = TimedOut;
    }

    /**
     * @return the TotalBlockingTime
     */
    public int getTotalBlockingTime() {
        return TotalBlockingTime;
    }

    /**
     * @param TotalBlockingTime the TotalBlockingTime to set
     */
    public void setTotalBlockingTime(int TotalBlockingTime) {
        this.TotalBlockingTime = TotalBlockingTime;
    }

    /**
     * @return the TotalCreationTime
     */
    public int getTotalCreationTime() {
        return TotalCreationTime;
    }

    /**
     * @param TotalCreationTime the TotalCreationTime to set
     */
    public void setTotalCreationTime(int TotalCreationTime) {
        this.TotalCreationTime = TotalCreationTime;
    }
    
    
}
