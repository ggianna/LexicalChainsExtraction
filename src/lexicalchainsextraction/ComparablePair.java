/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexicalchainsextraction;

import gr.demokritos.iit.jinsect.structs.Pair;

/**
 * A pair that allows comparison based on the string representation of a pair.
 * @author ggianna
 */
public class ComparablePair<A, B> extends Pair<A, B> implements Comparable<Pair<A, B>> {

    public ComparablePair(A oFirst, B oSecond) {
        super(oFirst, oSecond);
    }

    @Override
    public int compareTo(Pair<A, B> o) {
        return toString().compareTo(o.toString());
    }
    
    
}
