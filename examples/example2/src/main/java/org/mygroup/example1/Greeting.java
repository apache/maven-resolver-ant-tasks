package org.mygroup.example1;

import org.apache.commons.lang3.ArrayUtils;

public class Greeting {

  public String greet(String[] words) {
    return ArrayUtils.toString(words) + "!";
  }

  public static void main(String[] args) {
    Greeting greeting = new Greeting();
    String[] words = {"Hello", "World"};
    System.out.println(greeting.greet(words));
  }
}
