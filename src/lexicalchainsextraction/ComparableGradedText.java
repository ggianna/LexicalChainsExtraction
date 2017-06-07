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
