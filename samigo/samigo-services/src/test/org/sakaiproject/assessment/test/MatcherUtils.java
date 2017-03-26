/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assessment.test;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.ArrayList;
import java.util.List;

/**
 * Most entity classes do not have meaningful {@code equals()} nor {@code hashCode()} implementations, so we need
 * these head-scratchers for assertions on entity collection contents.
 *
 * <p>Lifted nearly verbaitim from <a href="http://stackoverflow.com/questions/27292452/hamcrest-matcher-to-compare-two-collections-of-different-types-by-items-using-a">here</a></p>
 */
public class MatcherUtils {

    public interface MatcherFunction<T, R> {
        Matcher<R> apply(T t);
    }

    public static <T, R> Matcher<Iterable<? extends R>> containsUsingCustomMatcher(List<T> items, MatcherFunction<T, R> matcherFunction) {
        return Matchers.contains(createMatchers(items, matcherFunction));
    }

    private static <R, T> List<Matcher<? super R>> createMatchers(List<T> items, MatcherFunction<T, R> matcherFunction) {
        List<Matcher<? super R>> matchers = new ArrayList<>();
        for (T item : items) {
            matchers.add(matcherFunction.apply(item));
        }
        return matchers;
    }
}
