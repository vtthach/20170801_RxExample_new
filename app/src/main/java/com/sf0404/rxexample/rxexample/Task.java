package com.sf0404.rxexample.rxexample;

public interface Task<T> {
        T run() throws InterruptedException;
}