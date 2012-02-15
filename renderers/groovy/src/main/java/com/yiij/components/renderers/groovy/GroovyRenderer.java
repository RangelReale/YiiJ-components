package com.yiij.components.renderers.groovy;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.yiij.base.interfaces.IContext;
import com.yiij.web.BaseController;
import com.yiij.web.WebApplicationComponent;
import com.yiij.web.interfaces.IApplicationViewRenderer;

public class GroovyRenderer extends WebApplicationComponent implements IApplicationViewRenderer
{
	public GroovyRenderer(IContext context)
	{
		super(context);
	}

	@Override
	public String getFileExtension()
	{
		return ".groovy";
	}

	@Override
	public String renderFile(BaseController controller, String file,
			Object data, boolean doReturn) throws IOException
	{
		SimpleTemplateEngine engine = new SimpleTemplateEngine();
		
		Map<String, Object> makedata = new HashMap<String, Object>();
		makedata.put("this", controller);
		makedata.put("data", data);
		
		Writable template = engine.createTemplate(new BufferedReader(new InputStreamReader(Object.class.getResourceAsStream(file)))).
				make(makedata);
		
		return template.toString();
	}
}