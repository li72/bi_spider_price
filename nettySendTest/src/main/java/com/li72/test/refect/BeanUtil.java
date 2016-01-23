package com.li72.test.refect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.li72.test.model.User;

public class BeanUtil {

	private static Logger logger = LoggerFactory.getLogger(BeanUtil.class);

	public static void transMap2Bean(Map<String, Object> map, Object obj)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IntrospectionException {

		BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo
				.getPropertyDescriptors();

		for (PropertyDescriptor property : propertyDescriptors) {
			String key = property.getName();

			if (map.containsKey(key)) {
				Object value = map.get(key);
				Method setter = property.getWriteMethod();
				setter.invoke(obj, value);
			}

		}

	}

	public static Map<String, Object> transBean2Map(Object obj)
			throws IntrospectionException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (obj == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo
				.getPropertyDescriptors();
		for (PropertyDescriptor property : propertyDescriptors) {
			String key = property.getName();

			// 过滤class属性
			if (!key.equals("class")) {
				// 得到property对应的getter方法
				Method getter = property.getReadMethod();
				Object value = getter.invoke(obj);

				map.put(key, value);
			}

		}

		return map;

	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String,Object> testMap = new HashMap<String,Object>();
		testMap.put("userName", "haha") ;
		User u= new User();
		transMap2Bean(testMap, u) ;
		System.out.println(u.getUserName());
		
		
	}
	
	
	
}