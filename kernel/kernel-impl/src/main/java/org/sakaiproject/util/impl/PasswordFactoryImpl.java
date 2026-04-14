/**
 * Copyright (c) 2003-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.util.impl;


import org.passay.data.CharacterData;
import org.passay.data.EnglishCharacterData;
import org.passay.generate.PasswordGenerator;
import org.passay.rule.CharacterCharacteristicsRule;
import org.passay.rule.CharacterRule;
import org.sakaiproject.util.api.PasswordFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordFactoryImpl implements PasswordFactory {

  @Override
  public String generatePassword() {
    
    CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
    CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars, 2);
     
    CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
    CharacterRule upperCaseRule = new CharacterRule(upperCaseChars, 2);
     
    CharacterData digitChars = EnglishCharacterData.Digit;
    CharacterRule digitRule = new CharacterRule(digitChars, 2);
     
    CharacterData specialChars = new CharacterData() {
      public String getErrorCode() {
        return CharacterCharacteristicsRule.ERROR_CODE;
      }
     
      public String getCharacters() {
        return "!@#$%^&*()_+";
      }
    };
    CharacterRule splCharRule = new CharacterRule(specialChars, 2);
    PasswordGenerator gen = new PasswordGenerator(10, splCharRule, lowerCaseRule,
            upperCaseRule, digitRule);
    String password = gen.generate().toString();
    log.debug("password: {}", password);
    return password;
  }

}
