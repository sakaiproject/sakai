/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.captcha.util;

import java.awt.Color;

/**
 * This class is responsible for generating CAPTCHA random color...
 * 
 * @since 1.1.7
 */
public class ColorGenerator
{

    private final static int COLOR_DEGREES = 255;
    private final static int COLOR_GENERATOR_DELTA = 127;

    /**
     * This method is used for generating a random color.
     * @param startFrom -> the color that we should be away from.
     * @return the new color.
     */
    public static Color generateRandomColor(Color startFrom)
    {

        /* if the startingFrom color is null, then generate a new random color. */
        if (startFrom == null)
        {
            return new Color((int) (Math.random() * COLOR_DEGREES), (int) (Math
                    .random() * COLOR_DEGREES),
                    (int) (Math.random() * COLOR_DEGREES));
        }

        /* try to avoid the startFrom color. */
        int startingRed = (startFrom.getRed() >= 128) ? 0 : 128;
        int startingGreen = (startFrom.getGreen() >= 128) ? 0 : 128;
        int startingBlue = (startFrom.getBlue() >= 128) ? 0 : 128;

        // generate the new random colors.  
        int newRandomRed = (int) (Math.random() * (startingRed + COLOR_GENERATOR_DELTA));
        int newRandomGreen = (int) (Math.random() * (startingGreen + COLOR_GENERATOR_DELTA));
        int newRandomBlue = (int) (Math.random() * (startingBlue + COLOR_GENERATOR_DELTA));

        /* 
         * If the newly generated color is less than the starting color 
         * then add the starting color to it. 
         */
        if (newRandomRed < startingRed)
        {
            newRandomRed += startingRed;
        }

        if (newRandomGreen < startingGreen)
        {
            newRandomGreen += startingGreen;
        }

        if (newRandomBlue < startingBlue)
        {
            newRandomBlue += startingBlue;
        }

        return new Color(newRandomRed, newRandomGreen, newRandomBlue);
    }
}
