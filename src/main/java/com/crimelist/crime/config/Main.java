package com.crimelist.crime.config;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        LinkedList ll = new LinkedList();
        ll.add("1");
        ll.add("2");
        System.out.println(ll.peek());
        System.out.println(ll.poll());
//        System.out.println(ll.pop());
        System.out.println(ll.pollLast());
    }

}
