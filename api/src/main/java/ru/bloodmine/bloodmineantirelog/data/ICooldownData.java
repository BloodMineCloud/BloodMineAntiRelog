package ru.bloodmine.bloodmineantirelog.data;

public interface ICooldownData {
    java.time.LocalTime getTime();

    String getItem();

    void setTime(java.time.LocalTime time);
}
