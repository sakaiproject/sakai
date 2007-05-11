package org.sakaiproject.sequencing.impl;

import org.adl.sequencer.ADLLaunch;
import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.SeqActivity;
import org.adl.sequencer.SeqNavRequests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sequencing.api.LaunchContent;
import org.sakaiproject.sequencing.api.Sequencer;

public class WrappedADLSequencer extends ADLSequencer implements Sequencer {
	private static Log log = LogFactory.getLog(WrappedADLSequencer.class);

	// Talks to ADL Sequencer
	// Has activityTree, knows about user through parameters

	// Definitely has to override some ADL stuff 'invokeRollup' at min. in 1.3

	public WrappedADLSequencer() {
		super();
	}

	/*public LaunchContent navigationRequest(int iRequest) {
		// This method implements all cases, except case #7 of the Navigation
		// Request Process (NB.2.1).
		//
		// It also applies the Overall Sequencing Process (OP) to the
		// indicated navigation request.

		if (log.isDebugEnabled()) {
			log.debug("  :: ADLSequencer --> BEGIN - navigate");
			log.debug("  ::--> " + iRequest);
		}

		// This function attempts to translate the navigation request into the
		// corresponding termination and sequencing requests, and invoke the
		// overall sequencing process.

		LaunchContent launch = new LaunchContentImpl();

		// Make sure an activity tree has been associated with this sequencer
		if (this.getActivityTree() == null) {
			log.error("  ::--> ERROR : No activity tree defined.");
			if (log.isDebugEnabled()) {
				log.debug("  :: ADLSequencer --> END   - " + "navigate");
			}

			// No activity tree, therefore nothing to do
			// -- inform the caller of the error.
			launch.setSeqNonContent(ADLLaunch.LAUNCH_ERROR);
			launch.setEndSession(true);

			return launch;
		}

		// If this is a new session, we start at the root.
		boolean newSession = false;

		SeqActivity cur = mSeqTree.getCurrentActivity();

		if (cur == null) {
			if (log.isDebugEnabled()) {
				log.debug("  ::--> No current Activity -- New Session");
			}

			prepareClusters();
			newSession = true;

			validateRequests();
		}

		boolean process = true;

		ADLValidRequests valid = null;

		if (newSession && iRequest == SeqNavRequests.NAV_NONE) {
			if (log.isDebugEnabled()) {
				log.debug("  ::--> Processing a TOC request");
			}
		} else if (newSession
				&& (iRequest == SeqNavRequests.NAV_EXITALL || iRequest == SeqNavRequests.NAV_ABANDONALL)) {
			if (log.isDebugEnabled()) {
				System.out
						.println("  ::--> Exiting a session that hasn't started");
			}

			launch.setSeqNonContent(ADLLaunch.LAUNCH_EXITSESSION);
			launch.mEndSession = true;

			process = false;
		} else if (iRequest == SeqNavRequests.NAV_CONTINUE
				|| iRequest == SeqNavRequests.NAV_PREVIOUS) {
			validateRequests();
			valid = mSeqTree.getValidRequests();

			// Can't validate requests -- Error
			if (valid == null) {
				if (log.isDebugEnabled()) {
					log.debug("  ::--> ERROR : "
							+ "Cannot validate request");
				}

				launch.setSeqNonContent(ADLLaunch.LAUNCH_ERROR);
				launch.mEndSession = true;

				// Invalid request -- do not process
				process = false;
			} else {
				if (iRequest == SeqNavRequests.NAV_CONTINUE) {
					if (!valid.mContinue) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> Continue not valid");
						}

						process = false;
						launch.setSeqNonContent(ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ);
					}
				} else {
					if (!valid.mPrevious) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> Previous not valid");
						}

						process = false;
						launch.setSeqNonContent(ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ);
					}
				}
			}
		} else {
			// Use the IMS Navigation Request Process to validate the request
			process = doIMSNavValidation(iRequest);

			if (!process) {
				launch.setSeqNonContent(ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ);
			}
		}

		// Process any pending navigation request
		if (process) {
			// This block implements the overall sequencing loop

			// Clear Global State
			mValidTermination = true;
			mValidSequencing = true;

			String seqReq = null;
			String delReq = null;

			// Translate the navigation request into termination and/or
			// sequencing
			// request(s).
			switch (iRequest) {

			case SeqNavRequests.NAV_START:

				delReq = doSequencingRequest(ADLSequencer.SEQ_START);

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_SEQ_BLOCKED);
				}

				break;

			case SeqNavRequests.NAV_RESUMEALL:

				delReq = doSequencingRequest(ADLSequencer.SEQ_RESUMEALL);

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_SEQ_BLOCKED);
				}

				break;

			case SeqNavRequests.NAV_CONTINUE:

				if (cur.getIsActive()) {
					// Issue a termination request of 'exit'
					seqReq = doTerminationRequest(ADLSequencer.TER_EXIT, false);
				}

				if (mValidTermination) {
					// Issue the pending sequencing request
					if (seqReq == null) {
						delReq = doSequencingRequest(ADLSequencer.SEQ_CONTINUE);
					} else {
						delReq = doSequencingRequest(seqReq);
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Termination");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_NOTHING);
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_SEQ_BLOCKED);
				}

				break;

			case SeqNavRequests.NAV_PREVIOUS:

				if (cur.getIsActive()) {
					// Issue a termination request of 'exit'
					seqReq = doTerminationRequest(ADLSequencer.TER_EXIT, false);
				}

				if (mValidTermination) {
					// Issue the pending sequencing request
					if (seqReq == null) {
						delReq = doSequencingRequest(ADLSequencer.SEQ_PREVIOUS);
					} else {
						delReq = doSequencingRequest(seqReq);
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Termination");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_NOTHING);
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_SEQ_BLOCKED);
				}

				break;

			case SeqNavRequests.NAV_ABANDON:

				// Issue a termination request of 'abandon'
				seqReq = doTerminationRequest(ADLSequencer.TER_ABANDON, false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);

					// If the session hasn't ended, re-validate nav requests
					if (!mEndSession && !mExitCourse) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> REVALIDATE");
						}

						validateRequests();
					}

				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Termination");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_NOTHING);
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_SEQ_BLOCKED);
				}

				break;

			case SeqNavRequests.NAV_ABANDONALL:

				// Issue a termination request of 'abandonAll'
				seqReq = doTerminationRequest(ADLSequencer.TER_ABANDONALL,
						false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Termination");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_NOTHING);
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_SEQ_BLOCKED);
				}

				break;

			case SeqNavRequests.NAV_SUSPENDALL:

				// Issue a termination request of 'suspendAll'
				seqReq = doTerminationRequest(ADLSequencer.TER_SUSPENDALL,
						false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);

				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Termination");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_NOTHING);
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.setSeqNonContent(ADLLaunch.LAUNCH_SEQ_BLOCKED);
				}

				break;

			case SeqNavRequests.NAV_EXIT:

				// Issue a termination request of 'exit'
				seqReq = doTerminationRequest(ADLSequencer.TER_EXIT, false);

				if (mValidTermination) {
					if (seqReq == null) {
						delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
					} else {
						delReq = doSequencingRequest(seqReq);
					}

					// If the session hasn't ended, re-validate nav requests
					if (!mEndSession && !mExitCourse) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> REVALIDATE");
						}

						validateRequests();
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_EXITALL:

				// Issue a termination request of 'exitAll'
				seqReq = doTerminationRequest(ADLSequencer.TER_EXITALL, false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (log.isDebugEnabled()) {
							log.debug("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_NONE:

				// Don't invoke any termination or sequencing requests,
				// but display a TOC if available
				launch.mSeqNonContent = ADLLaunch.LAUNCH_TOC;

				launch.mNavState = mSeqTree.getValidRequests();

				// Make sure that a TOC is realy available
				if (launch.mNavState.mTOC == null) {
					launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
				}

				break;

			default:

				if (log.isDebugEnabled()) {
					log.debug("  ::-->  ERROR : "
							+ "Invalid navigation request: " + iRequest);
				}

				launch.setSeqNonContent(ADLLaunch.LAUNCH_ERROR);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("  ::--> INVALID NAV REQUEST");
			}

			launch.mNavState = mSeqTree.getValidRequests();

			// If navigation requests haven't been validated, try to validate
			// now.
			if (launch.mNavState == null) {
				if (log.isDebugEnabled()) {
					System.out
							.println("  ::--> Not Validated Yet -- DO IT NOW");
				}

				validateRequests();
				launch.mNavState = mSeqTree.getValidRequests();
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("  :: ADLSequencer --> END   - navigate");
		}

		return launch;

	}*/

}
