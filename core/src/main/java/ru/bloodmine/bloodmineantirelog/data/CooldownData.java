package ru.bloodmine.bloodmineantirelog.data;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
public class CooldownData implements ICooldownData {
    @Setter
    private LocalTime time;
    private String item;

    public CooldownData(LocalTime time, String item) {
        this.time = time;
        this.item = item;
    }
}
