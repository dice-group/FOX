package org.aksw.fox.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Converts Collections and Arrays
 * 
 * @author rspeck
 *
 */
public class Converter {

    /**
     * Example code: <code>
        List<String> stringList = Arrays.asList("1","2","3"); 
        List<Integer> integerList = convertList(stringList, s -> Integer.parseInt(s));
      </code>
     * 
     * @param from
     * @param func
     * @return
     */
    public static <T, U> List<U> convertList(List<T> from, Function<T, U> func) {
        return from.stream().map(func).collect(Collectors.toList());
    }

    /**
     * example code:<code>
        String[] stringArr = {"1","2","3"}; 
        Double[] doubleArr = convertArray(stringArr, Double::parseDouble, Double[]::new);
     </code>
     * 
     * @param from
     * @param func
     * @param generator
     * @return
     */
    public static <T, U> U[] convertArray(T[] from, Function<T, U> func, IntFunction<U[]> generator) {
        return Arrays.stream(from).map(func).toArray(generator);
    }
}
