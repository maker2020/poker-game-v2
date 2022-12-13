package com.samay.controller.test;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    private static Trie trie;

    static{
        trie=new Trie();
        trie.init(new String[] { "hello", "hell", "he", "hey", "hero", "help" });
    }
    
    @RequestMapping("/search/{keyword}")
    public List<String> search(@PathVariable("keyword") String keyword){
        return trie.associationWords(keyword);
    }


}

class Trie {

    private boolean isEnd;
    private Map<Character, Trie> next;

    public Trie() {
        next = new HashMap<>();
        isEnd = false;
    }

    public void init(String[] words) {
        for (String word : words) {
            insert(word);
        }
    }

    /**
     * 构造word的字符路径
     * 
     * @param word
     */
    public void insert(String word) {
        Trie node = this;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (node.next.get(c) == null) {
                node.next.put(c, new Trie());
            }
            node = node.next.get(c);
        }
        node.isEnd = true;
    }

    public boolean search(String word) {
        Trie node = searchPrefix(word);
        return node != null && node.isEnd;
    }

    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
    }

    public List<String> associationWords(String keyword) {
        Trie node = searchPrefix(keyword);
        if (node == null)
            return null;
        List<String> list = new LinkedList<>();
        iterate(list, keyword, node);
        return list;
    }

    private void iterate(List<String> list, String prefix, Trie node) {
        if(node.isEnd){
            list.add(prefix);
        }
        Map<Character,Trie> next=node.next;
        Iterator<Character> iterator = next.keySet().iterator();
        while (iterator.hasNext()) {
            Character c = iterator.next();
            iterate(list, prefix+c, next.get(c));
        }
    }

    private Trie searchPrefix(String prefix) {
        Trie node = this;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (node.next.get(c) == null) {
                return null;
            }
            node = node.next.get(c);
        }
        return node;
    }
}