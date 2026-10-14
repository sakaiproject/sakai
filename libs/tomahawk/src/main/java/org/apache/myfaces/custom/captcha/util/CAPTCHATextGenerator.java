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

import java.util.Random;

/**
 * This class is responsible for generating the 
 * CAPTCHA text.
 * 
 * @since 1.1.7
 */
public class CAPTCHATextGenerator
{

    /* CAPTCHA possible characters */
    private final static char[] CAPTCHA_POSSIBLE_CHARS = new char[] { 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    /**
     * generateRandomText() generates the CAPTCHA text
     * @return the random generated text. 
     */
    public static String generateRandomText()
    {

        int totalPossibleCharacters = CAPTCHA_POSSIBLE_CHARS.length - 1;
        String captchaText = "";
        Random random = new Random();
        int captchaTextLength = 5;
        int randomNumber = random.nextInt(10);

        // Determine the CAPTCHA Length whether it is five or six.
        if (randomNumber >= 5)
        {
            captchaTextLength = 6;
        }

        // Generate the random String.
        for (int i = 0; i < captchaTextLength; ++i)
        {
            captchaText += CAPTCHA_POSSIBLE_CHARS[random
                    .nextInt(totalPossibleCharacters) + 1];
        }

        return captchaText;
    }

}
