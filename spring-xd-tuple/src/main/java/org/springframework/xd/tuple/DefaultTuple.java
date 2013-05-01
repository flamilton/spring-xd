/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.xd.tuple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Default implementation of Tuple interface
 * 
 * @author Mark Pollack
 *
 */
public class DefaultTuple implements Tuple {

	//TODO - error handling - when delegating to the conversion service, the ConversionFailedException does not have the context of which key
	//					      caused the failure.  Need to wrap ConversionFailedException with IllegalArgumentException and add that context back in.
	
	//TODO consider LinkedHashMap and map to link index position to nam,e, look at efficient impls in goldman sach's collection class lib.
	private List<String> names;
	private List<Object> values;
	private FormattingConversionService formattingConversionService;
	
	private UUID id;
	private Long timestamp;
	
	
	//TODO consider making final and package protect ctor so as to always use TupleBuilder
	
	public DefaultTuple(List<String> names, List<Object> values, FormattingConversionService formattingConversionService) {
		Assert.notNull(names);
		Assert.notNull(values);
		Assert.notNull(formattingConversionService);
		if (values.size() != names.size()) {
			throw new IllegalArgumentException("Field names must be same length as values: names="
					+ names + ", values=" + values);
		}
		//TODO check for no duplicate names.
		//TODO check for no null values.
		this.names = new ArrayList<String>(names);
		this.values = new ArrayList<Object>(values);  // shallow copy
		this.formattingConversionService = formattingConversionService;
		this.id = UUID.randomUUID();
		this.timestamp = new Long(System.currentTimeMillis());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#size()
	 */
	@Override
	public int size() {
		return values.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#getId()
	 */
	@Override
	public UUID getId() {
		return this.id;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#getTimestamp()
	 */
	@Override
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	/**
	 * Return the values for all the fields in this tuple
	 * @return an unmodifiable List of names.
	 */
	@Override
	public List<String> getFieldNames() {
		return Collections.unmodifiableList(names);
	}
	
	/**
	 * Return the values for all the fields in this tuple
	 * @return an unmodifiable List list of values.
	 */
	@Override
	public List<Object> getValues() {
		return Collections.unmodifiableList(values);
	}
	
	@Override
	public int getFieldCount() {
		return this.names.size();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#hasName(java.lang.String)
	 */
	@Override
	public boolean hasFieldName(String name) {
		return names.contains(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String name) {
		return values.get(indexOf(name));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#getValue(int)
	 */
    @Override
	public Object getValue(int index) {
		return values.get(index);
	}
    
    
    
	@SuppressWarnings("rawtypes")
	@Override
	public List<Class> getFieldTypes() {
		ArrayList<Class> types = new ArrayList<Class>(values.size());
		for (Object val : values) {
			types.add(val.getClass());
		}
		return Collections.unmodifiableList(types);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DefaultTuple)) {
			return false;
		}
		DefaultTuple other = (DefaultTuple) obj;
		if (names == null) {
			if (other.names != null) {
				return false;
			}
		} else if (!names.equals(other.names)) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String getString(String name) {
		return getString(indexOf(name));
	}
	
	@Override
	public String getString(int index) {
		return readAndTrim(index);
	}
	/**
	 * @param index
	 * @return
	 */
	private String readAndTrim(int index) {
		Object rawValue = values.get(index);
		if (rawValue != null) {
			String value = convert(rawValue, String.class);
			if (value != null) {
				return value.trim();
			} else {
				return null;
			}				
		} else {
			return null;
		}
	}
	
	@Override
	public Character getChar(int index) {
		String value = readAndTrim(index);

		Assert.isTrue(value.length() == 1, "Cannot convert field value '" + value + "' to char.");

		return value.charAt(0);
	}
	
	@Override
	public Character getChar(String name) {
		return getChar(indexOf(name));
	}
	
	@Override
	public Boolean getBoolean(int index) {
		return getBoolean(index, "true");
	}
	
	@Override
	public Boolean getBoolean(String name) {
		return getBoolean(indexOf(name));
	}
	
	@Override
	public Boolean getBoolean(int index, String trueValue) {
		Assert.notNull(trueValue, "'trueValue' cannot be null.");
		String value = readAndTrim(index);
		return trueValue.equals(value) ? true : false;
		
	}
	
    @Override
	public Boolean getBoolean(String name, String trueValue) {
		return getBoolean(indexOf(name), trueValue);
	}
    
    @Override 
    public Byte getByte(String name) {
    	return getByte(indexOf(name));
    }
    
    @Override
    public Byte getByte(int index) {
		return convert(values.get(index), Byte.class);
    }

	@Override
	public Short getShort(String name) {
		return getShort(indexOf(name));
	}
	
    
	@Override
	public Short getShort(int index) {
		return convert(values.get(index), Short.class);
	}
	
	@Override
	public Integer getInt(String name) {
		return getInt(indexOf(name));
	}
    
	@Override
	public Integer getInt(int index) {
		return convert(values.get(index), Integer.class);
	}

	@Override
	public Long getLong(String name) {
		return getLong(indexOf(name));
	}
	
	@Override
	public Long getLong(int index) {
		return convert(values.get(index), Long.class);
	}

    
	@Override
	public Float getFloat(String name) {
		return getFloat(indexOf(name));
	}
	
	@Override
	public Float getFloat(int index) {
		return convert(values.get(index), Float.class);
	}
	

	@Override
	public Double getDouble(String name) {
		return getDouble(indexOf(name));
	}
	
	@Override
	public Double getDouble(int index) {
		return convert(values.get(index), Double.class);
	}

	@Override
	public BigDecimal getBigDecimal(String name) {
		return getBigDecimal(indexOf(name));
	}

	@Override
	public BigDecimal getBigDecimal(int index) {
		return convert(values.get(index), BigDecimal.class);
	}

	@Override
	public Date getDate(int index) {
		return convert(values.get(index), Date.class);
	}
	
	@Override
	public Date getDate(String name) {
		return getDate(indexOf(name));
	}
	
	@Override
	public Date getDate(int index, String pattern) {
		StringToDateConverter converter = new StringToDateConverter(pattern);
		return converter.convert(this.readAndTrim(index));
	}
	
	@Override
	public Date getDate(String name, String pattern) {
		try {
			return getDate(indexOf(name), pattern);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage() + ", name: [" + name + "]");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#getValue(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T getValue(String name, Class<T> valueClass) {
		Object value = values.get(indexOf(name));
		return convert(value, valueClass);
	}
	/* (non-Javadoc)
	 * @see org.springframework.xd.tuple.Tuple#getValue(int, java.lang.Class)
	 */
	@Override
	public <T> T getValue(int index, Class<T> valueClass) {
		return convert(values.get(index), valueClass);
	}
	@SuppressWarnings("unchecked")
	@Override
	public Tuple select(String expression) {
		EvaluationContext context = new StandardEvaluationContext(toMap());
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(expression);
		
		//TODO test instance is a map
		Object result = exp.getValue(context);
		Map<String, Object> resultMap = null;
		if (ClassUtils.isAssignableValue(Map.class, result)) 
		{
			resultMap = (Map<String, Object>)result;
		}
		if (resultMap != null) {
			return toTuple(resultMap);				
		} else {
			return new DefaultTuple(new ArrayList<String>(0), new ArrayList<Object>(0), this.formattingConversionService);
		}
	}
	/**
	 * @return
	 */
	private Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<String, Object>(values.size());		
		for (int i = 0; i < values.size(); i++) {
			map.put(names.get(i), values.get(i));
		}
		return map;
	}
	
	private Tuple toTuple(Map<String, Object> resultMap) {
		
		List<String> newNames = new ArrayList<String>();
		List<Object> newValues = new ArrayList<Object>();
		for (String name : resultMap.keySet()) {
			newNames.add(name);
		}
		for (Object value : resultMap.values()) {
			newValues.add(value);
		}
		return new DefaultTuple(newNames, newValues, this.formattingConversionService);
		
	}


	
	
	@SuppressWarnings("unchecked")
	<T> T convert(Object value, Class<T> targetType) {
		//TODO wrap ConversionFailedException in IllegalArgumentException... may need to pass in index/field name for good error reporting.
		return (T) formattingConversionService.convert(value, TypeDescriptor.forObject(value), TypeDescriptor.valueOf(targetType));
	}
	
	
	

	/**
	 * Find the index in the names collection for the given name.
	 * 
	 * @throws IllegalArgumentException if a the given name is not defined.
	 */
	protected int indexOf(String name) {
		int index = names.indexOf(name);
		if (index >= 0) {
			return index;
		}
		throw new IllegalArgumentException("Cannot access field [" + name + "] from " + names);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultTuple [names=" + names + ", values=" + values + ", id="
				+ id + ", timestamp=" + timestamp + "]";
	}


}