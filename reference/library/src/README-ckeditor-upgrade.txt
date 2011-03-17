----------------------------
UPGRADING CKEDITOR FOR SAKAI
----------------------------

Performing an upgrade of CKEditor is quite straightforward since we do not
modify any of the distributed files. The package should be expanded and the
hidden/source files removed, indicated by directories beginning with an
underscore.

To keep trunk (or maintenance branches) in working order while upgrading as
well as catching any removed files, a temporary branch should be used. Below is
a sample workflow. It assumes a UNIX-like operating system (Mac OSX 10.6 was
used for 3.4.1->3.5.2).

As usual, you can work from any directories you like. For this example, you can
assume that this work all begins in /tmp if that is helpful. Please note that
this sample has the specific version and ticket numbers for one upgrade. These
are samples and should not be used verbatim.


    1. Create a temporary branch of reference/trunk

        svn cp -m 'Creating temporary branch to upgrade CKEditor to 3.5.2' \
            https://source.sakaiproject.org/svn/reference/trunk \
            https://source.sakaiproject.org/svn/reference/branches/SAK-20296

    2. Check out the temporary branch

        svn co https://source.sakaiproject.org/svn/reference/branches/SAK-20296

    3. Download the CKEditor distribution

        curl -O http://download.cksource.com/CKEditor/CKEditor/CKEditor%203.5.2/ckeditor_3.5.2.tar.gz

    4. Purge the current CKEditor version in the working copy and commit

        cd SAK-20206/library/src/webapp/editor
        svn rm ckeditor
        svn ci -m 'Removing CKEditor 3.4.1'

    5. Unpack CKEditor contents and remove sample and source materials

        tar zxvf /tmp/ckeditor_3.5.2.tar.gz
        rm -r ckeditor/_source
        rm -r ckeditor/_samples

    6. Add the new editor to the working copy and commit

        svn add ckeditor
        svn ci -m 'Adding CKEditor 3.5.2'

    7. Check out the destination branch (e.g., trunk)

        cd /tmp
        svn co https://source.sakaiproject.org/svn/reference/trunk

    8. Merge the temporary branch and commit

        cd trunk
        svn merge --reintegrate \
            https://source.sakaiproject.org/svn/reference/branches/SAK-20296 .
        svn ci -m 'SAK-20296 - Upgrading CKEditor to 3.5.2'


NOTES
-----

  * You should use at least Subversion 1.5 (ideally, 1.6 or newer) to take
    advantage of the enhanced merge tracking. This yields cleaner repository
    history and easier merges.
  * When merging into maintenance branches (step 8), you should specify the
    revision of trunk. It almost certainly contains upstream changes that you
    do not wish to merge. An example is:
    
        svn merge -c 89942 https://source.sakaiproject.org/svn/reference/trunk .

