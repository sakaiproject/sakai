/**
 * Copyright (c) 2005-2009 The Apereo Foundation
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
package org.sakaiproject.component.kerberos.user;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 * This test currently fail at times. It only fails for good user/pass and it detects a replay attack.
 * We think it is because two auths happen with the same timestamp. I can't manage to get bad user/pass to come back as good.
 * And we only see the problems because several thread are using the same user/pass combination.
 * This test performs a DOS attack on my ADSL router (can't track this many UDP connections?). 
 * @author Matthew Buckett
 *
 */
@Slf4j
public class ThreadedJaasAuthenticateTest extends AbstractAuthenticateTest {

	private int loopLimit = 1000;
	private int threadCount = 10;
	
	public void testThreads() throws InterruptedException {
		UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
		Thread[] threads = new Thread[threadCount];
		Random rnd = new Random();
		for (int i = 0; i < threadCount ; i++) {
			String name;
			if (rnd.nextBoolean()) {
				name = "Thread-"+ i+ "-good";
				threads[i] = new Thread(new Authenticate(goodUser, goodPass, true), name);
			} else {
				name = "Thread-"+ i+ "-bad";
				threads[i] = new Thread(new Authenticate(badUser, badPass, false), name);
			}
			threads[i].setUncaughtExceptionHandler(handler);
			threads[i].start();
			log.info("Started "+ name);
		}
		for (Thread thread: threads) {
			thread.join();
		}
	}
	
	private class Authenticate implements Runnable {

		String username;
		String password;
		boolean good;
		
		private Authenticate(String username, String password, boolean good) {
			this.username = username;
			this.password = password;
			this.good = good;
		}
		
		public void run() {
			for(int i = 0; i< loopLimit; i++) {
				JassAuthenticate jass = new JassAuthenticate(servicePrincipal, "ServiceKerberosAuthentication", "KerberosAuthentication");
				assertEquals(good,jass.attemptAuthentication(username, password));
			}
		}
		
	}

}
