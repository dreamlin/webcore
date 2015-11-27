# webcore
Java：一套简单的MVC框架，使用Velocity模板引擎。每个Action处理程序可以与Web环境解耦。支持UrlRewrite

**HttpAction：**

	public class SignInPostAction implements IHttpAction {

	private SignInPostBean	httpBean;
	public void doProcess(RequestContext requestCxt,ResponseContext responseCxt) {
		boolean boolLogin = MemberService.adminAuthenticate(httpBean.getAdminId(),
				httpBean.getPassword());
		if (!boolLogin) {
			responseCxt.setSendRedirectUrl("/SignIn.action");
			return;
		}
		responseCxt.setSendRedirectUrl("/AdminHome.action");
	}

	}

**HttpBean：（Bean可有可无）**

	public class SignInPostBean implements IHttpBean{

	private String adminId;
	private String password;
	
	public SignInPostBean(){
		
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ValidateError validate() {
		return null;
	}	
	}
