package org.restexpress.domain.ex;
 

/**
 * 服务返回的封装--为了兼容之前的接口和亿迅的共用基类字段一样
 * @author hanst
 *
 */
public class ServerResponse {
	public static final int SUCCESS_CODE = 0;
	
	public static ServerResponse SUCCESS_RESPONSE = new ServerResponse(0,null);
	
	public static ServerResponse INTERNAL_ERROR = new ServerResponse(500,"服务内部错误");
	
	public static ServerResponse NOT_AUTH_ERROR = new ServerResponse(401,"无权限");
	
	public static ServerResponse FORBIDDEN_ERROR = new ServerResponse(403,"禁止访问");
	
	public static ServerResponse REQUEST_FEQ_ERROR = new ServerResponse(1,"重复提交");
	
	//其他错误码,可用httpmethod httpstatus的代码
	
	private int code;
	   
	private String result;
	  
	private String statusText;
	
	public ServerResponse(){
		
	}
	
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
