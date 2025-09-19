package sc.fiji.CMP_BIA.tools;
package sc.fiji.CMP_BIA.tools;

import java.util.HashMap;

/**
 * @class OptionalParameters
 * @version 0.1
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @brief Class for handling optional parameters as key-value pairs
 */
public class OptionalParameters {

	private HashMap<String, Double> parameters;

	/**
	 * Constructor with initial parameters
	 * @param params Initial parameter map
	 */
	public OptionalParameters(HashMap<String, Double> params) {
		this.parameters = new HashMap<String, Double>(params);
	}

	/**
	 * Default constructor
	 */
	public OptionalParameters() {
		this.parameters = new HashMap<String, Double>();
	}

	/**
	 * Add or update a parameter
	 * @param key Parameter name
	 * @param value Parameter value
	 */
	public void putParam(String key, Double value) {
		this.parameters.put(key, value);
	}

	/**
	 * Remove a parameter
	 * @param key Parameter name to remove
	 */
	public void removeParam(String key) {
		this.parameters.remove(key);
	}

	/**
	 * Print all parameters
	 */
	public void printParams() {
		System.out.println("Parameters:");
		for (String key : parameters.keySet()) {
			System.out.println("  " + key + " = " + parameters.get(key));
		}
		System.out.println();
	}

	/**
	 * Get parameter value
	 * @param key Parameter name
	 * @return Parameter value or null if not found
	 */
	public Double getParam(String key) {
		return this.parameters.get(key);
	}

	/**
	 * Check if parameter exists
	 * @param key Parameter name
	 * @return true if parameter exists
	 */
	public boolean hasParam(String key) {
		return this.parameters.containsKey(key);
	}
}
import java.util.HashMap;
import java.util.Map;

/**
 * @class OptionalParameters
 * @version 0.1
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @brief Utility class for managing optional parameters
 */
public class OptionalParameters {

    private HashMap<String, Double> parameters;

    /**
     * Constructor that initializes with a HashMap of parameters
     * @param params Initial parameters
     */
    public OptionalParameters(HashMap<String, Double> params) {
        this.parameters = new HashMap<>(params);
    }

    /**
     * Add or update a parameter
     * @param key Parameter name
     * @param value Parameter value
     */
    public void putParam(String key, Double value) {
        parameters.put(key, value);
    }

    /**
     * Remove a parameter
     * @param key Parameter name to remove
     */
    public void removeParam(String key) {
        parameters.remove(key);
    }

    /**
     * Print all parameters
     */
    public void printParams() {
        System.out.println("Parameters:");
        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }
        System.out.println("Total parameters: " + parameters.size());
    }

    /**
     * Get a parameter value
     * @param key Parameter name
     * @return Parameter value or null if not found
     */
    public Double getParam(String key) {
        return parameters.get(key);
    }

    /**
     * Check if parameter exists
     * @param key Parameter name
     * @return true if parameter exists
     */
    public boolean hasParam(String key) {
        return parameters.containsKey(key);
    }
}
