package org.sakaiproject.user.impl;

import java.util.Optional;
import java.util.Stack;

import org.springframework.beans.factory.annotation.Autowired;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.repository.PreferenceRepository;
import org.sakaiproject.user.api.model.Preference;
import org.sakaiproject.util.StorageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JpaPreferencesService extends BasePreferencesService {

    private PreferenceRepository preferenceRepository;
    
    public void setPreferenceRepository(PreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    protected Storage newStorage() {
        return new JpaStorage();
    }
    
    // Abstract method implementations for dependencies
    private MemoryService m_memoryService = null;
    protected MemoryService memoryService() { return m_memoryService; }

    private ServerConfigurationService m_serverConfigurationService = null;
    protected ServerConfigurationService serverConfigurationService() { return m_serverConfigurationService; }

    private EntityManager m_entityManager = null;
    protected EntityManager entityManager() { return m_entityManager; }

    private SecurityService m_securityService = null;
    protected SecurityService securityService() { return m_securityService; }

    private FunctionManager m_functionManager = null;
    protected FunctionManager functionManager() { return m_functionManager; }

    private SessionManager m_sessionManager = null;
    protected SessionManager sessionManager() { return m_sessionManager; }

    private EventTrackingService m_eventTrackingService = null;
    protected EventTrackingService eventTrackingService() { return m_eventTrackingService; }

    private UserDirectoryService m_userDirectoryService = null;
    protected UserDirectoryService userDirectoryService() { return m_userDirectoryService; }
    
    // Added for backward compatibility with SqlService
    private SqlService m_sqlService = null;
    protected SqlService sqlService() { return m_sqlService; }
    
    /**
     * Final initialization, once all dependencies are set.
     */
    public void init() {
        try {
            super.init();
            
            log.info("init(): JPA-based preference storage ready");
        } catch (Exception t) {
            log.warn("init(): ", t);
        }
    }
    
    /**
     * Returns to uninitialized state.
     */
    public void destroy() {
        super.destroy();
        log.info("destroy(): JPA-based preference storage shutdown");
    }

    @lombok.RequiredArgsConstructor
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
