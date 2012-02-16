package com.yiij.components.renderers.yiij_render_jmte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.floreysoft.jmte.DefaultModelAdaptor;
import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.Processor;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.token.Token;
import com.yiij.base.interfaces.IContext;
import com.yiij.web.BaseController;
import com.yiij.web.WebApplicationComponent;
import com.yiij.web.interfaces.IApplicationViewRenderer;

public class JTMERenderer extends WebApplicationComponent implements
		IApplicationViewRenderer
{
	public JTMERenderer(IContext context)
	{
		super(context);
	}

	@Override
	public String getFileExtension()
	{
		return ".mte";
	}

	/**
	 * Initialize the JTME engine, add the data, and parse.
	 * @return the parsed template
	 */
	@Override
	public String renderFile(BaseController controller, String file,
			Object data, boolean doReturn) throws IOException
	{
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("this", controller);
		model.put("data", data);
		
		Engine engine = new Engine();
		engine.registerNamedRenderer(new HtmlEncodeRenderer());
		engine.setModelAdaptor(new YiiJModelAdaptor());
		if (doReturn)
			return engine.transform(readTextFile(file), model);
		webApp().getResponse().getWriter().print(engine.transform(readTextFile(file), model));
		return null;
	}

	private String readTextFile(String file) throws IOException {
	    StringBuilder result = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(Object.class.getResourceAsStream(file)));
	    try {

	        char[] buf = new char[1024];

	        int r = 0;

	        while ((r = reader.read(buf)) != -1) {
	            result.append(buf, 0, r);
	        }
	    }
	    finally {
	        reader.close();
	    }

	    return result.toString();     
	}
	
	/**
	 * Renderer for html encoding.
	 * Syntax: ${variable;htmlencoding}
	 * @author Rangel Reale
	 */
	private static class HtmlEncodeRenderer implements NamedRenderer
	{
		@Override
		public String render(Object o, String format) {
			return StringEscapeUtils.escapeHtml(o.toString());
		}

		@Override
		public String getName() {
			return "htmlencode";
		}

		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}

		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { Object.class };
		}
		
	}
	
	/**
	 * Interface to add callable objects in layout.
	 * Syntax: ${callable.method}, the callable object will receive the method name.
	 * @author Rangel Reale
	 */
	public static interface MethodCallable
	{
		Object call(TemplateContext context, String method, Token token);
	}

	/**
	 * Helper object to give context to a callable.
	 * @author Rangel Reale
	 */
	private static class MethodCallableCall implements Processor<Object>
	{
		private MethodCallable _callable;
		private String _method;
		private Token _token;
		
		public MethodCallableCall(MethodCallable callable, String method, Token token)
		{
			super();
			_callable = callable;
			_method = method;
			_token = token;
			
		}
		
		@Override
		public Object eval(TemplateContext context)
		{
			return _callable.call(context, _method, _token);
		}
	}
	
	/**
	 * Custom model adaptor for YiiJ.
	 * Allows the processing of {@link MethodCallable} objects to do custom programming
	 * @author Rangel Reale
	 */
	private static class YiiJModelAdaptor extends DefaultModelAdaptor
	{
		protected Object nextStep(Object o, String attributeName,
				ErrorHandler errorHandler, Token token) {
			if (o instanceof MethodCallable)
			{
				return new MethodCallableCall((MethodCallable)o, attributeName, token);
			}
			return super.nextStep(o, attributeName, errorHandler, token);
		}
	}
}
