/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.indexing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mbouadjenek
 */
public class EntityMention implements Serializable{

    public Map<String, Set<String>> title_map = new HashMap<>();
    public Map<String, Set<String>> abstract_map = new HashMap<>();
    public Map<String, Set<String>> body_map = new HashMap<>();

    public void insert(String k, String v, String field) {
        if (field.toLowerCase().equals("title")) {
            if (title_map.containsKey(k)) {
                Set<String> s = title_map.get(k);
                s.add(v);
                title_map.put(k, s);
            } else {
                Set<String> s = new HashSet<>();
                s.add(v);
                title_map.put(k, s);
            }
        } else if (field.toLowerCase().equals("abstract")) {
            if (abstract_map.containsKey(k)) {
                Set<String> s = abstract_map.get(k);
                s.add(v);
                abstract_map.put(k, s);
            } else {
                Set<String> s = new HashSet<>();
                s.add(v);
                abstract_map.put(k, s);
            }
        }
        if (field.toLowerCase().equals("body")) {
            if (body_map.containsKey(k)) {
                Set<String> s = body_map.get(k);
                s.add(v);
                body_map.put(k, s);
            } else {
                Set<String> s = new HashSet<>();
                s.add(v);
                body_map.put(k, s);
            }
        }
    }
}
