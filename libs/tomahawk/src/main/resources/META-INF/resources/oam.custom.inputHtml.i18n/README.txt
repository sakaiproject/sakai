Translation files for kupu.

These files are included in PloneTranslations so that they may be
maintained by Plone's translators who may not necessarily have direct
access to the Codespeak subversion server (which is where Kupu source
is held).  They are in a separate folder so that they may be pulled
into Kupu (using svn:external) and used for non-Plone implementations.

The i18n domains are:

kupu
        Contains all translatable strings used by kupu in Plone except
        those used in the Plone configuration. Also contains some
        strings used only in non-Plone versions of Kupu.

kupuconfig
        Translatable strings used by kupu's Plone configuration
        screens.

kupupox
        Translatable strings used in Javascript. N.B. Translation of
        Javascript strings is not implemented in Plone, so this domain
        is currently only used by other Kupu implementations.
