package com.gymcrm.Loader;

public interface Loader {
    // the lower this number is the earliest the loader will run basically.
    int getOrder();
    void load();

}