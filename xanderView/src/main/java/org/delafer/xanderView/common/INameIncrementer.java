package org.delafer.xanderView.common;

import net.j7.commons.types.DoubleValue;

import java.util.LinkedList;
import java.util.List;

public interface INameIncrementer {
    static INameIncrementer instance(String name) {
        return new DefaultNameIncrementer(name);
    }

    INameIncrementer increment();

    String build();
}
