package org.sakaiproject.user.impl;

import java.util.Optional;
import java.util.Stack;

import javax.inject.Inject;

import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.repository.PreferenceRepository;
import org.sakaiproject.user.api.model.Preference;
import org.sakaiproject.util.StorageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import lombok.Setter;

@Setter
public class JpaPreferencesService extends BasePreferencesService {

    @Inject
    private PreferenceRepository preferenceRepository;

    @Override
    protected Storage newStorage() {
        return new JpaStorage();
    }

    protected class JpaStorage implements Storage {

        public void open() {}

        public void close() {}

        public boolean check(String id) {
            return preferenceRepository.existsById(id);
        }

        public Preferences get(String id) {
            Optional<Preference> opt = preferenceRepository.findById(id);
            if (!opt.isPresent()) return null;
            Preference pref = opt.get();
            if (pref.getXml() == null) return new BasePreferences(id);
            Document doc = StorageUtils.readDocumentFromString(pref.getXml());
            if (doc == null) return new BasePreferences(id);
            Element el = doc.getDocumentElement();
            return new BasePreferences(el);
        }

        public PreferencesEdit put(String id) {
            if (preferenceRepository.existsById(id)) return null;
            BasePreferences pref = new BasePreferences(id);
            pref.activate();
            return pref;
        }

        public PreferencesEdit edit(String id) {
            Optional<Preference> opt = preferenceRepository.findById(id);
            if (!opt.isPresent()) return null;
            Preference pref = opt.get();
            BasePreferences bp;
            if (pref.getXml() == null) {
                bp = new BasePreferences(id);
            } else {
                Document doc = StorageUtils.readDocumentFromString(pref.getXml());
                if (doc == null) return null;
                bp = new BasePreferences(doc.getDocumentElement());
            }
            bp.activate();
            return bp;
        }

        public void commit(PreferencesEdit edit) {
            Document doc = StorageUtils.createDocument();
            ((BasePreferences) edit).toXml(doc, new Stack<>());
            String xml = StorageUtils.writeDocumentToString(doc);
            Preference pref = preferenceRepository.findById(edit.getId()).orElse(new Preference());
            pref.setId(edit.getId());
            pref.setXml(xml);
            preferenceRepository.save(pref);
        }

        public void cancel(PreferencesEdit edit) {
            // nothing to do
        }

        public void remove(PreferencesEdit edit) {
            preferenceRepository.deleteById(edit.getId());
        }
    }
}
