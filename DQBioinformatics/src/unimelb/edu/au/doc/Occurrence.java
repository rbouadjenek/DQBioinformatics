/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

/**
 *
 * @author mbouadjenek
 */
public class Occurrence implements Comparable<Occurrence> {

    String id;
    int char_start, char_end;

    public Occurrence(String id, int char_start, int char_end) {
        this.id = id;
        this.char_start = char_start;
        this.char_end = char_end;
    }

    public int getChar_start() {
        return char_start;
    }

    public int getChar_end() {
        return char_end;
    }

    public String getId() {
        return id;
    }
    
    

    @Override
    public int compareTo(Occurrence o) {
        return Integer.compare(char_start, o.getChar_start());
    }

}
