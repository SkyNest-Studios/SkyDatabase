package dev.skynest.xyz.interfaces;

import lombok.Getter;

@Getter
public abstract class IData {

    private final String name;

    public IData(String name) {
        this.name = name;
    }

    // create
    protected abstract void save();

}
