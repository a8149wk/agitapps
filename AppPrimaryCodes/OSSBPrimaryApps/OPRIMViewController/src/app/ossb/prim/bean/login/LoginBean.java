package app.ossb.prim.bean.login;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.security.auth.Subject;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import weblogic.security.services.Authentication;
import weblogic.security.URLCallbackHandler;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

public class LoginBean {
       private String uname;
       private String pwd;

       public void setUname(String uname) {
             this.uname = uname;
       }

       public String getUname() {
             return uname;
       }

       public void setPwd(String pwd) {
             this.pwd = pwd;
       }

       public String getPwd() {
             return pwd;
       }

       public LoginBean() {
       }

       public String doLogin() {
             String un = uname;
             byte[] pw = pwd.getBytes();
             FacesContext ctx = FacesContext.getCurrentInstance();
             HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
             try {
                    Subject subject = Authentication.login(new URLCallbackHandler(un, pw));
                    weblogic.servlet.security.ServletAuthentication.runAs(subject, request);
                    String loginUrl = "/adfAuthentication?success_url=/faces/main";

                    HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
                    sendForward(request, response, loginUrl);
             } catch (FailedLoginException fle) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Incorrect Username or Password",
                                 "An incorrect Username or Password was specified");
                    ctx.addMessage(null, msg);
             } catch (LoginException le) {
                    reportUnexpectedLoginError("LoginException", le);
             }
             return null;
       }

       private void sendForward(HttpServletRequest request, HttpServletResponse response, String forwardUrl) {
             FacesContext ctx = FacesContext.getCurrentInstance();
             RequestDispatcher dispatcher = request.getRequestDispatcher(forwardUrl);
             try {
                    dispatcher.forward(request, response);
             } catch (ServletException se) {
                    reportUnexpectedLoginError("ServletException", se);
             } catch (IOException ie) {
                    reportUnexpectedLoginError("IOException", ie);
             }
             ctx.responseComplete();
       }

       private void reportUnexpectedLoginError(String errType, Exception e) {
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unexpected error during login",
                           "Unexpected error during login (" + errType + "), please consult logs for detail");
             FacesContext.getCurrentInstance().addMessage(null, msg);
             e.printStackTrace();
       }

       public String onLogout() {
             FacesContext facesContext = FacesContext.getCurrentInstance();
             ExternalContext externalContext = facesContext.getExternalContext();
             String url = externalContext.getRequestContextPath()
                           + "/adfAuthentication?logout=true&end_url=/faces/login";
             try {
                    externalContext.redirect(url);
             } catch (IOException e) {
                    e.printStackTrace();
             }
             facesContext.getResponseComplete();
             return null;
       }
}