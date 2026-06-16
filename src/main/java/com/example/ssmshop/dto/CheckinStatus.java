package com.example.ssmshop.dto;

public class CheckinStatus {
    private int coins;
    private int streak;
    private boolean checkedToday;
    private int nextDay;
    private int nextReward;
    private int couponCount;

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public boolean isCheckedToday() {
        return checkedToday;
    }

    public void setCheckedToday(boolean checkedToday) {
        this.checkedToday = checkedToday;
    }

    public int getNextDay() {
        return nextDay;
    }

    public void setNextDay(int nextDay) {
        this.nextDay = nextDay;
    }

    public int getNextReward() {
        return nextReward;
    }

    public void setNextReward(int nextReward) {
        this.nextReward = nextReward;
    }

    public int getCouponCount() {
        return couponCount;
    }

    public void setCouponCount(int couponCount) {
        this.couponCount = couponCount;
    }
}
