package com.beirtipol.dates;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Component
public class Converters implements BeanPostProcessor {
	@Autowired
	BeanFactory							beanFactory;

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
						Class<?> from = (Class<?>) attribs.get("from").get(0);
						Class<?> to = (Class<?>) attribs.get("to").get(0);
						ConverterKey key = new ConverterKey(from, to);
						converters.put(key, (Function) bean);
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
		ConverterKey key = new ConverterKey(from.getClass(), to);
		Function converter = converters.get(key);
		if (converter == null) {
			throw new NoSuchBeanDefinitionException(to, String.format("No bean available to convert from %s to %s", from.getClass(), to));
		}
		return (T) converter.apply(from);
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
