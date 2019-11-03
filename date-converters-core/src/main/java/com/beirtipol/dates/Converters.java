package com.beirtipol.dates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.beirtipol.dates.converter.LocalDateConverters;
import com.beirtipol.dates.converter.LocalDateTimeConverters;
import com.beirtipol.dates.converter.UtilDateConverters;
import com.beirtipol.dates.converter.XMLDateConverters;
import com.beirtipol.dates.converter.ZonedDateTimeConverters;

/**
 * {@link Converters} will gather all bean methods which declare the annotation {@link Converter} and index them by the
 * 'from' and 'to' types declared in the annotation. It has a single useful method
 * {@link Converters#from(Object, Class)} which allows passing in any {@link Object} along with the desired return type
 * {@link Class} and will search for an appropriate {@link Converter} to apply the conversion.
 * 
 * A number of core {@link Converter} beans are provided by this project
 * 
 * @see LocalDateConverters
 * @see LocalDateTimeConverters
 * @see ZonedDateTimeConverters
 * @see UtilDateConverters
 * @see XMLDateConverters
 * 
 * @author beirtipol@gmail.com
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@SpringBootConfiguration
@ComponentScan
@Component
public class Converters implements BeanPostProcessor {
	private static final Logger			LOG			= LoggerFactory.getLogger(Converters.class);
	@Autowired
	private BeanFactory					beanFactory;

	private Map<ConverterKey, Function>	converters	= new HashMap<>();

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		if (registry.containsBeanDefinition(beanName)) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
			if (beanDefinition instanceof AnnotatedBeanDefinition) {
				if (beanDefinition.getSource() instanceof MethodMetadata) {
					MethodMetadata beanMethod = (MethodMetadata) beanDefinition.getSource();
					String annotationType = Converter.class.getName();
					if (beanMethod.isAnnotated(annotationType)) {
						MultiValueMap<String, Object> attribs = beanMethod.getAllAnnotationAttributes(annotationType);
						Class<?>[] froms = (Class<?>[]) attribs.get("from").get(0);
						Class<?> to = (Class<?>) attribs.get("to").get(0);
						Arrays.stream(froms).forEach(from -> {
							ConverterKey key = new ConverterKey(from, to);
							converters.put(key, (Function) bean);
						});
					}
				}
			}
		}

		return bean;
	}

	public <T> T from(Object from, Class<T> to) {
		if (from == null) {
			return null;
		}

		// In the case of java.util.Calendar, we may not have created converters for all types.
		Class<?> fromClass = from.getClass();
		Function converter = getConverter(fromClass, to);
		while (converter == null && fromClass.getSuperclass() != Object.class) {
			fromClass = fromClass.getSuperclass();
			converter = getConverter(fromClass, to);
		}
		if (converter == null) {
			throw new NoSuchBeanDefinitionException(to, String.format("No bean available to convert from %s to %s", from.getClass(), to));
		}
		if (fromClass != from.getClass()) {
//			LOG.warn(String.format("No direct converter found between %s and %s. Attempting to convert from %s to %s instead.", from.getClass(), to, fromClass, to));
		}
		return (T) converter.apply(from);
	}

	private <T> Function getConverter(Class<?> from, Class<T> to) {
		Function converter;
		ConverterKey key = new ConverterKey(from, to);
		converter = converters.get(key);
		return converter;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
