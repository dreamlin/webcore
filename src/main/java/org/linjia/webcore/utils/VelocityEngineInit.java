package org.linjia.webcore.utils;

import java.util.Properties;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

public class VelocityEngineInit {

	private volatile static VelocityEngineInit _MeObj;
	private VelocityEngine _engine;

	public static VelocityEngine getEngine() {
		return _MeObj._engine;
	}

	public static void create(String path) {
		if (_MeObj == null) {
			synchronized (VelocityEngineInit.class) {
				if (_MeObj == null) {
					try {
						_MeObj = new VelocityEngineInit(path);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public VelocityEngineInit(String path) throws Exception {
		_engine = new VelocityEngine();// 引擎初始化
		Properties properties = new Properties();
		properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, path);
		properties.setProperty(Velocity.INPUT_ENCODING, "utf8");
		properties.setProperty(Velocity.OUTPUT_ENCODING, "utf8");
		_engine.init(properties);// 装入初始化信息
	}
}
