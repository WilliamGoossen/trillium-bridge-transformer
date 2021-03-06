package edu.mayo.trilliumbridge.webapp;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

public class AcceptHeaderAdjustingFilter implements Filter {

	@Override
	public void destroy() {
		//
	}

	@Override
	public void doFilter(
			ServletRequest request, 
			ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(! (request instanceof HttpServletRequest)){
			throw new IllegalStateException("ServletRequest expected to be of type HttpServletRequest");
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		@SuppressWarnings("unchecked")
		Map<String, String[]> params = httpRequest.getParameterMap();
		
		if(params.containsKey("format")){
            String type = this.getFormatTypeFromParameter(params);
			
			chain.doFilter(new AcceptTypeChangingRequest(httpRequest, type), response);
		} else {
			chain.doFilter(request, response);
		}
	}

    protected String getFormatTypeFromParameter(Map<String, String[]> params) {
        String[] formats = params.get("format");
        if(formats.length != 1){
            throw new IllegalStateException("Only one 'format' parameter allowed.");
        }

        String format = formats[0];

        String type;

        if(format.equals("html")){
            type = "text/html";
        } else if (format.equals("xml")){
            type = "application/xml";
        } else if (format.equals("pdf")){
            type = "application/pdf";
        } else {
        // not sure what we do here... sending it along and downstream code can
        // try to make sense of it.
            type = "application/" + format;
        }
        return type;
    }

    @Override
	public void init(FilterConfig config) throws ServletException {
		//
	}
	
	public class AcceptTypeChangingRequest extends HttpServletRequestWrapper {

		private String acceptHeader;
		
		public AcceptTypeChangingRequest(HttpServletRequest request, String acceptHeader) {
			super(request);
			this.acceptHeader = acceptHeader;
		}
		
		@SuppressWarnings("rawtypes")
		public Enumeration getHeaders(String name){
			if(name.equalsIgnoreCase("accept")){
				return Collections.enumeration(Arrays.asList(acceptHeader));
			}
			
			return super.getHeaders(name);
		}

		@Override
		public String getHeader(String name) {
			if(name.equalsIgnoreCase("accept")){
				return acceptHeader;
			}
			
			return super.getHeader(name);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Enumeration<String> getHeaderNames() {
			List<String> headers = new ArrayList<String>();
			
			headers.addAll(Collections.list(super.getHeaderNames()));
			headers.add("Accept");
			
			return Collections.enumeration(headers);
		}
		
	}

}
