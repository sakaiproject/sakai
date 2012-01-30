package org.sakaiproject.scorm.dao.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.adl.sequencer.ISeqActivityTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;

public class StandaloneActivityTreeDaoImpl implements SeqActivityTreeDao {

	private static Log log = LogFactory.getLog(StandaloneActivityTreeDaoImpl.class);

	private final String storagePath = "trees";

	public ISeqActivityTree find(long contentPackageId, String userId) {
		return (ISeqActivityTree) getObject(getKey(contentPackageId, userId));
	}

	private String getKey(long contentPackageId, String learnerId) {
		return new StringBuilder(learnerId).append(":").append(contentPackageId).toString();
	}

	public Object getObject(String key) {
		File objectFile = new File(storagePath, key);

		if (objectFile.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectFile));

				Object obj = ois.readObject();

				return obj;
			} catch (Exception e) {
				log.error("Unable to unserialize the object for " + key, e);
			}
		}

		return null;
	}

	public void putObject(String key, Object object) {
		File directory = new File(storagePath);
		File objectFile = new File(directory, key);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				log.error("Unable to create directory " + directory);
			}
		}

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(objectFile);

			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.close();
			fos.close();
		} catch (Exception e) {
			log.error("Unable to serialize object to disk", e);
		}
	}

	public void save(ISeqActivityTree tree) {
		putObject(getKey(tree.getContentPackageId(), tree.getLearnerID()), tree);
	}

}
