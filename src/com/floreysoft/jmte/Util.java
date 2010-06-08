package com.floreysoft.jmte;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Assorted static utility methods.
 * 
 */
public class Util {

	/**
	 * Writes a string into a file.
	 * 
	 * @param string the string
	 * @param file the file
	 * @param charsetName encoding of the file
	 * @throws IOException
	 */
	public static void stringToFile(String string, File file, String charsetName)
			throws IOException {
		FileOutputStream fos = null;
		Writer writer = null;
		try {
			fos = new FileOutputStream(file);
			writer = new OutputStreamWriter(fos, charsetName);
			writer.write(string);
		} finally {
			if (writer != null) {
				writer.close();
			} else if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * Transforms a file into a string.
	 * 
	 * @param file
	 *            the file to be transformed
	 * @param charsetName
	 *            encoding of the file
	 * @return the string containing the content of the file
	 */
	public static String fileToString(File file, String charsetName)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			return streamToString(fileInputStream, charsetName);
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}

	/**
	 * Transforms a file into a string.
	 * 
	 * @param fileName
	 *            name of the file to be transformed
	 * @param charsetName
	 *            encoding of the file
	 * @return the string containing the content of the file
	 */
	public static String fileToString(String fileName, String charsetName)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		return fileToString(new File(fileName), charsetName);
	}

	/**
	 * Transforms a stream into a string.
	 * 
	 * @param is
	 *            the stream to be transformed
	 * @param charsetName
	 *            encoding of the file
	 * @return the string containing the content of the stream
	 */
	public static String streamToString(InputStream is, String charsetName)
			throws UnsupportedEncodingException, IOException {
		Reader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(is, charsetName));
			return readerToString(r);
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Transforms a reader into a string.
	 * 
	 * @param reader
	 *            the reader to be transformed
	 * @return the string containing the content of the reader
	 */
	public static String readerToString(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			sb.append(buf, 0, numRead);
		}
		return sb.toString();
	}

	/**
	 * Transforms any array to a matching list
	 * 
	 * @param value
	 *            something that might be an array
	 * @return List representation if passed in value was an array,
	 *         <code>null</code> otherwise
	 */
	@SuppressWarnings("unchecked")
	public static List arrayAsList(Object value) {
		List list = null;
		if (value instanceof int[]) {
			list = new ArrayList();
			int[] array = (int[]) value;
			for (int i : array) {
				list.add(i);
			}
		} else if (value instanceof short[]) {
			list = new ArrayList();
			short[] array = (short[]) value;
			for (short i : array) {
				list.add(i);
			}
		} else if (value instanceof char[]) {
			list = new ArrayList();
			char[] array = (char[]) value;
			for (char i : array) {
				list.add(i);
			}
		} else if (value instanceof byte[]) {
			list = new ArrayList();
			byte[] array = (byte[]) value;
			for (byte i : array) {
				list.add(i);
			}
		} else if (value instanceof long[]) {
			list = new ArrayList();
			long[] array = (long[]) value;
			for (long i : array) {
				list.add(i);
			}
		} else if (value instanceof double[]) {
			list = new ArrayList();
			double[] array = (double[]) value;
			for (double i : array) {
				list.add(i);
			}
		} else if (value instanceof float[]) {
			list = new ArrayList();
			float[] array = (float[]) value;
			for (float i : array) {
				list.add(i);
			}
		} else if (value instanceof boolean[]) {
			list = new ArrayList();
			boolean[] array = (boolean[]) value;
			for (boolean i : array) {
				list.add(i);
			}
		} else if (value.getClass().isArray()) {
			Object[] array = (Object[]) value;
			list = Arrays.asList(array);
		}
		return list;
	}

	/**
	 * Trims off white space from the beginning of a string.
	 * 
	 * @param input
	 *            the string to be trimmed
	 * @return the trimmed string
	 */
	public static String trimFront(String input) {
		int i = 0;
		while (i < input.length() && Character.isWhitespace(input.charAt(i)))
			i++;
		return input.substring(i);
	}

	/**
	 * Finds the property value for a certain object.
	 * 
	 * 
	 * @param o
	 *            object to find the property value for
	 * @param attributeName
	 *            name of the requested attribute
	 * @return the value for the requested attribute or <code>null</code> when
	 *         there is no such value
	 */
	@SuppressWarnings("unchecked")
	public static Object getPropertyValue(Object o, String attributeName)
			throws IntrospectionException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchFieldException {
		Object result = null;
		BeanInfo beanInfo = Introspector.getBeanInfo(o.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo
				.getPropertyDescriptors();
		// XXX this is so strange, can not call invoke on key and value for
		// Map.Entry, so we have to get this done like this:
		if (o instanceof Map.Entry) {
			Map.Entry entry = (Entry) o;
			if (attributeName.equals("key")) {
				result = entry.getKey();
			} else if (attributeName.equals("value")) {
				result = entry.getValue();
			}

		} else {

			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (propertyDescriptor.getName().equals(attributeName)) {
					Method readMethod = propertyDescriptor.getReadMethod();
					if (readMethod != null) {
						result = readMethod.invoke(o);
						break;
					}
				}
			}
			if (result == null) {
				Field field = o.getClass().getField(attributeName);
				if (Modifier.isPublic(field.getModifiers())) {
					result = field.get(o);
				}
			}
		}
		return result;
	}
}
