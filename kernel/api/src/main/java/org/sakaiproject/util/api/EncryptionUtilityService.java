/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.util.api;

public interface EncryptionUtilityService {

  /**
  * Encrypt allows String encryption using a server generated secret key.
  * See http://www.jasypt.org/api/jasypt/1.9.3/org/jasypt/util/text/AES256TextEncryptor.html#encrypt-java.lang.String-
  * @param stringToEncrypt The String to encrypt.
  * @return the string encrypted using a server generated secret key.
  */
  public String encrypt(String stringToEncrypt);

  /**
  * Decrypt allows String decryption using a server generated secret key.
  * See http://www.jasypt.org/api/jasypt/1.9.3/org/jasypt/util/text/AES256TextEncryptor.html#decrypt-java.lang.String-
  * @param stringToDecrypt The String to decrypt.
  * @return the string decrypted using a server generated secret key.
  */
  public String decrypt(String stringToDecrypt);

}
