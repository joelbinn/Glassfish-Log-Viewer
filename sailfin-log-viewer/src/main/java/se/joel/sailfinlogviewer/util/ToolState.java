package se.joel.sailfinlogviewer.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ToolState {
    private static final String LAST_SELECTED_FILE = "LastSelectedFile";
    private static final File TOOLSTATE_FILE = new File(System.getProperty("user.home") + File.separatorChar + ".logviewertoolstate");
    private static Logger logger = Logger.getLogger("ToolState");
    private Properties properties;

    public File getLastSelectedFile() {
        try {
            readStateFile();

            String lastSelectedFileName = properties.getProperty(LAST_SELECTED_FILE, null);

            return (lastSelectedFileName != null) ? new File(lastSelectedFileName) : null;
        } catch (InvalidPropertiesFormatException e) {
            logger.log(Level.WARNING, "Error when reading " + LAST_SELECTED_FILE, e);
        } catch (FileNotFoundException e) {
            // OK, will create a file when saving...
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error when reading " + LAST_SELECTED_FILE, e);
        }

        return null;
    }

    public void setLastSelectedFile(File file) throws IOException {
        properties.put(LAST_SELECTED_FILE, file.getAbsolutePath());
        syncToFile();
    }

    private void syncToFile() throws FileNotFoundException, IOException {
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(TOOLSTATE_FILE);
            properties.storeToXML(os, "");
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private void readStateFile() throws InvalidPropertiesFormatException, IOException {
        if (properties == null) {
            properties = new Properties();

            FileInputStream in = null;

            try {
                in = new FileInputStream(TOOLSTATE_FILE);
                properties.loadFromXML(in);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    public Collection<? extends String> getList(String listName) throws InvalidPropertiesFormatException, IOException {
        readStateFile();

        List<String> result = new ArrayList<String>();
        String listString = (String) properties.get(listName);
        List<String> list = stringToList(listString);

        if (list != null) {
            result.addAll(list);
        }

        return result;
    }

    public void saveList(String listName, Iterable<?> list) throws FileNotFoundException, IOException {
        if (list != null) {
            properties.put(listName, listToString(list));
            syncToFile();
        }
    }

    private List<String> stringToList(String listString) {
        List<String> result = new ArrayList<String>();

        if ((listString != null) && (listString.length() > 0)) {
            StringTokenizer st = new StringTokenizer(listString, ",");

            while (st.hasMoreTokens()) {
                String listItem = st.nextToken();
                listItem = listItem.replaceAll("%2", ",");
                listItem = listItem.replaceAll("%1", "%");

                if (!result.contains(listItem)) {
                    result.add(listItem);
                }
            }
        }

        return result;
    }

    private Object listToString(Iterable<?> list) {
        StringBuilder sb = new StringBuilder();

        for (Object o : list) {
            String string = o + "";
            string = string.replaceAll("%", "%1");
            string = string.replaceAll(",", "%2");
            sb.append(string).append(',');
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public boolean restorePosAndSize(String name, Component component)  {
        try {
            readStateFile();
            int x = Integer.parseInt(properties.getProperty(name+".x"));
            int y = Integer.parseInt(properties.getProperty(name+".y"));
            int w = Integer.parseInt(properties.getProperty(name+".w"));
            int h = Integer.parseInt(properties.getProperty(name+".h"));
            component.setLocation(new Point(x, y));
            component.setSize(new Dimension(w, h));
            return true;
        } catch (NumberFormatException e) {
            return false;
        } catch (InvalidPropertiesFormatException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public void savePosAndSize(String name, Component component) throws FileNotFoundException, IOException {
        properties.put(name+".x", component.getLocation().x+"");
        properties.put(name+".y", component.getLocation().y+"");
        properties.put(name+".w", component.getSize().width+"");
        properties.put(name+".h", component.getSize().height+"");
        syncToFile();
    }
}
