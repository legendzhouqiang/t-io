package org.tio.http.server.mvc;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.utils.json.Json;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.xiaoleilu.hutool.util.ArrayUtil;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.MethodAnnotationMatchProcessor;
import jodd.io.FileUtil;

/**
 * @author tanyaowu
 * 2017年7月1日 上午9:05:30
 */
public class Routes {
	private static Logger log = LoggerFactory.getLogger(Routes.class);
	//	private HttpConfig httpConfig = null;

	//	private String[] scanPackages = null;

	/**
	 * 格式化成"/user","/"这样的路径
	 * @param initPath
	 * @return
	 * @author tanyaowu
	 */
	private static String formateBeanPath(String initPath) {
		//		if (StringUtils.isBlank(initPath)) {
		//			return "/";
		//		}
		//		initPath = StringUtils.replaceAll(initPath, "//", "/");
		//		if (!StringUtils.startsWith(initPath, "/")) {
		//			initPath = "/" + initPath;
		//		}
		//
		//		if (StringUtils.endsWith(initPath, "/")) {
		//			initPath = initPath.substring(0, initPath.length() - 1);
		//		}
		return initPath;
	}

	private static String formateMethodPath(String initPath) {
		//		if (StringUtils.isBlank(initPath)) {
		//			return "";
		//		}
		//		initPath = StringUtils.replaceAll(initPath, "//", "/");
		//		if (!StringUtils.startsWith(initPath, "/")) {
		//			initPath = "/" + initPath;
		//		}

		return initPath;
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * 路径和对象映射
	 * key: /user
	 * value: object
	 */
	public Map<String, Object> pathBeanMap = new TreeMap<>();
	/**
	 * 路径和class映射
	 * 只是用来打印的
	 * key: /user
	 * value: Class
	 */
	public Map<String, Class<?>> pathClassMap = new TreeMap<>();

	/**
	 * 路径和class映射
	 * key: class
	 * value: /user
	 */
	public Map<Class<?>, String> classPathMap = new HashMap<>();

	/**
	 * Method路径映射
	 * key: /user/update
	 * value: method
	 */
	public Map<String, Method> pathMethodMap = new TreeMap<>();

	/**
	 * Method路径映射
	 * 只是用于打印日志
	 * key: /user/update
	 * value: method string
	 */
	public Map<String, String> pathMethodstrMap = new TreeMap<>();

	/**
	 * 含有路径变量的请求
	 * key: 子路径的个数（pathUnitCount），譬如/user/{userid}就是2
	 * value: VariablePathVo
	 */
	public Map<Integer, VariablePathVo[]> variablePathMap = new TreeMap<>();

	/**
	 * 含有路径变量的请求
	 * 只是用于打印日志
	 * key: 配置的路径/user/{userid}
	 * value: method string
	 */
	public Map<String, String> variablePathMethodstrMap = new TreeMap<>();

	/**
	 * 方法参数名映射
	 * key: method
	 * value: ["id", "name", "scanPackages"]
	 */
	public Map<Method, String[]> methodParamnameMap = new HashMap<>();

	/**
	 * 方法和对象映射
	 * key: method
	 * value: bean
	 */
	public Map<Method, Object> methodBeanMap = new HashMap<>();

	StringBuilder errorStr = new StringBuilder();

	/**
	 * 
	 * @param contextPath
	 * @param suffix
	 * @param scanPackages
	 * @author tanyaowu
	 */
	public Routes(String[] scanPackages) {
		//		this.scanPackages = scanPackages;
		//		if (contextPath == null) {
		//			contextPath = "";
		//		}
		//		this.contextPath = contextPath;
		//		
		//		if (suffix == null) {
		//			suffix = "";
		//		}
		//		this.suffix = suffix;

		if (scanPackages != null) {
			final FastClasspathScanner fastClasspathScanner = new FastClasspathScanner(scanPackages);
			//			fastClasspathScanner.verbose();
			fastClasspathScanner.matchClassesWithAnnotation(RequestPath.class, new ClassAnnotationMatchProcessor() {
				@Override
				public void processMatch(Class<?> classWithAnnotation) {
					try {
						Object bean = classWithAnnotation.newInstance();
						RequestPath mapping = classWithAnnotation.getAnnotation(RequestPath.class);
						//						String beanPath = Routes.this.contextPath + mapping.value();
						String beanPath = mapping.value();
						//						if (!StringUtils.endsWith(beanUrl, "/")) {
						//							beanUrl = beanUrl + "/";
						//						}

						beanPath = formateBeanPath(beanPath);

						Object obj = pathBeanMap.get(beanPath);
						if (obj != null) {
							log.error("mapping[{}] already exists in class [{}]", beanPath, obj.getClass().getName());
							errorStr.append("mapping[" + beanPath + "] already exists in class [" + obj.getClass().getName() + "]\r\n\r\n");
						} else {
							pathBeanMap.put(beanPath, bean);
							pathClassMap.put(beanPath, classWithAnnotation);
							classPathMap.put(classWithAnnotation, beanPath);
						}
					} catch (Throwable e) {

						log.error(e.toString(), e);
					}
				}
			});

			fastClasspathScanner.matchClassesWithMethodAnnotation(RequestPath.class, new MethodAnnotationMatchProcessor() {
				@Override
				public void processMatch(Class<?> matchingClass, Executable matchingMethodOrConstructor) {
					//					log.error(matchingMethodOrConstructor + "");
					RequestPath mapping = matchingMethodOrConstructor.getAnnotation(RequestPath.class);

					String methodName = matchingMethodOrConstructor.getName();

					//					String methodPath = mapping.value() + Routes.this.suffix;
					String methodPath = mapping.value();

					methodPath = formateMethodPath(methodPath);
					String beanPath = classPathMap.get(matchingClass);

					if (StringUtils.isBlank(beanPath)) {
						log.error("方法有注解，但类没注解, method:{}, class:{}", methodName, matchingClass);
						errorStr.append("方法有注解，但类没注解, method:" + methodName + ", class:" + matchingClass + "\r\n\r\n");
						return;
					}

					Object bean = pathBeanMap.get(beanPath);
					String completeMethodPath = methodPath;
					if (beanPath != null) {
						completeMethodPath = beanPath + methodPath;
					}

					Class<?>[] parameterTypes = matchingMethodOrConstructor.getParameterTypes();
					Method method;
					try {
						method = matchingClass.getMethod(methodName, parameterTypes);

						Paranamer paranamer = new BytecodeReadingParanamer();
						String[] parameterNames = paranamer.lookupParameterNames(method, false); // will return null if not found

						Method checkMethod = pathMethodMap.get(completeMethodPath);
						if (checkMethod != null) {
							log.error("mapping[{}] already exists in method [{}]", completeMethodPath, checkMethod.getDeclaringClass() + "#" + checkMethod.getName());
							errorStr.append(
									"mapping[" + completeMethodPath + "] already exists in method [" + checkMethod.getDeclaringClass() + "#" + checkMethod.getName() + "]\r\n\r\n");

							return;
						}

						pathMethodMap.put(completeMethodPath, method);

						pathMethodstrMap.put(completeMethodPath, methodToStr(method, parameterNames));

						methodParamnameMap.put(method, parameterNames);
						methodBeanMap.put(method, bean);
					} catch (Throwable e) {
						log.error(e.toString(), e);
					}
				}
			});

			fastClasspathScanner.scan();

			String pathClassMapStr = Json.toFormatedJson(pathClassMap);
			log.info("class  mapping\r\n{}", pathClassMapStr);
			//			log.info("classPathMap scan result :\r\n {}\r\n", Json.toFormatedJson(classPathMap));
			String pathMethodstrMapStr = Json.toFormatedJson(pathMethodstrMap);
			log.info("method mapping\r\n{}", pathMethodstrMapStr);
			//			log.info("methodParamnameMap scan result :\r\n {}\r\n", Json.toFormatedJson(methodParamnameMap));

			//
			processVariablePath();

			String variablePathMethodstrMapStr = Json.toFormatedJson(variablePathMethodstrMap);
			log.info("variable path mapping\r\n{}", variablePathMethodstrMapStr);

			try {
				FileUtil.writeString("/tio_path_class.json", pathClassMapStr, "utf-8");
				FileUtil.writeString("/tio_path_method.json", pathMethodstrMapStr, "utf-8");
				FileUtil.writeString("/tio_variablepath_method.json", variablePathMethodstrMapStr, "utf-8");

				if (errorStr.length() > 0) {
					FileUtil.writeString("/tio_mvc_error.txt", errorStr.toString(), "utf-8");
				}
			} catch (IOException e) {
				log.error(e.toString(), e);
			}

		}
	}

	/**
	 * 处理有变量的路径
	 * @param pathMethodMap
	 */
	private void processVariablePath() {
		Set<Entry<String, Method>> set = pathMethodMap.entrySet();
		//		Set<String> forRemoved = new HashSet<>();
		for (Entry<String, Method> entry : set) {
			String path = entry.getKey();
			Method method = entry.getValue();
			if (StringUtils.contains(path, "{") && StringUtils.contains(path, "}")) {
				String[] pathUnits = StringUtils.split(path, "/");
				PathUnitVo[] pathUnitVos = new PathUnitVo[pathUnits.length];

				boolean isVarPath = false; //是否是带变量的路径
				for (int i = 0; i < pathUnits.length; i++) {
					PathUnitVo pathUnitVo = new PathUnitVo();
					String pathUnit = pathUnits[i];
					if (StringUtils.contains(pathUnit, "{") || StringUtils.contains(pathUnit, "}")) {
						if (StringUtils.startsWith(pathUnit, "{") && StringUtils.endsWith(pathUnit, "}")) {
							String[] xx = methodParamnameMap.get(method);
							String varName = StringUtils.substringBetween(pathUnit, "{", "}");
							if (ArrayUtil.contains(xx, varName)) {
								isVarPath = true;
								pathUnitVo.setVar(true);
								pathUnitVo.setPath(varName);
							} else {
								log.error("path:{}, 对应的方法中并没有包含参数名为{}的参数", path, varName);
								errorStr.append("path:{" + path + "}, 对应的方法中并没有包含参数名为" + varName + "的参数\r\n\r\n");
							}
						} else {
							pathUnitVo.setVar(false);
							pathUnitVo.setPath(pathUnit);
						}
					} else {
						pathUnitVo.setVar(false);
						pathUnitVo.setPath(pathUnit);
					}
					pathUnitVos[i] = pathUnitVo;
				}

				if (isVarPath) {
					VariablePathVo variablePathVo = new VariablePathVo(path, method, pathUnitVos);
					addVariablePathVo(pathUnits.length, variablePathVo);
				}
			}
		}

		//		set.removeAll(forRemoved);
	}

	/**
	 * 
	 * @param pathUnitCount
	 * @param forceCreate
	 * @return
	 */
	@SuppressWarnings("unused")
	private VariablePathVo[] getVariablePathVos(Integer pathUnitCount, boolean forceCreate) {
		VariablePathVo[] ret = variablePathMap.get(pathUnitCount);
		if (forceCreate && ret == null) {
			ret = new VariablePathVo[0];
			variablePathMap.put(pathUnitCount, ret);
		}
		return ret;
	}

	/**
	 * 
	 * @param pathUnitCount
	 * @param variablePathVo
	 */
	private void addVariablePathVo(Integer pathUnitCount, VariablePathVo variablePathVo) {
		VariablePathVo[] existValue = variablePathMap.get(pathUnitCount);
		if (existValue == null) {
			existValue = new VariablePathVo[] { variablePathVo };
			variablePathMap.put(pathUnitCount, existValue);
		} else {
			VariablePathVo[] newExistValue = new VariablePathVo[existValue.length + 1];
			System.arraycopy(existValue, 0, newExistValue, 0, existValue.length);
			newExistValue[newExistValue.length - 1] = variablePathVo;
			variablePathMap.put(pathUnitCount, newExistValue);
		}
		variablePathMethodstrMap.put(variablePathVo.getPath(), methodToStr(variablePathVo.getMethod(), methodParamnameMap.get(variablePathVo.getMethod())));
		//org.tio.http.server.mvc.Routes.methodParamnameMap
	}

	private String methodToStr(Method method, String[] parameterNames) {
		return method.getDeclaringClass().getName() + "." + method.getName() + "(" + ArrayUtil.join(parameterNames, ",") + ")";
		//		matchingClass.getName() + "." + method.getName() + "(" + ArrayUtil.join(parameterNames, ",") + ")"
	}

	@SuppressWarnings("unused")
	public Method getMethodByPath(String path, HttpRequest request) {
		Method method = pathMethodMap.get(path);
		if (method == null) {
			String[] pathUnitsOfRequest = StringUtils.split(path, "/");
			VariablePathVo[] variablePathVos = variablePathMap.get(pathUnitsOfRequest.length);
			if (variablePathVos != null) {
				tag1: for (VariablePathVo variablePathVo : variablePathVos) {
					PathUnitVo[] pathUnitVos = variablePathVo.getPathUnits();
					tag2: for (int i = 0; i < pathUnitVos.length; i++) {
						PathUnitVo pathUnitVo = pathUnitVos[i];
						String pathUnitOfRequest = pathUnitsOfRequest[i];

						if (pathUnitVo.isVar()) {
							request.addParam(pathUnitVo.getPath(), pathUnitOfRequest);
						} else {
							if (!StringUtils.equals(pathUnitVo.getPath(), pathUnitOfRequest)) {
								break tag2;
							}
						}
					}
					method = variablePathVo.getMethod();
					return method;
				}
			}
			return null;
		} else {
			return method;
		}
	}

	//	public String getSuffix() {
	//		return suffix;
	//	}
	//
	//	public void setSuffix(String suffix) {
	//		this.suffix = suffix;
	//	}
}
