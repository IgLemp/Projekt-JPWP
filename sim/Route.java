package com.transport.sim;

import java.util.*;
import lombok.Getter;

public class Route {
    private List<String> path;
    @Getter private int length; // km

    public Route(List<String> path, int length) { this.path = path; this.length = length; }
    public List<String> getPath() { return path; }
}

