package org.restexpress.domain.ex;

/**
 * 服务返回的封装--为了兼容之前的接口和亿迅的共用基类字段一样
 * @author hanst
 *
 */
public class ServerResponse {
	public static final int SUCCESS_CODE = 0;
	//其他错误码,可用httpmethod httpstatus的代码
	
	private int code;
	   
	private String result;
	  
	private String statusText;
	
	public ServerResponse(int status,String statusText,String result){
		this.setCode(status);
		this.statusText = statusText;
		this.result = result;
	}
	
	public ServerResponse(int status,String statusText){
		this.setCode(status);
		this.statusText = statusText;
	}

	 

	public java.lang.String getResult() {
		return result;
	}

	public void setResult(java.lang.String result) {
		this.result = result;
	}

	public java.lang.String getStatusText() {
		return statusText;
	}

	public void setStatusText(java.lang.String statusText) {
		this.statusText = statusText;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
