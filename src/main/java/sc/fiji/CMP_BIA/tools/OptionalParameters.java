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
