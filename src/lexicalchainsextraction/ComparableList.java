/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexicalchainsextraction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ggianna
 */
class ComparableList<T extends String> extends ArrayList<String> implements Comparable<List<T>> {

    @Override
    public int compareTo(List<T> o) {
        return this.toString().compareTo(o.toString());
    }    
}
