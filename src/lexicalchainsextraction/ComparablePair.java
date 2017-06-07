/**
 * Copyright 2017 George Giannakopoulos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
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

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }
    
    
    
}
