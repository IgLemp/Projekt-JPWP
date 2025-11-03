package com.transport.sim;

import java.util.*;

public class Route {
    private List<String> path;
    private int length; // km

    public Route(List<String> path, int length) { this.path = path; this.length = length; }
    public List<String> getPath() { return path; }
    public int getLength() { return length; }
}

