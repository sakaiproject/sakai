Author: Ian Boston <ian@caret.cam.ac.uk>


 * Filter User Directory Provider, calls a configure provider, and if that fails
 * calls the next provider in the chain. It does this for all methods. If the
 * response is a boolean, then true stop processing, false continues processon.
 * It is the reponsibility of the injected user directory provider to ignore
 * those calls that have nothing to do with it, either by reference to the
 * session, or by refernce to something in the environment or request. To Use,
 * add one or more of these beans to Spring, in a chain, marking the first one
 * in the chain as the 'official' userdirectory provider used by Sakai.
 * Construct the chain by setting the next FilterUserDirectorProvider to the
 * nextProvider and the real User Directory Provider to myProvider eg
