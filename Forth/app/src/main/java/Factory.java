import exceptions.ConfigException;
import exceptions.InstantiationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * {@code Factory} based on map which is used for creating objects by their
 * class's names
 * <p>
 *      Firstly, for configuring factory {@code .properties} file required. It must contain
 *      pairs Key-Name of class, which will be used for filling maps with content.
 *      Then, after finding classes by their names and loading them, each of those
 *      classes will be placed in second map for future using.
 * </p>
 */
public class Factory {
    private final Map<String, String> propertiesMap = new HashMap<>();
    private final Map<String, Object> objectsMap = new HashMap<>();
    private final static Logger logger = LogManager.getLogger(Factory.class);
    
    public Factory() { 
        logger.debug("Factory created"); 
    }

    /**
     * Creates an object by given key
     * @param key Key needed for creating object
     * @return In case of success returns instance of object
     * @throws InstantiationException In case of any trouble with creating
     * instance of class
     */
    public Object create(String key) {
        if(!objectsMap.containsKey(key)) { 
            load(key);
        }
        
        Class<?> product = (Class<?>)objectsMap.get(key);
        Object instance = null;
        try {
            instance = product.getConstructor().newInstance();
        } catch(Exception exception) {
            throw new InstantiationException("instantiation of " + key + " failed");
        }

        return instance;
    }

    /**
     * Loading class to map by given key, class's name it takes from
     * .properties file
     * @param key Key needed for loading class by it's key
     * @throws ConfigException Throws exception if there is no class with such
     * name(taken from .properties)
     */
    private void load(String key) {
        logger.debug("Trying to load object by key {}", key); 

        String className = propertiesMap.get(key);
        if(className == null) {
            throw new ConfigException("class's name by key " + key + " wasn't found in map");
        }

        Class<?> product = null;
        try {
            product = Class.forName(className);
        } catch(Exception exception) {
            logger.error("Loading failed, no class by key {} in files", key);
            throw new ConfigException("class by key " + key + " wasn't found in files");
        }

        objectsMap.put(key, product);
        logger.debug("Object loaded succesfully");
    }

    /**
     * Parsing .properties file and filling map with pairs from that file
     */
    public void configure(String filePath) throws IOException {
        Properties properties = new Properties();

        try(InputStream input = Factory.class.getResourceAsStream(filePath)) {
            logger.debug("Stream {} for getting {} initialized successfully", input, properties);
            properties.load(input);
            logger.debug("Loaded {}", properties);

            for (String key : properties.stringPropertyNames()) {
                String className = properties.getProperty(key);
                propertiesMap.put(key, className);
                logger.debug("Key {} put in map", key);
            }
        }

        logger.info("map with properties configured: \n{}", propertiesMap);
    }
}
