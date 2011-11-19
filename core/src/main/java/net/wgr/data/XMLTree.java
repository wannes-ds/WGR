/*
 * Made by Wannes 'W' De Smet
 * (c) 2011 Wannes De Smet
 * All rights reserved.
 * 
 */
package net.wgr.data;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 
 * @created Aug 25, 2011
 * @author double-u
 */
public class XMLTree {

    private HashMap<String, Object> objects;

    public XMLTree(String fileName) {
        load(fileName);
    }

    protected final void load(String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            XStream xstream = new XStream();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(getClass().getName()).log(Level.ERROR, "Failed to open settings file", ex);
            }
            // CODE BLACK - 3h+ to learn that you do have to give the parser XML (NOT A FILE PATH)
            objects = (HashMap<String, Object>) xstream.fromXML(fis);
            xstream = null;
        } else {
            Logger.getLogger(getClass()).warn("Settings file was not found at " + fileName);
            objects = new HashMap<>();
        }
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public Object get(String key) {
        String[] parts = StringUtils.split(key, '.');
        if (parts.length == 0) return null;
        Object current = objects.get(parts[0]);
        String currentPath = "";
        boolean atEnd = false;

        for (int i = 0; i < parts.length; i++) {
            if (i == parts.length - 1) {
                atEnd = true;
            }

            currentPath += parts[i] + ".";

            if (current instanceof HashMap) {
                if (atEnd) {
                    throw new IllegalArgumentException("The key's location refers to a group of keys");
                }
                HashMap<String, Object> o = (HashMap<String, Object>) current;
                current = o.get(parts[i + 1]);

            } else {
                return notNull(current, currentPath);
            }
        }

        return null;
    }

    public HashMap<String, Object> getSection(String key) {
        String[] parts = StringUtils.split(key, '.');
        HashMap<String, Object> current = null;
        String currentPath = "";
        boolean atEnd = false;

        for (int i = 0; i < parts.length; i++) {
            if (i == parts.length - 1) {
                atEnd = true;
            }

            currentPath += parts[i];

            Object o = null;
            if (current == null) {
                o = objects.get(parts[i]);
            } else {
                o = current.get(parts[i]);
            }

            if (o instanceof HashMap) {
                if (atEnd) {
                    return (HashMap<String, Object>) o;
                }
                if (current == null) {
                    current = notNull((HashMap<String, Object>) objects.get(parts[i]), currentPath);
                } else {
                    current = notNull((HashMap<String, Object>) current.get(parts[i]), currentPath);
                }

                if (currentPath.equals(key)) {
                    return current;
                }
            }
        }

        return null;
    }

    protected <T> T notNull(T value, String path) {
        if (value == null) {
            throw new IllegalArgumentException("Key " + path + " does not exist");
        }
        return value;
    }

    public void set(String key, String value) {
        objects.put(key, value);
    }

    public void reload(String fileName) {
        objects.clear();
        load(fileName);
    }

    public void save(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.ERROR, "Failed to create file", ex);
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            XStream xstream = new XStream();
            xstream.toXML(objects, fos);
            xstream = null;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(getClass().getName()).log(Level.ERROR, "Failed to write file", ex);
        }
    }

    public void invalidate(String key) {
        objects.remove(key);
    }

    public boolean keyExists(String keyName) {
        String[] splitsies = StringUtils.split(keyName, '.');
        HashMap<String, Object> current = objects;
        for (int i = 0; i < splitsies.length; i++) {
            if (current.containsKey(splitsies[i])) {
                if (current.get(splitsies[i]) instanceof HashMap) {
                    current = (HashMap<String, Object>) current.get(splitsies[i]);
                    if (i == splitsies.length - 1) {
                        return true;
                    }
                } else if (current.get(splitsies[i]) != null) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}