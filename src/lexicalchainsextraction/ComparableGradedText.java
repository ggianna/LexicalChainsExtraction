/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexicalchainsextraction;

import gr.demokritos.iit.jinsect.structs.Pair;
import java.util.List;

/**
 *
 * @author ggianna
 */
public class ComparableGradedText extends Pair<Double, List<String>> {

    public ComparableGradedText(Double oFirst, List<String> oSecond) {
        super(oFirst, oSecond);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComparableGradedText) {
            ComparableGradedText cgtO = (ComparableGradedText)obj;
            return (getFirst().equals(cgtO.getFirst()) && 
                    (getSecond().equals(cgtO.getSecond())));
        }
        return false;
    }

    @Override
    public String toString() {
        return getFirst().toString() + ":" + getSecond().toString();
    }
    
    
    
}
